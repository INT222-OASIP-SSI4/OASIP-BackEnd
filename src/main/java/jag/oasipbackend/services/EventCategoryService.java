package jag.oasipbackend.services;

import jag.oasipbackend.dtos.EventCategoryDTO;
import jag.oasipbackend.dtos.UpdateEventCategoryDTO;
import jag.oasipbackend.entities.EventCategory;
import jag.oasipbackend.repositories.EventCategoryRepository;
import jag.oasipbackend.utils.ListMapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class EventCategoryService {
    @Autowired
    private EventCategoryRepository repository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ListMapper listMapper;

    public List<EventCategoryDTO> findAll() {
        List<EventCategory> eventCategories = repository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        return listMapper.mapList(eventCategories, EventCategoryDTO.class, modelMapper);
    }

    public EventCategoryDTO findById(Integer id) {
        EventCategory eventCategory = repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return modelMapper.map(eventCategory, EventCategoryDTO.class);
    }

    public Optional<EventCategory> findByCategoryName(String eventCategoryName) {
        return repository.findByEventCategoryName(eventCategoryName);
    }

    private EventCategory setEventCategory(EventCategory currentEventCategory, UpdateEventCategoryDTO updateEventCategoryDTO){
        if (updateEventCategoryDTO.getEventCategoryName() != null){
            currentEventCategory.setEventCategoryName(updateEventCategoryDTO.getEventCategoryName().trim());
        }
        if (currentEventCategory.getEventDuration() != null){
            currentEventCategory.setEventDuration(updateEventCategoryDTO.getEventDuration());
        }
            currentEventCategory.setEventCategoryDescription(updateEventCategoryDTO.getEventCategoryDescription());
        return currentEventCategory;
    }

    public EventCategory update(Integer id, UpdateEventCategoryDTO updateEventCategoryDTO) {
        Optional<EventCategory> eventCategoryFindByName = findByCategoryName(updateEventCategoryDTO.getEventCategoryName().trim());
        if (eventCategoryFindByName.isPresent() && !Objects.equals(eventCategoryFindByName.get().getId(), id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "eventCategoryName is not unique");
        }
        EventCategory eventCategory = repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "eventCategoryId :" + id + "Not Found"));
        EventCategory updateEventCategory = setEventCategory(eventCategory, updateEventCategoryDTO);
        return repository.saveAndFlush(updateEventCategory);
    }
}
