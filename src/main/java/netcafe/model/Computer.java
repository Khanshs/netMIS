package netcafe.model;
import netcafe.Identifiable;
import netcafe.enums.ComputerStatus;

public final class Computer implements Identifiable {
  // Attribute
  private final String computerId;
  private final ComputerSection section;
  private ComputerStatus computerStatus;
  // Constructor
  public Computer(String id, ComputerSection section, ComputerStatus status) {
    this.computerId = Checks.id(id);
    this.section = Checks.required(section, "section");
    this.computerStatus = Checks.required(status, "status");
  }
  // Getter implements Identifiable
  public String getId() { return computerId; }
  public String getComputerId() { return computerId; }
  public ComputerSection getSection() { return section; }
  public ComputerStatus getComputerStatus() { return computerStatus; }
  public void setComputerStatus(ComputerStatus status) { computerStatus = Checks.required(status, "status"); }
}
