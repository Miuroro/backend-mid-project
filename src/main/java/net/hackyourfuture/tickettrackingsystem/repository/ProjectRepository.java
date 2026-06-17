package net.hackyourfuture.tickettrackingsystem.repository;

import net.hackyourfuture.tickettrackingsystem.dto.ProjectDTO;
import net.hackyourfuture.tickettrackingsystem.exceptions.DatabaseAccessException;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ProjectRepository {

    private final DataSource dataSource;

    public ProjectRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    // get all projects
    public List<ProjectDTO> findAllWithTicketCounts() {
        List<ProjectDTO> projects = new ArrayList<>();

        // This query counts status totals grouped by each project ID.
        // If a project has 0 tickets, the count safely defaults to 0.
        String sql = "SELECT p.id, p.name, " +
                "COUNT(CASE WHEN t.status = 'open' THEN 1 END) AS open_count, " +
                "COUNT(CASE WHEN t.status = 'in progress' THEN 1 END) AS in_progress_count, " +
                "COUNT(CASE WHEN t.status = 'closed' THEN 1 END) AS closed_count " +
                "FROM projects p " +
                "LEFT JOIN tickets t ON p.id = t.project_id " +
                "GROUP BY p.id, p.name " +
                "ORDER BY p.id ASC;";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ProjectDTO dto = new ProjectDTO(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getInt("open_count"),
                        rs.getInt("in_progress_count"),
                        rs.getInt("closed_count")
                );
                projects.add(dto);
            }
        } catch (SQLException e) {
            throw new DatabaseAccessException("Database error fetching project summaries", e);
        }
        return projects;
    }
}