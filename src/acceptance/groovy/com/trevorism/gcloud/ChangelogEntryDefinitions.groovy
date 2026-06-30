package com.trevorism.gcloud

/**
 * Changelog Entry API acceptance steps. Registered at script top level with
 * capitalized When/Then (cucumber-groovy); step text kept distinct from
 * ContentRootDefinitions to avoid DuplicateStepDefinitionException.
 */

this.metaClass.mixin(io.cucumber.groovy.Hooks)
this.metaClass.mixin(io.cucumber.groovy.EN)

String base = "https://changelog.project.trevorism.com"
def entries

When(/I request the changelog entries/) { ->
    def conn = new URL("${base}/api/entry").openConnection() as java.net.HttpURLConnection
    conn.requestMethod = "GET"
    int code = conn.responseCode
    def stream = (code >= 200 && code < 400) ? conn.inputStream : conn.errorStream
    entries = [code: code, body: stream != null ? stream.text : ""]
    conn.disconnect()
}

Then(/the changelog entries are returned as a JSON array/) { ->
    assert entries.code == 200 : "Expected 200 but got ${entries.code}"
    String trimmed = entries.body.trim()
    assert trimmed.startsWith("[") && trimmed.endsWith("]") :
            "Expected a JSON array but got: ${trimmed.take(200)}"
}
