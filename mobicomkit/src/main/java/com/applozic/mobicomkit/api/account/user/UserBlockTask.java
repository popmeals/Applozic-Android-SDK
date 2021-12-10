package com.applozic.mobicomkit.api.account.user;

import android.content.Context;

import com.applozic.mobicomkit.feed.ApiResponse;
import com.applozic.mobicommons.task.AlAsyncTask;
import com.applozic.mobicommons.task.AlTask;

/**
 * An asynchronous task that blocks/unblocks the contact/user with the given userId.
 *
 * <code>
 *     TaskListener taskListener = new TaskListener() {
 *             @Override
 *             public void onSuccess(ApiResponse apiResponse) {
 *                 //apiResponse.getResponse();
 *             }
 *
 *             @Override
 *             public void onFailure(ApiResponse apiResponse, Exception exception) {
 *                 //if (apiResponse != null) {
 *                     //apiResponse.getErrorResponse();
 *                 //} else if (exception != null) {
 *                     //exception.printStackTrace();
 *                 //}
 *             }
 *
 *             @Override
 *             public void onCompletion() { }
 *         };
 *
 *         UserBlockTask userBlockTask = new UserBlockTask(context, taskListener, "userId", true);
 *         AlTask.execute(userBlockTask);
 *
 *         //for versions prior to v5.95 use:
 *         //userBlockTask.execute();
 * </code>
 *
 * <p>This task sends a block user API call and updates the local database on success.
 * Created for async execution of {@link UserService#processUserBlock(String, boolean)}.
 * Use {@link TaskListener} to get the results.</p>
 */
public class UserBlockTask extends AlAsyncTask<Void, Boolean> {
    private final TaskListener taskListener;
    private final Context context;
    private ApiResponse apiResponse;
    private String userId;
    private boolean block;
    private Exception mException;
    private Integer groupId;

    /**
     * @param context the context
     * @param listener the callback
     * @param userId the user-id of the user to block/unblock
     * @param block true to block/false to unblock
     */
    public UserBlockTask(Context context, TaskListener listener, String userId, boolean block) {
        this.context = context;
        this.taskListener = listener;
        this.userId = userId;
        this.block = block;
    }
    @Override
    protected Boolean doInBackground() {
        try {
            apiResponse = UserService.getInstance(context).processUserBlock(userId, block);
            return apiResponse != null && apiResponse.isSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            mException = e;
            return false;
        }
    }

    @Override
    protected void onPostExecute(final Boolean result) {
        if (result && this.taskListener != null) {
            this.taskListener.onSuccess(apiResponse);
        } else if (mException != null && this.taskListener != null) {
            this.taskListener.onFailure(apiResponse, mException);
        }
        this.taskListener.onCompletion();
    }

    public interface TaskListener {

        void onSuccess(ApiResponse apiResponse);

        void onFailure(ApiResponse apiResponse, Exception exception);

        void onCompletion();
    }


}
