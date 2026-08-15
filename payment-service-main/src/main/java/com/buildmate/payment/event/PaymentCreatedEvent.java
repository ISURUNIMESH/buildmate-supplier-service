package com.buildmate.payment.event;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCreatedEvent {

    private String paymentId;
    private String orderId;
    private String userId;
    private BigDecimal amount;
    private String status;

}
