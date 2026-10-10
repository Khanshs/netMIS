package netcafe;

import netcafe.Service.NetCafe;
import netcafe.enums.*;
import netcafe.exception.*;
import netcafe.model.*;
import netcafe.model.customer.NormalCustomer;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Independent account registration and session checks; run main() explicitly. */
public final class AccountChecks {
    private static int checks;

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

    private static CustomerAccount account(String id, String username, Customer customer) {
        return new CustomerAccount(id, username, "test-hash", BigDecimal.ZERO, AccountStatus.ACTIVE, customer);
    }

    public static void main(String[] args) {
        NetCafe cafe = Main.sample();
        Customer owner = cafe.getCustomers().get(3);
        LocalDateTime start = LocalDateTime.of(2026, 10, 5, 13, 0);
        Computer pc = cafe.getComputers().get(5);
        CustomerAccount value = account(" A05 ", " Player ", owner);
        equal("A05", value.getAccountId());
        equal("Player", value.getUsername());
        equal(true, value.getCustomer() == owner);
        equal(4, cafe.getAccounts().size());
        equal(true, cafe.getSessions().get(3).getAccount() == cafe.getAccounts().get(0));
        fails(ValidationException.class, () -> account(" ", "player", owner));
        fails(ValidationException.class, () -> account("A05", " ", owner));
        fails(ValidationException.class, () -> account("A05", null, owner));
        fails(ValidationException.class, () -> account("A05", "player", null));
        fails(ValidationException.class, () -> new CustomerAccount("A05", "player", null, BigDecimal.ZERO, AccountStatus.ACTIVE, owner));
        fails(ValidationException.class, () -> new CustomerAccount("A05", "player", " ", BigDecimal.ZERO, AccountStatus.ACTIVE, owner));
        fails(ValidationException.class, () -> new CustomerAccount("A05", "player", "test-hash", null, AccountStatus.ACTIVE, owner));
        fails(ValidationException.class, () -> new CustomerAccount("A05", "player", "test-hash", BigDecimal.valueOf(-1), AccountStatus.ACTIVE, owner));
        fails(ValidationException.class, () -> new CustomerAccount("A05", "player", "test-hash", BigDecimal.ZERO, null, owner));
        fails(ValidationException.class, () -> value.setBalance(BigDecimal.valueOf(-1)));
        fails(ValidationException.class, () -> value.setBalance(null));
        equal(0, value.getBalance());
        value.setBalance(BigDecimal.valueOf(50000));
        equal(50000, value.getBalance());
        fails(ValidationException.class, () -> value.setPasswordHash(null));
        fails(ValidationException.class, () -> value.setPasswordHash(" "));
        equal("test-hash", value.getPasswordHash());
        value.setPasswordHash("updated-test-hash");
        equal("updated-test-hash", value.getPasswordHash());
        fails(ValidationException.class, () -> value.setStatus(null));
        equal(AccountStatus.ACTIVE, value.getStatus());

        fails(ValidationException.class, () -> cafe.addAccount(null));
        fails(EntityNotFoundException.class, () -> cafe.addAccount(account("A05", "player", new NormalCustomer("C99", "Unknown"))));
        fails(EntityNotFoundException.class, () -> cafe.addAccount(account("A05", "player", new NormalCustomer(owner.getId(), owner.getName()))));
        cafe.getCatalogService().addAccount(value);
        fails(DuplicateIdException.class, () -> cafe.addAccount(account(" A05 ", "other", owner)));
        fails(DuplicateUsernameException.class, () -> cafe.addAccount(account("A06", "Player", owner)));
        fails(DuplicateUsernameException.class, () -> cafe.addAccount(account("A06", " pLaYeR ", owner)));
        equal(5, cafe.getAccounts().size());
        fails(UnsupportedOperationException.class, () -> cafe.getAccounts().clear());

        fails(ValidationException.class, () -> cafe.openSession("S05", null, pc, start));
        fails(ValidationException.class, () -> new Session("S05", null, pc, start));
        // A separately constructed object with the same account ID is still unregistered.
        fails(EntityNotFoundException.class, () -> cafe.openSession("S05", account("A05", "Player", owner), pc, start));
        value.setStatus(AccountStatus.INACTIVE);
        fails(InactiveAccountException.class, () -> cafe.openSession("S05", value, pc, start));
        equal(4, cafe.getSessions().size());
        equal(ComputerStatus.AVAILABLE, pc.getComputerStatus());
        equal(50000, value.getBalance());
        value.setStatus(AccountStatus.ACTIVE);
        Session session = cafe.getSessionService().openSession("S05", value, pc, start);
        equal(true, session.getAccount() == value);
        equal(ComputerStatus.IN_USE, pc.getComputerStatus());
        fails(ActiveSessionExistsException.class, () -> cafe.openSession("S06", value, cafe.getComputers().get(0), start));
        equal(5, cafe.getSessions().size());
        equal(ComputerStatus.AVAILABLE, cafe.getComputers().get(0).getComputerStatus());
        cafe.endSession(session, start.plusHours(1));
        fails(InvalidTimeException.class, () -> cafe.openSession("S06", value, cafe.getComputers().get(0), start.plusMinutes(30)));
        equal(5, cafe.getSessions().size());
        equal(ComputerStatus.AVAILABLE, cafe.getComputers().get(0).getComputerStatus());
        Session next = cafe.openSession("S06", value, pc, start.plusHours(1));
        equal(true, next.getAccount() == value);

        // Restrictions are per account; the requirements do not limit accounts per owner.
        CustomerAccount second = account("A06", "second-player", owner);
        cafe.addAccount(second);
        Session parallel = cafe.openSession("S07", second, cafe.getComputers().get(0), start.plusHours(1));
        equal(true, parallel.getAccount().getCustomer() == next.getAccount().getCustomer());
        equal(0, second.getBalance());
        equal(50000, value.getBalance());
        cafe.endSession(next, start.plusHours(2));
        cafe.endSession(parallel, start.plusHours(2));
        Receipt receipt = cafe.issueReceipt("R03", next, cafe.getEmployees().get(0));
        equal("NORMAL", receipt.getCustomerTier());
        equal(20000, receipt.getTotalAmount());
        equal(50000, value.getBalance());
        System.out.println("AccountChecks OK: " + checks + " checks");
    }
}
