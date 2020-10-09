package randomization;

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
   private Name    name;
   private Gender  gender;
   private boolean married;
   private Role    role;
   private int     salary;
   private Address address;
}
