package netcafe.model.NET;
import netcafe.Chargeable;
import netcafe.Identifiable;
import netcafe.enums.SessionStatus;
import netcafe.exception.DuplicateIdException;
import netcafe.exception.InvalidOrderException;
import netcafe.exception.InvalidTimeException;
import netcafe.exception.SessionStateException;
import netcafe.model.Checks;
import netcafe.model.CustomerAccount;
import netcafe.model.FoodService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Session implements Identifiable, Chargeable {
  // Attribute
  private final String sessionId;
  private final CustomerAccount account;
  private final Computer computer;
  private final LocalDateTime timeStart;
  private final List<FoodService> foodServices = new ArrayList<>();
  private LocalDateTime timeEnd;
  private SessionStatus sessionStatus;

  //Constructor
  public Session(String id, CustomerAccount account, Computer computer, LocalDateTime start) {
    sessionId= Checks.id(id);
    this.account=Checks.required(account, "account");
    this.computer=Checks.required(computer, "computer");
    timeStart=Checks.required(start, "start");
    sessionStatus=SessionStatus.ACTIVE;
    computer.occupy(this);
  }

  public String getId() { return sessionId; }
  public String getSessionId() { return sessionId; }
  public CustomerAccount getAccount() { return account; }
  public Computer getComputer() { return computer; }
  public LocalDateTime getTimeStart() { return timeStart; }
  public LocalDateTime getTimeEnd() { return timeEnd; }
  public SessionStatus getSessionStatus() { return sessionStatus; }
  public List<FoodService> getFoodServices() { return Collections.unmodifiableList(foodServices); }

  /** Validate and lock an order before linking it to this session. */
  public void recordFoodService(FoodService service) {
    Checks.required(service, "foodService");
    if (sessionStatus != SessionStatus.ACTIVE) throw new SessionStateException("Order requires active session: " + sessionId);
    if (service.getSession() != this) throw new InvalidOrderException("Order belongs to another session");
    if (service.getTimeStartService().isBefore(timeStart)) throw new InvalidTimeException("Order time precedes session start");
    for (FoodService old : foodServices) {
      if (old.getId().equals(service.getId())) throw new DuplicateIdException("Duplicate order ID: " + service.getId());
    }
    service.seal();
    foodServices.add(service);
  }

  /** Both direct callers and SessionService use these completion rules. */
  public void endSession(LocalDateTime end) {
    if (sessionStatus != SessionStatus.ACTIVE) throw new SessionStateException("Session is already completed: " + sessionId);
    if (end == null || !end.isAfter(timeStart)) throw new InvalidTimeException("End must be after start: " + sessionId);
    for (FoodService service : foodServices) {
      if (service.getTimeStartService().isAfter(end)) throw new InvalidTimeException("Order occurs after proposed session end");
    }
    computer.release(this);
    timeEnd=end; sessionStatus=SessionStatus.COMPLETED;
  }
  public BigDecimal amountAt(LocalDateTime at) {
    if (at == null || at.isBefore(timeStart)) throw new InvalidTimeException("Time before session start or missing: " + sessionId);
    long seconds=Duration.between(timeStart, at).getSeconds();
    return computer.getSection().getSectionPrice().multiply(BigDecimal.valueOf(seconds)).divide(BigDecimal.valueOf(3600), 0, RoundingMode.HALF_UP);
  }
  public BigDecimal calculateAmount() {
    if (timeEnd == null) throw new SessionStateException("Cannot finalize amount of active session: " + sessionId);
    return amountAt(timeEnd);
  }
}
