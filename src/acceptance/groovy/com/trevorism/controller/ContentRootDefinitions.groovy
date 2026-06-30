package com.trevorism.controller

/**
 * Context-root acceptance steps. Cucumber-Groovy glue MUST be registered at script
 * top level with capitalized Given/When/Then (see other trevorism services, e.g.
 * catalog) — steps wrapped in a constructor/method are never registered.
 */

this.metaClass.mixin(io.cucumber.groovy.Hooks)
this.metaClass.mixin(io.cucumber.groovy.EN)

String base = "https://changelog.project.trevorism.com"
def contextRoot
def ping

def httpGet = { String url ->
    def conn = new URL(url).openConnection() as java.net.HttpURLConnection
    conn.requestMethod = "GET"
    int code = conn.responseCode
    def stream = (code >= 200 && code < 400) ? conn.inputStream : conn.errorStream
    String body = stream != null ? stream.text : ""
    conn.disconnect()
    return [code: code, body: body]
}

Given(/the changelog application is alive/) { ->
    httpGet("${base}/api/ping")
}

When(/I navigate to the changelog context root/) { ->
    contextRoot = httpGet("${base}/api/")
}

Then(/a link to the help page is displayed/) { ->
    assert contextRoot.code == 200 : "Expected 200 but got ${contextRoot.code}"
    assert contextRoot.body.contains("/help") : "Expected body to contain /help but got: ${contextRoot.body}"
}

When(/I ping the changelog application/) { ->
    ping = httpGet("${base}/api/ping")
}

Then(/pong is returned/) { ->
    assert ping.code == 200 : "Expected 200 but got ${ping.code}"
    assert ping.body.trim() == "pong" : "Expected pong but got: ${ping.body}"
}
