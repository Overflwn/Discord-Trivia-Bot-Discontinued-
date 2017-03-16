package com.piorjade.triviabot;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.permissions.Role;
import de.btobastian.javacord.listener.message.*;

/**
 * 
 * 
 * This class / listener is specifically made for the owner of the bot to execute commands.
 * It only listens to commands executed by the users in the specified role, in the specified server and channel.
 * 
 * @author Patrick Swierzy (Piorjade)
 *
 */

public class MessageListener implements MessageCreateListener, MessageEditListener, MessageDeleteListener {

	static String turn = null;
	static boolean asking = false;
	
	boolean isRole(String id)
	{
		Collection<Role> roles;
		try {
			roles = MainClass.getBot().getAPI().getUserById(id).get().getRoles(MainClass.getBot().getAPI().getServerById(String.valueOf(config.serverID)));
			for (int i = 0; i < roles.size(); i++)
			{
				if (roles.iterator().hasNext() && roles.iterator().next().getName().equals(config.adminRole))
				{
					return true;
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("Couldn't get user by ID: " + id);
		}
		
		return false;
	}
	
	boolean isServer(String id)
	{
		if (id.equals(String.valueOf(config.serverID)))
		{
			return true;
		} else
		{
			return false;
		}
	}
	
	boolean isChannel(String id)
	{
		if (id.equals(String.valueOf(config.channel)))
		{
			return true;
		} else
		{
			return false;
		}
	}
	
	public void onMessageCreate(DiscordAPI arg0, Message arg1) {
		// TODO Auto-generated method stub
		
		if(!MainClass.getBot().needReboot && isChannel(arg1.getChannelReceiver().getId()) && isServer(arg1.getChannelReceiver().getServer().getId()) || config.notBound)
		{
			
			System.out.println("GOT COMMAND: " + arg1.getContent());
			String suffix = arg1.getContent().substring(1);
			
			if(arg1.getContent().startsWith("!") && isRole(arg1.getAuthor().getId())) {
				
				if (suffix.equalsIgnoreCase("resetTimerA")) {
					//restarts the timeout timer
					MainClass.getBot().resetTimer("a");
				}
				
				if (suffix.equalsIgnoreCase("resetTimerB")) {
					MainClass.getBot().resetTimer("b");
				}
				
				if (suffix.equalsIgnoreCase("reconnect")) {
					arg1.reply("Reconnecting....");
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						arg1.reply("Ohoh... something terrible happened, shutting down...");
						System.exit(1);
					}
					MainClass.getBot().reconnect();
				}
				
				if(suffix.equalsIgnoreCase("stop")) {
					if(MainClass.getBot().getQA().running)
					{
						arg1.reply("Stopping.");
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							arg1.reply("Ohoh... something terrible happened, shutting down...");
							System.exit(1);
						}
						MainClass.getBot().getQA().setOn(false);
					} else
					{
						arg1.reply("Already stopped.");
					}
				}
				
				if(suffix.equalsIgnoreCase("start"))
				{
					if(!MainClass.getBot().getQA().running)
					{
						arg1.reply("Starting.");
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							arg1.reply("Ohoh... something terrible happened, shutting down...");
							System.exit(1);
						}
						MainClass.getBot().getQA().setOn(true);
					} else
					{
						arg1.reply("Already running.");
					}
				}
				
				if(suffix.equalsIgnoreCase("shutdown"))
				{
					arg1.reply("Shutting system down...");
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						arg1.reply("Ohoh... something terrible happened, shutting down...");
						System.exit(1);
					}
					MainClass.getBot().shutdown();
				}
				
				if(suffix.equalsIgnoreCase("restart"))
				{
					if(MainClass.getBot().getQA().running)
					{
						arg1.reply("Stopping.");
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							arg1.reply("ERROR: Couldn't let the thread sleep! Shutting down...");
							System.exit(1);
						}
						MainClass.getBot().getQA().setOn(false);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
							arg1.reply("ERROR: Couldn't let the thread sleep! Shutting down...");
							System.exit(1);
						}
						arg1.reply("Starting.");
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							arg1.reply("ERROR: Couldn't let the thread sleep! Shutting down...");
							System.exit(1);
						}
						MainClass.getBot().getQA().setOn(true);
					} else
					{
						arg1.reply("Starting.");
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							arg1.reply("ERROR: Couldn't let the thread sleep! Shutting down...");
							System.exit(1);
						}
						MainClass.getBot().getQA().setOn(true);
					}
				}
				
				if(suffix.equalsIgnoreCase("timera"))
				{
					arg1.reply(String.valueOf(MainClass.getBot().getTimer("a").current));
					arg1.reply(String.valueOf(MainClass.getBot().timerA.running));
				}
				
				if(suffix.equalsIgnoreCase("timerb"))
				{
					arg1.reply(String.valueOf(MainClass.getBot().getTimer("b").current));
					arg1.reply(String.valueOf(MainClass.getBot().timerB.running));
				}
				
				
			}
			if(arg1.getContent().startsWith("!") && suffix.equalsIgnoreCase("help"))
			{
				arg1.reply("Commands (System): help, start, stop, resetTimerA, resetTimerB, restart, shutdown");
				arg1.reply("Commands (Question System): me, bp @NAME, openBp, ta [answer], next, turn, status @NAME, join, yes @NAME, categories, category [name]");
			}
			
			if(arg1.getContent().startsWith("!") && !isRole(arg1.getAuthor().getId()) && !suffix.equalsIgnoreCase("help"))
			{
				arg1.reply("You're not allowed to do that.");
			}
		}
	}
	


	public void onMessageDelete(DiscordAPI arg0, Message arg1) {
		// TODO Auto-generated method stub
		
	}

	public void onMessageEdit(DiscordAPI arg0, Message arg1, String arg2) {
		// TODO Auto-generated method stub
		
	}

}
