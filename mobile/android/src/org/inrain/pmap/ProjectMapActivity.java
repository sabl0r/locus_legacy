package org.inrain.pmap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class ProjectMapActivity extends Activity {
	public static final String PREFS_NAME = "MyPrefsFile";
	
	private String serverUrl;
	private String user;
	
	Button saveButton;
	ToggleButton serviceButton;
	Button mapButton;
	
	public static void debug(Context ctx, String msg) {
		//TextView debugLabel = (TextView) findViewById(R.id.debugLabel);
		//debugLabel.setText(msg);
		Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
	}
	
	private void setupButtons() {
	    boolean enabled = !serverUrl.equals("") && !user.equals("");
	    serviceButton.setEnabled(enabled);
	    mapButton.setEnabled(enabled);
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        saveButton = (Button) findViewById(R.id.saveButton);
        serviceButton = (ToggleButton) findViewById(R.id.serviceButton);
        mapButton = (Button) findViewById(R.id.mapButton);
        
        saveButton.setOnClickListener(mCorkyListener);
        mapButton.setOnClickListener(mapButtonClickListener);
        
        // Restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        serverUrl = settings.getString("serverUrl", "");
        user      = settings.getString("user", "");
        
        TextView serverText = (TextView) findViewById(R.id.serverText);
        TextView userText   = (TextView) findViewById(R.id.userText);
        serverText.setText(serverUrl);
        userText.setText(user);
        
        serviceButton.setChecked(ProjectMapService.running);
        serviceButton.setOnClickListener(serviceButtonClickListener);
        setupButtons();
    }
    
    private OnClickListener serviceButtonClickListener = new OnClickListener() {
        public void onClick(View view) {
            Intent intent = new Intent(ProjectMapActivity.this, ProjectMapService.class);
            //if (ProjectMapService.running) {
            if (serviceButton.isChecked()) {
                startService(intent);
                debug(ProjectMapActivity.this, "Starting...");
            } else {
                stopService(intent);
                debug(ProjectMapActivity.this, "Stopping...");
            }
        }
    };
    
    private OnClickListener mapButtonClickListener = new OnClickListener() {
        public void onClick(View view) {
            Uri uri = Uri.parse(serverUrl);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    };
    
    // Create an anonymous implementation of OnClickListener
    private OnClickListener mCorkyListener = new OnClickListener() {
        public void onClick(View v) {
            TextView serverText = (TextView) findViewById(R.id.serverText);
            TextView userText   = (TextView) findViewById(R.id.userText);
            serverUrl = serverText.getText().toString().trim();
            user = userText.getText().toString().trim();
        	
            // We need an Editor object to make preference changes.
            // All objects are from android.context.Context
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("serverUrl", serverUrl);
            editor.putString("user", user);
            // Commit the edits!
            editor.commit();
            
            /*if (serverUrl.equals("") || user.equals("")) {
            	debug(ProjectMapActivity.this, "ENTER SOMETHING!!x!!");
            }*/
            setupButtons();
        }
    };

}