package jag.oasipbackend.repositories;

import jag.oasipbackend.entities.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Integer> {
    List<Event> findByBookingEmail(String bookingEmail);

    @Query(value = "select e.* from events e join userCategory uc on e.eventCategoryId = uc.eventCategoryId where uc.userId = :userId", nativeQuery = true)
    List<Event> findAllByUserId(Integer userId);

}
