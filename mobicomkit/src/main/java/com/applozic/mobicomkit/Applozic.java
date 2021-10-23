package com.applozic.mobicomkit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.applozic.mobicomkit.api.MobiComKitClientService;
import com.applozic.mobicomkit.api.account.register.RegisterUserClientService;
import com.applozic.mobicomkit.api.account.register.RegistrationResponse;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.PushNotificationTask;
import com.applozic.mobicomkit.api.account.user.User;
import com.applozic.mobicomkit.api.account.user.UserClientService;
import com.applozic.mobicomkit.api.account.user.UserLoginTask;
import com.applozic.mobicomkit.api.account.user.UserLogoutTask;
import com.applozic.mobicomkit.api.authentication.AlAuthService;
import com.applozic.mobicomkit.api.authentication.RefreshAuthTokenTask;
import com.applozic.mobicomkit.api.conversation.ApplozicMqttWorker;
import com.applozic.mobicomkit.api.notification.MobiComPushReceiver;
import com.applozic.mobicomkit.api.notification.NotificationChannels;
import com.applozic.mobicomkit.broadcast.ApplozicBroadcastReceiver;
import com.applozic.mobicomkit.broadcast.BroadcastService;
import com.applozic.mobicomkit.contact.database.ContactDatabase;
import com.applozic.mobicomkit.listners.AlCallback;
import com.applozic.mobicomkit.listners.AlLoginHandler;
import com.applozic.mobicomkit.listners.AlLogoutHandler;
import com.applozic.mobicomkit.listners.AlPushNotificationHandler;
import com.applozic.mobicomkit.listners.ApplozicUIListener;
import com.applozic.mobicommons.AlLog;
import com.applozic.mobicommons.ApplozicService;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.data.AlPrefSettings;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.contact.Contact;
import com.applozic.mobicommons.task.AlAsyncTask;
import com.applozic.mobicommons.task.AlTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Contains all the major public methods for using the <i>Applozic Chat SDK</i>.
 */
public class Applozic {
    /**
     * Do not access directly. Use {@link #getInstance(Context)} instead.
     */
    @SuppressLint("StaticFieldLeak") //only application context is stored.
    @Nullable
    public static Applozic applozic;
    private final Context context;
    private ApplozicBroadcastReceiver applozicBroadcastReceiver;

    private Applozic(Context context) {
        this.context = ApplozicService.getContext(context);
    }

    public static @NonNull Applozic getInstance(@Nullable Context context) {
        if (applozic == null) {
            applozic = new Applozic(ApplozicService.getContext(context));
        }
        return applozic;
    }

    private static class LoggerListenerListSingletonHelper {
        private static final List<AlLog.AlLoggerListener> alLoggerListenerList = new ArrayList<>();
    }

    private static @NonNull List<AlLog.AlLoggerListener> getLoggerListenerListSingleton() {
        return LoggerListenerListSingletonHelper.alLoggerListenerList;
    }

    /**
     * Note: A max of 5 listeners can be added.
     */
    public synchronized static void addLoggerListener(@NonNull AlLog.AlLoggerListener alLoggerListener) {
        List<AlLog.AlLoggerListener> alLoggerListenerList = getLoggerListenerListSingleton();
        if (alLoggerListenerList.size() <= 5) {
            alLoggerListenerList.add(alLoggerListener);
        }
    }

    //do not call `logInfo(AlLog)` or `logError(AlLog)` inside this method
    private static void sendLogToListenerSingleton(@Nullable AlLog alLog) {
        if (alLog == null) {
            return;
        }

        List<AlLog.AlLoggerListener> alLoggerListenerList = getLoggerListenerListSingleton();
        for (AlLog.AlLoggerListener alLoggerListener : alLoggerListenerList) {
            alLoggerListener.onLogged(alLog);
        }
    }

    /**
     * Initializes the client SDK.
     *
     * <p>Must be run at-least once.</p>
     */
    public static @NonNull Applozic init(@NonNull Context context, @Nullable String applicationKey) {
        applozic = getInstance(context);
        AlPrefSettings.getInstance(context).setApplicationKey(applicationKey);
        return applozic;
    }

