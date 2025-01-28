package xyz.lebalex.weatherirk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;


import androidx.preference.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

/**
 * Created by ivc_lebedevav on 27.01.2017.
 */
public class SplashActivity extends Activity {

    private SharedPreferences sp;
    private String url = "https://lebalex.ru/meteo.json";

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
            HttpsURLConnection dc =  (HttpsURLConnection)jsonUrl.openConnection();
            dc.setHostnameVerifier(hostnameVerifier);
            dc.setConnectTimeout(5 * 1000);
            dc.setReadTimeout(5 * 1000);
            BufferedReader inputStream = new BufferedReader(new InputStreamReader(
                    dc.getInputStream()));
            resultJson = inputStream.readLine();
            try {
                JSONObject jsonObject = new JSONObject(resultJson);
                String mu = jsonObject.getString("meteo_url");
                if(mu!=null) {
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("meteo_url", mu);
                    editor.apply();
                }else
                {
                    LogWrite.LogError(getApplicationContext(), resultJson);
                }


            } catch (JSONException e) {
                LogWrite.LogError(getApplicationContext(), e.getMessage());
                Log.e("SPLASH",e.getMessage());
            }
        } catch (Exception e4) {
            LogWrite.LogError(getApplicationContext(), e4.getMessage());
            Log.e("SPLASH",e4.getMessage());
        }
    }

    HostnameVerifier hostnameVerifier = new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            HostnameVerifier hv =
                    HttpsURLConnection.getDefaultHostnameVerifier();
            return hv.verify("lebalex.ru", session);
        }
    };
}
