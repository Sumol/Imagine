package com.IMADroid.TimeKeeper;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class TimeKeeperPreferenceActivity extends PreferenceActivity {
	protected String TAG = "TimeKeeper";
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
    
    

}
