package jag.oasipbackend.services;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface StorageService {
    void deleteFile(Integer id);

    void init();

    String storeFile(MultipartFile file, Integer id) throws IOException;

    void store(MultipartFile file);

    Stream<Path> loadAll();

    Path load(String filename);

    Resource loadAsResource(String filename,Integer id);

    void deleteAll();
}
