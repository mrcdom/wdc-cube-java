package br.com.wedocode.framework.webflow;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.wedocode.framework.commons.function.ThrowingConsumer;
import br.com.wedocode.framework.commons.util.Promise;
import br.com.wedocode.framework.commons.util.Rethrow;

public abstract class WebFlowApplication {

    private static final Logger LOG = LoggerFactory.getLogger(WebFlowApplication.class);

    protected ConcurrentHashMap<Integer, WebFlowPresenter> presenterMap;

    protected WebFlowPlace lastPlace;

    protected String fragment;

    public WebFlowApplication() {
        this.presenterMap = new ConcurrentHashMap<>();
    }

    public void release() {
        var presenterIds = new ArrayList<Integer>();
        presenterMap.forEach((id, presenter) -> {
            presenterIds.add(id);
        });

        presenterIds.sort(Comparator.reverseOrder());

        for (var presenterId : presenterIds) {
            var presenter = presenterMap.remove(presenterId);
            if (presenter != null) {
                try {
                    presenter.release();
                } catch (Throwable caught) {
                    LOG.error("releasing " + presenter.getClass(), caught);
                }
            }
        }

        this.presenterMap.clear();
    }

    public String getFragment() {
        return this.fragment;
    }

    public void publishParameters(WebFlowIntent intent) {
        for (var presenter : this.presenterMap.values()) {
            presenter.publishParameters(intent);
        }
    }

    public void commitComputedState() {
        for (var presenter : this.presenterMap.values()) {
            try {
                presenter.commitComputedState();
            } catch (Throwable caught) {
                LOG.error("Processing " + presenter.getClass().getSimpleName(), caught);
            }
        }
    }

    public WebFlowIntent newPlace() {
        var place = new WebFlowIntent();
        place.setPlace(this.lastPlace);
        this.publishParameters(place);
        return place;
    }

    Navigation<?> navigation;

    protected <T extends WebFlowApplication> Navigation<T> navigate() {
        if (this.navigation != null) {
            this.navigation.interrupt();

            if (this.navigation.reflowCount > 10) {
                throw new RuntimeException("Navigation recursion detected");
            }

            var newContext = new Navigation<T>(this);
            newContext.reflowCount = this.navigation.reflowCount + 1;
            newContext = new Navigation<T>(this);
            this.navigation = newContext;
            return newContext;
        } else {
            var newContext = new Navigation<T>(this);
            this.navigation = newContext;
            return newContext;
        }
    }

    // Abstract

    public abstract Object setAttribute(String name, Object value);

    public abstract Object getAttribute(String name);

    public abstract Object removeAttribute(String name);

    public abstract void updateHistory();

    // :: Inner Classes

    public static class Navigation<T extends WebFlowApplication> {

        protected final T app;

        protected final ConcurrentHashMap<Integer, WebFlowPresenter> curPresenterMap;

        protected final ConcurrentHashMap<Integer, WebFlowPresenter> newPresenterMap;

        protected Promise<Boolean> fetchPromise;

        protected int reflowCount;

        private ThrowingConsumer<Boolean> doFetch;

        private WebFlowPlace targetPlace;

        private WebFlowIntent sourceIntent;
        private WebFlowIntent targetIntent;

        private List<PresenterHolder> nextPresenters;
        private boolean notInterruped;

        @SuppressWarnings("unchecked")
        public Navigation(WebFlowApplication app) {
            this.reflowCount = 1;
            this.notInterruped = true;
            this.app = (T) app;
            this.curPresenterMap = app.presenterMap;
            this.newPresenterMap = new ConcurrentHashMap<>();
            this.nextPresenters = new ArrayList<>();

            this.sourceIntent = app.newPlace();

            this.fetchPromise = new Promise<Boolean>((resolve, reject) -> {
                doFetch = resolve;
            });
        }

