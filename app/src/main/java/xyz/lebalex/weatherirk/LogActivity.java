package xyz.lebalex.weatherirk;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class LogActivity extends AppCompatActivity {
    private  LogActivity ctx = this;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        TextView logstring = (TextView) findViewById(R.id.logstring);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        logstring.setText(sp.getString("logs",""));



        TextView logtitle = (TextView) findViewById(R.id.logtitle);
        logtitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = sp.getString("logs", "");
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("logs", "");
                editor.commit();
                TextView logstring = (TextView) findViewById(R.id.logstring);
                logstring.setText("");

            }
        });

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
}
