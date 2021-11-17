package com.applozic.mobicomkit.api.account.user;

import android.content.Context;

import com.applozic.mobicomkit.feed.ApiResponse;
import com.applozic.mobicomkit.listners.AlCallback;
import com.applozic.mobicommons.task.AlAsyncTask;

import java.lang.ref.WeakReference;

/**
 * This asynchronous task can be used to update user details. The update will happen remotely as well as locally.
 *
 * <p>Created for async execution of {@link UserService#updateUserWithResponse(User)}.
 *
 * <code>
 *     AlUserUpdateTask alUserUpdateTask = new AlUserUpdateTask(context, user, new AlCallback() {
 *             @Override
 *             public void onSuccess(Object response) {
 *                 //((ApiResponse) response).getResponse();
 *             }
 *
 *             @Override
 *             public void onError(Object error) {
 *                 //if (error instanceof List) {
 *                     //((List<ErrorResponseFeed>) error).get(0);
 *                 //}
 *             }
 *         });
 *         AlTask.execute(alUserUpdateTask);
 *
 *         //for versions prior to v5.95 use:
 *         //alUserUpdateTask.execute();
 * </code>
 *
 * Use {@link AlCallback} to get the results.</p>
 */
public class AlUserUpdateTask extends AlAsyncTask<Void, ApiResponse> {
    private WeakReference<Context> context;
    private User user;
    private AlCallback callback;

    /**
     * @param context the context
     * @param user the user object with the details to update. see {@link UserService#updateUserWithResponse(User)}
     * @param callback the callback
     */
    public AlUserUpdateTask(Context context, User user, AlCallback callback) {
        this.context = new WeakReference<>(context);
        this.user = user;
        this.callback = callback;
    }

    @Override
    protected ApiResponse doInBackground() {
        return UserService.getInstance(context.get()).updateUserWithResponse(user);
    }

    @Override
    protected void onPostExecute(ApiResponse apiResponse) {
        super.onPostExecute(apiResponse);
        if (callback != null) {
            if (apiResponse != null) {
                if (apiResponse.isSuccess()) {
                    callback.onSuccess(apiResponse.getResponse());
                } else {
                    callback.onError(apiResponse.getErrorResponse());
                }
            } else {
                callback.onError("error");
            }
        }
    }
}
