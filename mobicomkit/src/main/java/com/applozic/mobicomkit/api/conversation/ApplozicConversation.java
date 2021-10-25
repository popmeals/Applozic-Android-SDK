package com.applozic.mobicomkit.api.conversation;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.applozic.mobicomkit.api.ApplozicMqttService;
import com.applozic.mobicomkit.api.MobiComKitConstants;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.User;
import com.applozic.mobicomkit.api.account.user.UserClientService;
import com.applozic.mobicomkit.api.account.user.UserDetail;
import com.applozic.mobicomkit.api.account.user.UserService;
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
import com.applozic.mobicomkit.feed.ErrorResponseFeed;
import com.applozic.mobicomkit.feed.GroupInfoUpdate;
import com.applozic.mobicomkit.listners.ConversationListHandler;
import com.applozic.mobicomkit.listners.MediaDownloadProgressHandler;
import com.applozic.mobicomkit.listners.MessageListHandler;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.contact.Contact;
import com.applozic.mobicommons.task.AlAsyncTask;
import com.applozic.mobicommons.task.AlTask;
import com.applozic.mobicommons.task.BaseAsyncTask;
import com.google.gson.reflect.TypeToken;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Public methods for {@link AlConversation conversations}, {@link Channel channels} and {@link Contact contacts}.
 */
public class ApplozicConversation {

    /**
     * Get the list of all conversations for the current user. Null in case of error or if there are no conversations.
     *
     * <p>Each {@link Message} object in the list corresponds to a conversation. It is the latest message for that conversation.</p>
     *
     * <code>
     *     message.getGroupId(); //if this is non-null, the message corresponds to a channel conversation
     *     message.getTo(); //if group-id is null then the message is for a 1-to-1 conversation, this is the user-id of that user.
     *     message.getMessage(); //the latest message text for the conversation
     * </code>
     *
     * @param forScroll pass false to get the first batch of 60 conversations. pass true to get the next batch of 60 and the one after that and so on.
     */
    public static @NonNull AlAsyncTask<Void, List<Message>> conversationList(@NonNull Context context, boolean forScroll) {
        if (!forScroll) {
            return conversationListFromCustomParameters(context, null, null, null);
        } else {
            return conversationListFromCustomParameters(context, null, MobiComUserPreference.getInstance(context).getStartTimeForPagination(), null);
        }
    }

    /**
     * Get the list of conversation for the given search string for the current user. Null in case of error or if there are no conversations.
     *
     * <p>Each {@link Message} object in the list corresponds to a conversation. It is the latest message for that conversation.</p>
     *
     * <code>
     *     message.getGroupId(); //if this is non-null, the message corresponds to a channel conversation
     *     message.getTo(); //if group-id is null then the message is for a 1-to-1 conversation, this is the user-id of that user.
     *     message.getMessage(); //the latest message text for the conversation
     * </code>
     *
     * @param forScroll pass false to get the first batch of 60 conversations. pass true to get the next batch of 60 and the one after that and so on.
     */
    public static @NonNull AlAsyncTask<Void, List<Message>> conversationListForSearch(@NonNull Context context, @Nullable String searchString, boolean forScroll) {
        if (!forScroll) {
            return conversationListFromCustomParameters(context, searchString, null, null);
        } else {
            return conversationListFromCustomParameters(context, searchString, MobiComUserPreference.getInstance(context).getStartTimeForPagination(), null);
        }
    }

    /**
     * Get the list of all conversations for the current user. Null in case of error or if there are no conversations.
     *
     * <p>Each {@link Message} object in the list corresponds to a conversation. It is the latest message for that conversation.</p>
     *
     * <code>
     *     message.getGroupId(); //if this is non-null, the message corresponds to a channel conversation
     *     message.getTo(); //if group-id is null then the message is for a 1-to-1 conversation, this is the user-id of that user.
     *     message.getMessage(); //the latest message text for the conversation
     * </code>
     *
     * @param searchString to search messages by
     * @param startTime {@link Message#getCreatedAtTime()} of the message to start the list from
     * @param endTime {@link Message#getCreatedAtTime()} of the message to end the list with
     */
    public static @NonNull AlAsyncTask<Void, List<Message>> conversationListFromCustomParameters(@NonNull Context context, @Nullable String searchString, @Nullable Long startTime, @Nullable Long endTime) {
        return new AlAsyncTask<Void, List<Message>>() {
            @Override
            protected List<Message> doInBackground() {
                return new MessageListTask(context, searchString, null, null, startTime, endTime, null, true).doInBackground();
            }
        };
    }

