package com.esp.esp_diplomas.dto;

import lombok.Data;

@Data
public class DocumentDTO {
    private Long id;
    private String title;
    private String description;
    private String type;
    private String dateGenerated;
    private boolean available;
    private String url;

    public DocumentDTO(Long id, String title, String description, String type,
                       String dateGenerated, boolean available, String url) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
        this.dateGenerated = dateGenerated;
        this.available = available;
        this.url = url;
    }
}