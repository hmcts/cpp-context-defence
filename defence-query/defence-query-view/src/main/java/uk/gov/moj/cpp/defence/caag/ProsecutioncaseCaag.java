package uk.gov.moj.cpp.defence.caag;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;

public class ProsecutioncaseCaag {
  private final CaseDetails caseDetails;

  private final String caseId;

  private final List<Defendants> defendants;

  private final List<LinkedApplications> linkedApplications;

  private final ProsecutorDetails prosecutorDetails;

  @JsonCreator
  public ProsecutioncaseCaag(final CaseDetails caseDetails, final String caseId, final List<Defendants> defendants, final List<LinkedApplications> linkedApplications, final ProsecutorDetails prosecutorDetails) {
    this.caseDetails = caseDetails;
    this.caseId = caseId;
    this.defendants = defendants;
    this.linkedApplications = linkedApplications;
    this.prosecutorDetails = prosecutorDetails;
  }

  public CaseDetails getCaseDetails() {
    return caseDetails;
  }

  public String getCaseId() {
    return caseId;
  }

  public List<Defendants> getDefendants() {
    return defendants;
  }

  public List<LinkedApplications> getLinkedApplications() {
    return linkedApplications;
  }

  public ProsecutorDetails getProsecutorDetails() {
    return prosecutorDetails;
  }

  public static Builder prosecutioncaseCaag() {
    return new ProsecutioncaseCaag.Builder();
  }

  public static class Builder {
    private CaseDetails caseDetails;

    private String caseId;

    private List<Defendants> defendants;

    private List<LinkedApplications> linkedApplications;

    private ProsecutorDetails prosecutorDetails;

    public Builder withCaseDetails(final CaseDetails caseDetails) {
      this.caseDetails = caseDetails;
      return this;
    }

    public Builder withCaseId(final String caseId) {
      this.caseId = caseId;
      return this;
    }

    public Builder withDefendants(final List<Defendants> defendants) {
      this.defendants = defendants;
      return this;
    }

    public Builder withLinkedApplications(final List<LinkedApplications> linkedApplications) {
      this.linkedApplications = linkedApplications;
      return this;
    }

    public Builder withProsecutorDetails(final ProsecutorDetails prosecutorDetails) {
      this.prosecutorDetails = prosecutorDetails;
      return this;
    }

    public Builder withValuesFrom(final ProsecutioncaseCaag prosecutioncaseCaag) {
      this.caseDetails = prosecutioncaseCaag.getCaseDetails();
      this.caseId = prosecutioncaseCaag.getCaseId();
      this.defendants = prosecutioncaseCaag.getDefendants();
      this.linkedApplications = prosecutioncaseCaag.getLinkedApplications();
      this.prosecutorDetails = prosecutioncaseCaag.getProsecutorDetails();
      return this;
    }

    public ProsecutioncaseCaag build() {
      return new ProsecutioncaseCaag(caseDetails, caseId, defendants, linkedApplications, prosecutorDetails);
    }
  }
}
