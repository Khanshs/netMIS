package netcafe.Service;

import netcafe.enums.*;
import netcafe.exception.*;
import netcafe.model.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/** Provides one shared data context and its business services. */
public final class NetCafe {
    private final NetCafeData data = new NetCafeData();
    private final CatalogService catalogService = new CatalogService(data);
    private final SessionService sessionService = new SessionService(data);
    private final FoodOrderService foodOrderService = new FoodOrderService(data);
    private final ReceiptService receiptService = new ReceiptService(data);
    private final PaymentService paymentService = new PaymentService(data);

    public CatalogService getCatalogService() { return catalogService; }
    public SessionService getSessionService() { return sessionService; }
    public FoodOrderService getFoodOrderService() { return foodOrderService; }
    public ReceiptService getReceiptService() { return receiptService; }
    public PaymentService getPaymentService() { return paymentService; }

    public void addSection(ComputerSection value) { catalogService.addSection(value); }
    public void addComputer(Computer value) { catalogService.addComputer(value); }
    public void addCustomer(Customer value) { catalogService.addCustomer(value); }
    public void addAccount(CustomerAccount value) { catalogService.addAccount(value); }
    public void addEmployee(Employee value) { catalogService.addEmployee(value); }
    public void addFood(Food value) { catalogService.addFood(value); }

    public Session openSession(String id, CustomerAccount account, Computer computer, LocalDateTime start) {
        return sessionService.openSession(id, account, computer, start);
    }

    public void endSession(Session session, LocalDateTime end) {
        sessionService.endSession(session, end);
    }

    public FoodService order(String id, Session session, Employee employee, LocalDateTime time,
                             Food[] items, int[] quantities) {
        return foodOrderService.order(id, session, employee, time, items, quantities);
    }

    public FoodService order(String id, Session session, Employee employee, LocalDateTime time,
                             Food[] items, int[] quantities, BigDecimal[] prices) {
        return foodOrderService.order(id, session, employee, time, items, quantities, prices);
    }

    public Receipt issueReceipt(String id, Session session, Employee employee) {
        return receiptService.issueReceipt(id, session, employee);
    }

    public Payment pay(String id, Receipt receipt, LocalDateTime time, PaymentMethod method, BigDecimal amount) {
        return paymentService.pay(id, receipt, time, method, amount);
    }

    public BigDecimal collectedAmount() { return paymentService.collectedAmount(); }

    public List<ComputerSection> getSections() { return Collections.unmodifiableList(data.sections); }
    public List<Computer> getComputers() { return Collections.unmodifiableList(data.computers); }
    public List<Customer> getCustomers() { return Collections.unmodifiableList(data.customers); }
    public List<CustomerAccount> getAccounts() { return Collections.unmodifiableList(data.accounts); }
    public List<Employee> getEmployees() { return Collections.unmodifiableList(data.employees); }
    public List<Food> getFoods() { return Collections.unmodifiableList(data.foods); }
    public List<Session> getSessions() { return Collections.unmodifiableList(data.sessions); }
    public List<FoodService> getServices() { return Collections.unmodifiableList(data.services); }
    public List<Receipt> getReceipts() { return Collections.unmodifiableList(data.receipts); }
    public List<Payment> getPayments() { return Collections.unmodifiableList(data.payments); }
}

