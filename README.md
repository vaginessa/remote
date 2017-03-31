# Remote Control App

This Android application and Python script allows you to control your devices if you have an IR blaster.

## Requirements

* Android 4.0+ with an IR blaster
* Python 3 
* Android Studio
* Internet connection

## How to use

Before you run the program in Android Studio, create the remote control codes for the app by running `python3 rcs.py`. 

Run the project in an emulator to generate a debug apk by going to `Run > Run 'app'`. 

The resulting `apk` file will be in `app/build/outputs/apk/app-debug.apk`. 

Download the `apk` file to your phone to install it.

## Contribute

If you contribute anything, you'll be credited here. :)

Here are a few things this project needs help on:

* User Interface design 
  * Currently, it lists the buttons as a list. A new user interface design would format the buttons like an actual remote regardless of model or brand. 
* A more complete remote control code database
  * The database is now unsaved in this project. If you run the `rcs.py` script to completion (it will take several minutes to download all codes), please send me the completed file to be added to the project.
  * Add more codes that don't exist from the website.
