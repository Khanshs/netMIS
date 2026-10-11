package netcafe.model.NetSpecs;
import netcafe.Identifiable;
import netcafe.enums.EmployeeStatus;
import netcafe.model.Checks;

public final class Employee implements Identifiable {
  private final String employeeId, employeeName;
  private EmployeeStatus employeeStatus;
  public Employee(String id, String name, EmployeeStatus status) { employeeId= Checks.id(id); employeeName=Checks.name(name); employeeStatus=Checks.required(status, "status"); }
  public String getId() { return employeeId; }
  public String getEmployeeId() { return employeeId; }
  public String getEmployeeName() { return employeeName; }
  public EmployeeStatus getEmployeeStatus() { return employeeStatus; }
  public void setEmployeeStatus(EmployeeStatus status) { employeeStatus=Checks.required(status, "status"); }
}
