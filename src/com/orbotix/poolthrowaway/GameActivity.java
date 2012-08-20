package com.orbotix.poolthrowaway;

import orbotix.robot.app.StartupActivity;
import orbotix.robot.base.Robot;
import orbotix.robot.base.RobotProvider;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class GameActivity extends Activity {

	private Robot mRobot = null;
	
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
    }
    
    @Override
    public void onStart()
    {
    	super.onStart();
    	if (mRobot != null)
    	{
    	    Game game = new Game(mRobot.getUniqueId(), 60);
    	    game.startGame();	
    	}
		
        Button startBtn = (Button) findViewById(R.id.PlayAgainButton);
        startBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	//if (game.isFinished())
            	//{
            		//Intent intent = new Intent(view.getContext(), GameActivity.class);
            		//startActivity(intent);
            		//finish();	
            	//}
            }
        });
    }
}
