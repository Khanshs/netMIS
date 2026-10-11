package netcafe.model;
import java.math.BigDecimal;
import netcafe.exception.ValidationException;
/** Shared validation for models in this package and its NET subpackage. */
public final class Checks {
  private Checks() {}

  public static String id(String value) {
    if (value == null || value.trim().isEmpty())
      throw new ValidationException("ID/name is required");
    return value.trim(); }

  public static String name(String value) { return id(value); }

  public static BigDecimal money(BigDecimal value) {
    if (value == null || value.signum() < 0)
      throw new ValidationException("Amount must be non-null and non-negative");
    return value; }

  public static <T> T required(T value, String field) {
    if (value == null)
      throw new ValidationException(field + " is required");
    return value; }
}
