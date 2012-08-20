package com.orbotix.poolthrowaway;

import java.util.Date;
import java.util.List;

import orbotix.macro.Delay;
import orbotix.macro.MacroObject;
import orbotix.macro.MacroObject.MacroObjectMode;
import orbotix.macro.RGB;
import orbotix.macro.RawMotor;
import orbotix.robot.base.DeviceAsyncData;
import orbotix.robot.base.DeviceMessenger;
import orbotix.robot.base.DeviceSensorsAsyncData;
import orbotix.robot.base.RGBLEDOutputCommand;
import orbotix.robot.base.RawMotorCommand;
import orbotix.robot.base.Robot;
import orbotix.robot.base.RobotProvider;
import orbotix.robot.base.SetDataStreamingCommand;
import orbotix.robot.base.StabilizationCommand;
import orbotix.robot.sensor.AccelerometerData;
import orbotix.robot.sensor.AttitudeData;
import orbotix.robot.sensor.DeviceSensorsData;
import android.app.Activity;
import android.widget.TextView;

public class Game extends Activity {

	private Robot mRobot = null;
	private MacroObject pulseMacroWinner;
	private Team blueTeam, redTeam, currentTeam;
	private long gameLengthInSeconds_;
	
	private static long MILLIS_PER_SECOND = 1000;
    /**
     * Data Streaming Packet Counts
     */
    private final static int TOTAL_PACKET_COUNT = 200;
    private final static int PACKET_COUNT_THRESHOLD = 50;
    private int mPacketCounter;
    
    /**
     * AsyncDataListener that will be assigned to the DeviceMessager, listen for streaming data, and update the score
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
                        		incrementCurrentTeam();
                        		TextView shakesText = (TextView) findViewById(R.id.ShakesValueBlue);
                        		shakesText.setText("" + getBlueScore());
                        		TextView shakesText2 = (TextView) findViewById(R.id.ShakesValueRed);
                        		shakesText2.setText("" + getRedScore());
                        	}
                        }
                    }
                }
            }
        }
    };
	

	public Game (String robotId, int gameLength) {
        mRobot = RobotProvider.getDefaultProvider().findRobot(robotId);
		gameLengthInSeconds_ = gameLength;
		
		blueTeam = new Team(0, 0, 255);
		redTeam = new Team(255, 0, 0);
		currentTeam = redTeam; // TODO: Randomize
        
        //Set the AsyncDataListener that will process each response.
        DeviceMessenger.getInstance().addAsyncDataListener(mRobot, mDataListener);

        StabilizationCommand.sendCommand(mRobot, false);		
	}

	private MacroObject makePulseMacro(Robot robot, RGB color)
	{
		MacroObject m = new MacroObject();
		m.setRobot(robot);
		m.addCommand(color);
		m.addCommand(new RawMotor(RawMotor.DriveMode.FORWARD, 255,
				RawMotor.DriveMode.FORWARD, 255, 0));
		m.addCommand(new Delay(5000));
		m.addCommand(new RawMotor(RawMotor.DriveMode.FORWARD, 0,
				RawMotor.DriveMode.FORWARD, 0, 0));
		m.setMode(MacroObjectMode.Normal);
		return m;
	}
	
	public void startGame() {		
		currentTeam = redTeam;
		Date startTime = new Date();
		Date currentTime = new Date();
		
		while ((currentTime.getTime() - startTime.getTime()) < (gameLengthInSeconds_ * MILLIS_PER_SECOND))
		{
			//Spin to signify turn change
			RGBLEDOutputCommand.sendCommand(mRobot, 255, 255, 255);
			RawMotorCommand.sendCommand(mRobot, RawMotorCommand.MOTOR_MODE_FORWARD, 255, RawMotorCommand.MOTOR_MODE_FORWARD, 255);
			
			try {
				Thread.sleep(5 * MILLIS_PER_SECOND);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			RawMotorCommand.sendCommand(mRobot, RawMotorCommand.MOTOR_MODE_FORWARD, 0, RawMotorCommand.MOTOR_MODE_FORWARD, 0);			
			//Display current team's color

			RGBLEDOutputCommand.sendCommand(mRobot, currentTeam.r_, currentTeam.g_, currentTeam.b_);
			
			try {
				Thread.sleep(5 * MILLIS_PER_SECOND);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			//Change teams
			if (currentTeam == redTeam)
				currentTeam = blueTeam;
			else
				currentTeam = redTeam;

			currentTime = new Date();
		}
		blinkEndGame();
	}

	private Team getWinner() {
		if (redTeam.getScore() > blueTeam.getScore())
			return redTeam;
		else
			return blueTeam;
	}
	
	public void incrementCurrentTeam()
	{
		currentTeam.getIncrementedScore();
	}
	
	public int getBlueScore()
	{
		System.out.println(blueTeam.getScore());
		return blueTeam.getScore();
	}
	
	public int getRedScore()
	{
		System.out.println(redTeam.getScore());
		return redTeam.getScore();
	}

	/**
	 * Causes the robot to blink once every second.
	 * 
	 * @param lit
	 */
	private void blinkEndGame() {
		if (mRobot != null) {
			pulseMacroWinner = makePulseMacro(mRobot, getWinner().getColor());
			pulseMacroWinner.playMacro();
		}
		DeviceMessenger.getInstance().removeAsyncDataListener(mRobot, mDataListener);
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