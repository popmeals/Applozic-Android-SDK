package com.applozic.mobicommons.task;

import androidx.annotation.Nullable;

import com.applozic.mobicommons.task.executor.ExecutorAsyncTask;

/**
 * Executes code in {@link BaseAsyncTask#doInBackground()} synchronously and asynchronously.
 */
public class AlAsyncTask<Progress, Result> extends ExecutorAsyncTask<Progress, Result> {
    /**
     * Execute in a new background thread.
     */
    public void executeAsync(@Nullable AsyncListener<Result> asyncListener) {
        execute(asyncListener);
    }

    /**
     * Execute in calling thread.
     */
    public @Nullable Result executeSync() {
        try {
            return doInBackground();
        } catch (Exception exception) {
            return null;
        }
    }
}
