package netcafe.Service;

import netcafe.enums.*;
import netcafe.exception.*;
import netcafe.model.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import static netcafe.Service.ServiceChecks.*;

public final class SessionService {
    private final NetCafeData data;

    SessionService(NetCafeData data) {
        this.data = data;
    }

    public Session openSession(String id, Customer customer, Computer computer, LocalDateTime start) {
        unique(data.sessions, id);
        registered(data.customers, customer);
        registered(data.computers, computer);
        if (start == null) throw new InvalidTimeException("Session start is required");
        if (computer.getComputerStatus() != ComputerStatus.AVAILABLE) {
            throw new ComputerUnavailableException("Computer unavailable: " + computer.getId());
        }
        for (Session old : data.sessions) {
            if (old.getSessionStatus() == SessionStatus.ACTIVE && old.getCustomer() == customer) {
                throw new ActiveSessionExistsException("Customer already active: " + customer.getId());
            }
            if ((old.getComputer() == computer || old.getCustomer() == customer)
                    && (old.getTimeEnd() == null || old.getTimeEnd().isAfter(start))) {
                throw new InvalidTimeException("Session overlaps history or starts out of order");
            }
        }
        Session session = new Session(id, customer, computer, start);
        data.sessions.add(session);
        computer.setComputerStatus(ComputerStatus.IN_USE);
        return session;
    }

    public void endSession(Session session, LocalDateTime end) {
        registered(data.sessions, session);
        if (session.getSessionStatus() != SessionStatus.ACTIVE) {
            throw new SessionStateException("Session already completed: " + session.getId());
        }
        if (end == null || !end.isAfter(session.getTimeStart())) {
            throw new InvalidTimeException("End must be after start");
        }
        for (FoodService service : data.services) {
            if (service.getSession() == session && service.getTimeStartService().isAfter(end)) {
                throw new InvalidTimeException("Order occurs after proposed session end");
            }
        }
        session.complete(end);
        session.getComputer().setComputerStatus(ComputerStatus.AVAILABLE);
    }
}

