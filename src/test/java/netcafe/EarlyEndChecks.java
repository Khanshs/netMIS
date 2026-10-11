package netcafe;

import netcafe.Service.NetCafe;
import netcafe.enums.*;
import netcafe.exception.*;
import netcafe.model.*;
import netcafe.model.customer.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Customers end sessions when they leave; billing and payment use the actual end. */
public final class EarlyEndChecks {
    private static int checks;
    private static final LocalDateTime START = LocalDateTime.of(2026, 10, 5, 3, 0);
    private static final LocalDateTime TWO_HOURS_LATER = START.plusHours(2);

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
            throw new AssertionError("Expected " + type.getSimpleName(), failure);
        }
        throw new AssertionError("Expected " + type.getSimpleName());
    }

    private static final class Fixture {
        final NetCafe cafe = new NetCafe();
        final ComputerSection section = new ComputerSection("CS01", "Demo", BigDecimal.valueOf(20000));
        final Computer computer = new Computer("PC01", section, ComputerStatus.AVAILABLE);
        final Employee employee = new Employee("E01", "Cashier", EmployeeStatus.ACTIVE);
        final CustomerAccount account;

        Fixture(Customer customer) {
            account = new CustomerAccount("A01", "test", "test-hash", BigDecimal.ZERO, AccountStatus.ACTIVE, customer);
            cafe.addSection(section);
            cafe.addComputer(computer);
            cafe.addCustomer(customer);
            cafe.addAccount(account);
            cafe.addEmployee(employee);
        }

        Session open() {
            return cafe.openSession("S01", account, computer, START);
        }
    }

    public static void main(String[] args) {
        Fixture early = new Fixture(new NormalCustomer("C01", "Normal"));
        Session session = early.open();
        equal(null, session.getTimeEnd());
        equal(SessionStatus.ACTIVE, session.getSessionStatus());
        equal(ComputerStatus.IN_USE, early.computer.getComputerStatus());
        fails(SessionStateException.class, () -> early.cafe.issueReceipt("R01", session, early.employee));
        equal(0, early.cafe.getReceipts().size());
        Receipt premature = new Receipt("UNREGISTERED", session, early.employee, BigDecimal.ZERO, BigDecimal.ZERO);
        fails(SessionStateException.class, () -> new Payment("P01", premature, TWO_HOURS_LATER, PaymentMethod.CASH, BigDecimal.ZERO));
        // Estimating accrued cost does not complete the session.
        equal(40333, session.amountAt(TWO_HOURS_LATER.plusMinutes(1)));
        equal(null, session.getTimeEnd());
        equal(SessionStatus.ACTIVE, session.getSessionStatus());

        LocalDateTime actualEnd = START.plusMinutes(90);
        early.cafe.endSession(session, actualEnd);
        equal(actualEnd, session.getTimeEnd());
        equal(SessionStatus.COMPLETED, session.getSessionStatus());
        equal(ComputerStatus.AVAILABLE, early.computer.getComputerStatus());
        Receipt receipt = early.cafe.issueReceipt("R01", session, early.employee);
        equal(30000, receipt.getGrossComputerAmount());
        equal(30000, receipt.getTotalAmount());
        fails(InvalidTimeException.class, () -> early.cafe.pay("P01", receipt, actualEnd.minusMinutes(1), PaymentMethod.CASH, receipt.getTotalAmount()));
        equal(0, early.cafe.getPayments().size());
        equal(0, early.cafe.collectedAmount());
        Payment payment = early.cafe.pay("P01", receipt, actualEnd, PaymentMethod.CASH, receipt.getTotalAmount());
        equal(actualEnd, payment.getTime());
        equal(30000, early.cafe.collectedAmount());
        fails(SessionStateException.class, () -> early.cafe.endSession(session, TWO_HOURS_LATER));
        equal(actualEnd, session.getTimeEnd());
        Session next = early.cafe.openSession("S02", early.account, early.computer, actualEnd);
        equal(SessionStatus.ACTIVE, next.getSessionStatus());

        Fixture onTime = new Fixture(new NormalCustomer("C02", "Two hours"));
        Session onTimeSession = onTime.open();
        onTime.cafe.endSession(onTimeSession, TWO_HOURS_LATER);
        Receipt onTimeReceipt = onTime.cafe.issueReceipt("R01", onTimeSession, onTime.employee);
        equal(40000, onTimeReceipt.getTotalAmount());
        onTime.cafe.pay("P01", onTimeReceipt, TWO_HOURS_LATER.plusMinutes(1), PaymentMethod.CASH, onTimeReceipt.getTotalAmount());
        equal(40000, onTime.cafe.collectedAmount());

        Fixture late = new Fixture(new NormalCustomer("C03", "Longer session"));
        Session lateSession = late.open();
        LocalDateTime lateEnd = TWO_HOURS_LATER.plusMinutes(30);
        late.cafe.endSession(lateSession, lateEnd);
        Receipt lateReceipt = late.cafe.issueReceipt("R01", lateSession, late.employee);
        equal(50000, lateReceipt.getTotalAmount());
        fails(InvalidTimeException.class, () -> late.cafe.openSession("S02", late.account, late.computer, TWO_HOURS_LATER.plusMinutes(15)));
        equal(1, late.cafe.getSessions().size());
        equal(ComputerStatus.AVAILABLE, late.computer.getComputerStatus());
        late.cafe.pay("P01", lateReceipt, lateEnd, PaymentMethod.CASH, lateReceipt.getTotalAmount());
        equal(50000, late.cafe.collectedAmount());

        Fixture ordered = new Fixture(new NormalCustomer("C04", "Food order"));

        Session orderedSession = ordered.open();
        Food food = new Food("F01", "Combo", BigDecimal.valueOf(10000));
        ordered.cafe.addFood(food);
        LocalDateTime orderTime = START.plusMinutes(100);
        ordered.cafe.order("SV01", orderedSession, ordered.employee, orderTime, new Food[]{food}, new int[]{1});
        fails(InvalidTimeException.class, () -> ordered.cafe.endSession(orderedSession, actualEnd));
        equal(null, orderedSession.getTimeEnd());
        equal(SessionStatus.ACTIVE, orderedSession.getSessionStatus());
        equal(ComputerStatus.IN_USE, ordered.computer.getComputerStatus());
        ordered.cafe.endSession(orderedSession, orderTime);
        equal(43333, ordered.cafe.issueReceipt("R01", orderedSession, ordered.employee).getTotalAmount());

        Customer[] tiers = {
                new MemberCustomer("C05", "Member"),
                new VipCustomer("C06", "VIP")};

        long[] expected = {27000, 25500};
        for (int i = 0; i < tiers.length; i++) {
            Fixture discounted = new Fixture(tiers[i]);
            Session discountedSession = discounted.open();
            discounted.cafe.endSession(discountedSession, actualEnd);
            Receipt discountedReceipt = discounted.cafe.issueReceipt("R01", discountedSession, discounted.employee);
            equal(30000, discountedReceipt.getGrossComputerAmount());
            equal(expected[i], discountedReceipt.getTotalAmount());
            discounted.cafe.pay("P01", discountedReceipt, actualEnd, PaymentMethod.CASH, discountedReceipt.getTotalAmount());
            equal(expected[i], discounted.cafe.collectedAmount());
        }
        checkDirectEnd();
        System.out.println("EarlyEndChecks OK: " + checks + " checks");
    }

    private static void checkDirectEnd() {
        Fixture direct = new Fixture(new NormalCustomer("C07", "Direct end"));
        Session session = direct.open();
        Food food = new Food("F01", "Combo", BigDecimal.valueOf(10000));
        direct.cafe.addFood(food);
        LocalDateTime orderTime = START.plusMinutes(100);

        // A rejected multi-line order must not become part of the session's history.
        fails(InvalidOrderException.class, () -> direct.cafe.order("SV-BAD", session, direct.employee,
                orderTime.plusHours(1), new Food[]{food, food}, new int[]{1, 0}));
        equal(0, session.getFoodServices().size());
        equal(0, direct.cafe.getServices().size());
        FoodService order = direct.cafe.order("SV01", session, direct.employee,
                orderTime, new Food[]{food}, new int[]{1});
        equal(order, session.getFoodServices().get(0));
        fails(UnsupportedOperationException.class, () -> session.getFoodServices().clear());
        order.seal();
        equal(1, session.getFoodServices().size());

        fails(InvalidTimeException.class, () -> session.endSession(null));
        fails(InvalidTimeException.class, () -> session.endSession(START));
        fails(InvalidTimeException.class, () -> session.endSession(START.minusMinutes(1)));
        // Regression: the model itself must reject an end before a recorded order.
        fails(InvalidTimeException.class, () -> session.endSession(START.plusMinutes(90)));
        equal(null, session.getTimeEnd());
        equal(SessionStatus.ACTIVE, session.getSessionStatus());
        equal(ComputerStatus.IN_USE, direct.computer.getComputerStatus());
        fails(SessionStateException.class, () -> direct.cafe.issueReceipt("R01", session, direct.employee));
        equal(0, direct.cafe.getReceipts().size());

        session.endSession(orderTime);
        equal(orderTime, session.getTimeEnd());
        equal(SessionStatus.COMPLETED, session.getSessionStatus());
        equal(ComputerStatus.AVAILABLE, direct.computer.getComputerStatus());
        Receipt receipt = direct.cafe.issueReceipt("R01", session, direct.employee);
        equal(43333, receipt.getTotalAmount());
        direct.cafe.pay("P01", receipt, orderTime, PaymentMethod.CASH, receipt.getTotalAmount());
        equal(43333, direct.cafe.collectedAmount());

        FoodService lateOrder = new FoodService("SV-LATE", session, direct.employee, orderTime.plusMinutes(1));
        lateOrder.addItem(food, 1, food.getPrice());
        fails(SessionStateException.class, lateOrder::seal);
        equal(1, session.getFoodServices().size());
        fails(SessionStateException.class, () -> session.endSession(orderTime.plusHours(1)));

        Session next = direct.cafe.openSession("S02", direct.account, direct.computer, orderTime);
        // Neither a detached session nor a manual status change may release the new session's machine.
        fails(ComputerUnavailableException.class, () -> new Session("DETACHED", direct.account, direct.computer, orderTime));
        fails(ComputerUnavailableException.class, () -> direct.computer.setComputerStatus(ComputerStatus.AVAILABLE));
        fails(ComputerUnavailableException.class, () -> direct.computer.setComputerStatus(ComputerStatus.MAINTENANCE));
        fails(SessionStateException.class, () -> direct.cafe.endSession(session, orderTime.plusHours(1)));
        equal(SessionStatus.ACTIVE, next.getSessionStatus());
        equal(ComputerStatus.IN_USE, direct.computer.getComputerStatus());
        equal(orderTime, session.getTimeEnd());
        next.endSession(orderTime.plusHours(1));
        equal(ComputerStatus.AVAILABLE, direct.computer.getComputerStatus());
        direct.computer.setComputerStatus(ComputerStatus.MAINTENANCE);
        fails(ComputerUnavailableException.class, () -> new Session("MAINTENANCE", direct.account, direct.computer, orderTime.plusHours(2)));
        direct.computer.setComputerStatus(ComputerStatus.AVAILABLE);
        fails(ComputerUnavailableException.class, () -> direct.computer.setComputerStatus(ComputerStatus.IN_USE));
        equal(ComputerStatus.AVAILABLE, direct.computer.getComputerStatus());

        Fixture outOfOrder = new Fixture(new NormalCustomer("C08", "Unsorted orders"));
        Session unordered = outOfOrder.open();
        outOfOrder.cafe.addFood(food);
        outOfOrder.cafe.order("SV01", unordered, outOfOrder.employee,
                START.plusHours(2), new Food[]{food}, new int[]{1});
        outOfOrder.cafe.order("SV02", unordered, outOfOrder.employee,
                START.plusHours(1), new Food[]{food}, new int[]{1});
        fails(InvalidTimeException.class, () -> unordered.endSession(START.plusMinutes(90)));
        equal(null, unordered.getTimeEnd());
        equal(ComputerStatus.IN_USE, outOfOrder.computer.getComputerStatus());
        unordered.endSession(START.plusHours(2));
        equal(ComputerStatus.AVAILABLE, outOfOrder.computer.getComputerStatus());
    }
}
