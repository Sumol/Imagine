package com.IMADroid.TimeKeeper;

import java.io.IOException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.text.Editable;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;
import android.widget.TextView;
import android.widget.Toast;


public class TimeKeeperActivity extends TimeKeeperBaseActivity  {
		
	protected Chronometer chrono_text;
	protected int duration = 0;
	protected TextView text_result;
	protected Button buttonStart10;
	protected Button buttonStart15;
	protected Button buttonPause;
	protected Button buttonPlay;
	protected Button buttonReset;
	protected boolean play=false;
	protected boolean count = false;// permet se savoir si l'on doit compter les seconde ou non ( pause & co)
	protected long timeWhenStopped;
	
	protected Vibrator vibrator;
	protected Ringtone ringtone;
	protected AlertDialog.Builder builder;
	protected AlertDialog alertDialog;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);
		//findViewById(R.id.main_layout).setOnKeyListener(onKey(text_result, duration, null));
		
		chrono_text = (Chronometer) findViewById(R.id.chrono_text);
		chrono_text.setTypeface(Typeface.createFromAsset(getAssets(), "AldotheApache.ttf"));
		
		vibrator = (Vibrator) getSystemService(getApplicationContext().VIBRATOR_SERVICE);
		builder = new AlertDialog.Builder(this);

		buttonStart10 = (Button)findViewById(R.id.buttonStart10);
		buttonStart15 = (Button)findViewById(R.id.buttonStart15);
		buttonPause = (Button)findViewById(R.id.buttonPause);
		buttonPlay = (Button)findViewById(R.id.buttonPlay);
		buttonReset = (Button)findViewById(R.id.buttonReset);
		
		timeWhenStopped = 0;
		
		chrono_text.setOnChronometerTickListener(new OnChronometerTickListener()
			{
	
				public void onChronometerTick(Chronometer arg0) {
					Log.d(TAG,"tick");
					if (count){ // pour être sur de pas incrementer pendant la pause
						Log.d(TAG,"count");
						duration--;
						if (duration==0){
							timer_finished();
						}
					}
				}
	
			}
		);


		buttonStart10.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				chrono_launch(10);
			}});

		buttonStart15.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				chrono_launch(15);
			}});

		buttonPause.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				timeWhenStopped = chrono_text.getBase() - SystemClock.elapsedRealtime();
				Log.d(TAG,"pause : base : dur"+ duration +" "+chrono_text.getBase()+" -- elapsedTime : "+ SystemClock.elapsedRealtime());
				chrono_text.stop();
				count=false;
				buttonPause.setVisibility(View.GONE);
				buttonPlay.setVisibility(View.VISIBLE);

			}});
		buttonPlay.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				Log.d(TAG,"bef start : dur"+ duration +"base : "+chrono_text.getBase()+" -- elapsedTime : "+ SystemClock.elapsedRealtime());
				
				chrono_text.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
				
				chrono_text.start();
				count=true;
				Log.d(TAG,"afeter start : dur"+ duration +" base : "+chrono_text.getBase()+" -- elapsedTime : "+ SystemClock.elapsedRealtime());
				
				buttonPause.setVisibility(View.GONE);
				buttonPlay.setVisibility(View.VISIBLE);

			}});

		buttonReset.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				chrono_text.stop();
				chrono_text.setBase(SystemClock.elapsedRealtime());
				toggleButton();
			}});
	}

	public void chrono_launch(int newDuration){
		duration = newDuration;
		timeWhenStopped = 0;
		chrono_text.setBase(SystemClock.elapsedRealtime());
		chrono_text.start();
		toggleButton();
	}

	public void timer_finished(){
		chrono_text.stop();
		alert_finish();
		toggleButton();
	}
	
	public void alert_finish(){
		//Personnalisation alert dialog fini
		LayoutInflater factory = LayoutInflater.from(this);
		View alertDialogView = factory.inflate(R.layout.alert_timer_finished, null);
        builder.setView(alertDialogView);
        builder.setCancelable(false)
	       .setPositiveButton(R.string.alert_finished_text_button, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	                stopNotification();
	           }
	       });
        
        if (pref.getBoolean("vibrator", true)){
			long[] pattern = { 0, 200, 500 };
			vibrator.vibrate(pattern, 0);
		}
        
        if (pref.getBoolean("ringtone_activated", false)){
			Log.d(TAG,"Sonnerie en cours");
			 Uri notification_uri= Uri.parse(pref.getString("ringtone", null));
			 if(notification_uri == null){
			 notification_uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
				 if(notification_uri == null){
			         // alert is null, using backup
					 notification_uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			         if(notification_uri == null){  // I can't see this ever being null (as always have a default notification) but just incase
			             // alert backup is null, using 2nd backup
			        	 notification_uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);               
			         }
			     }
			 }
			 Log.d(TAG,"ringtone lue : "+notification_uri);
			 ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification_uri);
			 ringtone.play();
		}
        alertDialog = builder.show();
	}
	
	public void toggleButton(){
		
		if (play){
			play = !play;
			count = false;
			buttonPause.setVisibility(View.GONE); //8 = gone, completement parti
			buttonPlay.setVisibility(View.GONE);
			buttonReset.setVisibility(View.GONE);
			buttonStart10.setVisibility(View.VISIBLE); 
			buttonStart15.setVisibility(View.VISIBLE);
		} else {
			play = !play;
			count= true;
			buttonPause.setVisibility(View.VISIBLE); 
			buttonReset.setVisibility(View.VISIBLE);
			buttonStart10.setVisibility(View.GONE);
			buttonStart15.setVisibility(View.GONE);
		}
	}
	
	protected void stopNotification(){
		if (pref.getBoolean("vibrator", true)){
			vibrator.cancel();
		}
		if (pref.getBoolean("ringtone_activated", false)){
			ringtone.stop();
		}
	}
    
}