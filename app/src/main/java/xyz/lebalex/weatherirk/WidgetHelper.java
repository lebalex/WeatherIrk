package xyz.lebalex.weatherirk;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.opengl.Visibility;
import android.os.AsyncTask;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class WidgetHelper {
    private Context context;
    private AppWidgetManager appWidgetManager;
    private int widgetID;

    class GetWaether extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            String result = "";
            try {
                LogWrite.Log(context, params[0]);
                URL url = new URL(params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                int code = urlConnection.getResponseCode();
                if(code==200){
                    LogWrite.Log(context, "ResponseCode:"+code);
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
                LogWrite.Log(context,  "http1: "+e.getMessage());
                StartServices.startBackgroundService(context,"10");
            } catch (IOException e) {
                LogWrite.Log(context,  "http1: "+e.getMessage());
                StartServices.startBackgroundService(context, "10");
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
                    LogWrite.Log(context, "result:"+result);
                    JSONArray jsonArray = new JSONArray(result);
                    SharedPreferences sp = getDefaultSharedPreferences(context);
                    JSONObject json_w = jsonArray.getJSONObject(Integer.parseInt(sp.getString("place_temp", "0")));
                    if(json_w.getString("temp").contains("NaN")) {
                        int i=0;
                        json_w = null;
                        while (i<jsonArray.length() && json_w == null)
                        {
                            JSONObject json = jsonArray.getJSONObject(i);
                            String val = json.getString("temp");
                            if(!val.contains("NaN")) {
                                json_w = jsonArray.getJSONObject(i);
                            }
                            i++;
                        }
                    }
                    LogWrite.Log(context,  json_w.toString());
                    updateWidget(context, appWidgetManager,widgetID, json_w);

                }catch(Exception e){
                    LogWrite.LogError(context, e.getMessage());
                }

            }
        }
    }
    public void updateWidget(Context ctx, AppWidgetManager appWidgetManager,
                              int widgetID, JSONObject json)
    {
        try {
            LogWrite.Log(ctx, "updateWidget");
            RemoteViews widgetView = new RemoteViews(ctx.getPackageName(),
                    R.layout.widget);

            widgetView.setViewVisibility(R.id.indeterminateBar, View.GONE);
            widgetView.setViewVisibility(R.id.updateBar, View.VISIBLE);

            String where=json.getString("where");
            if(where.length()>8)
                widgetView.setTextViewTextSize(R.id.where, TypedValue.COMPLEX_UNIT_DIP, 14);
            else
                widgetView.setTextViewTextSize(R.id.where, TypedValue.COMPLEX_UNIT_DIP, 16);
            widgetView.setTextViewText(R.id.where, where);
            widgetView.setTextViewText(R.id.temp, json.getString("temp"));


            Calendar calen = Calendar.getInstance();

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



            /*Intent intent1 = new Intent(context, MyWidgetProvider.class);
            intent1.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent1.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            PendingIntent pendingIntent1 = PendingIntent.getBroadcast(context,
                    0, intent1, 0);
            */
            int ids[] = appWidgetManager.getAppWidgetIds(new ComponentName(ctx, WeatherWidget.class));
            Intent updateIntent = new Intent(ctx, WeatherWidget.class);
            updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            PendingIntent pIntentUpdate = PendingIntent.getBroadcast(ctx, 0,
                    updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);


            widgetView.setOnClickPendingIntent(R.id.updateBar, pIntentUpdate);


            appWidgetManager.updateAppWidget(widgetID, widgetView);

            LogWrite.Log(ctx, "updateWidget complite");
        }catch(Exception e)
        {
            LogWrite.LogError(ctx, e.getMessage());
        }
    }
    public void updateWidget(Context ctx, AppWidgetManager appWidgetManager,
                                    int widgetID) {
        this.context = ctx;
        this.appWidgetManager = appWidgetManager;
        this.widgetID = widgetID;
        new GetWaether().execute(new String[]{"http://lebalex.xyz/lebalexServices/pogoda/meteo.php"});


    }
    }
