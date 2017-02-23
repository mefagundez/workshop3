package hello.models;

import lombok.Data;

@Data
public class AppFile {

    private Long size;
    private String type;
    private String name;
    private String path;
}
