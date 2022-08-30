package jag.oasipbackend.services;

import jag.oasipbackend.dtos.*;
import jag.oasipbackend.entities.User;
import jag.oasipbackend.repositories.UserRepository;
import jag.oasipbackend.utils.ListMapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository repository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ListMapper listMapper;

    private Argon2PasswordEncoder argon2 = new Argon2PasswordEncoder(8,14,1,65536,10);

    public List<UserDTO> findAll() {
        List<User> users = repository.findAll(Sort.by("userName"));
        return listMapper.mapList(users, UserDTO.class, modelMapper);
    }

    public UserDetailDTO findById(Integer userId) {
        User user = repository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                userId + " doesn't exist"));
        return modelMapper.map(user, UserDetailDTO.class);
    }

    public UserDTO save(CreateUserDTO createUserDTO) {
        if(checkUniqueName(null, createUserDTO.getUserName().trim()) && checkUniqueEmail(null, createUserDTO.getUserEmail().trim()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name and email is not unique");
        if(checkUniqueName(null, createUserDTO.getUserName().trim()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name is not unique");
        if(checkUniqueEmail(null, createUserDTO.getUserEmail().trim()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This email has already been used.");
        User newUser = modelMapper.map(createUserDTO, User.class);
        newUser.setId(null);
        newUser.setUserName(newUser.getUserName().trim());
        newUser.setPassword(argon2.encode(newUser.getPassword()));
        repository.saveAndFlush(newUser);
        return modelMapper.map(newUser, UserDTO.class);
    }

    private User mapUser(User existingUser, UpdateUserDTO updateUser) {
        if(updateUser.getUserName().trim() != null){
        existingUser.setUserName(updateUser.getUserName().trim());
        }
        if(updateUser.getUserEmail() != null){
            updateUser.setUserEmail(updateUser.getUserEmail().trim());
            existingUser.setUserEmail(updateUser.getUserEmail().trim());
        }
        if(updateUser.getRole().trim() != null){
        existingUser.setRole(updateUser.getRole().trim());
        }
        return existingUser;
    }

    public User update(UpdateUserDTO updateUserDTO, Integer userId) {
        User oldUser = repository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                userId + " doesn't exist"));
        if(checkUniqueName(userId, updateUserDTO.getUserName().trim()) && checkUniqueEmail(userId, updateUserDTO.getUserEmail().trim()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name and email is not unique");
        if(checkUniqueName(userId, updateUserDTO.getUserName().trim()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name is not unique");
        if(checkUniqueEmail(userId, updateUserDTO.getUserEmail().trim()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This email has already been used.");
        if(checkDataChange(userId, updateUserDTO.getUserName().trim(), updateUserDTO.getUserEmail().trim(), updateUserDTO.getRole().trim()))
            return oldUser;
        User user = repository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                userId + " doesn't exist"));
        User updatedUser = mapUser(user, updateUserDTO);
        updatedUser.setUserEmail(updateUserDTO.getUserEmail());
        return repository.saveAndFlush(updatedUser);

    }

    public void delete(Integer userId){
        repository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, userId + " doesn't exist"));
        repository.deleteById(userId);
    }

    public UserMatchDTO checkMatch(UserMatchDTO userMatchCheck){
        if(repository.existsByUserEmail(userMatchCheck.getUserEmail())) {
            Optional<User> user = repository.findByUserEmail(userMatchCheck.getUserEmail());
            if(argon2.matches(userMatchCheck.getPassword(), user.get().getPassword())){
                throw new ResponseStatusException(HttpStatus.OK, "Password is Match");
            }else {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Password doesn't Match");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "A user with the specified email doesn't exist");
        }
    }

    private boolean checkUniqueName(Integer userId, String name) {
        Optional<User> user = repository.findByUserName(name);
        if(userId != null && user.isPresent()) {
            return !Objects.equals(user.get().getId(), userId);
        }
        return user.isPresent();
    }

    private boolean checkUniqueEmail(Integer userId, String email) {
        Optional<User> user = repository.findByUserEmail(email);
        if(userId != null && user.isPresent()) {
            return !Objects.equals(user.get().getId(), userId);
        }
        return user.isPresent();
    }

    private boolean checkDataChange(Integer userId, String name, String email, String role) {
        User user = repository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                userId + " doesn't exist"));
        if(user.getUserName() == name.trim() && user.getUserEmail() == email.trim() && user.getRole() == role.trim()){
            return true;
        }
        return false;
    }
}
