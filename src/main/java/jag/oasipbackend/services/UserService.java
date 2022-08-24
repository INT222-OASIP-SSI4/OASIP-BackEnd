package jag.oasipbackend.services;

import jag.oasipbackend.dtos.CreateUserDTO;
import jag.oasipbackend.dtos.UpdateUserDTO;
import jag.oasipbackend.dtos.UserDTO;
import jag.oasipbackend.dtos.UserDetailDTO;
import jag.oasipbackend.entities.User;
import jag.oasipbackend.repositories.UserRepository;
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
public class UserService {
    @Autowired
    private UserRepository repository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ListMapper listMapper;

    public List<UserDTO> findAll() {
        List<User> users = repository.findAll(Sort.by("userName"));
        return listMapper.mapList(users, UserDTO.class, modelMapper);
    }

    public UserDetailDTO findById(Integer userId) {
        User user = repository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                userId + " doesn't exist"));
        return modelMapper.map(user, UserDetailDTO.class);
    }

    public User save(CreateUserDTO createUserDTO) {
        if(checkUniqueName(null, createUserDTO.getUserName().trim()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name is not unique");
        if(checkUniqueEmail(null, createUserDTO.getUserEmail().trim()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This email has already been used.");
        User newUser = modelMapper.map(createUserDTO, User.class);
        newUser.setId(null);
        newUser.setUserName(newUser.getUserName().trim());
        newUser.setUserEmail(newUser.getUserEmail().trim());
        return repository.saveAndFlush(newUser);
    }

    private User mapUser(User existingUser, UpdateUserDTO updateUser) {
        if(updateUser.getUserName() != null){
        existingUser.setUserName(updateUser.getUserName().trim());
        }
        if(updateUser.getUserEmail() != null){
        existingUser.setUserEmail(updateUser.getUserEmail().trim());
        }
        if(updateUser.getRole() != null){
        existingUser.setRole(updateUser.getRole());
        }
        return existingUser;
    }

    public User update(UpdateUserDTO updateUserDTO, Integer userId) {
        if(checkUniqueName(userId, updateUserDTO.getUserName().trim()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name is not unique");
        if(checkUniqueEmail(userId, updateUserDTO.getUserEmail().trim()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This email has already been used.");
        User user = repository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                userId + " doesn't exist"));
        User updatedUser = mapUser(user, updateUserDTO);
        return repository.saveAndFlush(updatedUser);
    }

    public void delete(Integer userId){
        repository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, userId + " doesn't exist"));
        repository.deleteById(userId);
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
}
