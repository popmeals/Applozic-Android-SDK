package com.applozic.mobicomkit.api.account.user;

import android.content.Context;
import android.text.TextUtils;

import com.applozic.mobicomkit.Applozic;
import com.applozic.mobicomkit.ApplozicClient;
import com.applozic.mobicomkit.listners.AlLoginHandler;
import com.applozic.mobicommons.ALSpecificSettings;
import com.applozic.mobicommons.json.JsonMarker;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Pattern;

/**
 * User is an authenticated entity that can use chat functionality. It sends and receives messages in 1-to-1 and group chats.
 *
 * <p>A <i>user</i> is identified by its {@link User#userId} which is <i>unique</i> for an {@link Applozic#getApplicationKey() application} .</p>
 *
 * <p>You can create a user like this:</p>
 * <code>
 *     User user = new User();
 *     user.setUserId(“user123”);
 *     user.setAuthenticationTypeId(User.AuthenticationType.APPLOZIC.getValue());
 *     user.setDisplayName(“Shubham Tewari”);
 *     user.setEmail(“shubham@dontemailme.com”);
 *     user.setPassword("cat123");
 *     user.setImageLink("http://mywebsite.com/profile_picture.jpg");
 * </code>
 *
 * <p>Note: When creating a new user, you should set two fields:</p>
 * <ol>
 *     <li>{@link #setUserId(String)}</li>
 *     <li>{@link #setAuthenticationTypeId(Short)}. Set this to {@link AuthenticationType#APPLOZIC} if you don't know any better.</li>
 * </ol>
 * <p>All other fields are optional.</p>
 *
 * Now you can register or login that user using {@link Applozic#connectUser(Context, User, AlLoginHandler)}.</p>
 *
 * <p>Also see {@link com.applozic.mobicommons.people.contact.Contact}.</p>
 */
public class User extends JsonMarker {
    private static final String TAG = "User";

    /**
     * @see #setUserId(String)
     */
    private String userId;
    private String password;
    private Short authenticationTypeId = AuthenticationType.CLIENT.getValue();
    private String displayName;
    private String imageLink;
    private String localImageUri;
    private String email;
    private String status;
    private String contactNumber;
    private String countryCode;
    private String timezone;
    private Map<String, String> metadata;
    private String applicationId;
    private String registrationId;
    private String userIdRegex = "^[a-zA-Z0-9_+#@.?|=;-]+$";
    private boolean enableEncryption;
    private boolean emailVerified = true;
    private List<String> features;

    //internal
    private Short roleType = RoleType.USER_ROLE.getValue();
    /**
     * @see #getDeviceType()
     */
    private Short deviceType = Short.valueOf("1");
    private String appModuleName;

    //deprecated
    @Deprecated
    private String roleName = "USER";
    /**
     * Identifies user sub-types.
     */
    @Deprecated
    private Short userTypeId;
    @Deprecated
    private Long lastMessageAtTime;
    @Deprecated
    private Short pushNotificationFormat;
    @Deprecated
    private Short appVersionCode = 112; //old code. used to track API versions.
    @Deprecated
    private boolean hideActionMessages;
    @Deprecated
    private boolean skipDeletedGroups;
    @Deprecated
    private Short prefContactAPI = Short.valueOf("2");
    @Deprecated
    private String kmBaseUrl;
    @Deprecated
    private String alBaseUrl;
    @Deprecated
    private String notificationSoundFilePath;

    public User() {
        this.timezone = TimeZone.getDefault().getID();
    }

    /**
     * @see #setStatus(String)
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the <i>status</i>(think of it as a temporary description) for the user.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Gets the local path to the user's profile/display image, if it is present, or null otherwise.
     */
    public String getLocalImageUri() {
        return localImageUri;
    }

    /**
     * @see #getLocalImageUri()
     */
    public void setLocalImageUri(String localImageUri) {
        this.localImageUri = localImageUri;
    }

    /**
     * @see #setUserId(String)
     */
    public String getUserId() {
        return userId;
    }

    /**
     * User-id is the primary identification for a user and is unique.
     *
     * <p>When you create a new user, you need to give it a <i>user-id</i>. This is mandatory.
     * And for all future references to that user, this <i>user-id</i> will be needed by both you and the SDK.</p>
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String emailId) {
        this.email = emailId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @see #setContactNumber(String)
     */
    public String getContactNumber() {
        return contactNumber;
    }

    /**
     * Sets the phone number string for the user.
     */
    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    /**
     * @see #setCountryCode(String)
     */
    public String getCountryCode() {
        return countryCode;
    }

