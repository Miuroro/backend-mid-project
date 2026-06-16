package net.hackyourfuture.tickettrackingsystem.dto;

public class ProjectDTO {
    private Long id;
    private String name;
    private int openTicketsCount;
    private int inProgressTicketsCount;
    private int closedTicketsCount;

    // Constructor
    public ProjectDTO(Long id, String name, int openTicketsCount, int inProgressTicketsCount, int closedTicketsCount) {
        this.id = id;
        this.name = name;
        this.openTicketsCount = openTicketsCount;
        this.inProgressTicketsCount = inProgressTicketsCount;
        this.closedTicketsCount = closedTicketsCount;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getOpenTicketsCount() { return openTicketsCount; }
    public void setOpenTicketsCount(int openTicketsCount) { this.openTicketsCount = openTicketsCount; }

    public int getInProgressTicketsCount() { return inProgressTicketsCount; }
    public void setInProgressTicketsCount(int inProgressTicketsCount) { this.inProgressTicketsCount = inProgressTicketsCount; }

    public int getClosedTicketsCount() { return closedTicketsCount; }
    public void setClosedTicketsCount(int closedTicketsCount) { this.closedTicketsCount = closedTicketsCount; }
}