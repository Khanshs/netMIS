package netcafe.dao;

import netcafe.model.NetSpecs.ComputerSection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** SQL CRUD for the ComputerSection model; business rules stay in the service. */
public final class ComputerSectionDAO {
    public void insert(Connection connection, ComputerSection section) throws SQLException {
        String sql = "INSERT INTO computer_sections(section_id, section_name, section_price) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, section.getSectionId());
            statement.setString(2, section.getSectionName());
            statement.setBigDecimal(3, section.getSectionPrice());
            statement.executeUpdate();
        }
    }

    public Optional<ComputerSection> findById(Connection connection, String id) throws SQLException {
        String sql = "SELECT section_id, section_name, section_price FROM computer_sections WHERE section_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(map(rows)) : Optional.empty();
            }
        }
    }

    public List<ComputerSection> findAll(Connection connection) throws SQLException {
        String sql = "SELECT section_id, section_name, section_price FROM computer_sections ORDER BY section_id";
        List<ComputerSection> result = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) result.add(map(rows));
        }
        return result;
    }

    public boolean update(Connection connection, ComputerSection section) throws SQLException {
        String sql = "UPDATE computer_sections SET section_name = ?, section_price = ? WHERE section_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, section.getSectionName());
            statement.setBigDecimal(2, section.getSectionPrice());
            statement.setString(3, section.getSectionId());
            return statement.executeUpdate() == 1;
        }
    }

    /** Deletion is rejected by the database while computers reference this section. */
    public boolean deleteById(Connection connection, String id) throws SQLException {
        String sql = "DELETE FROM computer_sections WHERE section_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            return statement.executeUpdate() == 1;
        }
    }

    private ComputerSection map(ResultSet rows) throws SQLException {
        return new ComputerSection(
                rows.getString("section_id"),
                rows.getString("section_name"),
                rows.getBigDecimal("section_price"));
    }
}
