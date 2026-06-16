package net.hackyourfuture.tickettrackingsystem.controllers;

import jakarta.validation.Valid;
import net.hackyourfuture.tickettrackingsystem.dto.TicketDTO;
import net.hackyourfuture.tickettrackingsystem.service.TicketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    // Create a new ticket
    @PostMapping
    public ResponseEntity<TicketDTO> createTicket(@Valid @RequestBody TicketDTO ticketDTO) {
        return new ResponseEntity<>(ticketService.createTicket(ticketDTO), HttpStatus.CREATED);
    }

    //  GET /api/tickets - Handles BOTH "Get All" and "Filtered Search" via query params
    @GetMapping
    public ResponseEntity<List<TicketDTO>> searchTickets(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ticketService.searchTickets(status, search));
    }

    // Get a single ticket by ID
    @GetMapping("/{id}")
    public ResponseEntity<TicketDTO> getTicketById(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getTicketById(id));
    }

    // Update a tickets
    @PutMapping("/{id}")
    public ResponseEntity<TicketDTO> updateTicket(@PathVariable Long id, @Valid @RequestBody TicketDTO ticketDTO) {
        return ResponseEntity.ok(ticketService.updateTicket(id, ticketDTO));
    }

    // Add an assignee to a ticket
    @PostMapping("/{id}/assignees")
    public ResponseEntity<Void> addAssignee(@PathVariable Long id, @RequestBody java.util.Map<String, Long> payload) {
        Long userId = payload.get("userId");
        ticketService.addAssignee(id, userId);
        return ResponseEntity.ok().build();
    }

    // Remove an assignee from a ticket
    @DeleteMapping("/{id}/assignees/{userId}")
    public ResponseEntity<Void> removeAssignee(@PathVariable Long id, @PathVariable Long userId) {
        ticketService.removeAssignee(id, userId);
        return ResponseEntity.noContent().build();
    }
}