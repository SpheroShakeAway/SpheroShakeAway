package com.orbotix.poolthrowaway;

/***
Copyright (c) 2008-2012 CommonsWare, LLC
Licensed under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License. You may obtain	a copy
of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
by applicable law or agreed to in writing, software distributed under the
License is distributed on an "AS IS" BASIS,	WITHOUT	WARRANTIES OR CONDITIONS
OF ANY KIND, either express or implied. See the License for the specific
language governing permissions and limitations under the License.
	
From _The Busy Coder's Guide to Android Development_
  http://commonsware.com/Android
*/


import java.util.Calendar;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

public class SettingsActivity extends Activity {
TextView game_duration, turn_duration;
Calendar dateAndTime=Calendar.getInstance();
    
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

private void updateLabelGameDuration(int duration) {
  game_duration.setText("" + duration);
}

private void updateLabelTurnDuration(int duration){
  turn_duration.setText(""+ duration);	
}

TimePickerDialog.OnTimeSetListener d=new TimePickerDialog.OnTimeSetListener() {
	  public void onTimeSet(TimePicker view, int hourOfDay,
              int minute) {
dateAndTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
dateAndTime.set(Calendar.MINUTE, minute);
updateLabelGameDuration(hourOfDay);
}
};  

TimePickerDialog.OnTimeSetListener t=new TimePickerDialog.OnTimeSetListener() {
  public void onTimeSet(TimePicker view, int hourOfDay,
                        int minute) {
    dateAndTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
    dateAndTime.set(Calendar.MINUTE, minute);
    updateLabelTurnDuration(hourOfDay);
  }
};  
}
