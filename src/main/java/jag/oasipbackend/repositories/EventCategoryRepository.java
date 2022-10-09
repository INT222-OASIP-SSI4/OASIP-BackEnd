package jag.oasipbackend.repositories;

import jag.oasipbackend.entities.EventCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EventCategoryRepository extends JpaRepository<EventCategory, Integer> {
    Optional<EventCategory> findByEventCategoryName(String eventCategoryName);

    @Query(value = "select ec.eventCategoryId from eventCategories ec join userCategory uc on ec.eventCategoryId = uc.eventCategoryId where uc.userId = :userId", nativeQuery = true)
    List<Integer> findEventCategoryIdByOwner(Integer userId);
}
