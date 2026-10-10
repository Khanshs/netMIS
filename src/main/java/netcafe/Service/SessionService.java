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

    public Session openSession(String id, CustomerAccount account, Computer computer, LocalDateTime start) {
        return openSession(id, account, computer, start, null);
    }

    public Session openSession(String id, CustomerAccount account, Computer computer, LocalDateTime start,
                               LocalDateTime plannedEndTime) {
        unique(data.sessions, id);
        registered(data.accounts, account);
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new InactiveAccountException("Inactive account: " + account.getId());
        }
        registered(data.computers, computer);
        if (start == null) throw new InvalidTimeException("Session start is required");
        if (computer.getComputerStatus() != ComputerStatus.AVAILABLE) {
            throw new ComputerUnavailableException("Computer unavailable: " + computer.getId());
        }
        for (Session old : data.sessions) {
            if (old.getSessionStatus() == SessionStatus.ACTIVE && old.getAccount() == account) {
                throw new ActiveSessionExistsException("Account already active: " + account.getId());
            }
            if ((old.getComputer() == computer || old.getAccount() == account)
                    && (old.getTimeEnd() == null || old.getTimeEnd().isAfter(start))) {
                throw new InvalidTimeException("Session overlaps history or starts out of order");
            }
        }
        Session session = new Session(id, account, computer, start, plannedEndTime);
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

