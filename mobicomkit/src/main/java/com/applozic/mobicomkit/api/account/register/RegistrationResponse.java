package com.applozic.mobicomkit.api.account.register;

import android.text.TextUtils;

import com.applozic.mobicommons.json.JsonMarker;

import java.util.Map;

/**
 * This is the model class for the registration response returned from a register/login/update request.
 *
 * <p>You will rarely need to use fields in this object.</p>
 */
public class RegistrationResponse extends JsonMarker {

    private String message;
    private String deviceKey;
    private String userKey;
    private String userId;
    private String contactNumber;
    private Long lastSyncTime;
    private Long currentTimeStamp;
    private String displayName;
    private String notificationResponse;
    private String brokerUrl;
    private String imageLink;
    private String statusMessage;
    private String encryptionKey;
    private String userEncryptionKey;
    private boolean enableEncryption;
    private Map<String, String> metadata;
    private Short roleType;
    private String authToken;
    private Short pricingPackage = PricingType.STARTER.getValue();
    private Long notificationAfter;
    private boolean deactivate;

    /**
     * This is the response message.
     */
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * This is the identification used for a user's device.
     */
    public String getDeviceKey() {
        return deviceKey;
    }

    public void setDeviceKey(String deviceKeyString) {
        this.deviceKey = deviceKeyString;
    }

    /**
     * The user identification.
     */
    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String suUserKeyString) {
        this.userKey = suUserKeyString;
    }

    /**
     * Gets is the contact number for the concerned user.
     */
    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    /**
     * @deprecated This method and concerning field is no longer used and will be removed soon.
     */
    @Deprecated
    public Long getLastSyncTime() {
        return lastSyncTime == null ? 0L : lastSyncTime;
    }

    /**
     * @deprecated This method and concerning field is no longer used and will be removed soon.
     */
    @Deprecated
    public void setLastSyncTime(Long lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
    }

    /**
     * Gets the timestamp of the last sync that took place.
     */
    public Long getCurrentTimeStamp() {
        return currentTimeStamp == null ? 0L : currentTimeStamp;
    }

    public void setCurrentTimeStamp(Long currentTimeStamp) {
        this.currentTimeStamp = currentTimeStamp;
    }

    /**
     * Used internally.
     */
    public String getNotificationResponse() {
        return notificationResponse;
    }

    public void setNotificationResponse(String notificationResponse) {
        this.notificationResponse = notificationResponse;
    }

    /**
     * Used internally.
     * Gets the URL for the MQTT broker, used for the web-socket connection.
     */
    public String getBrokerUrl() {
        return brokerUrl;
    }

    public void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }

    /**
     * @deprecated Backend changes can cause this method to return a wrong result.
     * You can use the {@link RegistrationResponse#message} field to get response details.
     */
    @Deprecated
    public boolean isPasswordInvalid() {
        return (!TextUtils.isEmpty(message) && ("PASSWORD_INVALID".equals(message) || "PASSWORD_REQUIRED".equals(message)));
    }

    /**
     * The pricing of your Applozic account.
     */
    public Short getPricingPackage() {
        return pricingPackage;
    }

    public void setPricingPackage(Short pricingPackage) {
        this.pricingPackage = pricingPackage;
    }

    /**
     * User-id of the concerned {@link com.applozic.mobicomkit.api.account.user.User}.
     */
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Display name of the concerned {@link com.applozic.mobicomkit.api.account.user.User}.
     */
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Display profile image link of the concerned {@link com.applozic.mobicomkit.api.account.user.User}.
     */
    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public boolean isEnableEncryption() {
        return enableEncryption;
    }

    public void setEnableEncryption(boolean enableEncryption) {
        this.enableEncryption = enableEncryption;
    }

    /**
     * Get the role type of the concerned {@link com.applozic.mobicomkit.api.account.user.User}.
     */
    public Short getRoleType() {
        return roleType;
    }

    public void setRoleType(Short roleType) {
        this.roleType = roleType;
    }

    public String getUserEncryptionKey() {
        return userEncryptionKey;
    }

    public void setUserEncryptionKey(String userEncryptionKey) {
        this.userEncryptionKey = userEncryptionKey;
    }

    /**
     * Gets the metadata sent with the response, if any.
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    /**
     * Gets the time (milliseconds) after which notifications are set to be received by the concerned user.
     */
    public Long getNotificationAfter() {
        return notificationAfter;
    }

    public void setNotificationAfter(Long notificationAfter) {
        this.notificationAfter = notificationAfter;
    }

    /**
     * Gets the JWT authentication token. This token is passed in the API call header to
     * authenticate the requests.
     */
    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    /**
     * Returns true if the concerned user has been deactivated.
     */
    public boolean isDeactivate() {
        return deactivate;
    }

    public void setDeactivate(boolean deactivate) {
        this.deactivate = deactivate;
    }

    /**
     * Pricing types for Applozic accounts.
     */
    public static enum PricingType {

        CLOSED(Short.valueOf("-1")), BETA(Short.valueOf("0")), STARTER(Short.valueOf("1")), LAUNCH(Short.valueOf("2")), GROWTH(Short.valueOf("3")), ENTERPRISE(
                Short.valueOf("4")), UNSUBSCRIBED(Short.valueOf("6"));
        private final Short value;

        private PricingType(Short c) {
            value = c;
        }

        public Short getValue() {
            return value;
        }
    }

    /**
     * Checks if the object is one for a successful registration.
     *
     * <p>This can be for user data updates, user registration and anonymous registration.</p>
     *
     * @return true/false accordingly
     */
    public boolean isRegistrationSuccess() {
        return (!TextUtils.isEmpty(message) && (SuccessResponse.UPDATED.getValue().equals(message) || SuccessResponse.REGISTERED.getValue().equals(message) || SuccessResponse.REGISTERED_WITHOUTREGISTRATIONID.getValue().equals(message)));
    }

    /**
     * These constants are used internally.
     *
     * Registration success response constants (that will be received from server).
     */
    public static enum SuccessResponse {
        UPDATED("UPDATED"), REGISTERED("REGISTERED"), REGISTERED_WITHOUTREGISTRATIONID("REGISTERED.WITHOUTREGISTRATIONID");
        private final String value;

        private SuccessResponse(String c) {
            value = c;
        }

        public String getValue() {
            return value;
        }
    }

    @Override
    public String toString() {
        return "RegistrationResponse{" +
                "message='" + message + '\'' +
                ", deviceKey='" + deviceKey + '\'' +
                ", userKey='" + userKey + '\'' +
                ", userId='" + userId + '\'' +
                ", contactNumber='" + contactNumber + '\'' +
                ", lastSyncTime=" + lastSyncTime +
                ", currentTimeStamp=" + currentTimeStamp +
                ", displayName='" + displayName + '\'' +
                ", notificationResponse='" + notificationResponse + '\'' +
                ", brokerUrl='" + brokerUrl + '\'' +
                ", imageLink='" + imageLink + '\'' +
                ", statusMessage='" + statusMessage + '\'' +
                ", encryptionKey='" + encryptionKey + '\'' +
                ", userEncryptionKey='" + userEncryptionKey + '\'' +
                ", enableEncryption=" + enableEncryption +
                ", metadata=" + metadata +
                ", roleType=" + roleType +
                ", authToken='" + authToken + '\'' +
                ", pricingPackage=" + pricingPackage +
                ", notificationAfter=" + notificationAfter +
                '}';
    }
}
