package com.orbotix.poolthrowaway;

import java.util.Calendar;

import orbotix.robot.app.StartupActivity;
import orbotix.robot.base.RobotProvider;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

public class SettingsActivity extends Activity {
TextView game_duration, turn_duration;
Calendar dateAndTime=Calendar.getInstance();
private int gameDuration_ = 30;
private int turnDuration_ = 10;
    
@Override
public void onCreate(Bundle icicle) {
  super.onCreate(icicle);
  setContentView(R.layout.settings);
  
  game_duration=(TextView)findViewById(R.id.game_duration);
  turn_duration=(TextView)findViewById(R.id.turn_duration);
  
  Button doneBtn = (Button) findViewById(R.id.doneBtn);
  doneBtn.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
  		doneSettings();
      }
  });
}

public void doneSettings(){	
	Intent intent = new Intent();
	Bundle b = new Bundle();
	b.putString("gameduration", ""+gameDuration_);
	b.putString("turnduration", ""+turnDuration_); //Your id
	intent.putExtras(b); //Put your id to your next Intent	
	System.out.println("Juan: Created bundle");
	
	setResult(RESULT_OK, intent);
	finish();
}

public void chooseDate(View v) {
  new TimePickerDialog(SettingsActivity.this, d,
          dateAndTime.get(Calendar.HOUR_OF_DAY),
          dateAndTime.get(Calendar.MINUTE),
          true)
    .show();
}

public void chooseTime(View v) {
  new TimePickerDialog(SettingsActivity.this, t,
                        dateAndTime.get(Calendar.HOUR_OF_DAY),
                        dateAndTime.get(Calendar.MINUTE),
                        true)
    .show();
}

private void updateLabelGameDuration(int mins, int secs) {
  gameDuration_ = (mins*60)+secs;
  game_duration.setText(mins+":"+secs);
}

private void updateLabelTurnDuration(int mins, int secs){
  turnDuration_ = (mins*60)+secs;
  turn_duration.setText(mins+":"+secs);	
}

TimePickerDialog.OnTimeSetListener d=new TimePickerDialog.OnTimeSetListener() {
	  public void onTimeSet(TimePicker view, int minutes,
              int seconds) {
dateAndTime.set(Calendar.MINUTE, minutes);
dateAndTime.set(Calendar.SECOND, seconds);
updateLabelGameDuration(minutes, seconds);
}
};  

TimePickerDialog.OnTimeSetListener t=new TimePickerDialog.OnTimeSetListener() {
  public void onTimeSet(TimePicker view, int minutes,
                        int seconds) {
	  dateAndTime.set(Calendar.MINUTE, minutes);
	  dateAndTime.set(Calendar.SECOND, seconds);
    updateLabelTurnDuration(minutes, seconds);
  }
};  
}
