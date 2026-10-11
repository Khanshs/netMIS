package netcafe.model.NET;
import netcafe.Identifiable;
import netcafe.model.Checks;

import java.math.BigDecimal;

public final class ComputerSection implements Identifiable {
  private final String sectionId, sectionName;
  private final BigDecimal sectionPrice;
  public ComputerSection(String id, String name, BigDecimal price) {
    this.sectionId = Checks.id(id); this.sectionName = Checks.name(name);
    this.sectionPrice = Checks.money(price);
  }
  public String getId() { return sectionId; }
  public String getSectionId() { return sectionId; }
  public String getSectionName() { return sectionName; }
  public BigDecimal getSectionPrice() { return sectionPrice; }
}
