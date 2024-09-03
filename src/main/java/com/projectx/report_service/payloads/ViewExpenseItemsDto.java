package com.projectx.report_service.payloads;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ViewExpenseItemsDto {
    private Integer srNo;
    private String itemName;
    private String itemPrice;
    private String paymentWith;
}
