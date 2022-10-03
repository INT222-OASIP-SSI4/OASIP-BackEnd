package jag.oasipbackend.services;

import jag.oasipbackend.dtos.UserLoginDTO;
import jag.oasipbackend.models.Response;
import jag.oasipbackend.repositories.UserRepository;
import jag.oasipbackend.utils.ListMapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class MatchService {
    @Autowired
    private UserRepository repository;

    @Autowired
    private ListMapper listMapper;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private Argon2PasswordEncoder argon2PasswordEncoder;

    public MatchService(UserRepository repository, ListMapper listMapper, ModelMapper modelMapper, Argon2PasswordEncoder argon2PasswordEncoder) {
        this.repository = repository;
        this.listMapper = listMapper;
        this.modelMapper = modelMapper;
        this.argon2PasswordEncoder = argon2PasswordEncoder;
    }

    public ResponseEntity matchCheck(UserLoginDTO userLoginDTO) {
        if (repository.existsByUserEmail(userLoginDTO.getUserEmail())) {
            if (argon2PasswordEncoder.matches(String.valueOf(userLoginDTO.getPassword()), repository.findByUserEmail(userLoginDTO.getUserEmail()).get().getPassword())) {
                return ResponseEntity.ok("Password match!");
            }
            return Response.response(HttpStatus.UNAUTHORIZED, "Password doesn't match");
        }
        return Response.response(HttpStatus.NOT_FOUND, "User not found username : " + userLoginDTO.getUserEmail());
    }
}
