package com.orbotix.streamingexample;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import android.os.Handler;

import orbotix.macro.Delay;
import orbotix.macro.MacroObject;
import orbotix.macro.RGB;
import orbotix.macro.RawMotor;
import orbotix.macro.MacroObject.MacroObjectMode;
import orbotix.robot.base.DeviceMessenger;
import orbotix.robot.base.RGBLEDOutputCommand;
import orbotix.robot.base.RawMotorCommand;
import orbotix.robot.base.Robot;

public class ChangeScoring {

	private Robot mRobot = null;
	private boolean colorState = true;
	private DeviceMessenger.AsyncDataListener listener = null;
	private MacroObject pulseMacro;
	private int shakesCount, shakesCount2 = 0;
	
	public ChangeScoring(Robot robot, DeviceMessenger.AsyncDataListener hear) {
		mRobot = robot;
		listener = hear;

		pulseMacro = new MacroObject();
		pulseMacro.setRobot(mRobot);
		pulseMacro.addCommand(new RGB(0, 0, 0, 0));
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
					RGBLEDOutputCommand.sendCommand(mRobot, 0, 0, 255);
					colorState = false;
				} else {
					RGBLEDOutputCommand.sendCommand(mRobot, 255, 0, 0);
					colorState = true;
				}
			}
		};

		final ScheduledFuture<?> beeperHandle = scheduler.scheduleAtFixedRate(
				beeper, 10, 10, TimeUnit.SECONDS);
		
		scheduler.schedule(new Runnable() {
			public void run() {
				
				beeperHandle.cancel(true);
			
			}
		},  1 * 60, TimeUnit.SECONDS);
	}

	public void initializeGame() {
		// TODO Auto-generated method stub
		RGBLEDOutputCommand.sendCommand(mRobot, 255, 255, 255);

		pulseMacro.playMacro();

	}
	
    /**
     * Causes the robot to blink once every second.
     * @param lit
     */
    private void blinkEndGame(final boolean lit){
    	DeviceMessenger.getInstance().removeAsyncDataListener(mRobot, listener);
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
                    blinkEndGame(!lit);
                }
            }, 1000);
        }
    }

	public int getShakesCount() {
		return shakesCount;
	}

	public void setShakesCount(int shakesCount) {
		this.shakesCount = shakesCount;
	}

	public int getShakesCount2() {
		return shakesCount2;
	}

	public void setShakesCount2(int shakesCount2) {
		this.shakesCount2 = shakesCount2;
	}
}