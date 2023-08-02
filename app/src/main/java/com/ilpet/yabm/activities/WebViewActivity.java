package com.ilpet.yabm.activities;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.ilpet.yabm.R;

import java.util.ArrayList;

public class WebViewActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private String url;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        progressBar = findViewById(R.id.progess_bar);
        WebView webView = findViewById(R.id.webview);

        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            url = intent.getStringExtra("url");
        }

        webView.loadUrl(url);
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                progressBar.setProgress(newProgress);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (isRunningActivity()) {
            Intent activityIntent = new Intent(WebViewActivity.this, MainActivity.class);
            startActivity(activityIntent);
        }
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private boolean isRunningActivity() {
        ArrayList<String> runningActivities = new ArrayList<>();
        ActivityManager activityManager = (ActivityManager) getBaseContext().getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.AppTask> services = (ArrayList<ActivityManager.AppTask>) activityManager.getAppTasks();

        for (ActivityManager.AppTask service : services) {
            runningActivities.add(service.toString());
        }
        return !runningActivities.contains("ComponentInfo{com.app/com.app.main.MyActivity}");
    }
}