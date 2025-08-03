package com.paras.Arthra.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryDTO {
    private Long id;
    private Long profileId;
    private String icon;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updateddAt;
    private String type;
}
