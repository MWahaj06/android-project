package com.example.link;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private WebView mWebView;
    TextView tvText;
    ImageView ivImage;
    String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWebView = (WebView) findViewById(R.id.activity_main_webview);
        ivImage = findViewById(R.id.ivImage);
        tvText = findViewById(R.id.tvText);


        WebSettings webSettings = mWebView.getSettings();

        Context context = this;
        mWebView.getSettings().setGeolocationDatabasePath(context.getFilesDir().getPath());

        webSettings.setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);


        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                CookieSyncManager.getInstance().sync();
                URL url1 = null;
                try {
                    url1 = new URL(url);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                System.out.println("Hordr user Ref " + url1.getRef());
                System.out.println("Hordr user host " + url1.getHost());
                System.out.println("Hordr user authority " + url1.getAuthority());
                String cookies = CookieManager.getInstance().getCookie(url);
                System.out.println("All COOKIES " + cookies);
                String[] temp = cookies.split(";");
                OkHttpScript okHttpScript = new OkHttpScript();
                okHttpScript.execute(temp[2]);
                Log.i("Cookies extracted: ", temp[2]);
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }

        });

        mWebView.loadUrl("https://hordr.app/");
        handleIntent();

    }

    private void handleIntent() {
        Intent intent = getIntent();
        String action = intent.getAction();
        type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleText(intent);
            }
        }
    }

    public void buttonClick(View v) {
        switch (v.getId()) {
            case R.id.button1:
                mWebView.loadUrl("https://hordr.app/");
                break;
            case R.id.button2:
                mWebView.loadUrl("http://bing.com");
                break;
        }
    }

    void handleText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            mWebView.loadUrl(sharedText);
//            tvText.setVisibility(View.VISIBLE);
//            tvText.setText("" + sharedText);
        }
    }

    public static class OkHttpScript extends AsyncTask {

        private static final String TAG = "OkHttpScript";

        @Override
        protected Object doInBackground(Object[] objects) {
            OkHttpClient client = new OkHttpClient().newBuilder().build();
            MediaType mediaType =MediaType.parse("application/json; charset=utf-8");
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("user", objects[0].toString());
                jsonObject.put("URL", objects[0].toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            RequestBody requestBody =  RequestBody.create(mediaType, jsonObject.toString());

            Request request = new Request.Builder()
                        .url("https://hordr-backup.bubbleapps.io/version-test/api/1.1/obj/API")
                        .method("POST",requestBody)
                        .addHeader("Authorization", "Bearer b94336e697812bdcf5927b12dbc78c28")
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    return response.body().string();
            } catch (IOException e) {
                    e.printStackTrace();
                }
            return "";
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            Log.d(TAG, "onPostExecute: "+o);
        }
    }

}






