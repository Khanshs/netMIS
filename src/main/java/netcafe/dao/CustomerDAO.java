package netcafe.dao;

import netcafe.model.customer.Customer;
import netcafe.model.customer.MemberCustomer;
import netcafe.model.customer.NormalCustomer;
import netcafe.model.customer.VipCustomer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Stores the customer tier used to restore the correct discount subclass. */
public final class CustomerDAO {
    public void insert(Connection connection, Customer customer) throws SQLException {
        String sql = "INSERT INTO customers(customer_id, customer_name, tier) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, customer.getCustomerId());
            statement.setString(2, customer.getName());
            statement.setString(3, customer.getTierName());
            statement.executeUpdate();
        }
    }

    public Optional<Customer> findById(Connection connection, String id) throws SQLException {
        String sql = "SELECT customer_id, customer_name, tier FROM customers WHERE customer_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(map(rows)) : Optional.empty();
            }
        }
    }

    public List<Customer> findAll(Connection connection) throws SQLException {
        String sql = "SELECT customer_id, customer_name, tier FROM customers ORDER BY customer_id";
        List<Customer> result = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) result.add(map(rows));
        }
        return result;
    }

    /** The model is immutable, so pass a replacement customer with the same ID. */
    public boolean update(Connection connection, Customer customer) throws SQLException {
        String sql = "UPDATE customers SET customer_name = ?, tier = ? WHERE customer_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, customer.getName());
            statement.setString(2, customer.getTierName());
            statement.setString(3, customer.getCustomerId());
            return statement.executeUpdate() == 1;
        }
    }

    /** MySQL rejects deletion while an account refers to this customer. */
    public boolean deleteById(Connection connection, String id) throws SQLException {
        String sql = "DELETE FROM customers WHERE customer_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            return statement.executeUpdate() == 1;
        }
    }

    private Customer map(ResultSet rows) throws SQLException {
        return fromValues(rows.getString("customer_id"),
                rows.getString("customer_name"), rows.getString("tier"));
    }

    static Customer fromValues(String id, String name, String tier) throws SQLException {
        switch (tier) {
            case "NORMAL": return new NormalCustomer(id, name);
            case "MEMBER": return new MemberCustomer(id, name);
            case "VIP": return new VipCustomer(id, name);
            default: throw new SQLException("Unknown customer tier: " + tier);
        }
    }
}
