Feature: Context Root

  Scenario: context root returns a link to the help page
    When I send a GET request to /api/
    Then the response status is 200
    And the response body contains /help

  Scenario: ping returns pong
    When I send a GET request to /api/ping
    Then the response status is 200
    And the response body is pong
