package com.trevorism.controller

import com.trevorism.data.FastDatastoreRepository
import com.trevorism.data.PingingDatastoreRepository
import com.trevorism.data.Repository
import com.trevorism.https.SecureHttpClient
import com.trevorism.model.ChangelogEntry
import com.trevorism.secure.Roles
import com.trevorism.secure.Secure
import io.micronaut.core.annotation.Nullable
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.annotation.Status
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Controller("/api/entry")
class ChangelogEntryController {

    private static final Logger log = LoggerFactory.getLogger(ChangelogEntryController)
    private final Repository<ChangelogEntry> repository

    ChangelogEntryController() {
        this.repository = new FastDatastoreRepository<>(ChangelogEntry)
    }

    @Tag(name = "Changelog Entry Operations")
    @Operation(summary = "List all changelog entries, sorted by date desc.")
    @Get(value = "/", produces = MediaType.APPLICATION_JSON)
    List<ChangelogEntry> list() {
        log.info("Listing changelog entries")
        def entries = repository.list()
        return entries.sort { a, b -> b.date.compareTo(a.date) }
    }

    @Tag(name = "Changelog Entry Operations")
    @Operation(summary = "Get a single changelog entry by id.")
    @Get(value = "{id}", produces = MediaType.APPLICATION_JSON)
    ChangelogEntry get(@PathVariable String id) {
        log.info("Getting changelog entry: {}", id)
        return repository.get(id)
    }

    @Tag(name = "Changelog Entry Operations")
    @Operation(summary = "Create a new changelog entry. Secure: Roles.SYSTEM.")
    @Post(value = "/", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    @Secure(Roles.SYSTEM)
    @Status(HttpStatus.CREATED)
    ChangelogEntry create(@Body Map<String, Object> body) {
        log.info("Creating changelog entry")
        // Leave id unset: the datastore assigns the id on create (it rejects a
        // client-supplied id with "Unable to create").
        def entry = new ChangelogEntry(
                date: parseDate(body.date),
                repository: body.repository as String,
                summary: body.summary as String
        )
        return repository.create(entry)
    }

    private static Date parseDate(Object raw) {
        // Honor a supplied ISO-8601 date (the offline generator backfills historical
        // commit dates); default to now only when none is given. Accepts full
        // timestamps ("2024-03-01T12:00:00Z") and date-only values ("2024-03-01").
        if (!raw) {
            return new Date()
        }
        String value = raw.toString().trim()
        try {
            return Date.from(java.time.Instant.parse(value))
        } catch (Exception ignored) {
        }
        try {
            return Date.from(java.time.LocalDate.parse(value).atStartOfDay(java.time.ZoneOffset.UTC).toInstant())
        } catch (Exception ignored) {
        }
        return new Date()
    }

    @Tag(name = "Changelog Entry Operations")
    @Operation(summary = "Delete a changelog entry by id. Secure: Roles.SYSTEM.")
    @Delete(value = "{id}")
    @Secure(Roles.SYSTEM)
    void delete(@PathVariable String id) {
        log.info("Deleting changelog entry: {}", id)
        repository.delete(id)
    }
}
