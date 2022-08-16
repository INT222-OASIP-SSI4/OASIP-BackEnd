package jag.oasipbackend.controllers;

import jag.oasipbackend.dtos.EventCategoryDTO;
import jag.oasipbackend.dtos.UpdateEventCategoryDTO;
import jag.oasipbackend.entities.EventCategory;
import jag.oasipbackend.repositories.EventCategoryRepository;
import jag.oasipbackend.services.EventCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/eventcategories")
@CrossOrigin(origins = "*")
public class EventCategoryController {
    @Autowired
    private EventCategoryRepository repository;

    @Autowired
    private EventCategoryService service;

    @GetMapping("")
    public List<EventCategoryDTO> getEventCatagories(){
        return service.findAll();
    }

    @GetMapping("/{categoryId}")
    public EventCategoryDTO getEventCategoryById(@PathVariable Integer categoryId) {
        return service.findById(categoryId);
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<EventCategory> updateCategory(@PathVariable Integer categoryId,
                                                        @Valid @RequestBody UpdateEventCategoryDTO eventCategoryDTO) {
        EventCategory eventCategory = service.update(categoryId, eventCategoryDTO);
        return new ResponseEntity<>(eventCategory, HttpStatus.OK);
    }
}
