CS455 (Distributed Systems); Project 3 (Identity Server Phase 2 - Reliability Replication); James Crowell, Jackson Morton (Team 10); Spring (2022)

[Video Demo](https://youtu.be/70LEJlMGrQM)

## BUILDING/RUNNING

From the root project directory run the following command to build the docker network, docker images for server and client, and a full maven clean, compile, and package for local versions of jar.
    
    $ make construct

Once complete you can use the following commands to generate a docker container for each server:

    $ make s1
    $ make s2
    $ make s3
    $ make s4


The following commands should be runnable from the cli of your host machine but they can also be ran inside of a Docker container for the client (since the entire project directory is copied into the container image)

If for whatever reason you are unable to run the client from the host cli or are unable to connect to the docker server network (because Apple M1... :( ) run the following command to startup a client container with a cli entrypoint where you can run the provided commands below!

    $ make client

or

    $ docker run -it --network idnetwork --ip 172.20.0.15 --name Client client bash

### Creates account credentials: LOG test-dummy REALNAME test-dummy PASS testing123
    $ make create1
or 

    java -cp target/p3-1.0-SNAPSHOT-jar-with-dependencies.jar client.IdClient --server 172.20.0.11 --numport 1099 --create test-dummy test-dummy --password testing123


### Creates account credentials: LOG test-dummy-2 REALNAME test-dummy PASS testing123
    $  make create2

or


    $ java -cp target/p3-1.0-SNAPSHOT-jar-with-dependencies.jar client.IdClient --server 172.20.0.11 --numport 1099 --create test-dummy-2 test-dummy --password testing123


### Creates account credentials: LOG test-dummy REALNAME test-dummy-2 PASS testing123
    $ make create3

or


    $ java -cp target/p3-1.0-SNAPSHOT-jar-with-dependencies.jar client.IdClient --server 172.20.0.11 --numport 1099 --create test-dummy test-dummy-2 --password testing123


### Deletes the entry created by create1
    $ make del
or

    $ java -cp target/p3-1.0-SNAPSHOT-jar-with-dependencies.jar client.IdClient --server 172.20.0.11 --numport 1099 --delete test-dummy --password testing123

### Gets all, Usernames and UUIDs
    $ make geta

or

    $ java -cp target/p3-1.0-SNAPSHOT-jar-with-dependencies.jar client.IdClient --server 172.20.0.11 --numport 1099 --get all

### Gets all, Usernames only
    $ make getus


or 


    $ java -cp target/p3-1.0-SNAPSHOT-jar-with-dependencies.jar client.IdClient --server 172.20.0.11 --numport 1099 --get users

### Gets all, UUIDs only
    $ make getuu
or

    $ java -cp target/p3-1.0-SNAPSHOT-jar-with-dependencies.jar client.IdClient --server 172.20.0.11 --numport 1099 --get uuids

### Calls for a lookup on the account created by create1
    $ make look
or
    $ java -cp target/p3-1.0-SNAPSHOT-jar-with-dependencies.jar client.IdClient --server 172.20.0.11 --numport 1099 --lookup test-dummy

### Calls for a modify on the account created by create1
    $ make mod

or

    $ java -cp target/p3-1.0-SNAPSHOT-jar-with-dependencies.jar client.IdClient --server 172.20.0.11 --numport 1099 --modify test-dummy test-idiot --password testing123


For added bonus, if you run the commands out of order (like trying to delete and account before creating it) you can see that our servers still handle errors properly as P2.

## TESTING
As this project was a continuation of P2, we did not feel it necessary to re-test the efficacy of the *commands* submitted by clients. That is to say, we are confident that all the functions available to the Client to communicate to the server, as enumerated in P2's README, continue to function as intended.

What *was* in need of testing was:
&emsp; The ability for servers to locate and establish communication with each other
&emsp; The ability for servers to elect a coordinator amongst themselves and consistently come to the same agreement with each other
&emsp; Ensuring the Client is always talking to the Coordinator
&emsp; Ensuring the Coordinator dispensed messages to its followers such that they will mirror actions taken on their own databases
&emsp; Ensuring a Coordinator can go down at any time and the survivors with both notice and elect a new Coordinator
&emsp; Ensuring a downed Server can be restarted and successfully re-join the cluster
&emsp; Finally, proving database consistency by making a write with one Coordinator, killing it, and running a read after a new Coordinator is up

All of these features are demonstrated in the included video, and we tested each in sequence during development. We expected it would be easiest to confirm completeness of a given feature in the individual before moving on to the next, testing features in concert where applicable (that is to say, first make sure the election algorithm works, then make sure a server can be downed and restarted before successfully still participating in an election, and then finally making sure consistency endures throughout both possible permutations of events).

In order to test Server-Server/Client-Server communications, we utilzied Docker containers, which should be included in the scripts submitted such that evaluators can try them in these same circumstances for themselves. This allowed us to create a cluster on a local machine and simulate communication between multiple devices.

These features were enumerated from the assignment and class lectures, and are currently in a state we believe satisfies the requests thereof.


## DESIGN NOTES
(First things first: this time, the mvn clean/compile/package commands absolutely work!)

I have a feeling Distributed Systems will prove to be the most difficult class of the Computer Science major, personally. I have never before had multiple-days-long stretches of just running into problem after problem, never really making any progress on the issue overall. By that I mean, when choosing to utilize Sockets to perform the handshake for the heartbeat, I must have recieved 14 different kinds of errors, one after another, in roughly the same place in the program/algorithm, and I fully expect had I solved problem #14, there would have been #15 waiting for me. Part of that is just lousy luck; there are a functionally infinite number of ways to accomplish the mission of designing a cluster server, and without having had more experience with the art or in the industry to cause one to lean one way or another in terms of design philosophy, the chances of just picking the "wrong" one (which is, one that is disproportionately more difficult than alternatives) is significant.

I intend to start this program over during the summer. I intend to revert back to P2 (which I intend to pull a local copy of before the semester ends and I lose access to the 455/555 Git repos) and start completely over with the intent of building this entirely around multicast, as was my original intent. I know there's a way to do it that probably works more easily than what we currently have (although RMI certainly has its advantages here), and I'm so innately fascinated by multicasting that I would love the opportunity to master it further. We were nervous enough about time crunches during the semester's end that I was afraid to spend too long going down a path that might not bear fruit (and as some are aware, that happened in other places, vis-a-vis Sockets), so we pivoted because one of us was more talented with and confident in RMI usage. I like the finished product that we have here, but I intend to make a version more in line with my original vision, once I have more time to do so.

This is, to date, the hardest assignment I've had to do. My partner is in quasi-agreement; his experience's in Buffenbarger's 354 might take the title, but I am prepared to say 455 P3 is the most difficult thing I've had to do, and I say that because this is the only assignment that's managed to completely screw up my ability to sleep for over a week. Don't mistake me: this is *also* my favorite class I've taken to date, and it completely saved this semester, even as it occasionally ruined my life for a time. Nothing we did here was boring or useless; RMI, multicasting, SSL, Sockets, election/coordination algorithms, replication; I'm taking *ALL* of it with me and hoping to digest it more over the coming months. I recognize that a great deal of the difficulty probably just came as a result of how my own brain tends to confront and process problems (I am immune to Occam's Razor. It literally does not occur to me. Life is Hell), but all the same, I intend to power through it and overcome such problems. My sanity is a small price to pay for mastery of the computer.

Going to try to do P4, starting on the 30th. It's going to be a _COMPLETE_ disaster.
