package randomization;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Address
{
   private String addressline1;
   private String landmark;
   private String addressline2;
   private int    pincode;
}
