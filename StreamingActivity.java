package com.orbotix.streamingexample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
    private ImuView mImuView;
    private CoordinateView mAccelerometerFilteredView;
    private ShakesView mShakeFilteredView;
    private int shakesCount, shakesCount2 = 0;
    private ChangeScoring scoring;

    public int getTeam1Score(){
    	return shakesCount;
    }
    
    public int getTeam2Score(){
    	return shakesCount2;
    }
    
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
                        if(attitude != null){
                            mImuView.setPitch("" + attitude.getAttitudeSensor().pitch);
                            mImuView.setRoll("" + attitude.getAttitudeSensor().roll);
                            mImuView.setYaw("" + attitude.getAttitudeSensor().yaw);
                        }

                        //Show accelerometer data
                        AccelerometerData accel = datum.getAccelerometerData();
                        if(attitude != null){
                        	double shakesThreshold = accel.getFilteredAcceleration().x + accel.getFilteredAcceleration().y + accel.getFilteredAcceleration().z;
                        	
                        	if(shakesThreshold > 4.0){
                        		if(scoring.getTeamScoring() == 1){
                        			shakesCount += 1;
                        			mShakeFilteredView.setShakesCount(""+shakesCount);
                        		}
                        		else{
                        			shakesCount2 += 1;
                        			mShakeFilteredView.setShakesCount2(""+shakesCount2);
                        		}
                        		//System.out.println("Shakes: "+shakesCount);                        		
                        	}
                                                                	
                            mAccelerometerFilteredView.setX("" + accel.getFilteredAcceleration().x);
                            mAccelerometerFilteredView.setY("" + accel.getFilteredAcceleration().y);
                            mAccelerometerFilteredView.setZ("" + accel.getFilteredAcceleration().z);
                        }
                    }
                }
            }
        }
    };

    //1000*60*60*24
    private final static long TIMER_CHANGE = 1000*60*60*24;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        //perform the task once a day at 4 a.m., starting tomorrow morning
        //(other styles are possible as well)

        //Get important views
        mImuView = (ImuView)findViewById(R.id.imu_values);
        mAccelerometerFilteredView = (CoordinateView)findViewById(R.id.accelerometer_filtered_coordinates);
        mShakeFilteredView = (ShakesView)findViewById(R.id.shakes_coordinate);
                
        //Show the StartupActivity to connect to Sphero
        startActivityForResult(new Intent(this, StartupActivity.class), sStartupActivity);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){

            if(requestCode == sStartupActivity){

                //Get the Robot from the StartupActivity
                String id = data.getStringExtra(StartupActivity.EXTRA_ROBOT_ID);
                mRobot = RobotProvider.getDefaultProvider().findRobot(id);

                requestDataStreaming();
                
                
                scoring = new ChangeScoring(mRobot, mDataListener);
                
                scoring.initializeGame();
                
                scoring.changeTeamRotationInterval();

//                RGBLEDOutputCommand.sendCommand(mRobot, 255, 0, 0);

                //Set the AsyncDataListener that will process each response.
                DeviceMessenger.getInstance().addAsyncDataListener(mRobot, mDataListener);

                StabilizationCommand.sendCommand(mRobot, false);
                
                FrontLEDOutputCommand.sendCommand(mRobot, 1f);

                
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

