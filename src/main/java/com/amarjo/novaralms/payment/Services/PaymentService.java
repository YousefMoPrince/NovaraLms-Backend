package com.amarjo.novaralms.payment.Services;

import com.amarjo.novaralms.auth.Services.SessionService;
import com.amarjo.novaralms.auth.model.users;
import com.amarjo.novaralms.auth.repository.UserRepo;
import com.amarjo.novaralms.course.model.Courses;
import com.amarjo.novaralms.course.model.EnrollmentStatus;
import com.amarjo.novaralms.course.model.Enrollments;
import com.amarjo.novaralms.course.repo.CourseRepo;
import com.amarjo.novaralms.course.repo.EnrollmentRepo;
import com.amarjo.novaralms.payment.DTO.*;
import com.amarjo.novaralms.payment.Model.*;
import com.amarjo.novaralms.payment.Repo.MainServerRepo;
import com.amarjo.novaralms.payment.Repo.MainUserRepo;
import com.amarjo.novaralms.payment.Repo.PaymentRepo;
import com.amarjo.novaralms.payment.Repo.ServerTransactionsRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class PaymentService {
    @Autowired
    private PaymentRepo paymentRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private MainServerRepo mainServerRepo;
    @Autowired
    private MainUserRepo mainUserRepo;
    @Autowired
    private EnrollmentRepo enrollmentRepo;
    @Autowired
    private CourseRepo courseRepo;
    @Autowired
    private ServerTransactionsRepo serverTransactionsRepo;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private WebClient payOnWebClient;

    private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.05");

    private String userAuth(Character c){
        String email;
        if (c == 'W') {
            if (sessionService.isSessionActive()) {
                email = sessionService.getEmail();
            } else {
                throw new RuntimeException("Unauthorized: No active Web Session found");
            }
        } else {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                email = auth.getName();
            } else {
                throw new RuntimeException("Unauthorized: Invalid or missing Mobile JWT");
            }
        }
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Unauthorized: Invalid or missing Auth");
        }
        return email;
    }

    public void validateUser(Character c, Long accountNumber) {
        String email = userAuth(c);
        users user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        try {
            Boolean isValidInPayOn = payOnWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/onPay/verify/{accountNumber}")
                            .queryParam("email", email)
                            .build(accountNumber))
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();

            if (isValidInPayOn == null || !isValidInPayOn) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account verification failed in PayOn Gateway");
            }

            user.setAccountNumber(accountNumber);
            user.setIsReadyForPayment(true);
            userRepo.save(user);

        } catch (WebClientResponseException e) {
            log.error("PayOn Gateway returned an error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ResponseStatusException(e.getStatusCode(), "PayOn Gateway Error: " + e.getStatusText());

        } catch (ResponseStatusException e) {
            throw e;

        } catch (Exception e) {
            log.error("Unexpected error during communication with PayOn", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error during payment validation");
        }
    }
    @Transactional
    public void receiveInfo(EnterpriseResponse response){
        try {
            if (response != null && response.getData() != null) {

                boolean isEnterpriseMerchant = response.getCompanyRole().equals("ENTERPRISE_MERCHANT_ACTIVE");
                boolean isActiveStatus = response.getData().getCompanyStatus().equals("ACTIVE");

                if (isEnterpriseMerchant && isActiveStatus) {
                    MainServerDB info = MainServerDB
                            .builder()
                            .companyName(response.getData().getCompanyName())
                            .companyEmail(response.getData().getCompanyEmail())
                            .companyDial(response.getData().getCompanyDial())
                            .companyPassword(response.getData().getCompanyPassword())
                            .companyRole(response.getCompanyRole())
                            .companyType(response.getData().getCompanyType())
                            .companyStatus(response.getData().getCompanyStatus())
                            .companyTaxId(response.getData().getCompanyTaxId())
                            .companyAccountNumber(response.getData().getCompanyAccountNumber())
                            .companyBalance(response.getData().getCompanyBalance())
                            .companyApiKey(response.getData().getCompanyApiKey())
                            .companyApiSecret(response.getData().getCompanyApiSecret())
                            .companyUrl(response.getData().getCompanyUrl())
                            .createdAt(response.getData().getCreatedAt())
                            .updatedAt(response.getData().getUpdatedAt())
                            .build();
                    mainServerRepo.save(info);
                }
            }
        }catch (Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }
    @Transactional(isolation = Isolation.REPEATABLE_READ,propagation = Propagation.REQUIRED)
    public ReceiptEventResponse createReceipt(Character c, String courseCode) {
        String email = userAuth(c);
        users user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        if (!user.getIsReadyForPayment()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not ready for payment");
        }
            MainServerDB info = mainServerRepo.findFirstBy()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Main Server info not found"));
            Enrollments enrollment = enrollmentRepo.findByStudentAndEnrollmentStatusAndCourseCode(user, EnrollmentStatus.PENDING,courseCode)
                    .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "Enrollment not found"));
            Courses course = courseRepo.findByCourseCode(enrollment.getCourseCode())
                    .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
            UUID idempotencyKey = UUID.randomUUID();
            ReceiptRequest receiptData = ReceiptRequest
                    .builder()
                    .state(PaymentStatus.PENDING)
                    .data(ReceiptRequest.ReceiptDataRequest
                            .builder()
                            .amount(course.getPrice())
                            .clientName(enrollment.getStudent().getName())
                            .accountNumber(info.getCompanyAccountNumber())
                            .currency(course.getCurrency())
                            .idempotencyKey(idempotencyKey)
                            .build())
                    .build();
            ReceiptEventResponse response;
            try {
                response = payOnWebClient.post()
                        .uri(uriBuilder -> uriBuilder
                                .path("/api/v1/enterprise/merchant/receipts/create")
                                .build())
                        .header("X-PAYON-API-KEY", info.getCompanyApiKey())
                        .header("X-PAYON-API-SECRET", info.getCompanyApiSecret())
                        .bodyValue(receiptData)
                        .retrieve()
                        .bodyToMono(ReceiptEventResponse.class)
                        .block();
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to communicate with Payment Gateway: " + e.getMessage(), e);
            }
            if (response == null || response.getData() == null) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid response from Payment Service");
            }
            enrollment.setEnrollmentStatus(EnrollmentStatus.PROCESSING);
            enrollmentRepo.save(enrollment);
            CoursePayment coursePayment = CoursePayment
                    .builder()
                    .courseCode(courseCode)
                    .accountNumber(info.getCompanyAccountNumber())
                    .payerAccountNumber(user.getAccountNumber())
                    .paymentStatus(PaymentStatus.PENDING)
                    .amount(course.getPrice())
                    .currency(course.getCurrency())
                    .paymentCode(response.getData().getTransactionCode())
                    .idempotencyKey(idempotencyKey)
                    .build();
            paymentRepo.save(coursePayment);
            return response;

    }
    @Transactional(isolation = Isolation.REPEATABLE_READ,propagation = Propagation.REQUIRED)
    public void paymentAcknowledged(PayOnAckRequest request) {
        MainServerDB serverInfo = mainServerRepo.findFirstBy()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Main Server info not found"));
        MainUserDB userInfo = mainUserRepo.findFirstBy()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Main user info not found"));
        CoursePayment payment = paymentRepo.findByIdempotencyKey(request.getData().getIdempotencyKey())
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
        users student = userRepo.findByAccountNumber(Long.valueOf(request.getData().getPayerAccountNumber()))
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
        Courses course = courseRepo.findByCourseCode(payment.getCourseCode())
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
        users instructor = userRepo.findByCode(course.getInstructorCode())
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "Instructor not found"));
        if (!payment.getPaymentStatus().equals(PaymentStatus.PENDING)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment not ready for acknowledgement");
        }
        if (!student.getAccountNumber().equals(payment.getPayerAccountNumber())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "that's not your payment account");
        }
        Enrollments enroll = enrollmentRepo.findByStudentANDCourseCodeAndEnrollmentStatusForUpdate(student, course.getCourseCode(), EnrollmentStatus.PROCESSING)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "Enrollment not found"));
        enroll.setEnrollmentStatus(EnrollmentStatus.ACTIVE);
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        paymentRepo.save(payment);
        enrollmentRepo.save(enroll);
        TransferResponse responseForInstructor;
        TransferResponse responseForMainUser;
        try {
            BigDecimal taxForMainUser = course.getPrice().multiply(COMMISSION_RATE).setScale(2, RoundingMode.HALF_UP);
            BigDecimal netAmountForInstructor = course.getPrice().subtract(taxForMainUser);
            UUID idempotency1 = UUID.randomUUID();
            UUID idempotency2 = UUID.randomUUID();
            TransferRequest transferRequestForInstructor = TransferRequest
                    .builder()
                    .amount(netAmountForInstructor)
                    .idempotencyKey(idempotency1.toString())
                    .receiverAccountNumber(instructor.getAccountNumber())
                    .build();
            TransferRequest transferRequestForMainUser = TransferRequest
                    .builder()
                    .amount(taxForMainUser)
                    .idempotencyKey(idempotency2.toString())
                    .receiverAccountNumber(userInfo.getMainUserAccountNumber())
                    .build();
            responseForInstructor = payOnWebClient.post()
                        .uri(uriBuilder -> uriBuilder
                                .path("/api/v1/enterprise/merchant/transfer")
                                .build())
                        .header("X-PAYON-API-KEY", serverInfo.getCompanyApiKey())
                        .header("X-PAYON-API-SECRET", serverInfo.getCompanyApiSecret())
                        .bodyValue(transferRequestForInstructor)
                        .retrieve()
                        .bodyToMono(TransferResponse.class)
                        .block();
            responseForMainUser = payOnWebClient.post()
                        .uri(uriBuilder -> uriBuilder
                                .path("/api/v1/enterprise/merchant/transfer")
                                .build())
                        .header("X-PAYON-API-KEY", serverInfo.getCompanyApiKey())
                        .header("X-PAYON-API-SECRET", serverInfo.getCompanyApiSecret())
                        .bodyValue(transferRequestForMainUser)
                        .retrieve()
                        .bodyToMono(TransferResponse.class)
                        .block();
            ServerTransactions transactionForInstructor = ServerTransactions
                    .builder()
                    .serverTransactionAmount(netAmountForInstructor)
                    .serverTransactionAccountNumber(serverInfo.getCompanyAccountNumber().toString())
                    .serverTransactionType(responseForInstructor.getType())
                    .serverTransactionStatus(responseForInstructor.getStatus())
                    .serverTransactionIdempotencyKey(idempotency1)
                    .serverTransactionReceiverAccountNumber(instructor.getAccountNumber().toString())
                    .serverTransactionTimeStamp(LocalDateTime.now())
                    .build();
            ServerTransactions transactionForMainUser = ServerTransactions
                    .builder()
                    .serverTransactionAmount(taxForMainUser)
                    .serverTransactionAccountNumber(serverInfo.getCompanyAccountNumber().toString())
                    .serverTransactionType(responseForMainUser.getType())
                    .serverTransactionStatus(responseForMainUser.getStatus())
                    .serverTransactionIdempotencyKey(idempotency2)
                    .serverTransactionReceiverAccountNumber(userInfo.getMainUserAccountNumber().toString())
                    .serverTransactionTimeStamp(LocalDateTime.now())
                    .build();
            serverTransactionsRepo.save(transactionForInstructor);
            serverTransactionsRepo.save(transactionForMainUser);
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to communicate with Payment Gateway: " + e.getMessage(), e);
            }
        }

    }

