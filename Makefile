# Creates the necessary Docker network and builds the images for the Server and Client containers
construct:
	docker network create idnetwork --subnet=172.20.0.0/24
	docker build -f Dockerfile.client --tag client .
	docker build -f Dockerfile.server --tag server .
	mvn clean
	mvn compile
	mvn package


# Cluster members
s1:
	docker run --network idnetwork --name ServerOne --ip=172.20.0.11 server

s2:
	docker run --network idnetwork --name ServerTwo --ip=172.20.0.12 server
	
s3:
	docker run --network idnetwork --name ServerThree --ip=172.20.0.13 server
	
s4: 
	docker run --network idnetwork --name ServerFour --ip=172.20.0.14 server


# Creates account credentials: LOG test-dummy REALNAME test-dummy PASS testing123
create1:
	java -cp target/p3-1.0-SNAPSHOT-jar-with-dependencies.jar client.IdClient --server 172.20.0.11 --numport 1099 --create test-dummy test-dummy --password testing123
	
	
# Creates account credentials: LOG test-dummy-2 REALNAME test-dummy PASS testing123
create2:
	java -cp target/p3-1.0-SNAPSHOT-jar-with-dependencies.jar client.IdClient --server 172.20.0.11 --numport 1099 --create test-dummy-2 test-dummy --password testing123
	
	
# Creates account credentials: LOG test-dummy REALNAME test-dummy-2 PASS testing123
create3:
	java -cp target/p3-1.0-SNAPSHOT-jar-with-dependencies.jar client.IdClient --server 172.20.0.11 --numport 1099 --create test-dummy test-dummy-2 --password testing123
	
	
# Deletes the entry created by create1
del:
	java -cp target/p3-1.0-SNAPSHOT-jar-with-dependencies.jar client.IdClient --server 172.20.0.11 --numport 1099 --delete test-dummy --password testing123
	
# Gets all, Usernames and UUIDs
geta:
	java -cp target/p3-1.0-SNAPSHOT-jar-with-dependencies.jar client.IdClient --server 172.20.0.11 --numport 1099 --get all
	
# Gets all, Usernames only	
getus:
	java -cp target/p3-1.0-SNAPSHOT-jar-with-dependencies.jar client.IdClient --server 172.20.0.11 --numport 1099 --get users
	
# Gets all, UUIDs only	
getuu:
	java -cp target/p3-1.0-SNAPSHOT-jar-with-dependencies.jar client.IdClient --server 172.20.0.11 --numport 1099 --get uuids
	
# Calls for a lookup on the account created by create1
look:
	java -cp target/p3-1.0-SNAPSHOT-jar-with-dependencies.jar client.IdClient --server 172.20.0.11 --numport 1099 --lookup test-dummy

# Calls for a modify on the account created by create1
mod:
	java -cp target/p3-1.0-SNAPSHOT-jar-with-dependencies.jar client.IdClient --server 172.20.0.11 --numport 1099 --modify test-dummy test-idiot --password testing123

client:
	docker run -it --network idnetwork --ip 172.20.0.15 --name Client client bash
