[![Build Status](http://ec2-54-71-127-226.us-west-2.compute.amazonaws.com/api/badges/jaisonpjohn/vertx-demo/status.svg)](http://ec2-54-71-127-226.us-west-2.compute.amazonaws.com/jaisonpjohn/vertx-demo)

# vertx-demo
This is to demo vertx microservice. This simple microservice has a PUT endpoint to persist a JSON message in local filesystem and generate an id (UUID) and return. Also it has a curresponding GET endpoint to expose the same item for the given id. 

---
 
# To Build
./gradlew clean shadowJar

# Docker
docker run -p 8080:8080 jaisonpjohn/vertx-demo

curl -v --header "Content-Type:application/json" --data '{"name" : "Egg Whisk", "price" : 3.99, "weight" : 150 }' -X PUT http://localhost:8080/products/

# CI-CD
It uses drone.io to pull, test and build and publish artifacts to docker hub, on different github events. Please note, this project's final artifact is not a JAR file, it is a Docker image with the Jar and the JVM burned-in.

# Stuff Todo

1. Read port from config.yml file than code
2. Add metrics - dropwizard
3. Enable Access logs
4. Trace-ability: Add correlation Id
5. Use Autoincrement semver tags to version the artifacts
5. Move fileAccess to separate layer (dao kind of) so that it can be easily swapped with an actual persistence layer later
6. When doing point 5, Use light-weight DI frameworks like guice to bind different singleton 'beans' together
7. When doing point 5, introduce a mocking framework so that we can spy on different layer's interaction and then code will be 'Unit' testable