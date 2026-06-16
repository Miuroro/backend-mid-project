package net.hackyourfuture.tickettrackingsystem.repository;

import net.hackyourfuture.tickettrackingsystem.exceptions.DatabaseAccessException;
import net.hackyourfuture.tickettrackingsystem.models.User;
import org.springframework.stereotype.Repository;
import org.springframework.dao.DataIntegrityViolationException;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {

    private final DataSource dataSource;

    public UserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // Save User
    public User save(User user) {
        String sql = "INSERT INTO users (name, email) VALUES (?, ?);";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getLong(1));
                }
            }
            return user;
        } catch (SQLException e) {
            if (isUniqueEmailViolation(e)) {
                throw new DataIntegrityViolationException("Email already exists", e);
            }
            throw new DatabaseAccessException("Database error saving user", e);
        }
    }

    // Find All
    public List<User> findAll() {
        String sql = "SELECT id, name, email FROM users;";
        List<User> users = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                users.add(new User(rs.getLong("id"), rs.getString("name"), rs.getString("email")));
            }
            return users;
        } catch (SQLException e) {
            throw new DatabaseAccessException("Database error fetching users", e);
        }
    }

    // Find By ID
    public Optional<User> findById(Long id) {
        String sql = "SELECT id, name, email FROM users WHERE id = ?;";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new User(rs.getLong("id"), rs.getString("name"), rs.getString("email")));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseAccessException("Database error fetching user by id", e);
        }
        return Optional.empty();
    }

    // Update
    public void update(User user) {
        String sql = "UPDATE users SET name = ?, email = ? WHERE id = ?;";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.setLong(3, user.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            if (isUniqueEmailViolation(e)) {
                throw new DataIntegrityViolationException("Email already exists", e);
            }
            throw new DatabaseAccessException("Database error updating user", e);
        }
    }

    private boolean isUniqueEmailViolation(SQLException e) {
        return "23505".equals(e.getSQLState());
    }

    // Delete
    public void deleteById(Long id) {
        String sql = "DELETE FROM users WHERE id = ?;";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseAccessException("Database error deleting user", e);
        }
    }
}