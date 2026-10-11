package netcafe.model;
import netcafe.Chargeable;
import netcafe.Identifiable;
import netcafe.exception.InvalidOrderException;
import netcafe.model.NET.Employee;
import netcafe.model.NET.Food;
import netcafe.model.NET.Session;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class FoodService implements Identifiable, Chargeable {
  // Attribute
  private final String serviceId;
  private final Session session;
  private final Employee employee;
  private final LocalDateTime timeStartService;
  private final List<FoodOrderDetail> details=new ArrayList<>();
  private boolean sealed;


  // Constructor
  public FoodService(String id, Session session, Employee employee, LocalDateTime time) {
    serviceId=Checks.id(id);
    this.session=Checks.required(session, "session");
    this.employee=Checks.required(employee, "employee");
    timeStartService=Checks.required(time, "time");
  }
  // Getter
  public String getId() { return serviceId; }
  public String getServiceId() { return serviceId; }
  public Session getSession() { return session; }
  public Employee getEmployee() { return employee; }
  public LocalDateTime getTimeStartService() { return timeStartService; }
  public List<FoodOrderDetail> getDetails() { return Collections.unmodifiableList(details); }

  public void addItem(Food food, int quantity, BigDecimal priceAtOrder) {
    if (sealed) throw new InvalidOrderException("Order is recorded and cannot be changed: " + serviceId);
    Checks.required(food, "food"); Checks.money(priceAtOrder);
    if (quantity <= 0) throw new InvalidOrderException("Quantity must be positive");
    for (FoodOrderDetail detail: details) if (detail.getFood() == food) {
      if (detail.getPriceAtOrder().compareTo(priceAtOrder) != 0) throw new InvalidOrderException("Different price for same food in one order");
      detail.addQuantity(quantity); return;
    }
    details.add(new FoodOrderDetail(this, food, quantity, priceAtOrder));
  }
  public BigDecimal getTotal() { return calculateAmount(); }
  public void seal() {
    if (sealed) return;
    if (details.isEmpty()) throw new InvalidOrderException("Order must have at least one line");
    sealed = true;
  }
  public BigDecimal calculateAmount() { BigDecimal total=BigDecimal.ZERO; for (FoodOrderDetail d: details) total=total.add(d.calculateAmount()); return total; }
}
