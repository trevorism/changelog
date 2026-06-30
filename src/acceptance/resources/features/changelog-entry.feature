Feature: Changelog Entry API

  Scenario: listing entries returns a JSON array
    When I request the changelog entries
    Then the changelog entries are returned as a JSON array
