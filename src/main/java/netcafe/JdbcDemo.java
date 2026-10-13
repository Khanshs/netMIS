package netcafe;

import netcafe.Service.NetCafe;
import netcafe.dao.ComputerDAO;
import netcafe.dao.ComputerSectionDAO;
import netcafe.dao.CustomerAccountDAO;
import netcafe.dao.CustomerDAO;
import netcafe.dao.PaymentDAO;
import netcafe.dao.ReceiptDAO;
import netcafe.database.DBConnection;
import netcafe.enums.ComputerStatus;
import netcafe.enums.PaymentMethod;
import netcafe.model.NetSpecs.Computer;
import netcafe.model.NetSpecs.ComputerSection;
import netcafe.model.CustomerAccount;
import netcafe.model.Payment;
import netcafe.model.Receipt;
import netcafe.model.customer.Customer;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

/** Separate JDBC walkthrough; the existing in-memory Main remains unchanged. */
public final class JdbcDemo {
    private JdbcDemo() { }

    public static void main(String[] args) throws SQLException {
        ComputerSectionDAO sections = new ComputerSectionDAO();
        ComputerDAO computers = new ComputerDAO();
        ReceiptDAO receipts = new ReceiptDAO();
        PaymentDAO payments = new PaymentDAO();

        try (Connection connection = DBConnection.getConnection()) {
            if (args.length == 1 && "--add-computer".equals(args[0])) {
                addDemoComputer(connection, sections, computers);
            } else if (args.length == 1 && "--write-billing".equals(args[0])) {
                writeBillingSample(connection, receipts, payments);
            } else if (args.length == 1 && "--customers".equals(args[0])) {
                showCustomers(connection);
                return;
            } else if (args.length != 0) {
                throw new IllegalArgumentException(
                        "Use no argument, --add-computer, --write-billing, or --customers");
            }

            System.out.println("Sections=" + sections.findAll(connection).size());
            for (Computer computer : computers.findAll(connection)) {
                System.out.println(computer.getComputerId() + " "
                        + computer.getSection().getSectionName() + " "
                        + computer.getComputerStatus());
            }
            ReceiptDAO.Summary first = receipts.findById(connection, "R01")
                    .orElseThrow(() -> new IllegalStateException("Run examples/billing_demo.sql first"));
            System.out.println(first.getReceiptId() + " total=" + first.getTotalAmount()
                    + " paid=" + first.isPaid());
            for (ReceiptDAO.Summary unpaid : receipts.findUnpaid(connection)) {
                System.out.println("Unpaid: " + unpaid.getReceiptId()
                        + " total=" + unpaid.getTotalAmount());
            }
            System.out.println("Collected=" + payments.collectedAmount(connection));
        }
    }

    private static void showCustomers(Connection connection) throws SQLException {
        CustomerDAO customers = new CustomerDAO();
        CustomerAccountDAO accounts = new CustomerAccountDAO();
        for (Customer customer : customers.findAll(connection)) {
            System.out.println(customer.getCustomerId() + " " + customer.getName()
                    + " tier=" + customer.getTierName()
                    + " computerDiscount=" + customer.getComputerDiscountRate());
        }
        for (CustomerAccount account : accounts.findAll(connection)) {
            System.out.println(account.getAccountId() + " username=" + account.getUsername()
                    + " customer=" + account.getCustomer().getCustomerId()
                    + " balance=" + account.getBalance()
                    + " status=" + account.getStatus());
        }
    }

    /** Insert section + computer atomically so an error cannot leave half the example. */
    private static void addDemoComputer(Connection connection, ComputerSectionDAO sections,
                                        ComputerDAO computers) throws SQLException {
        boolean oldAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            ComputerSection section = sections.findById(connection, "CS03")
                    .orElseGet(() -> new ComputerSection("CS03", "Khu demo", BigDecimal.valueOf(15000)));
            if (!sections.findById(connection, section.getId()).isPresent()) {
                sections.insert(connection, section);
            }
            if (!computers.findById(connection, "PC07").isPresent()) {
                computers.insert(connection, new Computer("PC07", section, ComputerStatus.AVAILABLE));
            }
            connection.commit();
            System.out.println("Demo computer PC07 is available in SQL");
        } catch (SQLException | RuntimeException failure) {
            connection.rollback();
            throw failure;
        } finally {
            connection.setAutoCommit(oldAutoCommit);
        }
    }

    /** Import two additional records from the matching in-memory fixture into the demo DB. */
    private static void writeBillingSample(Connection connection, ReceiptDAO receipts,
                                           PaymentDAO payments) throws SQLException {
        NetCafe cafe = Main.sample();
        Receipt unpaid = cafe.getReceipts().get(1); // R02 in examples/billing_demo.sql
        Receipt newReceipt = cafe.getReceiptService().issueReceipt(
                "R03", cafe.getSessions().get(2), cafe.getEmployees().get(0));
        Payment newPayment = new Payment(
                "P02", unpaid, LocalDateTime.of(2026, 10, 5, 10, 35),
                PaymentMethod.CASH, unpaid.getTotalAmount());

        boolean oldAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            if (!receipts.findById(connection, newReceipt.getReceiptId()).isPresent()) {
                receipts.insert(connection, newReceipt);
            }
            if (!payments.existsByReceipt(connection, unpaid.getReceiptId())) {
                payments.insert(connection, newPayment);
            }
            connection.commit();
            System.out.println("Demo billing saved: R03 and P02");
        } catch (SQLException | RuntimeException failure) {
            connection.rollback();
            throw failure;
        } finally {
            connection.setAutoCommit(oldAutoCommit);
        }
    }
}
