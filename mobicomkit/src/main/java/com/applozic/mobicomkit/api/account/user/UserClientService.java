package com.applozic.mobicomkit.api.account.user;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.applozic.mobicomkit.AlUserUpdate;
import com.applozic.mobicomkit.Applozic;
import com.applozic.mobicomkit.api.HttpRequestUtils;
import com.applozic.mobicomkit.api.MobiComKitClientService;
import com.applozic.mobicomkit.api.MobiComKitConstants;
import com.applozic.mobicomkit.api.conversation.ApplozicMqttWorker;
import com.applozic.mobicomkit.api.conversation.database.MessageDatabaseService;
import com.applozic.mobicomkit.api.notification.MuteUserResponse;
import com.applozic.mobicomkit.api.notification.NotificationChannels;
import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicomkit.database.MobiComDatabaseHelper;
import com.applozic.mobicomkit.exception.ApplozicException;
import com.applozic.mobicomkit.feed.ApiResponse;
import com.applozic.mobicomkit.feed.SyncBlockUserApiResponse;
import com.applozic.mobicomkit.feed.UserDetailListFeed;
import com.applozic.mobicomkit.listners.AlLogoutHandler;
import com.applozic.mobicommons.ALSpecificSettings;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.json.GsonUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The <code>UserClientService</code> class provides methods that perform requests to the Applozic servers for everything
 * {@link User} and {@link com.applozic.mobicommons.people.contact.Contact} related.
 *
 * <p>Methods of this class simply perform the required API call and return a response string or object when required.
 * They do not affect the SDKs persisted/local database data. The only exception are the {@link UserClientService#logout()}, {@link UserClientService#logout(boolean)}, {@link UserClientService#postUserDetailsByUserIds(Set)}
 * and {@link UserClientService#clearDataAndPreference()} methods.</p>
 *
 * <p>All methods of this class run blocking. You will need to run them asynchronously.</p>
 *
 * <p>Note: This class has multiple public variables. All of them are for internal use and you will
 * never need to work with any of them.</p>
 */
public class UserClientService extends MobiComKitClientService {
    //ApplozicInternal: all need to be private
    public static final String APP_VERSION_UPDATE_URL = "/rest/ws/register/version/update";
    public static final String USER_INFO_URL = "/rest/ws/user/info?";
    public static final Short MOBICOMKIT_VERSION_CODE = 109;
    public static final String USER_DISPLAY_NAME_UPDATE = "/rest/ws/user/name?";
    public static final String BLOCK_USER_URL = "/rest/ws/user/block";
    public static final String BLOCK_USER_SYNC_URL = "/rest/ws/user/blocked/sync";
    public static final String UNBLOCK_USER_SYNC_URL = "/rest/ws/user/unblock";
    public static final String USER_DETAILS_URL = "/rest/ws/user/detail?";
    public static final String ONLINE_USER_LIST_URL = "/rest/ws/user/ol/list";
    public static final String REGISTERED_USER_LIST_URL = "/rest/ws/user/filter";
    public static final String USER_PROFILE_UPDATE_URL = "/rest/ws/user/update";
    public static final String USER_READ_URL = "/rest/ws/user/read";
    public static final String USER_DETAILS_LIST_POST_URL = "/rest/ws/user/detail";
    public static final String UPDATE_USER_PASSWORD = "/rest/ws/user/update/password";
    public static final String USER_LOGOUT = "/rest/ws/device/logout";
    private static final String MUTE_USER_URL = "/rest/ws/user/chat/mute";
    private static final String USER_SEARCH_URL = "/rest/ws/user/search/contact";
    private static final String GET_MUTED_USER_LIST = "/rest/ws/user/chat/mute/list";
    public static final int BATCH_SIZE = 60;
    private static final String TAG = "UserClientService";
    private HttpRequestUtils httpRequestUtils;

    public UserClientService(Context context) {
        super(context);
        this.httpRequestUtils = new HttpRequestUtils(context);
    }

    //ApplozicInternal: private
    public String getUserProfileUpdateUrl() {
        return getBaseUrl() + USER_PROFILE_UPDATE_URL;
    }

    //ApplozicInternal: private
    public String getAppVersionUpdateUrl() {
        return getBaseUrl() + APP_VERSION_UPDATE_URL;
    }

    //ApplozicInternal: private
    public String getUpdateUserDisplayNameUrl() {
        return getBaseUrl() + USER_DISPLAY_NAME_UPDATE;
    }

    //ApplozicInternal: private
    public String getUserInfoUrl() {
        return getBaseUrl() + USER_INFO_URL;
    }

    //ApplozicInternal: private
    public String getBlockUserUrl() {
        return getBaseUrl() + BLOCK_USER_URL;
    }

    //ApplozicInternal: private
    public String getBlockUserSyncUrl() {
        return getBaseUrl() + BLOCK_USER_SYNC_URL;
    }

    //ApplozicInternal: private
    //Cleanup: the name says sync but the url isn't for sync
    public String getUnBlockUserSyncUrl() {
        return getBaseUrl() + UNBLOCK_USER_SYNC_URL;
    }

    //ApplozicInternal: private
    public String getUserDetailsListUrl() {
        return getBaseUrl() + USER_DETAILS_URL;
    }

    //ApplozicInternal: private
    public String getOnlineUserListUrl() {
        return getBaseUrl() + ONLINE_USER_LIST_URL;
    }

    //ApplozicInternal: private
    public String getRegisteredUserListUrl() {
        return getBaseUrl() + REGISTERED_USER_LIST_URL;
    }

    //ApplozicInternal: private
    public String getUserDetailsListPostUrl() {
        return getBaseUrl() + USER_DETAILS_LIST_POST_URL;
    }

    //ApplozicInternal: private
    public String getUserReadUrl() {
        return getBaseUrl() + USER_READ_URL;
    }

    //ApplozicInternal: private
    public String getUpdateUserPasswordUrl() {
        return getBaseUrl() + UPDATE_USER_PASSWORD;
    }

    //ApplozicInternal: private
    public String getUserLogout() {
        return getBaseUrl() + USER_LOGOUT;
    }

    private String getMuteUserUrl() {
        return getBaseUrl() + MUTE_USER_URL;
    }

    private String getMutedUserListUrl() {
        return getBaseUrl() + GET_MUTED_USER_LIST;
    }

    private String getUserSearchUrl() {
        return getBaseUrl() + USER_SEARCH_URL;
    }

    /**
     * Performs logout for the current user.
     *
     * Use {@link UserLogoutTask} or {@link Applozic#logoutUser(Context, AlLogoutHandler)}. They wrap around this method asynchronously.
     *
     * <p>A logout server call will be sent. Along with that:
     * - {@link MobiComUserPreference} shared preference will be cleared.
     * Everything except {@link MobiComUserPreference#getUrl()}.
     * - {@link ALSpecificSettings} shared preference will be cleared.
     * - All notification channel will be deleted.
     * - The local database will be deleted.
     * - All unsent messages will be removed.
     * - All notifications will be cancelled.</p>
     *
     * @see UserClientService#logout(boolean)
     * @return logout backend api response, use {@link ApiResponse#isSuccess()} to check for success
     */
    public ApiResponse logout() {
        return logout(false);
    }

    //ApplozicInternal: default
    /**
     * This method wipes all local data. Calling it will make the SDK falsely believe that the user has logged out.
     */
    public void clearDataAndPreference() {
        MobiComUserPreference mobiComUserPreference = MobiComUserPreference.getInstance(context);
        final String deviceKeyString = mobiComUserPreference.getDeviceKeyString();
        final String userKeyString = mobiComUserPreference.getSuUserKeyString();
        String url = mobiComUserPreference.getUrl();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        mobiComUserPreference.clearAll();
        ChannelService.clearInstance();
        MessageDatabaseService.recentlyAddedMessage.clear();
        MobiComDatabaseHelper.getInstance(context).delDatabase();
        mobiComUserPreference.setUrl(url);

        ApplozicMqttWorker.enqueueWorkDisconnectPublish(context, deviceKeyString, userKeyString, false);
    }

    //ApplozicInternal: private
    //Cleanup: fromLogin is always false
    /**
     * @deprecated This methods has a un-necessary parameter. Use {@link UserClientService#logout()} instead.
     *
     * @param fromLogin pass false
     * @return logout backend api response, use {@link ApiResponse#isSuccess()} to check for success
     */
    public ApiResponse logout(boolean fromLogin) {
        Utils.printLog(context, TAG, "Al Logout call !!");
        ApiResponse apiResponse = userLogoutResponse();
        MobiComUserPreference mobiComUserPreference = MobiComUserPreference.getInstance(context);
        final String deviceKeyString = mobiComUserPreference.getDeviceKeyString();
        final String userKeyString = mobiComUserPreference.getSuUserKeyString();
        String url = mobiComUserPreference.getUrl();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Applozic.getInstance(context).setCustomNotificationSound(null);
            new NotificationChannels(context, null).deleteAllChannels();
        }
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        mobiComUserPreference.clearAll();
        ALSpecificSettings.getInstance(context).clearAll();
        MessageDatabaseService.recentlyAddedMessage.clear();
        MobiComDatabaseHelper.getInstance(context).delDatabase();
        mobiComUserPreference.setUrl(url);
        if (!fromLogin) {
            ApplozicMqttWorker.enqueueWorkDisconnectPublish(context, deviceKeyString, userKeyString, false);
        }
        return apiResponse;
    }

    //Cleanup: rename to something more suitable for a public api
    /**
     * Simply sends a logout server call for the logged user.
     *
     * <p>Note: This is a network method. Run it asynchronously.</p>
     *
     * @return logout backend api response. use {@link ApiResponse#isSuccess()} to check for success
     */
    public ApiResponse userLogoutResponse() {
        String response = "";
        ApiResponse apiResponse = null;
        try {
            response = httpRequestUtils.postData(getUserLogout(), "application/json", "application/json", null);
            if (!TextUtils.isEmpty(response)) {
                apiResponse = (ApiResponse) GsonUtils.getObjectFromJson(response, ApiResponse.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return apiResponse;
    }

    public void updateCodeVersion(final String deviceKeyString) {
        String url = getAppVersionUpdateUrl() + "?appVersionCode=" + MOBICOMKIT_VERSION_CODE + "&deviceKey=" + deviceKeyString;
        String response = httpRequestUtils.getResponse(url, "text/plain", "text/plain");
        Utils.printLog(context, TAG, "Version update response: " + response);

    }

    //ApplozicInternal: private
    /**
     * @deprecated This method is no longer used and will be removed soon. Use {@link #getUserDetails(Set)} instead.
     */
    @Deprecated
    public Map<String, String> getUserInfo(Set<String> userIds) throws JSONException, UnsupportedEncodingException {

        if (userIds == null && userIds.isEmpty()) {
            return new HashMap<>();
        }

        String userIdParam = "";
        for (String userId : userIds) {
            userIdParam += "&userIds" + "=" + URLEncoder.encode(userId, "UTF-8");
        }

        String response = httpRequestUtils.getResponse(getUserInfoUrl() + userIdParam, "application/json", "application/json");
        Utils.printLog(context, TAG, "Response: " + response);

        JSONObject jsonObject = new JSONObject(response);

        Map<String, String> info = new HashMap<String, String>();

        Iterator iterator = jsonObject.keys();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            String value = jsonObject.getString(key);
            info.put(key, value);
        }
        return info;
    }

    /**
     * Sends a server request to update the display name for the given userId.
     *
     * <p>Note: This is a network method. Run it asynchronously.</p>
     *
     * @param userId the user id of the user
     * @param displayName the new display name
     * @return api response from the server, use {@link ApiResponse#isSuccess()} to check for success
     */
    public ApiResponse updateUserDisplayName(final String userId, final String displayName) {
        String parameters = "";
        try {
            if (!TextUtils.isEmpty(userId) && !TextUtils.isEmpty(displayName)) {
                parameters = "userId=" + URLEncoder.encode(userId, "UTF-8") + "&displayName=" + URLEncoder.encode(displayName, "UTF-8");
                String response = httpRequestUtils.getResponse(getUpdateUserDisplayNameUrl() + parameters, "application/json", "application/json");

                if (!TextUtils.isEmpty(response)) {
                    return (ApiResponse) GsonUtils.getObjectFromJson(response, ApiResponse.class);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Sends a server request to block/unblock the given userId for the current(logged) user.
     *
     * <p>You do not need to pass the current user's user-id, it is taken from the local shared preferences({@link MobiComUserPreference}).</p>
     *
     * <p>Note: This is a network method. Run it asynchronously.</p>
     *
     * @param userId the userId of the user to block/unblock
     * @param block true for block/false for unblock
     * @return api response from the server, use {@link ApiResponse#isSuccess()} to check for success
     */
    public ApiResponse userBlock(String userId, boolean block) {
        String response = "";
        ApiResponse apiResponse = null;
        try {
            if (!TextUtils.isEmpty(userId)) {
                response = httpRequestUtils.getResponse((block ? getBlockUserUrl() : getUnBlockUserSyncUrl()) + "?userId=" + URLEncoder.encode(userId, "UTF-8"), "application/json", "application/json");
                apiResponse = (ApiResponse) GsonUtils.getObjectFromJson(response, ApiResponse.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return apiResponse;
    }

    //ApplozicInternal: private
    /**
     * @deprecated This method has been replaced with {@link #userBlock(String, boolean)}. Pass "false" in the second parameter.
     */
    @Deprecated
    public ApiResponse userUnBlock(String userId) {
        String response = "";
        ApiResponse apiResponse = null;
        try {
            if (!TextUtils.isEmpty(userId)) {
                response = httpRequestUtils.getResponse(getUnBlockUserSyncUrl() + "?userId=" + URLEncoder.encode(userId, "UTF-8"), "application/json", "application/json");
                apiResponse = (ApiResponse) GsonUtils.getObjectFromJson(response, ApiResponse.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return apiResponse;
    }

    //ApplozicInternal: default

    /**
     * This is an internal method. Syncing is handled by the SDK internally.
     *
     * <p>Returns the users that were blocked since the last sync time.</p>
     */
    public SyncBlockUserApiResponse getSyncUserBlockList(String lastSyncTime) {
        try {
            String url = getBlockUserSyncUrl() + "?lastSyncTime=" + lastSyncTime;
            String response = httpRequestUtils.getResponse(url, "application/json", "application/json");

            if (response == null || TextUtils.isEmpty(response) || response.equals("UnAuthorized Access")) {
                return null;
            }
            return (SyncBlockUserApiResponse) GsonUtils.getObjectFromJson(response, SyncBlockUserApiResponse.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Retrieves data of users from the server for the given set of userIds.
     *
     * Convert the string response to {@link UserDetail}:
     * <code>
     *     UserDetail[] userDetails = (UserDetail[]) GsonUtils.getObjectFromJson(response, UserDetail[].class);
     * </code>
     *
     * <p>Note: This is a network method. Run it asynchronously.</p>
     *
     * @param userIds the set of userIds to get details for
     * @return string json api response. will be empty in case of error. convert to {@link UserDetail} using {@link GsonUtils#getObjectFromJson(String, Type)}
     */
    public String getUserDetails(Set<String> userIds) {
        try {
            if (userIds != null && userIds.size() > 0) {
                String response = "";
                String userIdParam = "";
                for (String userId : userIds) {
                    userIdParam += "&userIds" + "=" + URLEncoder.encode(userId, "UTF-8");
                }
                response = httpRequestUtils.getResponse(getUserDetailsListUrl() + userIdParam, "application/json", "application/json");
                Utils.printLog(context, TAG, "User details response is :" + response);
                if (TextUtils.isEmpty(response) || response.contains("<html>")) {
                    return null;
                }
                return response;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //ApplozicInternal: default
    /**
     * Retrieves data of users for the given user-ids from the backend and updates it locally.
     *
     * @param userIds set of user ids to sync
     * @return api response from the server
     */
    public String postUserDetailsByUserIds(Set<String> userIds) {
        try {
            if (userIds != null && userIds.size() > 0) {
                List<String> userDetailsList = new ArrayList<>();
                String response = "";
                int count = 0;
                for (String userId : userIds) {
                    count++;
                    userDetailsList.add(userId);
                    if (count % BATCH_SIZE == 0) {
                        UserDetailListFeed userDetailListFeed = new UserDetailListFeed();
                        userDetailListFeed.setContactSync(true);
                        userDetailListFeed.setUserIdList(userDetailsList);
                        String jsonFromObject = GsonUtils.getJsonFromObject(userDetailListFeed, userDetailListFeed.getClass());
                        Utils.printLog(context, TAG, "Sending json:" + jsonFromObject);
                        response = httpRequestUtils.postData(getUserDetailsListPostUrl(), "application/json", "application/json", jsonFromObject);
                        userDetailsList = new ArrayList<String>();
                        if (!TextUtils.isEmpty(response)) {
                            UserService.getInstance(context).processUserDetailsResponse(response);
                        }
                    }
                }
                if (!userDetailsList.isEmpty() && userDetailsList.size() > 0) {
                    UserDetailListFeed userDetailListFeed = new UserDetailListFeed();
                    userDetailListFeed.setContactSync(true);
                    userDetailListFeed.setUserIdList(userDetailsList);
                    String jsonFromObject = GsonUtils.getJsonFromObject(userDetailListFeed, userDetailListFeed.getClass());
                    response = httpRequestUtils.postData(getUserDetailsListPostUrl(), "application/json", "application/json", jsonFromObject);

                    Utils.printLog(context, TAG, "User details response is :" + response);
                    if (TextUtils.isEmpty(response) || response.contains("<html>")) {
                        return null;
                    }

                    if (!TextUtils.isEmpty(response)) {
                        UserService.getInstance(context).processUserDetailsResponse(response);
                    }
                }
                return response;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //ApplozicInternal: default
    /**
     * Gets a list of online users from the server.
     *
     * <p>Note: This is a network method. Run it asynchronously.</p>
     *
     * @param numberOfUser the number of users to get
     * @return a map with key = userId, value = connectedStatus of the online users. ignore the value (connectedStatus)
     */
    public Map<String, String> getOnlineUserList(int numberOfUser) {
        Map<String, String> info = new HashMap<String, String>();
        try {
            String response = httpRequestUtils.getResponse(getOnlineUserListUrl() + "?startIndex=0&pageSize=" + numberOfUser, "application/json", "application/json");
            if (response != null && !MobiComKitConstants.ERROR.equals(response)) {
                JSONObject jsonObject = new JSONObject(response);
                Iterator iterator = jsonObject.keys();
                while (iterator.hasNext()) {
                    String key = (String) iterator.next();
                    String value = jsonObject.getString(key);
                    info.put(key, value);
                }
                return info;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return info;
    }

    /**
     * Gets list of registered users from the server.
     *
     * Convert string response to {@link com.applozic.mobicomkit.feed.RegisteredUsersApiResponse}:
     * <code>
     *     RegisteredUsersApiResponse apiResponse = (RegisteredUsersApiResponse) GsonUtils.getObjectFromJson(response, RegisteredUsersApiResponse.class);
     * </code>
     *
     * <p>Note: This is a network method. Run it asynchronously.</p>
     *
     * @param startTime the time in milliseconds to get the registered users from. eg: a start
     *                  time of X will get all the registered users that were registered after time X
     * @param pageSize the number of users to get
     * @return the json api response. convert to {@link com.applozic.mobicomkit.feed.RegisteredUsersApiResponse}
     */
    public String getRegisteredUsers(Long startTime, int pageSize) {
        String response = null;
        try {
            String url = "?pageSize=" + pageSize;
            if (startTime > 0) {
                url = url + "&startTime=" + startTime;
            }
            response = httpRequestUtils.getResponse(getRegisteredUserListUrl() + url, "application/json", "application/json");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    //ApplozicInternal: default
    /**
     * Updates details of the current user.
     *
     * Use {@link UserService#updateDisplayNameORImageLink(String, String, String, String, String, String, Map, String)} instead.
     *
     * <p>Note: This is a network method. Run it asynchronously.</p>
     *
     * <p>Despite the name, this method updates more than just the display name or image link. See the parameters.</p>
     */
    public ApiResponse updateDisplayNameORImageLink(String displayName, String profileImageLink, String status, String contactNumber, String emailId, Map<String, String> metadata, String userId) {
        AlUserUpdate userUpdate = new AlUserUpdate();
        try {
            if (!TextUtils.isEmpty(displayName)) {
                userUpdate.setDisplayName(displayName);
            }
            if (!TextUtils.isEmpty(profileImageLink)) {
                userUpdate.setImageLink(profileImageLink);
            }
            if (!TextUtils.isEmpty(status)) {
                userUpdate.setStatusMessage(status);
            }
            if (!TextUtils.isEmpty(contactNumber)) {
                userUpdate.setPhoneNumber(contactNumber);
            }
            if (!TextUtils.isEmpty(emailId)) {
                userUpdate.setEmail(emailId);
            }
            if (metadata != null && !metadata.isEmpty()) {
                userUpdate.setMetadata(metadata);
            }

            String response = httpRequestUtils.postData(getUserProfileUpdateUrl(), "application/json", "application/json", GsonUtils.getJsonFromObject(userUpdate, AlUserUpdate.class), userId);
            Utils.printLog(context, TAG, response);
            return ((ApiResponse) GsonUtils.getObjectFromJson(response, ApiResponse.class));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Sends a mute notification request for the given userId.
     *
     * <p>The current user will be taken from the user shared preferences.</p>
     *
     * <p>Note: This is a network method. Run it asynchronously.</p>
     *
     * @param userId the user id of the user to mute
     * @param notificationAfterTime the time (in milliseconds) to mute the user for
     * @return the api response, use {@link ApiResponse#isSuccess()} to check for success
     */
    public ApiResponse muteUserNotifications(String userId, Long notificationAfterTime) {
        if (userId == null || notificationAfterTime == null) {
            return null;
        }

        JSONObject jsonFromObject = new JSONObject();

        try {
            String url = getMuteUserUrl() + "?userId=" + userId + "&notificationAfterTime=" + notificationAfterTime;
            String response = httpRequestUtils.postData(url, "application/json", "application/json", jsonFromObject.toString());
            Utils.printLog(context, TAG, "Mute user chat response : " + response);

            if (!TextUtils.isEmpty(response)) {
                return (ApiResponse) GsonUtils.getObjectFromJson(response, ApiResponse.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    /**
     * Gets the list of all the users that have been muted by the current(logged) user.
     *
     * <p>The muted user ids can be accessed from the array returned by using {@link MuteUserResponse#getUserId()}.</p>
     *
     * <p>Note: This is a network method. Run it asynchronously.</p>
     *
     * @return an array of {@link MuteUserResponse}
     */
    public MuteUserResponse[] getMutedUserList() {
        try {
            String response = httpRequestUtils.getResponse(getMutedUserListUrl(), "application/json", "application/json");
            Utils.printLog(context, TAG, "Muted users list reponse : " + response);

            if (!TextUtils.isEmpty(response)) {
                return (MuteUserResponse[]) GsonUtils.getObjectFromJson(response, MuteUserResponse[].class);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    //ApplozicInternal: default
    /**
     * @deprecated This method is not longer used and will be removed soon.
     */
    @Deprecated
    public ApiResponse getUserReadServerCall() {
        String response = null;
        ApiResponse apiResponse = null;
        try {
            response = httpRequestUtils.getResponse(getUserReadUrl(), null, null);
            if (response != null) {
                apiResponse = (ApiResponse) GsonUtils.getObjectFromJson(response, ApiResponse.class);
            }
            Utils.printLog(context, TAG, "User read response: " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return apiResponse;
    }

    //ApplozicInternal: default
    /**
     * @deprecated This method is not longer used and will be removed soon.
     */
    @Deprecated
    public String updateUserPassword(String oldPassword, String newPassword) {
        if (TextUtils.isEmpty(oldPassword) || TextUtils.isEmpty(newPassword)) {
            return null;
        }
        String response = "";
        ApiResponse apiResponse = null;
        try {
            response = httpRequestUtils.getResponse(getUpdateUserPasswordUrl() + "?oldPassword=" + oldPassword + "&newPassword=" + newPassword, "application/json", "application/json");
            if (TextUtils.isEmpty(response)) {
                return null;
            }
            apiResponse = (ApiResponse) GsonUtils.getObjectFromJson(response, ApiResponse.class);
            if (apiResponse != null && apiResponse.isSuccess()) {
                return apiResponse.getStatus();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Will return a list of contacts matching the search term , from the server. Search happens by user-id.
     *
     * <p>Note: This is a network method. Run it asynchronously.</p>
     *
     * To get result use:
     * <code>
     *     String userDetails = apiResponse.getResponse();
     *     UserDetail[] userDetails = (UserDetail[]) GsonUtils.getObjectFromJson(GsonUtils.getJsonFromObject(userDetails, List.class), UserDetail[].class);
     * </code>
     *
     * <p>Note: This is a network method. Run it asynchronously.</p>
     *
     * @param searchString the search term
     * @return the api response. use {@link ApiResponse#isSuccess()} to check for success. cast the result from {@link ApiResponse#getResponse()}
     * to a {@link UserDetail} array to get the list in usable form
     * @throws ApplozicException when the backend returns an error response
     */
    public ApiResponse getUsersBySearchString(String searchString) throws ApplozicException {
        if (TextUtils.isEmpty(searchString)) {
            return null;
        }
        String response;
        ApiResponse apiResponse;

        try {
            response = httpRequestUtils.getResponse(getUserSearchUrl() + "?name=" + URLEncoder.encode(searchString, "UTF-8"), "application/json", "application/json");
            if (TextUtils.isEmpty(response)) {
                return null;
            }
            Utils.printLog(context, TAG, "Search user response : " + response);
            apiResponse = (ApiResponse) GsonUtils.getObjectFromJson(response, ApiResponse.class);
        } catch (Exception e) {
            throw new ApplozicException(e.getMessage());
        }

        return apiResponse;
    }
}
