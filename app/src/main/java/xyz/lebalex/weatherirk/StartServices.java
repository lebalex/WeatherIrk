package xyz.lebalex.weatherirk;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Calendar;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class StartServices {
    private static boolean debug=false;

    public static void startBackgroundService(Context ctx) {
        try {

            SharedPreferences sp = getDefaultSharedPreferences(ctx);
            int interval = Integer.parseInt(sp.getString("update_frequency", "60")) * 1000 * 60;
            int startTime = Integer.parseInt(sp.getString("update_start", "0"));

            Intent alarmIntent = new Intent(ctx, UpdatesReceiver.class);
            alarmIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            alarmIntent.setAction("ACTION_AUTO_UPDATE_WIDGET");
            PendingIntent pendingIntent;
            pendingIntent = PendingIntent.getBroadcast(ctx, 121236, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager manager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);

            Calendar startCalen = Calendar.getInstance();
            //Calendar curentTime = Calendar.getInstance();
            if(debug) {
                startCalen.add(Calendar.MINUTE, 1);
            }else {
                startCalen.add(Calendar.MILLISECOND, interval);
                /*startCalen.set(Calendar.HOUR_OF_DAY, startTime);
                startCalen.set(Calendar.MINUTE, 1);
                startCalen.set(Calendar.SECOND, 1);
                startCalen.set(Calendar.MILLISECOND, 1);*/
            }
            /*while (startCalen.before(curentTime))
                startCalen.add(Calendar.MILLISECOND, interval);*/

            manager.setExact(AlarmManager.RTC_WAKEUP, startCalen.getTimeInMillis(), pendingIntent);
        }catch (Exception e)
        {
            LogWrite.Log(ctx, e.getMessage());
        }
    }
}
