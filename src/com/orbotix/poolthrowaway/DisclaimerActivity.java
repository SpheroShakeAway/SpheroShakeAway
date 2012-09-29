package com.orbotix.poolthrowaway;

import java.util.Calendar;

import orbotix.robot.app.StartupActivity;
import orbotix.robot.base.RobotProvider;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

public class DisclaimerActivity extends Activity {
	private boolean acceptDisclaimer;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.warning_disclaimer);

		Button doneBtn = (Button) findViewById(R.id.acceptBtn);
		doneBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				acceptDisclaimer = true;
				doneDisclaimer();
			}
		});

		Button declineBtn = (Button) findViewById(R.id.declineBtn);
		declineBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				acceptDisclaimer = false;
				doneDisclaimer();
			}
		});
	}
	
	public void doneDisclaimer(){	
		Intent intent = new Intent();
		Bundle b = new Bundle();
		b.putString("disclaimerAccepted", (acceptDisclaimer ? "true" : "false"));
		System.out.println("Juan: Disclaimer set to: "+ (acceptDisclaimer ? "true" : "false"));
		intent.putExtras(b); //Put your id to your next Intent	

		setResult(ShakeawayActivity.RESULT_OK, intent);
		finish();
	}
}
