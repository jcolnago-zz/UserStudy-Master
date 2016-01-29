package com.example.jessica.masterproject.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.provider.Settings;

import com.example.jessica.masterproject.MainActivity;
import com.example.jessica.masterproject.R;
import com.loopj.android.http.*;
import java.io.File;
import java.io.FileNotFoundException;

import cz.msebera.android.httpclient.Header;

public class FileUploader {

    public static void upload(final String filename, final String fileType, final Context context){
        String url = "http://jessicacolnago.com/upload";
        RequestParams params = new RequestParams();
        String id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        File file = new File(String.valueOf(Environment.getExternalStorageDirectory())+"/annoyme/"+filename+fileType);

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
                SharedPreferences mSharedPref = context.getSharedPreferences(String.valueOf(R.string.preference_file), Context.MODE_PRIVATE);
                SharedPreferences.Editor mEditor = mSharedPref.edit();
                mEditor.putBoolean(context.getString(R.string.upload_pending) + filename, false);
                mEditor.putBoolean(context.getString(R.string.upload_done) + filename, true);
                mEditor.commit();

                System.out.println("Successfully uploaded " + filename
                        + ", new status (true=error in saving status): "
                        + mSharedPref.getBoolean(context.getString(R.string.upload_pending)+filename, true));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] bytes, Throwable throwable) {
                //TODO: change this, this is horrible. can't keep trying forever
                upload(filename, fileType, context);
                System.err.println("Failure: " + statusCode);
            }
        });
    }

}
