package netcafe.Demo;
import netcafe.Main;
import netcafe.Service.NetCafe;

import netcafe.enums.PaymentMethod;
import netcafe.exception.*;
import netcafe.model.customer.NormalCustomer;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Run separately to see how the caller handles business errors. */
public final class ExceptionDemo {
    public static void main(String[] args) {
        NetCafe cafe = Main.sample();
        try {
            cafe.addCustomer(new NormalCustomer("C01", "Other customer"));
        }
        catch (DuplicateIdException failure) {
            System.out.println("DuplicateIdException: " + failure.getMessage());
        }

        try {
            cafe.issueReceipt("R03", cafe.getSessions().get(3), cafe.getEmployees().get(0));
        }
        catch (SessionStateException failure) {
            System.out.println("SessionStateException: " + failure.getMessage());
        }

        try {
            cafe.pay("P02", cafe.getReceipts().get(1), LocalDateTime.of(2026, 10, 5, 12, 0),
                     PaymentMethod.CASH, BigDecimal.ONE);
        }
        catch (PaymentAmountMismatchException failure) {
            System.out.println("PaymentAmountMismatchException: " + failure.getMessage());
        }
        System.out.println("Collected remains=" + cafe.collectedAmount());
    }
}
