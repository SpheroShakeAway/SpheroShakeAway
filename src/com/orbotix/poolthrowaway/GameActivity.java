package com.orbotix.poolthrowaway;

import orbotix.robot.app.StartupActivity;
import orbotix.robot.base.Robot;
import orbotix.robot.base.RobotProvider;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class GameActivity extends Activity {

	private Robot mRobot = null;
	private Game game = null;
	private int gDuration_ = 20;
	private int tDuration_ = 10;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_game);
        setCustomFonts();        
        
		Bundle b = getIntent().getExtras();
		if (b != null)
		{
			String robotId = b.getString(StartupActivity.EXTRA_ROBOT_ID);
			gDuration_ = b.getInt("gameduration");
			tDuration_ = b.getInt("turnduration");
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
    		Drawable dR = getResources().getDrawable(R.drawable.winner_blue);
    		Drawable dB = getResources().getDrawable(R.drawable.winner_red);
    	    game = new Game(
    	    		(View) this.findViewById(R.id.mainGameScreen),
    	    		dB, dR,
    	    		mRobot.getUniqueId(), 
    	    		gDuration_,
    	    		tDuration_);
    	    game.startGame();    	  
    	}
    	else{
    		//TODO handle null robot
    	}
		
//        Button playAgainBtn = (Button) findViewById(R.id.PlayAgainButton);
//        playAgainBtn.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View view) {
//            	if (game.isFinished())
//            	{            		
//            		Intent intent = new Intent();
//            		setResult(RESULT_OK, intent);
//            		endGame();
//            	}
//            }
//        });
        
        Button exitBtn = (Button) findViewById(R.id.ExitButton);
        exitBtn.setVisibility(View.INVISIBLE);
        exitBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	if (game.isFinished())
            	{            		
            		Intent intent = new Intent();
            		setResult(RESULT_CANCELED, intent);
            		endGame();
            	}
            }
        });
    }
    
    public void setCustomFonts()
    {
    	TextView txt = (TextView) findViewById(R.id.TimeValue);  
    	Typeface font = Typeface.createFromAsset(getAssets(), "sullivan_fill.otf");  
    	txt.setTypeface(font);
    	
    	txt = (TextView) findViewById(R.id.ShakesValueBlue);   
    	txt.setTypeface(font);
    	
    	txt = (TextView) findViewById(R.id.ShakesValueRed);  
    	txt.setTypeface(font);
    }
    
    public void endGame(){
    	finish();
    }
    
    @Override
    public void onStop(){
    	super.onStop();
    	game.getScheduler().shutdown();
    	setContentView(R.layout.main1);
    }

}
