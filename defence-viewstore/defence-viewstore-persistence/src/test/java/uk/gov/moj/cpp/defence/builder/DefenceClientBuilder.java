package uk.gov.moj.cpp.defence.builder;

import static java.time.LocalDate.of;
import static java.util.UUID.randomUUID;

import uk.gov.moj.cpp.defence.persistence.entity.DefenceClient;

public class DefenceClientBuilder {

    private DefenceClientBuilder() {
    }

    public static DefenceClient createDefenceClient() {
        return new DefenceClient(randomUUID(), "FIRST NAME", "LAST NAME", randomUUID(), of(1970, 5, 17), randomUUID());
    }
}
