package com.applozic.mobicomkit.api.attachment;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.applozic.mobicomkit.ApplozicClient;
import com.applozic.mobicomkit.annotations.ApplozicInternal;
import com.applozic.mobicomkit.api.HttpRequestUtils;
import com.applozic.mobicomkit.api.MobiComKitClientService;
import com.applozic.mobicomkit.api.attachment.urlservice.URLServiceProvider;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.api.conversation.MessageBuilder;
import com.applozic.mobicomkit.api.conversation.database.MessageDatabaseService;
import com.applozic.mobicomkit.api.conversation.service.ConversationService;
import com.applozic.mobicomkit.feed.TopicDetail;
import com.applozic.mobicommons.commons.core.utils.DateUtils;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.commons.image.ImageUtils;
import com.applozic.mobicommons.file.FileUtils;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.channel.Conversation;
import com.applozic.mobicommons.people.contact.Contact;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * The <code>FileClientService</code> provides methods that can be used to upload/download Applozic media/attachment files.
 *
 * <p>See {@link FileMeta}, {@link Message} and {@link MessageBuilder} for more information on how attachments are handled.</p>
 *
 * <p><i><b>Note:</b></i>All methods of this class run synchronously. Run them in a background thread.</p>
 *
 * <p>Attachment uploads are handled internally when a message is sent using the {@link MessageBuilder} class.</p>
 */
public class FileClientService extends MobiComKitClientService {

    //Todo: Make the base folder configurable using either strings.xml or properties file
    //ApplozicInternal: default, make all private unless specified
    public static final String MOBI_COM_IMAGES_FOLDER = "/image";
    public static final String MOBI_COM_VIDEOS_FOLDER = "/video";
    public static final String MOBI_COM_CONTACT_FOLDER = "/contact";
    public static final String MOBI_COM_OTHER_FILES_FOLDER = "/other";
    public static final String MOBI_COM_THUMBNAIL_SUFIX = "/.Thumbnail";
    @ApplozicInternal public static final String FILE_UPLOAD_URL = "/rest/ws/aws/file/url";
    public static final String IMAGE_DIR = "image";
    public static final String AL_UPLOAD_FILE_URL = "/rest/ws/upload/file";
    public static final String CUSTOM_STORAGE_SERVICE_END_POINT = "/rest/ws/upload/image";
    //    public static final String S3_SIGNED_URL_END_POINT = "/rest/ws/upload/file";
    public static final String S3_SIGNED_URL_END_POINT = "/rest/ws/upload/image"; //ApplozicInternal: default
    public static final String S3_SIGNED_URL_PARAM = "aclsPrivate"; //ApplozicInternal: default
    public static final String THUMBNAIL_URL = "/files/";
    private static final int MARK = 1024;
    private static final String TAG = "FileClientService";
    private static final String MAIN_FOLDER_META_DATA = "main_folder_name";
    private HttpRequestUtils httpRequestUtils;
    private MobiComKitClientService mobiComKitClientService;

    /**
     * Creates a new {@link FileClientService} object with the given context.
     *
     * @param context the context, can be application level
     */
    public FileClientService(Context context) {
        super(context);
        this.httpRequestUtils = new HttpRequestUtils(context);
        this.mobiComKitClientService = new MobiComKitClientService(context);
    }

