package xyz.lebalex.weatherirk;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;

public class HttpRunningTask implements Callable<String> {
    private final String input_url;
    private final Context context;

    public HttpRunningTask(Context context, String input_url) {
        this.input_url = input_url;
        this.context = context;
    }

    @Override
    public String call() {
        // Some long running task
        HttpURLConnection urlConnection = null;
        String result = "";
        try {
            URL url = new URL(this.input_url);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(35000);
            urlConnection.setReadTimeout(35000);
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(false);
            urlConnection.connect();
            int code = urlConnection.getResponseCode();
            if (code == 200) {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                if (in != null) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                    java.lang.String line = "";
                    while ((line = bufferedReader.readLine()) != null)
                        result += line;
                }
                in.close();
            }
        } catch (Exception e) {
            //LogWrite.Logg(context,  "http: "+e.getMessage());
        } finally {
            urlConnection.disconnect();
            //LogWrite.Logg(context, "finally: "+result);
            return result;
        }

    }
}
