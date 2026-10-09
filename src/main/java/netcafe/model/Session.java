package netcafe.model;
import netcafe.Chargeable;
import netcafe.Identifiable;
import netcafe.enums.SessionStatus;
import netcafe.exception.InvalidTimeException;
import netcafe.exception.SessionStateException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

public final class Session implements Identifiable, Chargeable {
  // Attribute
  private final String sessionId;
  private final Customer customer;
  private final Computer computer;
  private final LocalDateTime timeStart;
  private LocalDateTime timeEnd;
  private SessionStatus sessionStatus;

  //Constructor
  public Session(String id, Customer customer, Computer computer, LocalDateTime start) {
    sessionId=Checks.id(id);
    this.customer=Checks.required(customer, "customer");
    this.computer=Checks.required(computer, "computer");
    timeStart=Checks.required(start, "start");
    sessionStatus=SessionStatus.ACTIVE;
  }

  public String getId() { return sessionId; }
  public String getSessionId() { return sessionId; }
  public Customer getCustomer() { return customer; }
  public Computer getComputer() { return computer; }
  public LocalDateTime getTimeStart() { return timeStart; }
  public LocalDateTime getTimeEnd() { return timeEnd; }
  public SessionStatus getSessionStatus() { return sessionStatus; }
  public void complete(LocalDateTime end) {
    if (sessionStatus != SessionStatus.ACTIVE) throw new SessionStateException("Session is already completed: " + sessionId);
    if (end == null || !end.isAfter(timeStart)) throw new InvalidTimeException("End must be after start: " + sessionId);
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
