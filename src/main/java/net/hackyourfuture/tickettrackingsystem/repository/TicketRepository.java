package net.hackyourfuture.tickettrackingsystem.repository;

import net.hackyourfuture.tickettrackingsystem.exceptions.DatabaseAccessException;
import net.hackyourfuture.tickettrackingsystem.models.Ticket;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class TicketRepository {

    private final DataSource dataSource;

    public TicketRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // Create a new ticket
    public Ticket save(Ticket ticket) {
        String sql = "INSERT INTO tickets (title, description, status, project_id, created_at) VALUES (?, ?, ?, ?, ?);";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, ticket.getTitle());
            stmt.setString(2, ticket.getDescription());
            stmt.setString(3, ticket.getStatus());
            stmt.setLong(4, ticket.getProjectId());
            stmt.setTimestamp(5, Timestamp.valueOf(ticket.getCreatedAt()));

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    ticket.setId(keys.getLong(1));
                }
            }
            return ticket;
        } catch (SQLException e) {
            throw new DatabaseAccessException("Database error saving ticket", e);
        }
    }

    // Find a single ticket by ID
    public Optional<Ticket> findById(Long id) {
        String sql = "SELECT id, title, description, status, project_id, created_at, updated_at FROM tickets WHERE id = ?;";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToTicket(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseAccessException("Database error finding ticket by id", e);
        }
        return Optional.empty();
    }

    // Dynamic Search with AND logic (Handles status, keyword text, or no filters)
    public List<Ticket> search(String status, String searchKeyword) {
        List<Ticket> tickets = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM tickets WHERE 1=1 ");

        if (status != null && !status.isBlank()) {
            sql.append("AND status = ? ");
        }
        if (searchKeyword != null && !searchKeyword.isBlank()) {
            sql.append("AND (title ILIKE ? OR description ILIKE ?) ");
        }
        sql.append("ORDER BY id ASC;");

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            if (status != null && !status.isBlank()) {
                stmt.setString(paramIndex++, status);
            }
            if (searchKeyword != null && !searchKeyword.isBlank()) {
                String keywordParam = "%" + searchKeyword + "%";
                stmt.setString(paramIndex++, keywordParam);
                stmt.setString(paramIndex++, keywordParam);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tickets.add(mapRowToTicket(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseAccessException("Database error during ticket search", e);
        }
        return tickets;
    }

    // Update tickets & set update timestamp automatically
    public void update(Ticket ticket) {
        String sql = "UPDATE tickets SET title = ?, description = ?, status = ?, project_id = ?, updated_at = ? WHERE id = ?;";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, ticket.getTitle());
            stmt.setString(2, ticket.getDescription());
            stmt.setString(3, ticket.getStatus());
            stmt.setLong(4, ticket.getProjectId());
            stmt.setTimestamp(5, Timestamp.valueOf(ticket.getUpdatedAt()));
            stmt.setLong(6, ticket.getId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseAccessException("Database error updating ticket fields", e);
        }
    }

    // Add an Assignee to a ticket
    public void addAssignee(Long ticketId, Long userId) {
        String sql = "INSERT INTO ticket_assignees (ticket_id, user_id) VALUES (?, ?) ON CONFLICT DO NOTHING;";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, ticketId);
            stmt.setLong(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseAccessException("Database error adding ticket assignee", e);
        }
    }

    // Remove an Assignee from a ticket
    public void removeAssignee(Long ticketId, Long userId) {
        String sql = "DELETE FROM ticket_assignees WHERE ticket_id = ? AND user_id = ?;";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, ticketId);
            stmt.setLong(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseAccessException("Database error removing ticket assignee", e);
        }
    }

    // Find all assigned user IDs for a single ticket
    public List<Long> findAssigneeIdsByTicketId(Long ticketId) {
        List<Long> userIds = new ArrayList<>();
        String sql = "SELECT user_id FROM ticket_assignees WHERE ticket_id = ?;";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, ticketId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    userIds.add(rs.getLong("user_id"));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseAccessException("Database error fetching assignee IDs", e);
        }
        return userIds;
    }

    // Helper method to extract ticket row mapping and translate it into Java objects.
    private Ticket mapRowToTicket(ResultSet rs) throws SQLException {
        Timestamp updatedTimestamp = rs.getTimestamp("updated_at");
        LocalDateTime updatedAt = (updatedTimestamp != null) ? updatedTimestamp.toLocalDateTime() : null;

        return new Ticket(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getString("status"),
                rs.getLong("project_id"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                updatedAt
        );
    }
}