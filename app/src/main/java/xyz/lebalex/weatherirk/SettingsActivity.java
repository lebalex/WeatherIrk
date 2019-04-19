package xyz.lebalex.weatherirk;


import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;


import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    //private boolean inFragment;

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            if(preference.getKey().equals("update_frequency")) {
                SharedPreferences sp = getDefaultSharedPreferences(preference.getContext());
                if (!stringValue.equals(sp.getString("update_frequency", "60")))
                    StartServices.startBackgroundService(preference.getContext(), stringValue);
            }
            if(preference.getKey().equals("place_temp")) {
                SharedPreferences sp = getDefaultSharedPreferences(preference.getContext());
                if (!stringValue.equals(sp.getString("place_temp", "0"))) {
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("place_temp", stringValue);
                    editor.commit();

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
            }

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } /*else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    //preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            }*/ else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));



    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            //if (!inFragment) {
            super.onBackPressed();
            return true;
            //}
            //return false;
        }
        return super.onOptionsItemSelected(item);

    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);


            bindPreferenceSummaryToValue(findPreference("update_frequency"));
            bindPreferenceSummaryToValue(findPreference("update_start"));
            //bindPreferenceSummaryToValue(findPreference("timeout"));

            ListPreference listPreference = (ListPreference) findPreference("place_temp");
            setListPreferenceData(listPreference);
            bindPreferenceSummaryToValue(findPreference("place_temp"));


            /*listUpdateFrequency.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    StartServices.startBackgroundService(getActivity());
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), "");
                    LogWrite.Log(getActivity(), "startBackgroundService");
                    return true;
                }
            });*/



        }
        protected void setListPreferenceData(ListPreference lp) {
            JSONObject[] entries = MainActivity.getEntries();
            //CharSequence[] entryValues = new String[entries.length];
            //CharSequence[] entryNameValues = new String[entries.length];
            List<String> entryValues = new ArrayList<>();
            List<String> entryNameValues = new ArrayList<String>();
            int j=0;
            try {
            for (int i=0;i<entries.length;i++) {
                //entryValues[j] = Integer.toString(i);
                //entryNameValues[i] = entries[i];

                    if (!entries[i].getString("temp").contains("NaN")) {
                        entryNameValues.add(entries[i].getString("where"));
                        entryValues.add(Integer.toString(i));
                    }

            }

            }catch(Exception e){
                entryNameValues.add(getResources().getString(R.string.not_data));
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
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

}
