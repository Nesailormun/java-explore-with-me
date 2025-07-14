package ru.yandex.practicum.event.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.event.model.Event;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class CustomEventRepositoryImpl implements CustomEventRepository {

    private final EntityManager em;

    public CustomEventRepositoryImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public List<Event> searchPublicEvents(String text,
                                          List<Long> categoryIds,
                                          Boolean paid,
                                          LocalDateTime rangeStart,
                                          LocalDateTime rangeEnd,
                                          Boolean onlyAvailable,
                                          String sort,
                                          Pageable pageable) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Event> query = cb.createQuery(Event.class);
        Root<Event> event = query.from(Event.class);

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.equal(event.get("state"), Event.EventState.PUBLISHED));

        if (text != null && !text.isBlank()) {
            String pattern = "%" + text.toLowerCase() + "%";
            Predicate inAnnotation = cb.like(cb.lower(event.get("annotation")), pattern);
            Predicate inDescription = cb.like(cb.lower(event.get("description")), pattern);
            predicates.add(cb.or(inAnnotation, inDescription));
        }

        if (categoryIds != null && !categoryIds.isEmpty()) {
            predicates.add(event.get("category").get("id").in(categoryIds));
        }

        if (paid != null) {
            predicates.add(cb.equal(event.get("paid"), paid));
        }

        if (rangeStart != null) {
            predicates.add(cb.greaterThanOrEqualTo(event.get("eventDate"), rangeStart));
        }

        if (rangeEnd != null) {
            predicates.add(cb.lessThanOrEqualTo(event.get("eventDate"), rangeEnd));
        }

        if (onlyAvailable != null && onlyAvailable) {
            Predicate noLimit = cb.equal(event.get("participantLimit"), 0);
            Predicate hasFreeSpots = cb.greaterThan(event.get("participantLimit"), event.get("confirmedRequests"));
            predicates.add(cb.or(noLimit, hasFreeSpots));
        }

        query.select(event).where(cb.and(predicates.toArray(new Predicate[0])));

        if ("VIEWS".equals(sort)) {
            query.orderBy(cb.desc(event.get("views")));
        } else {
            query.orderBy(cb.asc(event.get("eventDate")));
        }

        return em.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();
    }

    @Override
    public List<Event> searchAdminEvents(List<Long> userIds,
                                         List<Event.EventState> states,
                                         List<Long> categoryIds,
                                         LocalDateTime rangeStart,
                                         LocalDateTime rangeEnd,
                                         Pageable pageable) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Event> query = cb.createQuery(Event.class);
        Root<Event> root = query.from(Event.class);

        List<Predicate> predicates = new ArrayList<>();

        if (userIds != null && !userIds.isEmpty()) {
            predicates.add(root.get("initiator").get("id").in(userIds));
        }

        if (states != null && !states.isEmpty()) {
            predicates.add(root.get("state").in(states));
        }

        if (categoryIds != null && !categoryIds.isEmpty()) {
            predicates.add(root.get("category").get("id").in(categoryIds));
        }

        if (rangeStart != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
        }

        if (rangeEnd != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
        }

        query.select(root).where(cb.and(predicates.toArray(new Predicate[0])));

        return em.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();
    }
}
