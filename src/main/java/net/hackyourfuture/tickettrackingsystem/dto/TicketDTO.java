package net.hackyourfuture.tickettrackingsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class TicketDTO {
    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;
    @NotBlank(message = "Status is required")
    @jakarta.validation.constraints.Pattern(regexp = "(?i)^(open|in progress|closed)$", message = "Status must be open, in progress, or closed")
    private String status;

    @NotNull(message = "Project ID is required")
    private Long projectId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructor
    public TicketDTO(Long id, String title, String description, String status, Long projectId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.projectId = projectId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}