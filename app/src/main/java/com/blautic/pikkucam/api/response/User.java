package com.blautic.pikkucam.api.response;

import com.google.gson.annotations.SerializedName;

public class User {
    public long id;
    public String email;
    @SerializedName("is_active")
    public boolean isActive;
    @SerializedName("full_name")
    public String fullName;

}
