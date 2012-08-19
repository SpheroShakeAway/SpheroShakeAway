package com.orbotix.streamingexample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import orbotix.robot.app.StartupActivity;
import orbotix.robot.base.*;
import orbotix.robot.sensor.AccelerometerData;
import orbotix.robot.sensor.AttitudeData;
import orbotix.robot.sensor.DeviceSensorsData;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class StreamingActivity extends Activity
{
    /**
     * ID for starting the StartupActivity
     */
    private final static int sStartupActivity = 0;
    
    /**
     * Data Streaming Packet Counts
     */
    private final static int TOTAL_PACKET_COUNT = 200;
    private final static int PACKET_COUNT_THRESHOLD = 50;
    private int mPacketCounter;

    /**
     * Robot to from which we are streaming
     */
    private Robot mRobot = null;

    //The views that will show the streaming data
    private ChangeScoring scoring;

    /**
     * AsyncDataListener that will be assigned to the DeviceMessager, listen for streaming data, and then do the
     */
    private DeviceMessenger.AsyncDataListener mDataListener = new DeviceMessenger.AsyncDataListener() {
        @Override
        public void onDataReceived(DeviceAsyncData data) {
        	
            if(data instanceof DeviceSensorsAsyncData){

            	// If we are getting close to packet limit, request more
            	mPacketCounter++;
            	if( mPacketCounter > (TOTAL_PACKET_COUNT - PACKET_COUNT_THRESHOLD) ) {
            		requestDataStreaming();
            	}
            	
                //get the frames in the response
                List<DeviceSensorsData> data_list = ((DeviceSensorsAsyncData)data).getAsyncData();
                if(data_list != null){

                    //Iterate over each frame
                    for(DeviceSensorsData datum : data_list){

                        //Show attitude data
                        AttitudeData attitude = datum.getAttitudeData();

                        //Show accelerometer data
                        AccelerometerData accel = datum.getAccelerometerData();
                        if(attitude != null){
                        	double shakesThreshold = accel.getFilteredAcceleration().x + accel.getFilteredAcceleration().y + accel.getFilteredAcceleration().z;
                        	
                        	if(shakesThreshold > 4.0){
                        		if(scoring.getTeamScoring() == 1){
                        			TextView shakesText = (TextView) findViewById(R.id.ShakesValueRed);
                        			shakesText.setText("" + scoring.incShakesCountRed());
                        		}
                        		else{
                        			TextView shakesText = (TextView) findViewById(R.id.ShakesValueBlue);
                        			shakesText.setText("" + scoring.incShakesCountBlue());
                        		}
                        	}
                        }
                    }
                }
            }
        }
    };


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
                setContentView(R.layout.activity_game);
                
                //Set the AsyncDataListener that will process each response.
                DeviceMessenger.getInstance().addAsyncDataListener(mRobot, mDataListener);

                StabilizationCommand.sendCommand(mRobot, false);
                
                requestDataStreaming();
                
                scoring = new ChangeScoring(mRobot, mDataListener);
                scoring.initializeGame();
                scoring.changeTeamRotationInterval();
            }
        });
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
                
                //FrontLEDOutputCommand.sendCommand(mRobot, 1f);

                //DeviceMessenger.getInstance().removeAsyncDataListener(robot, dataListener)
                //RawMotorCommand.sendCommand(mRobot, RawMotorCommand.MOTOR_MODE_FORWARD, 255, RawMotorCommand.MOTOR_MODE_FORWARD, 255);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(mRobot != null){

            StabilizationCommand.sendCommand(mRobot, true);
            FrontLEDOutputCommand.sendCommand(mRobot, 0f);

            RobotProvider.getDefaultProvider().disconnectControlledRobots();
        }
    }

    private void requestDataStreaming() {

        if(mRobot != null){
        	
            // Set up a bitmask containing the sensor information we want to stream
            final long mask = SetDataStreamingCommand.DATA_STREAMING_MASK_ACCELEROMETER_FILTERED_ALL |
            				  SetDataStreamingCommand.DATA_STREAMING_MASK_IMU_ANGLES_FILTERED_ALL;

            // Specify a divisor. The frequency of responses that will be sent is 400hz divided by this divisor.
            final int divisor = 50;

            // Specify the number of frames that will be in each response. You can use a higher number to "save up" responses
            // and send them at once with a lower frequency, but more packets per response.
            final int packet_frames = 1;

            // Reset finite packet counter
            mPacketCounter = 0;
            
            // Count is the number of async data packets Sphero will send you before
            // it stops.  You want to register for a finite count and then send the command
            // again once you approach the limit.  Otherwise data streaming may be left
            // on when your app crashes, putting Sphero in a bad state 
            final int response_count = TOTAL_PACKET_COUNT;

            //Send this command to Sphero to start streaming
            SetDataStreamingCommand.sendCommand(mRobot, divisor, packet_frames, mask, response_count);
        }
    }
}
