package com.applozic.mobicomkit.api.attachment.urlservice;

import android.content.Context;

import com.applozic.mobicomkit.ApplozicClient;
import com.applozic.mobicomkit.api.MobiComKitClientService;
import com.applozic.mobicomkit.api.attachment.FileMeta;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicommons.ApplozicService;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * Class that provides the appropriate URL service class({@link URLService}).
 *
 * Direct access to methods of this class will not be required.
 */
public class URLServiceProvider {

    private Context context;
    private URLService urlService;
    private MobiComKitClientService mobiComKitClientService;

    //Cleanup: default
    public URLServiceProvider(Context context) {
        this.context = ApplozicService.getContext(context);
        mobiComKitClientService = new MobiComKitClientService(context);
    }

    private URLService getUrlService(Context context) {

        if (urlService != null) {
            return urlService;
        }

        ApplozicClient appClient = ApplozicClient.getInstance(context);

        if (appClient.isS3StorageServiceEnabled()) {
            urlService = new S3URLService(context);
        } else if (appClient.isGoogleCloudServiceEnabled()) {
            urlService = new GoogleCloudURLService(context);
        } else if (appClient.isStorageServiceEnabled()) {
            urlService = new ApplozicMongoStorageService(context);
        } else {
            urlService = new DefaultURLService(context);
        }

        return urlService;
    }

    public HttpURLConnection getDownloadConnection(Message message) throws IOException {
        HttpURLConnection connection;

        try {
            connection = getUrlService(context).getAttachmentConnection(message);
        } catch (Exception e) {
            throw new IOException("Error connecting");
        }
        return connection;
    }

    /**
     * The thumbnail URL is either taken from {@link FileMeta#getThumbnailUrl()} or generated using {@link FileMeta#getThumbnailBlobKey()}.
     *
     * <p>This {@link FileMeta} object is retrieved for the using {@link Message#getFileMetas()}.</p>
     */
    public String getThumbnailURL(Message message) throws IOException {
        try {
            return getUrlService(context).getThumbnailURL(message);
        } catch (Exception e) {
            throw new IOException("Error connecting");
        }
    }

    public String getFileUploadUrl() {
        return getUrlService(context).getFileUploadUrl();
    }

    public String getImageURL(Message message) {
        return getUrlService(context).getImageUrl(message);
    }
}
