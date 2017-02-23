import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import hello.Application;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = Application.class)
public class FileTest {

    @Value("classpath:javascript.png")
    private File testFile;

    @Value("classpath:javascript2.png")
    private File testSecondFile;

    @Value("classpath:test1.txt")
    private File testTxtFile;

    @Autowired
    private TestRestTemplate restTemplate;

    private String url = "/%s";

    private static final String CLIENT_SECRET_KEY = "client_secret";

    private static final String CLIENT_SECRET_VALUE = "user";

    @Test
    public void getFileNotFound() {
        String endpointUrl = String.format(url,  "file/filename.pdf");
        ResponseEntity<JsonNode> response = restTemplate.exchange(endpointUrl, HttpMethod.GET, null, JsonNode.class);
        assertEquals("Wrong status code for getting a file not found", HttpStatus.NOT_FOUND, response.getStatusCode());
    }


    @Test
    public void deleteFileNotFound() {
        String endpointUrl = String.format(url,  "file/filename.pdf");
        ResponseEntity<JsonNode> response = restTemplate.exchange(endpointUrl, HttpMethod.DELETE, new HttpEntity<>(null, getHeaders(CLIENT_SECRET_KEY, CLIENT_SECRET_VALUE)), JsonNode.class);
        assertEquals("Wrong status code for deleting a file not found", HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void deleteFileNoClientSecret() throws Exception {
        String uploadUrl = String.format(url,  "file");
        MultiValueMap<String, Object> map = getImageEntity(testFile.getName(), testFile);
        ResponseEntity<String> uploadResponse = restTemplate.postForEntity(uploadUrl, getHttpEntity(map, CLIENT_SECRET_VALUE), String.class);
        String endpointUrl = String.format(url,  "file/" + uploadResponse.getBody());
        ResponseEntity<JsonNode> response = restTemplate.exchange(endpointUrl, HttpMethod.DELETE, null, JsonNode.class);
        assertEquals("Wrong status code for deleting a file with no client secret", HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void deleteFile() throws Exception {
        String uploadUrl = String.format(url,  "file");
        MultiValueMap<String, Object> map = getImageEntity(testFile.getName(), testFile);
        ResponseEntity<String> uploadResponse = restTemplate.postForEntity(uploadUrl, getHttpEntity(map, CLIENT_SECRET_VALUE), String.class);
        String endpointUrl = String.format(url,  "file/" + uploadResponse.getBody());
        ResponseEntity<JsonNode> response = restTemplate.exchange(endpointUrl, HttpMethod.DELETE, new HttpEntity<>(null, getHeaders(CLIENT_SECRET_KEY, CLIENT_SECRET_VALUE)), JsonNode.class);
        assertEquals("Wrong status code for deleting a file", HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void deleteFileWithWrongClientSecret() throws Exception {
        String uploadUrl = String.format(url,  "file");
        MultiValueMap<String, Object> map = getImageEntity(testFile.getName(), testFile);
        ResponseEntity<String> uploadResponse = restTemplate.postForEntity(uploadUrl, getHttpEntity(map, CLIENT_SECRET_VALUE), String.class);
        String endpointUrl = String.format(url,  "file/" + uploadResponse.getBody());
        ResponseEntity<JsonNode> response = restTemplate.exchange(endpointUrl, HttpMethod.DELETE, new HttpEntity<>(null, getHeaders(CLIENT_SECRET_KEY, "1")), JsonNode.class);
        assertEquals("Wrong status code for deleting a file with wrong client secret", HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void uploadEmptyFile() {
        String endpointUrl = String.format(url,  "file");
        ResponseEntity<JsonNode> response = restTemplate.exchange(endpointUrl, HttpMethod.POST, null, JsonNode.class);
        assertEquals("Wrong status code for uploading an empty file", HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void uploadFile() throws Exception {
        String endpointUrl = String.format(url,  "file");
        MultiValueMap<String, Object> map = getImageEntity(testFile.getName(), testFile);
        ResponseEntity<String> response = restTemplate.postForEntity(endpointUrl, getHttpEntity(map, CLIENT_SECRET_VALUE), String.class);
        assertEquals("Wrong status code for adding a new file", HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    public void uploadFileWithNoClientSecret() throws Exception {
        String endpointUrl = String.format(url,  "file");
        MultiValueMap<String, Object> map = getImageEntity(testFile.getName(), testFile);
        ResponseEntity<String> response = restTemplate.postForEntity(endpointUrl, new HttpEntity<>(map, null), String.class);
        assertEquals("Wrong status code for adding a new file with no client secret", HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void updateFile() throws Exception {
        String endpointUrl = String.format(url,  "file");
        MultiValueMap<String, Object> firstImage = getImageEntity(testFile.getName(), testFile);
        MultiValueMap<String, Object> secondImage = getImageEntity(testSecondFile.getName(), testSecondFile);
        ResponseEntity<String> response = restTemplate.postForEntity(endpointUrl, getHttpEntity(firstImage, CLIENT_SECRET_VALUE), String.class);
        String updateEndpointUrl = String.format(url,  "file/" + response.getBody());
        ResponseEntity<JsonNode> updateResponse = restTemplate.exchange(updateEndpointUrl, HttpMethod.PUT, getHttpEntity(secondImage, CLIENT_SECRET_VALUE), JsonNode.class);
        assertEquals("Wrong status code for updating a file", HttpStatus.OK, updateResponse.getStatusCode());
    }

    @Test
    public void updateFileNoClientSecret() throws Exception {
        String endpointUrl = String.format(url,  "file");
        MultiValueMap<String, Object> firstImage = getImageEntity(testFile.getName(), testFile);
        MultiValueMap<String, Object> secondImage = getImageEntity(testSecondFile.getName(), testSecondFile);
        ResponseEntity<String> response = restTemplate.postForEntity(endpointUrl, getHttpEntity(firstImage, CLIENT_SECRET_VALUE), String.class);
        String updateEndpointUrl = String.format(url,  "file/" + response.getBody());
        ResponseEntity<JsonNode> updateResponse = restTemplate.exchange(updateEndpointUrl, HttpMethod.PUT, new HttpEntity<>(secondImage, null), JsonNode.class);
        assertEquals("Wrong status code for updating a file with no client secret", HttpStatus.FORBIDDEN, updateResponse.getStatusCode());
    }

    @Test
    public void updateFileNotFound() throws Exception {
        String endpointUrl = String.format(url,  "file");
        MultiValueMap<String, Object> firstImage = getImageEntity(testFile.getName(), testFile);
        MultiValueMap<String, Object> secondImage = getImageEntity(testSecondFile.getName(), testSecondFile);
        ResponseEntity<String> response = restTemplate.postForEntity(endpointUrl, getHttpEntity(firstImage, CLIENT_SECRET_VALUE), String.class);
        String updateEndpointUrl = String.format(url,  "file/1");
        ResponseEntity<JsonNode> updateResponse = restTemplate.exchange(updateEndpointUrl, HttpMethod.PUT, getHttpEntity(secondImage, CLIENT_SECRET_VALUE), JsonNode.class);
        assertEquals("Wrong status code for updating a file not found", HttpStatus.NOT_FOUND, updateResponse.getStatusCode());
    }

    @Test
    public void getFile() throws Exception {
        String endpointUrl = String.format(url,  "file");
        MultiValueMap<String, Object> map = getImageEntity(testFile.getName(), testFile);
        ResponseEntity<String> response = restTemplate.postForEntity(endpointUrl, getHttpEntity(map, CLIENT_SECRET_VALUE), String.class);
        String getEndpointUrl = String.format(url,  "file/" + response.getBody());
        ResponseEntity<String> getResponse = restTemplate.exchange(getEndpointUrl, HttpMethod.GET, null, String.class);
        assertEquals("Wrong status code for getting a file", HttpStatus.OK, getResponse.getStatusCode());
    }
    @Test
    public void uploadWrongFormatFile() throws Exception {
        String endpointUrl = String.format(url,  "file");
        MultiValueMap<String, Object> map = getTextEntity();
        ResponseEntity<String> response = restTemplate.postForEntity(endpointUrl, getHttpEntity(map, CLIENT_SECRET_VALUE), String.class);
        assertEquals("Wrong status code for adding a new file with wrong format", HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void updateWrongFormatFile() throws Exception {
        String endpointUrl = String.format(url,  "file");
        MultiValueMap<String, Object> firstImage = getImageEntity(testFile.getName(), testFile);
        MultiValueMap<String, Object> textEntity = getTextEntity();
        ResponseEntity<String> response = restTemplate.postForEntity(endpointUrl, getHttpEntity(firstImage, CLIENT_SECRET_VALUE), String.class);
        String updateEndpointUrl = String.format(url,  "file/" + response.getBody());
        ResponseEntity<JsonNode> updateResponse = restTemplate.exchange(updateEndpointUrl, HttpMethod.PUT, getHttpEntity(textEntity, CLIENT_SECRET_VALUE), JsonNode.class);
        assertEquals("Wrong status code for updating a file with wrong format", HttpStatus.BAD_REQUEST, updateResponse.getStatusCode());
    }

    @Test
    public void updateFileWrongClientSecret() throws Exception {
        String endpointUrl = String.format(url,  "file");
        MultiValueMap<String, Object> firstImage = getImageEntity(testFile.getName(), testFile);
        MultiValueMap<String, Object> secondImage = getImageEntity(testSecondFile.getName(), testSecondFile);
        ResponseEntity<String> response = restTemplate.postForEntity(endpointUrl, getHttpEntity(firstImage, CLIENT_SECRET_VALUE), String.class);
        String updateEndpointUrl = String.format(url,  "file/" + response.getBody());
        ResponseEntity<JsonNode> updateResponse = restTemplate.exchange(updateEndpointUrl, HttpMethod.PUT, getHttpEntity(secondImage, CLIENT_SECRET_VALUE + "1"), JsonNode.class);
        assertEquals("Wrong status code for updating a file with no client secret", HttpStatus.FORBIDDEN, updateResponse.getStatusCode());
    }

    @Test
    public void updateEmptyFile() throws Exception {
        String endpointUrl = String.format(url,  "file/fileName.pdf");
        ResponseEntity<JsonNode> response = restTemplate.exchange(endpointUrl, HttpMethod.PUT, null, JsonNode.class);
        assertEquals("Wrong status code for updating wth empty file", HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void getFiles() {
        String endpointUrl = String.format(url,  "files");
        ResponseEntity<JsonNode> response = restTemplate.exchange(endpointUrl, HttpMethod.GET, null, JsonNode.class);
        assertEquals("If there are no files it should return an empty list", ArrayNode.class, response.getBody().getClass());
    }

    private MultiValueMap<String, Object> getImageEntity(String fileName, File testFile) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        final String filename=fileName;
        ByteArrayResource contentsAsResource = new ByteArrayResource(FileUtils.readFileToByteArray(testFile)){
            @Override
            public String getFilename(){
                return filename;
            }
        };
        HttpEntity<ByteArrayResource> bytesPart = new HttpEntity<>(contentsAsResource, headers);
        map.add("file", bytesPart);

        return map;
    }

    private MultiValueMap<String, Object> getTextEntity() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        final String filename="test1.txt";
        ByteArrayResource contentsAsResource = new ByteArrayResource(FileUtils.readFileToByteArray(testTxtFile)){
            @Override
            public String getFilename(){
                return filename;
            }
        };
        HttpEntity<ByteArrayResource> bytesPart = new HttpEntity<>(contentsAsResource, headers);
        map.add("file", bytesPart);

        return map;
    }

    private HttpHeaders getHeaders(String clientSecretKey, String clientSecretValue) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set(clientSecretKey, clientSecretValue);
        return headers;
    }

    private HttpEntity<?> getHttpEntity(MultiValueMap<String, Object> map, String clientSecretValue) {
        return new HttpEntity<>(map, getHeaders(CLIENT_SECRET_KEY, clientSecretValue));
    }
}
