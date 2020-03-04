## Webhook event source generator

The XL Release webhook event source support generator is a tool that can generate the content of files necessary to define a webhook event source for XL Release, given a JSON payload example of a request. These definitions are necessary to implement integrations with external systems (i.e. Jira or GitHub webhooks).

The tool performs the bulk of the work necessary to describe the entities sent by an external system (in the JSON payload) and model them in XL Release as configuration items. For example, if you want to add support for the 'Pull Request' webhook event that will be sent to XL Release when a pull request is opened on GitHub, you can use this tool to generate the content of the two files necessary (described further in the document) by feeding it an example of the JSON payload object that GitHub will send with the webhook request.

Some manual work on the generated output from this tool is still necessary to make the event source definitions production-ready.

The files that make a webhook event source are:

* `synthetic.xml` - defines the XL Release CI for a webhook event
* `...EventSource.py` - Python script which performs the mapping from JSON object to the webhook event CI

### Build

`./gradlew clean build`

### Run

The tool is in the form of a Spring Boot app so it can be run by executing the JAR, or through Gradle task:

`./gradlew bootRun`

The app will be available on http://localhost:8080

### Usage

1) Obtain/construct the most complete example of the JSON payload for the webhook request you want to add support for.
    * It is very important that the example payload uses a complete set of fields. For instance, the [example payloads on GitHub documentation](https://developer.github.com/v3/activity/events/types/) leave many fields empty or null. If fields are left empty the tool will be unable to guess the kind or type they should have. 
2) Execute an HTTP POST Request on the "/generate" endpoint with mandatory query parameters *typePrefix* and *typeName* as well as the JSON payload in the body of the request.
    * With the *typePrefix* and *typeName* you define the prefix and name for the XL Release CI type for one event.
    For example `http://localhost:8080/generate?typePrefix=github&typeName=PullRequestEvent` would be used to generate a definition for an XL Release event type `github.PullRequestEvent`. This, together with the [example JSON object](https://developer.github.com/v3/activity/events/types/#pullrequestevent) of the webhook payload in the body is enough for the tool to generate the definitions.
3) The content of `synthetic.xml` and `[_typeName_]EventSource.py` will be printed in the console.
4) Polish the content of the files to get them ready for a plugin:
    * Remove duplicate definitions for equal entities
    * Rename nested CIs of the event from the generic name that the generator provides to a more meaningful name
    * Add a definition for the actual EventSource to the synthetic.xml (this is not done by the generator).
