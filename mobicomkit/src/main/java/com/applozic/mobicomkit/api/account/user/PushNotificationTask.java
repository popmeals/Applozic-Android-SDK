package com.applozic.mobicomkit.api.account.user;

import android.content.Context;

import com.applozic.mobicomkit.api.account.register.RegisterUserClientService;
import com.applozic.mobicomkit.api.account.register.RegistrationResponse;
import com.applozic.mobicomkit.listners.AlPushNotificationHandler;
import com.applozic.mobicommons.task.AlAsyncTask;

import java.lang.ref.WeakReference;

/**
 * An asynchronous task that updates the server with a <p>"registration-id"</p> that will be used to identify the device for <i>Firebase Cloud Messaging</i> push-notifications.
 *
 * <p>FCM push-notifications are used for providing real-time updates for messages and other events your device.</p>
 *
 * <p>Created for async execution of the {@link RegisterUserClientService#updatePushNotificationId(String)}.
 *
 * <p>To setup push-notifications/real-time updates:</p>
 * <ol>
 *     <li>Add <i>Firebase (Firebase Cloud Messaging)</i> to your app if you don't already use it.</li>
 *     <li>Override <code>FirebaseMessageService.onNewToken(String registrationToken)</code> to get the <i>registration-id</i>.</li>
 *     <li>Execute this task inside your <code>onNewToken</code> implementation with that <i>registration-id</i>.</li>
 * </ol>
 *
 * <p>To execute this task:</p>
 * <code>
 *     PushNotificationTask pushNotificationTask = new PushNotificationTask(context, "registrationId", new AlPushNotificationHandler() {
 *                 @Override
 *                 public void onSuccess(RegistrationResponse registrationResponse) {
 *                     //registrationResponse.getMessage();
 *                 }
 *
 *                 @Override
 *                 public void onFailure(RegistrationResponse registrationResponse, Exception exception) {
 *                     //if(exception != null) {
 *                         //exception.printStackTrace();
 *                     //}
 *                 }
 *             });
 *             AlTask.execute(pushNotificationTask);
 *
 *             //for versions prior to v5.95 use:
 *             //pushNotificationTask.execute();
 * </code>
 *
 * Use {@link AlPushNotificationHandler} to get the results.</p>
 */
public class PushNotificationTask extends AlAsyncTask<Void, Boolean> {

    private String pushNotificationId;
    private TaskListener taskListener;
    private WeakReference<Context> context;
    private Exception mException;
    private RegistrationResponse registrationResponse;
    private AlPushNotificationHandler pushNotificationHandler;

    /**
     * @deprecated Instantiation using this constructor solves no unique purpose.
     * Use {@link PushNotificationTask#PushNotificationTask(Context, String, AlPushNotificationHandler)} instead.
     */
    @Deprecated
    public PushNotificationTask(String pushNotificationId, TaskListener listener, Context context) {
        this.pushNotificationId = pushNotificationId;
        this.taskListener = listener;
        this.context = new WeakReference<Context>(context);
    }

    /**
     * @param context the context
     * @param pushNotificationId the registration-id/push-notification-id received from Firebase
     * @param listener the callback
     */
    public PushNotificationTask(Context context, String pushNotificationId, AlPushNotificationHandler listener) {
        this.pushNotificationId = pushNotificationId;
        this.pushNotificationHandler = listener;
        this.context = new WeakReference<Context>(context);
    }

    @Override
    protected Boolean doInBackground() {
        try {
            registrationResponse = new RegisterUserClientService(context.get()).updatePushNotificationId(pushNotificationId);
        } catch (Exception e) {
            e.printStackTrace();
            mException = e;
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(final Boolean result) {
        // And if it is we call the callback function on it.
        if (taskListener != null) {
            if (registrationResponse != null) {
                if (registrationResponse.isRegistrationSuccess()) {
                    taskListener.onSuccess(registrationResponse);
                } else {
                    taskListener.onFailure(registrationResponse, mException);
                }
            } else {
                taskListener.onFailure(null, mException);
            }
        }

        if (pushNotificationHandler != null) {
            if (registrationResponse != null) {
                if (registrationResponse.isRegistrationSuccess()) {
                    pushNotificationHandler.onSuccess(registrationResponse);
                } else {
                    pushNotificationHandler.onFailure(registrationResponse, mException);
                }
            } else {
                pushNotificationHandler.onFailure(null, mException);
            }
        }
    }

    public interface TaskListener {

        void onSuccess(RegistrationResponse registrationResponse);

        void onFailure(RegistrationResponse registrationResponse, Exception exception);

    }
}