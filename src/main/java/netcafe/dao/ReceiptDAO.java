package netcafe.dao;

import netcafe.model.Receipt;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Persists the amounts frozen on a receipt and reads simple billing summaries. */
public final class ReceiptDAO {
    private static final String SUMMARY_SELECT =
            "SELECT r.receipt_id, r.session_id, r.total_amount, "
            + "p.payment_id FROM receipts r "
            + "LEFT JOIN payments p ON p.receipt_id = r.receipt_id";

    public void insert(Connection connection, Receipt receipt) throws SQLException {
        String sql = "INSERT INTO receipts (receipt_id, session_id, employee_id, customer_tier, "
                + "gross_computer_amount, gross_service_amount, computer_discount_rate, "
                + "service_discount_rate, computer_discount_amount, service_discount_amount, "
                + "computer_amount, service_amount, total_amount) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, receipt.getReceiptId());
            statement.setString(2, receipt.getSession().getSessionId());
            statement.setString(3, receipt.getEmployee().getEmployeeId());
            statement.setString(4, receipt.getCustomerTier());
            statement.setBigDecimal(5, receipt.getGrossComputerAmount());
            statement.setBigDecimal(6, receipt.getGrossServiceAmount());
            statement.setBigDecimal(7, receipt.getComputerDiscountRate());
            statement.setBigDecimal(8, receipt.getServiceDiscountRate());
            statement.setBigDecimal(9, receipt.getComputerDiscountAmount());
            statement.setBigDecimal(10, receipt.getServiceDiscountAmount());
            statement.setBigDecimal(11, receipt.getComputerAmount());
            statement.setBigDecimal(12, receipt.getServiceAmount());
            statement.setBigDecimal(13, receipt.getTotalAmount());
            statement.executeUpdate();
        }
    }

    public Optional<Summary> findById(Connection connection, String id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                SUMMARY_SELECT + " WHERE r.receipt_id = ?")) {
            statement.setString(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(map(rows)) : Optional.empty();
            }
        }
    }

    public List<Summary> findUnpaid(Connection connection) throws SQLException {
        List<Summary> result = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                SUMMARY_SELECT + " WHERE p.payment_id IS NULL ORDER BY r.receipt_id");
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) result.add(map(rows));
        }
        return result;
    }

    private Summary map(ResultSet rows) throws SQLException {
        return new Summary(
                rows.getString("receipt_id"),
                rows.getString("session_id"),
                rows.getBigDecimal("total_amount"),
                rows.getString("payment_id") != null);
    }

    /** A read projection avoids replaying business constructors for an old receipt. */
    public static final class Summary {
        private final String receiptId;
        private final String sessionId;
        private final BigDecimal totalAmount;
        private final boolean paid;

        private Summary(String receiptId, String sessionId, BigDecimal totalAmount, boolean paid) {
            this.receiptId = receiptId;
            this.sessionId = sessionId;
            this.totalAmount = totalAmount;
            this.paid = paid;
        }

        public String getReceiptId() { return receiptId; }
        public String getSessionId() { return sessionId; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public boolean isPaid() { return paid; }
    }
}
