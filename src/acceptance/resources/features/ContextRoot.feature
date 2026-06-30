Feature: Context Root

  Scenario: context root returns a link to the help page
    When I GET the context root path
    Then the context root status is 200
    And the context root body contains the help link

  Scenario: ping returns pong
    When I GET the ping path
    Then the ping status is 200
    And the ping body is pong
