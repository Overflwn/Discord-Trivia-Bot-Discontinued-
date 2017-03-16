package com.piorjade.triviabot;
import java.util.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.common.util.concurrent.FutureCallback;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.Javacord;
import de.btobastian.javacord.entities.User;

/**
 * 
 * This Discord bot is specifically made for the
 * "Movies"-Discord-Server!
 * Join us: https://discord.gg/9gNAVUa
 * 
 * @author Patrick Swierzy (Piorjade)
 *
 */

public class Bot implements Runnable {
	public String token;
	public DiscordAPI api;
	public MessageListener msgs;
	public QAListener qa;
	public User[] userList;
	public Timer timerA;
	public Timer timerB;
	public Thread timerAThread;
	public Thread timerBThread;
	public boolean needReboot = false;
	boolean printTimers = true;
	static JSONParser parser = new JSONParser();
	
	
	public String[] getCategories()
	{
		File folder = new File(System.getProperty("user.home") + "/Documents/trivia_bot/categories");
		File[] listOfFiles = folder.listFiles();
		if (listOfFiles.length < 1)
		{
			System.err.println("No files in " + System.getProperty("user.home") + "/Documents/trivia_bot/categories");
			System.exit(1);
			return null;
		} else
		{
			String[] list = new String[listOfFiles.length];
			for (int i = 0; i < listOfFiles.length; i++)
			{
				list[i] = listOfFiles[i].getName().substring(0, listOfFiles[i].getName().length()-5);
			}
			return list;
		}
	}
	
	public DiscordAPI getAPI()
	{
		return api;
	}
	
	public MessageListener getMsgs()
	{
		return msgs;
	}
	
	public Timer getTimer(String type)
	{
		if (type.equalsIgnoreCase("a"))
		{
			return timerA;
		} else if (type.equalsIgnoreCase("b"))
		{
			return timerB;
		} else
			return null;
	}
	
	public Thread getTimerThread(String type)
	{
		if (type.equalsIgnoreCase("a"))
		{
			return timerAThread;
		} else if (type.equalsIgnoreCase("b"))
		{
			return timerBThread;
		} else
			return null;
	}
	@SuppressWarnings("unchecked")
	public void refreshQuestions(String category, boolean mixed)
	{
		if(!mixed)
		{
			config.officialQuestions = new ArrayList<String>();
			config.officialAnswers = new ArrayList<String>();
			try {
				Object obj = parser.parse(new FileReader(
						System.getProperty("user.home") + "/Documents/trivia_bot/categories/"+category+".json"));
				JSONObject jsonObject = (JSONObject) obj;
				JSONArray questions = (JSONArray) jsonObject.get("questions");
				JSONArray answers = (JSONArray) jsonObject.get("answers");
				config.officialQuestions = questions;
				config.officialAnswers = answers;
			} catch (Exception e) {
				System.err.println("ERROR REFRESHING QUESTIONS");
				e.printStackTrace();
			}
		} else
		{
			config.officialQuestions = new ArrayList<String>();
			config.officialAnswers = new ArrayList<String>();
			File folder = new File(System.getProperty("user.home") + "/Documents/trivia_bot/categories");
			File[] listOfFiles = folder.listFiles();
			if (listOfFiles.length < 1)
			{
				System.err.println("No files in " + System.getProperty("user.home") + "/Documents/trivia_bot/categories");
				System.exit(1);
			}
			List<String> questions = new ArrayList<String>();
			List<String> answers = new ArrayList<String>();
			for (int i = 0; i < listOfFiles.length; i++)
			{
				if(listOfFiles[i] != null)
				{
					try {
						System.out.println("INFO: LOADING " + listOfFiles[i].getName());
						api.getChannelById(String.valueOf(config.channel)).sendMessage("INFO: LOADING " + listOfFiles[i].getName());
						Object obj = parser.parse(new FileReader(
								System.getProperty("user.home") + "/Documents/trivia_bot/categories/" + listOfFiles[i].getName()));
						JSONObject jsonObject = (JSONObject) obj;
						JSONArray questionss = (JSONArray) jsonObject.get("questions");
						JSONArray answerss = (JSONArray) jsonObject.get("answers");
						for (int j = 0; j < questionss.size(); j++)
						{
							questions.add(questionss.get(j).toString());
							answers.add(answerss.get(j).toString());
						}
						listOfFiles[i] = null;
					} catch (Exception e) {
						System.err.println("ERROR WITH " + listOfFiles[i].getName());
						e.printStackTrace();
					}
				}
			}
			if(questions.size() < config.numberOfQuestions)
			{
				System.err.println("NOT ENOUGH QUESTIONS!");
			}
			Random ran = new Random();
			int num = (int)config.numberOfQuestions;
			int rand;
			
			for (int i = 0; i < config.numberOfQuestions; i++)
			{
				if(!questions.isEmpty() && num > 0)
				{
					rand = ran.nextInt(num);
					config.officialQuestions.add(questions.get(rand));
					questions.remove(rand);
					config.officialAnswers.add(answers.get(rand));
					answers.remove(rand);
					num -= 1;
					
				}
			}
			
			api.getChannelById(String.valueOf(config.channel)).sendMessage("INFO: SIZE OF QUESTIONS " + config.officialQuestions.size());
			api.getChannelById(String.valueOf(config.channel)).sendMessage("INFO: SIZE OF QUESTIONS " + config.officialAnswers.size());
		}
	}
	
	
	public QAListener getQA()
	{
		return qa;
	}
	
	public void refreshUsers()
	{
		userList = new User[api.getUsers().size()];
		Iterator<User> it = api.getUsers().iterator();
		for (int i = 0; i < api.getUsers().size(); i++)
		{
			userList[i] = (User) it.next();
		}
	}
	
