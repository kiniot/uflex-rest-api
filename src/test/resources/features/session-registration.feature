Feature: Rehabilitation session registration
  As a patient
  I want to register an exercise session
  So that the clinic can review my rehabilitation progress

  @pending-implementation @TS34 @US13
  Scenario: Register a completed rehabilitation session
    Given an authenticated patient with an active treatment plan
    And the patient has completed the assigned routine
    When the patient submits the session repetitions, duration, and ROM samples
    Then the API should respond with 201 Created
    And the session should be associated with the authenticated patient and clinic

  @pending-implementation @US14
  Scenario: Report pain when finishing a session
    Given an authenticated patient has completed a rehabilitation session
    When the patient reports a pain level of 6
    Then the pain report should be stored with the session
    And the API should respond with 200 OK
