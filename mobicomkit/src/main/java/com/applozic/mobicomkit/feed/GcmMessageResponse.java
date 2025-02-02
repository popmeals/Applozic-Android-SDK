package com.applozic.mobicomkit.feed;

import com.applozic.mobicommons.json.JsonMarker;
import com.applozic.mobicomkit.api.conversation.Message;

/**
 * Model class for storing data for a Google Cloud Messaging response.
 */
public class GcmMessageResponse extends JsonMarker {

    private String id;
    private String type;
    private Message message;
    private boolean notifyUser;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public boolean isNotifyUser() {
        return notifyUser;
    }

    public void setNotifyUser(boolean notifyUser) {
        this.notifyUser = notifyUser;
    }

    @Override
    public String toString() {
        return "MqttMessageResponse{" +
                "id='" + id + '\'' +
                "type='" + type + '\'' +
                ", message='" + message + '\'' +
                ", notifyUser=" + notifyUser +
                '}';
    }
}
