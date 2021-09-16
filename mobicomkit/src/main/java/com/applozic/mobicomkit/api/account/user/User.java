package com.applozic.mobicomkit.api.account.user;

import android.content.Context;
import android.text.TextUtils;

import com.applozic.mobicomkit.Applozic;
import com.applozic.mobicomkit.listners.AlLoginHandler;
import com.applozic.mobicommons.json.JsonMarker;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Used to store data for the current logged-in <i>user<i/>.
 *
 * <p>A <code>User</code> sends and receives messages from other users in 1-to-1 and group chats.
 * However, before that, it must be authenticated using {@link Applozic#connectUser(Context, User, AlLoginHandler)}.</p>
 *
 * <p>A <i>user</i> is identified by it's {@link User#userId} which is <i>unique</i> for an application (See {@link Applozic#getApplicationKey()}).</p>
 *
 * <p>You can create a user like this:</p>
 * <code>
 *     User user = new User();
 *     user.setUserId(“userId”); //mandatory
 *     user.setDisplayName(“displayName”); //optional
 *     user.setEmail(“email”); //optional
 *     user.setAuthenticationTypeId(User.AuthenticationType.APPLOZIC.getValue()); //use this by default
 *     user.setPassword("password"); //optional
 *     user.setImageLink("url/to/profile/image"); //optional
 * </code>
 *
 * <p>Also see {@link com.applozic.mobicommons.people.contact.Contact}.</p>
 */
public class User extends JsonMarker {

    private static final String DEFAULT_USER_ID_REGEX = "^[a-zA-Z0-9_+#@.?|=;-]+$";
    private String userIdRegex;
    private String userId;
    private String email;
    private String password;
    private String registrationId;
    private String applicationId;
    private String contactNumber;
    private String countryCode;
    private Short prefContactAPI = Short.valueOf("2");
    private boolean emailVerified = true;
    private String timezone;
    private Short appVersionCode;
    private String roleName = "USER";
    private Short deviceType;
    private String imageLink;
    private boolean enableEncryption;
    private Short pushNotificationFormat;
    private Short authenticationTypeId = AuthenticationType.CLIENT.getValue();
    private String displayName;
    private String appModuleName;
    private Short userTypeId;
    private List<String> features;
    private String notificationSoundFilePath;
    private Long lastMessageAtTime;
    private Map<String, String> metadata;
    private String alBaseUrl;
    private String kmBaseUrl;
    private String status;
    private String localImageUri;
    private boolean skipDeletedGroups;
    private boolean hideActionMessages;
    private Short roleType = RoleType.USER_ROLE.getValue();

    /**
     * @see #setFeatures(List)
     */
    public List<String> getFeatures() {
        return features;
    }

    /**
     * Sets the list of added features enabled for the user. See {@link Features}.
     *
     * <p>Note: This method only lets the SDK know that the given user needs these features. Whether they will be enabled or not depends on other factors such as if they have been setup or not, or enabled from the dashboard.</p>
     */
    public void setFeatures(List<String> features) {
        this.features = features;
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
     * Gets the local path to the user's profile/display image, if it is present.
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
     * User-id is the primary identification for a user and is unique across an application ({@link Applozic#getApplicationKey()}).
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
     * The registration-id is used to identify a user session and for push notifications (real-time updates).
     */
    public String getRegistrationId() {
        return registrationId;
    }

    /**
     * @see #getRegistrationId()
     */
    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
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
     * Gets the id of the application in which this user exists.
     *
     * <p>Both application-id and application key refer to the same thing.</p>
     *
     * @see Applozic#getApplicationKey()
     */
    public String getApplicationId() {
        return applicationId;
    }

    /**
     * @see #getApplicationId()
     */
    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    /**
     * @see #setCountryCode(String)
     */
    public String getCountryCode() {
        return countryCode;
    }

    /**
     * Sets the code of the country in which the user resides (if applicable).
     *
     * <p>You can use this method if your use case requires it. Ignore it otherwise.</p>
     */
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    /**
     * @deprecated This method is not used and will be removed soon.
     */
    @Deprecated
    public Short getPrefContactAPI() {
        return prefContactAPI;
    }

    /**
     * This is an internal method. Do not use. It will be deprecated soon.
     */
    public void  setPrefContactAPI(Short prefContactAPI) {
        this.prefContactAPI = prefContactAPI;
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
     * @see #setTimezone(String)
     */
    public String getTimezone() {
        return timezone;
    }

    /**
     * Can be used to set a timezone for your user, if your use-case requires.
     */
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    /**
     * @deprecated This method is not used and will be removed soon.
     */
    @Deprecated
    public Short getAppVersionCode() {
        return appVersionCode;
    }

    /**
     * This is an internal method. Do not use. It will be deprecated soon.
     */
    public void setAppVersionCode(Short appVersionCode) {
        this.appVersionCode = appVersionCode;
    }

    /**
     * @deprecated Role names are no longer used. Use {@link #getRoleType()} instead.
     */
    @Deprecated
    public String getRoleName() {
        return roleName;
    }

    /**
     * @deprecated Role names are no longer used. Use {@link #setRoleType(Short)} instead.
     */
    @Deprecated
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    /**
     * This is an internal method. Do not use. It will be deprecated soon.
     */
    public Short getDeviceType() {
        return deviceType;
    }

    /**
     * This is an internal method. Do not use. It will be deprecated soon.
     */
    public void setDeviceType(Short deviceType) {
        this.deviceType = deviceType;
    }

    /**
     * See {@link AuthenticationType}.
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
     * @deprecated Access to <code>appModuleName</code> is not needed.
     */
    @Deprecated
    public String getAppModuleName() {
        return appModuleName;
    }

    /**
     * This is an internal method. Do not use. It will be deprecated soon.
     */
    public void setAppModuleName(String appModuleName) {
        this.appModuleName = appModuleName;
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
     * This is an internal method. You will not need it.
     */
    public Short getUserTypeId() {
        return userTypeId;
    }

    /**
     * This is an internal method. You will not need it.
     */
    public void setUserTypeId(Short userTypeId) {
        this.userTypeId = userTypeId;
    }

    /**
     * @see #setNotificationSoundFilePath(String)
     */
    public String getNotificationSoundFilePath() {
        return notificationSoundFilePath;
    }

    /**
     * Sets the local absolute path to a custom sound that will be played whenever notifications arrive for this user.
     */
    public void setNotificationSoundFilePath(String notificationSoundFilePath) {
        this.notificationSoundFilePath = notificationSoundFilePath;
    }

    /**
     * @deprecated Multiple push-notification file formats are no longer used.
     */
    @Deprecated
    public Short getPushNotificationFormat() {
        return pushNotificationFormat;
    }

    /**
     * @deprecated Multiple push-notification file formats are no longer used.
     */
    @Deprecated
    public void setPushNotificationFormat(Short pushNotificationFormat) {
        this.pushNotificationFormat = pushNotificationFormat;
    }

    /**
     * @deprecated This is now handled locally.
     */
    @Deprecated
    public Long getLastMessageAtTime() {
        return lastMessageAtTime;
    }

    /**
     * @deprecated This is now handled locally.
     */
    @Deprecated
    public void setLastMessageAtTime(Long lastMessageAtTime) {
        this.lastMessageAtTime = lastMessageAtTime;
    }

    /**
     * @see #setMetadata(Map)
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * Can be used to add any custom data for a user that you might need to store in the form of <code>string</code> <i>key:value</i> pairs.
     */
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    /**
     * @see RoleType
     */
    public void setRoleType(Short roleType) {
        this.roleType = roleType;
    }

    /**
     * @see RoleType
     */
    public Short getRoleType() {
        return roleType;
    }

    /**
     * This is an internal method. You will not need it.
     */
    public String getAlBaseUrl() {
        return alBaseUrl;
    }

    /**
     * @deprecated This url must not be changed.
     */
    @Deprecated
    public void setAlBaseUrl(String alBaseUrl) {
        this.alBaseUrl = alBaseUrl;
    }

    /**
     * This is an internal method. You will not need it.
     */
    public String getKmBaseUrl() {
        return kmBaseUrl;
    }

    /**
     * @deprecated This url must not be changed.
     */
    public void setKmBaseUrl(String kmBaseUrl) {
        this.kmBaseUrl = kmBaseUrl;
    }

    /**
     * This is an internal method. You will not need it.
     */
    public boolean isSkipDeletedGroups() {
        return skipDeletedGroups;
    }

    /**
     * This is an internal method. You will not need it.
     */
    public void setSkipDeletedGroups(boolean skipDeletedGroups) {
        this.skipDeletedGroups = skipDeletedGroups;
    }

    /**
     * This is an internal method. You will not need it.
     */
    public boolean isHideActionMessages() {
        return hideActionMessages;
    }

    /**
     * This is an internal method. You will not need it.
     */
    public void setHideActionMessages(boolean hideActionMessages) {
        this.hideActionMessages = hideActionMessages;
    }

    /**
     * @see #setUserIdRegex(String)
     */
    public String getUserIdRegex() {
        return userIdRegex;
    }

    /**
     * Sets the regular expression that will be used to verify the user-id before the user can be authenticated.
     */
    public void setUserIdRegex(String regex) {
        this.userIdRegex = regex;
    }

    /**
     * Verifies the user-id based on the regular expression set using {@link #setUserIdRegex(String)}. If none was set then {@link #DEFAULT_USER_ID_REGEX} is used.
     *
     * @return true if the user-id matches the regex pattern
     */
    public boolean isValidUserId() {
        if (TextUtils.isEmpty(userIdRegex)) {
            setUserIdRegex(DEFAULT_USER_ID_REGEX);
        }
        return Pattern.compile(userIdRegex).matcher(getUserId()).matches();
    }

    /**
     * Encodes the user-id string to <i>application/x-www-form-urlencoded</i> using the <i>UTF-8</i> scheme.
     *
     * @param userId the user-id of the user
     * @return the UTF-8 encoded string, will return non-encoded string in case of any exception while encoding
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
     * Decodes the <i>application/x-www-form-urlencoded</i> user-id string using the <i>UTF-8</i> scheme.
     *
     * @param encodedId the UTF-8 encoded user-id
     * @return the decoded user-id, will return the non-decoded string in case of any exception while encoding such as encoding scheme mismatch
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

    /**
     * Used to tell the backend what kind of authentication the user wishes to use to be authenticated. See the types for details.
     */
    public enum AuthenticationType {
        /**
         * <p>This tells the Applozic backend that you will handle authentication yourself and provide it with the access-token/password. In this case, pass your access token in the user’s `password` field ({@link #setPassword(String)}).</p>
         * <p>Refer to this(https://docs.applozic.com/docs/access-token-url) link get more information on how to implement your own authentication.</p>
         */
        CLIENT(Short.valueOf("0")),
        /**
         * <p>This tells the Applozic backend to handle the authentication itself. This is the default. Use this if you do not know what you should be using.</p>
         */
        APPLOZIC(Short.valueOf("1")),
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
     * These are added features that <i>Applozic</i> provides.
     *
     * <p>Features are functionalities that are advanced enough to require added setup for them to work. In case of <code>IP_AUDIO_CALL<code/>
     * and <code>IP_VIDEO_CALL</code> you will need to use <i>Applozic's Audio Video Call SDK</i> that work with the Chat SDK.</p>
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
     * Roles give your user certain privileges.
     *
     * <p>You do not need to worry about any these except {@link RoleType#APPLICATION_ADMIN}.</p>
     */
    public enum RoleType {
        BOT(Short.valueOf("1")),
        /**
         * A user with this role-type can not only modify it's own data but also data for other users.
         *
         * <p>This "modifying of data" is in reference to server API calls. An application-admin can perform API calls
         * in behalf of other users too.</p>
         */
        APPLICATION_ADMIN(Short.valueOf("2")),
        USER_ROLE(Short.valueOf("3")),
        ADMIN_ROLE(Short.valueOf("4")),
        BUSINESS(Short.valueOf("5")),
        APPLICATION_BROADCASTER(Short.valueOf("6")),
        SUPPORT(Short.valueOf("7")),
        AGENT(Short.valueOf("8"));

        private Short value;

        RoleType(Short r) {
            value = r;
        }

        public Short getValue() {
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

    /**
     * @deprecated Role names are no longer used. Use {@link RoleType} instead.
     */
    @Deprecated
    public enum RoleName {
        BOT("BOT"),
        APPLICATION_ADMIN("APPLICATION_ADMIN"),
        USER("USER"),
        ADMIN("ADMIN"),
        BUSINESS("BUSINESS"),
        APPLICATION_BROADCASTER("APPLICATION_BROADCASTER"),
        SUPPORT("SUPPORT"),
        APPLICATION_WEB_ADMIN("APPLICATION_WEB_ADMIN");

        private String value;

        RoleName(String r) {
            value = r;
        }

        public String getValue() {
            return value;
        }
    }
}
