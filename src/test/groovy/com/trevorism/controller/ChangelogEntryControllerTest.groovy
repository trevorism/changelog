package com.trevorism.controller

import org.junit.jupiter.api.Test

/**
 * Unit tests for ChangelogEntryController.
 * Does NOT call datastore methods (list/get/create/delete) — those are covered by Cucumber acceptance tests.
 */
class ChangelogEntryControllerTest {

    @Test
    void testControllerInstantiation() {
        def controller = new ChangelogEntryController()
        assert controller != null
    }
}
