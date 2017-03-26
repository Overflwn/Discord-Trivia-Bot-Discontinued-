# Discord-Trivia-Bot
This is a bot for Discord made with Java and Javacord.

It basically is a "question"-game where one user asks a question and the others have to answer it, the asking person then
gives the right to the winner using !yes @NAME.

Admins can open the bp rights or give them to others by using !openBp or !bp @NAME. (bp is basically the "turn")

This program reads the settings file (settings.json) from [HOME]/Documents/trivia_bot/settings.json and the category-files
(where questions and answers for the "Officials" are located) in [HOME]/Documents/trivia_bot/categories/.
([HOME] depends on the operating system and your currently logged-in user)
(e.g. on windows: C:/Users/John/Documents/trivia_bot/ and on Linux: /home/John/Documents/trivia_bot)

*YOU NEED TO MAKE THE settings.json YOURSELF!*

## Example settings.json

```json
{
	"token": "XXXX",
	"serverID": "00000000000000000",
	"channelID": "000000000000000000",
	"adminRole": "Moderators",
	"officialQuestionTimeout": "5",
	"availablePoints": "8",
	"numberOfQuestions": "100"
}
```

## Settings explanation

token: The token of the bot account

serverID: The ID of the server the bot should be on, you need to enable the Developer option in Discord and right-click on your desired server the bot should be on and select "Copy ID"

channelID: The ID of the channel the bot should be on, the same as above but right-click the text-channel your bot should only listen to this time

adminRole: The name of the role which should have admin-rights

officialQuestionTimeout: The amount of seconds the "Officials" should leave for users to answer the question

availablePoints: The amount of points you can maximally get from a question (-1 because you can't type that fast that you answer it in 0 seconds)

numberOfQuestions: The amount of questions the "Officials" should have

## Commands explained

!me: If the "bp" is open, you take the right to ask a question

!yes @NAME: If you are the asking user, you determine the winner with that and then the winner gets the bp

!openBp: If you have admin-rights, you open the bp with that, so that other users can use !me

!bp @NAME: If you have admin-rights, you give the bp to the mentioned user

!next: Tells you when the next Officials start, in hours

!stop: (admin-rights) Stops the bot and resets the timers

!start: (admin-rights) Restarts the timers and the bot (if the Officials where running, they got shut down and their timer got reset too)

!restart: (admin-rights) Basically stop and start

!triviaHelp: Prints all these commands

!status @NAME: Prints the status (offline, online, etc.) of the mentioned user in the chat.

!turn: Prints the name of the user that currently has the bp

!ta [answer]: Enters the argument as your answer for the current question of the Officials (NOTE: if you enter that 2 times for the same question, the old answer gets deleted)

!join: If the Officials are about to start, you can enter them with that.

!categories: (admin-rights) Prints every category in the categories folder.

!category [name]: (admin-rights) The next Officials get set to that category file. If you enter "mixed" however, the next Officials take random questions out of every file

!reconnect: (admin-rights) Reconnects the bot, whyever you'd want to do that.

!shutdown: (admin-rights) Shuts the bot down.

!timera: (admin-rights) Prints the current second of the question timeout.



# How points are determined

Points are given like this:

points = availablePoints - [time elapsed since the question came up]

Which with the example settings basically means that you always get AT LEAST 3 points for answering the question correctly.

Also the answer gets checked whether it CONTAINS the answer (normally a keyword), you can could enter asdasdANSWERasdas.

BUT

Whenever you entered more characters than the answer itself, it gets counted as wrong. So be aware of that.

Also case doesn't matter.



# Steps to do after downloading this bot

1. Create the trivia_bot folder in your Documents folder (NOTE: it must be "Documents", not in other languages)
2. Create the categories folder inside it.
3. Create your settings.json file
4. Create AT LEAST one .json file in the categories folder, the name doesn't matter. (the name will be a category in the commands later)

## Example category file:

```json
{
	"questions": [
		"What is Germany?",
		"What is Minecraft?",
		"What is water?"
	],
	"answers": [
		"country",
		"game",
		"H2O,liquid"
	]
}
```

*NOTE:* The answers HAVE to be in the same order as the questions.
*NOTE2:* JSON Arrays' last entry don't have a "," as you can see.
*NOTE3:* The answers MUST NOT contain a whitespace, as the answers get automatically made lowercase and their whitespaces get removed.