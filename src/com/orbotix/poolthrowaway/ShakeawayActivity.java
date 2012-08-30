package com.orbotix.poolthrowaway;

import orbotix.robot.app.StartupActivity;
import orbotix.robot.base.FrontLEDOutputCommand;
import orbotix.robot.base.Robot;
import orbotix.robot.base.RobotProvider;
import orbotix.robot.base.SetDataStreamingCommand;
import orbotix.robot.base.StabilizationCommand;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;

public class ShakeawayActivity extends Activity
{
    /**
     * ID for starting the StartupActivity
     */
    private final static int sStartupActivity = 0;
    public final static int RESULT_RELOAD_GAME = 10;
    public final static int RESULT_SETTINGS = 20;
    /**
     * Robot to from which we are streaming
     */
    private Robot mRobot = null;
    
    private int settingsGameDuration = 30;
    private int settingsTurnDuration = 10;
    
    /**
     * SlideToSleepView
     */
    //private SlideToSleepView mSlideToSleepView;
    
    /**
     * Simple Dialog used to show the splash screen
     */
    protected Dialog mSplashDialog;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main1);
        
        startActivityForResult(new Intent(this, StartupActivity.class), sStartupActivity);

        Button instructionsBtn = (Button) findViewById(R.id.InstructionsButton);
        instructionsBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent myIntent = new Intent(view.getContext(), InstructionActivity.class);
                startActivityForResult(myIntent, 9);
            }
        });
        Button startBtn = (Button) findViewById(R.id.StartButton);
        startBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
        		createGameActivity(view);
            }
        });
        
        Button settingsBtn = (Button) findViewById(R.id.SettingsButton);
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	Intent myIntent = new Intent(view.getContext(), SettingsActivity.class);
                startActivityForResult(myIntent, RESULT_SETTINGS);
            }
        });
    }
    
    public void createGameActivity(Object view)
    {
    	Context contextHandle = null;
    	if(view instanceof View){
    		contextHandle = ((View)view).getContext();
    	}
    	else{
    		contextHandle = (Activity) view;
    	}
    	Intent intent = new Intent((Context) contextHandle, GameActivity.class);
		Bundle b = new Bundle();
		if (mRobot != null)
		{
    		b.putString(StartupActivity.EXTRA_ROBOT_ID, mRobot.getUniqueId()); //Your id
    		b.putInt("gameduration", settingsGameDuration);
    		b.putInt("turnduration", settingsTurnDuration);
    		intent.putExtras(b); //Put your id to your next Intent	
    		System.out.println("Juan: Created bundle");
		}
		startActivityForResult(intent, RESULT_RELOAD_GAME);
    }
    
    /**
     * Shows the splash screen over the full Activity
     */
    protected void showSplashScreen() {
        mSplashDialog = new Dialog(this, R.style.SplashScreen);
        mSplashDialog.setContentView(R.layout.splashscreen);
        mSplashDialog.setCancelable(false);
        mSplashDialog.show();
         
        // Set Runnable to remove splash screen just in case
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
          @Override
          public void run() {
            removeSplashScreen();
          }
        }, 6000);
    }
    
    /**
     * Removes the Dialog that displays the splash screen
     */
    protected void removeSplashScreen() {
        if (mSplashDialog != null) {
            mSplashDialog.dismiss();
            mSplashDialog = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        showSplashScreen();
        
        if(resultCode == RESULT_OK){

            if(requestCode == sStartupActivity){
                //Get the Robot from the StartupActivity
            	if(mRobot == null){
            		String id = data.getStringExtra(StartupActivity.EXTRA_ROBOT_ID);
            		mRobot = RobotProvider.getDefaultProvider().findRobot(id);
            	}
            }
            if(requestCode == RESULT_RELOAD_GAME){
            	System.out.println("Launching new game.");
            	createGameActivity(this);
            }
            if(requestCode == RESULT_SETTINGS){
            	Bundle b = data.getExtras();
        		if (b != null)
        		{
        			settingsGameDuration = Integer.parseInt(b.getString("gameduration"));
        	        settingsTurnDuration = Integer.parseInt(b.getString("turnduration"));
        	        System.out.println("Juan: Found game Duration: "+settingsGameDuration);
        	        System.out.println("Juan: Found turn Duration: "+settingsTurnDuration);
        		}
        		else{
        			System.out.println("Juan: Intent null");
        		}
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mRobot != null)
        {
            StabilizationCommand.sendCommand(mRobot, true);
            FrontLEDOutputCommand.sendCommand(mRobot, 0f);
            SetDataStreamingCommand.sendCommand(mRobot, 0, 0, 0, 0);
            RobotProvider.getDefaultProvider().disconnectControlledRobots();
        }
    }
}
