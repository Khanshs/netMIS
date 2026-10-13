package netcafe.dao;

import netcafe.model.Payment;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/** Stores successful payments; the receipt_id UNIQUE constraint prevents duplicates. */
public final class PaymentDAO {
    public void insert(Connection connection, Payment payment) throws SQLException {
        String sql = "INSERT INTO payments(payment_id, receipt_id, time_paid, payment_method, amount) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, payment.getPaymentId());
            statement.setString(2, payment.getReceipt().getReceiptId());
            statement.setTimestamp(3, Timestamp.valueOf(payment.getTime()));
            statement.setString(4, payment.getPaymentMethod().name());
            statement.setBigDecimal(5, payment.getAmount());
            statement.executeUpdate();
        }
    }

    public boolean existsByReceipt(Connection connection, String receiptId) throws SQLException {
        String sql = "SELECT 1 FROM payments WHERE receipt_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, receiptId);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next();
            }
        }
    }

    public BigDecimal collectedAmount(Connection connection) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) AS total FROM payments";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            rows.next();
            return rows.getBigDecimal("total");
        }
    }
}