    /**
     * Returns the application key with which the given SDK has been initialized.
     *
     * <p>What is an application?</p>
     * <p>Whenever you sign up to <i>Applozic</i>, an <i>application</i> is created for you and an application key
     * is provided. This application, in simple words, is a "container" in which all your chat functionality and data live.</p>
     */
    public @Nullable String getApplicationKey() {
        String storeApplicationKey = Applozic.Store.getApplicationKey(context);
        if(!TextUtils.isEmpty(storeApplicationKey)) {
            return storeApplicationKey;
        } else {
            final String APPLICATION_KEY_MANIFEST_METADATA = "com.applozic.application.key";
            return Utils.getMetaDataValue(ApplozicService.getContext(context), APPLICATION_KEY_MANIFEST_METADATA);
        }
    }

    /**
     * Use this method to log-in or register your {@link User}. This must be done before using any other SDK method.
     *
     * <p>Before calling this method, make sure that {@link User#isValidUserId()} returns <i>true</i>.</p>
     *
     * <p>If the user (<code>userId</code>) is not present in the servers, a new
     * one will be created and registered. Otherwise the existing user will be authenticated and logged in.</p>
     *
     * <code>
     *     //this will run in calling thread
     *     RegistrationResponse registrationResponse = Applozic.connectUser(context, user).executeSync();
     *
     *     //this will execute in a background thread
     *     Applozic.connect(context, user).executeAsync(new BaseAsyncTask.AsyncListener<RegistrationResponse>() {
     *         @Override
     *         public void onComplete(RegistrationResponse registrationResponse) { }
     *
     *         @Override
     *         public void onFailed(Throwable t) { }
     *     });
     * </code>
     *
     * <p><i><b>Note:</b>Check if <code>registrationResponse</code> is non-null and {@link RegistrationResponse#isRegistrationSuccess()} is true to confirm a successful login.</i></p>
     *
     * <p>After a successful login, you'll be able to access:
     * <ul>
     *     <li>{@link MobiComUserPreference#getUserId() your user-id}.</li>
     *     <li>{@link com.applozic.mobicomkit.contact.AppContactService#getContactById(String) your contact object}.</li>
     *     <li>Your messages, contacts, channels and other freshly synced data to your local database for your user.</li>
     * </ul></p>
     *
     * <p>Other users will be able to see your status as "online".</p>
     *
     * <p>Next steps:</p>
     * <ol>
     *     <li>To set up push notifications - {@link #registerForPushNotification(Context, String)}. This is required for receiving messages.</li>
     *     <li>To send your first message - {@link com.applozic.mobicomkit.api.conversation.MessageBuilder}</li>
     * </ol>
     *
     * @see RegisterUserClientService#checkLoggedInAndCreateAccount(User)
     */
    public static @NonNull AlAsyncTask<Void, RegistrationResponse> connectUser(@NonNull Context context, @NonNull User user) {
        return new AlAsyncTask<Void, RegistrationResponse>() {
            @Override
            protected RegistrationResponse doInBackground() throws Exception {
                return new RegisterUserClientService(context).checkLoggedInAndCreateAccount(user);
            }
        };
    }

    /**
     * Enables push notifications, for messages and other events.
     *
     * <p>To set up:</p>
     * <ol>
     *     <li>Add <i>Firebase Cloud Messaging</i> to your app if you don't already use it.</li>
     *     <li>Override <code>FirebaseMessageService.onNewToken(String registrationToken)</code> to get the <i>registration-id</i>.</li>
     *     <li>Execute this task inside your <code>onNewToken</code> implementation with that <i>registration-id</i>.</li>
     * </ol>
     *
     * <code>
     *     //this will run in calling thread
     *     RegistrationResponse registrationResponse = Applozic.updatePushNotificationId(context, firebaseRegistrationToken).executeSync();
     *
     *     //this will execute in a background thread
     *     Applozic.updatePushNotificationId(context, firebaseRegistrationToken).executeAsync(new BaseAsyncTask.AsyncListener<RegistrationResponse>() {
     *         @Override
     *         public void onComplete(RegistrationResponse registrationResponse) { }
     *
     *         @Override
     *         public void onFailed(Throwable t) { }
     *     });
     * </code>
     *
     * <p><i><b>Note:</b>Check if <code>registrationResponse</code> is non-null and {@link RegistrationResponse#isRegistrationSuccess()} is true to confirm a successful registration.</i></p>
     *
     * @see RegisterUserClientService#updatePushNotificationId(String)
     */
    public static @NonNull AlAsyncTask<Void, RegistrationResponse> registerForPushNotification(@NonNull Context context, @Nullable String firebaseRegistrationToken) {
        return new AlAsyncTask<Void, RegistrationResponse>() {
            @Override
            protected RegistrationResponse doInBackground() throws Exception {
                return new RegisterUserClientService(context).updatePushNotificationId(firebaseRegistrationToken);
            }
        };
    }

