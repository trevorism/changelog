package com.trevorism.controller

/**
 * Cucumber step definitions for the Changelog Entry API acceptance tests.
 */

this.metaClass.mixin(io.cucumber.groovy.Hooks)
this.metaClass.mixin(io.cucumber.groovy.EN)

def endpoint
def lastResponseBody

ChangelogEntryDefinitions() {
    // Resolve the deployed service base URL from the acceptance plugin config
    this.endpoint = System.getProperty("endpoint") ?: "http://localhost:8080"

    when("I send a GET request to /api/entry") {
        def url = "${endpoint}/api/entry"
        lastResponseBody = new URL(url).getText()
    }

    then("the response status is 200") {
        assert lastResponseBody != null : "Response body was not captured"
        def url = "${endpoint}/api/entry"
        def conn = new URL(url).openConnection() as java.net.HttpURLConnection
        try {
            conn.requestMethod = "GET"
            def status = conn.responseCode
            assert status == 200 : "Expected status 200 but got ${status}"
        } finally {
            conn.disconnect()
        }
    }

    then("the response body is a JSON array") {
        assert lastResponseBody != null : "Response body was not captured"
        def trimmed = lastResponseBody.trim()
        assert trimmed.startsWith("[") && trimmed.endsWith("]") :
            "Expected response body to be a JSON array but got: ${trimmed.take(200)}"
    }
}
