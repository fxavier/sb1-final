package com.ecommerce.domain.dto;

import lombok.Data;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@Data
public class ProductImageUploadDTO {
    private FileUpload file;
    private Boolean isCover;
}