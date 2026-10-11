package netcafe;

import netcafe.Service.NetCafe;
import netcafe.enums.*;
import netcafe.exception.*;
import netcafe.model.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Service and direct completion must preserve the same session invariants. */
public final class SessionEndChecks {
    private static int checks;

    private static LocalDateTime time(int hour, int minute) {
        return LocalDateTime.of(2026, 10, 5, hour, minute);
    }

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

    private static void end(NetCafe cafe, Session session, LocalDateTime time, boolean direct) {
        if (direct) session.endSession(time);
        else cafe.endSession(session, time);
    }

    private static void completion(boolean direct) {
        NetCafe cafe = Main.sample();
        Session session = cafe.getSessions().get(3);
        equal(1, session.getFoodServices().size());
        equal(true, session.getFoodServices().get(0) == cafe.getServices().get(2));
        LocalDateTime[] invalid = {null, time(11, 0), time(10, 59), time(11, 1), time(11, 14).plusSeconds(59)};
        for (LocalDateTime proposed : invalid) {
            fails(InvalidTimeException.class, () -> end(cafe, session, proposed, direct));
            equal(null, session.getTimeEnd());
            equal(SessionStatus.ACTIVE, session.getSessionStatus());
            equal(ComputerStatus.IN_USE, session.getComputer().getComputerStatus());
            equal(1, session.getFoodServices().size());
            equal(3, cafe.getServices().size());
        }

        end(cafe, session, time(12, 0), direct);
        equal(time(12, 0), session.getTimeEnd());
        equal(SessionStatus.COMPLETED, session.getSessionStatus());
        equal(ComputerStatus.AVAILABLE, session.getComputer().getComputerStatus());
        // Repeating through either API must fail without rewriting the actual end.
        fails(SessionStateException.class, () -> session.endSession(time(12, 1)));
        fails(SessionStateException.class, () -> cafe.endSession(session, time(12, 1)));
        equal(time(12, 0), session.getTimeEnd());
        equal(ComputerStatus.AVAILABLE, session.getComputer().getComputerStatus());
        FoodService lateOrder = new FoodService("SV04", session, cafe.getEmployees().get(0), time(11, 30));
        lateOrder.addItem(cafe.getFoods().get(0), 1, BigDecimal.valueOf(13000));
        fails(SessionStateException.class, () -> session.recordFoodService(lateOrder));
        equal(1, session.getFoodServices().size());

        Receipt receipt = cafe.issueReceipt("R03", session, cafe.getEmployees().get(0));
        equal(12000, receipt.getGrossComputerAmount());
        equal(20000, receipt.getGrossServiceAmount());
        equal(32000, receipt.getTotalAmount());
        cafe.pay("P02", receipt, time(12, 0), PaymentMethod.CASH, receipt.getTotalAmount());
        equal(81000, cafe.collectedAmount());
        Session next = cafe.openSession("S05", session.getAccount(), session.getComputer(), time(12, 0));
        equal(SessionStatus.ACTIVE, next.getSessionStatus());
        equal(ComputerStatus.IN_USE, next.getComputer().getComputerStatus());

        // Ending exactly at the last order timestamp is valid through both APIs.
        NetCafe boundaryCafe = Main.sample();
        Session boundary = boundaryCafe.getSessions().get(3);
        end(boundaryCafe, boundary, time(11, 15), direct);
        equal(time(11, 15), boundary.getTimeEnd());
        equal(ComputerStatus.AVAILABLE, boundary.getComputer().getComputerStatus());
        equal(23000, boundaryCafe.issueReceipt("R03", boundary, boundaryCafe.getEmployees().get(0)).getTotalAmount());
    }

