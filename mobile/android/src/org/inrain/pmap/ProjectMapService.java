package org.inrain.pmap;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Notification;
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
import android.widget.TextView;

public class ProjectMapService extends Service {
    public static boolean running = false;
    
    /** 
     * A constructor is required, and must call the super IntentService(String)
     * constructor with a name for the worker thread.
     */
    public ProjectMapService() {
      
    }
    
    public void onCreate() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }
    
    private String serverUrl;
    private String user;
    LocationManager locationManager;

    /**
     * The IntentService calls this method from the default worker thread with
     * the intent that started the service. When this method returns, IntentService
     * stops the service, as appropriate.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        running = true;

        Intent notificationIntent = new Intent(this, ProjectMapActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        
        Notification notification = new Notification(R.drawable.ic_menu_mapmode,
            getText(R.string.ongoingNotification), System.currentTimeMillis());
        notification.setLatestEventInfo(
            this,
            getText(R.string.ongoingNotification),
            "foo",
            pendingIntent
        );
        
        int ONGOING_NOTIFICATION = 1;
        startForeground(1, notification);
        
        // Restore preferences
        SharedPreferences settings = getSharedPreferences(ProjectMapActivity.PREFS_NAME, 0);
        serverUrl = settings.getString("serverUrl", "");
        user      = settings.getString("user", "");
        
        // LOCATION
        // Acquire a reference to the system Location Manager
        //String locationProvider = LocationManager.GPS_PROVIDER;
        String locationProvider = LocationManager.NETWORK_PROVIDER;
      
        //debug("listening for locationsx");
        Location location = locationManager.getLastKnownLocation(locationProvider);
        if (location != null) {
            update(location);
        }

      
        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(locationProvider, 0, 0, locationListener);
	  
        // Normally we would do some work here, like download a file.
        // For our sample, we just sleep for 5 seconds.
        /*long endTime = System.currentTimeMillis() + 5*1000;
        while (System.currentTimeMillis() < endTime) {
            synchronized (this) {
                try {
                    wait(endTime - System.currentTimeMillis());
                } catch (Exception e) {}
        }*/
        
        // If we get killed, after returning from here, restart
        return START_STICKY; // we should not  be killed (foregroudn!)
    }
    
    @Override
    public void onDestroy() {
        locationManager.removeUpdates(locationListener);
        
        // The service is no longer used and is being destroyed
        stopForeground(true);
        
        running = false;
    }
  
    private int counter = 0;
  
    private void update(Location location) {	  
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet();
        try {
            String url = String.format("%s?user=%s&lat=%f&long=%f&accuracy=%f", serverUrl, user, location.getLatitude(), location.getLongitude(), location.getAccuracy());
            URI uri = new URI(url);
            counter++;
            ProjectMapActivity.debug(this, counter + " " + uri.toString());
			request.setURI(uri);
        } catch (URISyntaxException e) {
            ProjectMapActivity.debug(this, "fuck application bug!!" + e.toString());
    	    return;
        }
        try {
            client.execute(request);
		} catch (ClientProtocolException e) {
		    ProjectMapActivity.debug(this, "server error: " + e.toString());
		} catch (IOException e) {
		    ProjectMapActivity.debug(this, "server error: " + e.toString());
		}
    }


    // Define a listener that responds to location updates
    private LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            // Called when a new location is found by the network location provider.
            update(location);
        }
    
        public void onStatusChanged(String provider, int status, Bundle extras) {}
        public void onProviderEnabled(String provider) {}
        public void onProviderDisabled(String provider) {}
     };

    @Override
    public IBinder onBind(Intent arg0) {
    	// TODO Auto-generated method stub
    	return null;
    }
  
}
