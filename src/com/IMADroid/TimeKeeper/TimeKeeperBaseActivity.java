package com.IMADroid.TimeKeeper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class TimeKeeperBaseActivity extends Activity {
protected String TAG = "TimeKeeper";
	
	// globale
	
	protected Activity activity;
	protected Context context;
	protected SharedPreferences pref;
	protected OnSharedPreferenceChangeListener prefListener;
	
	protected PowerManager.WakeLock mWakeLock = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		activity = this;
		context = getApplicationContext();
		
		pref=PreferenceManager.getDefaultSharedPreferences(this);
		
		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, TAG);
		mWakeLock.setReferenceCounted(false);
		acquireWakeLock();
		
		
		//poure redemarrer l'activité lors de la modification de theme
		prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
      	  public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
      		// do whatever you want here
      	    	Log.d(TAG,"listener shareed preference");
      	    	if (key.equalsIgnoreCase("theme")){
      	    		Log.d(TAG,"listener shareed preference -> restart activity");
      	    		restartFirstActivity();
      	    	}
      		  }
      		};

		
		// mise en place du theme
		String theme_selected = pref.getString("theme", "theme_white_diagonal");
		if (theme_selected.equalsIgnoreCase("theme_black_diamonds")) {
			setTheme(R.style.theme_black_diamonds);
		} else if (theme_selected.equalsIgnoreCase("theme_blue_dust")) {
			setTheme(R.style.theme_blue_dust);
		} else if (theme_selected.equalsIgnoreCase("theme_white_arches")) {
			setTheme(R.style.theme_white_arches);
		} else if (theme_selected.equalsIgnoreCase("theme_white_diagonal")) {
			setTheme(R.style.theme_white_diagonal);
		} 
	}
	
	protected void restartFirstActivity()
    {
    Intent i = getApplicationContext().getPackageManager()
    .getLaunchIntentForPackage(getApplicationContext().getPackageName() );

    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK );
    startActivity(i);
    }
	
	// ======== GESTION MENU ================
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
	  MenuInflater inflater = getMenuInflater();
	  inflater.inflate(R.xml.menu_options, menu);
	  return true;
	}
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
    	    case R.id.menu_preferences:
    	    	try {
    	    	Intent i = new Intent(activity.getApplicationContext(), TimeKeeperPreferenceActivity.class);
    	    	startActivity(i);
    	    	pref.registerOnSharedPreferenceChangeListener(prefListener);
    	    	} catch (Exception ex){
    	    		ex.printStackTrace();
    	    	}
    	        return true;
    	    default:
    	        return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
	public void onResume() {
		super.onResume();
		acquireWakeLock();
	}

	@Override
	public void onPause() {
		super.onPause();
		releaseWakeLock();
	}

	private void acquireWakeLock() {
		// It's okay to double-acquire this because we are not using it
		// in reference-counted mode.
		if (pref.getBoolean("keep_screen_on", false))
			mWakeLock.acquire();
	}

	private void releaseWakeLock() {
		// Don't release the wake lock if it hasn't been created and acquired.
		if (mWakeLock != null && mWakeLock.isHeld()) {
			mWakeLock.release();
		}
	}
}
