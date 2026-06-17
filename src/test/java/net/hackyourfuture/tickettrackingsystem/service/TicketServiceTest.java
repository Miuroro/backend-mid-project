package net.hackyourfuture.tickettrackingsystem.service;

import net.hackyourfuture.tickettrackingsystem.dto.TicketDTO;
import net.hackyourfuture.tickettrackingsystem.exceptions.ResourceNotFoundException;
import net.hackyourfuture.tickettrackingsystem.models.Ticket;
import net.hackyourfuture.tickettrackingsystem.repository.ProjectRepository;
import net.hackyourfuture.tickettrackingsystem.repository.TicketRepository;
import net.hackyourfuture.tickettrackingsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TicketServiceTest {

    private TicketRepository ticketRepository;
    private UserRepository userRepository;
    private ProjectRepository projectRepository;
    private EmailService emailService;
    private TicketService ticketService;

    @BeforeEach
    void setUp() {
        // Create simple mock objects
        ticketRepository = Mockito.mock(TicketRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        projectRepository = Mockito.mock(ProjectRepository.class);
        emailService = Mockito.mock(EmailService.class);

        // Pass all 3 mocks so the constructor matches your service file exactly
        ticketService = new TicketService(ticketRepository, userRepository, projectRepository, emailService);
    }

    @Test
    void testCreateTicket_ShouldReturnSavedTicket() {
        TicketDTO inputDto = new TicketDTO(null, "Fix Login Bug", "Google login fails", "open", 1L, null, null);
        Ticket mockSavedTicket = new Ticket(100L, "Fix Login Bug", "Google login fails", "open", 1L, LocalDateTime.now(), null);

        Mockito.when(projectRepository.findById(1L)).thenReturn(java.util.Optional.of(new net.hackyourfuture.tickettrackingsystem.models.Project(1L, "Project A")));
        Mockito.when(ticketRepository.save(Mockito.any(Ticket.class))).thenReturn(mockSavedTicket);

        TicketDTO result = ticketService.createTicket(inputDto);

        assertNotNull(result.getId());
        assertEquals("Fix Login Bug", result.getTitle());
    }

    @Test
    void testCreateTicket_ShouldThrowException_WhenStatusMissing() {
        TicketDTO inputDto = new TicketDTO(null, "Fix Login Bug", "Google login fails", null, 1L, null, null);

        Mockito.when(projectRepository.findById(1L)).thenReturn(java.util.Optional.of(new net.hackyourfuture.tickettrackingsystem.models.Project(1L, "Project A")));

        assertThrows(IllegalArgumentException.class, () -> ticketService.createTicket(inputDto));
    }

    @Test
    void testCreateTicket_ShouldThrowException_WhenProjectMissing() {
        TicketDTO inputDto = new TicketDTO(null, "Fix Login Bug", "Google login fails", "open", 99L, null, null);

        Mockito.when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> ticketService.createTicket(inputDto));
    }

    @Test
    void testAddAssignee_ShouldThrowException_WhenUserNotFound() {
        Long ticketId = 1L;
        Long nonExistentUserId = 999L;

        Mockito.when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(new Ticket()));
        Mockito.when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            ticketService.addAssignee(ticketId, nonExistentUserId);
        });
    }

    @Test
    void testGetTicketById_ShouldThrowException_WhenTicketMissing() {
        Long missingId = 404L;
        Mockito.when(ticketRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            ticketService.getTicketById(missingId);
        });
    }
}