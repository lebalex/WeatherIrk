package xyz.lebalex.weatherirk;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
/*
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;*/

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Objects;


public class MainActivity extends AppCompatActivity {
    private SharedPreferences sp;
    private GridLayout gridlayout;
    private ProgressBar progressBar3;
    private static JSONObject[] entries;
    private void MessageBox(String error) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.error)
                .setMessage(error)
                .setCancelable(false)
                .setNegativeButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

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

        //MobileAds.initialize(this, "ca-app-pub-6392397454770928~3042427784");
        /*MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });*/



        /*AdView mAdView = (AdView) findViewById(R.id.adView);
        mAdView.loadAd(new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build());*/


        gridlayout = (GridLayout ) findViewById(R.id.grid);
        progressBar3 = (ProgressBar ) findViewById(R.id.progressBar3);

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        StartServices.startBackgroundService(this, sp.getString("update_frequency", "60"));


        if(sp.getString("meteo_url",null)!=null) {
            //new GetWaether().execute(new String[]{sp.getString("meteo_url",null)});
            TaskRunner taskRunner = new TaskRunner();
            taskRunner.executeAsync(new HttpRunningTask(getApplicationContext(),sp.getString("meteo_url",null)), new Callback<String>() {
                @Override
                public void onComplete(String data) {
                    if(!Objects.equals(data, "")) {
                        JSONObject dataJsonObj = null;

                        try {
                            JSONArray jsonArray = new JSONArray(data);
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


                            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplication());
                            int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(getApplication(), WeatherWidget.class));
                            for (int id : ids) {
                                new WidgetHelper().updateWidget(getApplication(), appWidgetManager, id, json_w);
                            }
                        }catch(Exception e){
                            LogWrite.LogError(getApplicationContext(), e.getMessage());
                        }
                    }else
                    {
                        setValue(gridlayout, "Иркутск", "0", 0);
                    }
                    progressBar3.setVisibility(View.GONE);
                }

            });

                }else
        {
            setValue(gridlayout, "Иркутск", "0", 0);
            progressBar3.setVisibility(View.GONE);
try {
    JSONArray jsonArray = new JSONArray("[{'where':'Нет данных','temp':''}]");
    JSONObject json_w = jsonArray.getJSONObject(0);
    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplication());
    int ids[] = appWidgetManager.getAppWidgetIds(new ComponentName(getApplication(), WeatherWidget.class));
    if (ids.length > 0) {
        for (int id : ids) {
            new WidgetHelper().updateWidget(getApplication(), appWidgetManager, id, json_w);
        }
    }
}catch(Exception e)
{
    LogWrite.LogError(getApplicationContext(), e.getMessage());
}

            MessageBox(getResources().getString(R.string.not_data));

        }


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
        }angddd
*/

    }
    private void setValue(GridLayout gridlayout, String where, String val, int i)
    {
        final TextView rowTextView = new TextView(this);
        rowTextView.setText(where);
        rowTextView.setTextColor(Color.BLACK);
        rowTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, Integer.parseInt(sp.getString("font_size", "20")));
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
        rowTextView2.setTextColor(Color.BLACK);
        rowTextView2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, Integer.parseInt(sp.getString("font_size", "20")));
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
/*
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


                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplication());
                    int ids[] = appWidgetManager.getAppWidgetIds(new ComponentName(getApplication(), WeatherWidget.class));
                    if (ids.length > 0) {
                        for (int id : ids) {
                            new WidgetHelper().updateWidget(getApplication(), appWidgetManager, id, json_w);
                        }
                    }



                }catch(Exception e){
                    LogWrite.LogError(getApplicationContext(), e.getMessage());
                }

            }
            progressBar3.setVisibility(View.GONE);
        }
    }
*/

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
            Intent i = new Intent(this, SettingsActivity2.class);
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
        if (id == R.id.politic) {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.politic_link)));
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
