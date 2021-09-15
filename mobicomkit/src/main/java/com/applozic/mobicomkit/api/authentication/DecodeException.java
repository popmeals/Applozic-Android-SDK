package com.applozic.mobicomkit.api.authentication;

/**
 * @ApplozicInternal Exception that might arise while decoding JWT token.
 */
@SuppressWarnings("WeakerAccess")
public class DecodeException extends RuntimeException {

    DecodeException(String message) {
        super(message);
    }

    DecodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
