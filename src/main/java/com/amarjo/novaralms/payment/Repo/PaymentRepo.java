package com.amarjo.novaralms.payment.Repo;

import com.amarjo.novaralms.payment.Model.CoursePayment;
import com.amarjo.novaralms.payment.Model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepo extends JpaRepository<CoursePayment, UUID> {
    Optional<CoursePayment> findByCourseCode(String courseCode);
    Optional<CoursePayment> findByPaymentCode(Long paymentCode);
    Optional<CoursePayment> findByPayerAccountNumber(Long payerAccountNumber);
    Optional<CoursePayment> findByIdempotencyKey(UUID idempotencyKey);
    boolean existsByPayerAccountNumberAndCourseCodeAndPaymentStatus(Long payerAccountNumber, String courseCode, PaymentStatus paymentStatus);
}
