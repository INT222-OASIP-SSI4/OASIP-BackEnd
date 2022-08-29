package jag.oasipbackend.controllers;

import jag.oasipbackend.dtos.*;
import jag.oasipbackend.entities.Event;
import jag.oasipbackend.entities.User;
import jag.oasipbackend.repositories.UserRepository;
import jag.oasipbackend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    @Autowired
    private UserRepository repository;

    @Autowired
    private UserService service;

    @GetMapping("")
    public List<UserDTO> getUsers() { return service.findAll(); }

    @GetMapping("/{userId}")
    public UserDetailDTO getUser(@PathVariable Integer userId) { return service.findById(userId); }

    @PostMapping("")
    @CrossOrigin(origins = "*")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<User> save(@Valid @RequestBody CreateUserDTO newUser){
        User response = service.save(newUser);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @CrossOrigin(origins = "*")
    @PutMapping("/{userId}")
    public ResponseEntity<User> updateUser(@Valid @RequestBody UpdateUserDTO updateUserDTO,
                                             @PathVariable Integer userId) {
        User user = service.update(updateUserDTO, userId);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable Integer userId) {
        service.delete(userId);
    }

    @PostMapping("/match")
    @ResponseStatus(HttpStatus.CREATED)
    public UserMatchDTO matchUser(@Valid @RequestBody UserMatchDTO userMatchDTO){
        return service.checkMatch(userMatchDTO);
    }

}
