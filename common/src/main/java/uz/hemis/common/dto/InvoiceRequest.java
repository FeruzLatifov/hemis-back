package uz.hemis.common.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class InvoiceRequest {
    private String studentId;
    private BigDecimal amount;
    private String description;
}
