package netcafe.model;

import netcafe.Identifiable;
import netcafe.IDiscount;
import java.math.BigDecimal;

/** Common customer data; each subclass supplies its own discount rates. */
public abstract class Customer implements Identifiable, IDiscount {
    private final String customerId;
    private final String name;

    protected Customer(String id, String name) {
        this.customerId = Checks.id(id);
        this.name = Checks.name(name);
    }

    public final String getId() { return customerId; }
    public final String getCustomerId() { return customerId; }
    public final String getName() { return name; }
    public abstract String getTierName();
    public abstract BigDecimal getComputerDiscountRate();
    public abstract BigDecimal getFoodDiscountRate();
}
