package com.orbotix.poolthrowaway;

import orbotix.robot.app.StartupActivity;
import orbotix.robot.base.Robot;
import orbotix.robot.base.RobotProvider;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class GameActivity extends Activity {

	private Robot mRobot = null;
	private Game game = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_game);
        
		Bundle b = getIntent().getExtras();
		if (b != null)
		{
			String robotId = b.getString(StartupActivity.EXTRA_ROBOT_ID);
	        mRobot = RobotProvider.getDefaultProvider().findRobot(robotId);	        
		}
		else{
			//TODO robot not initalized? quit
		}		
    }
    
    @Override
    public void onStart()
    {
    	super.onStart();
    	    	
    	if (mRobot != null)
    	{    		
    	    game = new Game((TextView) findViewById(R.id.ShakesValueBlue),
    	    		(TextView) findViewById(R.id.ShakesValueRed),
    	    		(TextView) findViewById(R.id.TimeValue),
    	    		mRobot.getUniqueId(), 
    	    		30);
    	    game.startGame();    	  
    	}
    	else{
    		//TODO handle null robot
    	}
		
        Button playAgainBtn = (Button) findViewById(R.id.PlayAgainButton);
        playAgainBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	if (game.isFinished())
            	{            		
            		Intent intent = new Intent();
            		setResult(RESULT_OK, intent);
            		endGame();
            	}
            }
        });
    }
    
    public void endGame(){
    	finish();
    }
    
    @Override
    public void onStop(){
    	super.onStop();
    	setContentView(R.layout.main1);
    }

}
