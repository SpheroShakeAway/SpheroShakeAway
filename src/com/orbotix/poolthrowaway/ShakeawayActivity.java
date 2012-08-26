package com.orbotix.poolthrowaway;

import orbotix.robot.app.StartupActivity;
import orbotix.robot.base.FrontLEDOutputCommand;
import orbotix.robot.base.Robot;
import orbotix.robot.base.RobotProvider;
import orbotix.robot.base.SetDataStreamingCommand;
import orbotix.robot.base.StabilizationCommand;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ShakeawayActivity extends Activity
{
    /**
     * ID for starting the StartupActivity
     */
    private final static int sStartupActivity = 0;
    public final static int RESULT_RELOAD_GAME = 10;
    /**
     * Robot to from which we are streaming
     */
    private Robot mRobot = null;

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
    		intent.putExtras(b); //Put your id to your next Intent	
    		System.out.println("Juan: Craeted bundle");
		}
		startActivityForResult(intent, 10);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

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
