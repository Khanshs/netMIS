package netcafe.Service;

import netcafe.IDiscount;

import netcafe.enums.*;
import netcafe.exception.*;
import netcafe.model.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import static netcafe.Service.ServiceChecks.*;

public final class ReceiptService {
    private final NetCafeData data;

    ReceiptService(NetCafeData data) {
        this.data = data;
    }

    public Receipt issueReceipt(String id, Session session, Employee employee) {
        unique(data.receipts, id);
        registered(data.sessions, session);
        registered(data.employees, employee);
        if (session.getSessionStatus() != SessionStatus.COMPLETED) {
            throw new SessionStateException("Receipt requires completed session");
        }
        activeEmployee(employee);
        for (Receipt receipt : data.receipts) {
            if (receipt.getSession() == session) throw new ReceiptAlreadyExistsException("Session already has a receipt");
        }
        BigDecimal serviceAmount = BigDecimal.ZERO;
        for (FoodService service : data.services) {
            if (service.getSession() == session) serviceAmount = serviceAmount.add(service.getTotal());
        }
        IDiscount policy = session.getCustomer();
        Receipt receipt = new Receipt(id, session, employee, serviceAmount, session.calculateAmount(), policy);
        data.receipts.add(receipt);
        return receipt;
    }
}

