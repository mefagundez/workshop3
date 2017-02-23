# Workshop 2 (File Manipulation in Rest APIs)

### What you need to have installed: 

- Java 8
- Apache Maven 3.3.9

### How to run the application

    mvn spring-boot:run

### How to run tests

    mvn test
    
### File Handling

We are using an streaming approach in the `GET` endpoint from a file and a Multipart approach for uploading/updating a file. The files are being stored encrypted in the server and are only readable by using the `GET` endpoint. Each user can update and delete only the files they have uploaded. 