    /**
     * If your use case requires it, you can set the code of the country the user resides in.
     */
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    /**
     * @see #setEmailVerified(boolean)
     */
    public boolean isEmailVerified() {
        return emailVerified;
    }

    /**
     * Set this to true if you have verified the email for this user.
     */
    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    /**
     * See {@link AuthenticationType}. This is set to {@link AuthenticationType#CLIENT} by default. You probably want to use {@link AuthenticationType#APPLOZIC} instead.
     */
    public Short getAuthenticationTypeId() {
        return authenticationTypeId;
    }

    /**
     * See {@link AuthenticationType}.
     */
    public void setAuthenticationTypeId(Short authenticationTypeId) {
        this.authenticationTypeId = authenticationTypeId;
    }

    /**
     * Remote URL to the profile image of the user.
     */
    public String getImageLink() {
        return imageLink;
    }

    /**
     * @see #getImageLink()
     */
    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    /**
     * Is message encryption enabled for this user.
     *
     * <p>Contact <i>support@applozic.com</i> for details on encryption.</p>
     */
    public boolean isEnableEncryption() {
        return enableEncryption;
    }

    /**
     * Set to true to request the backend to enable message encryption for this user.
     *
     * <p>Note: This does not guarantee encryption. Contact <i>support@applozic.com</i> for details on encryption.</p>
     */
    public void setEnableEncryption(boolean enableEncryption) {
        this.enableEncryption = enableEncryption;
    }

    /**
     * @see #setMetadata(Map)
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * Can be used to add any custom data for a user that you might need to store in the form of <i>key:value</i> pairs.
     */
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    /**
     * @see #setFeatures(List)
     */
    public List<String> getFeatures() {
        return features;
    }

    /**
     * Sets the added {@link Features} enabled for this user. These are functionalities that are advanced enough to require added setup to work.
     *
     * <p>Note: This method only lets the SDK know that the given user needs these features. Whether they will be enabled or not depends on other factors such as if they have been set up or not, or enabled from the dashboard.</p>
     */
    public void setFeatures(List<String> features) {
        this.features = features;
    }

    /**
     * @see #setUserIdRegex(String)
     */
    public String getUserIdRegex() {
        return userIdRegex;
    }

    /**
     * Sets the regex that will be used to verify the user-id before the user can be authenticated.
     */
    public void setUserIdRegex(String regex) {
        this.userIdRegex = regex;
    }

    public String getTimezone() {
        return timezone;
    }

    /**
     * Gets the id of the application in which this user exists. See {@link Applozic#getApplicationKey() application key}.
     */
    public String getApplicationId() {
        return applicationId;
    }

    /**
     * The registration-id is used to identify a user session and for push notifications (real-time updates).
     */
    public String getRegistrationId() {
        return registrationId;
    }

    /**
     * Verifies whether the user-id matches the regex from {@link #userIdRegex}.
     */
    public boolean isValidUserId() {
        if (TextUtils.isEmpty(userIdRegex)) {
            Applozic.logError(TAG, "User-id regex is null or empty. The default value was overwritten.", null);
            return false;
        }
        return Pattern.matches(userIdRegex, userId);
    }

