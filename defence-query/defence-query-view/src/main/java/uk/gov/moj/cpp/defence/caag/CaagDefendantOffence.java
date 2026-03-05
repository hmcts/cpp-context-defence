package uk.gov.moj.cpp.defence.caag;

import uk.gov.justice.core.courts.AllocationDecision;
import uk.gov.justice.core.courts.CustodyTimeLimit;
import uk.gov.justice.core.courts.Plea;
import uk.gov.justice.core.courts.ReportingRestriction;
import uk.gov.justice.core.courts.Verdict;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;

public class CaagDefendantOffence {
  private final AllocationDecision allocationDecision;

  private final List<CaagResult> caagResults;

  private final Integer count;

  private final CustodyTimeLimit custodyTimeLimit;

  private final String endDate;

  private final UUID id;

  private final String indictmentParticular;

  private final String offenceCode;

  private final String offenceLegislation;

  private final String offenceLegislationWelsh;

  private final String offenceTitle;

  private final String offenceTitleWelsh;

  private final Plea plea;

  private final List<ReportingRestriction> reportingRestrictions;

  private final String startDate;

  private final Verdict verdict;

  private final String wording;

  private final String wordingWelsh;

  @JsonCreator
  public CaagDefendantOffence(final AllocationDecision allocationDecision, final List<CaagResult> caagResults, final Integer count, final CustodyTimeLimit custodyTimeLimit, final String endDate, final UUID id, final String indictmentParticular, final String offenceCode, final String offenceLegislation, final String offenceLegislationWelsh, final String offenceTitle, final String offenceTitleWelsh, final Plea plea, final List<ReportingRestriction> reportingRestrictions, final String startDate, final Verdict verdict, final String wording, final String wordingWelsh) {
    this.allocationDecision = allocationDecision;
    this.caagResults = caagResults;
    this.count = count;
    this.custodyTimeLimit = custodyTimeLimit;
    this.endDate = endDate;
    this.id = id;
    this.indictmentParticular = indictmentParticular;
    this.offenceCode = offenceCode;
    this.offenceLegislation = offenceLegislation;
    this.offenceLegislationWelsh = offenceLegislationWelsh;
    this.offenceTitle = offenceTitle;
    this.offenceTitleWelsh = offenceTitleWelsh;
    this.plea = plea;
    this.reportingRestrictions = reportingRestrictions;
    this.startDate = startDate;
    this.verdict = verdict;
    this.wording = wording;
    this.wordingWelsh = wordingWelsh;
  }

  public AllocationDecision getAllocationDecision() {
    return allocationDecision;
  }

  public List<CaagResult> getCaagResults() {
    return caagResults;
  }

  public Integer getCount() {
    return count;
  }

  public CustodyTimeLimit getCustodyTimeLimit() {
    return custodyTimeLimit;
  }

  public String getEndDate() {
    return endDate;
  }

  public UUID getId() {
    return id;
  }

  public String getIndictmentParticular() {
    return indictmentParticular;
  }

  public String getOffenceCode() {
    return offenceCode;
  }

  public String getOffenceLegislation() {
    return offenceLegislation;
  }

  public String getOffenceLegislationWelsh() {
    return offenceLegislationWelsh;
  }

  public String getOffenceTitle() {
    return offenceTitle;
  }

  public String getOffenceTitleWelsh() {
    return offenceTitleWelsh;
  }

  public Plea getPlea() {
    return plea;
  }

  public List<ReportingRestriction> getReportingRestrictions() {
    return reportingRestrictions;
  }

  public String getStartDate() {
    return startDate;
  }

  public Verdict getVerdict() {
    return verdict;
  }

  public String getWording() {
    return wording;
  }

  public String getWordingWelsh() {
    return wordingWelsh;
  }

  public static Builder caagDefendantOffence() {
    return new CaagDefendantOffence.Builder();
  }

  public static class Builder {
    private AllocationDecision allocationDecision;

    private List<CaagResult> caagResults;

    private Integer count;

    private CustodyTimeLimit custodyTimeLimit;

    private String endDate;

    private UUID id;

    private String indictmentParticular;

    private String offenceCode;

    private String offenceLegislation;

    private String offenceLegislationWelsh;

    private String offenceTitle;

    private String offenceTitleWelsh;

