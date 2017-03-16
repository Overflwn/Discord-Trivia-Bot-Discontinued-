package com.piorjade.triviabot;
/**
 * 
 * This class gets executed at start and holds the reset-flags for the timers.
 * 
 * @author Patrick Swierzy (Piorjade)
 *
 */
public class MainClass {
	public static Bot bot = new Bot();
	public static Thread botThread = new Thread(bot);
	public static volatile boolean resetA = false;
	public static volatile boolean resetB = false;
	public static void reboot()
	{
		//not used, increased memory and CPU usage
		bot = new Bot();
		botThread = new Thread(bot);
		botThread.run();
	}
	
	public static boolean resetTimer(String nickname)
	{
		if (nickname.equals("a"))
		{
			return resetA;
		} else if(nickname.equals("b"))
		{
			return resetB;
		}
		return false;
	}
	
	public static Bot getBot()
	{
		return bot;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		botThread.run();
		while (true)
		{
			if(bot.needReboot)
				reboot();
		}
		
	}

}
