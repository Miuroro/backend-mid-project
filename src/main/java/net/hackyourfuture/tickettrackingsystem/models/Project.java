package net.hackyourfuture.tickettrackingsystem.models;

public class Project {
    private Long id;
    private String name;

    // Constructor
    public Project(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}