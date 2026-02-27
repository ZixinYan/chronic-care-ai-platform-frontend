package com.zixin.thirdpartyapi.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.Serializable;

@Data
public class OSSUploadFileRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private String fileName;
    private byte[] file;
}