    /**
     * You typically don't need to use this directly. It's used internally by Applozic to URL-encode + and # signs before putting them in a URL, because otherwise the URL would be invalid. This function uses UTF-8; if it's not supported, the original un-encoded user-id is returned.
     */
    public static String getEncodedUserId(String userId) {
        if (!TextUtils.isEmpty(userId) && (userId.contains("+") || userId.contains("#"))) {
            try {
                return URLEncoder.encode(userId, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return userId;
    }

    /**
     * Opposite of {@link #getEncodedUserId}.
     */
    public static String getDecodedUserId(String encodedId) {
        if (!TextUtils.isEmpty(encodedId)) {
            try {
                return URLDecoder.decode(encodedId, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return encodedId;
    }

    public enum AuthenticationType {
        /**
         * <p>Tells Applozic to handle the authentication itself. Use this if you do not know what you should be using.</p>
         */
        APPLOZIC(Short.valueOf("1")),

       /**
         * <p>Tells Applozic that you will handle authentication yourself using ({@link #setPassword(String)}).</p>
         * <p>This <a href="https://docs.applozic.com/docs/access-token-url">link</a> tells you how to implement your own authentication.</p>
         */
        CLIENT(Short.valueOf("0")),

        /**
         * @deprecated This type is not used anymore.
         */
        @Deprecated
        FACEBOOK(Short.valueOf("2"));

        private Short value;

        AuthenticationType(Short c) {
            value = c;
        }

        public Short getValue() {
            return value;
        }
    }

    /**
     * These are added functionalities that are advanced enough to require added setup for them to work. In case of <code>IP_AUDIO_CALL<code/>
     * and <code>IP_VIDEO_CALL</code> you will need to use <i>Applozic's Audio Video Call SDK</i> that works with the Chat SDK.</p>
     */
    public enum Features {
        IP_AUDIO_CALL("100"), IP_VIDEO_CALL("101");
        private String value;

        Features(String c) {
            value = c;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * This is an internal method. Do not use.
     */
    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    /**
     * This is an internal method. You will not need it.
     *
     * @see RoleType
     */
    public void setRoleType(Short roleType) {
        this.roleType = roleType;
    }

    /**
     * This is an internal method. You will not need it.
     *
     * @see RoleType
     */
    public Short getRoleType() {
        return roleType;
    }

    /**
     * This is an internal method. Do not use.
     */
    public void setAppModuleName(String appModuleName) {
        this.appModuleName = appModuleName;
    }

    /**
     * This is an internal method. You will not need it.
     *
     * @see #getRegistrationId()
     */
    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
    }

    /**
     * Internal. 0 = Web, 1 = Android, 4 = iOS.
     */
    public Short getDeviceType() {
        return deviceType;
    }

    /**
     * This is an internal class. You will not need it.
     */
    public enum RoleType {
        USER_ROLE(Short.valueOf("3")),
        APPLICATION_ADMIN(Short.valueOf("2")),
        AGENT(Short.valueOf("8")),
        @Deprecated
        BOT(Short.valueOf("1")),
        @Deprecated
        ADMIN_ROLE(Short.valueOf("4")),
        @Deprecated
        BUSINESS(Short.valueOf("5")),
        @Deprecated
        APPLICATION_BROADCASTER(Short.valueOf("6")),
        @Deprecated
        SUPPORT(Short.valueOf("7"));

        private final Short value;

        RoleType(Short r) {
            value = r;
        }

        public Short getValue() {
            return value;
        }
    }

    /**
     * @deprecated Set internally.
     */
    @Deprecated
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    /**
     * @deprecated Use {@link ALSpecificSettings#getAlBaseUrl()} instead.
     */
    @Deprecated
    public String getAlBaseUrl() {
        return alBaseUrl;
    }

    /**
     * Base URl for all API calls.
     *
     * @deprecated Use {@link com.applozic.mobicommons.ALSpecificSettings#setAlBaseUrl(String)} instead.
     */
    @Deprecated
    public void setAlBaseUrl(String alBaseUrl) {
        this.alBaseUrl = alBaseUrl;
    }

    /**
     * @deprecated Not required at client level.
     *
     * @see RoleName
     */
    @Deprecated
    public String getRoleName() {
        return roleName;
    }

    /**
     * @deprecated Not required at client level.
     *
     * @see RoleName
     */
    @Deprecated
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    /**
     * @deprecated Use {@link #setMetadata(Map)} instead, to work with custom data such as this.
     *
     * @see #setUserTypeId(Short)
     */
    @Deprecated
    public Short getUserTypeId() {
        return userTypeId;
    }

    /**
     * @deprecated Use {@link #setMetadata(Map)} instead, to work with custom data such as this.
     *
     * Identifies user sub-types.
     */
    @Deprecated
    public void setUserTypeId(Short userTypeId) {
        this.userTypeId = userTypeId;
    }

    /**
     * @deprecated Use {@link ApplozicClient#isSkipDeletedGroups()} instead.
     */
    @Deprecated
    public boolean isSkipDeletedGroups() {
        return skipDeletedGroups;
    }

    /**
     * @deprecated Use {@link com.applozic.mobicomkit.ApplozicClient#skipDeletedGroups(boolean)} instead.
     */
    @Deprecated
    public void setSkipDeletedGroups(boolean skipDeletedGroups) {
        this.skipDeletedGroups = skipDeletedGroups;
    }

    /**
     * @deprecated Use {@link ApplozicClient#isActionMessagesHidden()} instead.
     */
    @Deprecated
    public boolean isHideActionMessages() {
        return hideActionMessages;
    }

    /**
     * @deprecated Use {@link com.applozic.mobicomkit.ApplozicClient#hideActionMessages(boolean)} instead.
     */
    @Deprecated
    public void setHideActionMessages(boolean hideActionMessages) {
        this.hideActionMessages = hideActionMessages;
    }

    /**
     * @deprecated Use {@link Applozic#getCustomNotificationSound()} instead.
     */
    @Deprecated
    public String getNotificationSoundFilePath() {
        return notificationSoundFilePath;
    }

    /**
     * @deprecated Use {@link Applozic.Store#setCustomNotificationSound(Context, String)} instead.
     *
     * Sets the local absolute path to a custom sound that will be played whenever notifications arrive for this user.
     */
    @Deprecated
    public void setNotificationSoundFilePath(String notificationSoundFilePath) {
        this.notificationSoundFilePath = notificationSoundFilePath;
    }

    /**
     * @deprecated No longer used. Will be removed soon.
     */
    @Deprecated
    public Short getPrefContactAPI() {
        return prefContactAPI;
    }

    /**
     * @deprecated No longer used. Will be removed soon.
     */
    @Deprecated
    public void  setPrefContactAPI(Short prefContactAPI) {
        this.prefContactAPI = prefContactAPI;
    }

    /**
     * @deprecated No longer used. Will be removed soon.
     */
    @Deprecated
    public Short getAppVersionCode() {
        return appVersionCode;
    }

    /**
     * @deprecated No longer used. Will be removed soon.
     */
    @Deprecated
    public void setAppVersionCode(Short appVersionCode) {
        this.appVersionCode = appVersionCode;
    }

    /**
     * @deprecated No longer used. Will be removed soon.
     */
    @Deprecated
    public void setKmBaseUrl(String kmBaseUrl) {
        this.kmBaseUrl = kmBaseUrl;
    }

    /**
     * @deprecated No longer used. Will be removed soon.
     */
    @Deprecated
    public String getKmBaseUrl() {
        return kmBaseUrl;
    }

    /**
     * @deprecated No longer used. Will be removed soon.
     */
    @Deprecated
    public Short getPushNotificationFormat() {
        return pushNotificationFormat;
    }

    /**
     * @deprecated No longer used. Will be removed soon.
     */
    @Deprecated
    public void setPushNotificationFormat(Short pushNotificationFormat) {
        this.pushNotificationFormat = pushNotificationFormat;
    }

    /**
     * @deprecated This is now handled internally.
     */
    @Deprecated
    public Long getLastMessageAtTime() {
        return lastMessageAtTime;
    }

    /**
     * @deprecated This is now handled internally.
     */
    @Deprecated
    public void setLastMessageAtTime(Long lastMessageAtTime) {
        this.lastMessageAtTime = lastMessageAtTime;
    }

    /**
     * @deprecated Access is not needed.
     */
    @Deprecated
    public String getAppModuleName() {
        return appModuleName;
    }

    /**
     * @deprecated Functionality not used anymore.
     */
    @Deprecated
    public void setDeviceType(Short deviceType) {
        this.deviceType = deviceType;
    }

    /**
     * @deprecated Not required at client level. Will be removed soon.
     *
     * Roles decide the privilege level of your user.
     */
    @Deprecated
    public enum RoleName {
        /**
         * A user with this role-name can not only modify it's own data but also data for other users.
         *
         * <p>This "modifying of data" is in reference to server API calls. An application-admin can perform API calls
         * in behalf of other users too.</p>
         */
        APPLICATION_ADMIN("APPLICATION_ADMIN"),

        /**
         * A user with this role-name can modify only its own data.
         *
         * <p>This "modifying of data" is in reference to server API calls. An application-admin can perform API calls
         * in behalf of other users too.</p>
         */
        USER("USER"),

        /** For internal use only */
        BOT("BOT"),

        /** For internal use only */
        ADMIN("ADMIN"),

        /** For internal use only */
        BUSINESS("BUSINESS"),

        /** For internal use only */
        APPLICATION_BROADCASTER("APPLICATION_BROADCASTER"),

        /** For internal use only */
        SUPPORT("SUPPORT"),

        /** For internal use only */
        APPLICATION_WEB_ADMIN("APPLICATION_WEB_ADMIN");

        private String value;

        RoleName(String r) {
            value = r;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * @deprecated The same push-notifications format is used now for all SDKs.
     */
    @Deprecated
    public enum PushNotificationFormat {
        NATIVE(Short.valueOf("0")),
        PHONEGAP(Short.valueOf("1")),
        IONIC(Short.valueOf("2")),
        NATIVESCRIPT(Short.valueOf("3")),
        PUSHY_ME(Short.valueOf("4"));

        private Short value;

        PushNotificationFormat(Short p) {
            value = p;
        }

        public Short getValue() {
            return value;
        }
    }
}
