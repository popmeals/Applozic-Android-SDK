package com.applozic.mobicomkit.api.attachment.urlservice;

import android.content.Context;
import android.text.TextUtils;

import com.applozic.mobicomkit.annotations.ApplozicInternal;
import com.applozic.mobicomkit.api.HttpRequestUtils;
import com.applozic.mobicomkit.api.MobiComKitClientService;
import com.applozic.mobicomkit.api.attachment.FileClientService;
import com.applozic.mobicomkit.api.conversation.Message;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Date;

//ApplozicInternal: default
/**
 * This class provides URLs for upload/download of media stored at Applozic servers.
 */
@ApplozicInternal
public class DefaultURLService implements URLService {

    private MobiComKitClientService mobiComKitClientService;
    private HttpRequestUtils httpRequestUtils;

    //ApplozicInternal: default
    @ApplozicInternal
    public DefaultURLService(Context context) {
        this.httpRequestUtils = new HttpRequestUtils(context);
        mobiComKitClientService = new MobiComKitClientService(context);
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
        return !TextUtils.isEmpty(message.getFileMetas().getUrl()) ? mobiComKitClientService.openHttpConnection(message.getFileMetas().getUrl()) : mobiComKitClientService.openHttpConnection(mobiComKitClientService.getFileUrl() + message.getFileMetas().getBlobKeyString());
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
        return message.getFileMetas().getThumbnailUrl();
    }

    //ApplozicInternal: default
    /**
     * Gets the URL that can be used to upload media files to the Applozic servers.
     * @return the string URL
     */
    @ApplozicInternal(warningLevel = ApplozicInternal.WarningLevel.USE_WITH_CAUTION)
    @Override
    public String getFileUploadUrl() {
        return httpRequestUtils.getResponse(mobiComKitClientService.getFileBaseUrl() + FileClientService.FILE_UPLOAD_URL
                + "?data=" + new Date().getTime(), "text/plain", "text/plain", true);
    }

    //ApplozicInternal: default
    /**
     * Gets the URL that can be used to download media files from the Applozic servers.
     * @return the string URL
     */
    @ApplozicInternal
    @Override
    public String getImageUrl(Message message) {
        return mobiComKitClientService.getFileUrl() + message.getFileMetas().getBlobKeyString();
    }

}
