package loyaltycard.controller.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateTransactionRequestDto {

    @NotNull
    private DirectionDto direction;

    @NotNull
    @Min(1)
    private Integer amount;

    private String note;
}
