package jag.oasipbackend.repositories;

import jag.oasipbackend.entities.UserCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserCategoryRepository extends JpaRepository<UserCategory, Integer> {

}