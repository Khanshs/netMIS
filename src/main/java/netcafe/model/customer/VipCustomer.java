package netcafe.model.customer;

import netcafe.model.Customer;
import java.math.BigDecimal;

public final class VipCustomer extends Customer {
    public VipCustomer(String id, String name) {
        super(id, name);
    }

    @Override
    public String getTierName() { return "VIP"; }

    @Override
    public BigDecimal getComputerDiscountRate() { return new BigDecimal("0.15"); }

    @Override
    public BigDecimal getFoodDiscountRate() { return new BigDecimal("0.08"); }
}

