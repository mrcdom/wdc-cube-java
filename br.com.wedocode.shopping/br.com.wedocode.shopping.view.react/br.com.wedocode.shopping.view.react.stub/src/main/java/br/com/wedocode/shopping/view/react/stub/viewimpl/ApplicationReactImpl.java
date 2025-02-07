package br.com.wedocode.shopping.view.react.stub.viewimpl;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.stream.JsonWriter;

import br.com.wedocode.framework.commons.function.ThrowingRunnable;
import br.com.wedocode.framework.commons.util.Base62;
import br.com.wedocode.framework.commons.util.CoerceUtils;
import br.com.wedocode.framework.commons.util.Promise;
import br.com.wedocode.framework.commons.util.Rethrow;
import br.com.wedocode.framework.commons.util.StringUtils;
import br.com.wedocode.framework.webflow.WebFlowIntent;
import br.com.wedocode.shopping.presentation.ShoppingApplication;
import br.com.wedocode.shopping.presentation.presenter.RootPresenter;
import br.com.wedocode.shopping.presentation.presenter.nonrestricted.LoginPresenter;
import br.com.wedocode.shopping.presentation.presenter.restricted.CartPresenter;
import br.com.wedocode.shopping.presentation.presenter.restricted.ProductPresenter;
import br.com.wedocode.shopping.presentation.presenter.restricted.ReceiptPresenter;
import br.com.wedocode.shopping.presentation.presenter.restricted.RestrictedPresenter;
import br.com.wedocode.shopping.view.react.stub.util.AppSecurity;
import br.com.wedocode.shopping.view.react.stub.util.DataSecurity;
import br.com.wedocode.shopping.view.react.stub.util.GenericViewImpl;
import br.com.wedocode.shopping.view.react.stub.util.GsonExtensibleObjectOutput;

public class ApplicationReactImpl extends ShoppingApplication {

    private static Logger LOG = LoggerFactory.getLogger(ApplicationReactImpl.class);

    public static Duration DEFAULT_TIME_SPAN = Duration.ofMinutes(3);

    static {
        RootPresenter.createView = RootReactViewImpl::new;
        LoginPresenter.createView = LoginReactViewImpl::new;
        RestrictedPresenter.createView = RestrictedReactViewImpl::new;
        ProductPresenter.createView = ProductReactViewImpl::new;
        CartPresenter.createView = CartReactViewImpl::new;
        ReceiptPresenter.createView = ReceiptReactViewImpl::new;
    }

    private static ConcurrentHashMap<String, ApplicationReactImpl> instanceMap = new ConcurrentHashMap<>();

    public static ApplicationReactImpl get(String appId) {
        if (StringUtils.isBlank(appId)) {
            return null;
        }
        return instanceMap.get(appId);
    }

    public static ApplicationReactImpl getOrCreate(String appId, String path) {
        var request = new HashMap<String, Object>();

        {
            var browserViewState = new HashMap<String, Object>();
            browserViewState.put("p.path", path);

            request.put(BrowserReactViewImpl.VSID, browserViewState);
        }

        return ApplicationReactImpl.getOrCreate(appId, request);
    }

    public static ApplicationReactImpl getOrCreate(String appId, Map<String, Object> request) {
        return instanceMap.computeIfAbsent(appId,
                sessingId -> ApplicationReactImpl.createApp(appId, request));
    }

    private static ApplicationReactImpl createApp(String appId, Map<String, Object> request) {
        var app = new ApplicationReactImpl(appId);
        try {
            app.addReleaseAction(() -> instanceMap.remove(appId));

            String path = app.getFragment();
            {
                @SuppressWarnings("unchecked")
                Map<String, Object> browserViewState = (Map<String, Object>) request
                        .get(BrowserReactViewImpl.VSID);
                if (browserViewState != null) {
                    path = (String) browserViewState.get("p.path");
                    if (StringUtils.isBlank(path)) {
                        path = app.getFragment();
                    }
                }
            }

            app.safeGo(path);
        } catch (Exception caught) {
            app.release();
            return Rethrow.emit(caught);
        }
        return app;
    }

    public static ApplicationReactImpl remove(String sessionId) {
        return instanceMap.remove(sessionId);
    }

    public static void removeExpireds() {
        var now = System.currentTimeMillis();

        var appItertor = instanceMap.values().iterator();
        while (appItertor.hasNext()) {
            var app = appItertor.next();
            if (app.expireMoment < now) {
                if (app.wsSession == null) {
                    app.release();
                } else {
                    app.extendLife();
                }
            }
        }
    }

    public ApplicationReactImpl(String id) {
        this.id = id;
        this.removeInstanceAction = ThrowingRunnable.noop();
        this.postConstruct();
    }

    // :: Instance

    private final String id;
    private long expireMoment;

