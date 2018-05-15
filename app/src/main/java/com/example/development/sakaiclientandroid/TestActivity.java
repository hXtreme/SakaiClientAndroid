package com.example.development.sakaiclientandroid;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.development.sakaiclientandroid.api_models.all_sites.AllSitesAPI;
import com.example.development.sakaiclientandroid.api_models.all_sites.SiteCollectionObject;
import com.example.development.sakaiclientandroid.services.SakaiService;
import com.example.development.sakaiclientandroid.utils.HeaderInterceptor;
import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TestActivity extends AppCompatActivity {

    private String baseUrl;
    private String cookieUrl;

    OkHttpClient httpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        // Get the base url for the Sakai API
        baseUrl = getString(R.string.BASE_URL);
        // Get the url which has the relevant cookies for Sakai
        cookieUrl = getString(R.string.COOKIE_URL_1);

        // Create the custom OkHttpClient with the interceptor to inject
        // cookies into every request
        HeaderInterceptor interceptor = new HeaderInterceptor(this, cookieUrl);
        httpClient = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();

        // The Retrofit instance allows us to construct our own services
        // that will make network requests
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient)
                .build();


        SakaiService sakaiService = retrofit.create(SakaiService.class);
        Call<ResponseBody> fetchSitesCall = sakaiService.getResponseBody();

        fetchSitesCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.i("Response", "SUCCESS!");
                Log.i("Status Code", "" + response.code());

                try {

                    Gson gson = new Gson();
                    AllSitesAPI api = gson.fromJson(response.body().string(), AllSitesAPI.class);
                    Log.d("piano", api.getSiteCollectionObject().get(5).getSitePageObjects().size() + "");

                }
                catch(Exception e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // TODO: Handle errors give proper error message
                Log.i("Response", "failure");
                Log.e("Response error", t.getMessage());

            }
        });


    }
}