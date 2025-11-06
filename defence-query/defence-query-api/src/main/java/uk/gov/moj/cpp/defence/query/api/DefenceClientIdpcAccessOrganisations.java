package uk.gov.moj.cpp.defence.query.api;

import java.util.List;
import java.util.UUID;


public class DefenceClientIdpcAccessOrganisations {
  private final UUID defenceClientId;
  private final UUID caseId;
  private final UUID defendantId;
  private final AssociatedOrganisationVO associatedOrganisationVO;
  private final AssociatedOrganisationVO lastAssociatedOrganisationVO;

  private String errorMessage;

  private final List<OrderedOrganisationDetailsVO> idpcAccessingOrganisations;

  public DefenceClientIdpcAccessOrganisations(final UUID defenceClientId, final UUID caseId, final UUID defendantId,
                                              final AssociatedOrganisationVO associatedOrganisationVO,
                                              final AssociatedOrganisationVO lastAssociatedOrganisationVO, final List<OrderedOrganisationDetailsVO> idpcAccessingOrganisations) {
    this.defenceClientId = defenceClientId;
    this.caseId = caseId;
    this.defendantId = defendantId;
    this.associatedOrganisationVO = associatedOrganisationVO;
    this.lastAssociatedOrganisationVO = lastAssociatedOrganisationVO;
    this.idpcAccessingOrganisations = idpcAccessingOrganisations;
  }

  public UUID getDefenceClientId() {
    return defenceClientId;
  }

  public List<OrderedOrganisationDetailsVO> getIdpcAccessingOrganisations() {
    return idpcAccessingOrganisations;
  }

  public UUID getCaseId() {
    return caseId;
  }

  public UUID getDefendantId() {
    return defendantId;
  }

  public AssociatedOrganisationVO getAssociatedOrganisationVO() {
    return associatedOrganisationVO;
  }

  public AssociatedOrganisationVO getLastAssociatedOrganisationVO() {
    return lastAssociatedOrganisationVO;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(final String errorMessage) {
    this.errorMessage = errorMessage;
  }

}
