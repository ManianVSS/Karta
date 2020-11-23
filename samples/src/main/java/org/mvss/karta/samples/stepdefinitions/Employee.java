package org.mvss.karta.samples.stepdefinitions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
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
