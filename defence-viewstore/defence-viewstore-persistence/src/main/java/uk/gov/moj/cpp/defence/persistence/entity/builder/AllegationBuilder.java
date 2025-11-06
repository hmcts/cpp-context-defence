package uk.gov.moj.cpp.defence.persistence.entity.builder;

import uk.gov.moj.cpp.defence.persistence.entity.Allegation;

import java.time.LocalDate;
import java.util.UUID;

@SuppressWarnings({"pmd:BeanMembersShouldSerialize"})
public class AllegationBuilder {

    private UUID id;
    private UUID defenceClientId;
    private UUID offenceId;
    private String legislation;
    private String title;
    private LocalDate chargeDate;


    public static AllegationBuilder newAllegationBuilder() {
        return new AllegationBuilder();
    }

    public AllegationBuilder withDefenceClientId(UUID defenceClientId) {
        this.defenceClientId = defenceClientId;
        return this;
    }

    public AllegationBuilder withOffenceId(UUID offenceId) {
        this.offenceId = offenceId;
        return this;
    }

    public AllegationBuilder withLegislation(String legislation) {
        this.legislation = legislation;
        return this;
    }

    public AllegationBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    public AllegationBuilder withChargeDate(LocalDate chargeDate) {
        this.chargeDate = chargeDate;
        return this;
    }

    public AllegationBuilder withId(UUID id) {
        this.id = id;
        return this;
    }

    public Allegation build() {
        return new Allegation(id, defenceClientId, offenceId, legislation, title, chargeDate);
    }

    public UUID getId() {
        return id;
    }

    public UUID getDefenceClientId() {
        return defenceClientId;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public String getLegislation() {
        return legislation;
    }

    public String getTitle() {
        return title;
    }

    public LocalDate getChargeDate() {
        return chargeDate;
    }
}
