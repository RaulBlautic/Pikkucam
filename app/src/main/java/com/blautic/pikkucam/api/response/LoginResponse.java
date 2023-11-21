package com.blautic.pikkucam.api.response;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("access_token")
    public String accessToken;
    @SerializedName("token_type")
    public String tokenType;
    @SerializedName("user")
    public int id;
    @SerializedName("rol")
    public int rol;
}