    /**
     * Get the list of messages for the user(one-to-one) with the given user-id.
     *
     * @param endTime pass null for first batch of 50 messages. pass the {@link Message#getCreatedAtTime()} of the oldest message in previous list to get the next 50.
     */
    private static @NonNull AlAsyncTask<Void, List<Message>> messageListForContact(@NonNull Context context, @Nullable Long endTime, @Nullable String userId) {
        return new AlAsyncTask<Void, List<Message>>() {
            @Override
            protected List<Message> doInBackground() {
                return new MessageListTask(context, null, userId != null ? new AppContactService(context).getContactById(userId) : null, null, null, endTime, null, true).doInBackground();
            }
        };
    }

    /**
     * Get the list of messages for the channel with the given channel key.
     *
     * @param endTime pass null for first batch of 50 messages. pass the {@link Message#getCreatedAtTime()} of the oldest message in previous list to get the next 50.
     */
    public static @NonNull AlAsyncTask<Void, List<Message>> messageListForChannel(@NonNull Context context, @Nullable Long endTime, @Nullable Integer channelKey) {
        return new AlAsyncTask<Void, List<Message>>() {
            @Override
            protected List<Message> doInBackground() {
                return new MessageListTask(context, null, null, channelKey != null ? ChannelService.getInstance(context).getChannel(channelKey) : null, null, endTime, null, true).doInBackground();
            }
        };
    }

