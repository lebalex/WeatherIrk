package xyz.lebalex.weatherirk;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class UpdatesReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent)
    {
        try {
            StartServices.startBackgroundService(context);
            if (intent != null)
                doWork(context, intent.getAction());
        }catch(Exception e)
        {
            LogWrite.Log(context, e.getMessage());
        }
    }

    private void doWork(Context ctx, String action)
    {
        try {
            LogWrite.Log(ctx, action);
            Intent intent = new Intent(ctx, WeatherWidget.class);
            intent.setAction("ACTION_AUTO_UPDATE_WIDGET");
            int ids[] = AppWidgetManager.getInstance(ctx).getAppWidgetIds(new ComponentName(ctx, WeatherWidget.class));
            if(ids.length>0) {
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
                ctx.sendBroadcast(intent);
            }
        }catch(Exception e)
        {
            LogWrite.Log(ctx, e.getMessage());
        }
    }


}
