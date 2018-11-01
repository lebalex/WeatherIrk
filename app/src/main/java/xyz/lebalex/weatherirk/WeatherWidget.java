package xyz.lebalex.weatherirk;

/**
 * Created by ivc_lebedevav on 30.01.2017.
 */

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.StrictMode;
import android.provider.ContactsContract;

import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import org.json.JSONArray;
import org.json.JSONObject;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class WeatherWidget extends AppWidgetProvider {

    final static String LOG_TAG = "myLogs";
    static Object objReceived = null;
    private static Calendar calen;
    private static String howUpdate="start";
    public static String ACTION_AUTO_UPDATE_WIDGET = "ACTION_AUTO_UPDATE_WIDGET";
    public static String ACTION_USER_PRESENT = "android.intent.action.USER_PRESENT";
    public static String ACTION_APPWIDGET_ENABLED = "android.appwidget.action.APPWIDGET_ENABLED";
    public static String APPWIDGET_UPDATE = "android.appwidget.action.APPWIDGET_UPDATE";
    private Context context;
    private AppWidgetManager appWidgetManager;
    private int[] appWidgetIds;
    private SharedPreferences sp;

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        LogWrite.Log(context, "start after reboot");
            StartServices.startBackgroundService(context);

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        calen = Calendar.getInstance();
        howUpdate="hand";
        try {
            objReceived = ((ObjectWrapperForBinder) intent.getExtras().getBinder("object_value")).getData();
        } catch (Exception e) {
            objReceived = null;
        }

        super.onReceive(context, intent);
        LogWrite.Log(context, "-----------------------");
        LogWrite.Log(context, "onReceive="+intent.getAction());
        sp = getDefaultSharedPreferences(context);
        try {
        if (ACTION_AUTO_UPDATE_WIDGET.equalsIgnoreCase(intent.getAction()) || ACTION_APPWIDGET_ENABLED.equalsIgnoreCase(intent.getAction())
                || APPWIDGET_UPDATE.equalsIgnoreCase(intent.getAction())
                || (ACTION_USER_PRESENT.equalsIgnoreCase(intent.getAction()) && !sp.getBoolean("last_time", false))
                ) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("last_time", false);
            editor.commit();

            howUpdate=intent.getAction();
            LogWrite.Log(context,"");
            appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName thisAppWidget = new ComponentName(context.getPackageName(), WeatherWidget.class.getName());
            appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
            if(objReceived == null){
                LogWrite.Log(context,"objReceived == null");
                int startH = Integer.parseInt(sp.getString("update_start", "0"));
                if(ACTION_APPWIDGET_ENABLED.equalsIgnoreCase(intent.getAction()) || APPWIDGET_UPDATE.equalsIgnoreCase(intent.getAction()))
                    startH=0;
                if (calen.get(Calendar.HOUR_OF_DAY) >=startH)
                {
                    LogWrite.Log(context,"do");
                    /*PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WeatherWidget");*/
                    try {
                        /*wl.acquire();*/
                        /*ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                        if (cm == null) {
                            LogWrite.LogError(context, "Отсутствует соединение");
                        }else {
                            final NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                            boolean isNetworkConnectedOrConnecting = networkInfo != null && networkInfo.isConnectedOrConnecting();
                            LogWrite.Log(context, "isNetworkConnectedOrConnecting = " + isNetworkConnectedOrConnecting);
                            if (networkInfo != null && isNetworkConnectedOrConnecting) {
                                LogWrite.Log(context, networkInfo.getTypeName());
                            } else
                                LogWrite.Log(context, "Not Active Network");
                        }*/


                        new GetWaether().execute(new String[]{"http://lebalex.xyz/lebalexServices/pogoda/meteo.php"});
                    }catch(Exception ep)
                    {
                        LogWrite.LogError(context, ep.getMessage());
                    }/*finally {
                        wl.release();
                    }*/
                }else LogWrite.Log(context, "not time");
            } else {
                LogWrite.Log(context, "objReceived != null");
                onUpdate(context, appWidgetManager, appWidgetIds);
            }
        }
        }catch (Exception e) {
            LogWrite.LogError(context, e.getMessage());
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        for (int i : appWidgetIds) {
            updateWidget(context, appWidgetManager, i);
        }
    }

    static void updateWidget(Context ctx, AppWidgetManager appWidgetManager,
                             int widgetID) {


        try {
            JSONObject e = null;
            if (objReceived instanceof JSONObject) e = (JSONObject) objReceived;

            if(e!=null) {
                LogWrite.Log(ctx, "update "+howUpdate);
                RemoteViews widgetView = new RemoteViews(ctx.getPackageName(),
                        R.layout.widget);


                widgetView.setTextViewText(R.id.where, e.getString("where"));
                widgetView.setTextViewText(R.id.temp, e.getString("temp"));

                String minute = "" + calen.get(Calendar.MINUTE);
                if (calen.get(Calendar.MINUTE) < 10) minute = "0" + calen.get(Calendar.MINUTE);

                widgetView.setTextViewText(R.id.time, calen.get(Calendar.HOUR_OF_DAY) + ":" + minute);


                Intent configIntent = new Intent(ctx, SplashActivity.class);
                configIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
                configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
                PendingIntent pIntent = PendingIntent.getActivity(ctx, widgetID,
                        configIntent, 0);

                widgetView.setOnClickPendingIntent(R.id.where, pIntent);
                widgetView.setOnClickPendingIntent(R.id.temp, pIntent);

                appWidgetManager.updateAppWidget(widgetID, widgetView);
                LogWrite.Log(ctx, "update complite "+howUpdate);
                SharedPreferences sp = getDefaultSharedPreferences(ctx);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("last_time", true);
                editor.commit();
                LogWrite.Log(ctx, "set true");

            }else
            {
                LogWrite.LogError(ctx, "json is null");
            }
        } catch (Exception e1) {
            LogWrite.LogError(ctx, e1.getMessage());
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Intent alarmIntent = new Intent(context, UpdatesReceiver.class);
        alarmIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        alarmIntent.setAction("ACTION_AUTO_UPDATE_WIDGET");
        PendingIntent pendingIntent;
        pendingIntent = PendingIntent.getBroadcast(context, 121236, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.cancel(pendingIntent);

    }

    class GetWaether extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            String result = "";
            try {
                URL url = new URL(params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                int code = urlConnection.getResponseCode();
                if(code==200){
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    if (in != null) {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                        String line = "";
                        while ((line = bufferedReader.readLine()) != null)
                            result += line;
                    }
                    in.close();
                }
                return result;
            } catch (MalformedURLException e) {
                //e.printStackTrace();
            } catch (IOException e) {
                //e.printStackTrace();
            }
            finally {
                urlConnection.disconnect();
            }
            return result;

        }

        @Override
        protected void onPostExecute(String result) {
            if(result!="") {
                JSONObject dataJsonObj = null;
                try {
                    JSONArray jsonArray = new JSONArray(result);

                    objReceived=jsonArray.getJSONObject(Integer.parseInt(sp.getString("place_temp", "0")));
                    onUpdate(context, appWidgetManager, appWidgetIds);

                }catch(Exception e){
                    LogWrite.LogError(context, e.getMessage());
                }

            }
        }
    }


}
