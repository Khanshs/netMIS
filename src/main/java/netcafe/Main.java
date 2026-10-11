package netcafe;
import netcafe.Service.*;
import netcafe.enums.*;
import netcafe.model.*;
import netcafe.model.NET.*;
import netcafe.model.customer.NormalCustomer;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public final class Main {
  private static BigDecimal v(long amount) { return BigDecimal.valueOf(amount); }
  private static LocalDateTime t(int hour, int minute) { return LocalDateTime.of(2026,10,5,hour,minute); }
  public static NetCafe sample() {
    NetCafe cafe=new NetCafe();
    CatalogService catalog = cafe.getCatalogService();
    SessionService sessions = cafe.getSessionService();
    FoodOrderService orders = cafe.getFoodOrderService();
    ReceiptService receipts = cafe.getReceiptService();
    PaymentService payments = cafe.getPaymentService();
    ComputerSection
            normal=new ComputerSection("CS01","Khu thường",v(12000)),
            vip=new ComputerSection("CS02","Khu VIP",v(20000));
    catalog.addSection(normal); catalog.addSection(vip);

    Computer
            pc1=new Computer("PC01",normal,ComputerStatus.AVAILABLE),
            pc2=new Computer("PC02",normal,ComputerStatus.AVAILABLE),
            pc3=new Computer("PC03",vip,ComputerStatus.AVAILABLE),
            pc4=new Computer("PC04",vip,ComputerStatus.AVAILABLE);
    catalog.addComputer(pc1);
    catalog.addComputer(pc2);
    catalog.addComputer(pc3);
    catalog.addComputer(pc4);
    catalog.addComputer(new Computer("PC05",normal,ComputerStatus.MAINTENANCE));
    catalog.addComputer(new Computer("PC06",vip,ComputerStatus.AVAILABLE));

    Customer
            c1=new NormalCustomer("C01","Lamine Yamal"),
            c2=new NormalCustomer("C02","Bruno Fernades"),
            c3=new NormalCustomer("C03","Cristiano Ronaldo");
    catalog.addCustomer(c1);
    catalog.addCustomer(c2);
    catalog.addCustomer(c3);
    catalog.addCustomer(new NormalCustomer("C04","Lionel Messi"));

    // Demo-only placeholders; no login/authentication is implemented by this sample.
    CustomerAccount a1 = new CustomerAccount("A01", "yamal", "demo-hash-not-for-login", v(0), AccountStatus.ACTIVE, c1);
    CustomerAccount a2 = new CustomerAccount("A02", "bruno", "demo-hash-not-for-login", v(0), AccountStatus.ACTIVE, c2);
    CustomerAccount a3 = new CustomerAccount("A03", "ronaldo", "demo-hash-not-for-login", v(0), AccountStatus.ACTIVE, c3);
    CustomerAccount a4 = new CustomerAccount("A04", "messi", "demo-hash-not-for-login", v(0), AccountStatus.ACTIVE, cafe.getCustomers().get(3));
    catalog.addAccount(a1); catalog.addAccount(a2); catalog.addAccount(a3); catalog.addAccount(a4);

    Employee
            e1=new Employee("E01","Erik TenHag",EmployeeStatus.ACTIVE),
            e2=new Employee("E02","Anchelotti",EmployeeStatus.ACTIVE);
    catalog.addEmployee(e1);
    catalog.addEmployee(e2);
    catalog.addEmployee(new Employee("E03","Luis Enrique",EmployeeStatus.INACTIVE));

    Food
            f1=new Food("F01","Mì ly",v(13000)),
            f2=new Food("F02","Nước ngọt",v(15000));
    catalog.addFood(f1);
    catalog.addFood(f2);
    catalog.addFood(new Food("F03","Snack",v(7000)));

    Session s1=sessions.openSession("S01",a1,pc1,t(8,0));
    orders.order(
            "SV01",s1,e1,t(8,20),
            new Food[]{f1,f2},
            new int[]{1,1},
            new BigDecimal[]{v(15000),v(10000)});

    Session s2=sessions.openSession("S02",a2,pc3,t(9,0));
    orders.order(
            "SV02",s2,e2,t(9,30),
            new Food[]{f1},
            new int[]{2},
            new BigDecimal[]{v(15000)});

    Session s3=sessions.openSession("S03",a3,pc4,t(10,0));

    sessions.endSession(s1,t(10,0));
    sessions.endSession(s2,t(10,30));
    sessions.endSession(s3,t(11,0));

    Session s4=sessions.openSession("S04",a1,pc2,t(11,0));
    orders.order(
            "SV03",s4,e1,t(11,15),
            new Food[]{f2},
            new int[]{2},
            new BigDecimal[]{v(10000)});

    Receipt r1=receipts.issueReceipt("R01",s1,e2),
            r2=receipts.issueReceipt("R02",s2,e1);
    payments.pay("P01",r1,t(10,5),PaymentMethod.CASH,r1.getTotalAmount());
    return cafe;
  }
  public static void main(String[] args) {
    NetCafe cafe=sample();
    for (Receipt r:cafe.getReceipts()) System.out.println(r.getReceiptId()+" session="+r.getSession().getSessionId()+" computer="+r.getComputerAmount()+" service="+r.getServiceAmount()+" total="+r.getTotalAmount());
    System.out.println("Collected="+cafe.collectedAmount());
    for (Session s:cafe.getSessions()) System.out.println(s.getSessionId()+" " +s.getSessionStatus()+" " +s.getComputer().getComputerId());
  }
}
