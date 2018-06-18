XBotLib [![Build Status](https://drone.io/github.com/justintime4tea/XBotLib/status.png)](https://drone.io/github.com/justintime4tea/XBotLib/latest)
=============================================================================================================================================

XBotLib is a Java library for making bots for [XMPP][1]. It uses the [Smack API v4.1.8][2] to communicate with XMPP servers.

This is a conversion of HippyJava by [hypereddie][4] to a pure XMPP based chat bot library. 
Much appreciation and thanks goes to [hypereddie][4] who is the original author of the work this project is based off of.

## Current Features [WIP]
* Multi-room Chatting [XMPP]
* Private Messaging [XMPP / HTTP]
* Retrieving User Data [HTTP]
* Notification Sending [HTTP]

## Getting Started/How-To [Updates coming]
You can check out the [wiki][3] for tutorials/how-to's

## Contributing
If you would like to contribute to this project, simply fork the repo and send a pull request.
Please test your pull request and please try to keep your code neat.

## Building the Source
The source has a maven script for required dependencies, so just run 'mvn clean install' in the project folder.

[1]: https://xmpp.org/
[2]: https://www.igniterealtime.org/projects/smack/index.jsp
[3]: https://github.com/JustinTime4Tea/XBotLib/wiki
[4]: https://github.com/hypereddie/HippyJava