    private DataSecurity dataSecurity;
    private transient Session wsSession;
    private ConcurrentHashMap<String, Object> attributes = new ConcurrentHashMap<>();

    private ThrowingRunnable removeInstanceAction;

    private RootPresenter rootPresenter;
    private final Map<String, GenericViewImpl> dirtyViewMap = new LinkedHashMap<>();
    private final Map<String, GenericViewImpl> viewMap = new HashMap<>();
    private long lastRequestId;
    private boolean historyDirty;
    private BrowserReactViewImpl browserView;
    private int instanceIdGen = 1;

    protected void postConstruct() {
        this.dataSecurity = new DataSecurity();
        this.browserView = new BrowserReactViewImpl(this);
        this.viewMap.put(browserView.getStateId(), browserView);
        this.dirtyViewMap.put(browserView.getStateId(), browserView);
        this.expireMoment = System.currentTimeMillis() + DEFAULT_TIME_SPAN.toMillis();
    }

    @Override
    public void release() {
        try {
            try {
                this.browserView.release();
                this.removeInstanceAction.runThrows();
                this.removeInstanceAction = ThrowingRunnable.noop();
                super.release();
            } finally {
                ApplicationReactImpl.instanceMap.remove(id);
                LOG.info("Application removed: " + this.id);
            }
        } catch (Exception caught) {
            LOG.error("Running removeInstanceAction", caught);
        }
    }

    public void extendLife() {
        this.expireMoment = System.currentTimeMillis() + DEFAULT_TIME_SPAN.toMillis();
    }

    public int nextInstanceId() {
        return this.instanceIdGen++;
    }

    public void addReleaseAction(ThrowingRunnable newAction) {
        var oldAction = this.removeInstanceAction;
        this.removeInstanceAction = () -> {
            newAction.runThrows();
            oldAction.runThrows();
        };
    }

    public boolean isAuthenticated() {
        return this.getSubject() != null;
    }

    public DataSecurity getDataSecurity() {
        return dataSecurity;
    }

    public Session getWsSession() {
        return wsSession;
    }

    public void setWsSession(Session wsSession) {
        this.wsSession = wsSession;
    }

    @Override
    public Object setAttribute(String name, Object value) {
        return attributes.put(name, value);
    }

