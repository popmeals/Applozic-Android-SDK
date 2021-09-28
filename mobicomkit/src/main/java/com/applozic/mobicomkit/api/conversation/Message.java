package com.applozic.mobicomkit.api.conversation;

import android.content.Context;
import android.text.TextUtils;
import android.util.Patterns;

import com.applozic.mobicomkit.Applozic;
import com.applozic.mobicomkit.ApplozicClient;
import com.applozic.mobicomkit.api.notification.VideoCallNotificationHelper;
import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicommons.json.JsonMarker;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.attachment.FileMeta;
import com.applozic.mobicommons.commons.core.utils.DateUtils;
import com.applozic.mobicommons.file.FileUtils;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.channel.ChannelMetadata;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * A message is the data that is sent and received between two or more users.
 * It facilitates chat in one-to-one and group conversations.
 *
 * A typical message:
 * <code>
 *     Message message = new Message();
 *     message.setTo("user123"); //for a one-to-one message
 *     //message.setGroupId(123456); //or message.setClientGroupId("group123"); //for a group message
 *     message.setMessage("Have you checked out this awesome chat SDK called Applozic??");
 * </code>
 *
 * <p>Sent messages:</p>
 * <ul>
 *     <li>Return true for {@link #isTypeOutbox()}.</li>
 *     <li>Return the user-id of the receiver (in one-to-one conversations) for {@link #getTo()}.</li>
 * </ul>
 *
 * <p>Received messages:</p>
 * <ul>
 *     <li>Have a {@link #getType()} of either {@link MessageType#INBOX} or {@link MessageType#MT_INBOX}.</li>
 *     <li>Return the user-id of the sender for {@link #getTo()}.</li>
 * </ul>
 *
 * <p><b>Attachments:</b></p>
 * <p>A message can also have an attachment. Images, videos, audio, gifs, pdfs, contacts, location are supported.
 * That being said, you can send any binary file as an attachment. For these however, you must implement your custom download logic.
 * The max size for an attachment must not exceed 30 mb.</p>
 * <p>To add an attachment to a message, set the local path to it using {@link Message#setFilePaths(List)}.
 * Although this takes a list, only one attachment per message is supported.</p>
 * <p>For {@link MessageType#INBOX inbox} attachment messages, {@link #getFileMetas()} will return a non-null value. See {@link FileMeta}.</p>
 *
 * <p>To build and send a message refer to {@link MessageBuilder}.</p>
 * <p>To receive messages refer to {@link com.applozic.mobicomkit.listners.ApplozicUIListener}.</p>
 * <p>To retrieve existing messages refer to {@link ApplozicConversation}.</p>
 */
public class Message extends JsonMarker {
    //Cleanup: We have ContentType, attachment types, sentToServer etc. Do we need them?? Can the code be reduced?
    private Long createdAtTime = new Date().getTime();
    private String to;
    private String message;
    private String key;
    private String deviceKey;
    private String userKey;
    private String emailIds;
    private boolean shared;
    private boolean sent;
    private Boolean delivered;
    private Short type = MessageType.MT_OUTBOX.getValue();
    private boolean storeOnDevice;
    private String contactIds = "";
    private Integer groupId;
    private boolean sendToDevice;
    private Long scheduledAt;
    private Short source = Source.MT_MOBILE_APP.getValue();
    private Integer timeToLive;
    private boolean sentToServer = true;
    private String fileMetaKey;
    private List<String> filePaths;
    private String pairedMessageKey;
    private long sentMessageTimeAtServer;
    private boolean canceled = false;
    private String clientGroupId;
    @SerializedName("fileMeta")
    private FileMeta fileMeta;
    @SerializedName("id")
    private Long messageId;
    private Boolean read = false;
    private boolean attDownloadInProgress;
    private String applicationId;
    private Integer conversationId;
    private String topicId;
    private boolean connected = false;
    private short contentType = ContentType.DEFAULT.getValue();
    private Map<String, String> metadata = new HashMap<>();
    private short status = Status.READ.getValue();
    private boolean hidden;
    private int replyMessage;
    public static final String IMAGE = "image";
    public static final String VIDEO = "video";
    public static final String AUDIO = "audio";
    public static final String CONTACT = "contact";
    public static final String LOCATION = "location";
    public static final String OTHER = "other";
    public static final String BOT_ASSIGN = "KM_ASSIGN";
    public static final String CONVERSATION_STATUS = "KM_STATUS";
    public static final String FEEDBACK_METADATA_KEY = "feedback";
    public static final String SKIP_BOT = "skipBot";
    public static final String AL_DELETE_MESSAGE_FOR_ALL_KEY = "AL_DELETE_GROUP_MESSAGE_FOR_ALL";

    public Message() { }

    /**
     * @param to the user-id of the receiver
     * @param body the message text
     */
    public Message(String to, String body) {
        this.to = to;
        this.message = body;
    }

    /**
     * Copy constructor.
     */
    public Message(Message message) {
        //this.setKeyString(message.getKeyString());
        this.setMessage(message.getMessage());
        this.setContactIds(message.getContactIds());
        this.setCreatedAtTime(message.getCreatedAtTime());
        this.setDeviceKeyString(message.getDeviceKeyString());
        this.setSendToDevice(message.isSendToDevice());
        this.setTo(message.getTo());
        this.setType(message.getType());
        this.setSent(message.isSent());
        this.setDelivered(message.getDelivered());
        this.setStoreOnDevice(message.isStoreOnDevice());
        this.setScheduledAt(message.getScheduledAt());
        this.setSentToServer(message.isSentToServer());
        this.setSource(message.getSource());
        this.setTimeToLive(message.getTimeToLive());
        this.setFileMetas(message.getFileMetas());
        this.setFileMetaKeyStrings(message.getFileMetaKeyStrings());
        this.setFilePaths(message.getFilePaths());
        this.setGroupId(message.getGroupId());
        this.setRead(message.isRead());
        this.setApplicationId(message.getApplicationId());
        this.setContentType(message.getContentType());
        this.setStatus(message.getStatus());
        this.setConversationId(message.getConversationId());
        this.setTopicId(message.getTopicId());
        this.setMetadata(message.getMetadata());
        this.setHidden(message.hasHideKey());
    }

    /**
     * User-id of the message receiver (if {@link #isTypeOutbox()}) or sender otherwise.
     */
    public String getTo() {
        return to;
    }

    /**
     * Set the user-id of the message receiver.
     *
     * @see #setGroupId(Integer)
     * @see #setClientGroupId(String)
     */
    public void setTo(String to) {
        this.to = to;
    }

    /**
     * @see #setGroupId(Integer)
     *
     * <p>Will be null if the message is for a one-to-one conversation. In that case use {@link #getTo()}.</p>
     */
    public Integer getGroupId() {
        return groupId;
    }

    /**
     * The group-id (also called channel key) of the {@link Channel} this message is a part of.
     *
     * <p>For one-to-one conversations, ignore this and see {@link #setTo(String)} instead.</p>
     */
    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    /**
     * @see #setClientGroupId(String)
     *
     * <p>Will be null if the message is for a one-to-one conversation. In that case use {@link #getTo()}.</p>
     */
    public String getClientGroupId() {
        return clientGroupId;
    }

    /**
     * Set the {@link Channel#setClientGroupId(String)} client-group-id} of the {@link Channel} this message is a part of.
     *
     * <p>For one-to-one conversations, ignore this and see {@link #setTo(String)} instead.</p>
     */
    public void setClientGroupId(String clientGroupId) {
        this.clientGroupId = clientGroupId;
    }

    /**
     * Gets the time at which this message was sent to the backend.
     */
    public long getSentMessageTimeAtServer() {
        return sentMessageTimeAtServer;
    }

    /**
     * @see #setAttDownloadInProgress(boolean)
     */
    public boolean isAttDownloadInProgress() {
        return attDownloadInProgress;
    }

    /**
     * If required, you can set this to true while you are downloading the message attachment file.
     */
    public void setAttDownloadInProgress(boolean attDownloadInProgress) {
        this.attDownloadInProgress = attDownloadInProgress;
    }

    /**
     * Checks if the message has been read or not.
     *
     * <p><i>Outbox</i> type message will also return true.</p>
     */
    public Boolean isRead() {
        return read || isTypeOutbox() || getScheduledAt() != null;
    }

    /**
     * Checks if the message as an un-uploaded attachment.
     */
    public boolean isUploadRequired() {
        return hasAttachment() && (fileMeta == null);
    }

    /**
     * Checks if the message has an attachment.
     */
    public boolean hasAttachment() {
        return ((filePaths != null && !filePaths.isEmpty()) || (fileMeta != null));
    }

    /**
     * Checks if the attachment downloaded.
     *
     * <p>A downloaded attachment will return non-null/non-empty for {@link #getFilePaths()}.</p>
     */
    public boolean isAttachmentDownloaded() {
        return filePaths != null && !filePaths.isEmpty() && FileUtils.isFileExist(filePaths.get(0));
    }

    /**
     * Local database message primary key.
     */
    public Long getMessageId() {
        return messageId;
    }

    /**
     * Primary identification for a message. Is generated by the backend upon sending the message.
     */
    public String getKeyString() {
        return key;
    }

    /**
     * Timestamp (in milliseconds) at which the message was sent .
     */
    public Long getCreatedAtTime() {
        return createdAtTime;
    }

    /**
     * @see #getCreatedAtTime()
     *
     * <p>You do not need to set this yourself. It is set internally when sending the message.</p>
     */
    public void setCreatedAtTime(Long createdAtTime) {
        this.createdAtTime = createdAtTime;
    }

    /**
     * @see ##setMessage(String)
     */
    public String getMessage() {
        return message == null ? "" : message;
    }

    /**
     * The message's text content.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSent() {
        return sent;
    }

    public Boolean getDelivered() {
        return delivered != null ? delivered : false;
    }

    /**
     * @see MessageType
     */
    public Short getType() {
        return type;
    }

    /**
     * @see MessageType
     */
    public void setType(Short type) {
        this.type = type;
    }

    /**
     * Checks if the message has multiple recipients.
     *
     * <p>To send a message to multiple users, create a comma separated string of receiver user-ids and pass to {@link #setTo(String)}.
     * Example: <i>"userid1,userid2,userid3"</i>.</p>
     */
    public boolean isSentToMany() {
        return !TextUtils.isEmpty(getTo()) && getTo().split(",").length > 1;
    }

    /**
     * An outbox message is one that you <i>send</i> to some user/group.
     */
    public boolean isTypeOutbox() {
        return MessageType.OUTBOX.getValue().equals(type) || MessageType.MT_OUTBOX.getValue().equals(type) ||
                MessageType.OUTBOX_SENT_FROM_DEVICE.getValue().equals(type) || MessageType.CALL_OUTGOING.getValue().equals(type);
    }

    /**
     * @see #setFilePaths(List)
     */
    public List<String> getFilePaths() {
        return filePaths;
    }

    /**
     * The local path to the attachment file to upload.
     *
     * <p>You should set a list with only one item. Multiple attachments are not supported yet.</p>
     */
    public void setFilePaths(List<String> filePaths) {
        this.filePaths = filePaths;
    }

    /**
     * @see FileMeta
     */
    public FileMeta getFileMetas() {
        return fileMeta;
    }

    /**
     * @see Applozic#getApplicationKey()
     */
    public String getApplicationId() {
        return applicationId;
    }

    /**
     * @see ContentType
     */
    public short getContentType() {
        return contentType;
    }

    /**
     * @see ContentType
     */
    public void setContentType(short contentType) {
        this.contentType = contentType;
    }

    /**
     * Checks if the message string has a URL in it.
     */
    public boolean isTypeUrl() {
        return !TextUtils.isEmpty(getFirstUrl());
    }

    /**
     * Matches for and returns the first web url in the message string. Else null.
     */
    public String getFirstUrl() {
        Matcher matcher = Patterns.WEB_URL.matcher(getMessage());
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    /**
     * @see Status
     */
    public short getStatus() {
        return status;
    }

    /**
     * @see #setMetadata(Map)
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * Set any custom key-value data with your message.
     */
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    /**
     * Does the message belong to a {@link Channel group} conversation.
     */
    public boolean isGroupMessage() {
        return (this.groupId != null);
    }

    /**
     * Gets the message metadata value for the passed message metadata key.
     *
     * @return the value. will return null if the key is not present or metadata is null
     */
    public String getMetaDataValueForKey(String key) {
        return getMetadata() != null ? getMetadata().get(key) : null;
    }

    /**
     * @see ReplyMessage
     */
    public int isReplyMessage() {
        return replyMessage;
    }

    /**
     * @see ReplyMessage
     */
    public void setReplyMessage(int replyMessage) {
        this.replyMessage = replyMessage;
    }

    /**
     * This method is not required and will be deprecated soon. Do not use.
     */
    public boolean isActionMessage() {
        return getMetadata() != null && (getMetadata().containsKey(BOT_ASSIGN) || getMetadata().containsKey(CONVERSATION_STATUS) || getMetadata().containsKey(FEEDBACK_METADATA_KEY));
    }

    /**
     * Returns true if the message has been deleted for all.
     */
    public boolean isDeletedForAll() {
        return getMetadata() != null
                && getMetadata().containsKey(AL_DELETE_MESSAGE_FOR_ALL_KEY)
                && GroupMessageMetaData.TRUE.getValue().equals(getMetadata().get(AL_DELETE_MESSAGE_FOR_ALL_KEY));
    }

    /**
     * Gets the type of message attachment, either directly from {@link #getContentType()} or from the format/mime of the attached file.</p>
     *
     * @return the type (location, audio, video, contact, text, image)
     */
    public String getMessageType() {
        String type = null;

        if (getContentType() == ContentType.LOCATION.getValue()) {
            type = "location";
        } else if (getContentType() == ContentType.AUDIO_MSG.getValue()) {
            type = "audio";
        } else if (getContentType() == ContentType.VIDEO_MSG.getValue()) {
            type = "video";
        } else if (getContentType() == ContentType.ATTACHMENT.getValue()) {
            if (getFilePaths() != null) {
                String filePath = getFilePaths().get(getFilePaths().size() - 1);
                String mimeType = FileUtils.getMimeType(filePath);

                if (mimeType != null) {
                    if (mimeType.startsWith("image")) {
                        type = "image";
                    } else if (mimeType.startsWith("audio")) {
                        type = "audio";
                    } else if (mimeType.startsWith("video")) {
                        type = "video";
                    }
                }
            } else if (getFileMetas() != null) {
                if (getFileMetas().getContentType().contains("image")) {
                    type = "image";
                } else if (getFileMetas().getContentType().contains("audio")) {
                    type = "audio";
                } else if (getFileMetas().getContentType().contains("video")) {
                    type = "video";
                }
            }
        } else if (getContentType() == ContentType.CONTACT_MSG.getValue()) {
            type = "contact";
        } else {
            type = "text";
        }
        return type;
    }

    /**
     * Get the list of user-ids of the message <i><b>receivers</b></i>.
     */
    public List<String> getSenderIdListFor() {
        if (!TextUtils.isEmpty(getTo())) {
            return Arrays.asList(getTo().split("\\s*,\\s*"));
        } else if (!TextUtils.isEmpty(getContactIds())) {
            return Arrays.asList(getContactIds().split("\\s*,\\s*"));
        }

        return new ArrayList<>();
    }

    /**
     * Returns true if message has either an <i>uploaded</i> image or video or contact attachment.
     */
    public boolean isNormalAttachment() {
        if (getFileMetas() != null) {
            return !(getFileMetas().getContentType().contains("image") || getFileMetas().getContentType().contains("video") || isContactMessage());
        } else if (getFilePaths() != null) {
            String filePath = getFilePaths().get(0);
            final String mimeType = FileUtils.getMimeType(filePath);
            if (mimeType != null) {
                return !(mimeType.contains("image") || mimeType.contains("video") || isContactMessage());
            }
        }
        return false;
    }

    /**
     * Set this to identify a inbox/outbox/date type message.
     */
    public enum MessageType {
        /**
         * Messages received. Use this or {@link #MT_INBOX}.
         */
        INBOX(Short.valueOf("0")),
        /**
         * Messages sent. Use this or {@link #MT_OUTBOX}.
         */
        OUTBOX(Short.valueOf("1")),
        /**
         * Messages received. Use this or {@link #INBOX}.
         */
        MT_INBOX(Short.valueOf("4")),
        /**
         * Messages Sent. Use this or {@link #OUTBOX}.
         */
        MT_OUTBOX(Short.valueOf("5")),
        /**
         * Used for messages storing dates.
         */
        DATE_TEMP(Short.valueOf("100")),
        /**
         * Internal. Do not use.
         */
        CALL_INCOMING(Short.valueOf("6")),
        /**
         * Internal. Do not use.
         */
        CALL_OUTGOING(Short.valueOf("7")),
        /**
         * Internal. Do not use.
         */
        DRAFT(Short.valueOf("2")),
        /**
         * Internal. Do not use.
         */
        OUTBOX_SENT_FROM_DEVICE(Short.valueOf("3"));

        private Short value;

        MessageType(Short c) {
            value = c;
        }

        public Short getValue() {
            return value;
        }
    }

    //Cleanup: see if this can be cleaned up.
    /**
     * Can be used to show the appropriate message UI.
     */
    public enum ContentType {
        DEFAULT(Short.valueOf("0")),
        /**
         * When the message has a file attached to it.
         */
        ATTACHMENT(Short.valueOf("1")),
        /**
         * When the message content represents a location latitude and longitude
         */
        LOCATION(Short.valueOf("2")),
        /**
         * When the message content contains HTML
         */
        TEXT_HTML(Short.valueOf("3")),
        /**
         * When the message content contains a URL.
         */
        TEXT_URL(Short.valueOf("5")),
        /**
         * Internal. Do not use.
         */
        PRICE(Short.valueOf("4")),
        /**
         * Internal. Do not use.
         */
        CONTACT_MSG(Short.valueOf("7")),
        /**
         * Internal. Do not use.
         */
        AUDIO_MSG(Short.valueOf("8")),
        /**
         * Internal. Do not use.
         */
        VIDEO_MSG(Short.valueOf("9")),
        /**
         * Internal. Do not use.
         */
        CHANNEL_CUSTOM_MESSAGE(Short.valueOf("10")),
        /**
         * Internal. Do not use.
         */
        CUSTOM(Short.valueOf("101")),
        /**
         * Internal. Do not use.
         */
        HIDDEN(Short.valueOf("11")),
        /**
         * Internal. Do not use.
         */
        BLOCK_NOTIFICATION_IN_GROUP(Short.valueOf("13")),
        /**
         * Internal. Do not use.
         */
        VIDEO_CALL_NOTIFICATION_MSG(Short.valueOf("102")),
        /**
         * Internal. Do not use.
         */
        VIDEO_CALL_STATUS_MSG(Short.valueOf("103"));

        private Short value;

        ContentType(Short value) {
            this.value = value;
        }

        public Short getValue() {
            return value;
        }
    }

    /**
     * State of the sent message.
     */
    public enum Status {

        UNREAD(Short.valueOf("0")), READ(Short.valueOf("1")), PENDING(Short.valueOf("2")),
        SENT(Short.valueOf("3")), DELIVERED(Short.valueOf("4")), DELIVERED_AND_READ(Short.valueOf("5"));
        private Short value;

        Status(Short value) {
            this.value = value;
        }

        public Short getValue() {
            return value;
        }
    }

    /**
     * Messages that are replies to other messages.
     *
     * <p>You can use these types to aid with your chat UI development.</p>
     */
    public enum ReplyMessage {
        /**
         * A normal, non-reply message.
         */
        NON_HIDDEN(0),
        /**
         * Reply message with non-deleted parent message.
         */
        REPLY_MESSAGE(1),
        /**
         * When the parent message has been deleted.
         */
        HIDE_MESSAGE(2);
        private Integer value;

        ReplyMessage(Integer value) {
            this.value = value;
        }

        public Integer getValue() {
            return value;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;

        if (message.isTempDateType() && isTempDateType()) {
            return DateUtils.getDate(message.getCreatedAtTime()).equals(DateUtils.getDate(getCreatedAtTime()));
        }

        if (getMessageId() != null && message.getMessageId() != null && getMessageId().equals(message.getMessageId())) {
            return true;
        }

        if (getKeyString() != null && message.getKeyString() != null) {
            return (getKeyString().equals(message.getKeyString()));
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (messageId != null ? messageId.hashCode() : 0);
        if (isTempDateType()) {
            result = 31 * result + DateUtils.getDate(getCreatedAtTime()).hashCode();
        }
        return result;
    }

    @Override
    public String toString() {
        return "Message{" +
                "createdAtTime=" + createdAtTime +
                ", to='" + to + '\'' +
                ", message='" + message + '\'' +
                ", key='" + key + '\'' +
                ", deviceKey='" + deviceKey + '\'' +
                ", userKey='" + userKey + '\'' +
                ", emailIds='" + emailIds + '\'' +
                ", shared=" + shared +
                ", sent=" + sent +
                ", delivered=" + delivered +
                ", type=" + type +
                ", storeOnDevice=" + storeOnDevice +
                ", contactIds='" + contactIds + '\'' +
                ", groupId=" + groupId +
                ", sendToDevice=" + sendToDevice +
                ", scheduledAt=" + scheduledAt +
                ", source=" + source +
                ", timeToLive=" + timeToLive +
                ", sentToServer=" + sentToServer +
                ", fileMetaKey='" + fileMetaKey + '\'' +
                ", filePaths=" + filePaths +
                ", pairedMessageKey='" + pairedMessageKey + '\'' +
                ", sentMessageTimeAtServer=" + sentMessageTimeAtServer +
                ", canceled=" + canceled +
                ", clientGroupId='" + clientGroupId + '\'' +
                ", fileMeta=" + fileMeta +
                ", messageId=" + messageId +
                ", read=" + read +
                ", attDownloadInProgress=" + attDownloadInProgress +
                ", applicationId='" + applicationId + '\'' +
                ", conversationId=" + conversationId +
                ", topicId='" + topicId + '\'' +
                ", connected=" + connected +
                ", contentType=" + contentType +
                ", metadata=" + metadata +
                ", status=" + status +
                ", hidden=" + hidden +
                ", replyMessage=" + replyMessage +
                '}';
    }

    /**
     * Internal. You will not need to use this method.
     */
    public boolean isCanceled() {
        return canceled;
    }

    /**
     * Internal. You will not need to use this method.
     */
    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    /**
     * Internal. Do not use.
     */
    public void setStatus(short status) {
        this.status = status;
    }

    /**
     * Internal. You will not need to use this method.
     */
    public boolean isTempDateType() {
        return type.equals(MessageType.DATE_TEMP.value);
    }

    /**
     * Internal. You will not need to use this method.
     */
    public void setTempDateType(short tempDateType) {
        this.type = tempDateType;
    }

    /**
     * Internal. You will not need to use this method.
     */
    public boolean isCustom() {
        return contentType == ContentType.CUSTOM.value;
    }

    /**
     * Internal. You will not need to use this method.
     */
    public boolean isChannelCustomMessage() {
        return contentType == ContentType.CHANNEL_CUSTOM_MESSAGE.getValue();
    }

    /**
     * Internal. You will not need to use this method.
     */
    public boolean isDeliveredAndRead() {
        return Message.Status.DELIVERED_AND_READ.getValue().equals(getStatus());
    }

    /**
     * Internal. You will not need to use this method.
     */
    public boolean isReadStatus() {
        return Status.READ.getValue() == getStatus();
    }

    /**
     * Internal. You will not need to use this method.
     */
    public boolean isReadStatusForUpdate() {
        return Status.READ.getValue() == getStatus() || isTypeOutbox();
    }

    /**
     * Internal. You will not need to use this method.
     */
    public boolean isContactMessage() {
        return ContentType.CONTACT_MSG.getValue().equals(getContentType());
    }

    /**
     * Internal. You will not need to use this method.
     */
    public boolean isLocationMessage() {
        return ContentType.LOCATION.getValue().equals(getContentType());
    }

    /**
     * Internal. Do not use.
     */
    public void setSentMessageTimeAtServer(long sentMessageTimeAtServer) {
        this.sentMessageTimeAtServer = sentMessageTimeAtServer;
    }

    /**
     * Internal. Do not use.
     */
    public void setAsDeletedForAll() {
        if (metadata == null) {
            metadata = new HashMap<>();
        }

        metadata.put(AL_DELETE_MESSAGE_FOR_ALL_KEY, GroupMessageMetaData.TRUE.getValue());
    }

    /**
     * Internal. Do not use.
     */
    public boolean isIgnoreMessageAdding(Context context) {
        if (ApplozicClient.getInstance(context).isSubGroupEnabled() && MobiComUserPreference.getInstance(context).getParentGroupKey() != null || !TextUtils.isEmpty(MobiComUserPreference.getInstance(context).getCategoryName())) {
            Channel channel = ChannelService.getInstance(context).getChannelByChannelKey(getGroupId());
            boolean subGroupFlag = channel != null && channel.getParentKey() != null && MobiComUserPreference.getInstance(context).getParentGroupKey().equals(channel.getParentKey());
            boolean categoryFlag = channel != null && channel.isPartOfCategory(MobiComUserPreference.getInstance(context).getCategoryName());
            return (subGroupFlag || categoryFlag || ApplozicClient.getInstance(context).isSubGroupEnabled() || !TextUtils.isEmpty(MobiComUserPreference.getInstance(context).getCategoryName()));
        }
        return ((ApplozicClient.getInstance(context).isActionMessagesHidden() && isActionMessage()) || hasHideKey());
    }

    /**
     * Internal. You will not need to use this method.
     */
    public boolean isGroupDeleteAction() {
        return getMetadata() != null && getMetadata().containsKey(ChannelMetadata.AL_CHANNEL_ACTION)
                && Integer.parseInt(getMetadata().get(ChannelMetadata.AL_CHANNEL_ACTION)) == GroupAction.DELETE_GROUP.getValue();
    }

    /**
     * Internal. You will not need to use this method.
     */
    public boolean isUpdateMessage() {
        return !Message.ContentType.HIDDEN.getValue().equals(contentType)
                && (!Message.MetaDataType.ARCHIVE.getValue().equals(getMetaDataValueForKey(Message.MetaDataType.KEY.getValue())) || !isHidden())
                && !isVideoNotificationMessage();

    }

    /**
     * Internal. You will not need to use this method.
     */
    public boolean isVideoNotificationMessage() {
        return ContentType.VIDEO_CALL_NOTIFICATION_MSG.getValue().equals(getContentType());
    }

    /**
     * Internal. You will not need to use this method.
     */
    public boolean isVideoCallMessage() {
        return ContentType.VIDEO_CALL_STATUS_MSG.getValue().equals(getContentType());
    }

    /**
     * Internal. You will not need to use this method.
     */
    public boolean isVideoOrAudioCallMessage() {
        String msgType = getMetaDataValueForKey(VideoCallNotificationHelper.MSG_TYPE);
        return (VideoCallNotificationHelper.CALL_STARTED.equals(msgType)
                || VideoCallNotificationHelper.CALL_REJECTED.equals(msgType)
                || VideoCallNotificationHelper.CALL_CANCELED.equals(msgType)
                || VideoCallNotificationHelper.CALL_ANSWERED.equals(msgType)
                || VideoCallNotificationHelper.CALL_END.equals(msgType)
                || VideoCallNotificationHelper.CALL_DIALED.equals(msgType)
                || VideoCallNotificationHelper.CALL_ANSWERED.equals(msgType)
                || VideoCallNotificationHelper.CALL_MISSED.equals(msgType));
    }

    /**
     * Internal. You will not need to use this method.
     */
    public boolean isConsideredForCount() {
        return (!Message.ContentType.HIDDEN.getValue().equals(getContentType()) &&
                !ContentType.VIDEO_CALL_NOTIFICATION_MSG.getValue().equals(getContentType()) && !isReadStatus() && !hasHideKey());
    }

    /**
     * Internal. You will not need to use this method.
     */
    public boolean hasHideKey() {
        return GroupMessageMetaData.TRUE.getValue().equals(getMetaDataValueForKey(GroupMessageMetaData.HIDE_KEY.getValue())) || Message.ContentType.HIDDEN.getValue().equals(getContentType()) || hidden;
    }

    /**
     * Internal. You will not need to use this method.
     */
    public boolean isGroupMetaDataUpdated() {
        return ContentType.CHANNEL_CUSTOM_MESSAGE.getValue().equals(this.getContentType()) && this.getMetadata() != null && this.getMetadata().containsKey("action") && GroupAction.GROUP_META_DATA_UPDATED.getValue().toString().equals(this.getMetadata().get("action"));
    }

    /**
     * Internal. You will not need to use this method.
     */
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    /**
     * Internal. You will not need to use this method.
     */
    public boolean isHidden() {
        return hidden;
    }

    /**
     * Do not use. This method will be deprecated soon.
     */
    public Integer getConversationId() {
        return conversationId;
    }

    /**
     * Do not use. This method will be deprecated soon.
     */
    public void setConversationId(Integer conversationId) {
        this.conversationId = conversationId;
    }

    /**
     * Internal. You will not need to use this method.
     */
    public String getTopicId() {
        return topicId;
    }

    /**
     * Internal. You will not need to use this method.
     */
    public void setTopicId(String topicId) {
        this.topicId = topicId;
    }

    /**
     * Internal. You will not need to use this method.
     */
    public String getCurrentId() {
        return getGroupId() != null ? String.valueOf(getGroupId()) : getContactIds();
    }

    /**
     * Internal. Do not use.
     */
    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    /**
     * Internal. You will not need to use this method.
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Internal. You will not need to use this method.
     */
    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    /**
     * Internal. Do not use.
     */
    public boolean isAttachmentUploadInProgress() {
        return filePaths != null && !filePaths.isEmpty() && FileUtils.isFileExist(filePaths.get(0)) && !sentToServer;
    }

    /**
     * Internal. Do not use.
     */
    public boolean isCall() {
        return MessageType.CALL_INCOMING.getValue().equals(type) || MessageType.CALL_OUTGOING.getValue().equals(type);
    }

    /**
     * Internal. Do not use.
     */
    public boolean isOutgoingCall() {
        return MessageType.CALL_OUTGOING.getValue().equals(type);
    }

    /**
     * Internal. Do not use.
     */
    public boolean isIncomingCall() {
        return MessageType.CALL_INCOMING.getValue().equals(type);
    }


    /**
     * Internal. Do not use.
     */
    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    /**
     * Internal. Do not use.
     */
    public boolean isDummyEmptyMessage() {
        return getCreatedAtTime() != null && getCreatedAtTime() == 0 && TextUtils.isEmpty(getMessage());
    }

    /**
     * Internal. Do not use.
     */
    public boolean isLocalMessage() {
        return TextUtils.isEmpty(getKeyString()) && isSentToServer();
    }

    /**
     * Internal. Do not use.
     */
    public void setSent(boolean sent) {
        this.sent = sent;
    }

    /**
     * Internal. Do not use.
     */
    public void setDelivered(Boolean delivered) {
        this.delivered = delivered;
    }

    /**
     * Internal. Do not use.
     */
    public boolean isStoreOnDevice() {
        return storeOnDevice;
    }

    /**
     * Internal. Do not use.
     */
    public void setStoreOnDevice(boolean storeOnDevice) {
        this.storeOnDevice = storeOnDevice;
    }

    /**
     * Internal. Do not use.
     */
    public String getDeviceKeyString() {
        return deviceKey;
    }

    /**
     * Internal. Do not use.
     */
    public void setDeviceKeyString(String deviceKeyString) {
        this.deviceKey = deviceKeyString;
    }

    /**
     * Internal. Do not use.
     */
    public String getSuUserKeyString() {
        return userKey;
    }

    /**
     * Internal. Do not use.
     */
    public void setSuUserKeyString(String suUserKeyString) {
        this.userKey = suUserKeyString;
    }

    /**
     * Internal. Do not use.
     */
    public void processContactIds(Context context) {
        MobiComUserPreference userPreferences = MobiComUserPreference.getInstance(context);
        if (TextUtils.isEmpty(getContactIds())) {
            setContactIds(getTo());
        }
    }

    /**
     * Internal. Do not use.
     */
    public String getContactIds() {
        return getTo();
    }

    /**
     * Internal. Do not use.
     */
    public void setContactIds(String contactIds) {
        this.contactIds = contactIds;
    }

    /**
     * Internal. Do not use.
     */
    public boolean isSendToDevice() {
        return sendToDevice;
    }

    /**
     * Internal. Do not use.
     */
    public void setSendToDevice(boolean sendToDevice) {
        this.sendToDevice = sendToDevice;
    }

    /**
     * Internal. Do not use.
     */
    public Long getScheduledAt() {
        return scheduledAt;
    }

    /**
     * Internal. Do not use.
     */
    public void setScheduledAt(Long scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    /**
     * Internal. Do not use.
     */
    public boolean isSentToServer() {
        return sentToServer;
    }

    /**
     * Internal. Do not use.
     */
    public void setSentToServer(boolean sentToServer) {
        this.sentToServer = sentToServer;
    }

    /**
     * Internal. Do not use.
     */
    public boolean isSentViaApp() {
        return MessageType.MT_OUTBOX.getValue().equals(this.type);
    }

    /**
     * Internal. Do not use.
     */
    public boolean isSentViaCarrier() {
        return MessageType.OUTBOX.getValue().equals(type);
    }

    /**
     * Internal. Do not use.
     */
    public Short getSource() {
        return source;
    }

    /**
     * Internal. Do not use.
     */
    public void setSource(Short source) {
        this.source = source;
    }

    /**
     * Internal. Do not use.
     */
    public Integer getTimeToLive() {
        return timeToLive;
    }

    /**
     * Internal. Do not use.
     */
    public void setTimeToLive(Integer timeToLive) {
        this.timeToLive = timeToLive;
    }

    /**
     * Internal. Do not use.
     */
    public String getFileMetaKeyStrings() {
        return fileMetaKey;
    }

    /**
     * Internal. Do not use.
     */
    public void setFileMetaKeyStrings(String fileMetaKeyStrings) {
        this.fileMetaKey = fileMetaKeyStrings;
    }

    /**
     * Internal. Do not use.
     */
    public String getPairedMessageKeyString() {
        return pairedMessageKey;
    }

    /**
     * Internal. Do not use.
     */
    public void setPairedMessageKeyString(String pairedMessageKeyString) {
        this.pairedMessageKey = pairedMessageKeyString;
    }

    /**
     * Internal. You will not need to use this method.
     */
    public void setFileMetas(FileMeta fileMetas) {
        this.fileMeta = fileMetas;
    }

    /**
     * Do not set this yourself.
     */
    public void setKeyString(String keyString) {
        this.key = keyString;
    }

    /**
     * Internal. Do not use. Use {@link ApplozicConversation#markAsRead(Context, String, String, Integer)} instead.
     */
    public void setRead(Boolean read) {
        this.read = read;
    }

    /**
     * @deprecated This method has been deprecated and will be removed soon.
     */
    @Deprecated
    public boolean isSelfDestruct() {
        return getTimeToLive() != null;
    }

    /**
     * @deprecated No longer used.
     */
    @Deprecated
    public String getEmailIds() {
        return emailIds;
    }

    /**
     * @deprecated No longer used.
     */
    @Deprecated
    public void setEmailIds(String emailIds) {
        this.emailIds = emailIds;
    }

    /**
     * @deprecated No longer used.
     */
    @Deprecated
    public boolean isShared() {
        return shared;
    }

    /**
     * @deprecated No longer used.
     */
    @Deprecated
    public void setShared(boolean shared) {
        this.shared = shared;
    }

    /**
     * @deprecated Conversation assignees are no longer used and will be removed soon.
     */
    @Deprecated
    public String getConversationAssignee() {
        return (getMetadata() != null && getMetadata().containsKey(BOT_ASSIGN)) ? getMetadata().get(BOT_ASSIGN) : null;
    }

    /**
     * @deprecated Use {@link #getMessageType()}.
     */
    @Deprecated
    public String getAttachmentType() {
        String type = "no_attachment";

        if (getContentType() == Message.ContentType.LOCATION.getValue()) {
            type = "location";
        } else if (getContentType() == Message.ContentType.AUDIO_MSG.getValue()) {
            type = "audio";
        } else if (getContentType() == Message.ContentType.VIDEO_MSG.getValue()) {
            type = "video";
        } else if (getContentType() == Message.ContentType.ATTACHMENT.getValue()) {
            if (getFilePaths() != null) {
                String filePath = getFilePaths().get(getFilePaths().size() - 1);
                String mimeType = FileUtils.getMimeType(filePath);

                if (mimeType != null) {
                    if (mimeType.startsWith("image")) {
                        type = "image";
                    } else if (mimeType.startsWith("audio")) {
                        type = "audio";
                    } else if (mimeType.startsWith("video")) {
                        type = "video";
                    } else {
                        type = "others";
                    }
                }
            } else if (getFileMetas() != null) {
                if (getFileMetas().getContentType().contains("image")) {
                    type = "image";
                } else if (getFileMetas().getContentType().contains("audio")) {
                    type = "audio";
                } else if (getFileMetas().getContentType().contains("video")) {
                    type = "video";
                } else {
                    type = "others";
                }
            }
        } else if (getContentType() == Message.ContentType.CONTACT_MSG.getValue()) {
            type = "contact";
        } else if (hasAttachment()) {
            type = "others";
        }
        return type;
    }

    /**
     * @deprecated Conversation statuses are no longer used and will be removed soon.
     */
    @Deprecated
    public String getConversationStatus() {
        return (getMetadata() != null && getMetadata().containsKey(CONVERSATION_STATUS)) ? getMetadata().get(CONVERSATION_STATUS) : null;
    }

    /**
     * @deprecated Conversation assignees are no longer used and will be removed soon.
     */
    @Deprecated
    public String getAssigneId() {
        if (isActionMessage()) {
            return getMetadata().get(BOT_ASSIGN);
        }
        return null;
    }

    /**
     * Internal. You do not need to use this enum.
     */
    public enum GroupAction {
        CREATE(0),
        ADD_MEMBER(1),
        REMOVE_MEMBER(2),
        LEFT(3),
        DELETE_GROUP(4),
        CHANGE_GROUP_NAME(5),
        CHANGE_IMAGE_URL(6),
        JOIN(7),
        GROUP_USER_ROLE_UPDATED(8),
        GROUP_META_DATA_UPDATED(9);
        private Integer value;

        GroupAction(Integer value) {
            this.value = value;
        }

        public Short getValue() {
            return value.shortValue();
        }
    }

    /**
     * Internal. You do not need to use this enum.
     */
    public enum GroupMessageMetaData {
        KEY("show"),
        HIDE_KEY("hide"),
        FALSE("false"),
        TRUE("true");
        private String value;

        GroupMessageMetaData(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * Internal. You do not need to use it.
     */
    public enum MetaDataType {
        KEY("category"),
        HIDDEN("HIDDEN"),
        PUSHNOTIFICATION("PUSHNOTIFICATION"),
        ARCHIVE("ARCHIVE"), AL_REPLY("AL_REPLY");
        private String value;

        MetaDataType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    //Cleanup: see if this can be deprecated
    /**
     * Internal. Do not use. Will be deprecated soon.
     */
    public enum Source {

        DEVICE_NATIVE_APP(Short.valueOf("0")), WEB(Short.valueOf("1")), MT_MOBILE_APP(Short.valueOf("2")), API(Short.valueOf("3"));
        private Short value;

        Source(Short c) {
            value = c;
        }

        public Short getValue() {
            return value;
        }
    }
}
