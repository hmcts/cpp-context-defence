package uk.gov.moj.cpp.defence.caag;

import uk.gov.justice.core.courts.Address;
import uk.gov.justice.core.courts.AssociatedPerson;
import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.Organisation;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;

public class Defendants {
  private final Address address;

  private final Integer age;

  private final List<AssociatedPerson> associatedPersons;

  private final List<CaagDefendantOffence> caagDefendantOffences;

  private final Integer ctlExpiryCountDown;

  private final String ctlExpiryDate;

  private final String dateOfBirth;

  private final List<JudicialResult> defendantCaseJudicialResults;

  private final List<JudicialResult> defendantJudicialResults;

  private final List<String> defendantMarkers;

  private final String firstName;

  private final UUID id;

  private final String interpreterLanguageNeeds;

  private final String lastName;

  private final String legalAidStatus;

  private final CaagOrganisation legalEntityDefendant;

  private final UUID masterDefendantId;

  private final String nationality;

  private final String remandStatus;

  private final Organisation representation;

  @JsonCreator
  public Defendants(final Address address, final Integer age, final List<AssociatedPerson> associatedPersons, final List<CaagDefendantOffence> caagDefendantOffences, final Integer ctlExpiryCountDown, final String ctlExpiryDate, final String dateOfBirth, final List<JudicialResult> defendantCaseJudicialResults, final List<JudicialResult> defendantJudicialResults, final List<String> defendantMarkers, final String firstName, final UUID id, final String interpreterLanguageNeeds, final String lastName, final String legalAidStatus, final CaagOrganisation legalEntityDefendant, final UUID masterDefendantId, final String nationality, final String remandStatus, final Organisation representation) {
    this.address = address;
    this.age = age;
    this.associatedPersons = associatedPersons;
    this.caagDefendantOffences = caagDefendantOffences;
    this.ctlExpiryCountDown = ctlExpiryCountDown;
    this.ctlExpiryDate = ctlExpiryDate;
    this.dateOfBirth = dateOfBirth;
    this.defendantCaseJudicialResults = defendantCaseJudicialResults;
    this.defendantJudicialResults = defendantJudicialResults;
    this.defendantMarkers = defendantMarkers;
    this.firstName = firstName;
    this.id = id;
    this.interpreterLanguageNeeds = interpreterLanguageNeeds;
    this.lastName = lastName;
    this.legalAidStatus = legalAidStatus;
    this.legalEntityDefendant = legalEntityDefendant;
    this.masterDefendantId = masterDefendantId;
    this.nationality = nationality;
    this.remandStatus = remandStatus;
    this.representation = representation;
  }

  public Address getAddress() {
    return address;
  }

  public Integer getAge() {
    return age;
  }

  public List<AssociatedPerson> getAssociatedPersons() {
    return associatedPersons;
  }

  public List<CaagDefendantOffence> getCaagDefendantOffences() {
    return caagDefendantOffences;
  }

  public Integer getCtlExpiryCountDown() {
    return ctlExpiryCountDown;
  }

  public String getCtlExpiryDate() {
    return ctlExpiryDate;
  }

  public String getDateOfBirth() {
    return dateOfBirth;
  }

  public List<JudicialResult> getDefendantCaseJudicialResults() {
    return defendantCaseJudicialResults;
  }

  public List<JudicialResult> getDefendantJudicialResults() {
    return defendantJudicialResults;
  }

  public List<String> getDefendantMarkers() {
    return defendantMarkers;
  }

  public String getFirstName() {
    return firstName;
  }

  public UUID getId() {
    return id;
  }

  public String getInterpreterLanguageNeeds() {
    return interpreterLanguageNeeds;
  }

  public String getLastName() {
    return lastName;
  }

  public String getLegalAidStatus() {
    return legalAidStatus;
  }

  public CaagOrganisation getLegalEntityDefendant() {
    return legalEntityDefendant;
  }

  public UUID getMasterDefendantId() {
    return masterDefendantId;
  }

  public String getNationality() {
    return nationality;
  }

  public String getRemandStatus() {
    return remandStatus;
  }

  public Organisation getRepresentation() {
    return representation;
  }

  public static Builder defendants() {
    return new Defendants.Builder();
  }

  public static class Builder {
    private Address address;

    private Integer age;

    private List<AssociatedPerson> associatedPersons;

    private List<CaagDefendantOffence> caagDefendantOffences;

    private Integer ctlExpiryCountDown;

    private String ctlExpiryDate;

    private String dateOfBirth;

    private List<JudicialResult> defendantCaseJudicialResults;

    private List<JudicialResult> defendantJudicialResults;

    private List<String> defendantMarkers;

    private String firstName;

    private UUID id;

    private String interpreterLanguageNeeds;

