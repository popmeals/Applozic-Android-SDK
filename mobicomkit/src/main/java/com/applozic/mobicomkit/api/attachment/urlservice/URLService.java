package com.applozic.mobicomkit.api.attachment.urlservice;

import com.applozic.mobicomkit.api.conversation.Message;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * Interface for classes that provide upload/download URLs. To be implemented for different storage services.
 */
//Cleanup: protected
public interface URLService {

    HttpURLConnection getAttachmentConnection(Message message) throws IOException;

    String getThumbnailURL(Message message) throws IOException;

    String getFileUploadUrl();

    String getImageUrl(Message message);
}
