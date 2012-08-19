package com.orbotix.streamingexample;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import orbotix.macro.Delay;
import orbotix.macro.MacroObject;
import orbotix.macro.MacroObject.MacroObjectMode;
import orbotix.macro.RGB;
import orbotix.macro.RawMotor;
import orbotix.robot.base.DeviceMessenger;
import orbotix.robot.base.RGBLEDOutputCommand;
import orbotix.robot.base.Robot;

public class ChangeScoring {

	private Robot mRobot = null;
	private boolean colorState = true;
	private DeviceMessenger.AsyncDataListener listener = null;
	private MacroObject pulseMacro, pulseMacroWinner;
	private int shakesCountRed, shakesCountBlue = 0;
	private int BLUE_TEAM=2, RED_TEAM=1;
	private boolean gameFinished = false;

	public ChangeScoring(Robot robot, DeviceMessenger.AsyncDataListener hear) {
		mRobot = robot;
		listener = hear;

		pulseMacro = new MacroObject();
		pulseMacro.setRobot(mRobot);
		pulseMacro.addCommand(new RGB(255, 255, 255, 0));
		pulseMacro.addCommand(new RawMotor(RawMotor.DriveMode.FORWARD, 255,
				RawMotor.DriveMode.FORWARD, 255, 0));
		pulseMacro.addCommand(new Delay(5000));
		pulseMacro.addCommand(new RawMotor(RawMotor.DriveMode.FORWARD, 0,
				RawMotor.DriveMode.FORWARD, 0, 0));
		pulseMacro.setMode(MacroObjectMode.Normal);
	}

	public int getTeamScoring() {
		return (colorState ? 1 : 2);
	}

	private final ScheduledExecutorService scheduler = Executors
			.newScheduledThreadPool(1);

	public void changeTeamRotationInterval() {
		final Runnable beeper = new Runnable() {
			public void run() {
				System.out.println("Sphero: Change Team2");
				pulseMacro.playMacro();
				if (colorState) {
					if(!gameFinished){
					RGBLEDOutputCommand.sendCommand(mRobot, 0, 0, 255);
					}
					colorState = false;
				} else {
					if(!gameFinished){
					RGBLEDOutputCommand.sendCommand(mRobot, 255, 0, 0);
					}
					colorState = true;
				}
			}
		};

		final ScheduledFuture<?> beeperHandle = scheduler.scheduleAtFixedRate(
				beeper, 10, 10, TimeUnit.SECONDS);

		scheduler.schedule(new Runnable() {
			public void run() {

				beeperHandle.cancel(true);
				blinkEndGame(false, getWinner());

			}

		}, 1 * 60, TimeUnit.SECONDS);
	}

	private int getWinner() {
		// TODO Auto-generated method stub
		if (this.shakesCountRed > this.shakesCountBlue)
			return RED_TEAM;
		else
			return BLUE_TEAM;
	}

	public void initializeGame() {
		// TODO Auto-generated method stub
		RGBLEDOutputCommand.sendCommand(mRobot, 255, 255, 255);

		pulseMacro.playMacro();

	}

	/**
	 * Causes the robot to blink once every second.
	 * 
	 * @param lit
	 */
	private void blinkEndGame(final boolean lit, final int winningTeam) {
		DeviceMessenger.getInstance().removeAsyncDataListener(mRobot, listener);
		RGBLEDOutputCommand.sendCommand(mRobot, 0, 255, 0);
		System.out.println("Blink End Game Called");
		gameFinished = true;
		if (mRobot != null) {

			// If not lit, send command to show blue light, or else, send
			// command to show no light
			if(winningTeam == BLUE_TEAM){
				pulseMacroWinner = new MacroObject();
				pulseMacroWinner.setRobot(mRobot);
				pulseMacroWinner.addCommand( new RGB(0, 0, 255, 0));
				pulseMacroWinner.addCommand(new RawMotor(RawMotor.DriveMode.FORWARD, 255,
						RawMotor.DriveMode.FORWARD, 255, 0));
				pulseMacroWinner.addCommand(new Delay(5000));
				
				RGBLEDOutputCommand.sendCommand(mRobot, 0, 0, 255);
			}
			if (winningTeam == RED_TEAM){
				pulseMacroWinner = new MacroObject();
				pulseMacroWinner.setRobot(mRobot);
				pulseMacroWinner.addCommand( new RGB(255, 0, 0, 0));
				pulseMacroWinner.addCommand(new RawMotor(RawMotor.DriveMode.FORWARD, 255,
						RawMotor.DriveMode.FORWARD, 255, 0));
				pulseMacroWinner.addCommand(new Delay(5000));

				RGBLEDOutputCommand.sendCommand(mRobot, 255, 0, 0);
			}

			pulseMacroWinner.playMacro();
		}
	}

	public int incShakesCountRed() {
		return ++shakesCountRed;
	}

	public int incShakesCountBlue() {
		return ++shakesCountBlue;
	}
}