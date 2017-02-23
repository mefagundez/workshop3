package hello.controllers;

import hello.constants.FileConstants;
import hello.constants.FileErrorMessages;
import hello.exceptions.BadRequestException;
import hello.exceptions.FileNotInStorageException;
import hello.exceptions.ForbiddenException;
import hello.models.AppFile;
import hello.services.FileService;
import hello.util.EncryptionUtil;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.List;

@RestController
public class FileController {


    @Autowired
    private FileService fileService;

    private final String contentDisposition = "attachment;filename=%s";

    /* Get file */
    @RequestMapping(method = RequestMethod.GET, value = "file/{fileName:.+}")
    public void getFile(@PathVariable String fileName, HttpServletResponse response) throws IOException, GeneralSecurityException {

        Path filePath = Paths.get(FileConstants.ROOT_PATH, fileName);
        if (!Files.exists(filePath)) {
            throw new FileNotInStorageException(FileErrorMessages.FILE_NOT_FOUND);
        }
        byte[] actualFile = EncryptionUtil.decrypt(Files.readAllBytes(filePath));

        InputStream myStream = new ByteArrayInputStream(actualFile);
        response.addHeader("Content-disposition", String.format(contentDisposition, fileName));
        response.setContentType(Files.probeContentType(filePath));
        IOUtils.copy(myStream, response.getOutputStream());
        response.flushBuffer();

    }


    /* Upload File */
    @RequestMapping(method = RequestMethod.POST, value = "/file")
    public ResponseEntity<?> handleFileUpload(HttpServletRequest request, @RequestParam(name = "file", required = false) MultipartFile file) throws Exception{
        // It's being set the file as not required to override the validation with a bad request status
        if (file != null && !file.isEmpty()) {
            if (!fileService.isValid(file)) {
                throw new BadRequestException(FileErrorMessages.FORMAT_NOT_ALLOWED);
            }
            if (request.getHeader(FileConstants.CLIENT_SECRET_HEADER) == null) {
                throw new ForbiddenException(FileErrorMessages.AUTHORIZATION_REQUIRED);
            }
            return fileService.uploadFile(file, FileConstants.ROOT_PATH, request.getHeader(FileConstants.CLIENT_SECRET_HEADER));
        } else {
            throw new BadRequestException(FileErrorMessages.EMPTY_FILE);
        }
    }

    /* Update file */
    @RequestMapping(method = RequestMethod.PUT, value = "file/{fileName:.+}")
    public ResponseEntity<?> handleFileUpdate(HttpServletRequest request, @RequestParam(name = "file", required = false) MultipartFile file,
                                   @PathVariable String fileName) throws Exception {

        // It's being set the file as not required to override the validation with a bad request status
        if (file != null && !file.isEmpty()) {

            if (!fileService.isValid(file)) {
                throw new BadRequestException(FileErrorMessages.FORMAT_NOT_ALLOWED);
            }
            if (request.getHeader(FileConstants.CLIENT_SECRET_HEADER) == null) {
                throw new ForbiddenException(FileErrorMessages.AUTHORIZATION_REQUIRED);
            }

            return fileService.updateFile(file, FileConstants.ROOT_PATH, fileName, request.getHeader(FileConstants.CLIENT_SECRET_HEADER));
        } else {
            throw new BadRequestException(FileErrorMessages.EMPTY_FILE);
        }

    }

    /* Get files */
    @RequestMapping(method = RequestMethod.GET, value = "files")
    public List<AppFile> getFiles() {
        return fileService.getFiles(FileConstants.ROOT_PATH);
    }

    /* Delete File */
    @RequestMapping(method = RequestMethod.DELETE, value = "file/{fileName:.+}")
    public ResponseEntity<?> handleFileDeletion(HttpServletRequest request, @PathVariable String fileName) {
        if (request.getHeader(FileConstants.CLIENT_SECRET_HEADER) == null) {
            throw new ForbiddenException(FileErrorMessages.AUTHORIZATION_REQUIRED);
        }
        return fileService.deleteFile(fileName, FileConstants.ROOT_PATH, request.getHeader(FileConstants.CLIENT_SECRET_HEADER));
    }


    /* Exception handlers */

    @ExceptionHandler(BadRequestException.class)
    protected void handleBadRequestException(BadRequestException ex,
                                                  HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    protected void handleUnauthorizedException(BadRequestException ex,
                                             HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.FORBIDDEN.value(), ex.getMessage());
    }

    @ExceptionHandler(FileNotInStorageException.class)
    protected void handleFileNotInStorageException(BadRequestException ex,
                                                    HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.NOT_FOUND.value(), ex.getMessage());
    }

}