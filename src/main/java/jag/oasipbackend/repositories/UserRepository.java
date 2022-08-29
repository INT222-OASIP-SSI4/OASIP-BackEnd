package jag.oasipbackend.repositories;

import jag.oasipbackend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUserName(String userName);

    Optional<User> findByUserEmail(String userEmail);

    public boolean existsByUserEmail(String userEmail);
}