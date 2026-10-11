package netcafe.model;

import netcafe.Chargeable;
import netcafe.Identifiable;
import netcafe.IDiscount;
import netcafe.exception.ValidationException;
import netcafe.model.NetSpecs.Employee;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Gross amounts, applied rates and discounts are frozen when issued. */
public final class Receipt implements Identifiable, Chargeable {
    // Attribute
    private final String receiptId;
    private final Session session;
    private final Employee employee;
    private final String customerTier;
    private final BigDecimal grossServiceAmount, grossComputerAmount;
    private final BigDecimal serviceDiscountRate, computerDiscountRate;
    private final BigDecimal serviceDiscountAmount, computerDiscountAmount;
    private final BigDecimal serviceAmount, computerAmount;

    public Receipt(String id, Session session, Employee employee, BigDecimal serviceAmount, BigDecimal computerAmount) {
        this(id, session, employee, serviceAmount, computerAmount,
             Checks.required(session, "session").getAccount().getCustomer());
    }

    public Receipt(String id, Session session, Employee employee, BigDecimal grossServiceAmount, BigDecimal grossComputerAmount, IDiscount policy) {
        receiptId = Checks.id(id);
        this.session = Checks.required(session, "session");
        this.employee = Checks.required(employee, "employee");
        Checks.required(policy, "discountPolicy");
        customerTier = Checks.name(policy.getTierName());
        computerDiscountRate = rate(policy.getComputerDiscountRate());
        serviceDiscountRate = rate(policy.getFoodDiscountRate());

        this.grossServiceAmount = Checks.money(grossServiceAmount);
        this.grossComputerAmount = Checks.money(grossComputerAmount);

        serviceDiscountAmount = discount(this.grossServiceAmount, serviceDiscountRate);
        computerDiscountAmount = discount(this.grossComputerAmount, computerDiscountRate);
        serviceAmount = this.grossServiceAmount.subtract(serviceDiscountAmount);
        computerAmount = this.grossComputerAmount.subtract(computerDiscountAmount);
    }

    private static BigDecimal rate(BigDecimal value) {
        if (value == null || value.signum() < 0 || value.compareTo(BigDecimal.ONE) > 0) {
            throw new ValidationException("Discount rate must be between 0 and 1");
        }
        return value;
    }

    private static BigDecimal discount(BigDecimal gross, BigDecimal rate) {
        // Cap at gross even when a fractional VND price was entered.
        return gross.multiply(rate).setScale(0, RoundingMode.HALF_UP).min(gross);
    }

    public String getId() { return receiptId; }
    public String getReceiptId() { return receiptId; }
    public Session getSession() { return session; }
    public Employee getEmployee() { return employee; }
    public String getCustomerTier() { return customerTier; }
    public BigDecimal getGrossServiceAmount() { return grossServiceAmount; }
    public BigDecimal getGrossComputerAmount() { return grossComputerAmount; }
    public BigDecimal getServiceDiscountRate() { return serviceDiscountRate; }
    public BigDecimal getComputerDiscountRate() { return computerDiscountRate; }
    public BigDecimal getServiceDiscountAmount() { return serviceDiscountAmount; }
    public BigDecimal getComputerDiscountAmount() { return computerDiscountAmount; }
    public BigDecimal getTotalDiscountAmount() { return serviceDiscountAmount.add(computerDiscountAmount); }
    public BigDecimal getServiceAmount() { return serviceAmount; }
    public BigDecimal getComputerAmount() { return computerAmount; }
    public BigDecimal getTotalAmount() { return calculateAmount(); }
    public BigDecimal calculateAmount() { return serviceAmount.add(computerAmount); }
}
