package jag.oasipbackend.repositories;

import jag.oasipbackend.entities.Event;
import jag.oasipbackend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Integer> {
    List<Event> findByBookingEmail(String bookingEmail);
}