	public void reboot()
	{
		timerAThread = null;
		timerBThread = null;
		
		needReboot = true;
	}
	
	public void reconnect()
	{
		api.disconnect();
		try
		{
			Thread.sleep(3000);
		} catch (Exception e)
		{
			e.printStackTrace();
			System.err.println("CRITICAL ERROR");
			System.exit(1);
		}
		
		try
		{
			api.connectBlocking();
		} catch (Exception e)
		{
			reconnect();
		}
	}
	
	public User[] getUserList()
	{
		return userList;
	}
	
	public void resetTimer(String type)
	{
		if(type == "a")
		{
			timerA.reset();
			//timerA = new Timer(config.questionTimeout);
			//timerAThread = new Thread(timerA);
			//timerAThread.start();
		} else if(type == "b")
		{
			timerB.reset();
			//timerB = new Timer(config.officialQuestionTime);
			//timerBThread = new Thread(timerB);
			//timerBThread.start();
		}
	}
	
	public void shutdown()
	{
		api.disconnect();
		printTimers = false;
		System.out.println("INFO: SHUTTING SYSTEM DOWN");
		System.exit(0);
	}
	
	public void resetTimer(String type, long amount)
	{
		if(type == "a")
		{
			timerA.reset(amount);
			//timerA = new Timer(config.questionTimeout);
			//timerAThread = new Thread(timerA);
			//timerAThread.start();
		} else if(type == "b")
		{
			timerB.reset(amount);
			//timerB = new Timer(config.officialQuestionTime);
			//timerBThread = new Thread(timerB);
			//timerBThread.start();
		}
	}
	@SuppressWarnings("unchecked")
	public void run() {
		needReboot = false;
		try {
			//Open the json file and make it a json object
			Object obj = parser.parse(new FileReader(
					System.getProperty("user.home") + "/Documents/trivia_bot/settings.json"));
			
			JSONObject jsonObject = (JSONObject) obj;
			
			//Get the token
			token = (String) jsonObject.get("token");
			
			//Get the configuration
			String id = (String) jsonObject.get("channelID");
			config.notBound = false;
			config.channel = Long.parseLong(id);
			String timeoutA = (String) jsonObject.get("questionTimeout");
			String timeoutB = (String) jsonObject.get("officialQuestionCycle");
			config.questionTimeout = Long.parseLong(timeoutA);
			config.officialQuestionTime = Long.parseLong(timeoutB);
			
			String serverID = (String) jsonObject.get("serverID");
			config.serverID = Long.parseLong(serverID);
			String numberOfQuestions = (String) jsonObject.get("numberOfQuestions");
			config.numberOfQuestions = Integer.parseInt(numberOfQuestions);
			String officialQuestionTimeout = (String) jsonObject.get("officialQuestionTimeout");
			config.officialQuestionTimeout = Long.parseLong(officialQuestionTimeout);
			String adminRole = (String) jsonObject.get("adminRole");
			config.adminRole = adminRole;
			String joinTime = (String) jsonObject.get("joinTime");
			config.joinTime = Long.parseLong(joinTime);
			String availablePoints = (String) jsonObject.get("availablePoints");
			config.availablePoints = Long.parseLong(availablePoints);
			
			//Get the official questions and answers
			JSONArray questions = (JSONArray) jsonObject.get("questions");
			JSONArray answers = (JSONArray) jsonObject.get("answers");
			config.officialQuestions = questions;
			config.officialAnswers = answers;
			
			
		} catch (Exception e) {
			System.err.println("ERROR LOADING CONFIG");
			e.printStackTrace();
		}
		//instantiate the bot
		api = Javacord.getApi(token, true);
		api.setToken(token, true);
		System.out.println("Channel bound to " + config.channel);
		
		api.setAutoReconnect(true);
		try
		{
			api.connectBlocking();
		} catch (Exception e)
		{
			System.err.println("Connecting failed, retrying.");
			reconnect();
		}
		
		
		//Instantiate the 
		msgs = new MessageListener();
		qa = new QAListener();
		
		//Actually register them in the API
		api.registerListener(msgs);
		api.registerListener(qa);
		
		//This timer is here for the actual timeout for a question
		timerA = new Timer(config.questionTimeout, "a");
		timerAThread = new Thread(timerA);
		timerAThread.start();
		
		//This timer is here for the official questions to start every X hours (set in the settings.json)
		timerB = new Timer(config.officialQuestionTime, "b");
		timerBThread = new Thread(timerB);
		timerBThread.start();
		
		//Get a list of users.
		userList = new User[api.getUsers().size()];
		Iterator<User> it = api.getUsers().iterator();
		for (int i = 0; i < api.getUsers().size(); i++)
		{
			userList[i] = (User) it.next();
		}
		System.out.println("RUNNING");
		while(!needReboot)
		{
			if(printTimers)
				System.out.println(timerA.current + ":" + timerB.current);
			
			//The main loop, waits for the timer to finish and then resets everything
			if(timerA.finished && timerA.running && !qa.waiting && qa.running && !qa.init)
			{
				api.getChannelById(String.valueOf(config.channel)).sendMessage("New question!");
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				qa.nextQuestion();
				resetTimer("a");
			}
			
			if (timerA.finished && timerA.running && qa.running && qa.init)
			{
				api.getChannelById(String.valueOf(config.channel)).sendMessage("Joining time is over!");
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				qa.startOfficials();
				resetTimer("a", config.officialQuestionTimeout);
			}
				
			if(timerB.finished && timerB.running && qa.running )
			{
				resetTimer("a", config.joinTime);
				resetTimer("b");
				MainClass.resetB = true;
				qa.initOfficials();
					
			}
		}
		
		System.out.println("REBOOTING...");
	}


}
