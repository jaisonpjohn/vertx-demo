[![Build Status](http://ec2-54-71-127-226.us-west-2.compute.amazonaws.com/api/badges/jaisonpjohn/vertx-demo/status.svg)](http://ec2-54-71-127-226.us-west-2.compute.amazonaws.com/jaisonpjohn/vertx-demo)

# vertx-demo
This is to demo vertx microservice. This simple microservice has put endpoint to PUT a JSON message in local filesystem and generate an id (UUID) and return, it has a curresponding GET endpoint to expose the same item given the id. 

---
 
# To Build
./gradlew clean shadowJar

# Docker
docker run -p 8080:8080 jaisonpjohn/vertx-demo

curl -v --header "Content-Type:application/json" --data '{"name" : "Egg Whisk", "price" : 3.99, "weight" : 150 }' -X PUT http://localhost:8080/products/

# CI-CD
It uses drone.io to pull, test and build and publish artifacts to docker hub, on different github events. Please note, this project's final artifact is not a JAR file, it is a Docker image with the Jar and the JVM burned-in.
