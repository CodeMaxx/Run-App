The **Run** app was born at a hackathon and ended up winning the "_most creative and fun hack_" award. It is basically a multiplayer
(currently only 1v1) photo treasure hunt game where some random (pre-clicked) photos around your location come up on your screen and
you have to find them and click as similar a photo to the original as you can. Then, after some image processing magic, it tells you
if it's a match or not. The first to match them all wins!

There are two sides to this project - 
* Backend development (Django) - Learn Django and understand and implement models and logic.
* Android development - Communicating with the server and UI.

The two will go hand in hand, as in, any of the topics will need work on both Backend and Android. Some of the additions to be made are - 
* ***Multiplayer*** - Add support for more than 2 players playing in a game. Implement a proper lobby functionality.
* ***Location*** - People on the app are matched up using location and some maximum distance criteria. Also, images are uploaded with
their location and selection of appropriate images based on the location of the players.
* ***Spectators*** - Add a new option on start of the game - to spectate instead of play. Spectators will see player scores, a live map
with the players on them. Further additions possible - spectators can dynamically add an image to the game they are watching. 
