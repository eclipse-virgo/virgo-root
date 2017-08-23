package org.eclipse.virgo.nano.core.internal;

import static org.junit.Assert.assertNotNull;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Dummy implementation of the ExecutorService for testing purposes
 *
 * <strong>Concurrent Semantics</strong><br />
 * Thread-safe.
 */
class SyncTaskExecutor implements ExecutorService {
	/**
	 * Executes the given <code>task</code> synchronously, through direct
	 * invocation of it's {@link Runnable#run() run()} method.
	 * @throws IllegalArgumentException if the given <code>task</code> is <code>null</code> 
	 */
	@Override
	public void execute(Runnable task) {
		assertNotNull("Runnable must not be null", task);
		task.run();
	}

    @Override
    public void shutdown() {
        // No-op
    }

    @Override
    public List<Runnable> shutdownNow() {
        // No-op
        return null;
    }

    @Override
    public boolean isShutdown() {
        // No-op
        return false;
    }

    @Override
    public boolean isTerminated() {
        // No-op
        return false;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        // No-op
        return false;
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        // No-op
        return null;
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        // No-op
        return null;
    }

    @Override
    public Future<?> submit(Runnable task) {
        // No-op
        return null;
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        // No-op
        return null;
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        // No-op
        return null;
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        // No-op
        return null;
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException,
        TimeoutException {
        // No-op
        return null;
    }
}