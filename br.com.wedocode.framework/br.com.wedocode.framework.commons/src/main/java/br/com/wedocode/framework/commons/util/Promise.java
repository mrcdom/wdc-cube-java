package br.com.wedocode.framework.commons.util;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.wedocode.framework.commons.function.ThrowingConsumer;
import br.com.wedocode.framework.commons.function.ThrowingFunction;
import br.com.wedocode.framework.commons.function.ThrowingRunnable;

public class Promise<T> {

    private static Logger LOG = LoggerFactory.getLogger(Promise.class);

    /*
     * Class methods
     */

    public static <T> Promise<T> resolve(T value) {
        return new Promise<T>(value, null);
    }

    public static <T> Promise<T> reject(Throwable failure) {
        return new Promise<T>(null, failure);
    }

    /*
     * Instance
     */

    private volatile Object value;

    private volatile Throwable failure;

    private volatile Queue<PendingAction> pendingQueue;

    private volatile boolean solved;

    private Promise(T value, Throwable failure) {
        this.value = value;
        this.failure = failure;
        this.solved = true;
    }

    public Promise(BiConsumer<ThrowingConsumer<T>, ThrowingConsumer<Throwable>> callback) {
        try {
            callback.accept(this::doResolve, this::doReject);
        } catch (Throwable caught) {
            this.doReject(caught);
        }
    }

    //

    /*
     * Instance
     */

    private void doResolve(final Object value) {
        this.value = value;
        this.failure = null;

        Queue<PendingAction> queue;
        synchronized (this) {
            this.solved = true;
            queue = this.pendingQueue;
        }

        if (queue != null) {
            try {
                var action = queue.poll();
                while (action != null) {
                    if (action.resolveCb == null) {
                        if (action.finallyCb != null) {
                            try {
                                action.finallyCb.runThrows();
                            } catch (Throwable caught) {
                                this.value = null;
                                this.failure = caught;
                                doReject(this.failure);
                                return;
                            }
                        }
                        action = queue.poll();
                        continue;
                    }

                    var otherPromise = action.resolveCb.applyThrows(this.value);
                    if (otherPromise != null) {
                        if (!otherPromise.solved) {
                            synchronized (otherPromise) {
                                if (!otherPromise.solved) {
                                    this.importPendingActions(otherPromise);
                                    return;
                                }
                            }
                        }

                        this.value = otherPromise.value;
                        this.failure = otherPromise.failure;
                        if (this.failure != null) {
                            doReject(this.failure);
                            return;
                        }
                    } else {
                        this.value = null;
                    }
                    action = queue.poll();
                }
            } catch (Throwable caught) {
                doReject(caught);
                return;
            }
        }

        doFinally();
    }

    private void doReject(final Throwable failure) {
        this.value = null;
        this.failure = failure;

        Queue<PendingAction> queue;
        synchronized (this) {
            this.solved = true;
            queue = this.pendingQueue;
        }

        if (queue != null) {
            try {
                var action = queue.poll();
                while (action != null) {
                    if (action.rejectCb == null) {
                        if (action.finallyCb != null) {
                            try {
                                action.finallyCb.runThrows();
                            } catch (Throwable caught) {
                                this.value = null;
                                this.failure = caught;
                            }
                        }
                        action = queue.poll();
                        continue;
                    }
                    Promise<Object> otherPromise;
                    try {
                        otherPromise = action.rejectCb.applyThrows(this.failure);
                    } catch (Throwable caught) {
                        if (this.failure != null) {
                            caught.addSuppressed(this.failure);
                        }
                        this.value = null;
                        this.failure = caught;
                        action = queue.poll();
                        continue;
                    }

                    if (otherPromise == null) {
                        this.value = null;
                        this.failure = null;
                        break;
                    }

                    if (!otherPromise.solved) {
                        synchronized (otherPromise) {
                            if (!otherPromise.solved) {
                                this.importPendingActions(otherPromise);
                                return;
                            }
                        }
                    }

                    this.value = otherPromise.value;
                    this.failure = otherPromise.failure;
                    if (this.failure == null) {
                        break;
                    }

                    action = queue.poll();
                }
            } catch (Throwable caught) {
                this.failure = caught;
            }
        }

        doFinally();
    }

    private void doFinally() {
        var queue = this.pendingQueue;
        if (queue != null) {
            var finallyResolve = queue.peek();
            while (finallyResolve != null) {
                if (finallyResolve.rejectCb != null) {
                    // Do pool
                    if (queue.poll() != finallyResolve) {
                        LOG.error("Concorrent modification on queue");
                    }

                    // Next
                    finallyResolve = queue.poll();
                    continue;
                }

                if (finallyResolve.resolveCb != null) {
                    // By breaking we will repeat the process again
                    break;
                }

                // Do pool
                if (queue.poll() != finallyResolve) {
                    LOG.error("Concorrent modification on queue");
                }

                try {
                    finallyResolve.finallyCb.runThrows();
                } catch (Exception caught) {
                    if (this.failure != null) {
                        caught.addSuppressed(caught);
                    }
                    doReject(caught);
                    return;
                }
                finallyResolve = queue.poll();
            }

            if (queue.size() > 0) {
                doResolve(null);
            }
        }
    }

