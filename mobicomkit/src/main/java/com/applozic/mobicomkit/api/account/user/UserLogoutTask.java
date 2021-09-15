package com.applozic.mobicomkit.api.account.user;

import android.content.Context;

import com.applozic.mobicomkit.listners.AlLogoutHandler;
import com.applozic.mobicommons.task.AlAsyncTask;

import java.lang.ref.WeakReference;

/**
 * An asynchronous logout task for the current user.
 * <p>Refer to {@link UserClientService#logout()} for details.</p>
 *
 * <code>
 *     UserLogoutTask userLogoutTask = new UserLogoutTask(new AlLogoutHandler() {
 *             @Override
 *             public void onSuccess(Context context) { }
 *
 *             @Override
 *             public void onFailure(Exception exception) { }
 *         }, context.get());
 *
 *         AlTask.execute(userLogoutTask);
 *
 *         //for versions prior to v5.95 use:
 *         //userLogoutTask.execute();
 * </code>
 */
public class UserLogoutTask extends AlAsyncTask<Void, Boolean> {

    private TaskListener taskListener;
    private final WeakReference<Context> context;
    UserClientService userClientService;
    private Exception mException;
    private AlLogoutHandler logoutHandler;

    /**
     * @deprecated Use {@link UserLogoutTask#UserLogoutTask(AlLogoutHandler, Context)} instead.
     */
    public UserLogoutTask(TaskListener listener, Context context) {
        this.taskListener = listener;
        this.context = new WeakReference<Context>(context);
        userClientService = new UserClientService(context);
    }

    /**
     * @param listener success/failure callback
     * @param context the context
     */
    public UserLogoutTask(AlLogoutHandler listener, Context context) {
        this.logoutHandler = listener;
        this.context = new WeakReference<Context>(context);
        userClientService = new UserClientService(context);
    }

    @Override
    protected Boolean doInBackground() {
        try {
            userClientService.logout();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            mException = e;
            return false;
        }
    }

    @Override
    protected void onPostExecute(final Boolean result) {
        if (taskListener != null) {
            if (result) {
                taskListener.onSuccess(context.get());
            } else {
                taskListener.onFailure(mException);
            }
        }
        if (logoutHandler != null) {
            if (result) {
                logoutHandler.onSuccess(context.get());
            } else {
                logoutHandler.onFailure(mException);
            }
        }
    }

    public interface TaskListener {
        void onSuccess(Context context);

        void onFailure(Exception exception);
    }
}