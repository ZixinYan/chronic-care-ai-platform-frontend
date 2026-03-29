package com.zixin.thirdpartyapi.dto;

import com.zixin.utils.utils.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class OSSMultiUploadFileRequest extends OSSUploadFileRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private long partSize;
    private int maxRetry;
}
