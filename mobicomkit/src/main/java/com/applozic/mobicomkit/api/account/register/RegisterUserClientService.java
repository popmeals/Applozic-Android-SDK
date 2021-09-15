package com.applozic.mobicomkit.api.account.register;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.applozic.mobicomkit.Applozic;
import com.applozic.mobicomkit.ApplozicClient;
import com.applozic.mobicomkit.api.HttpRequestUtils;
import com.applozic.mobicomkit.api.MobiComKitClientService;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.User;
import com.applozic.mobicomkit.api.authentication.JWT;
import com.applozic.mobicomkit.api.conversation.ApplozicMqttWorker;
import com.applozic.mobicomkit.api.conversation.ConversationWorker;
import com.applozic.mobicomkit.api.notification.NotificationChannels;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.exception.ApplozicException;
import com.applozic.mobicomkit.feed.ApiResponse;
import com.applozic.mobicomkit.listners.AlLoginHandler;
import com.applozic.mobicommons.ALSpecificSettings;
import com.applozic.mobicommons.ApplozicService;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.contact.Contact;
import com.google.gson.Gson;

import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * The <code>RegisterUserClientService</code> has methods required for user authentication/registration.
 * This class handles server API calls for the mentioned functions.
 *
 * <p>Methods of this class mostly deal with <i>device session</i> related code. You can use methods of this class for:
 * <ul>
 *     <li>Logging in/connecting a user. See {@link RegisterUserClientService#createAccount(User)}.</li>
 *     <li>Updating user details. See {@link RegisterUserClientService#updateRegisteredAccount(User)}.</li>
 *     <li>Setting up FCM/GCM push notifications. See {@link RegisterUserClientService#updatePushNotificationId(String)}.</li>
 * </ul></p>
 */
public class RegisterUserClientService extends MobiComKitClientService {
    private static final String CREATE_ACCOUNT_URL = "/rest/ws/register/client?";
    private static final String UPDATE_ACCOUNT_URL = "/rest/ws/register/update?";
    private static final String CHECK_PRICING_PACKAGE = "/rest/ws/application/pricing/package";
    private static final String REFRESH_TOKEN_URL = "/rest/ws/register/refresh/token";
    /**
     * This is an internal field. Do not use.
     */
    public static final Short MOBICOMKIT_VERSION_CODE = 112;
    private static final String TAG = "RegisterUserClient";
    //Cleanup: can be removed
    private static final String INVALID_APP_ID = "INVALID_APPLICATIONID";
    private HttpRequestUtils httpRequestUtils;

    /**
     * Constructor. Also initializes static `application` object with the context passed.
     * Use it using {@link ApplozicService#getAppContext()}.
     *
     * @see ApplozicService#initWithContext(Context)
     * @param context the context
     */
    public RegisterUserClientService(Context context) {
        this.context = ApplozicService.getContext(context);
        ApplozicService.initWithContext(context);
        this.httpRequestUtils = new HttpRequestUtils(context);
    }

    //ApplozicInternal: private
    /**
     * This is an internal method. Do not use.
     */
    public String getCreateAccountUrl() {
        return getBaseUrl() + CREATE_ACCOUNT_URL;
    }

    //ApplozicInternal: private
    /**
     * This is an internal method. Do not use.
     */
    public String getPricingPackageUrl() {
        return getBaseUrl() + CHECK_PRICING_PACKAGE;
    }

    //ApplozicInternal: private
    /**
     * This is an internal method. Do not use.
     */
    public String getUpdateAccountUrl() {
        return getBaseUrl() + UPDATE_ACCOUNT_URL;
    }

    //ApplozicInternal: private
    /**
     * This is an internal method. Do not use.
     */
    public String getRefreshTokenUrl() {
        return getBaseUrl() + REFRESH_TOKEN_URL;
    }

    /**
     * Do not use this method directly. Use {@link Applozic#connectUser(Context, User, AlLoginHandler)} instead.
     *
     * This method registers(or authenticates/logs in) a {@link User} in
     * the Applozic servers. It also initializes the SDK for that user.
     *
     * <p>If the user (identified by it's userId) is not present in the servers, a new
     * one will be created and registered. Otherwise the user will be authenticated/logged in.
     *
     * After the server call to register/authenticate the user is successful a number of things
     * happen:
     * <ul>
     *     <li>Data for the user received in the register/authenticate response is saved locally.</li>
     *     <li>A {@link Contact} with the user's details is created and added to the local database.</li>
     *     <li>Sync messages, contacts, channels and other data from the server.</li>
     *     <li>Bring the client online.</li>
     * </ul>
     * </p>
     *
     * @param user the user object to register/authenticate
     * @return the {@link RegistrationResponse}
     * @throws Exception in case of empty or invalid user-id (see {@link User#isValidUserId()}, and connection errors
     */
    public RegistrationResponse createAccount(User user) throws Exception {
        if (user.getDeviceType() == null) {
            user.setDeviceType(Short.valueOf("1"));
        }
        user.setPrefContactAPI(Short.valueOf("2"));
        user.setTimezone(TimeZone.getDefault().getID());
        user.setEnableEncryption(user.isEnableEncryption());

        if (!TextUtils.isEmpty(user.getAlBaseUrl())) {
            ALSpecificSettings.getInstance(context).setAlBaseUrl(user.getAlBaseUrl());
        }

        if (!TextUtils.isEmpty(user.getKmBaseUrl())) {
            ALSpecificSettings.getInstance(context).setKmBaseUrl(user.getKmBaseUrl());
        }

        if (TextUtils.isEmpty(user.getUserId())) {
            throw new ApplozicException("userId cannot be empty");
        }

        if (!user.isValidUserId()) {
            throw new ApplozicException("Invalid userId. Spacing and set of special characters ^!$%&*:(), are not accepted. \nOnly english language characters are accepted");
        }

        MobiComUserPreference mobiComUserPreference = MobiComUserPreference.getInstance(context);

        Gson gson = new Gson();
        user.setAppVersionCode(MOBICOMKIT_VERSION_CODE);
        user.setApplicationId(getApplicationKey(context));
        user.setRegistrationId(mobiComUserPreference.getDeviceRegistrationId());

        if (getAppModuleName(context) != null) {
            user.setAppModuleName(getAppModuleName(context));
        }

        Utils.printLog(context, TAG, "Net status" + Utils.isInternetAvailable(context.getApplicationContext()));

        if (!Utils.isInternetAvailable(context.getApplicationContext())) {
            throw new ConnectException("No Internet Connection");
        }

        HttpRequestUtils.isRefreshTokenInProgress = true;
        Utils.printLog(context, TAG, "Registration json " + gson.toJson(user));
        String response = httpRequestUtils.postJsonToServer(getCreateAccountUrl(), gson.toJson(user));

        Utils.printLog(context, TAG, "Registration response is: " + response);

        if (TextUtils.isEmpty(response) || response.contains("<html")) {
            throw new Exception("503 Service Unavailable");
        }

        final RegistrationResponse registrationResponse = gson.fromJson(response, RegistrationResponse.class);
        if (registrationResponse.isRegistrationSuccess()) {

            Utils.printLog(context, "Registration response ", "is " + registrationResponse);
            if (registrationResponse.getNotificationResponse() != null) {
                Utils.printLog(context, "Registration response ", "" + registrationResponse.getNotificationResponse());
            }
            JWT.parseToken(context, registrationResponse.getAuthToken());
            mobiComUserPreference.setEncryptionKey(registrationResponse.getEncryptionKey());
            mobiComUserPreference.enableEncryption(user.isEnableEncryption());
            mobiComUserPreference.setCountryCode(user.getCountryCode());
            mobiComUserPreference.setUserId(user.getUserId());
            mobiComUserPreference.setContactNumber(user.getContactNumber());
            mobiComUserPreference.setEmailVerified(user.isEmailVerified());
            mobiComUserPreference.setDisplayName(user.getDisplayName());
            mobiComUserPreference.setMqttBrokerUrl(registrationResponse.getBrokerUrl());
            mobiComUserPreference.setDeviceKeyString(registrationResponse.getDeviceKey());
            mobiComUserPreference.setEmailIdValue(user.getEmail());
            mobiComUserPreference.setImageLink(user.getImageLink());
            mobiComUserPreference.setSuUserKeyString(registrationResponse.getUserKey());
            mobiComUserPreference.setLastSyncTimeForMetadataUpdate(String.valueOf(registrationResponse.getCurrentTimeStamp()));
            mobiComUserPreference.setLastSyncTime(String.valueOf(registrationResponse.getCurrentTimeStamp()));
            mobiComUserPreference.setLastSeenAtSyncTime(String.valueOf(registrationResponse.getCurrentTimeStamp()));
            mobiComUserPreference.setChannelSyncTime(String.valueOf(registrationResponse.getCurrentTimeStamp()));
            mobiComUserPreference.setUserBlockSyncTime("10000");
            mobiComUserPreference.setUserDeactivated(registrationResponse.isDeactivate());
            if (registrationResponse.getNotificationAfter() != null) {
                ALSpecificSettings.getInstance(context).setNotificationAfterTime(registrationResponse.getNotificationAfter());
            }
            ApplozicClient.getInstance(context).skipDeletedGroups(user.isSkipDeletedGroups()).hideActionMessages(user.isHideActionMessages());
            if (!TextUtils.isEmpty(registrationResponse.getUserEncryptionKey())) {
                mobiComUserPreference.setUserEncryptionKey(registrationResponse.getUserEncryptionKey());
            }
            mobiComUserPreference.setPassword(user.getPassword());
            mobiComUserPreference.setPricingPackage(registrationResponse.getPricingPackage());
            mobiComUserPreference.setAuthenticationType(String.valueOf(user.getAuthenticationTypeId()));
            if (registrationResponse.getRoleType() != null) {
                mobiComUserPreference.setUserRoleType(registrationResponse.getRoleType());
            }
            if (user.getUserTypeId() != null) {
                mobiComUserPreference.setUserTypeId(String.valueOf(user.getUserTypeId()));
            }
            if (!TextUtils.isEmpty(user.getNotificationSoundFilePath())) {
                Applozic.getInstance(context).setCustomNotificationSound(user.getNotificationSoundFilePath());
            }
            Contact contact = new Contact();
            contact.setUserId(user.getUserId());
            contact.setFullName(registrationResponse.getDisplayName());
            contact.setImageURL(registrationResponse.getImageLink());
            contact.setContactNumber(registrationResponse.getContactNumber());
            contact.setMetadata(registrationResponse.getMetadata());
            if (user.getUserTypeId() != null) {
                contact.setUserTypeId(user.getUserTypeId());
            }
            contact.setRoleType(user.getRoleType());
            contact.setStatus(registrationResponse.getStatusMessage());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Applozic.getInstance(context).setNotificationChannelVersion(NotificationChannels.NOTIFICATION_CHANNEL_VERSION - 1);
                new NotificationChannels(context, Applozic.getInstance(context).getCustomNotificationSound()).prepareNotificationChannels();
            }
            ApplozicClient.getInstance(context).setChatDisabled(contact.isChatForUserDisabled());
            new AppContactService(context).upsert(contact);

            ConversationWorker.enqueueWorkSync(context);
            ConversationWorker.enqueueWorkMutedUserListSync(context);

            ApplozicMqttWorker.enqueueWorkConnectPublish(context);
        }

        return registrationResponse;
    }

    /**
     * This method gets a fresh auth token from the Applozic servers and saves it locally for application use.
     *
     * <p>A JWT auth token is retrieved from the backend and saved in the
     * user specific shared preference file {@link MobiComUserPreference}.
     * This token is used for authentication/authorization of user level server calls.
     * See the implementation in {@link HttpRequestUtils#addGlobalHeaders(HttpURLConnection, String)} for details on
     * where and how this token is used.</p>
     *
     * <p>Tip: Run this method asynchronously. Or use {@link com.applozic.mobicomkit.api.authentication.RefreshAuthTokenTask}.</p>
     *
     * @param applicationId the Applozic application id
     * @param userId the user id of the user to get the auth token for
     * @return true if the auth token was successfully retrieved and saved/false otherwise
     */
    public boolean refreshAuthToken(String applicationId, String userId) {
        try {
            HttpRequestUtils.isRefreshTokenInProgress = true;
            Map<String, String> tokenRefreshBodyMap = new HashMap<>();
            tokenRefreshBodyMap.put("applicationId", applicationId);
            tokenRefreshBodyMap.put("userId", userId);
            String response = httpRequestUtils.postDataForAuthToken(getRefreshTokenUrl(), "application/json", "application/json", GsonUtils.getJsonFromObject(tokenRefreshBodyMap, Map.class), userId);
            if (!TextUtils.isEmpty(response)) {
                ApiResponse<String> jwtTokenResponse = (ApiResponse<String>) GsonUtils.getObjectFromJson(response, ApiResponse.class);
                if (jwtTokenResponse != null && !TextUtils.isEmpty(jwtTokenResponse.getResponse())) {
                    JWT.parseToken(context, jwtTokenResponse.getResponse());
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //Cleanup: can be removed
    /**
     * @deprecated Use {@link RegisterUserClientService#createAccount(User)} instead.
     */
    @Deprecated
    public RegistrationResponse createAccount(String email, String userId, String phoneNumber, String displayName, String imageLink, String pushNotificationId) throws Exception {
        MobiComUserPreference mobiComUserPreference = MobiComUserPreference.getInstance(context);
        String url = mobiComUserPreference.getUrl();
        mobiComUserPreference.clearAll();
        mobiComUserPreference.setUrl(url);

        return updateAccount(email, userId, phoneNumber, displayName, imageLink, pushNotificationId);
    }

    //Cleanup: can be removed
    /**
     * @deprecated This method is no longer used and will be removed soon.
     */
    @Deprecated
    private RegistrationResponse updateAccount(String email, String userId, String phoneNumber, String displayName, String imageLink, String pushNotificationId) throws Exception {
        User user = new User();
        user.setUserId(userId);
        user.setEmail(email);
        user.setImageLink(imageLink);
        user.setRegistrationId(pushNotificationId);
        user.setDisplayName(displayName);
        user.setContactNumber(phoneNumber);

        final RegistrationResponse registrationResponse = createAccount(user);

        ApplozicMqttWorker.enqueueWorkConnectPublish(context);

        return registrationResponse;
    }

    //Cleanup: can be removed, is used in just the PushNotificationTask
    /**
     * Updates the user's account with the push notification id from <i>FCM/GCM</i>.
     *
     * <p>This is required for setting up push notifications.
     *
     * <p>Applozic uses <i>Firebase push-notifications</i> to deliver {@link com.applozic.mobicomkit.api.conversation.Message}s and
     * other important updates when your app is in the background.</p>
     *
     * <p>To setup push-notification you need to get the <i>push notification id</i> for your device from Firebase.</p>
     * <ol>
     *     <li>Override <code>FirebaseMessageService#onNewToken</code> to get a registration-id.</li>
     *     <li>Pass that <i>id</i> to this method.</li>
     * </ol>
     *
     * This <i>id</i> will then be used to deliver push notifications to the user's device.
     *
     * <p>This method will also save the <i>id</i> locally in a shared preference {@link MobiComUserPreference}.</p>
     *
     * <p>Tip: Run this method asynchronously. Or use {@link com.applozic.mobicomkit.api.account.user.PushNotificationTask}.</p>
     *
     * @param pushNotificationId the <i>push notification id</i> received from <i>FCM</i>
     * @return the user account update response from the server
     */
    public RegistrationResponse updatePushNotificationId(final String pushNotificationId) throws Exception {
        MobiComUserPreference pref = MobiComUserPreference.getInstance(context);
        //Note: In case if gcm registration is done before login then only updating in pref

        RegistrationResponse registrationResponse = null;
        User user = getUserDetail();

        if (!TextUtils.isEmpty(pushNotificationId)) {
            pref.setDeviceRegistrationId(pushNotificationId);
        }
        user.setRegistrationId(pushNotificationId);
        if (pref.isRegistered()) {
            registrationResponse = updateRegisteredAccount(user);
        }
        return registrationResponse;
    }

    /**
     * This method updates the user's details in the backend.
     *
     * <p>The user object that you pass to this method will be used to update
     * the user's account in the server.
     * <b>However,<b/> not all data is taken from the user object.
     *
     * The applicationId, encryption enable/disable, time zone, pref contact api value, app version code,
     * authentication type, app module name and device registration id will be taken from
     * the local applozic shared preference if present.</p>
     *
     * <p>Tip: Run this method asynchronously.</p>
     *
     * @param user the user data
     * @return registration response obtained from server
     */
    public RegistrationResponse updateRegisteredAccount(User user) throws Exception {
        RegistrationResponse registrationResponse = null;

        if (user.getDeviceType() == null) {
            user.setDeviceType(Short.valueOf("1"));
        }
        user.setPrefContactAPI(Short.valueOf("2"));
        user.setTimezone(TimeZone.getDefault().getID());
        user.setAppVersionCode(MOBICOMKIT_VERSION_CODE);

        if (!TextUtils.isEmpty(user.getAlBaseUrl())) {
            ALSpecificSettings.getInstance(context).setAlBaseUrl(user.getAlBaseUrl());
        }

        if (!TextUtils.isEmpty(user.getKmBaseUrl())) {
            ALSpecificSettings.getInstance(context).setKmBaseUrl(user.getKmBaseUrl());
        }

        MobiComUserPreference mobiComUserPreference = MobiComUserPreference.getInstance(context);

        Gson gson = new Gson();
        user.setEnableEncryption(mobiComUserPreference.isEncryptionEnabled());
        user.setApplicationId(getApplicationKey(context));
        user.setAuthenticationTypeId(Short.valueOf(mobiComUserPreference.getAuthenticationType()));
        if (!TextUtils.isEmpty(mobiComUserPreference.getUserTypeId())) {
            user.setUserTypeId(Short.valueOf(mobiComUserPreference.getUserTypeId()));
        }
        if (getAppModuleName(context) != null) {
            user.setAppModuleName(getAppModuleName(context));
        }
        if (!TextUtils.isEmpty(mobiComUserPreference.getDeviceRegistrationId())) {
            user.setRegistrationId(mobiComUserPreference.getDeviceRegistrationId());
        }
        Utils.printLog(context, TAG, "Registration update json " + gson.toJson(user));
        String response = httpRequestUtils.postJsonToServer(getUpdateAccountUrl(), gson.toJson(user));

        if (TextUtils.isEmpty(response) || response.contains("<html")) {
            throw null;
        }

        registrationResponse = gson.fromJson(response, RegistrationResponse.class);

        Utils.printLog(context, TAG, "Registration update response: " + registrationResponse);
        mobiComUserPreference.setPricingPackage(registrationResponse.getPricingPackage());
        if (registrationResponse.getNotificationResponse() != null) {
            Utils.printLog(context, TAG, "Notification response: " + registrationResponse.getNotificationResponse());
        }

        return registrationResponse;
    }

    private User getUserDetail() {

        MobiComUserPreference pref = MobiComUserPreference.getInstance(context);

        User user = new User();
        user.setEmail(pref.getEmailIdValue());
        user.setUserId(pref.getUserId());
        user.setContactNumber(pref.getContactNumber());
        user.setDisplayName(pref.getDisplayName());
        user.setImageLink(pref.getImageLink());
        user.setRoleType(pref.getUserRoleType());
        return user;
    }

    //Cleanup: can be removed
    /**
     * @deprecated This method is no longer used and will be removed soon.
     */
    @Deprecated
    public void syncAccountStatus() {
        try {
            String response = httpRequestUtils.getResponse(getPricingPackageUrl(), "application/json", "application/json");
            Utils.printLog(context, TAG, "Pricing package response: " + response);
            ApiResponse apiResponse = (ApiResponse) GsonUtils.getObjectFromJson(response, ApiResponse.class);
            if (apiResponse.getResponse() != null) {
                int pricingPackage = Integer.parseInt(apiResponse.getResponse().toString());
                MobiComUserPreference.getInstance(context).setPricingPackage(pricingPackage);
            }
        } catch (Exception e) {
            Utils.printLog(context, TAG, "Account status sync call failed");
        }
    }
}
