/*
 * locus Android
 * Sven James <kalterregen AT gmx.net>
 */

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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class ProjectMapActivity extends Activity {
	public static final String PREFS_NAME = "MyPrefsFile"; // TODO
	
	private String serverUrl;
	private String user;
	private int    updateTick;
	
    private Button       mapButton;
	private ToggleButton serviceButton;
	private TextView     serverText;
	private TextView     userText;
	private EditText     updateTickText;
	private Button       saveButton;
	
	public static void debug(Context ctx, String msg) {
		Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
	}
	
	private void setupButtons() {
	    boolean enabled = !serverUrl.equals("") && !user.equals("");
	    serviceButton.setEnabled(enabled);
	    mapButton.setEnabled(enabled);
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mapButton      = (Button)       findViewById(R.id.mapButton);
        serviceButton  = (ToggleButton) findViewById(R.id.serviceButton);
        serverText     = (TextView)     findViewById(R.id.serverText);
        userText       = (TextView)     findViewById(R.id.userText);
        updateTickText = (EditText)     findViewById(R.id.updateTickText);
        saveButton     = (Button)       findViewById(R.id.saveButton);
        
        // restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        serverUrl  = settings.getString("serverUrl", "");
        user       = settings.getString("user", "");
        updateTick = settings.getInt("updateTick", 300);
        
        serverText.setText(serverUrl);
        userText.setText(user);
        updateTickText.setText(String.format("%d", updateTick));
        
        saveButton.setOnClickListener(mCorkyListener); // TODO
        mapButton.setOnClickListener(mapButtonClickListener);
        serviceButton.setChecked(ProjectMapService.running);
        serviceButton.setOnClickListener(serviceButtonClickListener);
        setupButtons();
    }
    
    private OnClickListener serviceButtonClickListener = new OnClickListener() {
        public void onClick(View view) {
            Intent intent = new Intent(
                ProjectMapActivity.this,
                ProjectMapService.class
            );
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
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(serverUrl)));
        }
    };
    
    private OnClickListener mCorkyListener = new OnClickListener() { // TODO
        public void onClick(View view) {
            serverUrl  = serverText.getText().toString().trim();
            user       = userText.getText().toString().trim();
            updateTick = Integer.parseInt(updateTickText.getText().toString());
            // TODO: check values
        	
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("serverUrl", serverUrl);
            editor.putString("user", user);
            editor.putInt("updateTick", updateTick);
            editor.commit();
            
            setupButtons();
        }
    };
}
