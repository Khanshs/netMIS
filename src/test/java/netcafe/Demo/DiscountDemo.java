package netcafe.Demo;

import netcafe.Service.NetCafe;
import netcafe.model.NetSpecs.*;
import netcafe.model.customer.*;
import netcafe.enums.*;
import netcafe.model.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Compare the three tiers using the same gross computer/food amounts. */
public final class DiscountDemo {
    public static NetCafe sample() {
        NetCafe cafe = new NetCafe();
        ComputerSection section = new ComputerSection("CS01", "Demo", new BigDecimal("100000"));
        Computer computer = new Computer("PC01", section, ComputerStatus.AVAILABLE);
        Employee employee = new Employee("E01", "Cashier", EmployeeStatus.ACTIVE);
        Food food = new Food("F01", "Combo", new BigDecimal("50000"));
        cafe.addSection(section);
        cafe.addComputer(computer);
        cafe.addEmployee(employee);
        cafe.addFood(food);
        Customer[] customers = {
            new NormalCustomer("C01", "Normal customer"),
            new MemberCustomer("C02", "Member customer"),
            new VipCustomer("C03", "VIP customer")
        };
        for (int i = 0; i < customers.length; i++) {
            String suffix = String.valueOf(i + 1);
            Customer customer = customers[i];
            cafe.addCustomer(customer);
            CustomerAccount account = new CustomerAccount("A0" + suffix, "demo" + suffix,
                    "demo-hash-not-for-login", BigDecimal.ZERO, AccountStatus.ACTIVE, customer);
            cafe.addAccount(account);
            LocalDateTime start = LocalDateTime.of(2026, 10, 5, 8 + i, 0);
            Session session = cafe.openSession("S0" + suffix, account, computer, start);
            cafe.getFoodOrderService().order("SV0" + suffix, session, employee,
                    start.plusMinutes(10), new Food[]{food}, new int[]{1});
            cafe.endSession(session, start.plusHours(1));
            Receipt receipt = cafe.getReceiptService().issueReceipt("R0" + suffix, session, employee);
            cafe.getPaymentService().pay("P0" + suffix, receipt, start.plusHours(1),
                    PaymentMethod.CASH, receipt.getTotalAmount());
        }
        return cafe;
    }

    public static void main(String[] args) {
        NetCafe cafe = sample();
        for (Receipt receipt : cafe.getReceipts()) {
            System.out.println(receipt.getCustomerTier()
                    + " grossComputer=" + receipt.getGrossComputerAmount()
                    + " grossFood=" + receipt.getGrossServiceAmount()
                    + " computerDiscount=" + receipt.getComputerDiscountAmount()
                    + " foodDiscount=" + receipt.getServiceDiscountAmount()
                    + " total=" + receipt.getTotalAmount());
        }
        System.out.println("Collected=" + cafe.collectedAmount());
    }
}
