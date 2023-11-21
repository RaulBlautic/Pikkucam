package com.blautic.pikkucam.api;

import com.blautic.pikkucam.api.response.LoginResponse;
import com.blautic.pikkucam.api.response.Model;
import com.blautic.pikkucam.api.response.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface PlataformaApi {

    @FormUrlEncoded
    @POST("/api/login/access-token")
    Call<LoginResponse> loginAccessToken(@Field("username") String username, @Field("password") String password);

    @GET("/api/v1/users/me")
    Call<User> getUserMe();

}
