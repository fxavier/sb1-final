package com.ecommerce.domain.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductWithImagesDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private Double averageRating;
    private String coverImageUrl;
    private List<String> imageUrls;
    private Long categoryId;
    private String categoryName;
    private Boolean active;
}