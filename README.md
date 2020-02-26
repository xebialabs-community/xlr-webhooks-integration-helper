# XL Release webhook event source support generator

### Utility tool for generating XL Release webhook event source definitions

This app can generate the content of files necessary to define a webhook event type source for xl-release, given a JSON payload example of a request. These definitions are necessary to implement integrations with external systems (i.e. Jira or GitHub webhook). 

The tool performs the bulk of the work necessary for us to describe the entities sent by an external system (in the JSON payload) and model them in xl-release as configuration items. For example, if we wanted to add support for the 'Pull Request' webhook event, that will be sent to xl-release when a pull request is opened on github, we would use this tool to generate the content of the two files necessary (described further in the document) by feeding it an example of the JSON payload object that GitHub will send with the webhook request.

Some manual work on the output of the tool is still necessary to make the event source definition production ready. 

The files that make a webhook event source:

* synthetic.xml - defines the xl-release CI for a WebHook event
* ...EventSource.py - python script which performs the mapping from JSON object to the webhook event CI

### Build

    ./gradlew clean build
    
### Run

The tools is in the form of a Spring Boot app so it can be run by executing the JAR, or through gradle task

    ./gradlew bootRun
    
The app will be available on http://localhost:8080

### Usage

1) Obtain/construct the most complete example of the JSON payload for the webhook request you want to add support for

2) Execute an Http POST Request on the "/generate" endpoint with mandatory query params *typePrefix* and *typeName* as well as the JSON payload in the body of the request. With the *typePrefix* and *typeName* we define the prefix and name for the XL Release CI type for one event. For example `http://localhost:8080/generate?typePrefix=github&typeName=PullRequestEvent` would be used to generate a definition for an xl-release event type `github.PullRequestEvent`. This, together with the [example JSON object](https://developer.github.com/v3/activity/events/types/#pullrequestevent) of the webhook payload in the body is enough for the tool to generate the definitions. 

3) The content of 'synthetic.xml' and '[_typeName_]EventSource.py' will printed in the console
4) Polish the content of the files to make the ready for a plugin with actions like: removing duplicate definitions for equal entities, rename nested CIs of the event from the generic name that the generator provides to a more meaningful name and a definition for the actual EventSource to the synthetic (this is not done by the generator).
