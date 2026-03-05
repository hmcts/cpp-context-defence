package uk.gov.moj.cpp.defence.caag;

import uk.gov.justice.core.courts.Address;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;

public class ProsecutorDetails {
  private final Address address;

  private final Boolean isCpsOrgVerifyError;

  private final String oldProsecutionAuthorityCode;

  private final String prosecutionAuthorityCode;

  private final UUID prosecutionAuthorityId;

  private final String prosecutionAuthorityReference;

  @JsonCreator
  public ProsecutorDetails(final Address address, final Boolean isCpsOrgVerifyError, final String oldProsecutionAuthorityCode, final String prosecutionAuthorityCode, final UUID prosecutionAuthorityId, final String prosecutionAuthorityReference) {
    this.address = address;
    this.isCpsOrgVerifyError = isCpsOrgVerifyError;
    this.oldProsecutionAuthorityCode = oldProsecutionAuthorityCode;
    this.prosecutionAuthorityCode = prosecutionAuthorityCode;
    this.prosecutionAuthorityId = prosecutionAuthorityId;
    this.prosecutionAuthorityReference = prosecutionAuthorityReference;
  }

  public Address getAddress() {
    return address;
  }

  public Boolean getIsCpsOrgVerifyError() {
    return isCpsOrgVerifyError;
  }

  public String getOldProsecutionAuthorityCode() {
    return oldProsecutionAuthorityCode;
  }

  public String getProsecutionAuthorityCode() {
    return prosecutionAuthorityCode;
  }

  public UUID getProsecutionAuthorityId() {
    return prosecutionAuthorityId;
  }

  public String getProsecutionAuthorityReference() {
    return prosecutionAuthorityReference;
  }

  public static Builder prosecutorDetails() {
    return new ProsecutorDetails.Builder();
  }

  public static class Builder {
    private Address address;

    private Boolean isCpsOrgVerifyError;

    private String oldProsecutionAuthorityCode;

    private String prosecutionAuthorityCode;

    private UUID prosecutionAuthorityId;

    private String prosecutionAuthorityReference;

    public Builder withAddress(final Address address) {
      this.address = address;
      return this;
    }

    public Builder withIsCpsOrgVerifyError(final Boolean isCpsOrgVerifyError) {
      this.isCpsOrgVerifyError = isCpsOrgVerifyError;
      return this;
    }

    public Builder withOldProsecutionAuthorityCode(final String oldProsecutionAuthorityCode) {
      this.oldProsecutionAuthorityCode = oldProsecutionAuthorityCode;
      return this;
    }

    public Builder withProsecutionAuthorityCode(final String prosecutionAuthorityCode) {
      this.prosecutionAuthorityCode = prosecutionAuthorityCode;
      return this;
    }

    public Builder withProsecutionAuthorityId(final UUID prosecutionAuthorityId) {
      this.prosecutionAuthorityId = prosecutionAuthorityId;
      return this;
    }

    public Builder withProsecutionAuthorityReference(final String prosecutionAuthorityReference) {
      this.prosecutionAuthorityReference = prosecutionAuthorityReference;
      return this;
    }

    public Builder withValuesFrom(final ProsecutorDetails prosecutorDetails) {
      this.address = prosecutorDetails.getAddress();
      this.isCpsOrgVerifyError = prosecutorDetails.getIsCpsOrgVerifyError();
      this.oldProsecutionAuthorityCode = prosecutorDetails.getOldProsecutionAuthorityCode();
      this.prosecutionAuthorityCode = prosecutorDetails.getProsecutionAuthorityCode();
      this.prosecutionAuthorityId = prosecutorDetails.getProsecutionAuthorityId();
      this.prosecutionAuthorityReference = prosecutorDetails.getProsecutionAuthorityReference();
      return this;
    }

    public ProsecutorDetails build() {
      return new ProsecutorDetails(address, isCpsOrgVerifyError, oldProsecutionAuthorityCode, prosecutionAuthorityCode, prosecutionAuthorityId, prosecutionAuthorityReference);
    }
  }
}
