package com.blautic.pikkucam.api;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final SessionManager sessionManager;

    public AuthInterceptor(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public @NotNull Response intercept(@NotNull Chain chain) throws IOException {
        Request.Builder requestBuilder = chain.request().newBuilder();
        if (sessionManager.getAuthToken() != null) {
            requestBuilder.addHeader("Authorization", "Bearer " + sessionManager.getAuthToken());
        }
        return chain.proceed(requestBuilder.build());
    }
}
