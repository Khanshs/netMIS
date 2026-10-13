package netcafe.dao;

import netcafe.enums.AccountStatus;
import netcafe.model.CustomerAccount;
import netcafe.model.customer.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Stores login data separately from the customer and their discount tier. */
public final class CustomerAccountDAO {
    private static final String SELECT_WITH_CUSTOMER =
            "SELECT a.account_id, a.username, a.password_hash, a.balance, a.account_status, "
            + "c.customer_id, c.customer_name, c.tier "
            + "FROM customer_accounts a JOIN customers c ON c.customer_id = a.customer_id";

    public void insert(Connection connection, CustomerAccount account) throws SQLException {
        String sql = "INSERT INTO customer_accounts "
                + "(account_id, customer_id, username, password_hash, balance, account_status) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, account.getAccountId());
            statement.setString(2, account.getCustomer().getCustomerId());
            statement.setString(3, account.getUsername());
            statement.setString(4, account.getPasswordHash());
            statement.setBigDecimal(5, account.getBalance());
            statement.setString(6, account.getStatus().name());
            statement.executeUpdate();
        }
    }

    public Optional<CustomerAccount> findById(Connection connection, String id) throws SQLException {
        return findOne(connection, SELECT_WITH_CUSTOMER + " WHERE a.account_id = ?", id);
    }

    public Optional<CustomerAccount> findByUsername(Connection connection, String username) throws SQLException {
        return findOne(connection, SELECT_WITH_CUSTOMER + " WHERE a.username = ?", username);
    }

    public List<CustomerAccount> findAll(Connection connection) throws SQLException {
        List<CustomerAccount> result = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                SELECT_WITH_CUSTOMER + " ORDER BY a.account_id");
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) result.add(map(rows));
        }
        return result;
    }

    /** Username and owner are fixed in the model; update its mutable fields. */
    public boolean update(Connection connection, CustomerAccount account) throws SQLException {
        String sql = "UPDATE customer_accounts SET password_hash = ?, balance = ?, "
                + "account_status = ? WHERE account_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, account.getPasswordHash());
            statement.setBigDecimal(2, account.getBalance());
            statement.setString(3, account.getStatus().name());
            statement.setString(4, account.getAccountId());
            return statement.executeUpdate() == 1;
        }
    }

    /** MySQL will reject deletion once sessions refer to the account. */
    public boolean deleteById(Connection connection, String id) throws SQLException {
        String sql = "DELETE FROM customer_accounts WHERE account_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            return statement.executeUpdate() == 1;
        }
    }

    private Optional<CustomerAccount> findOne(Connection connection, String sql, String value)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, value);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(map(rows)) : Optional.empty();
            }
        }
    }

    private CustomerAccount map(ResultSet rows) throws SQLException {
        Customer customer = CustomerDAO.fromValues(
                rows.getString("customer_id"), rows.getString("customer_name"),
                rows.getString("tier"));
        return new CustomerAccount(
                rows.getString("account_id"),
                rows.getString("username"),
                rows.getString("password_hash"),
                rows.getBigDecimal("balance"),
                AccountStatus.valueOf(rows.getString("account_status")),
                customer);
    }
}
