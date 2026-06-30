package com.trevorism.controller

/**
 * Cucumber step definitions for the Context Root acceptance tests.
 */

this.metaClass.mixin(io.cucumber.groovy.Hooks)
this.metaClass.mixin(io.cucumber.groovy.EN)

def endpoint
def lastResponseBody

ContentRootDefinitions() {
    this.endpoint = "https://changelog.project.trevorism.com"

    when("I send a GET request to /api/") {
        def url = "${endpoint}/api/"
        lastResponseBody = new URL(url).getText()
    }

    when("I send a GET request to /api/ping") {
        def url = "${endpoint}/api/ping"
        lastResponseBody = new URL(url).getText()
    }

    then("the response status is 200") {
        assert lastResponseBody != null : "Response body was not captured"
        def url = endpoint + (lastResponseBody.contains("/ping") ? "/api/ping" : "/api/")
        def conn = new URL(url).openConnection() as java.net.HttpURLConnection
        try {
            conn.requestMethod = "GET"
            def status = conn.responseCode
            assert status == 200 : "Expected status 200 but got ${status}"
        } finally {
            conn.disconnect()
        }
    }

    then("the response body contains /help") {
        assert lastResponseBody != null : "Response body was not captured"
        assert lastResponseBody.contains("/help") :
            "Expected response body to contain '/help' but got: ${lastResponseBody}"
    }

    then("the response body is pong") {
        assert lastResponseBody != null : "Response body was not captured"
        def trimmed = lastResponseBody.trim()
        assert trimmed == "pong" : "Expected response body to be 'pong' but got: ${trimmed}"
    }
}
