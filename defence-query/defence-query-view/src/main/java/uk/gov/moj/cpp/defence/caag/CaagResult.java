package uk.gov.moj.cpp.defence.caag;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;

public class CaagResult {
  private final String amendedBy;

  private final String amendmentDate;

  private final String amendmentReason;

  private final List<CaagResultPrompt> caagResultPrompts;

  private final UUID id;

  private final String label;

  private final String lastSharedDateTime;

  private final String orderedDate;

  private final List<String> usergroups;

  @JsonCreator
  public CaagResult(final String amendedBy, final String amendmentDate, final String amendmentReason, final List<CaagResultPrompt> caagResultPrompts, final UUID id, final String label, final String lastSharedDateTime, final String orderedDate, final List<String> usergroups) {
    this.amendedBy = amendedBy;
    this.amendmentDate = amendmentDate;
    this.amendmentReason = amendmentReason;
    this.caagResultPrompts = caagResultPrompts;
    this.id = id;
    this.label = label;
    this.lastSharedDateTime = lastSharedDateTime;
    this.orderedDate = orderedDate;
    this.usergroups = usergroups;
  }

  public String getAmendedBy() {
    return amendedBy;
  }

  public String getAmendmentDate() {
    return amendmentDate;
  }

  public String getAmendmentReason() {
    return amendmentReason;
  }

  public List<CaagResultPrompt> getCaagResultPrompts() {
    return caagResultPrompts;
  }

  public UUID getId() {
    return id;
  }

  public String getLabel() {
    return label;
  }

  public String getLastSharedDateTime() {
    return lastSharedDateTime;
  }

  public String getOrderedDate() {
    return orderedDate;
  }

  public List<String> getUsergroups() {
    return usergroups;
  }

  public static Builder caagResult() {
    return new CaagResult.Builder();
  }

  public static class Builder {
    private String amendedBy;

    private String amendmentDate;

    private String amendmentReason;

    private List<CaagResultPrompt> caagResultPrompts;

    private UUID id;

    private String label;

    private String lastSharedDateTime;

    private String orderedDate;

    private List<String> usergroups;

    public Builder withAmendedBy(final String amendedBy) {
      this.amendedBy = amendedBy;
      return this;
    }

    public Builder withAmendmentDate(final String amendmentDate) {
      this.amendmentDate = amendmentDate;
      return this;
    }

    public Builder withAmendmentReason(final String amendmentReason) {
      this.amendmentReason = amendmentReason;
      return this;
    }

    public Builder withCaagResultPrompts(final List<CaagResultPrompt> caagResultPrompts) {
      this.caagResultPrompts = caagResultPrompts;
      return this;
    }

    public Builder withId(final UUID id) {
      this.id = id;
      return this;
    }

    public Builder withLabel(final String label) {
      this.label = label;
      return this;
    }

    public Builder withLastSharedDateTime(final String lastSharedDateTime) {
      this.lastSharedDateTime = lastSharedDateTime;
      return this;
    }

    public Builder withOrderedDate(final String orderedDate) {
      this.orderedDate = orderedDate;
      return this;
    }

    public Builder withUsergroups(final List<String> usergroups) {
      this.usergroups = usergroups;
      return this;
    }

    public Builder withValuesFrom(final CaagResult caagResult) {
      this.amendedBy = caagResult.getAmendedBy();
      this.amendmentDate = caagResult.getAmendmentDate();
      this.amendmentReason = caagResult.getAmendmentReason();
      this.caagResultPrompts = caagResult.getCaagResultPrompts();
      this.id = caagResult.getId();
      this.label = caagResult.getLabel();
      this.lastSharedDateTime = caagResult.getLastSharedDateTime();
      this.orderedDate = caagResult.getOrderedDate();
      this.usergroups = caagResult.getUsergroups();
      return this;
    }

    public CaagResult build() {
      return new CaagResult(amendedBy, amendmentDate, amendmentReason, caagResultPrompts, id, label, lastSharedDateTime, orderedDate, usergroups);
    }
  }
}
