package com.piorjade.triviabot;

/**
 * 
 * This class is a basic timer-thread.
 * You can specify the amount of seconds you want to let it wait.
 * Once it reached that amount, it finishes itself and you can
 * check that by accessing the Timer-class and checking whether
 * "finished" is true or false.
 * 
 * @author Patrick Swiery (Piorjade)
 *
 */

public class Timer implements Runnable {
	//This class just counts up to the specified amount of
	//seconds and then sets the finished marker to true
	public volatile boolean finished = false;
	volatile long start = 0;
	volatile boolean running = true;
	volatile long tEnd = 0;
	public volatile long current = 0;
	public volatile String nickname;
	public Timer(long amount, String nickname) {
		tEnd = amount;
		start = System.currentTimeMillis();
		this.nickname = nickname;
	}
	
	
	public synchronized void reset() {
		start = System.currentTimeMillis();
		finished = false;
		running = true;
	}
	
	public synchronized void reset(long amount) {
		start = System.currentTimeMillis();
		tEnd = amount;
		finished = false;
		running = true;
	}
	
	
	public synchronized void run() {
		
		while (running)
		{
			if(!finished && !MainClass.shouldWait())
			{
				current = (System.currentTimeMillis() / 1000) - (start / 1000);
				if (current >= tEnd) {
					finished = true;
				}
			}
			
			if(MainClass.shouldReset())
			{
				reset();
				MainClass.resetTimer = false;
			}
		}
	}
}
