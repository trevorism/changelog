package com.trevorism.model

import groovy.transform.CompileStatic
import groovy.transform.ToString
import java.util.Date

@CompileStatic
@ToString(includeNames = true)
class ChangelogEntry {

    String id
    Date date
    String repository
    String summary

    ChangelogEntry() {}

    ChangelogEntry(String id, Date date, String repository, String summary) {
        this.id = id
        this.date = date
        this.repository = repository
        this.summary = summary
    }
}
