Feature: Patient rehabilitation metrics query
  As a physiotherapist
  I want to query patient ROM metrics
  So that I can review rehabilitation progress

  @pending-implementation @TS35 @US23
  Scenario: Query ROM metrics for a patient in the same clinic
    Given an authenticated physiotherapist belongs to clinic A
    And the patient belongs to clinic A
    And the patient has registered rehabilitation sessions
    When the physiotherapist requests the patient ROM metrics
    Then the API should respond with 200 OK
    And the response should contain ROM values grouped by exercise and date

  @pending-implementation @TS35
  Scenario: Reject metrics query for a patient in another clinic
    Given an authenticated physiotherapist belongs to clinic A
    And the patient belongs to clinic B
    When the physiotherapist requests the patient ROM metrics
    Then the API should respond with 403 Forbidden
