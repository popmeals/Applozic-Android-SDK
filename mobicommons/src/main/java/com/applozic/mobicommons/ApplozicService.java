package com.applozic.mobicommons;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;

public class ApplozicService {
    private static Context application;

    public static @Nullable Context getAppContext() {
        return application;
    }

    public static void initApp(@NonNull Application application) {
        ApplozicService.application = application;
    }

    /**
     * Gets the <i>application context</i> from the given context object.
     *
     * @param context passing null will default to {@link #getAppContext()}
     */
    public static @NonNull Context getContext(@NonNull Context context) { //return value can be null if context passed is null
        if (application == null && context != null) {
            application = context instanceof Application ? context : context.getApplicationContext();
        }
        return application;
    }

    public static @Nullable Context getContextFromWeak(@Nullable WeakReference<Context> contextWeakReference) {
        if (application == null && contextWeakReference != null) {
            application = contextWeakReference.get() instanceof Application ? contextWeakReference.get() : contextWeakReference.get().getApplicationContext();
        }
        return application;
    }

    /**
     * Stores the application context. Can be accessed later using {@link #getAppContext()}.
     *
     * @param context passing null will do nothing
     */
    public static void initWithContext(@Nullable Context context) {
        if (context != null && application == null) {
            if (context instanceof Application) {
                ApplozicService.application = context;
            } else {
                ApplozicService.application = context.getApplicationContext();
            }
        }
    }
}
