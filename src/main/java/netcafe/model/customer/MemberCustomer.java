package netcafe.model.customer;

import java.math.BigDecimal;

public final class MemberCustomer extends Customer {
    public MemberCustomer(String id, String name) {
        super(id, name);
    }

    @Override
    public String getTierName() { return "MEMBER"; }

    @Override
    public BigDecimal getComputerDiscountRate() { return new BigDecimal("0.10"); }

    @Override
    public BigDecimal getFoodDiscountRate() { return new BigDecimal("0.05"); }
}

