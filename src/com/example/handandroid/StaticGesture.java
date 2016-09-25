package com.example.handandroid;
import java.util.ArrayList;

import org.bytedeco.javacpp.opencv_core.*;


public class StaticGesture {
	public static final int NONE=0;
	public static final int READY=1;
	public static final int PRESSED=2;
	public static final int BLOOM=3;
	private int state=0;
	private int indexFingerMax=0;
	private int indexFingerMin=1000;
	private int indexFingerLen=0;
	
	private int fingerNumber=0;
	
	private float ratio=1.0f;
	
	private Point tip;
	
	public StaticGesture()
	{
		tip=new Point();
	}
	
	public void update(Point cog, ArrayList<Point> fingerTips,int radius)
	{
		fingerNumber=fingerTips.size();
		if(fingerNumber==2)fingerNumber=3;
		if(fingerNumber==1)
		{
			indexFingerLen=dist(fingerTips.get(0),cog);
			if(indexFingerLen<indexFingerMin)indexFingerMin=indexFingerLen;
			if(indexFingerLen>indexFingerMax)indexFingerMax=indexFingerLen;
			ratio=0.75f*indexFingerMax/indexFingerLen;
			getTip(fingerTips.get(0),cog);
			int dif=indexFingerMax-radius;
			int threshold=radius+dif*2/3;
			if(indexFingerLen<threshold)fingerNumber=2;
		}
		
		switch (fingerNumber)
		{
		case 1: state=READY; break;
		case 2: state=PRESSED; break;
		case 5: state=BLOOM; break;	
		default: state=NONE; break;
		}
	}
	
	private int dist(Point u,Point v)
	{
		return (int) Math.sqrt(
				((u.x()-v.x())*(u.x()-v.x())
				+(u.y()-v.y())*(u.y()-v.y())));
	}
	
	private void getTip(Point finger,Point cog)
	{
		tip.x((int) (ratio*(finger.x()-cog.x())+cog.x()));
		tip.y((int) (ratio*(finger.y()-cog.y())+cog.y()));
	}
	
	public int getGesture()
	{
		return state;
	}
	
	public Point getTipPostion()
	{
		if(state!=READY&&state!=PRESSED)return null;
		return tip;
	}

}
