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
        User newUser = modelMapper.map(createUserDTO, User.class);
        newUser.setId(null);
        return repository.saveAndFlush(newUser);
    }

    private User mapUser(User existingUser, UpdateUserDTO updateUser) {
        existingUser.setUserName(updateUser.getUserName());
        existingUser.setUserEmail(updateUser.getUserEmail());
        existingUser.setRole(updateUser.getRole());
        return existingUser;
    }

    public User update(UpdateUserDTO updateUserDTO, Integer userId) {
        User user = repository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                userId + " doesn't exist"));
        User updatedUser = mapUser(user, updateUserDTO);
        return repository.saveAndFlush(updatedUser);
    }

    public void delete(Integer userId){
        repository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, userId + " doesn't exist"));
        repository.deleteById(userId);
    }
}
