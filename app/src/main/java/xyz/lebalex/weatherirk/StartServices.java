package xyz.lebalex.weatherirk;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Calendar;


public class StartServices {
    private static boolean debug=false;
    private static SharedPreferences sp;

    /*public static void startBackgroundService(Context ctx) {
        sp = getSharedPreferences(MainActivity.APP_PREFERENCES, Context.MODE_PRIVATE);
        startBackgroundService(ctx, getDefaultSharedPreferences(ctx).getString("update_frequency", "60"));
    }*/

    public static void startBackgroundService(Context ctx, String intervalStr) {
        try {

            int interval = Integer.parseInt(intervalStr);

            Intent alarmIntent = new Intent(ctx, UpdatesReceiver.class);
            alarmIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            alarmIntent.setAction("ACTION_AUTO_UPDATE_WIDGET");
            PendingIntent pendingIntent;
            pendingIntent = PendingIntent.getBroadcast(ctx, 121236, alarmIntent, PendingIntent.FLAG_IMMUTABLE);
            AlarmManager manager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);

            Calendar startCalen = Calendar.getInstance();
            if(debug) {
                startCalen.add(Calendar.MINUTE, 10);
            }else {
                startCalen.add(Calendar.MINUTE, interval);
            }
            manager.setExact(AlarmManager.RTC_WAKEUP, startCalen.getTimeInMillis(), pendingIntent);
            LogWrite.Log(ctx, "started: "+startCalen.get(Calendar.HOUR_OF_DAY) + ":" +startCalen.get(Calendar.MINUTE) + ":" + startCalen.get(Calendar.SECOND));
        }catch (Exception e)
        {
            LogWrite.Log(ctx, e.getMessage());
        }
    }
}
