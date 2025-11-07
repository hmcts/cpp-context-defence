package uk.gov.moj.cpp.defence.query.view;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(value = NON_NULL)
public class OrderedOrganisationDetailsVO {
  private final int order;

  private final UUID organisationId;

  private final String name;

  public OrderedOrganisationDetailsVO(final int accessOrder, final UUID organisationId, final String name) {
    this.order = accessOrder;
    this.organisationId = organisationId;
    this.name = name;
  }

  public int getOrder() {
    return order;
  }

  public UUID getOrganisationId() {
    return organisationId;
  }

  public String getName() {
    return name;
  }
}

