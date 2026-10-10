package netcafe;

import netcafe.Service.NetCafe;
import netcafe.model.customer.*;
import netcafe.enums.*;
import netcafe.exception.*;
import netcafe.model.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class DiscountChecks {
    private static int checks;
    private static BigDecimal money(long value) { return BigDecimal.valueOf(value); }
    private static void equal(Object expected, Object actual) {
        checks++;
        if (!String.valueOf(expected).equals(String.valueOf(actual))) {
            throw new AssertionError("Expected " + expected + ", got " + actual);
        }
    }
    private static void fails(Class<? extends RuntimeException> type, Runnable action) {
        checks++;
        try { action.run(); }
        catch (RuntimeException failure) {
            if (type.isInstance(failure)) return;
            throw new AssertionError("Unexpected exception", failure);
        }
        throw new AssertionError("Expected " + type.getSimpleName());
    }
    private static IDiscount policy(BigDecimal computerRate, BigDecimal foodRate) {
        return new IDiscount() {
            public String getTierName() { return "CUSTOM"; }
            public BigDecimal getComputerDiscountRate() { return computerRate; }
            public BigDecimal getFoodDiscountRate() { return foodRate; }
        };
    }

    /** Test double for checking invalid rates and frozen receipt values. */
    private static final class TestCustomer extends Customer {
        private BigDecimal computerRate, foodRate;
        TestCustomer(String id, BigDecimal computerRate, BigDecimal foodRate) {
            super(id, "Test customer");
            setRates(computerRate, foodRate);
        }
        void setRates(BigDecimal computerRate, BigDecimal foodRate) {
            this.computerRate = computerRate;
            this.foodRate = foodRate;
        }
        public String getTierName() { return "TEST"; }
        public BigDecimal getComputerDiscountRate() { return computerRate; }
        public BigDecimal getFoodDiscountRate() { return foodRate; }
    }
    public static void main(String[] args) {
        NetCafe cafe = DiscountDemo.sample();
        Receipt normal = cafe.getReceipts().get(0), member = cafe.getReceipts().get(1), vip = cafe.getReceipts().get(2);
        equal("NORMAL", normal.getCustomerTier());
        equal(0, normal.getTotalDiscountAmount());
        equal(150000, normal.getTotalAmount());
        equal("MEMBER", member.getCustomerTier());
        equal(100000, member.getGrossComputerAmount());
        equal(50000, member.getGrossServiceAmount());
        equal("0.10", member.getComputerDiscountRate());
        equal("0.05", member.getServiceDiscountRate());
        equal(10000, member.getComputerDiscountAmount());
        equal(2500, member.getServiceDiscountAmount());
        equal(90000, member.getComputerAmount());
        equal(47500, member.getServiceAmount());
        equal(137500, member.calculateAmount());
        equal(15000, vip.getComputerDiscountAmount());
        equal(4000, vip.getServiceDiscountAmount());
        equal(131000, vip.getTotalAmount());
        equal(418500, cafe.collectedAmount());

        equal(true, normal.getSession().getAccount().getCustomer() instanceof NormalCustomer);
        equal(true, member.getSession().getAccount().getCustomer() instanceof MemberCustomer);
        equal(true, vip.getSession().getAccount().getCustomer() instanceof VipCustomer);
        CustomerAccount account = vip.getSession().getAccount();
        Customer customer = account.getCustomer();
        equal("VIP", customer.getTierName());
        equal("MEMBER", member.getCustomerTier());
        equal(137500, member.getTotalAmount());
        LocalDateTime start = LocalDateTime.of(2026, 10, 5, 11, 0);
        Session vipSession = cafe.openSession("S04", account, cafe.getComputers().get(0), start);
        cafe.endSession(vipSession, start.plusHours(1));
        Receipt vipReceipt = cafe.issueReceipt("R04", vipSession, cafe.getEmployees().get(0));
        equal("VIP", vipReceipt.getCustomerTier());
        equal(85000, vipReceipt.getTotalAmount());
        fails(PaymentAmountMismatchException.class, () -> cafe.pay("P04", vipReceipt, start.plusHours(1), PaymentMethod.CASH, money(100000)));
        equal(3, cafe.getPayments().size());
        cafe.pay("P04", vipReceipt, start.plusHours(1), PaymentMethod.CASH, money(85000));
        equal(503500, cafe.collectedAmount());

        Receipt rounded = new Receipt("ROUND", member.getSession(), cafe.getEmployees().get(0), money(10), money(5));
        equal(1, rounded.getComputerDiscountAmount());
        equal(1, rounded.getServiceDiscountAmount());
        equal(13, rounded.getTotalAmount());
        Receipt free = new Receipt("FREE", vipSession, cafe.getEmployees().get(0), money(10), money(5), policy(BigDecimal.ONE, BigDecimal.ONE));
        equal(0, free.getTotalAmount());
        Receipt zero = new Receipt("ZERO", vipSession, cafe.getEmployees().get(0), BigDecimal.ZERO, BigDecimal.ZERO);
        equal(0, zero.getTotalAmount());

        NetCafe invalid = Main.sample();
        TestCustomer testCustomer = new TestCustomer("C99", new BigDecimal("0.10"), new BigDecimal("0.05"));
        invalid.addCustomer(testCustomer);
        CustomerAccount testAccount = new CustomerAccount("A99", "testcustomer", "test-hash", money(0), AccountStatus.ACTIVE, testCustomer);
        invalid.addAccount(testAccount);
        Session session = invalid.openSession("S99", testAccount, invalid.getComputers().get(5), start.plusHours(3));
        invalid.endSession(session, start.plusHours(4));
        Employee employee = invalid.getEmployees().get(0);
        Receipt frozen = new Receipt("FROZEN", session, employee, money(50000), money(100000));
        testCustomer.setRates(new BigDecimal("0.15"), new BigDecimal("0.08"));
        equal(137500, frozen.getTotalAmount());
        equal("0.10", frozen.getComputerDiscountRate());
        equal("0.05", frozen.getServiceDiscountRate());
        testCustomer.setRates(new BigDecimal("1.01"), BigDecimal.ZERO);
        fails(ValidationException.class, () -> invalid.issueReceipt("R03", session, employee));
        testCustomer.setRates(BigDecimal.ZERO, new BigDecimal("-0.01"));
        fails(ValidationException.class, () -> invalid.issueReceipt("R03", session, employee));
        testCustomer.setRates(null, BigDecimal.ZERO);
        fails(ValidationException.class, () -> invalid.issueReceipt("R03", session, employee));
        equal(2, invalid.getReceipts().size());
        fails(ValidationException.class, () -> new Receipt("NULL", session, employee, money(10), money(5), null));
        testCustomer.setRates(new BigDecimal("0.15"), new BigDecimal("0.08"));
        equal(17000, invalid.issueReceipt("R03", session, employee).getTotalAmount());
        System.out.println("DiscountChecks OK: " + checks + " checks");
    }
}
