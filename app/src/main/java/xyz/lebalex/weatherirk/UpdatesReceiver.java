package xyz.lebalex.weatherirk;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Calendar;


public class UpdatesReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent)
    {
        try {
            SharedPreferences sp = context.getSharedPreferences(MainActivity.APP_PREFERENCES, Context.MODE_PRIVATE);

            StartServices.startBackgroundService(context, sp.getString("update_frequency", "60"));
            if (intent != null) {
                Calendar calen = Calendar.getInstance();
                if (calen.get(Calendar.HOUR_OF_DAY) >= Integer.parseInt(sp.getString("update_start", "0")))
                    doWork(context, intent.getAction());

            }
        }catch(Exception e)
        {
            LogWrite.Log(context, e.getMessage());
        }
    }

    private void doWork(Context ctx, String action)
    {
        try {
            LogWrite.Log(ctx, "doWork:"+action);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(ctx);
            int ids[] = appWidgetManager.getAppWidgetIds(new ComponentName(ctx, WeatherWidget.class));
            if (ids.length > 0) {
                for (int id : ids) {
                    new WidgetHelper().updateWidget(ctx, appWidgetManager, id);
                }
            }
        }catch(Exception e)
        {
            LogWrite.Log(ctx, "doWork:"+e.getMessage());
        }
    }


}
