package com.applozic.mobicomkit.listners;

import android.content.Context;

import com.applozic.mobicomkit.api.account.register.RegistrationResponse;

/**
 * Callbacks for login/registration.
 *
 * {@link RegistrationResponse#isRegistrationSuccess()} will be true in case of a successful login/register. Otherwise {@link RegistrationResponse#getMessage()} will have the error message.
 */
public interface AlLoginHandler {
    void onSuccess(RegistrationResponse registrationResponse, Context context);

    void onFailure(RegistrationResponse registrationResponse, Exception exception);
}
