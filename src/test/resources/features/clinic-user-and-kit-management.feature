Feature: Clinic user and IoT kit management
  As a clinic administrator or physiotherapist
  I want to manage clinic users and patient kits
  So that rehabilitation care is correctly assigned

  @US29
  Scenario: Clinic administrator registers a physiotherapist
    Given an authenticated clinic administrator
    And the license and email are unique in the clinic
    When the administrator registers the physiotherapist
    Then the API should respond with 201 Created
    And the physiotherapist should belong to the administrator's clinic

  @US29
  Scenario: Clinic administrator cannot update another clinic physiotherapist
    Given an authenticated clinic administrator belongs to clinic A
    And the physiotherapist belongs to clinic B
    When the administrator updates the physiotherapist
    Then the API should reject the request

  @US30
  Scenario: Assign an available calibrated IoT kit to a patient
    Given an authenticated clinic user and patient belong to the same clinic
    And an available calibrated IoT kit belongs to that clinic
    When the user assigns the kit to the patient
    Then the API should respond with 200 OK
    And the kit status should be ASSIGNED

  @US30
  Scenario: Reject assignment of a kit that is already assigned
    Given an IoT kit is already assigned to a patient
    When a clinic user assigns the kit to another patient
    Then the API should reject the request