        public <P extends WebFlowPresenter> Navigation<T> step(WebFlowPlace place, WebFlowIntent intent,
                boolean deepest, Function<T, P> factory) {
            this.targetIntent = intent;

            this.fetchPromise.then(goAhead -> {
                if (!Boolean.TRUE.equals(goAhead)) {
                    return null;
                }

                intent.setPlace(place);

                var holder = new PresenterHolder();
                holder.id = place.getId();
                holder.deepst = deepest;

                @SuppressWarnings("unchecked")
                P presenter = (P) curPresenterMap.get(place.getId());
                if (presenter == null) {
                    presenter = factory.apply(this.app);
                    newPresenterMap.put(place.getId(), presenter);

                    holder.presenter = presenter;
                    holder.initialize = true;
                } else {
                    newPresenterMap.put(place.getId(), presenter);
                    holder.presenter = presenter;
                    holder.initialize = false;
                }

                this.nextPresenters.add(holder);

                return presenter.resolveParameters(intent);
            });

            if (deepest) {
                targetPlace = place;
            }

            return this;
        }

        public Promise<Boolean> promise() {
            var failed = new AtomicReference<Throwable>(null);

            this.fetchPromise
                    //
                    .then(result -> {
                        applyParameters();
                        return Promise.resolve(Boolean.TRUE);
                    })
                    //
                    .catch_(caught -> {
                        failed.set(caught);
                        return Promise.reject(caught);
                    })
                    //
                    .finally_(() -> {
                        if (notInterruped) {
                            if (failed.get() != null) {
                                rollback();
                                Rethrow.emit(failed.get());
                            } else {
                                commit();
                            }
                        }
                    });

            doFetch.accept(Boolean.TRUE);

            return this.fetchPromise;
        }

        private void applyParameters() throws Exception {
            // Make sure that execution is in correct order
            this.nextPresenters.sort((a, b) -> a.id - b.id);

            for (var holder : this.nextPresenters) {
                var goAhread = holder.presenter.applyParameters(this.targetIntent, holder.initialize, holder.deepst);
                if (!goAhread || notInterruped == false) {
                    break;
                }
            }
        }

        public void interrupt() {
            this.notInterruped = false;
            this.newPresenterMap.forEach((id, presenter) -> {
                this.curPresenterMap.put(id, presenter);
            });
        }

        private void rollback() {
            try {
                var presenterIds = new ArrayList<Integer>();

                this.curPresenterMap.forEach((id, presenter) -> {
                    presenterIds.add(id);
                });

                // Sort according to ID to preserve composition order
                presenterIds.sort(Comparator.naturalOrder());

                for (int i = 0, iLast = presenterIds.size() - 1; i <= iLast; i++) {
                    var presenterId = presenterIds.get(i);
                    try {
                        this.newPresenterMap.remove(presenterId);

                        var presenter = this.curPresenterMap.get(presenterId);
                        if (presenter != null) {
                            presenter.applyParameters(this.sourceIntent, false, i == iLast);
                        } else {
                            LOG.warn("Missing presenter for ID=" + presenter);
                        }
                    } catch (Throwable caught) {
                        LOG.error("Restoring source state", caught);
                    }
                }

                if (this.newPresenterMap.size() > 0) {
                    releasePresenters(this.newPresenterMap);
                    this.newPresenterMap.clear();
                }

            } finally {
                this.app.updateHistory();
                this.app.navigation = null;
            }
        }

        private void commit() {
            try {
                // Remove presenters that will be kept
                for (var presenterId : this.newPresenterMap.keySet()) {
                    this.curPresenterMap.remove(presenterId);
                }

                // The remainder must be released
                if (this.curPresenterMap.size() > 0) {
                    releasePresenters(this.curPresenterMap);
                }
            } finally {
                this.app.presenterMap = this.newPresenterMap;
                this.app.lastPlace = this.targetPlace;
                this.app.navigation = null;
                this.app.updateHistory();
            }
        }

        private static void releasePresenters(Map<Integer, WebFlowPresenter> presenterInstanceMap) {
            var presenterIds = new ArrayList<Integer>();
            for (var presenterId : presenterInstanceMap.keySet()) {
                presenterIds.add(presenterId);
            }

            // release on reverse order (deepest level first)
            presenterIds.sort(Comparator.reverseOrder());

            for (var presenterId : presenterIds) {
                var presenter = presenterInstanceMap.remove(presenterId);
                if (presenter != null) {
                    try {
                        presenter.release();
                    } catch (Throwable caught) {
                        LOG.error("Releasing presenter", caught);
                    }
                }
            }
        }
    }

    private static class PresenterHolder {
        Integer id;
        WebFlowPresenter presenter;
        boolean initialize;
        boolean deepst;
    }

}