    private Plea plea;

    private List<ReportingRestriction> reportingRestrictions;

    private String startDate;

    private Verdict verdict;

    private String wording;

    private String wordingWelsh;

    public Builder withAllocationDecision(final AllocationDecision allocationDecision) {
      this.allocationDecision = allocationDecision;
      return this;
    }

    public Builder withCaagResults(final List<CaagResult> caagResults) {
      this.caagResults = caagResults;
      return this;
    }

    public Builder withCount(final Integer count) {
      this.count = count;
      return this;
    }

    public Builder withCustodyTimeLimit(final CustodyTimeLimit custodyTimeLimit) {
      this.custodyTimeLimit = custodyTimeLimit;
      return this;
    }

    public Builder withEndDate(final String endDate) {
      this.endDate = endDate;
      return this;
    }

    public Builder withId(final UUID id) {
      this.id = id;
      return this;
    }

    public Builder withIndictmentParticular(final String indictmentParticular) {
      this.indictmentParticular = indictmentParticular;
      return this;
    }

    public Builder withOffenceCode(final String offenceCode) {
      this.offenceCode = offenceCode;
      return this;
    }

    public Builder withOffenceLegislation(final String offenceLegislation) {
      this.offenceLegislation = offenceLegislation;
      return this;
    }

    public Builder withOffenceLegislationWelsh(final String offenceLegislationWelsh) {
      this.offenceLegislationWelsh = offenceLegislationWelsh;
      return this;
    }

    public Builder withOffenceTitle(final String offenceTitle) {
      this.offenceTitle = offenceTitle;
      return this;
    }

    public Builder withOffenceTitleWelsh(final String offenceTitleWelsh) {
      this.offenceTitleWelsh = offenceTitleWelsh;
      return this;
    }

    public Builder withPlea(final Plea plea) {
      this.plea = plea;
      return this;
    }

    public Builder withReportingRestrictions(final List<ReportingRestriction> reportingRestrictions) {
      this.reportingRestrictions = reportingRestrictions;
      return this;
    }

    public Builder withStartDate(final String startDate) {
      this.startDate = startDate;
      return this;
    }

    public Builder withVerdict(final Verdict verdict) {
      this.verdict = verdict;
      return this;
    }

    public Builder withWording(final String wording) {
      this.wording = wording;
      return this;
    }

    public Builder withWordingWelsh(final String wordingWelsh) {
      this.wordingWelsh = wordingWelsh;
      return this;
    }

    public Builder withValuesFrom(final CaagDefendantOffence caagDefendantOffence) {
      this.allocationDecision = caagDefendantOffence.getAllocationDecision();
      this.caagResults = caagDefendantOffence.getCaagResults();
      this.count = caagDefendantOffence.getCount();
      this.custodyTimeLimit = caagDefendantOffence.getCustodyTimeLimit();
      this.endDate = caagDefendantOffence.getEndDate();
      this.id = caagDefendantOffence.getId();
      this.indictmentParticular = caagDefendantOffence.getIndictmentParticular();
      this.offenceCode = caagDefendantOffence.getOffenceCode();
      this.offenceLegislation = caagDefendantOffence.getOffenceLegislation();
      this.offenceLegislationWelsh = caagDefendantOffence.getOffenceLegislationWelsh();
      this.offenceTitle = caagDefendantOffence.getOffenceTitle();
      this.offenceTitleWelsh = caagDefendantOffence.getOffenceTitleWelsh();
      this.plea = caagDefendantOffence.getPlea();
      this.reportingRestrictions = caagDefendantOffence.getReportingRestrictions();
      this.startDate = caagDefendantOffence.getStartDate();
      this.verdict = caagDefendantOffence.getVerdict();
      this.wording = caagDefendantOffence.getWording();
      this.wordingWelsh = caagDefendantOffence.getWordingWelsh();
      return this;
    }

    public CaagDefendantOffence build() {
      return new CaagDefendantOffence(allocationDecision, caagResults, count, custodyTimeLimit, endDate, id, indictmentParticular, offenceCode, offenceLegislation, offenceLegislationWelsh, offenceTitle, offenceTitleWelsh, plea, reportingRestrictions, startDate, verdict, wording, wordingWelsh);
    }
  }
}
