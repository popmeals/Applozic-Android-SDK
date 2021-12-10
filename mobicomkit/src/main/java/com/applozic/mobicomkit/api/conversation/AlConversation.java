package com.applozic.mobicomkit.api.conversation;

import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.contact.Contact;

/**
 * A conversation is an exchange of messages between two or more users. It can be one-to-one or a group conversation.
 *
 * <p>{@link #channel} will be non-null for a group conversation.</p>
 * <p>In-case {@link #channel} is null, {@link #contact} will store the denote a one-to-one conversation.</p>
 */
public class AlConversation {
    /**
     * Group conversation.
     */
    private Channel channel;
    /**
     * One-to-one conversation.
     */
    private Contact contact;
    /**
     * The latest message for the given conversation.
     */
    private Message message;
    private int unreadCount;

    /**
     * @see #channel
     */
    public Channel getChannel() {
        return channel;
    }

    /**
     * @see #contact
     */
    public Contact getContact() {
        return contact;
    }

    /**
     * @see #message
     */
    public Message getMessage() {
        return message;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    //internal methods >>>

    /** Internal. **/
    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    /** Internal. **/
    public void setContact(Contact contact) {
        this.contact = contact;
    }

    /** Internal. **/
    public void setMessage(Message message) {
        this.message = message;
    }

    /** Internal. **/
    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }
}
