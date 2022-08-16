package jag.oasipbackend.services;

import jag.oasipbackend.dtos.CreateEventDTO;
import jag.oasipbackend.dtos.EventDTO;
import jag.oasipbackend.dtos.UpdateEventDTO;
import jag.oasipbackend.entities.Event;
import jag.oasipbackend.entities.EventCategory;
import jag.oasipbackend.repositories.EventCategoryRepository;
import jag.oasipbackend.repositories.EventRepository;
import jag.oasipbackend.utils.ListMapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

@Service
public class EventService {
    @Autowired
    private EventRepository repository;

    @Autowired
    private EventCategoryRepository ecRepo;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ListMapper listMapper;

    public  List<EventDTO> findAll() {
        List<Event> events = repository.findAll(Sort.by(Sort.Direction.DESC, "eventStartTime"));
        return listMapper.mapList(events, EventDTO.class, modelMapper);
    }

    public EventDTO findById(Integer eventId) {
        Event event = repository.findById(eventId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                eventId + " doesn't exist"));
        return  modelMapper.map(event, EventDTO.class);
    }

    public Event save(CreateEventDTO createEventDTO) {
        Event e = modelMapper.map(createEventDTO, Event.class);
        EventCategory ec = ecRepo.findById(createEventDTO.getEventCategoryId()).orElseThrow(
                () ->new ResponseStatusException(HttpStatus.NOT_FOUND, createEventDTO.getEventCategoryId() +
                        " does not exist !! "));
        e.setId(null);
        e.setEventDuration(ec.getEventDuration());
        e.setEventCategory(ec);
        validateOverlap(e);
        return repository.saveAndFlush(e);
    }

    private Event mapEvent(Event existingEvent, UpdateEventDTO updateEvent) {
        if (updateEvent.getEventStartTime() != null)
            existingEvent.setEventStartTime(updateEvent.getEventStartTime());
        if (updateEvent.getEventNotes() != null)
            existingEvent.setEventNotes(updateEvent.getEventNotes());
        return existingEvent;
    }

    public Event update(UpdateEventDTO updateEventDTO, Integer eventId){
        Event event = repository.findById(eventId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Event Id : " + eventId + " Not found"));
        Event updatedEvent = mapEvent(event, updateEventDTO);
        validateOverlap(updatedEvent);
        return repository.saveAndFlush(updatedEvent);
    }

    public void delete(Integer eventId) {
        repository.findById(eventId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, eventId + " does not exist !!!"));
        repository.deleteById(eventId);
    }

    private void validateOverlap(Event newEvent) {
    Instant newStartTime = newEvent.getEventStartTime();
    Instant newEndTime = newEvent.getEventStartTime().plus(newEvent.getEventDuration(), ChronoUnit.MINUTES);
    List<Event> events = repository.findAll();
    events.stream().filter((event -> Objects.equals(event.getEventCategory().getId(), newEvent.getEventCategory().getId())
            && !Objects.equals(event.getId(), newEvent.getId()) && Objects.equals(event.getEventStartTime().toString().substring(0,10),
            newStartTime.toString().substring(0,10)))).forEach(event -> {
        Instant selectStartTime = event.getEventStartTime();
        Instant selectEndTime = event.getEventStartTime().plus(event.getEventDuration(), ChronoUnit.MINUTES);
        if(checkOverlap(selectStartTime, selectEndTime, newStartTime, newEndTime)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Date, Date is overlap");
        }
    });
    }

    private boolean checkOverlap(Instant aStart, Instant aEnd, Instant bStart, Instant bEnd){
        //b start in a
        if((aStart.isBefore(bStart) || aStart.equals(bStart)) && aEnd.isAfter(bStart))
            return true;
        //b end in a
        if(aStart.isBefore(bEnd) && (aEnd.isAfter(bEnd) || aEnd.equals(bEnd)))
            return true;
        //b in a
        if((aStart.isBefore(bStart) || aStart.equals(bStart)) && (aEnd.isAfter(bEnd) || aEnd.equals(bEnd)))
            return true;
        //a in b
        if((aStart.isAfter(bStart) || aStart.equals(bStart)) && (aEnd.isBefore(bEnd) || aEnd.equals(bEnd)))
            return true;
        //a start in b
        if((bStart.isBefore(aStart) || bStart.equals(aStart)) && bEnd.isAfter(aStart))
            return true;
        return false;
    }

}
