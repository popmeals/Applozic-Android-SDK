package com.applozic.mobicomkit.api.conversation;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.applozic.mobicomkit.api.ApplozicMqttService;
import com.applozic.mobicomkit.api.MobiComKitConstants;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.attachment.AttachmentManager;
import com.applozic.mobicomkit.api.attachment.AttachmentTask;
import com.applozic.mobicomkit.api.conversation.database.MessageDatabaseService;
import com.applozic.mobicomkit.api.notification.MuteNotificationRequest;
import com.applozic.mobicomkit.api.people.ChannelInfo;
import com.applozic.mobicomkit.api.people.UserWorker;
import com.applozic.mobicomkit.channel.database.ChannelDatabaseService;
import com.applozic.mobicomkit.channel.service.ChannelClientService;
import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.exception.ApplozicException;
import com.applozic.mobicomkit.feed.ApiResponse;
import com.applozic.mobicomkit.feed.ChannelFeed;
import com.applozic.mobicomkit.feed.ChannelFeedApiResponse;
import com.applozic.mobicomkit.feed.ChannelName;
import com.applozic.mobicomkit.feed.GroupInfoUpdate;
import com.applozic.mobicomkit.listners.ConversationListHandler;
import com.applozic.mobicomkit.listners.MediaDownloadProgressHandler;
import com.applozic.mobicomkit.listners.MessageListHandler;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.contact.Contact;
import com.applozic.mobicommons.task.AlAsyncTask;
import com.applozic.mobicommons.task.AlTask;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ApplozicConversation {
    /**
     * Delete the group (<i>channelKey</i>) or 1-to-1 conversation (<i>contact</i>) or both. Use with caution.
     *
     * <p>Will return true for a successful delete.</p>
     */
    public static @NonNull AlAsyncTask<Void, Boolean> deleteConversation(@NonNull Context context, @Nullable Integer channelKey, @Nullable Contact contact) {
        return new AlAsyncTask<Void, Boolean>() {
            @Override
            protected Boolean doInBackground() {
                if ((channelKey == null || channelKey == 0) && contact == null) {
                    return false;
                }

                Channel channel = (channelKey != null && channelKey != 0) ? new Channel(channelKey) : null;

                String response = new MobiComConversationService(context).deleteSync(contact, channel, null);

                return !TextUtils.isEmpty(response) && MobiComKitConstants.SUCCESS.equals(response);
            }
        };
    }

    public static void getLatestMessageList(Context context, String searchString, boolean isScroll, MessageListHandler handler) {
        if (!isScroll) {
            AlTask.execute(new MessageListTask(context, searchString, null, null, null, null, handler, true));
        } else {
            AlTask.execute(new MessageListTask(context, searchString, null, null, MobiComUserPreference.getInstance(context).getStartTimeForPagination(), null, handler, true));
        }
    }

    public static void getLatestMessageList(Context context, boolean isScroll, MessageListHandler handler) {
        getLatestMessageList(context, null, isScroll, handler);
    }

    public static void getLatestMessageList(Context context, String searchString, Long startTime, MessageListHandler handler) {
        AlTask.execute(new MessageListTask(context, searchString, null, null, startTime, null, handler, true));
    }

    public static void getConversationList(Context context, String searchString, boolean isScroll, ConversationListHandler handler) {
        AlTask.execute(new ConversationListTask(context,
                searchString,
                null,
                null,
                (isScroll ? MobiComUserPreference.getInstance(context).getStartTimeForPagination() : null),
                null,
                handler,
                true));
    }

    public static void getMessageListForContact(Context context, Contact contact, Long endTime, MessageListHandler handler) {
        AlTask.execute(new MessageListTask(context, null, contact, null, null, endTime, handler, false));
    }

    public static void getMessageListForChannel(Context context, Channel channel, Long endTime, MessageListHandler handler) {
        AlTask.execute(new MessageListTask(context, null, null, channel, null, endTime, handler, false));
    }

    public static void getMessageListForContact(Context context, String userId, Long endTime, MessageListHandler handler) {
        AlTask.execute(new MessageListTask(context, null, new AppContactService(context).getContactById(userId), null, null, endTime, handler, false));
    }

    public static void getMessageListForChannel(Context context, Integer channelKey, Long endTime, MessageListHandler handler) {
        AlTask.execute(new MessageListTask(context, null, null, ChannelService.getInstance(context).getChannel(channelKey), null, endTime, handler, false));
    }

    public static void downloadMessage(Context context, Message message, MediaDownloadProgressHandler handler) {
        ApplozicException e;
        if (message == null || handler == null) {
            return;
        }
        if (!message.hasAttachment()) {
            e = new ApplozicException("Message does not have Attachment");
            handler.onProgressUpdate(0, e);
            handler.onCompleted(null, e);
        } else if (message.isAttachmentDownloaded()) {
            e = new ApplozicException("Attachment for the message already downloaded");
            handler.onProgressUpdate(0, e);
            handler.onCompleted(null, e);
        } else {
            AttachmentTask mDownloadThread = null;
            if (!AttachmentManager.isAttachmentInProgress(message.getKeyString())) {
                // Starts downloading this View, using the current cache setting
                mDownloadThread = AttachmentManager.startDownload(null, true, message, handler, context);
                // After successfully downloading the image, this marks that it's available.
            }
            if (mDownloadThread == null) {
                mDownloadThread = AttachmentManager.getBGThreadForAttachment(message.getKeyString());
                if (mDownloadThread != null) {
                    mDownloadThread.setAttachment(message, handler, context);
                }
            }
        }
    }

    public static synchronized void addLatestMessage(Message message, List<Message> messageList) {
        Iterator<Message> iterator = messageList.iterator();
        boolean shouldAdd = false;

        while (iterator.hasNext()) {
            Message currentMessage = iterator.next();

            if ((message.getGroupId() != null && currentMessage.getGroupId() != null && message.getGroupId().equals(currentMessage.getGroupId())) ||
                    (message.getGroupId() == null && currentMessage.getGroupId() == null && message.getContactIds() != null && currentMessage.getContactIds() != null &&
                            message.getContactIds().equals(currentMessage.getContactIds()))) {
                //do nothing
            } else {
                currentMessage = null;
            }

            if (currentMessage != null) {
                if (message.getCreatedAtTime() >= currentMessage.getCreatedAtTime()) {
                    iterator.remove();
                } else {
                    return;
                }
            }

            shouldAdd = true;
        }

        if (shouldAdd) {
            messageList.add(0, message);
        }
    }

    public static synchronized void addLatestConversation(Context context, Message message, List<AlConversation> conversationList) {
        Iterator<AlConversation> iterator = conversationList.iterator();
        boolean shouldAdd = false;

        while (iterator.hasNext()) {
            AlConversation currentMessage = iterator.next();

            if ((message.getGroupId() != null && currentMessage.getMessage().getGroupId() != null && message.getGroupId().equals(currentMessage.getMessage().getGroupId())) ||
                    (message.getGroupId() == null && currentMessage.getMessage().getGroupId() == null && message.getContactIds() != null && currentMessage.getMessage().getContactIds() != null &&
                            message.getContactIds().equals(currentMessage.getMessage().getContactIds()))) {
                //do nothing
            } else {
                currentMessage = null;
            }

            if (currentMessage != null) {
                if (message.getCreatedAtTime() >= currentMessage.getMessage().getCreatedAtTime()) {
                    iterator.remove();
                } else {
                    return;
                }
            }

            shouldAdd = true;
        }

        if (shouldAdd) {
            conversationList.add(0, getConversationFromMessage(context, message));
        }
    }

    public static synchronized void removeLatestConversation(String userId, Integer groupId, List<AlConversation> conversationList) {
        int index = -1;

        for (AlConversation message : conversationList) {
            if (message.getMessage().getGroupId() != null) {
                if (message.getMessage().getGroupId() != 0 && message.getMessage().getGroupId().equals(groupId)) {
                    index = conversationList.indexOf(message);
                }
            } else if (message.getMessage().getContactIds() != null && message.getMessage().getContactIds().equals(userId)) {
                index = conversationList.indexOf(message);
            }
        }
        if (index != -1) {
            conversationList.remove(index);
        }
    }

    public static AlConversation getConversationFromMessage(Context context, Message message) {
        AlConversation conversation = new AlConversation();

        conversation.setMessage(message);

        if (message.getGroupId() == null || message.getGroupId() == 0) {
            conversation.setContact(new AppContactService(context).getContactById(message.getContactIds()));
            conversation.setChannel(null);
            conversation.setUnreadCount(new MessageDatabaseService(context).getUnreadMessageCountForContact(message.getContactIds()));
        } else {
            conversation.setChannel(ChannelDatabaseService.getInstance(context).getChannelByChannelKey(message.getGroupId()));
            conversation.setContact(null);
            conversation.setUnreadCount(new MessageDatabaseService(context).getUnreadMessageCountForChannel(message.getGroupId()));
        }

        return conversation;
    }

    public static synchronized void removeLatestMessage(String userId, Integer groupId, List<Message> messageList) {
        Message tempMessage = null;

        for (Message message : messageList) {
            if (message.getGroupId() != null) {
                if (message.getGroupId() != 0 && message.getGroupId().equals(groupId)) {
                    tempMessage = message;
                }
            } else if (message.getContactIds() != null && message.getContactIds().equals(userId)) {
                tempMessage = message;
            }
        }
        if (tempMessage != null) {
            messageList.remove(tempMessage);
        }
    }

    public static boolean isMessageStatusPublished(Context context, String pairedMessageKey, Short status) {
        ApplozicMqttService applozicMqttService = ApplozicMqttService.getInstance(context);

        if (!TextUtils.isEmpty(pairedMessageKey) && applozicMqttService.isConnected()) {
            final String MESSAGE_STATUS_TOPIC = "message-status";
            applozicMqttService.connectAndPublishMessageStatus(MESSAGE_STATUS_TOPIC, MobiComUserPreference.getInstance(context).getUserId() + "," + pairedMessageKey + "," + status);
            return true;
        }
        return false;
    }

    public static void markAsRead(Context context, String pairedMessageKey, String userId, Integer groupId) {
        try {
            int unreadCount = 0;
            Contact contact = null;
            Channel channel = null;
            if (userId != null) {
                contact = new AppContactService(context).getContactById(userId);
                unreadCount = contact.getUnreadCount();
                new MessageDatabaseService(context).updateReadStatusForContact(userId);
            } else if (groupId != null && groupId != 0) {
                channel = ChannelService.getInstance(context).getChannelByChannelKey(groupId);
                unreadCount = channel.getUnreadCount();
                new MessageDatabaseService(context).updateReadStatusForChannel(String.valueOf(groupId));
            }

            UserWorker.enqueueWork(context, null, contact, channel, pairedMessageKey, unreadCount, false);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    /**
     * High level methods for dealing with {@link Channel}s.
     *
     * <p>Support offline functionality. In other words, all <i>channel</i> data is also stored locally.</p>
     */
    public static class Channels {
        /**
         * Creates a new {@link Channel}.
         *
         * <p>When {@link ChannelFeedApiResponse#isSuccess()} gives true, pass {@link ChannelFeedApiResponse#getResponse()} to {@link ChannelService#getChannel(ChannelFeed)} to get your newly created <i>channel</i> object.</p>
         */
        public static @NonNull AlAsyncTask<Void, ChannelFeedApiResponse> createChannel(@NonNull Context context, @NonNull ChannelInfo channelInfo) {
            return new AlAsyncTask<Void, ChannelFeedApiResponse>() {
                @Override
                protected ChannelFeedApiResponse doInBackground() {
                    return ChannelService.getInstance(context).createChannelWithResponse(channelInfo);
                }
            };
        }

        /**
         * Gets the channel object for the given <i>clientGroupId</i>, or <i>channelKey</i>.
         *
         * <p>Pass either one of the two. If both are non-null then <i>clientGroupId</i> will be used.</p>
         */
        public static @NonNull AlAsyncTask<Void, Channel> getChannel(@NonNull Context context, @Nullable String clientGroupId, @Nullable Integer orChannelKey) {
            return new AlAsyncTask<Void, Channel>() {
                @Override
                protected Channel doInBackground() {
                    ChannelService channelService = ChannelService.getInstance(context);

                    Channel channel = channelService.getChannelInfo(clientGroupId);
                    if (channel == null) {
                        return channelService.getChannelInfo(orChannelKey);
                    }
                    return channel;
                }
            };
        }

        /**
         * Gets all the channels for the user. These are synced.
         *
         * <p>Will return null in-case of failure.</p>
         */
        public static @NonNull AlAsyncTask<Void, List<Channel>> getAllChannels(@NonNull Context context) {
            return new AlAsyncTask<Void, List<Channel>>() {
                @Override
                protected List<Channel> doInBackground() throws Exception {
                    return ChannelService.getInstance(context).getAllChannelList();
                }
            };
        }

        /**
         * Adds a user with the given user-id to the channel with the given channel key.
         *
         * <p>Check for success using {@link ApiResponse#isSuccess()}.</p>
         */
        public static @NonNull AlAsyncTask<Void, ApiResponse<?>> addMemberToChannel(@NonNull Context context, @Nullable String userId, @Nullable Integer channelKey) {
            return new AlAsyncTask<Void, ApiResponse<?>>() {
                @Override
                protected ApiResponse<?> doInBackground() {
                    return ChannelService.getInstance(context).addMemberToChannelWithResponseProcess(channelKey, userId);
                }
            };
        }

        /**
         * Adds a user with the given user-id to the channel with the given client group id.
         *
         * <p>Note: This method does not update the channel data locally.</p>
         *
         * <p>Check for success using {@link ApiResponse#isSuccess()}.</p>
         */
        public static @NonNull AlAsyncTask<Void, ApiResponse<?>> addMemberToChannel(@NonNull Context context, @Nullable String userId, @Nullable String clientGroupId) {
            return new AlAsyncTask<Void, ApiResponse<?>>() {
                @Override
                protected ApiResponse<?> doInBackground() {
                    return ChannelService.getInstance(context).addMemberToChannelProcessWithResponse(clientGroupId, userId);
                }
            };
        }

        /**
         * Removes a user with the given user-id from the channel with the given channel key.
         *
         * <p>Note: Only channel admins can remove users. See {@link ChannelService#isUserAdminInChannel(String, Channel)}.</p>
         *
         * <p>Check for success using {@link ApiResponse#isSuccess()}.</p>
         */
        public static @NonNull AlAsyncTask<Void, ApiResponse<?>> removeMemberFromChannel(@NonNull Context context, @Nullable String userId, @Nullable Integer channelKey) {
            return new AlAsyncTask<Void, ApiResponse<?>>() {
                @Override
                protected ApiResponse<?> doInBackground() {
                    return ChannelService.getInstance(context).removeMemberFromChannelProcessWithResponse(channelKey, userId);
                }
            };
        }

        /**
         * Removes a user with the given user-id from the channel with the given client-group-id.
         *
         * <p>Note: Only channel admins can remove users. See {@link ChannelService#isUserAdminInChannel(String, Channel)}.</p>
         *
         * <p>Check for success using {@link ApiResponse#isSuccess()}.</p>
         */
        public static @NonNull AlAsyncTask<Void, ApiResponse<?>> removeMemberFromChannel(@NonNull Context context, @Nullable String userId, @Nullable String clientGroupId) {
            return new AlAsyncTask<Void, ApiResponse<?>>() {
                @Override
                protected ApiResponse<?> doInBackground() {
                    return ChannelService.getInstance(context).removeMemberFromChannelProcessWithResponse(clientGroupId, userId);
                }
            };
        }

        /**
         * Update channel name and/or image for the channel with the given channel key.
         *
         * <p>Returns true for successful update.</p>
         */
        public static @NonNull AlAsyncTask<Void, Boolean> updateChannelNameAndImage(@NonNull Context context, @Nullable Integer channelKey, @Nullable String newChannelName, @Nullable String newRemoteImageUrl) {
            return new AlAsyncTask<Void, Boolean>() {
                @Override
                protected Boolean doInBackground() {
                    if (channelKey == null || channelKey == 0) {
                        return false;
                    }

                    GroupInfoUpdate groupInfoUpdate = new GroupInfoUpdate(channelKey);
                    groupInfoUpdate.setNewName(newChannelName);
                    groupInfoUpdate.setImageUrl(newRemoteImageUrl);

                    String response = ChannelService.getInstance(context).updateChannel(groupInfoUpdate);
                    return !TextUtils.isEmpty(response) && MobiComKitConstants.SUCCESS.equals(response);
                }
            };
        }

        /**
         * Update channel metadata. See {@link Channel#getMetadata()}.
         *
         * <p>Note: Existing metadata will be replaced.</p>
         *
         * <p>Returns true for successful update.</p>
         */
        public static @NonNull AlAsyncTask<Void, Boolean> updateChannelMetadata(@NonNull Context context, @Nullable Integer channelKey, @Nullable HashMap<String, String> metadata) {
            return new AlAsyncTask<Void, Boolean>() {
                @Override
                protected Boolean doInBackground() {
                    if (channelKey == null || channelKey == 0 || metadata == null) {
                        return false;
                    }

                    GroupInfoUpdate groupInfoUpdate = new GroupInfoUpdate(channelKey);
                    groupInfoUpdate.setMetadata(metadata);

                    String response = ChannelService.getInstance(context).updateChannel(groupInfoUpdate);
                    return !TextUtils.isEmpty(response) && MobiComKitConstants.SUCCESS.equals(response);
                }
            };
        }

        /**
         * Update the role of the channel user with the given user-id, for the given channel key.
         *
         * <p>Pass makeAdmin = true to change the role to <i>admin</i>, and false to change the role to <i>user</i>.</p>
         *
         * <p>Returns true for successful update.</p>
         */
        public static @NonNull AlAsyncTask<Void, Boolean> updateChannelUserRole(@NonNull Context context, @Nullable Integer channelKey, @Nullable String userId, boolean makeAdmin) {
            return new AlAsyncTask<Void, Boolean>() {
                @Override
                protected Boolean doInBackground() {
                    if (channelKey == null || channelKey == 0 || TextUtils.isEmpty(userId)) {
                        return false;
                    }

                    return ChannelService.getInstance(context).updateRoleForUserInChannel(channelKey, userId, makeAdmin ? 1 : 0);
                }
            };
        }

        /**
         * Mute notifications for channel.
         *
         * <code>
         *     MuteNotificationRequest muteNotificationRequest = new MuteNotificationRequest(12345, 1800000L); //channel key and interval of 30 minutes in milliseconds
         * </code>
         *
         * <p>Check for success using {@link ApiResponse#isSuccess()}.</p>
         */
        public static @NonNull AlAsyncTask<Void, ApiResponse<?>> muteNotifications(@NonNull Context context, @Nullable MuteNotificationRequest muteNotificationRequest) {
            return new AlAsyncTask<Void, ApiResponse<?>>() {
                @Override
                protected ApiResponse<?> doInBackground() {
                    if (muteNotificationRequest == null) {
                        return null;
                    }

                    return ChannelService.getInstance(context).muteNotifications(muteNotificationRequest);
                }
            };
        }
    }

    /**
     * For dealing with {@link Contact}s.
     */
    public static class Contacts {

    }
}
