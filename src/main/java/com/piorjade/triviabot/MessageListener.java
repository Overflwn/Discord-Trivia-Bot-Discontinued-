package com.piorjade.triviabot;
import java.util.Collection;
import java.util.Iterator;

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
	static boolean isRole(String id)
	{
		Collection<Role> roles;
		try {
			roles = MainClass.getAPI().getUserById(id).get().getRoles(MainClass.getAPI().getServerById(String.valueOf(config.serverID)));
			Iterator<Role> it = roles.iterator();
			for (int i = 0; i < roles.size(); i++)
			{
				if(it.hasNext())
				{
					Role current = it.next();
					if (current.getName().equals(config.adminRole))
					{
						return true;
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("Couldn't get user by ID: " + id);
		}
		
		return false;
	}
	
	static boolean isServer(String id)
	{
		if (id.equals(String.valueOf(config.serverID)))
		{
			return true;
		} else
		{
			return false;
		}
	}
	
	static boolean isChannel(String id)
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
		
		if(isChannel(arg1.getChannelReceiver().getId()) && isServer(arg1.getChannelReceiver().getServer().getId()) || config.notBound)
		{
			String suffix = arg1.getContent().substring(1);
			
			if(arg1.getContent().startsWith("!") && isRole(arg1.getAuthor().getId())) {
				
				
				
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
					MainClass.reconnect();
				}
				
				if(suffix.equalsIgnoreCase("stopBot")) {
					if(MainClass.getQA().running)
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
						MainClass.getQA().setOn(false);
					} else
					{
						arg1.reply("Already stopped.");
					}
				}
				
				if(suffix.equalsIgnoreCase("startBot"))
				{
					if(!MainClass.getQA().running)
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
						MainClass.getQA().setOn(true);
					} else
					{
						arg1.reply("Already running.");
					}
				}
				
				if(suffix.equalsIgnoreCase("shutdown"))
				{
					arg1.reply("Shutting bot down...");
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						arg1.reply("Ohoh... something terrible happened, shutting down...");
						System.exit(1);
					}
					MainClass.shutdown();
				}
				
				if(suffix.equalsIgnoreCase("restartBot"))
				{
					if(MainClass.getQA().running)
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
						MainClass.getQA().setOn(false);
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
						MainClass.getQA().setOn(true);
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
						MainClass.getQA().setOn(true);
					}
				}
				
				if(suffix.equalsIgnoreCase("timera"))
				{
					arg1.reply(String.valueOf(MainClass.timerA.current));
				}
				
				
			}
			if(arg1.getContent().startsWith("!") && suffix.equalsIgnoreCase("triviaHelp"))
			{
				arg1.reply("Commands (System): triviaHelp, startBot, stopBot, resetTimerA, restartBot, shutdown, timera");
				arg1.reply("Commands (Question System): me, bp @NAME, openBp, ta [answer], turn, status @NAME, join, yes @NAME, categories, category [name], host, start");
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