    private String lastName;

    private String legalAidStatus;

    private CaagOrganisation legalEntityDefendant;

    private UUID masterDefendantId;

    private String nationality;

    private String remandStatus;

    private Organisation representation;

    public Builder withAddress(final Address address) {
      this.address = address;
      return this;
    }

    public Builder withAge(final Integer age) {
      this.age = age;
      return this;
    }

    public Builder withAssociatedPersons(final List<AssociatedPerson> associatedPersons) {
      this.associatedPersons = associatedPersons;
      return this;
    }

    public Builder withCaagDefendantOffences(final List<CaagDefendantOffence> caagDefendantOffences) {
      this.caagDefendantOffences = caagDefendantOffences;
      return this;
    }

    public Builder withCtlExpiryCountDown(final Integer ctlExpiryCountDown) {
      this.ctlExpiryCountDown = ctlExpiryCountDown;
      return this;
    }

    public Builder withCtlExpiryDate(final String ctlExpiryDate) {
      this.ctlExpiryDate = ctlExpiryDate;
      return this;
    }

    public Builder withDateOfBirth(final String dateOfBirth) {
      this.dateOfBirth = dateOfBirth;
      return this;
    }

    public Builder withDefendantCaseJudicialResults(final List<JudicialResult> defendantCaseJudicialResults) {
      this.defendantCaseJudicialResults = defendantCaseJudicialResults;
      return this;
    }

    public Builder withDefendantJudicialResults(final List<JudicialResult> defendantJudicialResults) {
      this.defendantJudicialResults = defendantJudicialResults;
      return this;
    }

    public Builder withDefendantMarkers(final List<String> defendantMarkers) {
      this.defendantMarkers = defendantMarkers;
      return this;
    }

    public Builder withFirstName(final String firstName) {
      this.firstName = firstName;
      return this;
    }

    public Builder withId(final UUID id) {
      this.id = id;
      return this;
    }

    public Builder withInterpreterLanguageNeeds(final String interpreterLanguageNeeds) {
      this.interpreterLanguageNeeds = interpreterLanguageNeeds;
      return this;
    }

    public Builder withLastName(final String lastName) {
      this.lastName = lastName;
      return this;
    }

    public Builder withLegalAidStatus(final String legalAidStatus) {
      this.legalAidStatus = legalAidStatus;
      return this;
    }

    public Builder withLegalEntityDefendant(final CaagOrganisation legalEntityDefendant) {
      this.legalEntityDefendant = legalEntityDefendant;
      return this;
    }

    public Builder withMasterDefendantId(final UUID masterDefendantId) {
      this.masterDefendantId = masterDefendantId;
      return this;
    }

    public Builder withNationality(final String nationality) {
      this.nationality = nationality;
      return this;
    }

    public Builder withRemandStatus(final String remandStatus) {
      this.remandStatus = remandStatus;
      return this;
    }

    public Builder withRepresentation(final Organisation representation) {
      this.representation = representation;
      return this;
    }

    public Builder withValuesFrom(final Defendants defendants) {
      this.address = defendants.getAddress();
      this.age = defendants.getAge();
      this.associatedPersons = defendants.getAssociatedPersons();
      this.caagDefendantOffences = defendants.getCaagDefendantOffences();
      this.ctlExpiryCountDown = defendants.getCtlExpiryCountDown();
      this.ctlExpiryDate = defendants.getCtlExpiryDate();
      this.dateOfBirth = defendants.getDateOfBirth();
      this.defendantCaseJudicialResults = defendants.getDefendantCaseJudicialResults();
      this.defendantJudicialResults = defendants.getDefendantJudicialResults();
      this.defendantMarkers = defendants.getDefendantMarkers();
      this.firstName = defendants.getFirstName();
      this.id = defendants.getId();
      this.interpreterLanguageNeeds = defendants.getInterpreterLanguageNeeds();
      this.lastName = defendants.getLastName();
      this.legalAidStatus = defendants.getLegalAidStatus();
      this.legalEntityDefendant = defendants.getLegalEntityDefendant();
      this.masterDefendantId = defendants.getMasterDefendantId();
      this.nationality = defendants.getNationality();
      this.remandStatus = defendants.getRemandStatus();
      this.representation = defendants.getRepresentation();
      return this;
    }

    public Defendants build() {
      return new Defendants(address, age, associatedPersons, caagDefendantOffences, ctlExpiryCountDown, ctlExpiryDate, dateOfBirth, defendantCaseJudicialResults, defendantJudicialResults, defendantMarkers, firstName, id, interpreterLanguageNeeds, lastName, legalAidStatus, legalEntityDefendant, masterDefendantId, nationality, remandStatus, representation);
    }
  }
}
