package jag.oasipbackend.storage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;
import java.util.Objects;

import jag.oasipbackend.services.StorageService;
import jag.oasipbackend.utils.ListMapper;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileSystemStorageService implements StorageService {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ListMapper listMapper;

    private final Path rootLocation;

    private final Path fileStorageLocation;
//    private final Path rootLocation = Paths.get("uploads");

    @Autowired
    public FileSystemStorageService(StorageProperties properties) {
        this.rootLocation = Paths.get(properties.getLocation());
        this.fileStorageLocation = Paths.get(properties.getLocation()).toAbsolutePath().normalize();


    }

    @Override
    public String storeFile(MultipartFile file, Integer id) {

        try {

//      Normalize file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

//      call id -> file
        Path getpathbyid = Paths.get(String.valueOf(this.fileStorageLocation)+ "/" + id);

//      create New Dircertoty
        Files.createDirectories(getpathbyid);

        System.out.println(fileName);
        System.out.println(getpathbyid);

        try {
            // Check if the file's name contains invalid characters
            if(fileName.contains("..")) {
                throw new StorageException("Sorry! Filename contains invalid path sequence " + fileName);
            }

//          Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = getpathbyid.resolve(fileName);
//          see targetLocation file
            System.out.println(targetLocation);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            throw new StorageException("Could not store file " + fileName + ". Please try again!", ex);
        }} catch (IOException e) {
            throw new StorageException("Could not store file ", e);
        }
    }

    @Override
    public void store(MultipartFile file) {
        try {
            Files.createDirectories(rootLocation);

                            Path destinationFile = this.rootLocation.resolve(Paths.get(Objects.requireNonNull(file.getOriginalFilename()))).normalize().toAbsolutePath();

            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                // This is a security check
                throw new StorageException(
                        "Cannot store file outside current directory.");
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile,
                        StandardCopyOption.REPLACE_EXISTING);
            }
        }
        catch (IOException e) {
            throw new StorageException("Failed to store file.", e);
        }
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.rootLocation, 1)
                    .filter(path -> !path.equals(this.rootLocation))
                    .map(this.rootLocation::relativize);
        }
        catch (IOException e) {
            throw new StorageException("Failed to read stored files", e);
        }

    }

    @Override
    public Path load(String filename) {
        return this.rootLocation.resolve(filename);
    }

    public Path getPath(Integer id) {
        Path getpathbyid = Paths.get(String.valueOf(this.fileStorageLocation)+ "/" + id);
        return getpathbyid;
    }

    @Override
    public Resource loadAsResource(String filename,Integer id) {
        try {
            Path file = getPath(id).resolve(filename).normalize();
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            }
            else {
                throw new StorageFileNotFoundException(
                        "Could not read file: " + filename);

            }
        }
        catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

    @Override
    public void deleteFile(Integer id){
        try {
            FileUtils.deleteDirectory(new File(getPath(id).toUri()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        }
        catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }
}
