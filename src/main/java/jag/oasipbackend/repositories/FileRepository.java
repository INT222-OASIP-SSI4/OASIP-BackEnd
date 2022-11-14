package jag.oasipbackend.repositories;

import jag.oasipbackend.entities.File;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File, Integer> {
}