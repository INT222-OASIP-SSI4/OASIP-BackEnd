package jag.oasipbackend.controllers;

import jag.oasipbackend.dtos.CreateEventDTO;
import jag.oasipbackend.dtos.EventDTO;
import jag.oasipbackend.dtos.UpdateEventDTO;
import jag.oasipbackend.entities.Event;
import jag.oasipbackend.repositories.EventRepository;
import jag.oasipbackend.services.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class EventController {
    @Autowired
    private EventRepository repository;
    @Autowired
    private EventService service;

    @GetMapping("")
    public List<EventDTO> getEvents(HttpServletRequest httpServletRequest){
       return service.findAllEvent(httpServletRequest);
    }

    @GetMapping("/validate")
    public List<EventDTO> getEventForValidate(){
        return service.findAll();
    }

    @GetMapping("/{eventId}")
    public EventDTO getEvent(@PathVariable Integer eventId, HttpServletRequest httpServletRequest) {
        return service.findById(eventId, httpServletRequest);
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("!isAuthenticated() or hasAnyRole(\"admin\",\"student\")")
    public ResponseEntity<Event> save(@Valid @RequestPart("event") CreateEventDTO newEvent,@RequestPart(value = "file",required = false) MultipartFile file, HttpServletRequest request){
        Event response = service.save(newEvent, file, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<Event> updateEvent(@Valid @RequestPart(value = "event",required = false) UpdateEventDTO updateEventDTO,
                                             @PathVariable Integer eventId, @RequestPart(value = "file",required = false) MultipartFile file, HttpServletRequest httpServletRequest) {
        Event event = service.update(updateEventDTO, eventId, httpServletRequest, file);
        return new ResponseEntity<>(event, HttpStatus.OK);
    }

    @DeleteMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable Integer eventId, HttpServletRequest httpServletRequest) {
        service.delete(eventId, httpServletRequest);
    }

}
