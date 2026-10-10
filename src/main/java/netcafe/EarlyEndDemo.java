package netcafe;

import netcafe.Service.NetCafe;
import netcafe.enums.*;
import netcafe.model.*;
import netcafe.model.customer.NormalCustomer;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

/** Started at 03:00; the customer ends the session and pays at 04:30. */
public final class EarlyEndDemo {
    public static NetCafe sample() {
        NetCafe cafe = new NetCafe();
        ComputerSection section = new ComputerSection("CS01", "Demo", new BigDecimal("20000"));
        Computer computer = new Computer("PC01", section, ComputerStatus.AVAILABLE);
        Customer customer = new NormalCustomer("C01", "Early departure");
        CustomerAccount account = new CustomerAccount("A01", "early-demo", "demo-hash-not-for-login",
                BigDecimal.ZERO, AccountStatus.ACTIVE, customer);
        Employee employee = new Employee("E01", "Cashier", EmployeeStatus.ACTIVE);
        cafe.addSection(section);
        cafe.addComputer(computer);
        cafe.addCustomer(customer);
        cafe.addAccount(account);
        cafe.addEmployee(employee);

        LocalDateTime start = LocalDateTime.of(2026, 10, 5, 3, 0);
        Session session = cafe.openSession("S01", account, computer, start);
        LocalDateTime actualEnd = start.plusMinutes(90);
        cafe.endSession(session, actualEnd);
        Receipt receipt = cafe.issueReceipt("R01", session, employee);
        cafe.pay("P01", receipt, actualEnd, PaymentMethod.CASH, receipt.getTotalAmount());
        return cafe;
    }

    public static void main(String[] args) {
        NetCafe cafe = sample();
        Session session = cafe.getSessions().get(0);
        Receipt receipt = cafe.getReceipts().get(0);
        System.out.println("Start=" + session.getTimeStart().toLocalTime()
                + " ActualEnd=" + session.getTimeEnd().toLocalTime());
        System.out.println("Minutes=" + Duration.between(session.getTimeStart(), session.getTimeEnd()).toMinutes());
        System.out.println("Total=" + receipt.getTotalAmount());
        System.out.println("Collected=" + cafe.collectedAmount());
    }
}
