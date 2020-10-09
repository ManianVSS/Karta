package org.mvss.karta.samples.stepdefinitions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Employee
{
   private String  employeeId;
   private String  name;
   private String  gender;
   private boolean married;
   private String  role;
   private int     salary;
   private String  address;
}
