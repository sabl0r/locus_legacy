/*
 */

package org.inrain.pmap;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.net.wifi.ScanResult;
import android.util.Log;

public class Util {
    String serverUrl;
    
    public static String encodeAccessPoints(List<ScanResult> accessPoints) {
        String json = "";
        if (json != null) {
            for (ScanResult ap : accessPoints) {
                if (json.length() != 0) {
                    json += ",";
                }
                json += String.format(
                   "{\"id\":\"%s\",\"l\":%s}",
                   ap.BSSID,
                   ap.level
                );
            }
            json = "[" + json + "]";
        }
        return json;
    }
    
    public Util(String serverUrl) {
        this.serverUrl = serverUrl;
    }
    
    public void call(String n, List<BasicNameValuePair> args) throws IOException {
        // backwardsbla
        String url = serverUrl;
        if (!url.endsWith("/")) {
            url += "/";
        }
        
        url = url + "api" + n;
        
        try {
            HttpClient client = new DefaultHttpClient();
            HttpPost request = new HttpPost();
            request.setURI(new URI(url));
            request.setEntity(new UrlEncodedFormEntity(args));
            
            try {
                Log.d("locus", request.getURI().toString());
                Log.d("locus", EntityUtils.toString(request.getEntity()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            HttpResponse response = client.execute(request);
            int code = response.getStatusLine().getStatusCode();
            if (code != 200) {
                throw new IOException("http" + Integer.toString(code));
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new RuntimeException();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
}
