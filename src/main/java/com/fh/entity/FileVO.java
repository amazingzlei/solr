package com.fh.entity;

import lombok.Data;

@Data
public class FileVO {
    private String id;
    private String fileName;
    private String filePath;
    private String fileContent;
}
