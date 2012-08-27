package com.orbotix.poolthrowaway;

import orbotix.macro.RGB;

public class Team {

	private int score_ = 0;
	public int r_;
	public int g_;
	public int b_;
	public String teamName_ = null;


	public Team (int r, int g, int b, String name)
	{
		score_ = 0;
		r_ = r;
		g_ = g;
		b_ = b;
		teamName_ = name;
	}
	
	public int getScore()
	{
		return score_;
	}
	
	public int getIncrementedScore()
	{
		++score_;
		return score_;
	}

	public RGB getColor()
	{
		return new RGB(r_, g_, b_, 0);
	}
	
	public String getTeamName()
	{
		return teamName_;
	}
	
}