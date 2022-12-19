package jag.oasipbackend.services;

import jag.oasipbackend.configurations.JwtRequestFilter;
import jag.oasipbackend.configurations.JwtTokenUtil;
import jag.oasipbackend.dtos.CreateEventDTO;
import jag.oasipbackend.dtos.EventDTO;
import jag.oasipbackend.dtos.UpdateEventDTO;
import jag.oasipbackend.entities.*;
import jag.oasipbackend.repositories.EventCategoryRepository;
import jag.oasipbackend.repositories.EventRepository;
import jag.oasipbackend.repositories.UserCategoryRepository;
import jag.oasipbackend.repositories.UserRepository;
import jag.oasipbackend.storage.FileSystemStorageService;
import jag.oasipbackend.utils.ListMapper;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    private EmailService emailService;

    @Autowired
    private FileSystemStorageService fileSystemStorageService;

    @Autowired
    private UserCategoryRepository userCategoryRepository;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    public List<EventDTO> findAll() {
        List<Event> events = repository.findAll(Sort.by(Sort.Direction.DESC, "eventStartTime"));
        return listMapper.mapList(events, EventDTO.class, modelMapper);
    }

    public EventDTO findById(Integer eventId, HttpServletRequest request) {
        Event event = repository.findById(eventId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, eventId + " does not exist !!!"));
        String userEmail = getUserEmail(jwtRequestFilter.getJwtToken());
        UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(userEmail);
        System.out.println(event);
        if(userDetails != null){
            Optional<User> user = userRepository.findByUserEmail(userEmail);
            List<Integer> eventCategoryIdByOwner = ecRepo.findEventCategoryIdByOwner(user.get().getId());
            if (user.isPresent()) {
                if((request.isUserInRole("ROLE_student")) && !event.getBookingEmail().equals(user.get().getUserEmail())) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot see event which you didn't own");
                }
                if((request.isUserInRole("ROLE_lecturer")) && !eventCategoryIdByOwner.stream().anyMatch(event.getEventCategory().getId()::equals)) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot see event which you didn't own of category's event");
                }

            }else{
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please Sign in again");
            }
        }else{
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please Sign in again");
        }
        return modelMapper.map(event, EventDTO.class);
    }

    public Event save(CreateEventDTO createEventDTO, MultipartFile file, HttpServletRequest request) {

                if((request.isUserInRole("ROLE_student"))) {
                    String userEmail = getEmailFromToken(request);
                    if(!createEventDTO.getBookingEmail().equals(userEmail)) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "booking email must be the same as the student's email.");
                    }
                }

        Event event = modelMapper.map(createEventDTO, Event.class);
        EventCategory ec = ecRepo.findById(createEventDTO.getEventCategoryId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, createEventDTO.getEventCategoryId() +
                        " does not exist !! "));
        event.setId(null);
        event.setEventDuration(ec.getEventDuration());
        event.setEventCategory(ec);
