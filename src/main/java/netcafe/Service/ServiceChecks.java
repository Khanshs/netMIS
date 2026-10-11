package netcafe.Service;

import netcafe.Identifiable;
import netcafe.enums.EmployeeStatus;
import netcafe.exception.*;
import netcafe.model.NetSpecs.Employee;
import java.util.List;

/** Validation helpers internal to the service package. */
final class ServiceChecks {
    private ServiceChecks() {}

    static <T> T required(T value, String field) {
        if (value == null) throw new ValidationException(field + " is required");
        return value;
    }

    static <T extends Identifiable> void unique(List<T> list, String id) {
        if (id == null || id.trim().isEmpty()) throw new ValidationException("ID is required");
        for (T old : list) {
            if (old.getId().equals(id.trim())) throw new DuplicateIdException("Duplicate ID: " + id);
        }
    }

    static <T extends Identifiable> void add(List<T> list, T value) {
        required(value, "entity");
        unique(list, value.getId());
        list.add(value);
    }

    static <T> void registered(List<T> list, T value) {
        required(value, "reference");
        if (!list.contains(value)) throw new EntityNotFoundException("Reference is not registered");
    }

    static void activeEmployee(Employee employee) {
        if (employee.getEmployeeStatus() != EmployeeStatus.ACTIVE) {
            throw new InactiveEmployeeException("Inactive employee: " + employee.getId());
        }
    }
}

