package com.applozic.mobicommons.task.executor;

import android.os.Binder;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.applozic.mobicommons.task.BaseAsyncTask;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * this implementation of the {@link BaseAsyncTask} uses {@link ExecutorService}, {@link Future} and {@link Handler}
 * this is very similar to the now deprecated {@link android.os.AsyncTask}, the source code was continuously referenced

 * @author shubham tewari
 */
public abstract class ExecutorAsyncTask<Progress, Result> extends BaseAsyncTask<Progress, Result> {
    private static final String TAG = "ExecutorAsyncTask";

    private final @NonNull Executor executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
    private final @NonNull Handler handler = new Handler(Looper.getMainLooper());
    FutureTask<Result> future;

    private final AtomicBoolean cancelled = new AtomicBoolean();
    private final AtomicBoolean taskInvoked = new AtomicBoolean();
    private Status status = Status.PENDING;

    public Status getStatus() {
        return status;
    }

    public boolean isCancelled() {
        return cancelled.get();
    }

    WorkerRunnable<Result> worker = new WorkerRunnable<Result>() {
        @Override
        public Result call() throws Exception {
            taskInvoked.set(true);
            Result result = null;
            try {
                result = doInBackground();
                Binder.flushPendingCommands();
            } catch (Throwable t) {
                cancelled.set(true);
                if (asyncListener != null) {
                    asyncListener.onFailed(t);
                }
                throw t;
            } finally {
                status = Status.FINISHED;
                postResult(result, asyncListener);
            }
            return result;
        }
    };

    @Override
    public void execute(@Nullable AsyncListener<Result> asyncListener) {
        if (status != Status.PENDING) {
            switch (status) {
                case RUNNING:
                    throw new IllegalStateException("Cannot execute task:"
                            + " the task is already running.");
                case FINISHED:
                    throw new IllegalStateException("Cannot execute task:"
                            + " the task has already been executed "
                            + "(a task can be executed only once)");
            }
        }

        onPreExecute();
        status = Status.RUNNING;
        executeTask(asyncListener);
    }

    private void executeTask(@Nullable final AsyncListener<Result> asyncListener) {
        if (asyncListener != null) {
            worker.asyncListener = asyncListener;
        }
        future = new FutureTask<Result>(worker) {
            @Override
            protected void done() {
                try {
                    Result result = get();
                    postResultIfNotInvoked(result);
                    if (asyncListener != null) {
                        asyncListener.onComplete(result);
                    }
                } catch (InterruptedException e) {
                    android.util.Log.w(TAG, e);
                    if (asyncListener != null) {
                        asyncListener.onFailed(e.getCause());
                    }
                } catch (ExecutionException e) {
                    throw new RuntimeException("An error occurred while executing doInBackground()", e.getCause());
                } catch (CancellationException e) {
                    postResultIfNotInvoked(null);
                    if (asyncListener != null) {
                        asyncListener.onFailed(new Throwable("Task cancelled."));
                    }
                }
            }
        };
        executor.execute(future);
    }

    private void postResult(final Result result, final AsyncListener<Result> asyncListener) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(!isCancelled()) {
                    onPostExecute(result);
                    if (asyncListener != null) {
                        asyncListener.onComplete(result);
                    }
                } else {
                    onCancelled();
                    if (asyncListener != null) {
                        asyncListener.onFailed(new Throwable("Task cancelled."));
                    }
                }
            }
        });
    }

    private void postResultIfNotInvoked(Result result) {
        final boolean wasTaskInvoked = taskInvoked.get();
        if (!wasTaskInvoked) {
            postResult(result, null);
        }
    }

    @Override
    protected void publishProgress(final Progress progress) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                onProgress(progress);
            }
        });
    }

    public void cancel(boolean mayInterruptIfRunning) {
        cancelled.set(true);
        future.cancel(mayInterruptIfRunning);
    }

    public final Result get() throws InterruptedException, ExecutionException {
        return future.get();
    }

    private static abstract class WorkerRunnable<Result> implements Callable<Result> {
        AsyncListener<Result> asyncListener;
    }

    public enum Status {
        /**
         * Indicates that the task has not been executed yet.
         */
        PENDING,
        /**
         * Indicates that the task is running.
         */
        RUNNING,
        /**
         * Indicates that {@link BaseAsyncTask#onPostExecute(Object)} has finished.
         */
        FINISHED,
    }
}
