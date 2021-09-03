package com.applozic.mobicomkit.api.account.user;

import android.content.Context;

import com.applozic.mobicomkit.api.account.register.RegisterUserClientService;
import com.applozic.mobicomkit.api.account.register.RegistrationResponse;
import com.applozic.mobicomkit.listners.AlLoginHandler;
import com.applozic.mobicommons.task.AlAsyncTask;

import java.lang.ref.WeakReference;

/**
 * An asynchronous login/registration task used to authenticate the user.
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
 *
 * <p>Use this or {@link com.applozic.mobicomkit.Applozic#connectUser(Context, User, AlLoginHandler)}</p>
 */
public class UserLoginTask extends AlAsyncTask<Void, Boolean> {

    private TaskListener taskListener;
    private final WeakReference<Context> context;
    private User user;
    private Exception mException;
    private RegistrationResponse registrationResponse;
    private UserClientService userClientService;
    private RegisterUserClientService registerUserClientService;
    private AlLoginHandler loginHandler;

    /**
     * @deprecated Use {@link UserLoginTask#UserLoginTask(User, AlLoginHandler, Context)} instead.
     */
    @Deprecated
    public UserLoginTask(User user, TaskListener listener, Context context) {
        this.taskListener = listener;
        this.context = new WeakReference<Context>(context);
        this.user = user;
        this.userClientService = new UserClientService(context);
        this.registerUserClientService = new RegisterUserClientService(context);
    }

    /**
     * @param user the {@link User} to login/register
     * @param listener the callback
     * @param context the context
     */
    public UserLoginTask(User user, AlLoginHandler listener, Context context) {
        this.loginHandler = listener;
        this.context = new WeakReference<Context>(context);
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
        // And if it is we call the callback function on it.
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
    }

    public interface TaskListener {
        void onSuccess(RegistrationResponse registrationResponse, Context context);

        void onFailure(RegistrationResponse registrationResponse, Exception exception);

    }


}