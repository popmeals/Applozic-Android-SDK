package com.applozic.mobicomkit.api.account.user;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.applozic.mobicomkit.api.account.register.RegisterUserClientService;
import com.applozic.mobicomkit.api.account.register.RegistrationResponse;
import com.applozic.mobicomkit.listners.AlPushNotificationHandler;
import com.applozic.mobicommons.task.AlAsyncTask;

import java.lang.ref.WeakReference;

/**
 * @deprecated Use the newer {@link com.applozic.mobicomkit.Applozic#registerForPushNotification(Context, String, AlPushNotificationHandler)}.
 *
 * Enables push notifications, for messages and other events.
 *
 * <p>To set up:</p>
 * <ol>
 *     <li>Add <i>Firebase Cloud Messaging</i> to your app if you don't already use it.</li>
 *     <li>Override <code>FirebaseMessageService.onNewToken(String registrationToken)</code> to get the <i>registration-id</i>.</li>
 *     <li>Execute this task inside your <code>onNewToken</code> implementation with that <i>registration-id</i>.</li>
 * </ol>
 *
 * <p>To execute this task:</p>
 * <code>
 *     PushNotificationTask pushNotificationTask = new PushNotificationTask(context, "registrationId", new AlPushNotificationHandler() {
 *         @Override
 *         public void onSuccess(RegistrationResponse registrationResponse) {
 *             // Your application logic to handle success.
 *         }
 *
 *         @Override
 *         public void onFailure(RegistrationResponse registrationResponse, Exception exception) {
 *             // Your application logic to handle failure.
 *         }
 *      });

 *      AlTask.execute(pushNotificationTask);
 *
 *      // For versions prior to v5.95 use:
 *      // pushNotificationTask.execute();
 * </code>
 *
 * <p>Use {@link AlPushNotificationHandler} to get the results.</p>
 *
 * <p>If you're running in a background thread, you can instead use {@link RegisterUserClientService#updatePushNotificationId(String)}.
 */
@Deprecated
public class PushNotificationTask extends AlAsyncTask<Void, Boolean> {
    private final String firebaseRegistrationToken;
    private final WeakReference<Context> context;
    private Exception mException;
    private RegistrationResponse registrationResponse;
    private AlPushNotificationHandler pushNotificationHandler;

    @Deprecated
    private TaskListener taskListener;

    public PushNotificationTask(@NonNull Context context, @Nullable String firebaseRegistrationToken, @Nullable AlPushNotificationHandler listener) {
        this.firebaseRegistrationToken = firebaseRegistrationToken;
        this.pushNotificationHandler = listener;
        this.context = new WeakReference<>(context);
    }

    /**
     * @deprecated Instantiation using this constructor solves no unique purpose.
     * Use {@link #PushNotificationTask(Context, String, AlPushNotificationHandler)} instead.
     */
    @Deprecated
    public PushNotificationTask(String pushNotificationId, TaskListener listener, Context context) {
        this.firebaseRegistrationToken = pushNotificationId;
        this.taskListener = listener;
        this.context = new WeakReference<>(context);
    }

    @Override
    protected Boolean doInBackground() {
        try {
            registrationResponse = new RegisterUserClientService(context.get()).updatePushNotificationId(firebaseRegistrationToken);
        } catch (Exception e) {
            e.printStackTrace();
            mException = e;
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(final Boolean result) {
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

        if (taskListener != null) {
            if (registrationResponse != null && registrationResponse.isRegistrationSuccess()) {
                taskListener.onSuccess(registrationResponse);
            } else {
                taskListener.onFailure(registrationResponse, mException);
            }
        }
    }

    @Deprecated
    public interface TaskListener {
        void onSuccess(RegistrationResponse registrationResponse);
        void onFailure(RegistrationResponse registrationResponse, Exception exception);
    }
}