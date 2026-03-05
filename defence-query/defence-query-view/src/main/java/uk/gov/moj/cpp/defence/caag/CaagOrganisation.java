package uk.gov.moj.cpp.defence.caag;

import uk.gov.justice.core.courts.Address;

import com.fasterxml.jackson.annotation.JsonCreator;

public class CaagOrganisation {
  private final Address address;

  private final String name;

  @JsonCreator
  public CaagOrganisation(final Address address, final String name) {
    this.address = address;
    this.name = name;
  }

  public Address getAddress() {
    return address;
  }

  public String getName() {
    return name;
  }

  public static Builder caagOrganisation() {
    return new CaagOrganisation.Builder();
  }

  public static class Builder {
    private Address address;

    private String name;

    public Builder withAddress(final Address address) {
      this.address = address;
      return this;
    }

    public Builder withName(final String name) {
      this.name = name;
      return this;
    }

    public Builder withValuesFrom(final CaagOrganisation caagOrganisation) {
      this.address = caagOrganisation.getAddress();
      this.name = caagOrganisation.getName();
      return this;
    }

    public CaagOrganisation build() {
      return new CaagOrganisation(address, name);
    }
  }
}
