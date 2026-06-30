package com.trevorism.model

import io.micronaut.core.annotation.Introspected

@Introspected
class Entry {

    String id
    String repository
    String summary
    String date

    Entry() {}

    Entry(String id, String repository, String summary, String date) {
        this.id = id
        this.repository = repository
        this.summary = summary
        this.date = date
    }
}
