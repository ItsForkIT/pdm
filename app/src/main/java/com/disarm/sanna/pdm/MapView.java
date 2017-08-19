package com.disarm.sanna.pdm;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class MapView extends AppCompatActivity {
    WebView wv;
    ProgressBar pb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        getWindow().setFeatureInt( Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);
        setContentView(R.layout.activity_map_view);
        pb = (ProgressBar) findViewById(R.id.progressBar);
        pb.setMax(100);
        final String URL = "http://127.0.0.1:8080/getMapAsset/index.html";
        wv = (WebView) findViewById(R.id.wv);
        wv.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress)
            {
                setTitle("MAP");
                pb.setProgress(progress);
                setProgress(progress * 100); //Make the bar disappear after URL is loaded
                if(progress==100){
                    pb.setVisibility(View.GONE);
                }
            }
        });
        WebSettings wb = wv.getSettings();
        wb.setJavaScriptEnabled(true);
        wv.setWebViewClient(new WebViewClient());
        wb.setJavaScriptCanOpenWindowsAutomatically(true);
        wv.loadUrl(URL);
    }
}
