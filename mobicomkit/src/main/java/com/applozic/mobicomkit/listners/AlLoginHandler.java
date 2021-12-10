package com.applozic.mobicomkit.listners;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.applozic.mobicomkit.api.account.register.RegistrationResponse;

/**
 * Callbacks for login/registration.
 *
 * {@link RegistrationResponse#isRegistrationSuccess()} will be true in case of a successful login/register. Otherwise {@link RegistrationResponse#getMessage()} will have the error message.
 */
public interface AlLoginHandler {
    void onSuccess(@NonNull RegistrationResponse registrationResponse, @Nullable Context context);
    void onFailure(@Nullable RegistrationResponse registrationResponse, @Nullable Exception exception);
}
