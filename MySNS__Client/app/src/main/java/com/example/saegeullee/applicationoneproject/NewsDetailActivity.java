package com.example.saegeullee.applicationoneproject;

import android.app.PendingIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class NewsDetailActivity extends AppCompatActivity {

    private static final String TAG = "NewsDetailActivity";

    private WebView webView;
    private WebSettings webSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);

        webView = findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient());
        webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        if(getIntent().hasExtra(getString(R.string.intent_news_content))) {

            String link = getIntent().getStringExtra(getString(R.string.intent_news_content));
            Log.d(TAG, "onCreate: link : " + link);

            webView.loadUrl(link);

        }
    }
}
