package xyz.lebalex.weatherirk;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.InputStream;
import java.io.BufferedReader;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import static android.net.ConnectivityManager.RESTRICT_BACKGROUND_STATUS_DISABLED;
import static android.net.ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED;
import static android.net.ConnectivityManager.RESTRICT_BACKGROUND_STATUS_WHITELISTED;


import static android.preference.PreferenceManager.getDefaultSharedPreferences;


public class MainActivity extends AppCompatActivity {
    private GridLayout gridlayout;
    private ProgressBar progressBar3;
    private static JSONObject[] entries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        MobileAds.initialize(this, "ca-app-pub-6392397454770928/1263238767");


        AdView mAdView = (AdView) findViewById(R.id.adView);
        mAdView.loadAd(new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build());


        gridlayout = (GridLayout ) findViewById(R.id.grid);
        progressBar3 = (ProgressBar ) findViewById(R.id.progressBar3);

        //StartServices.startBackgroundService(this);

        SharedPreferences sp = getDefaultSharedPreferences(this);
        new GetWaether().execute(new String[]{"http://lebalex.xyz/lebalexServices/pogoda/meteo.php"});

///////////////
/*        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.isActiveNetworkMetered()) {
            switch (connectivityManager.getRestrictBackgroundStatus()) {
                case RESTRICT_BACKGROUND_STATUS_ENABLED:
                    Toast.makeText(getApplication(), "Enabled Data Saver.", Toast.LENGTH_SHORT).show();
                    break;
                case RESTRICT_BACKGROUND_STATUS_WHITELISTED:
                    Toast.makeText(getApplication(), "The app is whitelisted.", Toast.LENGTH_SHORT).show();
                    break;
                case RESTRICT_BACKGROUND_STATUS_DISABLED:
                    Toast.makeText(getApplication(), "Disabled Data Saver.", Toast.LENGTH_SHORT).show();
                    break;
            }
        } else {
            Toast.makeText(getApplication(), "The device is not on a metered network.", Toast.LENGTH_SHORT).show();
        }
*/

    }
    private void setValue(GridLayout gridlayout, String where, String val, int i)
    {
        final TextView rowTextView = new TextView(this);
        rowTextView.setText(where);
        rowTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        rowTextView.setPadding(0,0,0,20);
        gridlayout.addView(rowTextView);
        GridLayout.LayoutParams param =new GridLayout.LayoutParams();
        param.height = GridLayout.LayoutParams.WRAP_CONTENT;
        param.width = GridLayout.LayoutParams.WRAP_CONTENT;
        //param.rightMargin = 5;
        param.topMargin = 15;
        param.leftMargin = 15;
        param.columnSpec = GridLayout.spec(0);
        param.rowSpec = GridLayout.spec(i);
        rowTextView.setLayoutParams (param);



        final TextView rowTextView2 = new TextView(this);
        rowTextView2.setText(val);
        rowTextView2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        rowTextView2.setGravity(Gravity.RIGHT);
        gridlayout.addView(rowTextView2);
        param =new GridLayout.LayoutParams();
        param.height = GridLayout.LayoutParams.WRAP_CONTENT;
        param.width = GridLayout.LayoutParams.WRAP_CONTENT;
        param.rightMargin = 15;
        param.topMargin = 5;
        //param.leftMargin = 5;
        param.columnSpec = GridLayout.spec(1);
        param.rowSpec = GridLayout.spec(i);
        param.setGravity(Gravity.RIGHT);
        rowTextView2.setLayoutParams (param);
    }

    class GetWaether extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            String result = "";
            try {
                URL url = new URL(params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
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
                    SharedPreferences sp = getDefaultSharedPreferences(getApplication());
                    JSONArray jsonArray = new JSONArray(result);
                    entries = new JSONObject[jsonArray.length()];
                    int id_notNan=-1;
                    for (int i=0;i<jsonArray.length();i++)
                    {
                        JSONObject json = jsonArray.getJSONObject(i);
                        String where = json.getString("where");
                        String val = json.getString("temp");
                        entries[i]=json;
                        if(!val.contains("NaN")) {
                            setValue(gridlayout, where, val, i);
                            if(id_notNan<0)
                                id_notNan=i;
                        }
                    }
                    JSONObject json_w = jsonArray.getJSONObject(Integer.parseInt(sp.getString("place_temp", "0")));
                    if(json_w.getString("temp").contains("NaN"))
                        json_w=jsonArray.getJSONObject(id_notNan);

                    Intent intent = new Intent(getApplicationContext(), WeatherWidget.class);
                    intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
                    int ids[] = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), WeatherWidget.class));
                    if(ids.length>0) {
                        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
                        final Bundle bundle = new Bundle();

                        bundle.putBinder("object_value", new ObjectWrapperForBinder(json_w));
                        intent.putExtras(bundle);
                        sendBroadcast(intent);
                    }

                }catch(Exception e){
                    LogWrite.LogError(getApplicationContext(), e.getMessage());
                }

            }
            progressBar3.setVisibility(View.GONE);
        }
    }


    public static JSONObject[] getEntries() {
        return entries;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
            return true;
        }
        /*if (id == R.id.action_logs) {
            Intent i = new Intent(this, LogActivity.class);
            startActivity(i);
            return true;
        }*/
        /*if (id == R.id.action_backgroundStatus) {
            Intent intent = new Intent(Settings.ACTION_IGNORE_BACKGROUND_DATA_RESTRICTIONS_SETTINGS, Uri.parse("package:" + getPackageName()));
            startActivity(intent);
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }
}
