package xyz.lebalex.weatherirk;

import android.content.Context;
import android.content.SharedPreferences;


import androidx.preference.PreferenceManager;

import java.util.Calendar;

/**
 * Created by ivc_lebedevav on 03.02.2017.
 */

public class LogWrite {
    public static void Log(Context pContext, String str) {
        /*SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(pContext);
        if (sp.getBoolean("save_log", false)) {
            WriteLog(pContext, str);
        }*/
        //WriteLog(pContext, str);
        //Log.i("LogWrite", str);
    }
    public static void LogError(Context pContext, String str) {
        //WriteLog(pContext, str);
        //Log.i("LogWrite", str);
    }

    private static void WriteLog(Context pContext, String str)
    {
        try {
            //Log.i("LogWrite", str);
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(pContext);
                Calendar calen = Calendar.getInstance();
                int c = calen.get(Calendar.DATE);
                String logs = sp.getString("logs", "");
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("logs", logs + calen.get(Calendar.YEAR) + "-" + calen.get(Calendar.MONTH) + "-" + calen.get(Calendar.DATE) + " " + calen.get(Calendar.HOUR_OF_DAY) + ":" +
                        calen.get(Calendar.MINUTE) + ":" + calen.get(Calendar.SECOND) + " " + str + "\n");
                editor.commit();
        }catch(Exception e)
        {
            //
        }
    }
}
