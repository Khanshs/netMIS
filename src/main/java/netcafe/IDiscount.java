package netcafe;

import java.math.BigDecimal;

/** Rates are fractions: 0.10 means 10 percent. */
public interface IDiscount {
    String getTierName();
    BigDecimal getComputerDiscountRate();
    BigDecimal getFoodDiscountRate();
}
