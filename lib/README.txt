IBM FileNet Content Engine (CE) client JARs — external lib/ folder

================================================================



Place ALL vendor JAR files required for your environment in this folder.

They are loaded at runtime and are NOT repackaged inside the application JAR.



Typical contents (names vary by IBM release; copy from your Java CEWS client bundle):

  - Jace.jar          — FileNet P8 Java API (com.filenet.api.*)

  - Plus any other JARs from the same IBM client package your deployment needs

    (if you see ClassNotFoundException for com.ibm.* / com.filenet.*, add the missing JAR here).



Obtain files from your IBM installation or ACCE:

  Domain > IBM FileNet Content Manager > Java CEWS client



How the app loads lib/

  The executable JAR is built with Spring Boot layout ZIP (PropertiesLauncher).

  run.cmd / run.ps1 start Java with: -Dloader.path=lib

  so every *.jar in lib/ is on the classpath alongside the application.



Build (IBM JARs are not part of the Maven build):

  mvn clean install



Run (from project root, with lib/ populated when app.filenet.enabled=true):

  run.cmd --fromDate=YYYY-MM-DD --toDate=YYYY-MM-DD



If you are not using a real FileNet connection, set app.filenet.enabled=false in config\application.yml

so the stub connector runs; lib/ can be empty in that case.


Troubleshooting

  NoClassDefFoundError: org/apache/log4j/Priority (or other org.apache.log4j.*):

  IBM Jace expects the Log4j 1.x API. This project adds reload4j in pom.xml so those classes

  ship inside the application JAR. Rebuild: mvn clean install


