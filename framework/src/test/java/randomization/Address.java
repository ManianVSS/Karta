package randomization;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Address {
    private String addressline1;
    private String landmark;
    private String addressline2;
    private int pincode;
}
