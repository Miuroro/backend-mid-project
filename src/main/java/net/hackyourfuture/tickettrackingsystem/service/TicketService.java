package net.hackyourfuture.tickettrackingsystem.service;

import net.hackyourfuture.tickettrackingsystem.dto.TicketDTO;
import net.hackyourfuture.tickettrackingsystem.exceptions.ResourceNotFoundException;
import net.hackyourfuture.tickettrackingsystem.models.Ticket;
import net.hackyourfuture.tickettrackingsystem.repository.ProjectRepository;
import net.hackyourfuture.tickettrackingsystem.repository.TicketRepository;
import net.hackyourfuture.tickettrackingsystem.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TicketService {

    private static final Logger logger = LoggerFactory.getLogger(TicketService.class);

    private static final String TICKET_NOT_FOUND_MSG = "Ticket not found with id: ";

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final EmailService emailService;

    public TicketService(TicketRepository ticketRepository, UserRepository userRepository, ProjectRepository projectRepository, EmailService emailService) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.emailService = emailService;
    }

    // Create a Ticket
    public TicketDTO createTicket(TicketDTO dto) {
        ensureProjectExists(dto.getProjectId());

        Ticket ticket = new Ticket();
        ticket.setTitle(dto.getTitle());
        ticket.setDescription(dto.getDescription());

        ticket.setStatus(normalizeStatus(dto.getStatus()));
        ticket.setProjectId(dto.getProjectId());
        ticket.setCreatedAt(LocalDateTime.now());

        Ticket savedTicket = ticketRepository.save(ticket);

        sendNotificationLog("TICKET CREATED", "Ticket ID: " + savedTicket.getId() + " - " + savedTicket.getTitle());

        return mapToDTO(savedTicket);
    }

    // Get Single Ticket
    public TicketDTO getTicketById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(TICKET_NOT_FOUND_MSG + id));
        return mapToDTO(ticket);
    }

    // Search & List Tickets
    public List<TicketDTO> searchTickets(String status, String search) {
        return ticketRepository.search(status, search).stream()
                .map(this::mapToDTO)
                .toList();
    }

    // Update Tickets
    public TicketDTO updateTicket(Long id, TicketDTO dto) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(TICKET_NOT_FOUND_MSG + id));

        ensureProjectExists(dto.getProjectId());

        ticket.setTitle(dto.getTitle());
        ticket.setDescription(dto.getDescription());
        ticket.setStatus(normalizeStatus(dto.getStatus()));
        ticket.setProjectId(dto.getProjectId());
        ticket.setUpdatedAt(LocalDateTime.now());

        ticketRepository.update(ticket);
        triggerEmailAlert(id, ticket.getTitle(), ticket.getStatus());
        sendNotificationLog("TICKET UPDATED", "Ticket ID: " + ticket.getId() + " status is now " + ticket.getStatus());

        return mapToDTO(ticket);
    }

    // Add Assignee
    public void addAssignee(Long ticketId, Long userId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException(TICKET_NOT_FOUND_MSG + ticketId));

        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        ticketRepository.addAssignee(ticketId, userId);
        // email trigger
        triggerEmailAlert(ticketId, ticket.getTitle(), ticket.getStatus());
        sendNotificationLog("ASSIGNEE ADDED", "User ID " + userId + " assigned to Ticket ID " + ticketId);
    }

    // Remove Assignee
    public void removeAssignee(Long ticketId, Long userId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException(TICKET_NOT_FOUND_MSG + ticketId));

        ticketRepository.removeAssignee(ticketId, userId);
        // email trigget
        triggerEmailAlert(ticketId, ticket.getTitle(), ticket.getStatus());
        sendNotificationLog("ASSIGNEE REMOVED", "User ID " + userId + " unassigned from Ticket ID " + ticketId);
    }

    // Helper: Map ticket entity to dynamic DTO
    private TicketDTO mapToDTO(Ticket ticket) {
        return new TicketDTO(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getProjectId(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt()
        );
    }

    private void sendNotificationLog(String action, String details) {
        try {
            logger.info("[NOTIFICATION SIMULATION] Action: {} | Details: {}", action, details);
        } catch (Exception e) {
            logger.error("Notification service failed gracefully", e);
        }
    }

    private void ensureProjectExists(Long projectId) {
        projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status is required and must be open, in progress, or closed.");
        }

        String normalizedStatus = status.trim().toLowerCase();
        if (!List.of("open", "in progress", "closed").contains(normalizedStatus)) {
            throw new IllegalArgumentException("Invalid status value. Must be open, in progress, or closed.");
        }

        return normalizedStatus;
    }

    private void triggerEmailAlert(Long ticketId, String title, String status) {
        try {
            // Fetch the assigned user IDs from the database repository
            List<Long> assigneeIds = ticketRepository.findAssigneeIdsByTicketId(ticketId);

            // Look up each user in the database and pull their real names
            List<String> assigneeNames = assigneeIds.stream()
                    .map(id -> userRepository.findById(id).orElse(null))
                    .filter(user -> user != null && user.getName() != null)
                    .map(user -> user.getName())
                    .toList();
            // my email for Resend
            List<String> emails = List.of("monera.mual@gmail.com");

            // Dispatch the message
            emailService.sendTicketUpdateEmail(emails, ticketId, title, status, assigneeNames);
        } catch (Exception e) {
            logger.error("Failed to compile ticket assignee contacts list for email. Error: {}", e.getMessage());
        }
    }
}