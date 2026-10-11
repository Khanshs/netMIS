package netcafe.Checks;
import netcafe.Main;
import netcafe.Service.FoodService;
import netcafe.Service.NetCafe;

import netcafe.enums.*;
import netcafe.exception.*;
import netcafe.model.*;
import netcafe.model.NetSpecs.Employee;
import netcafe.model.NetSpecs.Food;
import netcafe.model.customer.NormalCustomer;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Independent checks: run main(), no JUnit dependency. */
public final class NetCafeChecks {
    private static int checks;
    private static BigDecimal money(long amount) { return BigDecimal.valueOf(amount); }
    private static LocalDateTime time(int hour, int minute) { return LocalDateTime.of(2026, 10, 5, hour, minute); }

    private static void equal(Object expected, Object actual) {
        checks++;
        if (!String.valueOf(expected).equals(String.valueOf(actual))) {
            throw new AssertionError("Expected " + expected + ", got " + actual);
        }
    }

    private static void fails(Class<? extends RuntimeException> type, Runnable action) {
        checks++;
        try {
            action.run();
        } catch (RuntimeException failure) {
            if (type.isInstance(failure)) return;
            throw new AssertionError("Expected " + type.getSimpleName() + ", got " + failure.getClass().getSimpleName(), failure);
        }
        throw new AssertionError("Expected " + type.getSimpleName());
    }

    public static void main(String[] args) {
        NetCafe cafe = Main.sample();
        Session active = cafe.getSessions().get(3);
        Employee employee = cafe.getEmployees().get(0);
        Food food = cafe.getFoods().get(0);
        Receipt paid = cafe.getReceipts().get(0), unpaid = cafe.getReceipts().get(1);
        equal(4, cafe.getSessions().size());
        equal(3, cafe.getServices().size());
        equal(49000, paid.getTotalAmount());
        equal(60000, unpaid.getTotalAmount());
        equal(49000, cafe.collectedAmount());
        equal(ComputerStatus.IN_USE, active.getComputer().getComputerStatus());
        equal(10000, cafe.getServices().get(0).getDetails().get(1).getPriceAtOrder());

        fails(ValidationException.class, () -> new NormalCustomer(" ", "Name"));
        fails(ValidationException.class, () -> new Food("F99", "Bad", money(-1)));
        fails(ValidationException.class, () -> cafe.addCustomer(null));
        fails(DuplicateIdException.class, () -> cafe.addCustomer(new NormalCustomer(" C01 ", "Other")));
        CustomerAccount unknown = new CustomerAccount("A99", "unknown", "test-hash", money(0), AccountStatus.ACTIVE, cafe.getCustomers().get(3));
        fails(EntityNotFoundException.class, () -> cafe.openSession("S05", unknown, cafe.getComputers().get(5), time(12, 0)));
        fails(ComputerUnavailableException.class, () -> cafe.openSession("S05", cafe.getAccounts().get(3), cafe.getComputers().get(4), time(12, 0)));
        fails(ComputerUnavailableException.class, () -> cafe.openSession("S05", cafe.getAccounts().get(3), active.getComputer(), time(12, 0)));
        fails(ActiveSessionExistsException.class, () -> cafe.openSession("S05", active.getAccount(), cafe.getComputers().get(5), time(12, 0)));
        fails(InvalidTimeException.class, () -> cafe.openSession("S05", cafe.getAccounts().get(3), cafe.getComputers().get(0), time(9, 0)));
        fails(InvalidTimeException.class, () -> cafe.endSession(active, time(11, 10)));
        equal(SessionStatus.ACTIVE, active.getSessionStatus());
        equal(ComputerStatus.IN_USE, active.getComputer().getComputerStatus());
        fails(SessionStateException.class, () -> cafe.endSession(cafe.getSessions().get(0), time(12, 0)));
        fails(SessionStateException.class, () -> cafe.issueReceipt("R03", active, employee));
        fails(ReceiptAlreadyExistsException.class, () -> cafe.issueReceipt("R03", cafe.getSessions().get(0), employee));
        fails(InactiveEmployeeException.class, () -> cafe.order("SV04", active, cafe.getEmployees().get(2), time(11, 30), new Food[]{food}, new int[]{1}));
        fails(InvalidOrderException.class, () -> cafe.order("SV04", active, employee, time(11, 30), new Food[]{food}, new int[]{0}));
        fails(InvalidOrderException.class, () -> cafe.order("SV04", active, employee, time(11, 30), new Food[]{food}, new int[]{1, 2}));
        fails(InvalidTimeException.class, () -> cafe.order("SV04", active, employee, time(10, 0), new Food[]{food}, new int[]{1}));
        fails(PaymentAlreadyExistsException.class, () -> cafe.pay("P02", paid, time(12, 0), PaymentMethod.CARD, money(49000)));
        fails(PaymentAmountMismatchException.class, () -> cafe.pay("P02", unpaid, time(12, 0), PaymentMethod.CASH, money(59999)));
        fails(PaymentAmountMismatchException.class, () -> cafe.pay("P02", unpaid, time(12, 0), PaymentMethod.CASH, money(60001)));
        fails(InvalidTimeException.class, () -> cafe.pay("P02", unpaid, time(10, 0), PaymentMethod.CASH, money(60000)));
        equal(1, cafe.getPayments().size());
        equal(3, cafe.getServices().size());
        FoodService merged = cafe.order("SV04", active, employee, time(11, 30), new Food[]{food, food}, new int[]{1, 2});
        equal(1, merged.getDetails().size());
        equal(3, merged.getDetails().get(0).getOrderQuantity());
        equal(39000, merged.getTotal());
        food.setPrice(money(20000));
        equal(39000, merged.getTotal());
        fails(InvalidOrderException.class, () -> merged.addItem(food, 1, money(13000)));
        fails(InvalidOrderException.class, () -> cafe.order("SV05", active, employee, time(11, 40), new Food[]{food, food}, new int[]{Integer.MAX_VALUE, 1}));
        equal(4, cafe.getServices().size());
        cafe.getSessionService().endSession(active, time(12, 0));
        equal(ComputerStatus.AVAILABLE, active.getComputer().getComputerStatus());
        Receipt finalReceipt = cafe.getReceiptService().issueReceipt("R03", active, employee);
        equal(71000, finalReceipt.getTotalAmount());
        cafe.getPaymentService().pay("P02", finalReceipt, time(12, 0), PaymentMethod.BANK_TRANSFER, finalReceipt.getTotalAmount());
        equal(120000, cafe.collectedAmount());
        Session rounding = new Session("S99", cafe.getAccounts().get(3), cafe.getComputers().get(0), time(13, 0));
        rounding.endSession(time(13, 0).plusSeconds(1));
        equal(3, rounding.calculateAmount());
        System.out.println("NetCafeChecks OK: " + checks + " checks");
    }
}
