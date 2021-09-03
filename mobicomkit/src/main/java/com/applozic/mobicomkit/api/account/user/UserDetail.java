package com.applozic.mobicomkit.api.account.user;

import com.applozic.mobicomkit.annotations.ApplozicInternal;
import com.applozic.mobicommons.json.JsonMarker;

import java.util.Map;

/**
 * The <code>UserDetail</code> class is used to store <i>user/contact</i> data that is retrieved from the server or that needs to be sent to the server.
 *
 * <p>Objects of this object can be converted to a {@link com.applozic.mobicommons.people.contact.Contact} object
 * using {@link UserService#getContactFromUserDetail(UserDetail)}. However keep in mind that this
 * conversion will also add/update those user details in the local database.</p>
 */
public class UserDetail extends JsonMarker {

    private String userId;
    private boolean connected;
    private String displayName;
    private Long lastSeenAtTime;
    private String imageLink;
    private Integer unreadCount;
    private String phoneNumber;
    private String statusMessage;
    private Short userTypeId;
    private Long deletedAtTime;
    private Long notificationAfterTime;
    private Long lastMessageAtTime;
    private String email;
    private Map<String,String> metadata;
    private Short roleType;

    /**
     * Returns the time(in milliseconds) at which this user sent it's last message.
     */
    public Long getLastMessageAtTime() {
        return lastMessageAtTime;
    }

    public void setLastMessageAtTime(Long lastMessageAtTime) {
        this.lastMessageAtTime = lastMessageAtTime;
    }

    /**
     * Metadata is any custom data in the form of a <i>key:value</i> pair that can be sent with a user.
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    @ApplozicInternal
    public void setRoleType(Short roleType){
        this.roleType = roleType;
    }

    @ApplozicInternal
    public Short getRoleType(){
        return roleType;
    }

    /**
     * Unique id of a user.
     */
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Returns true if the user is online.
     */
    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    /**
     * Returns the last-seen timestamp(in milliseconds) for the user.
     *
     * <p>Last seen for a user is updated whenever the user logs-in and also when the user uses any
     * chat activity of the <i>Applozic pre-built UI Kit</i>.</p>
     */
    public Long getLastSeenAtTime() {
        return lastSeenAtTime;
    }

    public void setLastSeenAtTime(Long lastSeenAtTime) {
        this.lastSeenAtTime = lastSeenAtTime;
    }

    /**
     * Returns the count of unread messages for the user.
     */
    public Integer getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }

    /**
     * Returns the display name of the user.
     */
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the remote URL to the display/profile image of the user.
     */
    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    /**
     * Returns the phone/contact number string for the user.
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * Returns the status string (similar to the old <i>Whatsapp</i> statuses) for the user.
     */
    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    @ApplozicInternal
    public Short getUserTypeId() {
        return userTypeId;
    }

    public void setUserTypeId(Short userTypeId) {
        this.userTypeId = userTypeId;
    }

    /**
     * Returns the timestamp(in milliseconds) at which the user was deleted. This is usually done from the backend/dashboard.
     */
    public Long getDeletedAtTime() {
        return deletedAtTime;
    }

    public void setDeletedAtTime(Long deletedAtTime) {
        this.deletedAtTime = deletedAtTime;
    }

    public void setNotificationAfterTime(Long notificationAfterTime) {
        this.notificationAfterTime = notificationAfterTime;
    }

    /**
     * Returns the time(in milliseconds) until which notifications have been enabled for this user.
     */
    public Long getNotificationAfterTime() {
        return notificationAfterTime;
    }

    /**
     * Returns the email id of the user.
     */
    public String getEmailId() {
        return email;
    }

    public void setEmailId(String emailId) {
        this.email = emailId;
    }

    @Override
    public String toString() {
        return "UserDetail{" +
                "userId='" + userId + '\'' +
                ", connected=" + connected +
                ", displayName='" + displayName + '\'' +
                ", lastSeenAtTime=" + lastSeenAtTime +
                ", imageLink='" + imageLink + '\'' +
                ", unreadCount=" + unreadCount +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", statusMessage='" + statusMessage + '\'' +
                ", userTypeId=" + userTypeId +
                ", deletedAtTime=" + deletedAtTime +
                ", notificationAfterTime=" + notificationAfterTime +
                ", lastMessageAtTime=" + lastMessageAtTime +
                ", email='" + email + '\'' +
                ", metadata=" + metadata +
                ", roleType=" + roleType +
                '}';
    }
}
