package com.applozic.mobicomkit.api.account.user;

import android.content.Context;
import android.text.TextUtils;

import com.applozic.mobicomkit.annotations.ApplozicInternal;
import com.applozic.mobicomkit.listners.AlLoginHandler;
import com.applozic.mobicommons.json.JsonMarker;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * The User class will be used to store data for the current Applozic user(i.e. the user using the application).
 *
 * <p>A User is the entity logged onto Applozic. The User sends and receives messages to and from other Contacts and participates
 * in group chats.
 * Before a User can do all this, it must be authenticated {@link com.applozic.mobicomkit.Applozic#connectUser(Context, User, AlLoginHandler)}.
 *
 * <p>In context to a <i>User</i> that is sending messages etc, other(receiver) Users are {@link com.applozic.mobicommons.people.contact.Contact}s.
 * However, for non-client jargon, a <i>User</i> can be <b>ANY</b> entity using Applozic.
 * Contacts exist mainly for the client SDKs.</p>
 *
 * <p>A user is identified by it's {@link User#userId}. To create a user do this:</p>
 * <code>
 *     User user = new User();
 *     user.setUserId(“userId”); //mandatory
 *     user.setDisplayName(“displayName”);
 *     user.setEmail(“email”);
 *     user.setAuthenticationTypeId(User.AuthenticationType.APPLOZIC.getValue());  //use this by default
 *     user.setPassword("password");
 *     user.setImageLink("url/to/profile/image");
 * </code>
 *
 * See the respective <b>getters</b> of the various fields for details.
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

    @ApplozicInternal
    public List<String> getFeatures() {
        return features;
    }

    @ApplozicInternal
    public void setFeatures(List<String> features) {
        this.features = features;
    }

    /**
     * Gets the status string (similar to the old <i>Whatsapp</i> statuses) for the user.
     */
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Gets the local path to the user's profile picture.
     */
    public String getLocalImageUri() {
        return localImageUri;
    }

    public void setLocalImageUri(String localImageUri) {
        this.localImageUri = localImageUri;
    }

    /**
     * Gets the user-id of the user. This is the primary identification for a user and is unique across
     * an Applozic application.
     */
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Gets the email string for the user.
     */
    public String getEmail() {
        return email;
    }

    public void setEmail(String emailId) {
        this.email = emailId;
    }

    /**
     * Gets the password for the user.
     */
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the display name string for the user.
     */
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the registration-id for the user. This id is used to identify a session and for push notifications(real-time updates).
     */
    public String getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
    }

    /**
     * Gets the contact/phone number string for the user.
     */
    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    /**
     * Gets the application-id of the Application application in which this user exists.
     *
     * <p>The application-id or application-key is used to identify an Applozic application.
     * It can be considered as a container for all your messages, users and all other data.</p>
     */
    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    @ApplozicInternal
    public String getCountryCode() {
        return countryCode;
    }

    @ApplozicInternal
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    @ApplozicInternal
    public Short getPrefContactAPI() {
        return prefContactAPI;
    }

    @ApplozicInternal
    public void setPrefContactAPI(Short prefContactAPI) {
        this.prefContactAPI = prefContactAPI;
    }

    @ApplozicInternal
    public boolean isEmailVerified() {
        return emailVerified;
    }

    @ApplozicInternal
    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    @ApplozicInternal
    public String getTimezone() {
        return timezone;
    }

    @ApplozicInternal
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    @ApplozicInternal
    public Short getAppVersionCode() {
        return appVersionCode;
    }

    @ApplozicInternal
    public void setAppVersionCode(Short appVersionCode) {
        this.appVersionCode = appVersionCode;
    }

    @ApplozicInternal
    public String getRoleName() {
        return roleName;
    }

    @ApplozicInternal
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    @ApplozicInternal
    public Short getDeviceType() {
        return deviceType;
    }

    @ApplozicInternal
    public void setDeviceType(Short deviceType) {
        this.deviceType = deviceType;
    }

    /**
     * See {@link User.AuthenticationType}.
     */
    public Short getAuthenticationTypeId() {
        return authenticationTypeId;
    }

    public void setAuthenticationTypeId(Short authenticationTypeId) {
        this.authenticationTypeId = authenticationTypeId;
    }

    @ApplozicInternal
    public String getAppModuleName() {
        return appModuleName;
    }

    @ApplozicInternal
    public void setAppModuleName(String appModuleName) {
        this.appModuleName = appModuleName;
    }

    /**
     * Remote URL to the display/profile image of the user.
     */
    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    @ApplozicInternal
    public boolean isEnableEncryption() {
        return enableEncryption;
    }

    @ApplozicInternal
    public void setEnableEncryption(boolean enableEncryption) {
        this.enableEncryption = enableEncryption;
    }

    @ApplozicInternal
    public Short getUserTypeId() {
        return userTypeId;
    }

    @ApplozicInternal
    public void setUserTypeId(Short userTypeId) {
        this.userTypeId = userTypeId;
    }

    @ApplozicInternal
    public String getNotificationSoundFilePath() {
        return notificationSoundFilePath;
    }

    @ApplozicInternal
    public void setNotificationSoundFilePath(String notificationSoundFilePath) {
        this.notificationSoundFilePath = notificationSoundFilePath;
    }

    @ApplozicInternal
    public Short getPushNotificationFormat() {
        return pushNotificationFormat;
    }

    @ApplozicInternal
    public void setPushNotificationFormat(Short pushNotificationFormat) {
        this.pushNotificationFormat = pushNotificationFormat;
    }

    @ApplozicInternal
    public Long getLastMessageAtTime() {
        return lastMessageAtTime;
    }

    @ApplozicInternal
    public void setLastMessageAtTime(Long lastMessageAtTime) {
        this.lastMessageAtTime = lastMessageAtTime;
    }

    @ApplozicInternal
    public Map<String, String> getMetadata() {
        return metadata;
    }

    @ApplozicInternal
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    @ApplozicInternal
    public void setRoleType(Short roleType) {
        this.roleType = roleType;
    }

    @ApplozicInternal
    public Short getRoleType() {
        return roleType;
    }

    @ApplozicInternal
    public String getAlBaseUrl() {
        return alBaseUrl;
    }

    @ApplozicInternal
    public void setAlBaseUrl(String alBaseUrl) {
        this.alBaseUrl = alBaseUrl;
    }

    @ApplozicInternal
    public String getKmBaseUrl() {
        return kmBaseUrl;
    }

    @ApplozicInternal
    public void setKmBaseUrl(String kmBaseUrl) {
        this.kmBaseUrl = kmBaseUrl;
    }

    @ApplozicInternal
    public boolean isSkipDeletedGroups() {
        return skipDeletedGroups;
    }

    @ApplozicInternal
    public void setSkipDeletedGroups(boolean skipDeletedGroups) {
        this.skipDeletedGroups = skipDeletedGroups;
    }

    @ApplozicInternal
    public boolean isHideActionMessages() {
        return hideActionMessages;
    }

    @ApplozicInternal
    public void setHideActionMessages(boolean hideActionMessages) {
        this.hideActionMessages = hideActionMessages;
    }

    @ApplozicInternal
    public String getUserIdRegex() {
        return userIdRegex;
    }

    @ApplozicInternal
    public void setUserIdRegex(String regex) {
        this.userIdRegex = regex;
    }

    @ApplozicInternal
    public boolean isValidUserId() {
        if (TextUtils.isEmpty(userIdRegex)) {
            setUserIdRegex(DEFAULT_USER_ID_REGEX);
        }
        return Pattern.compile(userIdRegex).matcher(getUserId()).matches();
    }

    @ApplozicInternal
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

    @ApplozicInternal
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
     * <p>User.AuthenticationType.APPLOZIC.getValue() tells the Applozic backend to handle the authentication itself. This is the default. Use this if you do not know what you should be using.</p>
     *
     * <p>User.AuthenticationType.CLIENT.getValue() tells the Applozic backend that you will handle authentication yourself and provide the access-token. In this case, pass the access token in the user’s `password` field.</p>
     * <p>Refer to this(https://docs.applozic.com/docs/access-token-url) link to know more about how to implement your own authentication.</p>
     */
    public enum AuthenticationType {

        CLIENT(Short.valueOf("0")), APPLOZIC(Short.valueOf("1")), FACEBOOK(Short.valueOf("2"));
        private Short value;

        AuthenticationType(Short c) {
            value = c;
        }

        public Short getValue() {
            return value;
        }
    }

    @ApplozicInternal
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

    @ApplozicInternal
    public enum RoleType {
        BOT(Short.valueOf("1")),
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

    @ApplozicInternal
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

    @ApplozicInternal
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
