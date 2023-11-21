package com.blautic.pikkucam.di;

import android.content.Context;

import com.blautic.pikkucam.api.AuthInterceptor;
import com.blautic.pikkucam.api.PikkucamApi;
import com.blautic.pikkucam.api.PikkucamRepository;
import com.blautic.pikkucam.api.PlataformaApi;
import com.blautic.pikkucam.api.PlataformaRepository;
import com.blautic.pikkucam.api.SessionManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javax.inject.Singleton;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@InstallIn(SingletonComponent.class)
@Module
public class ApiModule {


    @Singleton
    @Provides
    PlataformaRepository providerPlatRepository(PlataformaApi plataformaApi){
        return new PlataformaRepository(plataformaApi);
    }


    @Singleton
    @Provides
    PikkucamRepository providerRepository(PikkucamApi pikkucamApi){
        return new PikkucamRepository(pikkucamApi);
    }



    @Singleton
    @Provides
    PlataformaApi providePlataformaApi(OkHttpClient client) {

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create();

        return new Retrofit.Builder()
                .baseUrl("http://sinequanon-smartdispensing.com:8083")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(PlataformaApi.class);
    }


    @Singleton
    @Provides
    PikkucamApi providePikkuApi(OkHttpClient client) {

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create();

        return new Retrofit.Builder()
                .baseUrl("http://sinequanon-smartdispensing.com:8081")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(PikkucamApi.class);
    }

    @Singleton
    @Provides
    OkHttpClient provideHttpClient(SessionManager sessionManager) {
        HttpLoggingInterceptor logger = new HttpLoggingInterceptor(HttpLoggingInterceptor.Logger.DEFAULT);
        logger.setLevel(HttpLoggingInterceptor.Level.BODY);
        return new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(sessionManager))
                .addInterceptor(logger)
                .build();
    }

    @Singleton
    @Provides
    SessionManager provideSessionManager(@ApplicationContext Context context) {
        return new SessionManager(context);
    }

}