    /**
     * Logout the current user.
     *
     * <code>
     *     //this will run in calling thread
     *     Boolean success = logoutUser(context).executeSync();
     *
     *     //this will execute in a background thread
     *     logoutUser(context).executeAsync(new BaseAsyncTask.AsyncListener<Boolean>() {
     *         @Override
     *         public void onComplete(Boolean aBoolean) { }
     *
     *         @Override
     *         public void onFailed(Throwable t) { }
     *     });
     * </code>
     *
     * <p><i><b>Note:</b> <code>aBoolean</code> will be true for success.
     *
     * @see UserClientService#logout() for details.
     */
    public static @NonNull AlAsyncTask<Void, Boolean> logoutUser(@NonNull Context context) {
        return new AlAsyncTask<Void, Boolean>() {
            @Override
            protected Boolean doInBackground() {
                new UserClientService(context).logout();
                return true;
            }
        };
    }

    /**
     * Refreshes the JWT authentication token and saves it locally for future use.
     * This token is used by the SDK to authenticate all future API calls.
     *
     * <p>To save unnecessary calls, run this task only if {@link AlAuthService#isTokenValid(Context)} returns <code>false</code>.</p>
     *
     * <code>
     *     //this will run in calling thread
     *     Boolean success = Applozic.refreshAuthToken(context).executeSync();
     *
     *     //this will execute in a background thread
     *     Applozic.refreshAuthToken(context).executeAsync(new BaseAsyncTask.AsyncListener<Boolean>() {
     *         @Override
     *         public void onComplete(Boolean aBoolean) { }
     *
     *         @Override
     *         public void onFailed(Throwable t) { }
     *     });
     * </code>
     *
     * <p><i><b>Note:</b> <code>aBoolean</code> will be true for success, false otherwise.
     */
    public static @NonNull AlAsyncTask<Void, Boolean> refreshAuthToken(@NonNull Context context) {
        return new AlAsyncTask<Void, Boolean>() {
            @Override
            protected Boolean doInBackground() {
                return new RegisterUserClientService(context).refreshAuthToken(Applozic.getInstance(context).getApplicationKey(), MobiComUserPreference.getInstance(context).getUserId());
            }
        };
    }

    /**
     * Checks if a user is connected and authenticated.
     *
     * <p>The SDK can be used only after a user has been logged in/connected.</p>
     *
     * @param context the context
     * @return true/false
     */
    public static boolean isConnected(@NonNull Context context) {
        return MobiComUserPreference.getInstance(context).isLoggedIn();
    }

    /**
     * Asynchronously connects to MQTT for receiving messages and other chat events.
     *
     * <p>Before calling this method, make sure that {@link AlAuthService#isTokenValid(Context)} returns true.</p>
     * <p>Otherwise refresh the token first using {@link #refreshAuthToken(Context)}.</p>
     *
     * <p>MQTT will receive messages only for the <i>application</i> lifecycle. You can alternatively use {@link com.applozic.mobicomkit.broadcast.AlEventManager}.</p>
     */
    public static void connectPublish(@NonNull Context context) {
        ApplozicMqttWorker.enqueueWorkSubscribeAndConnectPublishAfter(context, true, 0);
    }

