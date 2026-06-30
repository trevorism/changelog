package com.trevorism.controller

import com.trevorism.model.Entry
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.time.LocalDate
import java.time.ZoneId
import java.util.stream.Collectors

@Controller("/api/entry")
class EntryController {

    private static final Logger log = LoggerFactory.getLogger(EntryController)

    @Tag(name = "Entry Operations")
    @Operation(summary = "Returns all changelog entries, newest first")
    @ApiResponse(
            responseCode = "200",
            content = @ArraySchema(schema = @Schema(type = "string"))
    )
    @Get(produces = MediaType.APPLICATION_JSON)
    List<Entry> list() {
        log.info("Listing changelog entries")

        def now = LocalDate.now(ZoneId.of("UTC"))

        List<Entry> entries = [
            new Entry("1", "trevorism/platform", "Added timeline view for changelog entries",
                      now.minusDays(1).format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)),
            new Entry("2", "trevorism/changelog", "Initial release of the changelog service",
                      now.minusDays(5).format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)),
            new Entry("3", "trevorism/secure-utils", "JWT signing key rotation support",
                      (now.minusDays(10)).format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE))
        ]

        entries.sort { a, b -> b.date.compareTo(a.date) }
    }
}
