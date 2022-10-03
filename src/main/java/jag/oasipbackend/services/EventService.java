package jag.oasipbackend.services;

import jag.oasipbackend.configurations.JwtTokenUtil;
import jag.oasipbackend.dtos.CreateEventDTO;
import jag.oasipbackend.dtos.EventDTO;
import jag.oasipbackend.dtos.UpdateEventDTO;
import jag.oasipbackend.entities.Event;
import jag.oasipbackend.entities.EventCategory;
import jag.oasipbackend.entities.User;
import jag.oasipbackend.enums.RoleType;
import jag.oasipbackend.repositories.EventCategoryRepository;
import jag.oasipbackend.repositories.EventRepository;
import jag.oasipbackend.repositories.UserRepository;
import jag.oasipbackend.utils.ListMapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

    @Autowired
    private JwtTokenUtil jwtTokenUtill;

    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;

    @Autowired
    private UserRepository userRepository;

    public List<EventDTO> findAll() {
        List<Event> events = repository.findAll(Sort.by(Sort.Direction.DESC, "eventStartTime"));
        return listMapper.mapList(events, EventDTO.class, modelMapper);
    }

    public EventDTO findById(Integer eventId) {
        Event event = repository.findById(eventId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                eventId + " doesn't exist"));
        return modelMapper.map(event, EventDTO.class);
    }

    public Event save(CreateEventDTO createEventDTO, HttpServletRequest request) {
        String userEmail = getUserEmail(getRequestAccessToken(request));
        UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(userEmail);
        if(userDetails != null){
            Optional<User> user = userRepository.findByUserEmail(userEmail);
            if (user.isPresent()) {
                if((request.isUserInRole("ROLE_student")) && !createEventDTO.getBookingEmail().equals(user.get().getUserEmail())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "booking email must be the same as the student's email.");
                }
                if((request.isUserInRole("ROLE_lecturer"))) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only student, admin can delete event");
                }

            }else{
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        }else{
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please Sign in again");
        }
        Event e = modelMapper.map(createEventDTO, Event.class);
        EventCategory ec = ecRepo.findById(createEventDTO.getEventCategoryId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, createEventDTO.getEventCategoryId() +
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

    public Event update(UpdateEventDTO updateEventDTO, Integer eventId, HttpServletRequest request) {
        Event event = repository.findById(eventId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, eventId + " does not exist !!!"));
        String userEmail = getUserEmail(getRequestAccessToken(request));
        UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(userEmail);
        if(userDetails != null){
            Optional<User> user = userRepository.findByUserEmail(userEmail);
        if (user.isPresent()) {
            if((request.isUserInRole("ROLE_student")) && !event.getBookingEmail().equals(user.get().getUserEmail())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot delete event which you didn't own");
            }
            if((request.isUserInRole("ROLE_lecturer"))) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only student, admin can delete event");
            }

        }else{
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        }else{
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please Sign in again");
        }

        Event updatedEvent = mapEvent(event, updateEventDTO);
        validateOverlap(updatedEvent);
        return repository.saveAndFlush(updatedEvent);
    }

    public void delete(Integer eventId, HttpServletRequest request) {
        Event event = repository.findById(eventId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, eventId + " does not exist !!!"));
        String getUserEmail = getUserEmail(getRequestAccessToken(request));
        Optional<User> user = userRepository.findByUserEmail(getUserEmail);
            if(user.isPresent()) {
                if((request.isUserInRole("ROLE_student")) && !event.getBookingEmail().equals(user.get().getUserEmail())) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot delete event which you didn't own");
                }
                if((request.isUserInRole("ROLE_lecturer"))) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only student, admin can delete event");
                }
            } else {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please Sign in again");
            }
        repository.deleteById(eventId);
    }

    public List<EventDTO> findAllEvent(HttpServletRequest request) {
        List<Event> eventsList = repository.findAll(Sort.by(Sort.Direction.DESC, "eventStartTime"));
        String getUserEmail = getUserEmail(getRequestAccessToken(request));
        UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(getUserEmail);
        if (userDetails != null && (request.isUserInRole("ROLE_student"))) {
            List<Event> eventsListByEmail = repository.findByBookingEmail(getUserEmail);
            return listMapper.mapList(eventsListByEmail, EventDTO.class, modelMapper);
        }
//        else if(userDetails != null && (request.isUserInRole("ROLE_lecturer"))){
////            List<Events> eventsListByEmail = repository.findByBookingEmail(getUserEmail);
//            List<Event> eventsListByCategoryOwner = repository.findEventsCategoryOwnerByEmail(getUserEmail);
//
//            return listMapper.mapList(eventsListByCategoryOwner , EventDTO.class,modelMapper);
//
//        }
        return listMapper.mapList(eventsList, EventDTO.class, modelMapper);

    }

    public String getRequestAccessToken (HttpServletRequest request){
        return request.getHeader("Authorization").substring(7);
    }

    public String getUserEmail (String requestAccessToken){
        return jwtTokenUtill.getUsernameFromToken(requestAccessToken);
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