    /**
     * Connect to MQTT after for publishing and receiving events after verifying and refreshing the JWT auth token.
     *
     * @param context the context
     * @param loadingMessage the message to display in the progress dialog while loading
     */
    public static void connectPublishWithVerifyToken(@Nullable final Context context, @Nullable String loadingMessage) {
        connectPublishWithVerifyTokenAfter(context, loadingMessage, 0);
    }

    /**
     * Asynchronously disconnects from MQTT for receiving messages and other events.
     */
    public static void disconnectPublish(@NonNull Context context) {
        disconnectPublish(context, true);
    }

    /**
     * Subscribe to the MQTT topic for typing for a particular conversation.
     *
     * <p>Either one of the channel or contact passed can be null.</p>
     *
     * @param context the context
     * @param channel the channel you wish to subscribe to for typing
     * @param contact the contact you wish to subscribe to for typing
     */
    public static void subscribeToTyping(Context context, Channel channel, Contact contact) {
        ApplozicMqttWorker.enqueueWorkSubscribeToTyping(context, channel, contact);
    }

    /**
     * Unsubscribe to the MQTT topic for typing for a particular conversation.
     *
     * <p>Either one of the channel or contact passed can be null.</p>
     *
     * @param context the context
     * @param channel the channel you wish to unsubscribe to for typing
     * @param contact the contact you wish to unsubscribe to for typing
     */
    public static void unSubscribeToTyping(Context context, Channel channel, Contact contact) {
        ApplozicMqttWorker.enqueueWorkUnSubscribeToTyping(context, channel, contact);
    }

    /**
     * Publish your typing status to MQTT for a particular conversation.
     *
     * <p>Either one of the channel or contact passed can be null.</p>
     *
     * @param context the context
     * @param channel the channel you wish to publish typing status to
     * @param contact the contact you wish to publish typing status to
     */
    public static void publishTypingStatus(Context context, Channel channel, Contact contact, boolean typingStarted) {
        ApplozicMqttWorker.enqueueWorkPublishTypingStatus(context, channel, contact, typingStarted);
    }

    /**
     * Logs the given info message to the console.
     *
     * <p>if a {@link com.applozic.mobicommons.AlLog.AlLoggerListener} listener has been
     * set, {@link com.applozic.mobicommons.AlLog.AlLoggerListener#onLogged(AlLog)} will be called
     * and a corresponding {@link AlLog} object will be passed to it.</p>
     *
     * @param tag The log tag.
     * @param message The log message.
     */
    public static void logInfo(String tag, String message) {
        AlLog alLog = AlLog.i(tag, null, message);
        sendLogToListenerSingleton(alLog);
    }

    /**
     * Logs the given error message to the console.
     *
     * <p>if a {@link com.applozic.mobicommons.AlLog.AlLoggerListener} listener has been
     * set, {@link com.applozic.mobicommons.AlLog.AlLoggerListener#onLogged(AlLog)} will be called
     * and a corresponding {@link AlLog} object will be passed to it.</p>
     *
     * @param tag The log tag.
     * @param message The log message.
     */
    public static void logError(String tag, String message, Throwable throwable) {
        AlLog alLog = AlLog.e(tag, null, message, throwable);
        sendLogToListenerSingleton(alLog);
    }

    public static class Store {
        static final String APPLICATION_KEY = "APPLICATION_KEY";
        static final String DEVICE_REGISTRATION_ID = "DEVICE_REGISTRATION_ID";
        static final String NOTIFICATION_CHANNEL_VERSION_STATE = "NOTIFICATION_CHANNEL_VERSION_STATE";
        static final String CUSTOM_NOTIFICATION_SOUND = "CUSTOM_NOTIFICATION_SOUND";

        private Store() { }

        static @NonNull SharedPreferences getSharedPreferences(Context context) {
            final String SHARED_PREF_NAME = "applozic_preference_key";
            return context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        }

        /**
         * Identifies the device for real-time updates (push-notifications).
         */
        public static @Nullable String getDeviceRegistrationId(@NonNull Context context) {
            return getSharedPreferences(context).getString(DEVICE_REGISTRATION_ID, null);
        }

