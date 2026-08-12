package com.realconstruction.payment.event;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRefundedEvent {

    private String paymentId;
    private String orderId;
    private String userId;
    private BigDecimal amount;
    private String status;
}