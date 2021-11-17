package com.applozic.mobicomkit.listners;

import androidx.annotation.Nullable;

import com.applozic.mobicomkit.api.conversation.Message;

import java.util.Set;

/**
 * Callbacks for real-time chat events such as <i>incoming messages</i> etc.
 *
 * @see com.applozic.mobicomkit.broadcast.AlEventManager
 */
public interface ApplozicUIListener {
    /**
     * Called whenever the current user sends a message.
     */
    void onMessageSent(@Nullable Message message);

    /**
     * Called whenever the current user receives a message.
     */
    void onMessageReceived(@Nullable Message message);

    /**
     * Called whenever a new message is synced from the server.
     * This will happen whenever a message is received, sent or updated in any way.
     *
     * @param key the key of the synced message
     */
    void onMessageSync(@Nullable Message message, @Nullable String key);

    /**
     * @param messageKey {@link Message#getKeyString()}
     * @param userId ignore. will be null.
     */
    void onMessageDeleted(@Nullable String messageKey, @Nullable String userId);

    /**
     * @param userId the user that the successfully received the message
     */
    void onMessageDelivered(@Nullable Message message, @Nullable String userId);

    /**
     * Called when all messages sent to a user(/contact) have been delivered.
     *
     * @param userId user-id of the receiver.
     */
    void onAllMessagesDelivered(@Nullable String userId);

    /**
     * Called when all message sent to a user(/contact) have been read.
     *
     * @param userId user-id of the receiver.
     */
    void onAllMessagesRead(@Nullable String userId);

    /**
     * Called whenever a conversation(group or 1-to-1 chat) is deleted.
     *
     * @param userId for one-to-one conversation. null/empty if a group was deleted
     * @param channelKey for group conversation. null/0 if a 1-to-1 conversation was deleted
     */
    void onConversationDeleted(@Nullable String userId, @Nullable Integer channelKey, @Nullable String response);

    /**
     * Called whenever the typing status of contacts (in conversation) with changes.
     *
     * @param userId the user the typing status corresponds to
     * @param isTyping true if user is typing
     */
    void onUpdateTypingStatus(@Nullable String userId, @Nullable String isTyping);

    /**
     * Called whenever the last seen of contacts (in conversation) is updated.
     *
     * <p>To get the updated last seen, use {@link com.applozic.mobicomkit.api.account.user.UserClientService#getUserDetails(Set)}.</p>
     *
     * @param userId user whose last seen was updated
     */
    void onUpdateLastSeen(@Nullable String userId);

    /**
     * Called when MQTT is disconnected.
     */
    void onMqttDisconnected();

    /**
     * Called when MQTT is connected.
     */
    void onMqttConnected();

    /**
     * Called when the current user comes online.
     */
    void onUserOnline();

    /**
     * Called when the user goes offline.
     */
    void onUserOffline();

    /**
     * Called when the current user is activated/deactivated.
     *
     * <p>User activation/deactivation can be done from the <i>applozic dashboard</i>.</p>
     */
    void onUserActivated(boolean isActivated);

    /**
     * Called when new and existing channel data is synced with the server.
     */
    void onChannelUpdated();

    /**
     * Called when all messages of a conversation have been read.
     *
     * @param userId stores the user-id if <code>isGroup</code> is false or group-id if <code>isGroup</code> is true.
     * @param isGroup true if the conversation read is a group. false otherwise.
     */
    void onConversationRead(@Nullable String userId, @Nullable boolean isGroup);

    /**
     * Called when a details of contacts (in conversation) are updated and synced with the server.
     *
     * <p>Use {@link com.applozic.mobicomkit.api.account.user.UserClientService#getUserDetails(Set)} to get the fresh user details.</p>
     */
    void onUserDetailUpdated(@Nullable String userId);

    /**
     * Called when the metadata for any message is updated.
     *
     * @param keyString {@link Message#getKeyString()}
     */
    void onMessageMetadataUpdated(@Nullable String keyString);

    /**
     * Called when a contact is muted/un-muted for the current user.
     *
     * @param userId the user muted/un-muted
     */
    void onUserMute(boolean mute, @Nullable String userId);

    /**
     * Called when a channel/group is muted for the current user.
     *
     * @param groupId the channel muted
     */
    void onGroupMute(@Nullable Integer groupId);

    /**
     * Not used. Please ignore.
     */
    void onLoadMore(boolean loadMore);
}
