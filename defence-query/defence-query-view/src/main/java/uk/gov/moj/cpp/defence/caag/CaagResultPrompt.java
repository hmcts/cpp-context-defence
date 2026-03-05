package uk.gov.moj.cpp.defence.caag;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;

public class CaagResultPrompt {
  private final String label;

  private final List<String> usergroups;

  private final String value;

  @JsonCreator
  public CaagResultPrompt(final String label, final List<String> usergroups, final String value) {
    this.label = label;
    this.usergroups = usergroups;
    this.value = value;
  }

  public String getLabel() {
    return label;
  }

  public List<String> getUsergroups() {
    return usergroups;
  }

  public String getValue() {
    return value;
  }

  public static Builder caagResultPrompt() {
    return new CaagResultPrompt.Builder();
  }

  public static class Builder {
    private String label;

    private List<String> usergroups;

    private String value;

    public Builder withLabel(final String label) {
      this.label = label;
      return this;
    }

    public Builder withUsergroups(final List<String> usergroups) {
      this.usergroups = usergroups;
      return this;
    }

    public Builder withValue(final String value) {
      this.value = value;
      return this;
    }

    public Builder withValuesFrom(final CaagResultPrompt caagResultPrompt) {
      this.label = caagResultPrompt.getLabel();
      this.usergroups = caagResultPrompt.getUsergroups();
      this.value = caagResultPrompt.getValue();
      return this;
    }

    public CaagResultPrompt build() {
      return new CaagResultPrompt(label, usergroups, value);
    }
  }
}
