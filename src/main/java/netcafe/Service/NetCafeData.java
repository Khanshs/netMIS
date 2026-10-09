package netcafe.Service;

import netcafe.model.*;
import java.util.ArrayList;
import java.util.List;

/** Shared lists for the services of one cafe. */
// GENERIC
final class NetCafeData {
    final List<ComputerSection> sections = new ArrayList<>();
    final List<Computer> computers = new ArrayList<>();
    final List<Customer> customers = new ArrayList<>();
    final List<Employee> employees = new ArrayList<>();
    final List<Food> foods = new ArrayList<>();
    final List<Session> sessions = new ArrayList<>();
    final List<FoodService> services = new ArrayList<>();
    final List<Receipt> receipts = new ArrayList<>();
    final List<Payment> payments = new ArrayList<>();
}

