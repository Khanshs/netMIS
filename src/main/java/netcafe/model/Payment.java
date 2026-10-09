package netcafe.model;
import netcafe.Identifiable;
import netcafe.enums.PaymentMethod;
import netcafe.exception.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class Payment implements Identifiable {
  private final String paymentId;
  private final Receipt receipt;
  private final LocalDateTime time;
  private final PaymentMethod paymentMethod;
  private final BigDecimal amount;
  public Payment(String id, Receipt receipt, LocalDateTime time, PaymentMethod method, BigDecimal amount) {
    paymentId=Checks.id(id); this.receipt=Checks.required(receipt, "receipt"); this.time=Checks.required(time, "time");
    paymentMethod=Checks.required(method, "method"); this.amount=Checks.money(amount);
    if (receipt.getSession().getTimeEnd() == null) throw new SessionStateException("Cannot pay an active session");
    if (time.isBefore(receipt.getSession().getTimeEnd())) throw new InvalidTimeException("Payment precedes session end");
    if (this.amount.compareTo(receipt.getTotalAmount()) != 0) throw new PaymentAmountMismatchException("Expected " + receipt.getTotalAmount() + ", received " + amount);
  }
  public String getId() { return paymentId; }
  public String getPaymentId() { return paymentId; }
  public Receipt getReceipt() { return receipt; }
  public LocalDateTime getTime() { return time; }
  public PaymentMethod getPaymentMethod() { return paymentMethod; }
  public BigDecimal getAmount() { return amount; }
}
