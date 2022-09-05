package jag.oasipbackend.controllers;

import jag.oasipbackend.configurations.JwtTokenUtil;
import jag.oasipbackend.dtos.*;
import jag.oasipbackend.entities.User;
import jag.oasipbackend.models.JwtRequest;
import jag.oasipbackend.models.JwtResponse;
import jag.oasipbackend.repositories.UserRepository;
import jag.oasipbackend.responses.ResponseHandler;
import jag.oasipbackend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.security.core.userdetails.UserDetails;

import javax.servlet.http.HttpServletResponse;
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

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    public AuthenticationManager authenticationManager;

    @GetMapping("")
    public List<UserDTO> getUsers() {
        return service.findAll();
    }

    @GetMapping("/{userId}")
    public UserDetailDTO getUser(@PathVariable Integer userId) {
        return service.findById(userId);
    }

    @PostMapping("")
    @CrossOrigin(origins = "*")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> save(@Valid @RequestBody CreateUserDTO newUser) {
        try {
            UserDTO response = service.save(newUser);
            return ResponseHandler.generateResponse("Successfully added data!", HttpStatus.OK, response);
        } catch (Exception e) {
            return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.MULTI_STATUS, null);
        }
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

    @PostMapping("/authenticate")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {
        authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());
        final UserDetails userDetails = userDetailsService
                .loadUserByUsername(authenticationRequest.getUsername());
        final String token = jwtTokenUtil.generateToken(userDetails);
        return ResponseEntity.ok(new JwtResponse(token));
    }

    private void authenticate(String username, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }


    @PostMapping("/match")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity matchUser(@Valid @RequestBody UserMatchDTO userMatchDTO, HttpServletResponse httpServletResponse, ServletWebRequest request) throws Exception{
        return service.checkMatch(userMatchDTO, httpServletResponse, request);
    }

}
