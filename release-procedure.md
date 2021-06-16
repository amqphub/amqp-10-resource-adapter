You must have:

* Nexus repo ("ossrh") credentials in ~/.m2/settings.xml

Steps:

* Run the test: `mvn clean package && scripts/test-example.sh`
* Prepare the release: `mvn -Prelease clean release:prepare`
* (Make sure things are pushed to Git here)
* Perform the release: `mvn -Prelease clean release:perform`
* Go to https://oss.sonatype.org/#stagingRepositories
* Select the open repo and "Close" it
* Wait until the close completes and then "Release" it
