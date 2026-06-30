package com.trevorism.controller

/**
 * Cucumber step definitions for the Context Root acceptance tests.
 *
 * Step text is kept distinct from ChangelogEntryDefinitions (e.g. "the context root
 * status is 200") so Cucumber does not see duplicate step definitions across glue
 * files. The HTTP status is captured in the When step, not re-fetched in the Then.
 */

this.metaClass.mixin(io.cucumber.groovy.Hooks)
this.metaClass.mixin(io.cucumber.groovy.EN)

def endpoint
def lastStatus
def lastBody

ContentRootDefinitions() {
    this.endpoint = "https://changelog.project.trevorism.com"

    def get = { String path ->
        def conn = new URL("${endpoint}${path}").openConnection() as java.net.HttpURLConnection
        try {
            conn.requestMethod = "GET"
            lastStatus = conn.responseCode
            def stream = (lastStatus >= 200 && lastStatus < 400) ? conn.inputStream : conn.errorStream
            lastBody = stream != null ? stream.text : ""
        } finally {
            conn.disconnect()
        }
    }

    when("I GET the context root path") { get("/api/") }
    when("I GET the ping path") { get("/api/ping") }

    then("the context root status is 200") {
        assert lastStatus == 200 : "Expected status 200 but got ${lastStatus}"
    }
    then("the context root body contains the help link") {
        assert lastBody?.contains("/help") : "Expected body to contain '/help' but got: ${lastBody}"
    }
    then("the ping status is 200") {
        assert lastStatus == 200 : "Expected status 200 but got ${lastStatus}"
    }
    then("the ping body is pong") {
        assert lastBody?.trim() == "pong" : "Expected body 'pong' but got: ${lastBody}"
    }
}
