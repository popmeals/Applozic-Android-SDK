package com.applozic.mobicomkit.api.attachment;

import android.content.Context;
import android.text.TextUtils;

import com.applozic.mobicommons.task.AlAsyncTask;

import java.lang.ref.WeakReference;

/**
 * This task downloads and saves the <i>GIF</i> file from the given URL. It returns the local path where the downloaded GIF is saved.
 *
 * <p>Created for async execution of {@link FileClientService#downloadGif(String)}.</p>
 *
 * <code>
 *     GifDownloadAsyncTask gifDownloadAsyncTask = new GifDownloadAsyncTask(context, gifUrl, new GifDownloadCallback() {
 *             @Override
 *             public void onGifDownloaded(String localPath) { }
 *
 *             @Override
 *             public void onFailed() { }
 *         });
 *         AlTask.execute(gifDownloadAsyncTask);
 *
 *         //for versions prior to v5.95 use:
 *         //gifDownloadAsyncTask.execute();
 * </code>
 */
public class GifDownloadAsyncTask extends AlAsyncTask<Void, String> {
    private final WeakReference<Context> contextWeakReference;
    private final String url;
    private final GifDownloadCallback gifDownloadCallback;

    public GifDownloadAsyncTask(Context context, String url, GifDownloadCallback gifDownloadCallback) {
        contextWeakReference = new WeakReference<>(context);
        this.url = url;
        this.gifDownloadCallback = gifDownloadCallback;
    }

    @Override
    protected String doInBackground() {
        return new FileClientService(contextWeakReference.get()).downloadGif(url);
    }

    @Override
    protected void onPostExecute(String localPath) {
        super.onPostExecute(localPath);
        if (gifDownloadCallback == null) {
            return;
        }

        if (TextUtils.isEmpty(localPath)) {
            gifDownloadCallback.onFailed();
        } else {
            gifDownloadCallback.onGifDownloaded(localPath);
        }
    }

    /**
     * Callback for GIF download success/failure.
     */
    public interface GifDownloadCallback {
        /**
         * To be called on success.
         *
         * @param localPath the path to the downloaded gif
         */
        void onGifDownloaded(String localPath);

        /**
         * To be called in-case of failure.
         */
        void onFailed();
    }
}
