package xyz.lebalex.weatherirk;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;

import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity2 extends AppCompatActivity {

    public static Resources mResources;
    private static SharedPreferences sp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mResources = getResources();
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.pref_general, rootKey);
            final ListPreference frequencyPreference = (ListPreference) findPreference("update_frequency");
            frequencyPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String stringValue = newValue.toString();
                    if (!stringValue.equals(sp.getString("update_frequency", "60")))
                        StartServices.startBackgroundService(preference.getContext(), stringValue);
                    return true;
                }
            });

            final ListPreference listPreference = (ListPreference) findPreference("place_temp");
            setListPreferenceData(listPreference);
            listPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    setListPreferenceData(listPreference);
                    return false;
                }
            });
            listPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String stringValue = newValue.toString();
                    if (!stringValue.equals(sp.getString("place_temp", "0"))) {
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("place_temp", stringValue);
                        editor.apply();

                        try {
                            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(preference.getContext());
                            int ids[] = appWidgetManager.getAppWidgetIds(new ComponentName(preference.getContext(), WeatherWidget.class));
                            if (ids.length > 0) {
                                JSONObject[] entries = MainActivity.getEntries();
                                for (int id : ids) {
                                    new WidgetHelper().updateWidget(preference.getContext(), appWidgetManager, id, entries[Integer.parseInt(stringValue)]);
                                }
                            }
                        } catch (Exception e) {
                        }
                    }
                    return true;
                }
            });
        }
    }
    protected static void setListPreferenceData(ListPreference lp) {
        JSONObject[] entries = MainActivity.getEntries();
        List<String> entryValues = new ArrayList<>();
        List<String> entryNameValues = new ArrayList<String>();
        int j=0;
        try {
            for (int i=0;i<entries.length;i++) {
                if (!entries[i].getString("temp").contains("NaN")) {
                    entryNameValues.add(entries[i].getString("where"));
                    entryValues.add(Integer.toString(i));
                }
            }

        }catch(Exception e){
            entryNameValues.add(mResources.getString(R.string.not_data));
            entryValues.add("0");
        }
        lp.setEntries(entryNameValues.toArray(new CharSequence[entryNameValues.size()]));
        lp.setDefaultValue("0");
        lp.setEntryValues(entryValues.toArray(new CharSequence[entryValues.size()]));
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);

    }
}