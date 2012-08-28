package com.orbotix.poolthrowaway;

import java.util.Date;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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

import android.os.AsyncTask;
import android.os.Handler;
import android.content.Context;

public class Game {

	private Robot mRobot = null;
	private MacroObject blinkMacroWinner, pulseMacroChange;
	private Team blueTeam, redTeam, currentTeam;
	private long gameLengthInSeconds_, gameTurnLengthInSeconds_;
	private boolean gameFinishedFlag = false;
	private TextView shakesBlueText, shakesRedText, timerText;

	/**
	 * Data Streaming Packet Counts
	 */
	private final static int TOTAL_PACKET_COUNT = 200;
	private final static int PACKET_COUNT_THRESHOLD = 50;
	private int mPacketCounter;
	private int timeLeft = 0;
	
	/*
	 * Schedulers
	 */
	private final ScheduledExecutorService scheduler = Executors
			.newScheduledThreadPool(2);
	private final ScheduledExecutorService scheduler2 = Executors
			.newScheduledThreadPool(2);

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
								shakesRedText.setText(""+ getBlueScore());
								shakesBlueText.setText(""+ getRedScore());						
							}
						}
					}
				}
			}
			timerText.setText(""+timeLeft);
		}
	};

	public Game (TextView blueText, TextView redText, TextView timerTextEntry, String robotId, int gameLength, int turnLength) {
		mRobot = RobotProvider.getDefaultProvider().findRobot(robotId);
		gameLengthInSeconds_ = gameLength;
		gameTurnLengthInSeconds_ = turnLength;
		timeLeft = gameLength;
		
		blueTeam = new Team(0, 0, 255, "Blue Team");
		redTeam = new Team(255, 0, 0, "Red Team");
		currentTeam = redTeam; // TODO: Randomize

		pulseMacroChange = makePulseMacro(mRobot, new RGB(255, 255, 255, 0));

		//Set the AsyncDataListener that will process each response.
		DeviceMessenger.getInstance().addAsyncDataListener(mRobot, mDataListener);
		
		shakesBlueText = blueText;
		shakesRedText = redText;
		timerText = timerTextEntry;
		
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

	public boolean isFinished()
	{
		return gameFinishedFlag;
	}
	
	public void startGame() {
		
		requestDataStreaming();
		
		final Runnable beeper = new Runnable() {
			public void run() {
				
				if(mRobot == null){
					//TODO Add reconnection logic										
					System.out.println("Juan: null robot Game logic broken!");
					return;
				}

				pulseMacroChange.playMacro();							

				//Display current team's color
				if(!isFinished()){
					RGBLEDOutputCommand.sendCommand(mRobot, currentTeam.r_, currentTeam.g_, currentTeam.b_);
				}
				
				//Change teams
				if (currentTeam == redTeam)
					currentTeam = blueTeam;
				else
					currentTeam = redTeam;
			}
		};
		
		final Runnable beeper2 = new Runnable() {
			public void run() {
				decreaseTimer();
			}
		};

		final ScheduledFuture<?> beeperHandle = getScheduler().scheduleAtFixedRate(
				beeper, 0, gameTurnLengthInSeconds_, TimeUnit.SECONDS);

		final ScheduledFuture<?> beeperHandle2 = getScheduler2().scheduleAtFixedRate(
				beeper2, 0, 1, TimeUnit.SECONDS);

		getScheduler().schedule(new Runnable() {
			public void run() {		
				beeperHandle.cancel(true);
				beeperHandle2.cancel(true);
				blinkEndGame();
			}

		}, 1 * gameLengthInSeconds_, TimeUnit.SECONDS);
	}
	
	private void decreaseTimer(){
		timeLeft = timeLeft - 1;
	}
	
	private Team getWinner() {
		if (redTeam.getScore() < blueTeam.getScore())
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
		gameFinishedFlag = true;
		DeviceMessenger.getInstance().removeAsyncDataListener(mRobot, mDataListener);
	
		Team winning = getWinner();
		RGBLEDOutputCommand.sendCommand(mRobot, winning.r_, winning.g_, winning.b_);
		
		timerText.setText(winning.getTeamName() + " WINS!");
//		if (mRobot != null) {
//			blink(false);
//		}
	}

	/**
     * Causes the robot to blink once every second.
     * @param lit
     */
    private void blink(final boolean lit){
        
        if(mRobot != null){
            
            //If not lit, send command to show blue light, or else, send command to show no light
            if(lit){
                RGBLEDOutputCommand.sendCommand(mRobot, 0, 0, 0);
            }else{
                RGBLEDOutputCommand.sendCommand(mRobot, 0, 0, 255);
            }
            
            //Send delayed message on a handler to run blink again
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    blink(!lit);
                }
            }, 1000);
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
		else{
			//TODO handle not listening error: null robot
			System.out.println("Juan: Cannot listen to async stream.");
		}
	}

	public ScheduledExecutorService getScheduler() {
		return scheduler;
	}

	public ScheduledExecutorService getScheduler2() {
		return scheduler2;
	}
	
}