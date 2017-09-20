package xyz.lebalex.weatherirk;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
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
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStream;
import java.io.BufferedReader;
import java.net.URLConnection;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;


public class MainActivity extends AppCompatActivity {

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

        GridLayout gridlayout = (GridLayout ) findViewById(R.id.grid);

        SharedPreferences sp = getDefaultSharedPreferences(this);
        String resultJson = loadWaether(Integer.parseInt(sp.getString("timeout", "10")));
        JSONObject dataJsonObj = null;

        try {
            JSONArray jsonArray = new JSONArray(resultJson);
            for (int i=0;i<jsonArray.length();i++)
            {
                JSONObject json = jsonArray.getJSONObject(i);
                String where = json.getString("where");
                String val = json.getString("temp");
                setValue(gridlayout, where, val, i);
            }
            Intent intent = new Intent(this, WeatherWidget.class);
            intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
            int ids[] = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), WeatherWidget.class));
            if(ids.length>0) {
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
                final Bundle bundle = new Bundle();
                bundle.putBinder("object_value", new ObjectWrapperForBinder(jsonArray.getJSONObject(0)));
                intent.putExtras(bundle);
                sendBroadcast(intent);
            }

        }catch(Exception e){
            LogWrite.LogError(this, e.getMessage());
        }





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
        param.topMargin = 5;
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
        param.rightMargin = 5;
        param.topMargin = 5;
        //param.leftMargin = 5;
        param.columnSpec = GridLayout.spec(1);
        param.rowSpec = GridLayout.spec(i);
        param.setGravity(Gravity.RIGHT);
        rowTextView2.setLayoutParams (param);
    }
    public String loadWaether(int timeout) {


            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

            StrictMode.setThreadPolicy(policy);

            //String urls = "http://lebalexwebapp.azurewebsites.net/pogodaIrk.aspx";
            String urls = "http://lebalex.xyz/pogoda/pogoda.php";
            /*URL url = new URL(urls);

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            if ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            String rs = buffer.toString();*/
            String rs = null;
            try {

            BufferedReader inputStream = null;
            URL jsonUrl = new URL(urls);
            URLConnection dc = jsonUrl.openConnection();
            dc.setConnectTimeout(timeout*1000);
            dc.setReadTimeout(timeout*1000);
            inputStream = new BufferedReader(new InputStreamReader(
                    dc.getInputStream()));

            rs = inputStream.readLine();
            }catch (Exception eee) {
                LogWrite.LogError(this, eee.getMessage());
                rs=null;
            }

            if(rs!=null) {
                String a = "" + (char) 0x00B0;
                //rs = rs.replaceAll("u0026deg;",a);
                rs = rs.replaceAll("&deg;", a);
                rs = rs.replaceAll("&nbsp;", " ");
                //(char) 0x00B0
            }
            return rs;


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
        if (id == R.id.action_logs) {
            Intent i = new Intent(this, LogActivity.class);
            startActivity(i);
            return true;
        }


        return super.onOptionsItemSelected(item);
    }
}
