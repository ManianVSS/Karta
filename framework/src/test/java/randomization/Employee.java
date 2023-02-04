package randomization;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Employee {
    private String employeeId;
    private Name name;
    private Gender gender;
    private boolean married;
    private Role role;
    private int salary;
    private Address address;
}
