package xyz.lebalex.weatherirk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;


import androidx.preference.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by ivc_lebedevav on 27.01.2017.
 */
public class SplashActivity extends Activity {

    private static int SPLASH_TIME_OUT = 2000;
    private SharedPreferences sp;
    private String url = "https://www.lebalex.ru/meteo.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        getJsonFromUrl(url);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();

        /*new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                Intent mainIntent = new Intent(SplashActivity.this,MainActivity.class);
                SplashActivity.this.startActivity(mainIntent);
                SplashActivity.this.finish();
            }
        }, SPLASH_TIME_OUT);
        */

    }

    private void getJsonFromUrl(String urls) {

        String resultJson = null;

        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            URL jsonUrl = new URL(urls);
            URLConnection dc = jsonUrl.openConnection();
            dc.setConnectTimeout(10 * 1000);
            dc.setReadTimeout(10 * 1000);
            BufferedReader inputStream = new BufferedReader(new InputStreamReader(
                    dc.getInputStream()));
            resultJson = inputStream.readLine();
            try {
                JSONObject jsonObject = new JSONObject(resultJson);
                if(jsonObject.getString("meteo_url")!=null) {
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("meteo_url", jsonObject.getString("meteo_url"));
                    editor.apply();
                }


            } catch (JSONException e) {

            }
        } catch (Exception e4) {

        }
    }

}