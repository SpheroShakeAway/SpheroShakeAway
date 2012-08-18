package com.orbotix.streamingexample;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import orbotix.macro.Delay;
import orbotix.macro.MacroObject;
import orbotix.macro.RGB;
import orbotix.macro.RawMotor;
import orbotix.macro.MacroObject.MacroObjectMode;
import orbotix.robot.base.RGBLEDOutputCommand;
import orbotix.robot.base.RawMotorCommand;
import orbotix.robot.base.Robot;

public class ChangeScoring {

	private Robot mRobot = null;
	private boolean colorState = true; 
	
	private MacroObject pulseMacro;
	
	public ChangeScoring(Robot robot){
		mRobot = robot;
		
		pulseMacro = new MacroObject();
		pulseMacro.setRobot(mRobot);
		pulseMacro.addCommand(new RGB(0, 0, 0, 0));
		pulseMacro.addCommand(new RawMotor(RawMotor.DriveMode.FORWARD, 255, RawMotor.DriveMode.FORWARD, 255,0));
		pulseMacro.addCommand(new Delay(5000));
		pulseMacro.addCommand(new RawMotor(RawMotor.DriveMode.FORWARD, 0, RawMotor.DriveMode.FORWARD, 0, 0));
		pulseMacro.setMode(MacroObjectMode.Normal);
		
	}
	
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

		    public void changeTeams() {
		        final Runnable beeper = new Runnable() {
		                public void run() { 
		                	System.out.println("Sphero: Change Team2");
		                	pulseMacro.playMacro();		                	
		                	if(colorState){
		                		RGBLEDOutputCommand.sendCommand(mRobot, 0, 0, 255);
		                		colorState = false;
		                	}
		                	else{
		                		RGBLEDOutputCommand.sendCommand(mRobot, 255, 0, 0);
		                		colorState = true;
		                	}
		                }
		            };
		            
		        final ScheduledFuture<?> beeperHandle =
		            scheduler.scheduleAtFixedRate(beeper, 10, 10, TimeUnit.SECONDS);
		        scheduler.schedule(new Runnable() {
		                public void run() { beeperHandle.cancel(true); }
		            }, 60 * 60, TimeUnit.SECONDS);
		    }
}