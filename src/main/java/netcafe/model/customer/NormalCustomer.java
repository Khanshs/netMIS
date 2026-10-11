package netcafe.model.customer;

import java.math.BigDecimal;

public final class NormalCustomer extends Customer {
    public NormalCustomer(String id, String name) {
        super(id, name);
    }

    @Override
    public String getTierName() { return "NORMAL"; }

    @Override
    public BigDecimal getComputerDiscountRate() { return new BigDecimal("0"); }

    @Override
    public BigDecimal getFoodDiscountRate() { return new BigDecimal("0"); }
}

