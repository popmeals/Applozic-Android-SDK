package com.applozic.mobicomkit.api.attachment.urlservice;

import android.content.Context;
import android.text.TextUtils;

import com.applozic.mobicomkit.annotations.ApplozicInternal;
import com.applozic.mobicomkit.api.HttpRequestUtils;
import com.applozic.mobicomkit.api.MobiComKitClientService;
import com.applozic.mobicomkit.api.conversation.Message;

import java.io.IOException;
import java.net.HttpURLConnection;

//ApplozicInternal: default
/**
 * This class provides URLs for upload/download of media stored at Google Cloud Storage.
 */
@ApplozicInternal
public class GoogleCloudURLService implements URLService {

    private MobiComKitClientService mobiComKitClientService;
    private HttpRequestUtils httpRequestUtils;
    private static final String GET_SIGNED_URL = "/files/url?key=";
    private static final String UPLOAD_URL = "/files/upload";

    //ApplozicInternal: default
    @ApplozicInternal
    public GoogleCloudURLService(Context context) {
        mobiComKitClientService = new MobiComKitClientService(context);
        httpRequestUtils = new HttpRequestUtils(context);
    }

    //ApplozicInternal: default
    /**
     * Gets the HTTP connection object that can be used to download the attachment image for the given message.
     *
     * @param message the {@link Message} to get the connection for
     * @return the HTTP connection object, null if message does not have an attachment or thumbnail
     * @throws IOException while opening connection
     */
    @ApplozicInternal
    @Override
    public HttpURLConnection getAttachmentConnection(Message message) throws IOException {

        String response = httpRequestUtils.getResponse(mobiComKitClientService.getFileBaseUrl() + GET_SIGNED_URL + message.getFileMetas().getBlobKeyString(), "application/json", "application/json",true);
        if (TextUtils.isEmpty(response)) {
            return null;
        } else {
            return mobiComKitClientService.openHttpConnection(response);
        }
    }

    //ApplozicInternal: default
    /**
     * Gets the URL to download the image/video attachment thumbnail.
     *
     * @param message the {@link Message} to get the thumbnail URL for
     * @return the string URL, empty if message does not have an attachment or thumbnail
     * @throws IOException ignore, does not throw IOException
     */
    @ApplozicInternal
    @Override
    public String getThumbnailURL(Message message) throws IOException {
        return httpRequestUtils.getResponse(mobiComKitClientService.getFileBaseUrl() + GET_SIGNED_URL + message.getFileMetas().getThumbnailBlobKey(), "application/json", "application/json",true);
    }

    //ApplozicInternal: default
    /**
     * Gets the URL that can be used to upload media files to the Google Cloud Storage.
     * @return the string URL
     */
    @ApplozicInternal(warningLevel = ApplozicInternal.WarningLevel.USE_WITH_CAUTION)
    @Override
    public String getFileUploadUrl() {
        return mobiComKitClientService.getFileBaseUrl() + UPLOAD_URL;
    }

    //ApplozicInternal: default
    /**
     * Gets the URL that can be used to download media files from the Google Cloud Storage.
     * @return the string URL
     */
    @ApplozicInternal
    @Override
    public String getImageUrl(Message message) {
        return message.getFileMetas().getUrl();
    }
}
