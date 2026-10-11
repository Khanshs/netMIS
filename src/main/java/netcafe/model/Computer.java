package netcafe.model;
import netcafe.Identifiable;
import netcafe.enums.ComputerStatus;
import netcafe.exception.ComputerUnavailableException;
import netcafe.exception.SessionStateException;

public final class Computer implements Identifiable {
  // Attribute
  private final String computerId;
  private final ComputerSection section;
  private ComputerStatus computerStatus;
  private Session activeSession;

  public Computer(String id, ComputerSection section, ComputerStatus status) {
    this.computerId = Checks.id(id);
    this.section = Checks.required(section, "section");
    this.computerStatus = Checks.required(status, "status");
  }
  // Constructor
  // Getter implements Identifiable
  public String getId() { return computerId; }
  public String getComputerId() { return computerId; }
  public ComputerSection getSection() { return section; }
  public ComputerStatus getComputerStatus() { return computerStatus; }
  // Manual changes are for idle machines; IN_USE is controlled by the session lifecycle.
  public void setComputerStatus(ComputerStatus status) {
    Checks.required(status, "status");
    if (activeSession != null || status == ComputerStatus.IN_USE) {
      throw new ComputerUnavailableException("An active session controls the computer status: " + computerId);
    }
    computerStatus = status;
  }

  void occupy(Session session) {
    if (computerStatus != ComputerStatus.AVAILABLE || activeSession != null) {
      throw new ComputerUnavailableException("Computer unavailable: " + computerId);
    }
    activeSession = session;
    computerStatus = ComputerStatus.IN_USE;
  }

  void release(Session session) {
    if (activeSession != session) throw new SessionStateException("Session does not own computer: " + computerId);
    activeSession = null;
    computerStatus = ComputerStatus.AVAILABLE;
  }
}