    /**
     * See {@link AlConversation}.
     */
    public static @NonNull AlConversation getConversationFromMessage(@NonNull Context context, @NonNull Message message) {
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

    /**
     * Delete a group (<i>channelKey</i>) or 1-to-1 conversation (<i>contact</i>) or both. Use with caution.
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

    /**
     * Downloads and save attachment for message.
     *
     * <p>After {@link MediaDownloadProgressHandler#onCompleted(Message, ApplozicException)} is called, {@link Message#getFilePaths()} will
     * give you the local file path of the downloaded attachment.</p>
     */
    public static void downloadMessage(@NonNull Context context, @Nullable Message message, @Nullable MediaDownloadProgressHandler handler) {
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

    /**
     * Mark a conversation as read. Either one of the <i>userId</i> or the <i>groupId</i> can be null.
     *
     * @param pairedMessageKey not used. pass null
     * @param userId for one-to-one conversation
     * @param groupId for group conversation
     */
    public static void markAsRead(@NonNull Context context, @Nullable String pairedMessageKey, @Nullable String userId, @Nullable Integer groupId) {
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

    private static @NonNull AlAsyncTask<Void, List<Message>> messageListFromCustomParameters(@NonNull Context context, @Nullable String searchString, @Nullable Long startTime, @Nullable Long endTime, @Nullable String userId, @Nullable Integer channelKey) {
        return new AlAsyncTask<Void, List<Message>>() {
            @Override
            protected List<Message> doInBackground() {
                return new MessageListTask(context, searchString, userId != null ? new AppContactService(context).getContactById(userId) : null, channelKey != null ? ChannelService.getInstance(context).getChannel(channelKey) : null, startTime, endTime, null, true).doInBackground();
            }
        };
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
        public static @NonNull AlAsyncTask<Void, Channel> channel(@NonNull Context context, @Nullable String clientGroupId, @Nullable Integer orChannelKey) {
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
        public static @NonNull AlAsyncTask<Void, List<Channel>> allChannels(@NonNull Context context) {
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

        /**
         * Downloads and saves the channel display image. Gives null for <i>bitmap</i> if not present.
         *
         * <p>On successful download (bitmap != null), {@link Channel#getLocalImageUri()} will give you the local path to image.</p>
         */
        public static @NonNull AlAsyncTask<Void, Bitmap> downloadChannelDisplayImage(@NonNull Context context, @Nullable Channel channel) {
            return new AlAsyncTask<Void, Bitmap>() {
                @Override
                protected Bitmap doInBackground() {
                    return new AppContactService(context).downloadGroupImage(context, channel);
                }
            };
        }
    }

    /**
     * For dealing with {@link Contact}s.
     */
    public static class Contacts {

        /**
         * Use the {@link AppContactService} to add, update, retrieve contacts.
         *
         * <p>Methods of the {@link AppContactService} class can be run asynchronously using the {@link AlAsyncTask}.</p>
         *
         * <p>Create a custom task:</p>
         * <code>
         *     AlAsyncTask<Void, Contact> retrieveContact = new AlAsyncTask<Void, Contact>() { //Contact is the return type for doInBackground(), and you can ignore Void
         *         @Override
         *         protected Contact doInBackground() throws Exception {
         *             return getChannelDatabaseService(context).getContactById("userid");
         *         }
         *     };
         *
         *     Contact contact = retrieveContact.executeSync();
         *     //or
         *     retrieveContact.executeAsync(new BaseAsyncTask.AsyncListener<Contact>() {
         *         @Override
         *         public void onComplete(@Nullable Contact contact) { }
         *
         *         @Override
         *         public void onFailed(Throwable throwable) { }
         *     });
         * </code>
         */
        public static @NonNull AppContactService getChannelDatabaseService(@NonNull Context context) {
            AlAsyncTask<Void, Contact> retrieveContact = new AlAsyncTask<Void, Contact>() {
                @Override
                protected Contact doInBackground() throws Exception {
                    return getChannelDatabaseService(context).getContactById("userid");
                }
            };
            return new AppContactService(context);
        }

        /**
         * Get user details from the backend for the given user-ids.
         *
         * <p>Contact objects for these users are also created/updated.</p>
         */
        public static @NonNull AlAsyncTask<Void, List<UserDetail>> userDetails(@NonNull Context context, @Nullable Set<String> userIds) {
            return new AlAsyncTask<Void, List<UserDetail>>() {
                @Override
                protected List<UserDetail> doInBackground() throws Exception {
                    String response = new UserClientService(context).postUserDetailsByUserIds(userIds);
                    try {
                        return (List<UserDetail>) GsonUtils.getObjectFromJson(response, new TypeToken<List<UserDetail>>() {}.getType());
                    } catch (Exception exception) {
                        return null;
                    }
                }
            };
        }

        /**
         * Updates user details locally and in the backend.
         *
         * <p>{@link User#getUserId()} must be non-null and non-empty.</p>
         *
         * <p>On success, the <i>apiResponse</i> object will be non-null and {@link ApiResponse#isSuccess()} will be true.</p>
         */
        public static @NonNull AlAsyncTask<Void, ApiResponse<?>> updateUserDetails(@NonNull Context context, @Nullable User user) {
            return new AlAsyncTask<Void, ApiResponse<?>>() {
                @Override
                protected ApiResponse<?> doInBackground() {
                    if (user == null) {
                        return null;
                    }

                    return UserService.getInstance(context).updateUserWithResponse(user);
                }
            };
        }

        /**
         * Blocks/unblocks the user with the given userId for the current logged in user.
         *
         * <p>On success, the <i>apiResponse</i> object will be non-null and {@link ApiResponse#isSuccess()} will be true.</p>
         */
        public static @NonNull AlAsyncTask<Void, ApiResponse<?>> blockUser(@NonNull Context context, @Nullable String userId, boolean block) {
            return new AlAsyncTask<Void, ApiResponse<?>>() {
                @Override
                protected ApiResponse<?> doInBackground() {
                    if (TextUtils.isEmpty(userId)) {
                        return null;
                    }

                    return UserService.getInstance(context).processUserBlock(userId, block);
                }
            };
        }

        /**
         * Mutes notifications for the given user Id.
         *
         * <p>To un-mute, pass 0 for <code>notificationAfterTime</code>.</p>
         *
         * <p>On success, the <i>apiResponse</i> object will be non-null and {@link ApiResponse#isSuccess()} will be true.</p>
         */
        public static @NonNull AlAsyncTask<Void, ApiResponse<?>> muteUser(@NonNull Context context, @Nullable String userId, @Nullable Long notificationAfterTime) {
            return new AlAsyncTask<Void, ApiResponse<?>>() {
                @Override
                protected ApiResponse<?> doInBackground() {
                    if (TextUtils.isEmpty(userId) || notificationAfterTime == null) {
                        return null;
                    }

                    return UserService.getInstance(context).muteUserNotifications(userId, notificationAfterTime);
                }
            };
        }

        /**
         * Downloads and saves the contact display image. Gives null for <i>bitmap</i> if not present.
         *
         * <p>On successful download (bitmap != null), {@link Contact#getLocalImageUrl()} will give you the local path to image.</p>
         */
        public static @NonNull AlAsyncTask<Void, Bitmap> downloadContactDisplayImage(@NonNull Context context, @Nullable Contact contact) {
            return new AlAsyncTask<Void, Bitmap>() {
                @Override
                protected Bitmap doInBackground() {
                    return new AppContactService(context).downloadContactImage(context, contact);
                }
            };
        }
    }

    //old api >>>

    /**
     * Consider using {@link ApplozicConversation#conversationList(Context, boolean)} instead.
     */
    public static void getLatestMessageList(@NonNull Context context, boolean isScroll, @Nullable MessageListHandler handler) {
        getLatestMessageList(context, null, isScroll, handler);
    }

    /**
     * Consider using {@link ApplozicConversation#conversationListForSearch(Context, String, boolean)} instead.
     */
    public static void getLatestMessageList(@NonNull Context context, @Nullable String searchString, boolean isScroll, @Nullable MessageListHandler handler) {
        if (!isScroll) {
            AlTask.execute(new MessageListTask(context, searchString, null, null, null, null, handler, true));
        } else {
            AlTask.execute(new MessageListTask(context, searchString, null, null, MobiComUserPreference.getInstance(context).getStartTimeForPagination(), null, handler, true));
        }
    }

    /**
     * Consider using {@link ApplozicConversation#conversationListFromCustomParameters(Context, String, Long, Long)} instead.
     */
    public static void getLatestMessageList(@NonNull Context context, @Nullable String searchString, @Nullable Long startTime, @Nullable MessageListHandler handler) {
        AlTask.execute(new MessageListTask(context, searchString, null, null, startTime, null, handler, true));
    }

    /**
     * Consider using {@link ApplozicConversation#messageListForContact(Context, Long, String)} instead.
     */
    public static void getMessageListForContact(@NonNull Context context, @NonNull String userId, @Nullable Long endTime, @Nullable MessageListHandler handler) {
        AlTask.execute(new MessageListTask(context, null, new AppContactService(context).getContactById(userId), null, null, endTime, handler, false));
    }

    /**
     * Consider using {@link ApplozicConversation#messageListForChannel(Context, Long, Integer)} instead.
     */
    public static void getMessageListForChannel(@NonNull Context context, @NonNull Integer channelKey, @Nullable Long endTime, @Nullable MessageListHandler handler) {
        AlTask.execute(new MessageListTask(context, null, null, ChannelService.getInstance(context).getChannel(channelKey), null, endTime, handler, false));
    }

    //internal methods >>>

    /**
     * Internal method. Do not use.
     */
    public static boolean isMessageStatusPublished(Context context, String pairedMessageKey, Short status) {
        ApplozicMqttService applozicMqttService = ApplozicMqttService.getInstance(context);

        if (!TextUtils.isEmpty(pairedMessageKey) && applozicMqttService.isConnected()) {
            final String MESSAGE_STATUS_TOPIC = "message-status";
            applozicMqttService.connectAndPublishMessageStatus(MESSAGE_STATUS_TOPIC, MobiComUserPreference.getInstance(context).getUserId() + "," + pairedMessageKey + "," + status);
            return true;
        }
        return false;
    }

    //deprecated >>>

    /**
     * @deprecated Use {@link ApplozicConversation#conversationList(Context, boolean)}. Same functionality.
     */
    @Deprecated
    public static void getConversationList(Context context, String searchString, boolean isScroll, ConversationListHandler handler) {
        AlTask.execute(new ConversationListTask(context, searchString, null, null, (isScroll ? MobiComUserPreference.getInstance(context).getStartTimeForPagination() : null), null, handler, true));
    }

    /**
     * @deprecated Same as {@link #getMessageListForContact(Context, String, Long, MessageListHandler)}. You can use {@link Contact#getUserId()}.
     */
    @Deprecated
    public static void getMessageListForContact(Context context, Contact contact, Long endTime, MessageListHandler handler) {
        AlTask.execute(new MessageListTask(context, null, contact, null, null, endTime, handler, false));
    }

    /**
     * @deprecated Same as {@link #getMessageListForChannel(Context, Integer, Long, MessageListHandler)}. You can use {@link Channel#getKey()}.
     */
    @Deprecated
    public static void getMessageListForChannel(Context context, Channel channel, Long endTime, MessageListHandler handler) {
        AlTask.execute(new MessageListTask(context, null, null, channel, null, endTime, handler, false));
    }

    /**
     * @deprecated This method solves no special purpose. Its a simple utility method that maintains a list of latest messages. Please implement it on your own.
     */
    @Deprecated
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

    /**
     * @deprecated This method solves no special purpose. Its a simple utility method that maintains a list of latest messages. Please implement it on your own.
     */
    @Deprecated
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

    /**
     * @deprecated This method solves no special purpose. Its a simple utility method that maintains a list of latest messages. Please implement it on your own.
     */
    @Deprecated
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

    /**
     * @deprecated This method solves no special purpose. Its a simple utility method that maintains a list of latest messages. Please implement it on your own.
     */
    @Deprecated
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
}
