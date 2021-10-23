package com.applozic.mobicomkit.api.account.user;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.applozic.mobicomkit.api.account.register.RegisterUserClientService;
import com.applozic.mobicomkit.api.account.register.RegistrationResponse;
import com.applozic.mobicomkit.listners.AlLoginHandler;
import com.applozic.mobicommons.task.AlAsyncTask;

import java.lang.ref.WeakReference;

/**
 * @deprecated Use the newer {@link com.applozic.mobicomkit.Applozic#connectUser(Context, User)} instead.
 *
 * <p>An asynchronous login/registration task used to authenticate the user.</p>
 *
 * <p>It provides an async wrapper for {@link RegisterUserClientService#createAccount(User)}.
 * It also wipes out the existing shared preferences before it starts the login process.</p>
 *
 * <code>
 *     User user = new User();
 *     user.setUserId(“userId”); //mandatory
 *     user.setDisplayName(“displayName”);
 *     user.setEmail(“email”);
 *     user.setAuthenticationTypeId(User.AuthenticationType.APPLOZIC.getValue());  //use this by default
 *     user.setPassword("password");
 *     user.setImageLink("url/to/profile/image");
 *
 *     UserLoginTask userLoginTask = new UserLoginTask(user, new AlLoginHandler() {
 *         @Override
 *         public void onSuccess(RegistrationResponse registrationResponse, Context context) {
 *             //registrationResponse.getMessage();
 *         }
 *
 *         @Override
 *         public void onFailure(RegistrationResponse registrationResponse, Exception exception) { }
 *     }, context.get());
 *     AlTask.execute(userLoginTask);
 *
 *     //for versions prior to v5.95 use:
 *     userLoginTask.execute();
 * </code>
 */
@Deprecated
public class UserLoginTask extends AlAsyncTask<Void, Boolean> {
    private final WeakReference<Context> context;
    private final User user;
    private Exception mException;
    private RegistrationResponse registrationResponse;
    private final UserClientService userClientService;
    private final RegisterUserClientService registerUserClientService;
    private AlLoginHandler loginHandler;

    @Deprecated
    private TaskListener taskListener;

    /**
     * @param user the {@link User} to login/register
     * @param listener the callback
     * @param context the context
     */
    public UserLoginTask(@NonNull User user, @Nullable AlLoginHandler listener, @NonNull Context context) {
        this.loginHandler = listener;
        this.context = new WeakReference<>(context);
        this.user = user;
        this.userClientService = new UserClientService(context);
        this.registerUserClientService = new RegisterUserClientService(context);
    }

    /**
     * @deprecated Use {@link #UserLoginTask(User, AlLoginHandler, Context)} instead.
     */
    @Deprecated
    public UserLoginTask(User user, TaskListener listener, Context context) {
        this.taskListener = listener;
        this.context = new WeakReference<>(context);
        this.user = user;
        this.userClientService = new UserClientService(context);
        this.registerUserClientService = new RegisterUserClientService(context);
    }

    @Override
    protected Boolean doInBackground() {
        try {
            userClientService.clearDataAndPreference();
            registrationResponse = registerUserClientService.createAccount(user);
        } catch (Exception e) {
            e.printStackTrace();
            mException = e;
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(final Boolean result) {
        if (loginHandler != null) {
            if (registrationResponse != null) {
                if (registrationResponse.isRegistrationSuccess()) {
                    loginHandler.onSuccess(registrationResponse, context.get());
                } else {
                    loginHandler.onFailure(registrationResponse, mException);
                }
            } else {
                loginHandler.onFailure(null, mException);
            }
        }

        if (taskListener != null) {
            if (registrationResponse != null) {
                if (registrationResponse.isRegistrationSuccess()) {
                    taskListener.onSuccess(registrationResponse, context.get());
                } else {
                    taskListener.onFailure(registrationResponse, mException);
                }
            } else {
                taskListener.onFailure(null, mException);
            }
        }
    }

    @Deprecated
    public interface TaskListener {
        void onSuccess(RegistrationResponse registrationResponse, Context context);
        void onFailure(RegistrationResponse registrationResponse, Exception exception);
    }
}