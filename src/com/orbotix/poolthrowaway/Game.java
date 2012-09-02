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
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import android.os.AsyncTask;
import android.os.Handler;
import android.content.Context;
import android.graphics.drawable.Drawable;

public class Game {

	private Robot mRobot = null;
	private MacroObject blinkMacroWinner, pulseMacroChange;
	private Team blueTeam, redTeam, currentTeam;
	private long gameLengthInSeconds_, gameTurnLengthInSeconds_;
	private boolean gameFinishedFlag = false;
	private TextView shakesBlueText, shakesRedText, timerText;
	private View gameScreen_;
	private Drawable bWin, rWin;

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
			.newScheduledThreadPool(1);

	public Game (View gameScreen, Drawable blueWin, Drawable redWin, String robotId, int gameLength, int turnLength) {
		mRobot = RobotProvider.getDefaultProvider().findRobot(robotId);
		gameLengthInSeconds_ = gameLength;
		gameTurnLengthInSeconds_ = turnLength;
		timeLeft = gameLength;
		gameScreen_ = gameScreen;
		
		bWin = blueWin;
		rWin = redWin;
		blueTeam = new Team(0, 0, 255, "Blue Team");
		redTeam = new Team(255, 0, 0, "Red Team");
		currentTeam = redTeam; // TODO: Randomize

		pulseMacroChange = makePulseMacro(mRobot, new RGB(255, 255, 255, 0));

		//Set the AsyncDataListener that will process each response.
		DeviceMessenger.getInstance().addAsyncDataListener(mRobot, mDataListener);
		
		shakesBlueText = (TextView) gameScreen.findViewById(R.id.ShakesValueBlue);
		shakesRedText = (TextView) gameScreen.findViewById(R.id.ShakesValueRed);
		timerText = (TextView) gameScreen.findViewById(R.id.TimeValue);
		
		StabilizationCommand.sendCommand(mRobot, false);		
	}
	
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
			updateGameDurationDisplay();
			if(gameFinishedFlag){
				handleEndGame();
			}
		}
	};

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
				
				if( timeLeft % gameTurnLengthInSeconds_ == 0 && timeLeft >0){
					if(mRobot == null){
						//TODO Add reconnection logic										
						return;
					}

					//Display current team's color
					if(!isFinished()){
						pulseMacroChange.playMacro();
						RGBLEDOutputCommand.sendCommand(mRobot, currentTeam.r_, currentTeam.g_, currentTeam.b_);
					}
					
					//Change teams
					if (currentTeam == redTeam)
						currentTeam = blueTeam;
					else
						currentTeam = redTeam;
				}
				if(timeLeft > 0){
					decreaseTimer();
				}
				else{
					gameFinishedFlag = true;
				}
			}
		};	

		final ScheduledFuture<?> beeperHandle = getScheduler().scheduleAtFixedRate(
				beeper, 0, 1, TimeUnit.SECONDS);

		getScheduler().schedule(new Runnable() {
			public void run() {
				beeperHandle.cancel(true);
			}

		}, 1 * gameLengthInSeconds_, TimeUnit.SECONDS);
		System.out.println("Juan: game blocked?");
	}
	
	private void decreaseTimer(){
		timeLeft = timeLeft - 1;
	}
	
	private Team getWinner() {
		if (redTeam.getScore() > blueTeam.getScore())
		{
	  	  	gameScreen_.setBackgroundDrawable(rWin);
	  	  	gameScreen_.invalidate();
			return redTeam;
		}
		else{
	  	  	gameScreen_.setBackgroundDrawable(bWin);
	  	  	gameScreen_.invalidate();
			return blueTeam;
		}
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
	
	public void updateGameDurationDisplay()
	{
		int mins = timeLeft/60;
		int secs = timeLeft % 60;
		timerText.setText(mins +":"+secs);
	}

	/**
	 * Causes the robot to blink once every second.
	 * 
	 * @param lit
	 */
	private void handleEndGame() {
		scheduler.shutdownNow();
		
		Team winning = getWinner();
		//For some reason this needs to be inverted for correct winner match. Investigate!
		RGBLEDOutputCommand.sendCommand(mRobot, winning.b_, winning.g_, winning.r_);
  	  	System.out.println("Juan: Game ended!");
  	  	gameScreen_.findViewById(R.id.TimeValue).setVisibility(View.INVISIBLE);
  	  	gameScreen_.findViewById(R.id.ShakesValueBlue).setVisibility(View.INVISIBLE);
  	  	gameScreen_.findViewById(R.id.ShakesValueRed).setVisibility(View.INVISIBLE);
		gameScreen_.findViewById(R.id.ExitButton).setVisibility(View.VISIBLE);
		gameScreen_.invalidate();
		
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
		else{
			//TODO handle not listening error: null robot
			System.out.println("Juan: Cannot listen to async stream.");
		}
	}

	public ScheduledExecutorService getScheduler() {
		return scheduler;
	}
	
}