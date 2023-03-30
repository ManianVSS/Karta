package org.mvss.cucumber.tests.hooks;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Employee {
    private String employeeId;
    private String name;
    private String gender;
    private boolean married;
    private String role;
    private int salary;
    private String address;
}
