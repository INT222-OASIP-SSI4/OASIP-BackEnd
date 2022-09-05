package jag.oasipbackend.services;

import jag.oasipbackend.entities.User;
import jag.oasipbackend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class JwtUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository repository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> user = repository.findByUserEmail(email);
        return new org.springframework.security.core.userdetails.User(user.get().getUserEmail(), user.get().getPassword() , new ArrayList<>());
    }
}
