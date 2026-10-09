package netcafe.Service;

import netcafe.enums.*;
import netcafe.exception.*;
import netcafe.model.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import static netcafe.Service.ServiceChecks.*;

public final class CatalogService {
    private final NetCafeData data;

    CatalogService(NetCafeData data) {
        this.data = data;
    }

    public void addSection(ComputerSection value) { add(data.sections, value); }
    public void addCustomer(Customer value) { add(data.customers, value); }
    public void addEmployee(Employee value) { add(data.employees, value); }
    public void addFood(Food value) { add(data.foods, value); }

    public void addComputer(Computer value) {
        required(value, "computer");
        registered(data.sections, value.getSection());
        if (value.getComputerStatus() == ComputerStatus.IN_USE) {
            throw new ComputerUnavailableException("New computer cannot be IN_USE without a session");
        }
        add(data.computers, value);
    }
}

