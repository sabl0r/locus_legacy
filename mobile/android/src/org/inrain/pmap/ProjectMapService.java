/*
 * locus Android
 * Sven James <kalterregen AT gmx.net>
 */

package org.inrain.pmap;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class ProjectMapService extends Service {
    private final static int ONGOING_NOTIFICATION = 1;
    
    public static boolean running = false;
    
    private String serverUrl;
    private String user;
    private int    updateTick;
    
    private LocationManager     locationManager;
    private WifiManager         wifiManager;
    private NotificationManager notificationManager;
    private Notification        notification;
    
    private int      failed       = 0;
    private long     nextUpdate   = 0;
    private Location bestLocation = null;
    
    @Override
    public void onCreate() {
        locationManager = (LocationManager) getSystemService(
             Context.LOCATION_SERVICE
        );
        notificationManager = (NotificationManager) getSystemService(
            Context.NOTIFICATION_SERVICE
        );
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        running = true;
        
        // copied from ProjectMapActivity
        // restore preferences
        SharedPreferences settings = getSharedPreferences(ProjectMapActivity.PREFS_NAME, 0);
        serverUrl  = settings.getString("serverUrl", "");
        user       = settings.getString("user", "");
        updateTick = settings.getInt("updateTick", 300);
        
        Intent notificationIntent = new Intent(this, LocusActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            0
        );
        notification = new Notification(
            R.drawable.ic_menu_mapmode,
            getText(R.string.ongoingNotification),
            System.currentTimeMillis()
        );
        notification.setLatestEventInfo(
            this,
            getText(R.string.ongoingNotification),
            "",
            pendingIntent
        );
        startForeground(ONGOING_NOTIFICATION, notification);
        
        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(this, "To enable indoor-tracking please activate Wifi.", Toast.LENGTH_LONG).show();
        }
        
        // DEBUG
        //String locationProvider = LocationManager.GPS_PROVIDER;
        // RELEASE
        String locationProvider = LocationManager.NETWORK_PROVIDER;
        
        /*Location location = locationManager.getLastKnownLocation(
            locationProvider
        );
        if (location != null) {
            update(location, wifiManager.getScanResults());
        }*/
        // ^ this will block the ui; don't do this here; onLocationChanged will
        // be called soon enough
        
        locationManager.requestLocationUpdates(
            locationProvider,
            0,
            0,
            locationListener
        );
        
        // we should not be killed (foreground)
        return START_STICKY;
    }
    
    @Override
    public void onDestroy() {
        locationManager.removeUpdates(locationListener);
        stopForeground(true);
        running = false;
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    private int counter = 0;
    
    private void update(Location location) {
        update(location, null);
    }
    
    private void update(Location location, List<ScanResult> accessPoints) {
        String msg;
        HttpClient client = new DefaultHttpClient();
        HttpResponse response;
        HttpPost request = new HttpPost();
        
        String accessPointsJson = "";
        if (accessPoints != null) {
            for (ScanResult ap : accessPoints) {
                if (accessPointsJson.length() != 0) {
                    accessPointsJson += ",";
                }
                accessPointsJson += String.format(
                   "{\"id\":\"%s\",\"l\":%s}",
                   ap.BSSID,
                   ap.level
                );
            }
            accessPointsJson = "[" + accessPointsJson + "]";
        }
       // Log.d("locus", accessPointsJson);
        
        try {
            
            // TODO: move somewhere else
            String url = serverUrl;
            if (!url.endsWith("/")) {
                url += "/";
            }
            
            request.setURI(new URI(String.format(
                "%sapi/location/",
                url
            )));
            
            
            ArrayList<BasicNameValuePair> data = new ArrayList<BasicNameValuePair>();
            //List<BasicNameValuePair> data = Arrays.asList(new BasicNameValuePair[] {
            data.add(new BasicNameValuePair("username",  user));
            data.add(new BasicNameValuePair("latitude",  Double.toString(location.getLatitude())));
            data.add(new BasicNameValuePair("longitude", Double.toString(location.getLongitude())));
            data.add(new BasicNameValuePair("accuracy",  Double.toString(location.getAccuracy())));
            data.add(new BasicNameValuePair("provider",  "network"));
            //});
            if (accessPoints != null) {
                data.add(new BasicNameValuePair("accesspoints", accessPointsJson));
            }
            request.setEntity(new UrlEncodedFormEntity(data));
            
            try {
                Log.d("locus", request.getURI().toString());
                Log.d("locus", EntityUtils.toString(request.getEntity()));
            } catch (IOException e) {
                e.printStackTrace();
            }
//            
//            URI uri = new URI(
//                String.format(
//                    "%s/api/location/?username=%s&latitude=%f&longitude=%f&accuracy=%f&provider=x",
//                    serverUrl,
//                    user,
//                    location.getLatitude(),
//                    location.getLongitude(),
//                    location.getAccuracy()
//                )
//            );
//            counter++;
//            //ProjectMapActivity.debug(this, counter + " " + uri.toString());
//            request.setURI(uri);
        } catch (URISyntaxException e) {
            // TODO
            ProjectMapActivity.debug(this, "fuck application bug!!" + e.toString());
            return;
        } catch (UnsupportedEncodingException e) {
            // TODO
            ProjectMapActivity.debug(this, "fuck application bug!!" + e.toString());
    	    return;
        }
        
        try {
            response = client.execute(request);
            int code = response.getStatusLine().getStatusCode();
            if (code == 200) {
                failed = 0;
                msg = "last update: " +
                    DateFormat.getTimeInstance().format(new Date());
            } else {
                failed++;
                msg = String.format("error (%d): HTTP %d", failed, code);
            }
		} catch (IOException e) {
		    failed++;
		    msg = String.format("error (%d): %s", failed, e.toString());
		}
        
        if (failed == 0 || failed > 3) {
            nextUpdate = System.currentTimeMillis() + updateTick * 1000;
        } else {
            nextUpdate = System.currentTimeMillis() + failed * 60 * 1000;
        }
        setNotification(msg);
    }

    private void setNotification(String msg) {
        notification.setLatestEventInfo(
            ProjectMapService.this,
            getText(R.string.ongoingNotification),
            msg,
            notification.contentIntent
        );
        notificationManager.notify(ONGOING_NOTIFICATION, notification);
    }
    
    private LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            Log.d("locus", "onLocationChanged");
            
            // record the best location in the update time window (updateTick)
            if (
                bestLocation == null ||
                location.getAccuracy() <= bestLocation.getAccuracy()
            ) {
                bestLocation = location;
            }
            
            // TODO: move somewhere else
            List<ScanResult> accessPoints = wifiManager.getScanResults();
            /*if (accessPoints == null) {
                Log.d("locus", "no access points/wifi off...");
            } else {
                Log.d("locus", accessPoints.toString());
            }*/

            if (System.currentTimeMillis() >= nextUpdate) {
                update(bestLocation, accessPoints);
                bestLocation = null;
            }
        }
        
        public void onStatusChanged(String provider, int status, Bundle extras) {}
        public void onProviderEnabled(String provider) {}
        public void onProviderDisabled(String provider) {}
     };
}
