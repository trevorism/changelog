Feature: Changelog Entry API

  Scenario: GET /api/entry returns 200 and a JSON array
    When I send a GET request to /api/entry
    Then the response status is 200
    And the response body is a JSON array
