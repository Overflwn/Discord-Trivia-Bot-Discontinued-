package com.piorjade.triviabot;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.Javacord;
import de.btobastian.javacord.entities.User;

/**
 * 
 * This class is the core of the bot and sets the connection and listeners up.
 * 
 * @author Patrick Swierzy (Piorjade)
 *
 */
public class MainClass {
	//define variables
	static DiscordAPI api;
	static MessageListener msgs;
	static QAListener qa;
	static User[] userList;
	static String token = "";
	static JSONParser parser = new JSONParser();
	public volatile boolean nextQuestion = false;
	public static volatile boolean resetTimer = false;
	public static volatile boolean timerWait = true;
	public static volatile Timer timerA;
	public static volatile Thread timerAThread;
	public static String[] getCategories()
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
	
	public static DiscordAPI getAPI()
	{
		return api;
	}
	
	public static MessageListener getMsgs()
	{
		return msgs;
	}
	
	public static QAListener getQA()
	{
		return qa;
	}
	
	@SuppressWarnings("unchecked")
	public static void refreshQuestions(String category, boolean mixed)
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
		}
	}
	
	static void reconnect()
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
	
	
	public static User[] getUserList()
	{
		userList = new User[api.getUsers().size()];
		Iterator<User> it = api.getUsers().iterator();
		for (int i = 0; i < api.getUsers().size(); i++)
		{
			userList[i] = (User) it.next();
		}
		return userList;
	}
	
	public static void shutdown()
	{
		api.disconnect();
		System.out.println("INFO: SHUTTING SYSTEM DOWN");
		System.exit(0);
	}
	
	public static synchronized boolean shouldReset()
	{
		return resetTimer;
	}
	
	public static synchronized boolean shouldWait()
	{
		return timerWait;
	}
	
	public static void main(String[] args) {
		//Load config
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
			
			String serverID = (String) jsonObject.get("serverID");
			config.serverID = Long.parseLong(serverID);
			String numberOfQuestions = (String) jsonObject.get("numberOfQuestions");
			config.numberOfQuestions = Integer.parseInt(numberOfQuestions);
			String officialQuestionTimeout = (String) jsonObject.get("officialQuestionTimeout");
			config.officialQuestionTimeout = Long.parseLong(officialQuestionTimeout);
			String adminRole = (String) jsonObject.get("adminRole");
			config.adminRole = adminRole;
			String availablePoints = (String) jsonObject.get("availablePoints");
			config.availablePoints = Long.parseLong(availablePoints);
			
			
			
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
		
		//Instantiate the listeners
		msgs = new MessageListener();
		qa = new QAListener();
		
		//Actually register them in the API
		api.registerListener(msgs);
		api.registerListener(qa);
		
		//Get a list of users.
		userList = new User[api.getUsers().size()];
		Iterator<User> it = api.getUsers().iterator();
		for (int i = 0; i < api.getUsers().size(); i++)
		{
			userList[i] = (User) it.next();
		}
		System.out.println("RUNNING");
		
		timerA = new Timer(config.officialQuestionTimeout, "a");
		timerAThread = new Thread(timerA);
		timerAThread.start();
		while(true)
		{
			//System.out.println(timerA.current);
			if (timerA.finished && qa.running && !qa.init && !qa.waiting && qa.isOfficial()) 
			{
				resetTimer = true;
				qa.nextQuestion();
			}
		}
	}

}
