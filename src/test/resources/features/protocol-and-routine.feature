Feature: Protocol creation and routine consultation
  As a physiotherapist and patient
  I want treatment protocols to be created and consulted
  So that daily rehabilitation can follow an assigned plan

  @US21
  Scenario: Create a protocol for a patient in the authenticated clinic
    Given an authenticated physiotherapist and patient belong to the same clinic
    And every exercise in the protocol belongs to that clinic
    When the physiotherapist creates a treatment plan with routines
    Then the API should respond with 201 Created
    And the protocol status should be SCHEDULED

  @US21
  Scenario: Reject a protocol containing an exercise from another clinic
    Given an authenticated physiotherapist belongs to clinic A
    And an exercise belongs to clinic B
    When the physiotherapist includes that exercise in a protocol
    Then the API should reject the protocol as a tenant conflict

  @US11
  Scenario: Patient consults the active routine
    Given an authenticated patient has an active treatment plan
    When the patient requests the active treatment plan
    Then the API should respond with 200 OK
    And the response should contain the assigned routines and exercise series
