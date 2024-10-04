# Usage
Download the source and compile in your IDE of choice. EDDNConsumer4J makes use off apache log4J, Gson, jeromq. You will need to download those dependencies.
The app will prompt for a save location to export json responses to. After that location is chosen, the app will begin listening for EDDN events and synchronously
exports responses to your chosen saved location. At any point you can click stop receiving to prevent more EDDN response receipts. EDDNConsumer will then finish up
exporting and close down.

# Why?
I made it from the zeromq test and my own personal touch because I needed a lot of journal json data for testing purposes for a personal library. This was the easiest way to
get that information. I may have went overboard on the functionality of EDDNConsumer but it was a fun little project. I've never dealt with synchronizing threads before! So much fun!
