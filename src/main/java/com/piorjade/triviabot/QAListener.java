package com.piorjade.triviabot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.permissions.Role;
import de.btobastian.javacord.listener.message.MessageCreateListener;

/**
 * 
 * This class / listener basically skips to the next question OR (depending on whether officials are running or nah) selects a random user
 * to be the next person who asks a question.
 * 
 * It also listens to commands related to the question and "Officials"-system
 * @author Patrick Swierzy (Piorjade)
 *
 */

public class QAListener implements MessageCreateListener {

	static String turn = "none";
	static boolean asking = false;
	static boolean official = false;
	static String question;
	static String answer;
	Iterator<String> iter;
	Iterator<String> iterAns;
	public static boolean waiting = true;
	public static boolean running = true;
	public static String category = "mixed";
	public static boolean init = false;
	public static boolean mixed = true;
	List<User> joinedIds;
	String[] enteredAnswers;
	int[] points;
	long[] time;
	long availablePoints = config.availablePoints;
	public String[] getCurrentAnswer() 
	{
		if(official)
		{
			String[] suff = answer.split("\\,");
			return suff;
		} else
		{
			notRunning();
			return null;
		}
		
	}

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
	
	public void setTurn(String name)
	{
		if(running)
		{
			turn = name;
			asking = true;
		} else
		{
			notRunning();
		}
	}
	
	public void setOn(boolean status)
	{
		running = status;
		if(status)
		{
			waiting = true;
			asking = false;
			turn = null;
			init = false;
			official = false;
			MainClass.resetA = false;
			MainClass.resetB = false;
			MainClass.getBot().getAPI().getChannelById(String.valueOf(config.channel)).sendMessage("Who wants to ask next? Enter '!me' !");
		} else
		{
			
			waiting = true;
			asking = false;
			turn = null;
			official = false;
			init = false;
			MainClass.getBot().getAPI().getChannelById(String.valueOf(config.channel)).sendMessage("Stopped!");
			MainClass.getBot().resetTimer("a", config.questionTimeout);
			MainClass.resetA = true;
			MainClass.resetB = true;
		}
	}
	
	public void startOfficials()
	{
		if(running)
		{
			official = true;
			waiting = false;
			asking = true;
			init = false;
			MainClass.getBot().refreshQuestions(category, mixed);
			iter = config.officialQuestions.iterator();
			iterAns = config.officialAnswers.iterator();
			nextQuestion();
			
		} else
		{
			notRunning();
		}
	}
	
	public void initOfficials()
	{
		if(running)
		{
			init = true;
			joinedIds = new ArrayList<User>();
			enteredAnswers = new String[MainClass.getBot().getAPI().getUsers().size()];
			points = new int[MainClass.getBot().getAPI().getUsers().size()];
			time = new long[MainClass.getBot().getAPI().getUsers().size()];
			availablePoints = 5;
			MainClass.getBot().getAPI().getChannelById(String.valueOf(config.channel)).sendMessage("The Officials are starting! Enter '!join' to joing the game!");
			MainClass.getBot().resetTimer("a", config.joinTime);
		} else
		{
			notRunning();
		}
	}
	
	void checkAnswers()
	{
		for (int i = 0; i < joinedIds.size(); i++)
		{
			String[] answer = getCurrentAnswer();
			for (int j = 0; j < answer.length; j++)
			{
				if(enteredAnswers[i].toLowerCase().matches("(.*)" + answer[j].toLowerCase() + "(.*)") && enteredAnswers[i].length() < (answer[j].length()*2))
				{
					long timeNeeded = time[i];
					points[i] += (availablePoints - timeNeeded);
				}
			}
		}
	}
	
