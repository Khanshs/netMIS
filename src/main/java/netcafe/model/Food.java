package netcafe.model;
import netcafe.Identifiable;
import java.math.BigDecimal;
public final class Food implements Identifiable {
  private final String foodId, foodName;
  private BigDecimal price;
  public Food(String id, String name, BigDecimal price) { foodId=Checks.id(id); foodName=Checks.name(name); this.price=Checks.money(price); }
  public String getId() { return foodId; }
  public String getFoodId() { return foodId; }
  public String getFoodName() { return foodName; }
  public BigDecimal getPrice() { return price; }
  public void setPrice(BigDecimal price) { this.price=Checks.money(price); }
}
