## Running the Project
### Requirements
- Maven 3.5 or higher
- Java 8
  -  **NOT** Java 9+ - the build will fail with a newer version of Java

### Setup

 place pom.xml,test-links.txt, and pom.xml~ in a folder named `imagefinder`
 in this folder, create a folder called `src`
 in this folder, create a folder called `main`
 in this folder, create two folders: `java` and `webapp`
 in webapp, place favicon.ico, index.html, web.xml, manifest.mf, and index.html~.
 in the java folder, make another folder called `imagefinder`
 in this folder, place the ImageFinder.java.
 you are now ready to open a terminal window and navigate to the root directory `imagefinder`. To build the project, run the command:

>`mvn package`

If all goes well you should see some lines that end with "BUILD SUCCESS". When you build your project, maven should build it in the `target` directory. To clear this, you may run the command:

>`mvn clean`

To run the project, use the following command to start the server:

>`mvn clean test package jetty:run`

You should see a line at the bottom that says "Started Jetty Server". Now, if you enter `localhost:8080` into your browser, you should see the `index.html` welcome page.