	void checkWinner()
	{
		String firstName = "";
		int first = 0;
		String secondName = "";
		int second = 0;
		String thirdName = "";
		int third = 0;
		
		for (int i = 0; i < points.length; i++)
		{
			if(points[i] > first)
			{
				first = points[i];
				firstName = joinedIds.get(i).getName();
			} else if (points[i] > second)
			{
				second = points[i];
				secondName = joinedIds.get(i).getName();
			} else if (points[i] > third)
			{
				third = points[i];
				thirdName = joinedIds.get(i).getName();
			}
		}
		
		MainClass.getBot().getAPI().getChannelById(String.valueOf(config.channel)).sendMessage("Here are the winners:");
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("ERROR: Couldn't let the thread sleep! Shutting down...");
			MainClass.getBot().getAPI().getChannelById(String.valueOf(config.channel)).sendMessage("ERROR: Couldn't let the thread sleep! Shutting down...");
			System.exit(1);
		}
		MainClass.getBot().getAPI().getChannelById(String.valueOf(config.channel)).sendMessage("First: " + firstName + " - " + first + " points.");
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("ERROR: Couldn't let the thread sleep! Shutting down...");
			MainClass.getBot().getAPI().getChannelById(String.valueOf(config.channel)).sendMessage("ERROR: Couldn't let the thread sleep! Shutting down...");
			System.exit(1);
		}
		MainClass.getBot().getAPI().getChannelById(String.valueOf(config.channel)).sendMessage("Second: " + secondName + " - " + second + " points.");
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("ERROR: Couldn't let the thread sleep! Shutting down...");
			MainClass.getBot().getAPI().getChannelById(String.valueOf(config.channel)).sendMessage("ERROR: Couldn't let the thread sleep! Shutting down...");
			System.exit(1);
		}
		MainClass.getBot().getAPI().getChannelById(String.valueOf(config.channel)).sendMessage("Third: " + thirdName + " - " + third + " points.");
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("ERROR: Couldn't let the thread sleep! Shutting down...");
			MainClass.getBot().getAPI().getChannelById(String.valueOf(config.channel)).sendMessage("ERROR: Couldn't let the thread sleep! Shutting down...");
			System.exit(1);
		}
	}
	
	public void nextQuestion()
	{
		if (running)
		{
			if(official && iter.hasNext())
			{
				if (question != null)
				{
					checkAnswers();
				}
				boolean winner = false;
				for (int i = 0; i < joinedIds.size(); i++)
				{
					if(points[i] >= 50)
					{
						winner = true;
					}
				}
				if(!winner)
				{
					question = iter.next();
					answer = iterAns.next();
					
					try
					{
						MainClass.getBot().getAPI().getChannelById(String.valueOf(config.channel)).sendMessage("Question: " + question);
					} catch (Exception e)
					{
						e.printStackTrace();
						MainClass.getBot().getAPI().getChannelById(String.valueOf(config.channel)).sendMessage("Question: " + question);
					}
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.err.println("ERROR: Couldn't let the thread sleep! Shutting down...");
						MainClass.getBot().getAPI().getChannelById(String.valueOf(config.channel)).sendMessage("ERROR: Couldn't let the thread sleep! Shutting down...");
						System.exit(1);
					}
				} else
				{
					checkWinner();
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.err.println("ERROR: Couldn't let the thread sleep! Shutting down...");
						MainClass.getBot().getAPI().getChannelById(String.valueOf(config.channel)).sendMessage("ERROR: Couldn't let the thread sleep! Shutting down...");
						System.exit(1);
					}
					MainClass.getBot().getAPI().getChannelById(String.valueOf(config.channel)).sendMessage("These were all questions! Next round starts in: " + (double) (config.officialQuestionTime / 60 / 60) + " hours.");
					official = false;
					MainClass.getBot().resetTimer("b");
					MainClass.getBot().resetTimer("a", config.questionTimeout);
					nextQuestion();
				}
			} else if (official && !iter.hasNext())
			{
				checkWinner();
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.err.println("ERROR: Couldn't let the thread sleep! Shutting down...");
					MainClass.getBot().getAPI().getChannelById(String.valueOf(config.channel)).sendMessage("ERROR: Couldn't let the thread sleep! Shutting down...");
					System.exit(1);
				}
				MainClass.getBot().getAPI().getChannelById(String.valueOf(config.channel)).sendMessage("These were all questions! Next round starts in: " + (double) (config.officialQuestionTime / 60 / 60) + " hours.");
				official = false;
				MainClass.resetB = false;
				MainClass.getBot().resetTimer("b");
				MainClass.getBot().resetTimer("a", config.questionTimeout);
				nextQuestion();
			} else if (!official && !waiting)
			{
				Random gen = new Random();
				int ran;
				do
				{
					ran = gen.nextInt(MainClass.getBot().getUserList().length-1);
				} while (MainClass.getBot().getUserList()[ran].isBot() || !(MainClass.getBot().getUserList()[ran].getStatus().equals("ONLINE")));
				setTurn(MainClass.getBot().getUserList()[ran].getName());
				MainClass.getBot().getAPI().getChannelById(String.valueOf(config.channel)).sendMessage("It's " + turn + "'s turn now!");
			}
		} else
		{
			notRunning();
		}
	}
	
	boolean hasJoined(String id)
	{
		
		boolean found = false;
		for (int i = 0; i < joinedIds.size(); i++)
		{
			if (joinedIds.get(i).getId().equals(id))
				found = true;
		}
		
		return found;
	}
	
	public boolean isOfficial()
	{
		return official;
	}
	
	void notRunning()
	{
		MainClass.getBot().getAPI().getChannelById(String.valueOf(config.channel)).sendMessage("I'm currently not running! The owner must enter '!start' to start me");
	}
	
	public void onMessageCreate(DiscordAPI arg0, Message arg1) {
		if(running)
		{
			// TODO Auto-generated method stub
			if(!MainClass.getBot().needReboot && isChannel(arg1.getChannelReceiver().getId()) && isServer(arg1.getChannelReceiver().getServer().getId()) || config.notBound)
			{
				if(arg1.getContent().startsWith("!")) {
					//divide by space
					String[] suff = arg1.getContent().split("\\s+");
					
					String suffix = suff[0].substring(1);
					String afterSuff = arg1.getContent().substring(suff[0].length());
					System.out.println(suffix);
					if (suffix.equalsIgnoreCase("me") && !asking) {
						arg1.reply("Okay, it's " + arg1.getAuthor().getName() + "'s turn now!");
						turn = arg1.getAuthor().getName();
						asking = true;
						waiting = false;
						MainClass.getBot().resetTimer("a");
					}
					
					if (suffix.equalsIgnoreCase("openBp") && asking && isRole(arg1.getAuthor().getId()) && !isOfficial())
					{
						waiting = true;
						asking = false;
						turn = null;
						arg1.reply("Who wants to ask next? Enter '!me' !");
					} else if(suffix.equalsIgnoreCase("bp") && asking && !isRole(arg1.getAuthor().getId()))
					{
						arg1.reply(arg1.getAuthor().getMentionTag() + " You're not allowed to do that.");
					}
					
					if(suffix.equalsIgnoreCase("join") && init)
					{
						if (!hasJoined(arg1.getAuthor().getId()))
						{
							joinedIds.add(arg1.getAuthor());
							enteredAnswers[joinedIds.size()-1] = "none";
							points[joinedIds.size()-1] = 0;
							time[joinedIds.size()-1] = 0;
							arg1.reply(arg1.getAuthor().getMentionTag() + " joined the game!");
						}
					}
					
					if(suffix.equalsIgnoreCase("join") && !init && !official)
					{
						arg1.reply("The Officials are currently not running!");
					}
					
					if(suffix.equalsIgnoreCase("join") && !init && official)
					{
						arg1.reply(arg1.getAuthor().getMentionTag() + " You can't join in while the Officials are running.");
					}
					
					if(suffix.equalsIgnoreCase("bp") && isRole(arg1.getAuthor().getId()) && !isOfficial())
					{
						
						if (!(arg1.getMentions().size() > 0))
						{
							arg1.reply("Usage: !bp @NAME");
						}
						String userID = arg1.getMentions().get(0).getId();
						try {
							arg1.reply("It's " + MainClass.getBot().getAPI().getUserById(userID).get().getName() + "'s turn now!");
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							arg1.reply("There was an error getting the name, tell an admin!");
							return;
						}
						
						try {
							turn = MainClass.getBot().getAPI().getUserById(userID).get().getName();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							arg1.reply("There was an error getting the name, tell an admin!");
							return;
						}
						asking = true;
						waiting = false;
						MainClass.getBot().resetTimer("a");
					}
					
					if(suffix.equalsIgnoreCase("ta") && isOfficial() && suff.length > 1 && hasJoined(arg1.getAuthor().getId()))
					{
						String msg = suff[1];
						for (int i = 0; i < joinedIds.size(); i++)
						{
							if(joinedIds.get(i).getId().equals(arg1.getAuthor().getId()))
							{
								enteredAnswers[i] = afterSuff.replaceAll("\\s+", "");
								time[i] = MainClass.getBot().getTimer("a").current;
							}
						}
					}
					
					if(suffix.equalsIgnoreCase("ta") && !isOfficial())
					{
						arg1.reply(arg1.getAuthor().getMentionTag() + " The Officials are currently not running!");
					}
					
					if(suffix.equalsIgnoreCase("ta") && isOfficial() && suff.length > 1 && !hasJoined(arg1.getAuthor().getId()))
					{
						arg1.reply(arg1.getAuthor().getMentionTag() + " You didn't join the game!");
					}
					
					if(suffix.equalsIgnoreCase("turn"))
					{
						if (asking && !isOfficial())
							arg1.reply("It's " + turn + "'s turn.");
						else if(asking && isOfficial())
							arg1.reply("The Officials are currently running.");
						else
							arg1.reply("Noone is asking. Enter '!me' to ask a question!");
					}
					if(suffix.equalsIgnoreCase("next") && !isOfficial())
					{
						long cTime = MainClass.getBot().timerB.current;
						long end = MainClass.getBot().timerB.tEnd;
						long left = end - cTime;
						double leftH = (left/60) / 60;
						arg1.reply(leftH + " hours left until the next Officials.");
					}
					
					if(suffix.equalsIgnoreCase("yes") && asking && arg1.getAuthor().getName().equals(turn) && !isOfficial() ) 
					{
						System.out.println("Entered yes");
						String winner = arg1.getMentions().get(0).getName();
						System.out.println(winner + " won.");
						arg1.reply("Looks like " + arg1.getMentions().get(0).getMentionTag() + " is the winner of this turn!");
						arg1.reply("It's " + arg1.getMentions().get(0).getMentionTag() + "'s turn now!");
						setTurn(winner);
						MainClass.getBot().resetTimer("a");
					}
					
					if(suffix.equalsIgnoreCase("categories") && !isOfficial() && isRole(arg1.getAuthor().getId())) 
					{
						String[] categories = MainClass.getBot().getCategories();
						String message = "Categories: ";
						for (int i = 0; i < categories.length; i++)
						{
							message = message + categories[i] + ", ";
						}
						arg1.reply(message);
					}
					if(suffix.equalsIgnoreCase("category") && !isOfficial() && isRole(arg1.getAuthor().getId()) && suff.length > 1)
					{
						if(!suff[1].equalsIgnoreCase("mixed"))
						{
							String[] categories = MainClass.getBot().getCategories();
							
							for (int i = 0; i < categories.length; i++)
							{
								if(categories[i].equalsIgnoreCase(suff[1]))
								{
									category = suff[1];
									mixed = false;
									arg1.reply("Next category set to: " + suff[1]);
									return;
								}
							}
							
							arg1.reply("Category not found.");
						} else
						{
							category = "mixed";
							mixed = true;
							arg1.reply("Next category is mixed.");
							
						}
					}
					
					if(suffix.equalsIgnoreCase("category") && isOfficial() && isRole(arg1.getAuthor().getId()) && suff.length > 1)
					{
						arg1.reply(arg1.getAuthor().getMentionTag() + " You can't do that while the Officials are running.");
					}
					
					if(suffix.equalsIgnoreCase("category") && !isRole(arg1.getAuthor().getId()) && suff.length > 1)
					{
						arg1.reply(arg1.getAuthor().getMentionTag() + " You can't do that.");
					}
					
					if(suffix.equalsIgnoreCase("category") && !isOfficial() && isRole(arg1.getAuthor().getId()) && !(suff.length > 1))
					{
						arg1.reply("Usage: !category [name]");
					}
					
					
					if(suffix.equalsIgnoreCase("status") && suff.length > 1 && arg1.getMentions().size() > 0)
					{
						System.out.println(arg1.getAuthor().getName() + " asked for " + arg1.getMentions().get(0).getName() + "'s status.");
						try {
							arg1.reply(arg1.getMentions().get(0).getMentionTag() + "'s status: " + MainClass.getBot().getAPI().getUserById(arg1.getMentions().get(0).getId()).get().getStatus());
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							arg1.reply("ERROR: Couldn't get the status of ID: " + arg1.getMentions().get(0).getId());
						}
					}
				}
			}
		}
	}

}