package netcafe.dao;

import netcafe.enums.ComputerStatus;
import netcafe.model.NetSpecs.Computer;
import netcafe.model.NetSpecs.ComputerSection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Maps computers and their section relationship to SQL rows. */
public final class ComputerDAO {
    private static final String SELECT_WITH_SECTION =
            "SELECT c.computer_id, c.computer_status, "
            + "s.section_id, s.section_name, s.section_price "
            + "FROM computers c JOIN computer_sections s ON s.section_id = c.section_id";

    public void insert(Connection connection, Computer computer) throws SQLException {
        String sql = "INSERT INTO computers(computer_id, section_id, computer_status) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, computer.getComputerId());
            statement.setString(2, computer.getSection().getSectionId());
            statement.setString(3, computer.getComputerStatus().name());
            statement.executeUpdate();
        }
    }

    public Optional<Computer> findById(Connection connection, String id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                SELECT_WITH_SECTION + " WHERE c.computer_id = ?")) {
            statement.setString(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(map(rows)) : Optional.empty();
            }
        }
    }

    public List<Computer> findAll(Connection connection) throws SQLException {
        List<Computer> result = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                SELECT_WITH_SECTION + " ORDER BY c.computer_id");
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) result.add(map(rows));
        }
        return result;
    }

    public boolean updateStatus(Connection connection, String id, ComputerStatus status) throws SQLException {
        String sql = "UPDATE computers SET computer_status = ? WHERE computer_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            statement.setString(2, id);
            return statement.executeUpdate() == 1;
        }
    }

    /** Deletion is rejected by the database while sessions reference this computer. */
    public boolean deleteById(Connection connection, String id) throws SQLException {
        String sql = "DELETE FROM computers WHERE computer_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            return statement.executeUpdate() == 1;
        }
    }

    private Computer map(ResultSet rows) throws SQLException {
        ComputerSection section = new ComputerSection(
                rows.getString("section_id"),
                rows.getString("section_name"),
                rows.getBigDecimal("section_price"));
        return new Computer(
                rows.getString("computer_id"),
                section,
                ComputerStatus.valueOf(rows.getString("computer_status")));
    }
}
