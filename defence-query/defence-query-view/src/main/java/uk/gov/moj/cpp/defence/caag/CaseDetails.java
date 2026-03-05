package uk.gov.moj.cpp.defence.caag;

import uk.gov.justice.core.courts.CivilFees;
import uk.gov.justice.core.courts.MigrationSourceSystem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;

public class CaseDetails {
  private final List<String> caseMarkers;

  private final String caseStatus;

  private final String caseURN;

  private final List<CivilFees> civilFees;

  private final UUID groupId;

  private final String initiationCode;

  private final Boolean isCivil;

  private final Boolean isGroupMaster;

  private final Boolean isGroupMember;

  private final List<RelatedReference> relatedReferenceList;

  private final String relatedUrn;

  private final String removalReason;

  private final MigrationSourceSystem migrationSourceSystem;

  private final HashMap<String, Object> additionalProperties;

  @JsonCreator
  public CaseDetails(final List<String> caseMarkers, final String caseStatus, final String caseURN, final List<CivilFees> civilFees, final UUID groupId, final String initiationCode, final Boolean isCivil, final Boolean isGroupMaster, final Boolean isGroupMember, final List<RelatedReference> relatedReferenceList, final String relatedUrn, final String removalReason, final MigrationSourceSystem migrationSourceSystem, final Map<String, Object> additionalProperties) {
    this.caseMarkers = caseMarkers;
    this.caseStatus = caseStatus;
    this.caseURN = caseURN;
    this.civilFees = civilFees;
    this.groupId = groupId;
    this.initiationCode = initiationCode;
    this.isCivil = isCivil;
    this.isGroupMaster = isGroupMaster;
    this.isGroupMember = isGroupMember;
    this.relatedReferenceList = relatedReferenceList;
    this.relatedUrn = relatedUrn;
    this.removalReason = removalReason;
    this.migrationSourceSystem = migrationSourceSystem;
    this.additionalProperties = additionalProperties == null ? null : new HashMap<>(additionalProperties);
  }

  public List<String> getCaseMarkers() {
    return caseMarkers;
  }

  public String getCaseStatus() {
    return caseStatus;
  }

  public String getCaseURN() {
    return caseURN;
  }

  public List<CivilFees> getCivilFees() {
    return civilFees;
  }

  public UUID getGroupId() {
    return groupId;
  }

  public String getInitiationCode() {
    return initiationCode;
  }

  public Boolean getIsCivil() {
    return isCivil;
  }

  public Boolean getIsGroupMaster() {
    return isGroupMaster;
  }

  public Boolean getIsGroupMember() {
    return isGroupMember;
  }

  public List<RelatedReference> getRelatedReferenceList() {
    return relatedReferenceList;
  }

  public String getRelatedUrn() {
    return relatedUrn;
  }

  public String getRemovalReason() {
    return removalReason;
  }

  public MigrationSourceSystem getMigrationSourceSystem() {
    return migrationSourceSystem;
  }

  public static Builder caseDetails() {
    return new CaseDetails.Builder();
  }

  public Map<String, Object> getAdditionalProperties() {
    return additionalProperties;
  }

  public void setAdditionalProperty(final String name, final Object value) {
    additionalProperties.put(name, value);
  }

  public static class Builder {
    private List<String> caseMarkers;

    private String caseStatus;

    private String caseURN;

    private List<CivilFees> civilFees;

    private UUID groupId;

    private String initiationCode;

    private Boolean isCivil;

    private Boolean isGroupMaster;

    private Boolean isGroupMember;

    private List<RelatedReference> relatedReferenceList;

    private String relatedUrn;

    private String removalReason;

    private MigrationSourceSystem migrationSourceSystem;

    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public Builder withCaseMarkers(final List<String> caseMarkers) {
      this.caseMarkers = caseMarkers;
      return this;
    }

    public Builder withCaseStatus(final String caseStatus) {
      this.caseStatus = caseStatus;
      return this;
    }

    public Builder withCaseURN(final String caseURN) {
      this.caseURN = caseURN;
      return this;
    }

    public Builder withCivilFees(final List<CivilFees> civilFees) {
      this.civilFees = civilFees;
      return this;
    }

    public Builder withGroupId(final UUID groupId) {
      this.groupId = groupId;
      return this;
    }

    public Builder withInitiationCode(final String initiationCode) {
      this.initiationCode = initiationCode;
      return this;
    }

    public Builder withIsCivil(final Boolean isCivil) {
      this.isCivil = isCivil;
      return this;
    }

    public Builder withIsGroupMaster(final Boolean isGroupMaster) {
      this.isGroupMaster = isGroupMaster;
      return this;
    }

    public Builder withIsGroupMember(final Boolean isGroupMember) {
      this.isGroupMember = isGroupMember;
      return this;
    }

    public Builder withRelatedReferenceList(final List<RelatedReference> relatedReferenceList) {
      this.relatedReferenceList = relatedReferenceList;
      return this;
    }

    public Builder withRelatedUrn(final String relatedUrn) {
      this.relatedUrn = relatedUrn;
      return this;
    }

    public Builder withRemovalReason(final String removalReason) {
      this.removalReason = removalReason;
      return this;
    }

    public Builder withMigrationSourceSystem(final MigrationSourceSystem migrationSourceSystem) {
      this.migrationSourceSystem = migrationSourceSystem;
      return this;
    }

    public Builder withAdditionalProperty(final String name, final Object value) {
      additionalProperties.put(name, value);
      return this;
    }

    public CaseDetails build() {
      return new CaseDetails(caseMarkers, caseStatus, caseURN, civilFees, groupId, initiationCode, isCivil, isGroupMaster, isGroupMember, relatedReferenceList, relatedUrn, removalReason, migrationSourceSystem, additionalProperties);
    }
  }
}
