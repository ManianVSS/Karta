package randomization;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Name {
    private String firstName;
    private String middleName;
    private String lastName;
}
