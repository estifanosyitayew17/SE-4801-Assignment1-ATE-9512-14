// ID: ATE/9512/14
package com.shopwave.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStockRequest {

    @NotNull(message = "Delta is required")
    private Integer delta; // Can be positive (add stock) or negative (reduce stock)
}