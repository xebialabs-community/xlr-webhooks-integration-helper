# XLR WebHook event type Parser generator app

### Utility Spring Boot App for generating xl-release WebHook event type parser definitions

This app can generate the content of files necessary to define a webhook event parser for xl-release, given a JSON payload example of a request. 

The files being:

* synthetic.xml - defines the xl-release CI for a WebHook event
* parser.py - python script which performs the mapping from JSON object to the WebHook Event CI

### Usage

1) Run the web app
2) Execute an HTTP POST Request on the /generate endpoint with query params typePrefix and typeName as well as the JSON payload in the body of the request
3) The content of synthetic.xml and parser.py is printed in the console
