Feature: JWT multitenant access
  As the platform
  I want every protected request scoped to the JWT tenant
  So that clinic data cannot leak across tenants

  @TS37
  Scenario: Accept a valid tenant-aware JWT
    Given a valid JWT for a clinic user in clinic A
    When the user requests a protected clinic resource
    Then the API should authenticate the user
    And the request should execute in clinic A

  @TS37
  Scenario: Reject a missing or invalid JWT
    Given no valid JWT is present
    When a client requests a protected resource
    Then the API should respond with 401 Unauthorized

  @TS37
  Scenario: Reject access to another clinic patient
    Given a valid JWT for a physiotherapist in clinic A
    And the requested patient belongs to clinic B
    When the physiotherapist requests the patient's treatment plan
    Then the API should reject the request
