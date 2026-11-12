package com.amarjo.novaralms.payment.Controller;

import com.amarjo.novaralms.payment.DTO.PaymentApiResponse;
import com.amarjo.novaralms.payment.DTO.ReceiptEventResponse;
import com.amarjo.novaralms.payment.Services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mob/v1/payment")
public class MobilePaymentController {
    @Autowired
    private PaymentService paymentService;
    public static final Character c = 'M';

    @GetMapping("/verify/{accountNumber}")
    public ResponseEntity<String> verify(@PathVariable Long accountNumber) {
        paymentService.validateUser(c,accountNumber);
        return ResponseEntity.ok("Payment Verify Successful");
    }
    @PostMapping("/receipt/{courseCode}/create")
    public ResponseEntity<PaymentApiResponse<ReceiptEventResponse>> createReceipt(@PathVariable String courseCode) {
        ReceiptEventResponse response = paymentService.createReceipt(c,courseCode);
        return ResponseEntity.status(HttpStatus.CREATED).body(new PaymentApiResponse<>("Receipt created Successfully",response));
    }
}
