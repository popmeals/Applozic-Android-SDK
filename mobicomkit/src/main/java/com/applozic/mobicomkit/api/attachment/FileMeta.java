package com.applozic.mobicomkit.api.attachment;

import com.applozic.mobicommons.json.JsonMarker;

/**
 * Model class to store the file attachments information for a {@link com.applozic.mobicomkit.api.conversation.Message}.
 *
 * <p>This includes the remote file data such as thumbnail key, file key, file URLs etc.
 * You do not need to access fields of this class directly. {@link FileClientService} has most of the
 * methods needed to download/upload attachments and other Applozic media.</p>
 *
 * <p>The blob keys when appended to the appropriate URLs can be used to download the files.
 * See {@link com.applozic.mobicomkit.api.attachment.urlservice.URLServiceProvider}.</p>
 */
public class FileMeta extends JsonMarker {

    private String key;
    private String userKey;
    private String blobKey;
    private String thumbnailBlobKey;
    private String name;
    private String url;
    private int size;
    private String contentType;
    private String thumbnailUrl;
    private Long createdAtTime;

    /**
     * Returns the id of the <code>FileMeta</code> object.
     */
    public String getKeyString() {
        return key;
    }

    public void setKeyString(String keyString) {
        this.key = keyString;
    }

    /**
     * @deprecated This method is no longer used and will be removed soon.
     */
    @Deprecated
    public String getSuUserKeyString() {
        return userKey;
    }

    /**
     * @deprecated This method is no longer used and will be removed soon.
     */
    @Deprecated
    public void setSuUserKeyString(String suUserKeyString) {
        this.userKey = suUserKeyString;
    }

    /**
     * Gets the blob key for the file. This key can be appended to the appropriate URLs to download the file. See {@link com.applozic.mobicomkit.api.attachment.urlservice.URLServiceProvider}.
     */
    public String getBlobKeyString() {
        return blobKey;
    }

    /**
     * Gets the blob key for the file thumbnail (image/video). This key can be appended to the appropriate URLs to download the file. See {@link com.applozic.mobicomkit.api.attachment.urlservice.URLServiceProvider}.
     */
    public String getThumbnailBlobKey() {
        return thumbnailBlobKey;
    }

    public void setThumbnailBlobKey(String thumbnailBlobKey) {
        this.thumbnailBlobKey = thumbnailBlobKey;
    }

    public void setBlobKeyString(String blobKeyString) {
        this.blobKey = blobKeyString;
    }

    /**
     * Gets the name fo the file.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the time at which the file was uploaded to the server.
     */
    public Long getCreatedAtTime() {
        return createdAtTime;
    }

    public void setCreatedAtTime(Long createdAtTime) {
        this.createdAtTime = createdAtTime;
    }

    /**
     * Gets the size of the file in bytes.
     */
    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    /**
     * Gets the file content type (image,video,contact etc)
     */
    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Gets the URL of the file thumbnail, if present. This is usually non-empty if the file is being stored in the Applozic servers.
     */
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    /**
     * Gets the URL of the file, if present. This is usually non-empty if the file is being stored in the Applozic servers.
     */
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @deprecated This method is not usable. Will be removed soon.
     */
    @Deprecated
    public String getSizeInReadableFormat() {
        String value = "0 KB";
        if (size / 1024 >= 1024) {
            value = String.valueOf(Math.round(size / (1024 * 1024))) + " MB";
        } else {
            value = String.valueOf(Math.round(size / 1024)) + " KB";
        }
        return value;
    }

    @Override
    public String toString() {
        return "FileMeta{" +
                "key='" + key + '\'' +
                ", userKey='" + userKey + '\'' +
                ", blobKey='" + blobKey + '\'' +
                ", thumbnailBlobKey='" + thumbnailBlobKey + '\'' +
                ", url=" + url +
                ", name='" + name + '\'' +
                ", size=" + size +
                ", contentType='" + contentType + '\'' +
                ", thumbnailUrl='" + thumbnailUrl + '\'' +
                ", createdAtTime=" + createdAtTime +
                '}';
    }


}
