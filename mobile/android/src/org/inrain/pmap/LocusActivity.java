/*
 */

package org.inrain.pmap;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LocusActivity extends Activity {
    private SharedPreferences settings;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.locus);
        settings = getSharedPreferences(ProjectMapActivity.PREFS_NAME, 0);
    }
    
    // would need to inherit
    public boolean isSetup() {
        settings = getSharedPreferences(ProjectMapActivity.PREFS_NAME, 0);
        String serverUrl = settings.getString("serverUrl", "");
        String user      = settings.getString("user",      "");
        return !serverUrl.equals("") && !user.equals("");
    }
    
    public void onResume() {
        super.onResume();
        settings = getSharedPreferences(ProjectMapActivity.PREFS_NAME, 0);
        String serverUrl = settings.getString("serverUrl", "");
        
        TextView setupLabel = (TextView) findViewById(R.id.setupLabel);
        WebView  webView    = (WebView)  findViewById(R.id.webView);
        if (!isSetup()) {
            webView.setVisibility(View.GONE);
            setupLabel.setVisibility(View.VISIBLE);
        } else {
            webView.setVisibility(View.VISIBLE);
            setupLabel.setVisibility(View.GONE);
            if (webView.getUrl() == null) {
                webView.setWebViewClient(client);
                WebSettings webSettings = webView.getSettings();
                webSettings.setJavaScriptEnabled(true);
                webView.loadUrl(serverUrl);
            }
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.trackingItem);
        updateTrackingItem(item, ProjectMapService.running);
        item.setEnabled(isSetup());
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.trackingItem:
            Intent intent = new Intent(this, ProjectMapService.class);
            if (ProjectMapService.running) {
                stopService(intent);
                updateTrackingItem(item, false);
                //debug(ProjectMapActivity.this, "Starting...");
            } else {
                startService(intent);
                updateTrackingItem(item, true);
                //debug(ProjectMapActivity.this, "Stopping...");
            }
            break;
        case R.id.tagLocationItem:
            final WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            if (!wifiManager.isWifiEnabled()) {
                Toast.makeText(LocusActivity.this, "To tag an indoor-location please activate Wifi.", Toast.LENGTH_LONG).show();
                break;
            }
            
            Builder builder = new Builder(this);
            
            final View layout = getLayoutInflater().inflate(R.layout.taglocation, null);
            
            builder.setView(layout);
            builder.setTitle("Tag Your Current Location");
            builder.setPositiveButton(R.string.tag, new OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    List<ScanResult> accessPoints = wifiManager.getScanResults();
                    
                    EditText tagText = (EditText) layout.findViewById(R.id.tagText);
                    String name = tagText.getText().toString().trim().toLowerCase();
                    if (name.equals("")) {
                        Toast.makeText(LocusActivity.this, "Please enter a name.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    
                    Util util = new Util(settings.getString("serverUrl", null));
                    try {
                        util.call("/pois/", Arrays.asList(new BasicNameValuePair[] {
                            new BasicNameValuePair("username", settings.getString("user", null)),
                            new BasicNameValuePair("name", name),
                            new BasicNameValuePair("accesspoints", Util.encodeAccessPoints(accessPoints))
                        }));
                        
                        Toast.makeText(LocusActivity.this, "Location tagged.", Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        Log.i("locus", e.toString());
                        Toast.makeText(LocusActivity.this, "Sorry, something went wrong talking to the server. Please try again in a few moments.", Toast.LENGTH_LONG).show();
                    }
                }
            });
            builder.setNegativeButton(R.string.cancel, new OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    return;
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
            break;
        case R.id.settingsItem:
            startActivity(new Intent(this, ProjectMapActivity.class));
            break;
        }
        return true;
    }
    
    private void updateTrackingItem(MenuItem item, boolean running) {
        if (running) {
            item.setTitle(getString(R.string.stopTracking));
        } else {
            item.setTitle(getString(R.string.startTracking));
        }
    }
    
    private WebViewClient client = new WebViewClient() {
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            settings = getSharedPreferences(ProjectMapActivity.PREFS_NAME, 0);
            String serverUrl = settings.getString("serverUrl", "");
            Log.d("locus", url);
            Log.d("locus", serverUrl);
            if (url.startsWith(serverUrl)) {
                return false;
            } else {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                return true;
            }
            /*if (Uri.parse(url).getHost().equals("www.example.com")) {
                // This is my web site, so do not override; let my WebView load the page
                return false;
            }
            // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
            return true;*/
        }
    };
}
