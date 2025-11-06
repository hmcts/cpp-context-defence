package uk.gov.justice.cps.defence;

public enum RepresentationType {
    PRIVATE("PRIVATE"),

    PRO_BONO("PRO_BONO"),

    COURT_APPOINTED("COURT_APPOINTED"),

    REPRESENTATION_ORDER("REPRESENTATION_ORDER"),

    REPRESENTATION_ORDER_APPLIED_FOR("REPRESENTATION_ORDER_APPLIED_FOR");

    private final String value;

    RepresentationType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }


}
