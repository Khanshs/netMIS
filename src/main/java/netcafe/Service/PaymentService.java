package netcafe.Service;

import netcafe.enums.*;
import netcafe.exception.*;
import netcafe.model.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import static netcafe.Service.ServiceChecks.*;

public final class PaymentService {
    private final NetCafeData data;

    PaymentService(NetCafeData data) {
        this.data = data;
    }

    public Payment pay(String id, Receipt receipt, LocalDateTime time, PaymentMethod method, BigDecimal amount) {
        unique(data.payments, id);
        registered(data.receipts, receipt);
        for (Payment payment : data.payments) {
            if (payment.getReceipt() == receipt) throw new PaymentAlreadyExistsException("Receipt already paid: " + receipt.getId());
        }
        Payment payment = new Payment(id, receipt, time, method, amount);
        data.payments.add(payment);
        return payment;
    }

    public BigDecimal collectedAmount() {
        BigDecimal sum = BigDecimal.ZERO;
        for (Payment payment : data.payments) sum = sum.add(payment.getAmount());
        return sum;
    }
}