        /**
         * The sound to be played whenever a device notification is shown.
         *
         * @param filePath the local absolute filepath to the sound file
         */
        public static void setCustomNotificationSound(@NonNull Context context, @Nullable String filePath) {
            getSharedPreferences(context).edit().putString(CUSTOM_NOTIFICATION_SOUND, filePath).apply();
        }

        /**
         * Sets the API key for location sharing to work.
         *
         * <p>Alternatively you can set it in your <code>AndroidManifest.xml</code>. Put this inside the <i>application</i> tag:</p>
         * <code>
         *     <meta-data
         *         android:name="com.google.android.geo.API_KEY"
         *         android:value="<THE_API_KEY>" />
         * </code>
         *
         * @param geoApiKey You can get it from <i>Google's Geolocation API</i>.
         */
        public static void setGeoApiKey(@NonNull Context context, @Nullable String geoApiKey) {
            AlPrefSettings.getInstance(context).setGeoApiKey(geoApiKey);
        }

        static String getGeoApiKey(@NonNull Context context) {
            return AlPrefSettings.getInstance(context).getGeoApiKey();
        }

        static @Nullable String getApplicationKey(@NonNull Context context) {
            String decryptedApplicationKey = AlPrefSettings.getInstance(context).getApplicationKey();
            if (!TextUtils.isEmpty(decryptedApplicationKey)) {
                return decryptedApplicationKey;
            }
            String existingAppKey = getSharedPreferences(context).getString(APPLICATION_KEY, null);
            if (!TextUtils.isEmpty(existingAppKey)) {
                AlPrefSettings.getInstance(context).setApplicationKey(existingAppKey);
                getSharedPreferences(context).edit().remove(APPLICATION_KEY).apply();
            }
            return existingAppKey;
        }

        /** Internal. Do not use. */
        public static String getCustomNotificationSound(@NonNull Context context) {
            return getSharedPreferences(context).getString(CUSTOM_NOTIFICATION_SOUND, null);
        }

        /** Internal. Do not use. */
        public static void setDeviceRegistrationId(@NonNull Context context, @Nullable String registrationId) {
            getSharedPreferences(context).edit().putString(DEVICE_REGISTRATION_ID, registrationId).apply();
        }

        /** Internal. Do not use. */
        public static int getNotificationChannelVersion(@NonNull Context context) {
            return getSharedPreferences(context).getInt(NOTIFICATION_CHANNEL_VERSION_STATE, NotificationChannels.NOTIFICATION_CHANNEL_VERSION - 1);
        }

        /** Internal. Do not use. */
        public static void setNotificationChannelVersion(@NonNull Context context, int version) {
            getSharedPreferences(context).edit().putInt(NOTIFICATION_CHANNEL_VERSION_STATE, version).apply();
        }
    }

    //internal methods >>>

    //Cleanup: private
    /** This is an internal method. Do not use */
    public static void disconnectPublish(@NonNull Context context, @Nullable String deviceKeyString, @Nullable String userKeyString, boolean useEncrypted) {
        if (!TextUtils.isEmpty(userKeyString) && !TextUtils.isEmpty(deviceKeyString)) {
            ApplozicMqttWorker.enqueueWorkDisconnectPublish(context, deviceKeyString, userKeyString, useEncrypted);
        }
    }

    //Cleanup: private
    /** This is an internal method. Do not use */
    public static void disconnectPublish(@NonNull Context context, boolean useEncrypted) {
        final String deviceKeyString = MobiComUserPreference.getInstance(context).getDeviceKeyString();
        final String userKeyString = MobiComUserPreference.getInstance(context).getSuUserKeyString();
        disconnectPublish(context, deviceKeyString, userKeyString, useEncrypted);
    }

    /** This is an internal method. Do not use. */
    public @Nullable String getGeoApiKey() {
        String geoApiKey = Applozic.Store.getGeoApiKey(context);
        if (!TextUtils.isEmpty(geoApiKey)) {
            return geoApiKey;
        }
        return Utils.getMetaDataValue(context, AlPrefSettings.GOOGLE_API_KEY_META_DATA);
    }