    private static void orders() {
        NetCafe cafe = Main.sample();
        Session session = cafe.getSessions().get(3);
        Employee employee = cafe.getEmployees().get(0);
        Food food = cafe.getFoods().get(0);
        List<FoodService> view = session.getFoodServices();
        fails(UnsupportedOperationException.class, () -> view.clear());
        fails(UnsupportedOperationException.class, () -> view.remove(0));
        fails(UnsupportedOperationException.class, () -> view.add(cafe.getServices().get(0)));
        fails(ValidationException.class, () -> session.recordFoodService(null));
        fails(InvalidOrderException.class, () -> session.recordFoodService(cafe.getServices().get(0)));

        FoodService beforeStart = new FoodService("SV04", session, employee, time(10, 59));
        beforeStart.addItem(food, 1, BigDecimal.valueOf(13000));
        fails(InvalidTimeException.class, () -> session.recordFoodService(beforeStart));
        FoodService empty = new FoodService("SV04", session, employee, time(11, 30));
        fails(InvalidOrderException.class, () -> session.recordFoodService(empty));
        fails(DuplicateIdException.class, () -> session.recordFoodService(cafe.getServices().get(2)));
        FoodService duplicate = new FoodService("SV03", session, employee, time(11, 30));
        duplicate.addItem(food, 1, BigDecimal.valueOf(13000));
        fails(DuplicateIdException.class, () -> session.recordFoodService(duplicate));
        equal(1, view.size());
        equal(3, cafe.getServices().size());

        // A failure midway through building an order must not attach a partial order.
        Food unknown = new Food("F99", "Unknown", BigDecimal.valueOf(13000));
        fails(EntityNotFoundException.class, () -> cafe.order("SV04", session, employee, time(11, 30),
                new Food[]{food, unknown}, new int[]{1, 1},
                new BigDecimal[]{BigDecimal.valueOf(13000), BigDecimal.valueOf(13000)}));
        fails(InvalidOrderException.class, () -> cafe.order("SV04", session, employee, time(11, 30),
                new Food[]{food, food}, new int[]{Integer.MAX_VALUE, 1}));
        equal(1, view.size());
        equal(3, cafe.getServices().size());

        FoodService recorded = cafe.order("SV04", session, employee, time(11, 30), new Food[]{food}, new int[]{1});
        equal(2, view.size());
        equal(4, cafe.getServices().size());
        equal(true, view.get(1) == recorded);
        equal(true, cafe.getServices().get(3) == recorded);
        fails(InvalidOrderException.class, () -> recorded.addItem(food, 1, BigDecimal.valueOf(13000)));
        fails(InvalidTimeException.class, () -> session.endSession(time(11, 20)));
        equal(null, session.getTimeEnd());
        equal(SessionStatus.ACTIVE, session.getSessionStatus());
        equal(ComputerStatus.IN_USE, session.getComputer().getComputerStatus());
        session.endSession(time(11, 30));
        equal(time(11, 30), session.getTimeEnd());
        equal(ComputerStatus.AVAILABLE, session.getComputer().getComputerStatus());

        // Registration remains a responsibility of the cafe's service.
        NetCafe otherCafe = Main.sample();
        Session foreign = otherCafe.getSessions().get(3);
        fails(EntityNotFoundException.class, () -> cafe.endSession(foreign, time(12, 0)));
        equal(null, foreign.getTimeEnd());
        equal(SessionStatus.ACTIVE, foreign.getSessionStatus());
        equal(ComputerStatus.IN_USE, foreign.getComputer().getComputerStatus());
    }

    public static void main(String[] args) {
        completion(false);
        completion(true);
        orders();
        directOrderBilling();
        System.out.println("SessionEndChecks OK: " + checks + " checks");
    }

    private static void directOrderBilling() {
        NetCafe cafe = Main.sample();
        Session session = cafe.getSessions().get(3);
        FoodService extra = new FoodService("EXTRA", session, cafe.getEmployees().get(0), time(11, 30));
        extra.addItem(cafe.getFoods().get(0), 1, BigDecimal.valueOf(13000));
        session.recordFoodService(extra);
        equal(2, session.getFoodServices().size());
        fails(InvalidOrderException.class, () -> extra.addItem(cafe.getFoods().get(0), 1, BigDecimal.valueOf(13000)));
        session.endSession(time(12, 0));
        Receipt receipt = cafe.issueReceipt("R03", session, cafe.getEmployees().get(0));
        equal(33000, receipt.getGrossServiceAmount());
        equal(45000, receipt.getTotalAmount());
        cafe.pay("P02", receipt, time(12, 0), PaymentMethod.CASH, receipt.getTotalAmount());
        equal(94000, cafe.collectedAmount());
    }
}
