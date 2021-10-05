package com.applozic.mobicomkit.api.attachment.urlservice;

import android.content.Context;

import com.applozic.mobicomkit.api.MobiComKitClientService;
import com.applozic.mobicomkit.api.conversation.Message;

import java.io.IOException;
import java.net.HttpURLConnection;

//Cleanup: default

/**
 * This class provides URLs for upload/download of media stored at Mongo storage.
 */
public class ApplozicMongoStorageService implements URLService {

    private MobiComKitClientService mobiComKitClientService;

    private static final String UPLOAD_URL ="/files/v2/upload";
    private static final String DOWNLOAD_URL ="/files/get/";

    //Cleanup: default
    public ApplozicMongoStorageService(Context context) {
        mobiComKitClientService = new MobiComKitClientService(context);
    }

    //Cleanup: default
    /**
     * Gets the HTTP connection object that can be used to download the attachment image for the given message.
     *
     * @param message the {@link Message} to get the connection for
     * @return the HTTP connection object, null if message does not have an attachment or thumbnail
     * @throws IOException while opening connection
     */
    @Override
    public HttpURLConnection getAttachmentConnection(Message message) throws IOException {

        return mobiComKitClientService.openHttpConnection( mobiComKitClientService.getFileBaseUrl()
                + DOWNLOAD_URL
                + message.getFileMetas().getBlobKeyString());

    }

    //Cleanup: default
    /**
     * Gets the URL to download the image/video attachment thumbnail.
     *
     * @param message the {@link Message} to get the thumbnail URL for
     * @return the string URL, empty if message does not have an attachment or thumbnail
     * @throws IOException ignore, does not throw IOException
     */
    @Override
    public String getThumbnailURL(Message message) throws IOException {
        return message.getFileMetas().getThumbnailUrl();
    }

    //Cleanup: default
    /**
     * Gets the URL that can be used to upload media files to the Mongo storage.
     * @return the string URL
     */
    @Override
    public String getFileUploadUrl() {
        return mobiComKitClientService.getFileBaseUrl() + UPLOAD_URL;
    }

    //Cleanup: default
    /**
     * Gets the URL that can be used to download media files from the Mongo storage.
     * @return the string URL
     */
    @Override
    public String getImageUrl(Message message) {
        return message.getFileMetas().getBlobKeyString();
    }
}
