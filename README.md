                                                         LENS
                                                  A traveler’s guide
                                                                                                          
## Team members: Anvitha Karanam, Akanksha Jaiswal ##  
[Anvitha Karanam](https://www.linkedin.com/in/anvitha-karanam-546589121/)
[Akanksha Jaiswal](https://www.linkedin.com/in/akanksha-jaiswal-53395960/)

## Introduction: ## 

The Lens app is designed primarily for travelers, keeping in mind the language barriers they face when they travel to different language speaking countries. However, it is not confined to tourists and could be used whenever such use occurs.

On a high level, it allows the user to upload the image and recognize the text from it which can be translated into English. As of now, we are translating 2 languages French and Spanish to English. Identified text is also clickable for more options like Search from Google or Speak out loud to learn selected text’s pronunciation.

## Features: ## <br />
•	Register and Login – Users can register and login to our app using email or Google.<br />
•	Text Recognition from image – Users can choose the image from the gallery or click a new one from the camera through the app. Once the picture is selected, an OCR icon can be used to identify the underlying text and display on the screen.<br />	
•	Search capability of identified text on Google - used SpannableString to make the recognized text clickable and passing the clickable as an intent to another activity which displays google search results on selected text.<br />
•	Translate the identified text - Users can choose to see the translated text from different languages like Spanish and French to English.<br />
•	Text to Speech  – Users can listen to text-to-speech translation of the selected words in the image.<br />

## Technology Stack: ## 
 
Android, Kotlin, 
Google Firebase ML for Text Recognition and Translation (On device), 
Firebase UI auth for authenticating users,
Android TTS Engine for text to speech functionality

## Team Contribution: ##

# Akanksha Jaiswal: # <br />
•	Worked on adding user authentication using Firebase UI Auth with Email and Google as providers.<br />
•	Worked on recognizing text from an image using Firebase ML Kit’s Text Recognition on device API.<br />
•	Worked on giving the option to the user to either select the image from the Gallery or click it from the camera. Created different intents for each.<br />

# Anvitha Karanam: # <br />
•	Worked on manipulation of Recognized Firebase Text using Spannable String to make it clickable and passing the clickable as an intent to another activity which displays google search results in webview.<br />
•	Worked on Translating the identified text by using the  Firebase ML Kit's on-device translation API, and dynamically translated the text to English from different languages like Spanish and French.<br />
•	Worked on Text to Speech by using Android TTS Engine to obtain text to speech pronunciation of the words in the image. 



