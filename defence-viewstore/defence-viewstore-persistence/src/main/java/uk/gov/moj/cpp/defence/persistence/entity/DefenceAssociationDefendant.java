package uk.gov.moj.cpp.defence.persistence.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "defence_association_defendant")
@SuppressWarnings("squid:S2384")
public class DefenceAssociationDefendant implements Serializable {

    @Id
    @Column(name = "defendant_id", unique = true, nullable = false)
    private UUID defendantId;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "defenceAssociationDefendant", orphanRemoval = true)
    private List<DefenceAssociation> defenceAssociations = new ArrayList<>();

    public UUID getDefendantId() {
        return defendantId;
    }

    public void setDefendantId(final UUID defendantId) {
        this.defendantId = defendantId;
    }

    public List<DefenceAssociation> getDefenceAssociations() {
        return defenceAssociations;
    }

    public void setDefenceAssociations(final List<DefenceAssociation> defenceAssociations) {
        this.defenceAssociations = defenceAssociations;
    }
}
