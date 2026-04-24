package uz.hemis.service.university.dto;

import lombok.Builder;
import lombok.Data;
import uz.hemis.domain.entity.employee.Employee;

@Data
@Builder
public class PersonRefDto {
    private String pinfl;
    private String firstName;
    private String lastName;
    private String middleName;
    private String phone;
    private String email;
    private String tin;
    private String passport;
    private String address;

    public static PersonRefDto from(Employee emp) {
        if (emp == null) return null;
        return PersonRefDto.builder()
                .pinfl(emp.getPinfl() != null ? emp.getPinfl().value() : null)
                .firstName(emp.getFirstName())
                .lastName(emp.getLastName())
                .middleName(emp.getMiddleName())
                .phone(emp.getPhone() != null ? emp.getPhone().value() : null)
                .email(emp.getEmail())
                .tin(emp.getTin() != null ? emp.getTin().value() : null)
                .passport(emp.getPassport())
                .address(emp.getAddress())
                .build();
    }

    public String getFullName() {
        StringBuilder sb = new StringBuilder();
        if (lastName != null) sb.append(lastName);
        if (firstName != null) { if (!sb.isEmpty()) sb.append(" "); sb.append(firstName); }
        if (middleName != null) { if (!sb.isEmpty()) sb.append(" "); sb.append(middleName); }
        return sb.isEmpty() ? null : sb.toString();
    }
}
