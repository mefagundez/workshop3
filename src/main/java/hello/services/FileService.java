package hello.services;

import hello.constants.FileErrorMessages;
import hello.exceptions.FileNotInStorageException;
import hello.exceptions.ForbiddenException;
import hello.models.AppFile;
import hello.util.EncryptionUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FileService {

    private final String fullPath = "%s/%s";
    private final String formatPath = "%s.%s";
    private HashMap<String, String> filesStored = new HashMap<>();
    private List<String> whiteListedFormats = Collections.unmodifiableList(Arrays.asList(
                    MediaType.APPLICATION_PDF_VALUE,
                    MediaType.IMAGE_JPEG_VALUE,
                    MediaType.IMAGE_GIF_VALUE,
                    MediaType.IMAGE_PNG_VALUE
            )
    );

    public List<AppFile> getFiles(String rootPath){
        List<AppFile> appFiles = filesStored.keySet().stream().map(s -> {

            AppFile file = new AppFile();
            Path filePath = Paths.get(rootPath, s);
            try {
                file.setSize(Files.size(filePath));
                file.setPath(String.format(fullPath, rootPath, s));
                file.setType(FilenameUtils.getExtension(filePath.toString()));
                file.setName(s);
            } catch (IOException e) {
                return file;
            }
            return file;
        }).collect(Collectors.toList());

        return appFiles;
    }

    public ResponseEntity<?> uploadFile(MultipartFile file, String rootPath, String clientSecret) throws Exception {
        String fileExtension = file.getOriginalFilename().split("\\.")[1];
        String savedFile = saveFile(file, rootPath, fileExtension, clientSecret);
        return new ResponseEntity<>(savedFile, null, HttpStatus.CREATED);
    }

    public ResponseEntity<?> updateFile(MultipartFile file, String rootPath, String fileToUpdate, String clientSecret) throws Exception {
        Path filePath = Paths.get(rootPath, fileToUpdate);
        if (Files.exists(filePath)) {
            if (!isClientSecret(fileToUpdate, clientSecret)){
                throw new ForbiddenException(FileErrorMessages.FORBIDDEN);
            }
            removeFile(filePath, fileToUpdate);
            addToStorage(file, filePath, fileToUpdate, clientSecret);
        } else {
            throw new FileNotInStorageException(FileErrorMessages.FILE_NOT_FOUND);
        }
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> deleteFile(String fileId, String rootPath, String clientSecret) {

        Path filePath = Paths.get(rootPath, fileId);
        if (Files.exists(filePath)) {
            if (!isClientSecret(fileId, clientSecret)){
                throw new ForbiddenException(FileErrorMessages.FORBIDDEN);
            }
            removeFile(filePath, fileId);
        } else {
            throw new FileNotInStorageException(FileErrorMessages.FILE_NOT_FOUND);
        }
        return ResponseEntity.ok().build();
    }


    public boolean isValid(MultipartFile file) {
        return isValidFormat(file);
    }

    private boolean isValidFormat(MultipartFile file) {
        return file.getContentType() == null ? false : whiteListedFormats.contains(file.getContentType());
    }

    private void removeFile(Path filePath, String fileId) {
        try {
            Files.delete(filePath);
            filesStored.remove(fileId);
        } catch (IOException e) {
            throw new InternalError();
        }
    }

    private String saveFile(MultipartFile file, String rootPath, String fileExtension, String clientSecret) throws Exception {
        String randomUUID = String.format(formatPath, UUID.randomUUID().toString(), fileExtension);
        Path filePath = Paths.get(rootPath, randomUUID);
        addToStorage(file, filePath, randomUUID, clientSecret);
        return randomUUID;
    }


    private void addToStorage(MultipartFile file, Path filePath, String fileId, String clientSecret) throws Exception {
        try {
            FileUtils.writeByteArrayToFile(new File(filePath.toString()), EncryptionUtil.encrypt(file.getBytes()));
            filesStored.put(fileId, clientSecret);
        } catch (IOException e) {
            throw new InternalError();
        }

    }

    private boolean isClientSecret(String fileId, String clientSecret) {
        return filesStored.containsKey(fileId) && filesStored.get(fileId).equals(clientSecret);
    }
}
