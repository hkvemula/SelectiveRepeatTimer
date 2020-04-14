package com.srtimer;
import java.util.Timer;
import java.util.TimerTask;

public class BufferSlotTimer {
	public final static int EMPTY = 0;
	// This is performed by sender
	public final static int SENT = 1; 
	public final static int ACKED = 2; 
	// This is performed by receiver
	public final static int BUFFERED = 3; 
	public final static int RECEIVED = 4; 
        
        private boolean FASTER;
	private int mState;
	
	public final static int SEC_TIMEOUT = 10;
	public final static int SEC_DELIVERY = 3;
	public final static int MSEC_ANIM = 100;
        private int animationTime;
	public Timer mTimerTimeout = null;
	public Timer mTimerAnimation = null;
	
	public BufferSlotTimer() { 
		mState = BufferSlotTimer.EMPTY; 
                animationTime = 100;
                FASTER = false;
	}
	
	public int getState() { return mState; }
	public void setState(int state) { mState = state; } 
        
        public int getAnimation() { return animationTime; }
	public void setAnimation(int state) { animationTime = state; } 
        
        public boolean getFasterState() { return FASTER; }
	public void setFasterState(boolean p) { FASTER = p; } 
	
	/**
	 * Timer Starts
	 * @param onTimeout
	 */
	public void startTimerTimeout(TimerTask onTimeout) {
		if(mTimerTimeout != null) { stopTimerTimeout(); }
		mTimerTimeout = new Timer();
		mTimerTimeout.schedule(onTimeout, SEC_TIMEOUT * 1000);
	}
	
	public void startTimerAnimation(TimerTask onTimeAnimate) {
		if(mTimerAnimation != null) { stopTimerAnimation(); }
		mTimerAnimation = new Timer();
		mTimerAnimation.scheduleAtFixedRate(onTimeAnimate, 0, animationTime);
	}
    
	/**
	 * Timer stops
	 */
	public void stopTimerTimeout() { if(mTimerTimeout != null) { mTimerTimeout.cancel(); } }

	    
	public void stopTimerAnimation() { if(mTimerAnimation != null) { mTimerAnimation.cancel(); } }
	
} 

