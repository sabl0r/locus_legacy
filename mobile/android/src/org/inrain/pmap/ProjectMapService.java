/*
 * locus Android
 * Sven James <kalterregen AT gmx.net>
 */

package org.inrain.pmap;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.util.Date;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
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
import android.os.Bundle;
import android.os.IBinder;

public class ProjectMapService extends Service {
    private final static int ONGOING_NOTIFICATION = 1;
    
    public static boolean running = false;
    
    private String serverUrl;
    private String user;
    private int    updateTick;
    
    private LocationManager     locationManager;
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
        
        Intent notificationIntent = new Intent(this, ProjectMapActivity.class);
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
        
        // DEBUG
        String locationProvider = LocationManager.GPS_PROVIDER;
        // RELEASE
        //String locationProvider = LocationManager.NETWORK_PROVIDER;
        
        Location location = locationManager.getLastKnownLocation(
            locationProvider
        );
        if (location != null) {
            update(location);
        }
        
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
        String msg;
        HttpClient client = new DefaultHttpClient();
        HttpPost request = new HttpPost();
        HttpResponse response;
        
        try {
            URI uri = new URI(
                String.format(
                    "%s/api/update/?user=%s&lat=%f&long=%f&accuracy=%f",
                    serverUrl,
                    user,
                    location.getLatitude(),
                    location.getLongitude(),
                    location.getAccuracy()
                )
            );
            counter++;
            ProjectMapActivity.debug(this, counter + " " + uri.toString());
            request.setURI(uri);
        } catch (URISyntaxException e) {
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
            // record the best location in the update time window (updateTick)
            if (
                bestLocation == null ||
                location.getAccuracy() <= bestLocation.getAccuracy()
            ) {
                bestLocation = location;
            }

            if (System.currentTimeMillis() >= nextUpdate) {
                update(bestLocation);
                bestLocation = null;
            }
        }
        
        public void onStatusChanged(String provider, int status, Bundle extras) {}
        public void onProviderEnabled(String provider) {}
        public void onProviderDisabled(String provider) {}
     };
}
