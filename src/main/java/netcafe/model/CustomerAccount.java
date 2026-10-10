package netcafe.model;

import netcafe.Identifiable;
import netcafe.enums.AccountStatus;
import java.math.BigDecimal;

/** Login information and balance, separate from the owner's discount tier. */
public final class CustomerAccount implements Identifiable {
    private final String accountId;
    private final String username;
    private String passwordHash;
    private BigDecimal balance;
    private AccountStatus status;
    private final Customer customer;

    /** passwordHash must already be hashed by the caller; this model does not authenticate. */
    public CustomerAccount(String accountId, String username, String passwordHash,
                           BigDecimal balance, AccountStatus status, Customer customer) {
        this.accountId = Checks.id(accountId);
        this.username = Checks.name(username);
        this.passwordHash = Checks.required(passwordHash, "passwordHash");
        Checks.name(passwordHash);
        this.balance = Checks.money(balance);
        this.status = Checks.required(status, "status");
        this.customer = Checks.required(customer, "customer");
    }

    public String getId() { return accountId; }
    public String getAccountId() { return accountId; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public BigDecimal getBalance() { return balance; }
    public AccountStatus getStatus() { return status; }
    public Customer getCustomer() { return customer; }
    public void setPasswordHash(String passwordHash) {
        Checks.name(passwordHash);
        this.passwordHash = passwordHash;
    }
    public void setBalance(BigDecimal balance) { this.balance = Checks.money(balance); }
    public void setStatus(AccountStatus status) { this.status = Checks.required(status, "status"); }
}
