package com.applozic.mobicomkit.api.authentication;

import com.applozic.mobicomkit.annotations.ApplozicInternal;

/**
 * @ApplozicInternal Exception that might arise while decoding JWT token.
 */
@SuppressWarnings("WeakerAccess")
@ApplozicInternal
public class DecodeException extends RuntimeException {

    DecodeException(String message) {
        super(message);
    }

    DecodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