    //ApplozicInternal: rename to getApplozicInternalFilePath
    /**
     * @ApplozicInternal This method is used internally to get the local file path of media files based on their content type.
     *
     * @param fileName the name of the media file
     * @param context the context
     * @param contentType the content type (image, video, text/x-vCard). it is usually retrieved for a message using {@link Message#getFileMetas()} and then {@link FileMeta#getContentType()}.
     * @param isThumbnail if the file is to be used as a thumbnail
     * @return the {@link File} object for the given filepath
     */
    @ApplozicInternal
    public static File getFilePath(String fileName, Context context, String contentType, boolean isThumbnail) {
        File filePath;
        File dir = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            String folder = "/" + Utils.getMetaDataValue(context, MAIN_FOLDER_META_DATA) + MOBI_COM_OTHER_FILES_FOLDER;

            if (contentType.startsWith("image")) {
                folder = "/" + Utils.getMetaDataValue(context, MAIN_FOLDER_META_DATA) + MOBI_COM_IMAGES_FOLDER;
            } else if (contentType.startsWith("video")) {
                folder = "/" + Utils.getMetaDataValue(context, MAIN_FOLDER_META_DATA) + MOBI_COM_VIDEOS_FOLDER;
            } else if (contentType.equalsIgnoreCase("text/x-vCard")) {
                folder = "/" + Utils.getMetaDataValue(context, MAIN_FOLDER_META_DATA) + MOBI_COM_CONTACT_FOLDER;
            }
            if (isThumbnail) {
                folder = folder + MOBI_COM_THUMBNAIL_SUFIX;
            }
            File directory = context.getExternalFilesDir(null);
            if (directory != null) {
                dir = new File(directory.getAbsolutePath() + folder);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
            }
        } else {
            dir = new File(new ContextWrapper(context).getFilesDir().getAbsolutePath() + MOBI_COM_IMAGES_FOLDER);
        }
        // Create image name
        //String extention = "." + contentType.substring(contentType.indexOf("/") + 1);
        filePath = new File(dir, fileName);
        return filePath;
    }

    /**
     * See {@link #getFilePath(String, Context, String, boolean)}.
     */
    @ApplozicInternal
    public static File getFilePath(String fileName, Context context, String contentType) {
        return getFilePath(fileName, context, contentType, false);
    }

    //ApplozicInternal: private
    /**
     * @ApplozicInternal Gets the URL that will be used to upload the profile image for the logged in user.
     */
    @ApplozicInternal(warningLevel = ApplozicInternal.WarningLevel.WILL_BREAK_CODE)
    public String profileImageUploadURL() {
        return getBaseUrl() + AL_UPLOAD_FILE_URL;
    }

    private String[] getParts(String filePath) {
        return filePath.split("/");
    }

    private String getVideoThumbnailFileNameForLocalGeneration(String filePath) {
        String[] parts = getParts(filePath);
        String videoFileName = parts[parts.length - 1].split("[.]")[0];
        return videoFileName + ".jpeg";
    }

    private String getThumbnailFileNameForServerDownload(Message message) {
        String contentType = message.getFileMetas().getContentType();
        String messageFileFormat = FileUtils.getFileFormat(message.getFileMetas().getName());
        boolean isThumbnailForVideo = !TextUtils.isEmpty(contentType) && contentType.contains("video");
        final String DEFAULT_EXTENSION_FOR_VIDEO_THUMBNAILS = "jpeg";
        String thumbnailExtension = isThumbnailForVideo ? DEFAULT_EXTENSION_FOR_VIDEO_THUMBNAILS : messageFileFormat;
        return FileUtils.getName(message.getFileMetas().getName()) + message.getCreatedAtTime() + "." + thumbnailExtension;
    }

    /**
     * This method downloads and saves the <i>thumbnail</i> for the message attachment (if image/video) to the appropriate folder. It also returns a bitmap of the image.
     *
     * <p>In the case where the message object being passed to this method already has the thumbnail downloaded and saved, this method will simply return a bitmap of that existing thumbnail.</p>
     *
     * <p>The thumbnail is retrieved/generated using {@link URLServiceProvider#getThumbnailURL(Message)}.
     * The thumbnail is then downloaded and saved locally ({@link FileClientService#getFilePath(String, Context, String, boolean)}).</p>
     *
     * <p>The thumbnail image bitmap once downloaded and saved can be accessed calling the method again:
     * <code>
     *     Bitmap thumbnailImage = downloadAndSaveThumbnailImage(context, theMessageObject, 0, 200);
     * </code>
     *
     * @param context the context
     * @param message the message object for which the thumbnail image is to be download and saved locally
     * @param reqHeight the requested height of the bitmap returned
     * @param reqWidth ignore, not used
     *
     * @return the image bitmap
     */
    public Bitmap downloadAndSaveThumbnailImage(Context context, Message message, int reqWidth, int reqHeight) {
        HttpURLConnection connection = null;
        try {
            Bitmap attachedImage = null;

            String thumbnailUrl = new URLServiceProvider(context).getThumbnailURL(message);

            if (TextUtils.isEmpty(thumbnailUrl)) {
                return null;
            }

            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            String contentType = message.getFileMetas().getContentType();
            String imageName = getThumbnailFileNameForServerDownload(message);
            String imageLocalPath = getFilePath(imageName, context, contentType, true).getAbsolutePath();

            if (imageLocalPath != null) {
                try {
                    attachedImage = BitmapFactory.decodeFile(imageLocalPath);
                } catch (Exception ex) {
                    Utils.printLog(context, TAG, "File not found on local storage: " + ex.getMessage());
                }
            }

            if (attachedImage == null) {
                connection = openHttpConnection(thumbnailUrl);
                if (connection.getResponseCode() == 200) {
                    // attachedImage = BitmapFactory.decodeStream(connection.getInputStream(),null,options);
                    attachedImage = BitmapFactory.decodeStream(connection.getInputStream());
                    File file = FileClientService.getFilePath(imageName, context, contentType, true);
                    imageLocalPath = ImageUtils.saveImageToInternalStorage(file, attachedImage);

                } else {
                    Utils.printLog(context, TAG, "Download is failed response code is ...." + connection.getResponseCode());
                    return null;
                }
            }
            // Calculate inSampleSize
            options.inSampleSize = ImageUtils.calculateInSampleSize(options, 200, reqHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            attachedImage = BitmapFactory.decodeFile(imageLocalPath, options);
            return attachedImage;
        } catch (FileNotFoundException ex) {
            Utils.printLog(context, TAG, "File not found on server: " + ex.getMessage());
        } catch (Exception ex) {
            Utils.printLog(context, TAG, "Exception fetching file from server: " + ex.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return null;
    }

    /**
     * This method will downloads and saves the contact attachment card for the given message object (if it has one).
     *
     * <p>The local path for this contact card is decided by the {@link #getFilePath(String, Context, String)} method.</p>
     *
     * <p><i>NOTE: This local path is also updated in the local database for the message object.</i></p>
     *
     * <p>Note: Multiple calls to this method <i>DO NOT</i> download the contact multiple time.</p>
     *
     * <p>To later get the local path of the contact card use:
     * <code>
     *     Message updatedMessage = new MessageDatabaseService(context).getMessage(messageKeyString); //message.getKeyString();
     *     updatedMessage.getFilePaths();
     * </code></p>
     *
     * @param message the message object to download the contact card for
     */
    public void loadContactsvCard(Message message) {
        File file = null;
        HttpURLConnection connection = null;
        try {
            InputStream inputStream = null;
            FileMeta fileMeta = message.getFileMetas();
            String contentType = fileMeta.getContentType();
            String fileName = fileMeta.getName();
            file = FileClientService.getFilePath(fileName, context.getApplicationContext(), contentType);
            if (!file.exists()) {

                connection = new URLServiceProvider(context).getDownloadConnection(message);
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    inputStream = connection.getInputStream();
                } else {
                    //TODO: Error Handling...
                    Utils.printLog(context, TAG, "Got Error response while uploading file : " + connection.getResponseCode());
                    return;
                }

                OutputStream output = new FileOutputStream(file);
                byte data[] = new byte[1024];
                int count = 0;
                while ((count = inputStream.read(data)) != -1) {
                    output.write(data, 0, count);
                }
                output.flush();
                output.close();
                inputStream.close();
            }
            //Todo: Fix this, so that attach package can be moved to mobicom mobicom.
            new MessageDatabaseService(context).updateInternalFilePath(message.getKeyString(), file.getAbsolutePath());

            ArrayList<String> arrayList = new ArrayList<String>();
            arrayList.add(file.getAbsolutePath());
            message.setFilePaths(arrayList);

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            Utils.printLog(context, TAG, "File not found on server");
        } catch (Exception ex) {
            //If partial file got created delete it, we try to download it again
            if (file != null && file.exists()) {
                Utils.printLog(context, TAG, " Exception occured while downloading :" + file.getAbsolutePath());
                file.delete();
            }
            ex.printStackTrace();
            Utils.printLog(context, TAG, "Exception fetching file from server");
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * This method downloads and saves the <i>GIF</i> file from the given URL. It returns the local path where the downloaded GIF is saved.
     *
     * <p>Some things to note:
     * <ul>
     *     <li>The download is done using a {@link java.net.URLConnection}.</li>
     *     <li>This name of the GIF will be generated as such: <code>"GIF_" + System.currentTimeMillis() + ".gif"</code>.</li>
     *     <li>The local path of the GIF will be decided by {@link #getFilePath(String, Context, String, boolean)}.</li>
     *     <li>There is no check for if the file being downloaded is a GIF or not.</li>
     * </ul></p>
     *
     * See the asynchronous {@link GifDownloadAsyncTask}.
     *
     * @param url the URL of the GIF to download
     * @return the absolute local file path for the downloaded GIF
     */
    public String downloadGif(String url) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try {
            URL urlConnection = new URL(url);
            connection = (HttpURLConnection) urlConnection.openConnection();
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "Gif Download: Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage());
                return null;
            }

            input = connection.getInputStream();

            String gifName = "GIF_" + System.currentTimeMillis() + ".gif";
            String gifLocalPath = getFilePath(gifName, context, "image", false).getAbsolutePath();
            File downloadedFile = new File(gifLocalPath);

            output = new FileOutputStream(downloadedFile);

            byte[] data = new byte[4096];
            int count;
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }

            return downloadedFile.getAbsolutePath();
        } catch (Exception e) {
            return e.toString();
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
                if (input != null) {
                    input.close();
                }
            } catch (IOException ignored) { }

            if (connection != null)
                connection.disconnect();
        }
    }

    /**
     * @ApplozicInternal Gets a bitmap from the URL, if found.
     */
    @ApplozicInternal
    public Bitmap loadMessageImage(Context context, String url) {
        try {
            Bitmap attachedImage = null;

            if (attachedImage == null) {
                InputStream in = new java.net.URL(url).openStream();
                if (in != null) {
                    attachedImage = BitmapFactory.decodeStream(in);
                }
            }
            return attachedImage;
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            Utils.printLog(context, TAG, "File not found on server: " + ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            Utils.printLog(context, TAG, "Exception fetching file from server: " + ex.getMessage());
        }

        return null;
    }

    /**
     * @ApplozicInternal Uploads the message attachment image/video if any. This method is called internally when the message is being sent to the server({@link MessageBuilder#send()}.
     */
    @ApplozicInternal
    public String uploadBlobImage(String path, Handler handler, String oldMessageKey) throws
            UnsupportedEncodingException {
        try {

            ApplozicMultipartUtility multipart = new ApplozicMultipartUtility(getUploadURL(), "UTF-8", context);
            if (ApplozicClient.getInstance(context).isS3StorageServiceEnabled()) {
                multipart.addFilePart("file", new File(path), handler, oldMessageKey);
            } else {
                multipart.addFilePart("files[]", new File(path), handler, oldMessageKey);
            }
            return multipart.getResponse();
//            return new URLServiceProvider(context).getMultipartFile(path, handler).getResponse();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //ApplozicInternal: private
    /**
     * @ApplozicInternal Gets the upload URL based on the URL provider.
     */
    @ApplozicInternal
    public String getUploadURL() {
        String fileUrl = new URLServiceProvider(context).getFileUploadUrl();
        return fileUrl;
    }

    //ApplozicInternal: default
    /**
     * Downloads the display image for a {@link Contact} or {@link Channel} and returns the bitmap.
     *
     * <p>To get the image URL, {@link Contact#getImageURL()} and {@link Channel#getImageUrl()} is used.</p>
     *
     * <p>Note: Only one of the two parameters is used (either the contact or the channel). If the contact is <i>non-null</i> it is used, otherwise channel.</p>
     *
     * @param contact the contact object to download the image for, can be null
     * @param channel the channel object to download the image for, can be null
     * @return the image bitmap, null in-case of failure
     */
    public Bitmap downloadBitmap(Contact contact, Channel channel) {
        HttpURLConnection connection = null;
        MarkStream inputStream = null;
        try {
            if (contact != null) {
                connection = openHttpConnection(contact.getImageURL());
            } else {
                connection = openHttpConnection(channel.getImageUrl());
            }
            if (connection != null) {
                if (connection.getResponseCode() == 200) {
                    inputStream = new MarkStream(connection.getInputStream());
                    BitmapFactory.Options optionsBitmap = new BitmapFactory.Options();
                    optionsBitmap.inJustDecodeBounds = true;
                    inputStream.allowMarksToExpire(false);
                    long mark = inputStream.setPos(MARK);
                    BitmapFactory.decodeStream(inputStream, null, optionsBitmap);
                    inputStream.resetPos(mark);
                    optionsBitmap.inJustDecodeBounds = false;
                    optionsBitmap.inSampleSize = ImageUtils.calculateInSampleSize(optionsBitmap, 100, 50);
                    Bitmap attachedImage = BitmapFactory.decodeStream(inputStream, null, optionsBitmap);
                    inputStream.allowMarksToExpire(true);
                    return attachedImage;
                } else {
                    Utils.printLog(context, TAG, "Download is failed response code is ...." + connection.getResponseCode());
                }
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            Utils.printLog(context, TAG, "Image not found on server: " + ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            Utils.printLog(context, TAG, "Exception fetching file from server: " + ex.getMessage());
        } catch (Throwable t) {

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;

    }

    //ApplozicInternal: private
    /**
     * @ApplozicInternal Returns a path with a ".Thumbnail" directory added to the passed filepath.
     */
    @ApplozicInternal
    public String getThumbnailParentDir(String filePath) {
        String[] parts = getParts(filePath);
        String thumbnailDir = "";

        for (int i = 0; i < parts.length - 1; i++) {
            thumbnailDir += (parts[i] + "/");
        }
        thumbnailDir = thumbnailDir + ".Thumbnail/";
        return thumbnailDir;
    }

    /**
     * @ApplozicInternal Generates and returns a thumbnail path for the <i>video</i> with the given <code>filepath</code>.
     *
     * This path is supposed to be the same as the path where video thumbnails are downloaded.
     * see {@link FileClientService#downloadAndSaveThumbnailImage(Context, Message, int, int)}
     */
    @ApplozicInternal
    public String getThumbnailPath(String filePath) { //ApplozicInternal: default
        String thumbnailParentDir = getThumbnailParentDir(filePath);
        File dir = new File(thumbnailParentDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return thumbnailParentDir + getVideoThumbnailFileNameForLocalGeneration(filePath);
    }

    //ApplozicInternal: default
    /**
     * @ApplozicInternal Generates and saves a thumbnail image for the video with the passed <code>filepath</code>. Also returns the thumbnail bitmap.
     */
    @ApplozicInternal
    public Bitmap createThumbnailFileInLocalStorageAndReturnBitmap(String filePath) {
        Bitmap videoThumbnail;
        OutputStream fOut;
        File file = new File(getThumbnailParentDir(filePath), getVideoThumbnailFileNameForLocalGeneration(filePath));
        try {
            file.createNewFile();
            fOut = new FileOutputStream(file);
            videoThumbnail = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
            videoThumbnail.compress(Bitmap.CompressFormat.JPEG, 50, fOut);
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return videoThumbnail;
    }

    /**
     * @ApplozicInternal Returns the thumbnail for the video with the passed <code>filepath</code>. Generates and returns one if it does not exist.
     *
     * This methods aims to save the video thumbnail in the same location where it will save downloaded thumbnails from the server.
     * see {@link FileClientService#downloadAndSaveThumbnailImage(Context, Message, int, int)}
     */
    @ApplozicInternal
    public Bitmap getOrCreateVideoThumbnail(String filePath) {
        String videoThumbnailPath = getThumbnailPath(filePath);

        Bitmap videoThumbnail;
        if (new File(videoThumbnailPath).exists()) {
            videoThumbnail = BitmapFactory.decodeFile(videoThumbnailPath);
        } else {
            videoThumbnail = createThumbnailFileInLocalStorageAndReturnBitmap(filePath);
        }

        return videoThumbnail;
    }

    /**
     * @ApplozicInternal Uploads the logged in user's profile/display image to the servers.
     */
    @ApplozicInternal
    public String uploadProfileImage(String path) throws UnsupportedEncodingException {
        try {
            ApplozicMultipartUtility multipart = new ApplozicMultipartUtility(profileImageUploadURL(), "UTF-8", context);
            multipart.addFilePart("file", new File(path), null, null);
            return multipart.getResponse();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @deprecated This method has been deprecated and will be removed soon. Conversations are being deprecated.
     */
    @Deprecated
    @ApplozicInternal
    public Bitmap loadMessageImage(Context context, Conversation conversation) {
        try {
            if (conversation == null) {
                return null;
            }
            Bitmap attachedImage = ImageUtils.getBitMapFromLocalPath(conversation.getTopicLocalImageUri());
            if (attachedImage != null) {
                return attachedImage;
            }
            Bitmap bitmap = downloadProductImage(conversation);
            if (bitmap != null) {
                File file = FileClientService.getFilePath("topic_" + conversation.getId(), context.getApplicationContext(), "image", true);
                String imageLocalPath = ImageUtils.saveImageToInternalStorage(file, bitmap);
                conversation.setTopicLocalImageUri(imageLocalPath);
            }
            if (!TextUtils.isEmpty(conversation.getTopicLocalImageUri())) {
                ConversationService.getInstance(context).updateTopicLocalImageUri(conversation.getTopicLocalImageUri(), conversation.getId());
            }
            return bitmap;

        } catch (Exception e) {

        }
        return null;
    }

    //ApplozicInternal: private
    /**
     * @deprecated This method has been deprecated and will be removed soon. Conversations are being deprecated.
     */
    @Deprecated
    @ApplozicInternal
    public Bitmap downloadProductImage(Conversation conversation) {
        TopicDetail topicDetail = (TopicDetail) GsonUtils.getObjectFromJson(conversation.getTopicDetail(), TopicDetail.class);
        if (TextUtils.isEmpty(topicDetail.getLink())) {
            return null;
        }
        HttpURLConnection connection = null;
        MarkStream inputStream = null;
        try {
            if (conversation != null) {
                connection = openHttpConnection(topicDetail.getLink());
            }
            if (connection != null) {
                if (connection.getResponseCode() == 200) {
                    inputStream = new MarkStream(connection.getInputStream());
                    BitmapFactory.Options optionsBitmap = new BitmapFactory.Options();
                    optionsBitmap.inJustDecodeBounds = true;
                    inputStream.allowMarksToExpire(false);
                    long mark = inputStream.setPos(MARK);
                    BitmapFactory.decodeStream(inputStream, null, optionsBitmap);
                    inputStream.resetPos(mark);
                    optionsBitmap.inJustDecodeBounds = false;
                    optionsBitmap.inSampleSize = ImageUtils.calculateInSampleSize(optionsBitmap, 100, 50);
                    Bitmap attachedImage = BitmapFactory.decodeStream(inputStream, null, optionsBitmap);
                    inputStream.allowMarksToExpire(true);
                    return attachedImage;
                } else {
                    return null;
                }
            }
        } catch (FileNotFoundException ex) {
            Utils.printLog(context, TAG, "Image not found on server: " + ex.getMessage());
        } catch (Exception ex) {
            Utils.printLog(context, TAG, "Exception fetching file from server: " + ex.getMessage());
        } catch (Throwable t) {

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * Writes file from given URI to the {@link File} object.
     */
    @ApplozicInternal
    public void writeFile(Uri uri, File file) {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = context.getContentResolver().openInputStream(uri);
            byte[] buffer = new byte[1024];
            int bytesRead = -1;
            out = new FileOutputStream(file);
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (in != null && out != null) {
                try {
                    out.flush();
                    out.close();
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    /**
     * Infers the file format from the mime type, if possible.
     *
     * @param mimeType the mime type
     * @return the file extension
     */
    public @Nullable String getFileFormatFromMimeType(@NonNull String mimeType) {
        String[] array = mimeType.split("/");
        String fileFormat = null;

        if (array.length > 1) {
            fileFormat = array[1];
        }

        if (TextUtils.isEmpty(fileFormat)) {
            return null;
        }

        return fileFormat;
    }

    /**
     * @ApplozicInternal Saves the file to the Applozic apps media/file folder.
     *
     * <p>Name of the file will be set using {@link DateUtils#getDateStringForLocalFileName()}.
     * and the path from {@link FileClientService#getFilePath(String, Context, String)}.<p/>
     *
     * @param fromUri the uri of the file to save
     * @param mimeType mime type of the file to save
     * @return the saved file object
     */
    @ApplozicInternal
    public @Nullable File saveFileToApplozicLocalStorage(@NonNull Uri fromUri, @Nullable String mimeType) {
        if (context != null && !TextUtils.isEmpty(mimeType)) {
            String fileFormat = getFileFormatFromMimeType(mimeType);

            if (TextUtils.isEmpty(fileFormat)) {
                return null;
            }

            String fileNameToWrite = DateUtils.getDateStringForLocalFileName() + "." + fileFormat;

            File mediaFile = FileClientService.getFilePath(fileNameToWrite, context, mimeType);

            writeFile(fromUri, mediaFile);

            return mediaFile;
        }
        return null;
    }
}
