Feature: DefenceClient

  Scenario: Create a defence client whenever the prosecution adds a suspect

   Given no previous events
   When you receiveADefenceClient on a DefenceClient with a defence client details
   Then defence client received

  Scenario: Charge the defence client if the prosecution authority charges the suspect

    Given defence client received
    When you receiveAllegations on a DefenceClient with a receiveAllegationsOnADefenceClientCommand
    Then allegations received against a defence client

  Scenario: Receive URN for a defence client

    Given defence client received
    When you receiveUrn on a DefenceClient with a receiveDefenceClientUrnCommand
    Then urn added to defence client

  Scenario: Record defence instruction details

    Given defence client received
    When you recordInstructionDetails on a DefenceClient with a recordInstructionDetailsCommand
    Then instruction details recorded

  Scenario: Record IDPC details

    Given defence client received
    When you recordIdpcDetails on a DefenceClient with a recordIdpcDetailsCommand
    Then idpc details recorded

  Scenario: Record IDPC accessed by user in an organisation

    Given defence client received
    When you recordIdpcAccess on a DefenceClient with a recordIdpcAccessCommand
    Then idpc access recorded
    And idpc access by organisation recorded

  Scenario: Record IDPC accessed by new organisation

    Given defence client received, idpc access by organisation recorded
    When you recordIdpcAccess on a DefenceClient with a recordIdpcAccesCommandContainingNewOrganisationId
    Then idpc access with new org recorded
    And idpc access by new organisation recorded

  Scenario: Record IDPC accessed by user in same organisation as previous access

    Given defence client received, idpc access by organisation recorded
    When you recordIdpcAccess on a DefenceClient with a recordIdpcAccessCommand
    Then idpc access recorded
