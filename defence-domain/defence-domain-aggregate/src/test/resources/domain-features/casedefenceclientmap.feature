Feature: CaseDefenceClientMap

  Scenario: A defendant is added

    Given a defence client is mapped to a case
    When you addADefendant on a CaseDefenceClientMap using a addDefendantCommand
    Then defendant added


  Scenario: A prosecution case is received when a case is created

    Given no previous events
    When you receiveDetails on a CaseDefenceClientMap with a caseId urn prosecutingAuthority isCivil isGroupMember
    Then prosecution case received