    @Override
    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }

    @Override
    public Object removeAttribute(String name) {
        return this.attributes.remove(name);
    }

    @Override
    public RootPresenter getRootPresenter() {
        return this.rootPresenter;
    }

    public void setRootPresenter(RootPresenter presenter) {
        this.rootPresenter = presenter;
    }

    public GenericViewImpl getViewInstanceById(String vsid) {
        synchronized (this) {
            return this.viewMap.get(vsid);
        }
    }

    public Map<String, GenericViewImpl> getViewMap() {
        return viewMap;
    }

    @Override
    public void updateHistory() {
        this.historyDirty = true;
    }

    public void doUpdateHistory() {
        if (this.historyDirty) {
            var security = AppSecurity.BEAN;
            var intent = new WebFlowIntent();
            intent.setPlace(lastPlace != null ? lastPlace : this.getRootPlace());
            this.publishParameters(intent);

            var b62 = Base62.BEAN;
            var signature = b62.encodeToString(security.signAsHash(intent.toString().getBytes(StandardCharsets.UTF_8)));
            intent.setParameter("sign", signature);

            this.fragment = intent.toString();
            this.historyDirty = false;
        }
    }

    public Promise<Boolean> safeGo(String path) throws Exception {
        var security = AppSecurity.BEAN;
        var intent = WebFlowIntent.parse(path);
        if (intent.getPlace() == null) {
            intent.setPlace(this.getRootPlace());
        }

        var b62 = Base62.BEAN;

        var actualSignature = String.valueOf(intent.removeParameter("sign"));
        var expectedSignature = b62
                .encodeToString(security.signAsHash(intent.toString().getBytes(StandardCharsets.UTF_8)));

        if (!Objects.equals(actualSignature, expectedSignature)) {
            this.updateHistory();
            intent = this.newPlace();
            if (intent.getPlace() == null) {
                intent.setPlace(this.getRootPlace());
            }
        }
        return this.go(intent);
    }

    public void putView(GenericViewImpl view) {
        this.viewMap.put(view.getStateId(), view);
    }

    public GenericViewImpl removeView(String stateId) {
        this.dirtyViewMap.remove(stateId);
        return this.viewMap.remove(stateId);
    }

    public void markDirty(GenericViewImpl view) {
        this.dirtyViewMap.put(view.getStateId(), view);
    }

    public void updateAllViews() {
        this.viewMap.forEach((k, v) -> this.markDirty(v));
    }

    @Override
    public void alertUnexpectedError(Logger logger, String message, Throwable e) {
        this.browserView.alertUnexpectedError(message, e);
    }

    public Promise<Boolean> beginRequest() throws Exception {
        return Promise.resolve(true);
    }

    public void endRequest() throws Exception {
        // NOOP
    }

    public void sendResponse(Map<String, Object> request, boolean fromFlush) throws Exception {
        var task = Promise.resolve(Boolean.TRUE);
        task.then((__) -> this.beginRequest());
        task.then((__) -> this.dispatchPhase(request));
        task.then((__) -> this.responsePhase(request, fromFlush));
        task.catch_(e -> {
            LOG.error(e.getMessage(), e);
            return Promise.reject(e);
        });
        task.finally_(this::endRequest);
    }

    private synchronized Promise<Boolean> dispatchPhase(final Map<String, Object> request) throws Exception {
        var signature = CoerceUtils.toString(request.get("secret"));
        if (StringUtils.isNotBlank(signature)) {
            this.getDataSecurity().updateSecret(signature);
        }

        // First, update application state
        for (final Map.Entry<String, Object> entry : request.entrySet()) {
            var view = this.viewMap.get(entry.getKey());
            if (view != null) {
                @SuppressWarnings("unchecked")
                var formData = (Map<String, Object>) entry.getValue();
                view.syncClientToServer(formData);
            }
        }

        var eventMissed = false;

        var action = Promise.resolve(Boolean.TRUE);
        {
            @SuppressWarnings("unchecked")
            final List<String> eventList = (List<String>) request.get("event");
            if (eventList != null) {
                final Map<String, Integer> eventMap = new HashMap<>(eventList.size());
                eventList.forEach(eventId -> {
                    final Integer qtde = eventMap.get(eventId);
                    if (qtde == null) {
                        eventMap.put(eventId, 1);
                    } else {
                        eventMap.put(eventId, qtde + 1);
                    }
                });

                for (var eventEntry : eventMap.entrySet()) {
                    // <view-id>:<instance-id>:<event-code>
                    var rawEvent = eventEntry.getKey();

                    var pos = rawEvent.lastIndexOf(':');
                    if (pos != -1) {
                        var vsid = rawEvent.substring(0, pos);
                        var view = this.viewMap.get(vsid);
                        if (view != null) {
                            @SuppressWarnings("unchecked")
                            var formData = (Map<String, Object>) request.get(vsid);
                            if (formData == null) {
                                formData = Collections.emptyMap();
                            }

                            try {
                                var eventCode = Integer.parseInt(rawEvent.substring(pos + 1));
                                action.then(view.submit(eventCode, eventEntry.getValue(), formData));
                            } catch (final RuntimeException e) {
                                this.alertUnexpectedError(LOG, e.getMessage(), e);
                            }

                        } else {
                            eventMissed = true;
                        }
                    }
                }
            }
        }

        if (eventMissed) {
            this.updateAllViews();
        }

        return action;
    }

    private synchronized Promise<Boolean> responsePhase(Map<String, Object> request, boolean fromFlush)
            throws Exception {

        final Long requestId = CoerceUtils.toLong(request.get("requestId"), this.lastRequestId);
        this.lastRequestId = requestId;

        final boolean isPing = fromFlush || CoerceUtils.toBoolean(request.get("ping"), false);
        if (!isPing && !this.historyDirty && (this.dirtyViewMap.size() == 0)) {
            return Promise.resolve(Boolean.FALSE);
        }

        this.presenterMap.forEach((k, presenter) -> {
            try {
                presenter.commitComputedState();
            } catch (Exception cause) {
                LOG.error("presenter.commitComputedState()", cause);
            }
        });

        this.doUpdateHistory();

        // Response
        var strWriter = new StringWriter();
        var json = new GsonExtensibleObjectOutput(new JsonWriter(strWriter));
        json.beginObject();
        {
            json.name("requestId").value(requestId);

            if (isPing) {
                json.name("ping").value(true);
            }

            json.name("uri").value(this.getFragment());

            if (this.dirtyViewMap.size() > 0) {
                json.name("states");
                json.beginArray();

                var dirtyViews = this.dirtyViewMap.values().iterator();

                while (dirtyViews.hasNext()) {
                    var view = dirtyViews.next();
                    view.writeState(json);
                    dirtyViews.remove();
                }

                json.endArray();
            }
        }
        json.endObject();
        json.flush();

        var jsonResponse = strWriter.toString();
        this.sendTextToClient(jsonResponse);
        return Promise.resolve(Boolean.TRUE);
    }

    // :: Internal

    protected void sendTextToClient(String text) {
        if (this.wsSession == null) {
            throw new RuntimeException("Missing WebSocket Session");
        }
        this.wsSession.getAsyncRemote().sendText(text);
    }

}
