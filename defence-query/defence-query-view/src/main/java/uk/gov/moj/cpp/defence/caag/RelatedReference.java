package uk.gov.moj.cpp.defence.caag;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;

public class RelatedReference {
  private final UUID prosecutionCaseId;

  private final String relatedReference;

  private final UUID relatedReferenceId;

  @JsonCreator
  public RelatedReference(final UUID prosecutionCaseId, final String relatedReference, final UUID relatedReferenceId) {
    this.prosecutionCaseId = prosecutionCaseId;
    this.relatedReference = relatedReference;
    this.relatedReferenceId = relatedReferenceId;
  }

  public UUID getProsecutionCaseId() {
    return prosecutionCaseId;
  }

  public String getRelatedReference() {
    return relatedReference;
  }

  public UUID getRelatedReferenceId() {
    return relatedReferenceId;
  }

  public static Builder relatedReference() {
    return new RelatedReference.Builder();
  }

  public static class Builder {
    private UUID prosecutionCaseId;

    private String relatedReference;

    private UUID relatedReferenceId;

    public Builder withProsecutionCaseId(final UUID prosecutionCaseId) {
      this.prosecutionCaseId = prosecutionCaseId;
      return this;
    }

    public Builder withRelatedReference(final String relatedReference) {
      this.relatedReference = relatedReference;
      return this;
    }

    public Builder withRelatedReferenceId(final UUID relatedReferenceId) {
      this.relatedReferenceId = relatedReferenceId;
      return this;
    }

    public Builder withValuesFrom(final RelatedReference relatedReference) {
      this.prosecutionCaseId = relatedReference.getProsecutionCaseId();
      this.relatedReference = relatedReference.getRelatedReference();
      this.relatedReferenceId = relatedReference.getRelatedReferenceId();
      return this;
    }

    public RelatedReference build() {
      return new RelatedReference(prosecutionCaseId, relatedReference, relatedReferenceId);
    }
  }
}
