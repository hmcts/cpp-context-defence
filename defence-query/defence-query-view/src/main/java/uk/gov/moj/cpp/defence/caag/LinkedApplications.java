package uk.gov.moj.cpp.defence.caag;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;

public class LinkedApplications {
  private final String applicantDisplayName;

  private final UUID applicantId;

  private final UUID applicationId;

  private final String applicationReference;

  private final String applicationStatus;

  private final String applicationTitle;

  private final Boolean isAppeal;

  private final String removalReason;

  private final List<String> respondentDisplayNames;

  private final List<UUID> respondentIds;

  private final UUID subjectId;

  @JsonCreator
  public LinkedApplications(final String applicantDisplayName, final UUID applicantId, final UUID applicationId, final String applicationReference, final String applicationStatus, final String applicationTitle, final Boolean isAppeal, final String removalReason, final List<String> respondentDisplayNames, final List<UUID> respondentIds, final UUID subjectId) {
    this.applicantDisplayName = applicantDisplayName;
    this.applicantId = applicantId;
    this.applicationId = applicationId;
    this.applicationReference = applicationReference;
    this.applicationStatus = applicationStatus;
    this.applicationTitle = applicationTitle;
    this.isAppeal = isAppeal;
    this.removalReason = removalReason;
    this.respondentDisplayNames = respondentDisplayNames;
    this.respondentIds = respondentIds;
    this.subjectId = subjectId;
  }

  public String getApplicantDisplayName() {
    return applicantDisplayName;
  }

  public UUID getApplicantId() {
    return applicantId;
  }

  public UUID getApplicationId() {
    return applicationId;
  }

  public String getApplicationReference() {
    return applicationReference;
  }

  public String getApplicationStatus() {
    return applicationStatus;
  }

  public String getApplicationTitle() {
    return applicationTitle;
  }

  public Boolean getIsAppeal() {
    return isAppeal;
  }

  public String getRemovalReason() {
    return removalReason;
  }

  public List<String> getRespondentDisplayNames() {
    return respondentDisplayNames;
  }

  public List<UUID> getRespondentIds() {
    return respondentIds;
  }

  public UUID getSubjectId() {
    return subjectId;
  }

  public static Builder linkedApplications() {
    return new LinkedApplications.Builder();
  }

  public static class Builder {
    private String applicantDisplayName;

    private UUID applicantId;

    private UUID applicationId;

    private String applicationReference;

    private String applicationStatus;

    private String applicationTitle;

    private Boolean isAppeal;

    private String removalReason;

    private List<String> respondentDisplayNames;

    private List<UUID> respondentIds;

    private UUID subjectId;

    public Builder withApplicantDisplayName(final String applicantDisplayName) {
      this.applicantDisplayName = applicantDisplayName;
      return this;
    }

    public Builder withApplicantId(final UUID applicantId) {
      this.applicantId = applicantId;
      return this;
    }

    public Builder withApplicationId(final UUID applicationId) {
      this.applicationId = applicationId;
      return this;
    }

    public Builder withApplicationReference(final String applicationReference) {
      this.applicationReference = applicationReference;
      return this;
    }

    public Builder withApplicationStatus(final String applicationStatus) {
      this.applicationStatus = applicationStatus;
      return this;
    }

    public Builder withApplicationTitle(final String applicationTitle) {
      this.applicationTitle = applicationTitle;
      return this;
    }

    public Builder withIsAppeal(final Boolean isAppeal) {
      this.isAppeal = isAppeal;
      return this;
    }

    public Builder withRemovalReason(final String removalReason) {
      this.removalReason = removalReason;
      return this;
    }

    public Builder withRespondentDisplayNames(final List<String> respondentDisplayNames) {
      this.respondentDisplayNames = respondentDisplayNames;
      return this;
    }

    public Builder withRespondentIds(final List<UUID> respondentIds) {
      this.respondentIds = respondentIds;
      return this;
    }

    public Builder withSubjectId(final UUID subjectId) {
      this.subjectId = subjectId;
      return this;
    }

    public Builder withValuesFrom(final LinkedApplications linkedApplications) {
      this.applicantDisplayName = linkedApplications.getApplicantDisplayName();
      this.applicantId = linkedApplications.getApplicantId();
      this.applicationId = linkedApplications.getApplicationId();
      this.applicationReference = linkedApplications.getApplicationReference();
      this.applicationStatus = linkedApplications.getApplicationStatus();
      this.applicationTitle = linkedApplications.getApplicationTitle();
      this.isAppeal = linkedApplications.getIsAppeal();
      this.removalReason = linkedApplications.getRemovalReason();
      this.respondentDisplayNames = linkedApplications.getRespondentDisplayNames();
      this.respondentIds = linkedApplications.getRespondentIds();
      this.subjectId = linkedApplications.getSubjectId();
      return this;
    }

    public LinkedApplications build() {
      return new LinkedApplications(applicantDisplayName, applicantId, applicationId, applicationReference, applicationStatus, applicationTitle, isAppeal, removalReason, respondentDisplayNames, respondentIds, subjectId);
    }
  }
}
