package br.com.wedocode.shopping.business.jdbc.util;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import br.com.wedocode.framework.commons.concurrent.ScheduledExecutor;
import br.com.wedocode.framework.commons.function.ThrowingSupplier;
import br.com.wedocode.framework.commons.util.Promise;

public class ReentrantReadWriteLockExt extends ReentrantReadWriteLock {

    private static final long serialVersionUID = 3914865895425004937L;

    private final ScheduledExecutor executor;

    public ReentrantReadWriteLockExt(ScheduledExecutor executor) {
        this.executor = executor;
    }

    public <T> Promise<T> readLockPromise(ThrowingSupplier<T> callback) {
        return new Promise<>((resolve, reject) -> {
            executor.execute(() -> {
                try {
                    this.readLock().lock();
                    try {
                        resolve.accept(callback.getThrows());
                    } finally {
                        this.readLock().unlock();
                    }
                } catch (Throwable caught) {
                    reject.accept(caught);
                }
            });
        });
    }

    public <T> Promise<T> writeLockPromise(ThrowingSupplier<T> callback) {
        return new Promise<>((resolve, reject) -> {
            executor.execute(() -> {
                try {
                    this.writeLock().lock();
                    try {
                        resolve.accept(callback.getThrows());
                        ;
                    } finally {
                        this.writeLock().unlock();
                    }
                } catch (Throwable caught) {
                    reject.accept(caught);
                }
            });
        });
    }

}
