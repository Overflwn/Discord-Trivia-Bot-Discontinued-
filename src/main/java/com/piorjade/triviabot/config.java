package com.piorjade.triviabot;

import java.util.List;

/**
 * 
 * This is just a class to let other classes access basic
 * properties.
 * 
 * It gets "loaded" when the Bot class gets run as a thread.
 * 
 * @author Patrick Swierzy (Piorjade)
 *
 */

public class config {
	//This is the channel ID, if the bot is bound to one.
	public static long channel = 0;
	public static boolean notBound = true;
	public static long questionTimeout = 0;
	public static long officialQuestionTime = 0;
	public static List<String> officialQuestions;
	public static List<String> officialAnswers;
	public static long serverID;
	public static String adminRole;
	public static int numberOfQuestions = 0;
	public static long officialQuestionTimeout;
	public static long joinTime;
	public static long availablePoints;
}
