package com.zixin.thirdpartyapi.dto;

import com.zixin.utils.utils.BaseResponse;
import lombok.Data;

import java.io.Serializable;

@Data
public class OSSMultiUploadFileResponse extends BaseResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private String url;
}
