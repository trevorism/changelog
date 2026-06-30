Feature: Context Root

  Scenario: the context root links to the help page
    Given the changelog application is alive
    When I navigate to the changelog context root
    Then a link to the help page is displayed

  Scenario: ping returns pong
    Given the changelog application is alive
    When I ping the changelog application
    Then pong is returned
