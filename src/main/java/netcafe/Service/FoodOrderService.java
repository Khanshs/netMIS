package netcafe.Service;

import netcafe.enums.*;
import netcafe.exception.*;
import netcafe.model.*;
import netcafe.model.NetSpecs.Employee;
import netcafe.model.NetSpecs.Food;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static netcafe.Service.ServiceChecks.*;

public final class FoodOrderService {
    private final NetCafeData data;

    FoodOrderService(NetCafeData data) {
        this.data = data;
    }

    /** Normal orders use the current catalogue price. */
    public FoodService order(String id, Session session, Employee employee, LocalDateTime time,
                             Food[] items, int[] quantities) {
        if (items == null)
            throw new InvalidOrderException("Order items are required");
        BigDecimal[] prices = new BigDecimal[items.length];
        for (int i = 0; i < items.length; i++) {
            registered(data.foods, items[i]);
            prices[i] = items[i].getPrice();
        }
        return order(id, session, employee, time, items, quantities, prices);
    }

    /** Explicit prices support importing the historical sample in the specification. */
    public FoodService order(String id, Session session, Employee employee, LocalDateTime time,
                             Food[] items, int[] quantities, BigDecimal[] prices) {
        unique(data.services, id);
        registered(data.sessions, session);
        registered(data.employees, employee);
        if (session.getSessionStatus() != SessionStatus.ACTIVE) {
            throw new SessionStateException("Order requires active session");
        }
        activeEmployee(employee);
        if (time == null || time.isBefore(session.getTimeStart())) {
            throw new InvalidTimeException("Order time precedes session start or is missing");
        }
        if (items == null || quantities == null || prices == null || items.length == 0
                || items.length != quantities.length || items.length != prices.length) {
            throw new InvalidOrderException("Order must contain matching nonempty lines");
        }
        FoodService service = new FoodService(id, session, employee, time);
        for (int i = 0; i < items.length; i++) {
            registered(data.foods, items[i]);
            service.addItem(items[i], quantities[i], prices[i]);
        }
        session.recordFoodService(service);
        data.services.add(service);
        return service;
    }
}

