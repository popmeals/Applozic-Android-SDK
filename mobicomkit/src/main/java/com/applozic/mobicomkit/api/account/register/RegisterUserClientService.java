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
 * Handles registration and authentication for your {@link User} and login session.
 *
 * <p>The <i>user</i></p> need to be registered/authenticated before any of the SDK's methods can be used.
 * <p>The <i>login session</i> needs to be registered if you want real-time updates to be delivered.</p>
 *
 * <ul>
 *     <li>To register or authenticate a user, see the doc for {@link RegisterUserClientService#createAccount(User)}.</li>
 *     <li>To register your login session, see the doc for {@link RegisterUserClientService#updatePushNotificationId(String)}.</li>
 * </ul>
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
    private static final String INVALID_APP_ID = "INVALID_APPLICATIONID"; //Cleanup: can be removed
    private HttpRequestUtils httpRequestUtils;

    /**
     * Constructor. Also stores the application context. You can access later it using {@link ApplozicService#getAppContext()}.
     *
     * @param context the context
     */
    public RegisterUserClientService(Context context) {
        this.context = ApplozicService.getContext(context);
        ApplozicService.initWithContext(context);
        this.httpRequestUtils = new HttpRequestUtils(context);
    }

    //Cleanup: private
    /**
     * This is an internal method. Do not use.
     */
    public String getCreateAccountUrl() {
        return getBaseUrl() + CREATE_ACCOUNT_URL;
    }

    //Cleanup: private
    /**
     * This is an internal method. Do not use.
     */
    public String getPricingPackageUrl() {
        return getBaseUrl() + CHECK_PRICING_PACKAGE;
    }

    //Cleanup: private
    /**
     * This is an internal method. Do not use.
     */
    public String getUpdateAccountUrl() {
        return getBaseUrl() + UPDATE_ACCOUNT_URL;
    }

    //Cleanup: private
    /**
     * This is an internal method. Do not use.
     */
    public String getRefreshTokenUrl() {
        return getBaseUrl() + REFRESH_TOKEN_URL;
    }

    /**
     * This method registers(or logs in) a {@link User} to the Applozic servers. It also initializes the SDK for that user.
     *
     * Do not use this method directly. Use the <i>asynchronous</i> {@link Applozic#connectUser(Context, User, AlLoginHandler)} instead.
     *
     * @param user the user object to register/authenticate
     * @return the {@link RegistrationResponse}, {@link RegistrationResponse#isRegistrationSuccess()} will be true in case of a successful login/register. otherwise {@link RegistrationResponse#getMessage()} will have the error message
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
     * This method gets a fresh JWT authentication token from the Applozic servers and saves it locally for application use.
     *
     * <p><b>Note:</b> You do not need to use this method and refresh the JWT token manually. All this is handled by the SDK.</p>
     *
     * <p><i>What is this JWT token?</i></p>
     * <p>The JWT token is used for authentication/authorization of user level server calls.
     * This token is received from the backend after your user has been successfully logged-in or registered.</p>
     *
     * @see com.applozic.mobicomkit.api.authentication.RefreshAuthTokenTask
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
     * Updates the user's account with the registration-id from <i>Firebase Cloud Messaging</i>.
     *
     * <p>FCM is used for providing real-time updates for messages and other events to your device.</p>
     *
     * <p>This method will block the main thread. Use the asynchronous {@link com.applozic.mobicomkit.api.account.user.PushNotificationTask} instead.</p>
     *
     * @param pushNotificationId the <i>registration id/token</i> received from <i>Firebase Cloud Messaging</i>
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
     * <p>This is an internal method. You do not need to use it.</p>
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
