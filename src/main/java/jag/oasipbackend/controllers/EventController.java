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
import org.springframework.web.bind.annotation.*;

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
    public List<EventDTO> getEvents(){
       return service.findAll();
}

    @GetMapping("/{eventId}")
    public EventDTO getEvent(@PathVariable Integer eventId) {
        return service.findById(eventId);
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Event> save(@Valid @RequestBody CreateEventDTO newEvent){
        Event response = service.save(newEvent);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<Event> updateEvent(@Valid @RequestBody UpdateEventDTO updateEventDTO,
                                             @PathVariable Integer eventId) {
        Event event = service.update(updateEventDTO, eventId);
        return new ResponseEntity<>(event, HttpStatus.OK);
    }

    @DeleteMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable Integer eventId) {
        service.delete(eventId);
    }

}
