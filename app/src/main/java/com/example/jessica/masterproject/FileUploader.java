package com.example.jessica.masterproject;


import android.content.Context;
import android.provider.Settings;

import com.loopj.android.http.*;
import java.io.File;
import java.io.FileNotFoundException;

import cz.msebera.android.httpclient.Header;

public class FileUploader {

    public static void upload(File file, Context context){
        String url = "http://jessicacolnago.com/upload";
        RequestParams params = new RequestParams();
        String id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        try {
            params.put("file", file);
            params.put("folder", id);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // send request
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                System.out.println("Success" + statusCode);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] bytes, Throwable throwable) {
                System.out.println("Failure: " + statusCode);
            }
        });
    }

}