//        fileSystemStorageService.store(file);
        validateOverlap(event);
        Event createEvent = repository.saveAndFlush(event);
        if(file != null){
            sendFile(file, createEvent.getId());
            event.setFileName(file.getOriginalFilename());
        }else {
            event.setFileName(null);
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Hi " + createEvent.getBookingEmail() + ". Your event has been created.\n\n");
        stringBuilder.append("Event Detail \n");
        stringBuilder.append(" - Booking Name :" + createEvent.getBookingName() + "\n");
        stringBuilder.append(" - Booking Email :" + createEvent.getBookingEmail() + "\n");
        stringBuilder.append(" - Event Duration :" + createEvent.getEventDuration() + "\n");
        if(event.getEventNotes() != null){
            stringBuilder.append(" - Event Notes :" + createEvent.getEventNotes() + "\n");
            stringBuilder.append(" - Event Category :" + ec.getEventCategoryName() + "\n");
        try {
            emailService.sendSimpleMessage(createEventDTO.getBookingEmail(),"New Event was created", stringBuilder.toString());
        } catch (Exception e){
            e.printStackTrace();
        }
        }
        return repository.saveAndFlush(event);
 }

    public void sendFile(MultipartFile multipartFile,Integer id){
        if (multipartFile != null){
        try {
                fileSystemStorageService.storeFile(multipartFile, id);

        }catch (Exception e){
            System.out.println(e);
        }}
    }

    private Event mapEvent(Event existingEvent, UpdateEventDTO updateEvent, MultipartFile multipartFile) {
        Path getPath = fileSystemStorageService.getPath(existingEvent.getId());
        File directoryPath = new File(getPath.toString());

        File[] files = directoryPath.listFiles();
        if (updateEvent.getEventStartTime() != null)
            existingEvent.setEventStartTime(updateEvent.getEventStartTime());
        if (updateEvent.getEventNotes() != null)
            existingEvent.setEventNotes(updateEvent.getEventNotes());
        if(multipartFile == null && directoryPath.isDirectory() == false && updateEvent.isFileDelete()==false) {
            existingEvent.setFileName(existingEvent.getFileName());
        }

        if(updateEvent.isFileDelete()==true){
            if(directoryPath.isDirectory() == true)
                fileSystemStorageService.deleteFile(existingEvent.getId());
            existingEvent.setFileName("null");
        }

        try{

            if (directoryPath.isDirectory() == true) {
                System.out.println("directoryPath.isDirectory() == true 245");
                if (multipartFile == null) {
                    existingEvent.setFileName(existingEvent.getFileName());
                    System.out.println(updateEvent.isFileDelete());
                    System.out.println("if multipartFile == null 248");
                } else {
                    System.out.println("else 251");
                    directoryPath.isDirectory();
                    System.out.println("directory: " + directoryPath.isDirectory());
                    String fileName = null;
                    for (File file1 : files) {
                        if (file1.isFile()) {
                            fileName = file1.getName();
                        }
                    }
                    System.out.println("same file: " + fileName);
                    System.out.println("current file: " + multipartFile.getOriginalFilename());
                    System.out.println("check same file: " + multipartFile.getOriginalFilename().equals(fileName));
                    if (multipartFile.getOriginalFilename().isEmpty()) {
                        System.out.println("multipartFile.getOriginalFilename().isEmpty() 264");
                        System.out.println("Equal file name");
                    } else {
                        System.out.println("No equal file name");
                        if (Files.exists(Path.of(getPath.toString()))) {
                            FileUtils.cleanDirectory(new File(getPath.toString()));
                            System.out.println("have file");
                            fileSystemStorageService.storeFile(multipartFile, existingEvent.getId());
                            existingEvent.setFileName(multipartFile.getOriginalFilename());
                        }
                        fileSystemStorageService.storeFile(multipartFile, existingEvent.getId());
                        existingEvent.setFileName(multipartFile.getOriginalFilename());
                    }
                }
            } else {
                if(multipartFile == null && updateEvent.isFileDelete()==true)
                {
                    existingEvent.setFileName("null");
                } else{
                    Files.createDirectories(directoryPath.toPath());
                    fileSystemStorageService.storeFile(multipartFile, existingEvent.getId());
                    existingEvent.setFileName(multipartFile.getOriginalFilename());
                }
            }}catch(IOException e){new ResponseStatusException(HttpStatus.BAD_REQUEST,"IOEXCEPTION was catch during update");}
        return existingEvent;
    }

    public Event update(UpdateEventDTO updateEventDTO, Integer eventId, HttpServletRequest request, MultipartFile multipartFile) {
        Event event = repository.findById(eventId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, eventId + " does not exist !!!"));
        String userEmail = getUserEmail(jwtRequestFilter.getJwtToken());
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

        Event updatedEvent = mapEvent(event, updateEventDTO, multipartFile);
        validateOverlap(updatedEvent);
        return repository.saveAndFlush(updatedEvent);
    }

    public void delete(Integer eventId, HttpServletRequest request) {
        Event event = repository.findById(eventId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, eventId + " does not exist !!!"));
        String getUserEmail = getUserEmail(jwtRequestFilter.getJwtToken());
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
        fileSystemStorageService.deleteFile(eventId);
        repository.deleteById(eventId);
    }

    public List<EventDTO> findAllEvent(HttpServletRequest request) {

        String userEmail = getUserEmail(jwtRequestFilter.getJwtToken());
        UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(userEmail);
        if (userDetails != null) {
            Optional<User> user = userRepository.findByUserEmail(userEmail);
            if(user.isPresent()){
                List<Event> eventsList = repository.findAll(Sort.by(Sort.Direction.DESC, "eventStartTime"));
                if((request.isUserInRole("ROLE_student")))
                    eventsList = eventsList.stream().filter(event -> event.getBookingEmail().equals(userEmail)).collect(Collectors.toList());
                if((request.isUserInRole("ROLE_lecturer")))
                    eventsList = repository.findAllByUserId(user.get().getId());

                return listMapper.mapList(eventsList, EventDTO.class, modelMapper);
            }else {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please Sign in again");
            }
        }
        return null;
    }

    public String getRequestAccessToken (HttpServletRequest request){
        return request.getHeader("Authorization").substring(7);
    }

    public String getUserEmail (String requestAccessToken){
        return jwtTokenUtill.getUsernameFromToken(requestAccessToken);
    }

    public String getEmailFromToken(HttpServletRequest httpServletRequest){
        return getUserEmail(getRequestAccessToken(httpServletRequest));
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
