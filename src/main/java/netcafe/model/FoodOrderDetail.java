package netcafe.model;
import netcafe.Chargeable;
import netcafe.Service.FoodService;
import netcafe.exception.InvalidOrderException;
import netcafe.model.NetSpecs.Food;

import java.math.BigDecimal;

public final class FoodOrderDetail implements Chargeable {
  // Attribute
  private final FoodService service;
  private final Food food;
  private int orderQuantity;
  private final BigDecimal priceAtOrder;

  // Constructor
  public FoodOrderDetail(FoodService service, Food food, int quantity, BigDecimal price) {
    this.service=Checks.required(service, "service");
    this.food=Checks.required(food, "food");
    if (quantity <= 0)
      throw new InvalidOrderException("Quantity must be positive");
    orderQuantity=quantity;
    priceAtOrder=Checks.money(price);
  }

  public void addQuantity(int quantity) {
    if (service.isSealed())
      throw new InvalidOrderException("Order is recorded and cannot be changed: " + service.getId());
    if (quantity <= 0 || orderQuantity > Integer.MAX_VALUE - quantity)
      throw new InvalidOrderException("Quantity must be positive and within integer range");
    orderQuantity += quantity;
  }
  // Getter
  public FoodService getService() { return service; }
  public Food getFood() { return food; }
  public int getOrderQuantity() { return orderQuantity; }
  public BigDecimal getPriceAtOrder() { return priceAtOrder; }
  public BigDecimal calculateAmount() {
    return priceAtOrder.multiply(BigDecimal.valueOf(orderQuantity)); }
}