    private void importPendingActions(Promise<? extends Object> otherPromise) {
        // Both promise are not solved
        this.solved = false;
        this.value = null;
        this.failure = null;

        otherPromise.solved = false;
        otherPromise.value = null;
        otherPromise.failure = null;

        // Migrate pending actions to this promise

        if (otherPromise.pendingQueue != null) {
            Queue<PendingAction> newQueue = otherPromise.pendingQueue;

            otherPromise.pendingQueue = null;

            if (this.pendingQueue != null) {
                PendingAction action = this.pendingQueue.poll();
                while (action != null) {
                    newQueue.offer(action);
                    action = this.pendingQueue.poll();
                }

                // Reusing queue
                otherPromise.pendingQueue = this.pendingQueue;
            }

            this.pendingQueue = newQueue;
        }

        if (otherPromise.pendingQueue == null) {
            otherPromise.pendingQueue = new LinkedList<PendingAction>();
        }

        { // When the other promise were resolved, then resolve this one as well
            PendingAction action = new PendingAction();
            action.resolveCb = otherValue -> {
                this.doResolve(otherValue);
                return null;
            };
            otherPromise.pendingQueue.add(action);
        }

        { // When the other promise were reject, then reject this one as well
            PendingAction action = new PendingAction();
            action.rejectCb = otherCaught -> {
                this.doReject(otherCaught);
                return null;
            };
            otherPromise.pendingQueue.add(action);
        }
    }

    /*
     * API
     */

    public <V> Promise<V> then(ThrowingFunction<T, Promise<V>> resolve) {
        return this.then(resolve, null);
    }

    @SuppressWarnings({ "unchecked" })
    public <V> Promise<V> then(Promise<V> promise) {
        if (promise != null) {
            return this.then(discartedValue -> promise);
        }

        return (Promise<V>) this;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <V> Promise<V> then(ThrowingFunction<T, Promise<V>> resolve,
            ThrowingFunction<Throwable, Promise<V>> reject) {
        if (this.solved) {
            return this.thenSolved(resolve, reject);
        }

        synchronized (this) {
            if (!this.solved) {
                if (resolve != null) {
                    if (this.pendingQueue == null) {
                        this.pendingQueue = new LinkedList<>();
                    }
                    var action = new PendingAction();
                    action.resolveCb = (ThrowingFunction) resolve;
                    this.pendingQueue.add(action);
                }

                if (reject != null) {
                    if (this.pendingQueue == null) {
                        this.pendingQueue = new LinkedList<>();
                    }
                    var action = new PendingAction();
                    action.rejectCb = (ThrowingFunction) reject;
                    this.pendingQueue.add(action);
                }

                return (Promise<V>) this;
            }
        }

        return this.thenSolved(resolve, reject);
    }

    public <V> Promise<V> catch_(ThrowingFunction<Throwable, Promise<V>> reject) {
        return this.then(null, reject);
    }

    public Promise<T> finally_(ThrowingRunnable resolve) {
        if (resolve == null) {
            return this;
        }

        if (this.solved) {
            finallyWhenSolved(resolve);
            return this;
        }

        synchronized (this) {
            if (!this.solved) {
                if (resolve != null) {
                    if (this.pendingQueue == null) {
                        this.pendingQueue = new LinkedList<>();
                    }
                    var action = new PendingAction();
                    action.finallyCb = resolve;
                    this.pendingQueue.add(action);
                }
                return this;
            }
        }

        finallyWhenSolved(resolve);
        return this;
    }

    /*
     * Privates
     */

    private void importValuesOrPendingActions(Promise<? extends Object> otherPromise) {
        if (otherPromise.solved) {
            this.value = otherPromise.value;
            this.failure = otherPromise.failure;
            this.solved = true;
            this.pendingQueue = null;
        } else
            synchronized (otherPromise) {
                if (otherPromise.solved) {
                    this.value = otherPromise.value;
                    this.failure = otherPromise.failure;
                    this.solved = true;
                    this.pendingQueue = null;
                } else {
                    synchronized (this) {
                        this.importPendingActions(otherPromise);
                    }
                }
            }
    }

    @SuppressWarnings("unchecked")
    private <V> Promise<V> thenSolved(ThrowingFunction<T, Promise<V>> resolve, Function<Throwable, Promise<V>> reject) {
        var failure = this.failure;
        if (failure != null) {
            if (reject != null) {
                try {
                    var chainPromise = reject.apply(failure);
                    if (chainPromise != null) {
                        importValuesOrPendingActions(chainPromise);
                    } else {
                        synchronized (this) {
                            this.value = null;
                            this.failure = null;
                        }
                    }
                } catch (Throwable caught) {
                    caught.addSuppressed(failure);

                    synchronized (this) {
                        this.value = null;
                        this.failure = caught;
                    }
                }
            }

            return (Promise<V>) this;
        }

        if (resolve != null) {
            try {
                var chainPromise = resolve.applyThrows((T) this.value);
                if (chainPromise != null) {
                    importValuesOrPendingActions(chainPromise);
                } else {
                    synchronized (this) {
                        this.failure = null;
                        this.value = null;
                    }
                }
            } catch (Throwable caught) {
                synchronized (this) {
                    this.failure = caught;
                    this.value = null;
                }

                if (reject != null) {
                    try {
                        var chainPromise = reject.apply(caught);
                        if (chainPromise != null) {
                            importValuesOrPendingActions(chainPromise);
                        }
                    } catch (Throwable unexpected) {
                        unexpected.addSuppressed(caught);

                        synchronized (this) {
                            this.failure = unexpected;
                            this.value = null;
                        }
                    }
                }
            }
        }

        return (Promise<V>) this;
    }

    private void finallyWhenSolved(ThrowingRunnable resolve) {
        try {
            resolve.runThrows();
        } catch (Throwable caught) {
            this.failure = caught;
        }
    }

    private static class PendingAction {

        ThrowingFunction<Object, Promise<Object>> resolveCb;

        ThrowingFunction<Throwable, Promise<Object>> rejectCb;

        ThrowingRunnable finallyCb;

    }
}
