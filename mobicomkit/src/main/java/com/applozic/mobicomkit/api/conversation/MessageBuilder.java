package com.applozic.mobicomkit.api.conversation;

import android.content.Context;

import com.applozic.mobicomkit.api.people.ChannelInfo;
import com.applozic.mobicomkit.exception.ApplozicException;
import com.applozic.mobicomkit.listners.MediaUploadProgressHandler;
import com.applozic.mobicommons.people.channel.Channel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Helper class to send a {@link Message}.
 *
 * <p><b>To send a message to user123:</b></p>
 * <code>
 *     MessageBuilder messageBuilder = new MessageBuilder(context)
 *                 .setMessage("Hello there!")
 *                 .setTo("user123");
 *
 *     messageBuilder.send();
 * </code>
 *
 * <p><b>To send a message to a group conversation:</b></p>
 * <p>- First create a {@link com.applozic.mobicommons.people.channel.Channel} using {@link com.applozic.mobicomkit.channel.service.ChannelService#createChannelWithResponse(ChannelInfo)}. Have the {@link Channel#getClientGroupId()} handy. Then:</p>
 * <code>
 *     MessageBuilder messageBuilder = new MessageBuilder(context)
 *                 .setMessage("Hello channel!")
 *                 .setClientGroupId("group123");
 *
 *     messageBuilder.send();
 * </code>
 *
 * <p><b>To send a message with an attachment:</b></p>
 * <code>
 *     MessageBuilder messageBuilder = new MessageBuilder(context)
 *                 .setMessage("Hello! Here is a picture.")
 *                 .setTo("user123")
 *                 .setContentType(Message.ContentType.ATTACHMENT.getValue())
 *                 .setFilePath("local/path/to/image.jpg");
 *
 *         messageBuilder.send(new MediaUploadProgressHandler() { ... });
 * </code>
 *
 * <p>To receive messages, refer to {@link com.applozic.mobicomkit.broadcast.AlEventManager} and {@link com.applozic.mobicomkit.Applozic#connectPublish(Context)}.</p>
 */
public class MessageBuilder {

    private Message message;
    private Context context;

    public MessageBuilder(Context context) {
        this.message = new Message();
        this.context = context;
    }

    /**
     * Sets the user-id of the receiver for a 1:1 chat.
     */
    public MessageBuilder setTo(String to) {
        message.setTo(to);
        return this;
    }

    /**
     * Set the message text. This is optional if you're sending an attachment.
     */
    public MessageBuilder setMessage(String message) {
        this.message.setMessage(message);
        return this;
    }

    /**
     * Set the message {@link com.applozic.mobicomkit.api.conversation.Message.MessageType type}.
     */
    public MessageBuilder setType(Short type) {
        message.setType(type);
        return this;
    }

    /**
     * Set the local path to the message attachment file.
     *
     * <p>Images, video, audio, documents are supported.</p>
     */
    public MessageBuilder setFilePath(String filePath) {
        List<String> pathList = new ArrayList<>();
        pathList.add(filePath);
        message.setFilePaths(pathList);
        return this;
    }

    /**
     * Set the message {@link com.applozic.mobicomkit.api.conversation.Message.ContentType content type}.
     */
    public MessageBuilder setContentType(short contentType) {
        message.setContentType(contentType);
        return this;
    }

    /**
     * To send a message to a group, call this to set its {@link Channel#getKey() id} (also called key/channel key).
     *
     * This is an alternative to {@link #setClientGroupId(String)}.
     */
    public MessageBuilder setGroupId(Integer groupId) {
        message.setGroupId(groupId);
        return this;
    }

    /**
     * To send a message to a group, call this to set the {@link com.applozic.mobicommons.people.channel.Channel#setClientGroupId(String) client group id}.
     *
     * This is an alternative to {@link #setGroupId(Integer)}.
     */
    public MessageBuilder setClientGroupId(String clientGroupId) {
        message.setClientGroupId(clientGroupId);
        return this;
    }

    /**
     * Sets any custom key-value data for the message.
     */
    public MessageBuilder setMetadata(Map<String, String> metadata) {
        message.setMetadata(metadata);
        return this;
    }

    /**
     * Alternatively, you can directly set a {@link Message} object to send.
     */
    public MessageBuilder setMessageObject(Message message) {
        this.message = message;
        return this;
    }

    /**
     * Returns the message object to be sent.
     */
    public Message getMessageObject() {
        return message;
    }

    /**
     * Sends the created message.
     */
    public void send() {
        new MobiComConversationService(context).sendMessageWithHandler(message, null);
    }

    /**
     * Sends the created message with attachment progress and success callbacks.
     */
    public void send(MediaUploadProgressHandler handler) {
        new MobiComConversationService(context).sendMessageWithHandler(message, handler);
    }
}
