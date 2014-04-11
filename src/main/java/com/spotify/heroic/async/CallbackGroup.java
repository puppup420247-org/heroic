package com.spotify.heroic.async;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CallbackGroup<T> implements Callback.Cancelled {
    public static interface Handle<T> {
        void done(Collection<T> results, Collection<Throwable> errors,
                Collection<CancelReason> cancelled) throws Exception;
    }

    private final AtomicInteger countdown;
    private final List<Callback<T>> callbacks;
    private final Handle<T> handle;
    private volatile boolean done = false;

    private final Queue<Throwable> errors = new ConcurrentLinkedQueue<Throwable>();
    private final Queue<T> results = new ConcurrentLinkedQueue<T>();
    private final Queue<CancelReason> cancelled = new ConcurrentLinkedQueue<CancelReason>();

    private final Callback.Handle<T> listener = new Callback.Handle<T>() {
        @Override
        public void error(Throwable e) throws Exception {
            errors.add(e);
            CallbackGroup.this.check();
        }

        @Override
        public void finish(T result) throws Exception {
            results.add(result);
            CallbackGroup.this.check();
        }

        @Override
        public void cancel(CancelReason reason) throws Exception {
            cancelled.add(reason);
            CallbackGroup.this.check();
        }
    };

    public CallbackGroup(Collection<Callback<T>> callbacks, Handle<T> handle) {
        this.countdown = new AtomicInteger(callbacks.size());
        this.callbacks = new ArrayList<Callback<T>>(callbacks);
        this.handle = handle;
        this.done = false;

        for (final Callback<T> callback : callbacks)
            callback.register(listener);

        if (callbacks.isEmpty())
            end();
    }

    private void check() {
        final int value = countdown.decrementAndGet();

        if (value != 0)
            return;

        end();
    }

    private synchronized void end() {
        if (done)
            return;

        trigger(handle);

        done = true;
    }

    private void trigger(Handle<T> handle) {
        try {
            handle.done(results, errors, cancelled);
        } catch (final Exception e) {
            log.error("Failed to call handler", e);
        }
    }

    /* cancel all queries in this group */
    @Override
    public void cancel(CancelReason reason) {
        log.warn("Cancelling");

        for (final Callback<T> callback : callbacks) {
            callback.cancel(reason);
        }
    }
}
