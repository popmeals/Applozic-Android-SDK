package com.applozic.mobicomkit.listners;

import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.exception.ApplozicException;

/**
 * Has callbacks for message attachment media upload.
 */
public interface MediaUploadProgressHandler {
    /**
     * @param oldMessageKey the key-string of the message before attachment upload
     */
    void onUploadStarted(ApplozicException e, String oldMessageKey);

    /**
     * @param oldMessageKey the key-string of the message before attachment upload
     */
    void onProgressUpdate(int percentage, ApplozicException e, String oldMessageKey);

    /**
     * @param oldMessageKey the key-string of the message before attachment upload
     */
    void onCancelled(ApplozicException e, String oldMessageKey);

    /**
     * @param oldMessageKey the key-string of the message before attachment upload
     */
    void onCompleted(ApplozicException e, String oldMessageKey);

    /**
     * {@link Message#getFileMetas()} contains remote URls to the uploaded attachment.
     *
     * @param oldMessageKey the key-string of the message before attachment upload
     */
    void onSent(Message message, String oldMessageKey);
}