    /** This is an internal method. Do not use */
    public static boolean isApplozicNotification(@NonNull Context context, @Nullable Map<String, String> data) {
        if (MobiComPushReceiver.isMobiComPushNotification(data)) {
            MobiComPushReceiver.processMessageAsync(context, data);
            return true;
        }
        return false;
    }

    /** This is an internal method. Do not use. */
    public static void connectPublishWithVerifyTokenAfter(@Nullable final Context context, @Nullable String loadingMessage, int minutes) {
        if (context == null) {
            return;
        }
        AlAuthService.verifyToken(context, loadingMessage, new AlCallback() {
            @Override
            public void onSuccess(Object response) {
                ApplozicMqttWorker.enqueueWorkSubscribeAndConnectPublishAfter(context, true, minutes);
            }

            @Override
            public void onError(Object error) { }
        });
    }

    //deprecated code >>>

    /**
     * @deprecated Use the newer {@link #connectUser(Context, User)}. It supports both sync and async execution.
     *
     * Use this method to log-in or register your {@link User}. This must be done before using any other SDK method.
     */
    @Deprecated
    public static void connectUser(@NonNull Context context, @NonNull User user, @Nullable AlLoginHandler loginHandler) {
        if (isConnected(context)) {
            RegistrationResponse registrationResponse = new RegistrationResponse();
            registrationResponse.setMessage("User already Logged in");
            Contact contact = new ContactDatabase(context).getContactById(MobiComUserPreference.getInstance(context).getUserId());
            if (contact != null) {
                registrationResponse.setUserId(contact.getUserId());
                registrationResponse.setContactNumber(contact.getContactNumber());
                registrationResponse.setRoleType(contact.getRoleType());
                registrationResponse.setImageLink(contact.getImageURL());
                registrationResponse.setDisplayName(contact.getDisplayName());
                registrationResponse.setStatusMessage(contact.getStatus());
            }
            if (loginHandler != null) {
                loginHandler.onSuccess(registrationResponse, context);
            }
        } else {
            AlTask.execute(new UserLoginTask(user, loginHandler, context));
        }
    }

    /**
     * @deprecated Use the newer {@link Applozic#logoutUser(Context)}. It supports both sync and async execution.
     */
    @Deprecated
    public static void logoutUser(@NonNull final Context context, @Nullable AlLogoutHandler logoutHandler) {
        AlTask.execute(new UserLogoutTask(logoutHandler, context));
    }

    /**
     * @deprecated Use {@link Applozic#connectUser(Context, User)}.
     */
    @Deprecated
    public static void loginUser(Context context, User user, boolean withLoggedInCheck, AlLoginHandler loginHandler) {
        if (withLoggedInCheck && MobiComUserPreference.getInstance(context).isLoggedIn()) {
            RegistrationResponse registrationResponse = new RegistrationResponse();
            registrationResponse.setMessage("User already Logged in");
            loginHandler.onSuccess(registrationResponse, context);
        } else {
            AlTask.execute(new UserLoginTask(user, loginHandler, context));
        }
    }

    /**
     * @deprecated Use {@link Applozic#connectUser(Context, User)}.
     */
    @Deprecated
    public static void loginUser(Context context, User user, AlLoginHandler loginHandler) {
        if (MobiComUserPreference.getInstance(context).isLoggedIn()) {
            RegistrationResponse registrationResponse = new RegistrationResponse();
            registrationResponse.setMessage("User already Logged in");
            loginHandler.onSuccess(registrationResponse, context);
        } else {
            AlTask.execute(new UserLoginTask(user, loginHandler, context));
        }
    }

    //Cleanup: private
    /**
     * @deprecated Use {@link Applozic#connectUser(Context, User)}.
     */
    @Deprecated
    public static void connectUserWithoutCheck(Context context, User user, AlLoginHandler loginHandler) {
        AlTask.execute(new UserLoginTask(user, loginHandler, context));
    }

    //Cleanup: private
    /**
     * @deprecated Support groups are no longer used.
     */
    @Deprecated
    public static void subscribeToSupportGroup(Context context, boolean useEncrypted) {
        ApplozicMqttWorker.enqueueWorkSubscribeToSupportGroup(context, useEncrypted);
    }

