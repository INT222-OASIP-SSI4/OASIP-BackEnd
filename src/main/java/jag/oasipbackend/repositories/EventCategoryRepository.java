package jag.oasipbackend.repositories;

import jag.oasipbackend.entities.EventCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventCategoryRepository extends JpaRepository<EventCategory, Integer> {
    Optional<EventCategory> findByEventCategoryName(String eventCategoryName);
}
