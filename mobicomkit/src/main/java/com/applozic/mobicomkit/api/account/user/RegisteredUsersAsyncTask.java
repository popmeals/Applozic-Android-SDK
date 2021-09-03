package com.applozic.mobicomkit.api.account.user;

import android.content.Context;

import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.feed.RegisteredUsersApiResponse;
import com.applozic.mobicommons.task.AlAsyncTask;

/**
 * An asynchronous task that gets a list of users from the server for the given Applozic application.
 *
 * <p>Based on the flag {@link RegisteredUsersAsyncTask#callForRegistered}, it will either:
 * - Fetch all registered users from a particular time.
 * - Fetch currently online users.</p>
 *
 * <p>To retrieve (with pagination) registered users from the backend:
 * <ol>
 *     <li>For the first fetch, call this task with:
 *         <ul>
 *             <li>{@link RegisteredUsersAsyncTask#callForRegistered} = true</li>
 *             <li>{@link RegisteredUsersAsyncTask#lastTimeFetched} = 0L</li>
 *             <li>{@link RegisteredUsersAsyncTask#numberOfUsersToFetch} = <i>[your page size]</i></li>
 *         </ul>
 *     </li>
 *     <li>From the success callback use {@link RegisteredUsersApiResponse#getUsers()} to get the list of users.</li>
 *     <li>Save the last fetch time from {@link RegisteredUsersApiResponse#getLastFetchTime()} somewhere.</li>
 *     <li>For the next fetch, call this task with:
 *         <ul>
 *             <li>{@link RegisteredUsersAsyncTask#callForRegistered} = true</li>
 *             <li>{@link RegisteredUsersAsyncTask#lastTimeFetched} = <i>[the last fetch time you saved earlier]</i></li>
 *             <li>{@link RegisteredUsersAsyncTask#numberOfUsersToFetch} = <i>[your page size]</i></li>
 *         </ul>
 *     </li>
 *     <li>Do not forget to save/replace the fetch time.</li>
 * </ol></p>
 *
 * <code>
 *     TaskListener taskListener = new TaskListener() {
 *             @Override
 *             public void onSuccess(RegisteredUsersApiResponse registeredUsersApiResponse, String[] userIdArray) {
 *                 //use registeredUsersApiResponse.getUsers() for registered users (callForRegistered = true)
 *                 //use userIdArray for online users (callForRegistered = false)
 *             }
 *
 *             @Override
 *             public void onFailure(RegisteredUsersApiResponse registeredUsersApiResponse, String[] userIdArray, Exception exception) {
 *                 //if (exception != null) {
 *                     //exception.printStackTrace();
 *                 //}
 *             }
 *
 *             @Override
 *             public void onCompletion() { }
 *         };
 *
 *         RegisteredUsersAsyncTask registeredUsersAsyncTask = new RegisteredUsersAsyncTask(context, taskListener, 30, 0L, null, null, true);
 *         AlTask.execute(registeredUsersAsyncTask);
 *
 *         //for versions prior to v5.95 use:
 *         //registeredUsersAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
 * </code>
 *
 * @see RegisteredUsersAsyncTask#RegisteredUsersAsyncTask(Context, TaskListener, int, long, Message, String, boolean) for parameter details.
 * @see TaskListener for results.
 */
public class RegisteredUsersAsyncTask extends AlAsyncTask<Void, Boolean> {

    private final TaskListener taskListener;
    Context context;
    int numberOfUsersToFetch;
    UserService userService;
    long lastTimeFetched;
    String[] userIdArray;
    RegisteredUsersApiResponse registeredUsersApiResponse;
    boolean callForRegistered;
    private Exception mException;
    private Message message;
    private String messageContent;

    /**
     * @deprecated Use {@link RegisteredUsersAsyncTask#RegisteredUsersAsyncTask(Context, TaskListener, int, long, Message, String, boolean)}.
     */
    @Deprecated
    public RegisteredUsersAsyncTask(Context context, TaskListener listener, int numberOfUsersToFetch, Message message, String messageContent) {
        this.message = message;
        this.context = context;
        this.taskListener = listener;
        this.messageContent = messageContent;
        this.numberOfUsersToFetch = numberOfUsersToFetch;
        this.userService = UserService.getInstance(context);
    }

    /**
     * @param context the context
     * @param listener for callback
     * @param numberOfUsersToFetch the number of users to fetch from the backend
     * @param lastTimeFetched last time fetch. pass a time if you want all registered users
     * @param message pass NULL
     * @param messageContent pass NULL
     * @param callForRegistered pass true if you want all registered users, false if you only want the current online users
     */
    public RegisteredUsersAsyncTask(Context context, TaskListener listener, int numberOfUsersToFetch, long lastTimeFetched, Message message, String messageContent, boolean callForRegistered) {
        this.callForRegistered = callForRegistered;
        this.message = message;
        this.taskListener = listener;
        this.context = context;
        this.messageContent = messageContent;
        this.numberOfUsersToFetch = numberOfUsersToFetch;
        this.lastTimeFetched = lastTimeFetched;
        this.userService = UserService.getInstance(context);
    }

    @Override
    protected Boolean doInBackground() {
        try {
            if (callForRegistered) {
                registeredUsersApiResponse = userService.getRegisteredUsersList(lastTimeFetched, numberOfUsersToFetch);
            } else {
                userIdArray = userService.getOnlineUsers(numberOfUsersToFetch);
            }
            return registeredUsersApiResponse != null || userIdArray != null;
        } catch (Exception e) {
            e.printStackTrace();
            mException = e;
            return false;
        }
    }

    @Override
    protected void onPostExecute(final Boolean result) {
        if (result && this.taskListener != null) {
            this.taskListener.onSuccess(registeredUsersApiResponse, userIdArray);
        } else if (!result && this.taskListener != null) {
            this.taskListener.onFailure(registeredUsersApiResponse, userIdArray, mException);
        }
        this.taskListener.onCompletion();
    }

    public interface TaskListener {
        /**
         * This is the success callback.
         *
         * @param registeredUsersApiResponse when fetching registered users, use <code>registeredUsersApiResponse</code> as result
         * @param userIdArray when fetching online users, use <code>userIdArray</code> as result
         */
        void onSuccess(RegisteredUsersApiResponse registeredUsersApiResponse, String[] userIdArray);

        /**
         * This is the failure callback.
         *
         * @param registeredUsersApiResponse will be null in most cases
         * @param userIdArray will be null in most cases
         * @param exception the exception object. can be null
         */
        void onFailure(RegisteredUsersApiResponse registeredUsersApiResponse, String[] userIdArray, Exception exception);

        /**
         * This method will be called after the call completes, for both success and failure.
         */
        void onCompletion();
    }


}
