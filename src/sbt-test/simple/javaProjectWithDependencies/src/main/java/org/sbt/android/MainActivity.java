package org.sbt.android;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState) ;
        TextView v = new TextView(this);
        v.setText("hello, world");
        setContentView(v);
        com.google.gson.Gson gson = new com.google.gson.Gson();
    }
}