    //Cleanup: private
    /**
     * @deprecated Support groups are no longer used.
     */
    @Deprecated
    public static void unSubscribeToSupportGroup(Context context, boolean useEncrypted) {
        ApplozicMqttWorker.enqueueWorkUnSubscribeToSupportGroup(context, useEncrypted);
    }

    /**
     * @deprecated Use {@link #registerForPushNotification(Context, String)}.
     */
    @Deprecated
    public static void registerForPushNotification(Context context, String pushToken, AlPushNotificationHandler handler) {
        AlTask.execute(new PushNotificationTask(context, pushToken, handler));
    }

    //Cleanup: private
    /**
     * @deprecated Use {@link #registerForPushNotification(Context, String)}.
     */
    @Deprecated
    public static void registerForPushNotification(Context context, AlPushNotificationHandler handler) {
        registerForPushNotification(context, Applozic.Store.getDeviceRegistrationId(context), handler);
    }

    /**
     * @deprecated Will be removed soon. Use {@link Applozic.Store#setCustomNotificationSound(Context, String)} instead.
     */
    @Deprecated
    public Applozic setCustomNotificationSound(@Nullable String filePath) {
        Applozic.Store.setCustomNotificationSound(context, filePath);
        return this;
    }

    /**
     * @deprecated Not required. Will be removed soon.
     */
    @Deprecated
    public @Nullable String getCustomNotificationSound() {
        return Applozic.Store.getCustomNotificationSound(context);
    }

    /**
     * @deprecated Use {@link Applozic.Store#setGeoApiKey(Context, String)}.
     */
    @Deprecated
    public void setGeoApiKey(String geoApiKey) {
        Applozic.Store.setGeoApiKey(context, geoApiKey);
    }

    /**
     * @deprecated Use {@link com.applozic.mobicomkit.broadcast.AlEventManager#registerUIListener(String, ApplozicUIListener)}.
     */
    @Deprecated
    public void registerUIListener(ApplozicUIListener applozicUIListener) {
        applozicBroadcastReceiver = new ApplozicBroadcastReceiver(applozicUIListener);
        LocalBroadcastManager.getInstance(context).registerReceiver(applozicBroadcastReceiver, BroadcastService.getIntentFilter());
    }

    /**
     * @deprecated Use {@link com.applozic.mobicomkit.broadcast.AlEventManager#unregisterMqttListener(String)}.
     */
    @Deprecated
    public void unregisterUIListener() {
        if (applozicBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(applozicBroadcastReceiver);
            applozicBroadcastReceiver = null;
        }
    }

    /**
     * @deprecated Not required. Will be removed soon.
     */
    @Deprecated
    public int getNotificationChannelVersion() {
        return Applozic.Store.getNotificationChannelVersion(context);
    }

    /**
     * @deprecated Not required. Will be removed soon.
     */
    @Deprecated
    public void setNotificationChannelVersion(int version) {
        Applozic.Store.setNotificationChannelVersion(context, version);
    }

    /**
     * @deprecated Will be removed soon. Use {@link Applozic.Store#getDeviceRegistrationId(Context)} instead.
     */
    @Deprecated
    public @Nullable String getDeviceRegistrationId() {
        return Applozic.Store.getDeviceRegistrationId(context);
    }

    /**
     * @deprecated Not required. Will be removed soon.
     */
    @Deprecated
    public @NonNull Applozic setDeviceRegistrationId(@Nullable String registrationId) {
        Applozic.Store.setDeviceRegistrationId(context, registrationId);
        return this;
    }

    /**
     * @deprecated Use {@link Applozic#isConnected(Context)} instead.
     */
    @Deprecated
    public static boolean isLoggedIn(Context context) {
        return MobiComUserPreference.getInstance(context).isLoggedIn();
    }

    //Cleanup: private
    /**
     * @deprecated Use {@link MobiComUserPreference#isRegistered()} instead.
     */
    @Deprecated
    public static boolean isRegistered(Context context) {
        return MobiComUserPreference.getInstance(context).isRegistered();
    }
}