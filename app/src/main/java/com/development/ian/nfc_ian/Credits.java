package com.development.ian.nfc_ian;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

public class Credits extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credits);

        WebView wv=(WebView)findViewById(R.id.credits);
        wv.loadUrl("file:///android_asset/credits.html");

        Button b=(Button)findViewById(R.id.creditButton);
        b.setOnClickListener(this);

        setTitle(MainActivity.version);
    }

    @Override
    public void onClick(View view) {
        finish();
    }
}
