package uk.gov.moj.cpp.defence.persistence;

import uk.gov.moj.cpp.defence.persistence.entity.Allegation;

import java.util.List;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class AllegationRepository {

    @PersistenceContext(unitName = "defence")
    EntityManager entityManager;

    public Allegation findBy(final UUID id) {
        return entityManager.find(Allegation.class, id);
    }

    public List<Allegation> findAllegationByCriteria(final UUID defenceClientId) {
        return entityManager.createQuery(
                        "SELECT alg FROM Allegation alg WHERE alg.defenceClientId = :defenceClientId AND alg.defenceClientId IN (SELECT dc.id FROM DefenceClient dc WHERE dc.id = :defenceClientId AND dc.visible = true)",
                        Allegation.class)
                .setParameter("defenceClientId", defenceClientId)
                .getResultList();
    }

    public Allegation findAllegationByDefenceClientIdAndOffenceId(final UUID defenceClientId, final UUID offenceId) {
        return entityManager.createQuery(
                        "SELECT alg FROM Allegation alg WHERE alg.defenceClientId = :defenceClientId AND alg.offenceId = :offenceId",
                        Allegation.class)
                .setParameter("defenceClientId", defenceClientId)
                .setParameter("offenceId", offenceId)
                .getResultStream().findFirst().orElse(null);
    }

    public Allegation save(final Allegation entity) {
        return entityManager.merge(entity);
    }

    public void remove(final Allegation entity) {
        final Allegation managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }
}
