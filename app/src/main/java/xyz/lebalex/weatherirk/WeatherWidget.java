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
        LogWrite.Log(context, "onEnabled");
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        LogWrite.Log(context, "-----------------------");
        LogWrite.Log(context, "onReceive="+intent.getAction());
        super.onReceive(context, intent);

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        RemoteViews widgetView = new RemoteViews(context.getPackageName(),
                R.layout.widget);

        widgetView.setViewVisibility(R.id.updateBar, View.GONE);
        widgetView.setViewVisibility(R.id.indeterminateBar, View.VISIBLE);


        for (int i : appWidgetIds) {
            appWidgetManager.updateAppWidget(i, widgetView);
            new WidgetHelper().updateWidget(context, appWidgetManager, i);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Intent intent0 = new Intent(context, UpdatesReceiver.class);
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.cancel(PendingIntent.getBroadcast(context, 121236, intent0, PendingIntent.FLAG_CANCEL_CURRENT));


    }




}
