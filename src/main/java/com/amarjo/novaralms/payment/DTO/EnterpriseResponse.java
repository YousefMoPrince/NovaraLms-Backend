package com.amarjo.novaralms.payment.DTO;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@Builder
public class EnterpriseResponse {
    private String companyRole;
    private EnterpriseDataResponse data;
    @Data
    @Builder
    public static class EnterpriseDataResponse {
        private String companyName;
        private String companyEmail;
        private String companyDial;
        private String companyPassword;
        private String companyType;
        private String companyStatus;
        private String companyTaxId;
        private Long companyAccountNumber;
        private BigDecimal companyBalance;
        private String companyApiKey;
        private String companyApiSecret;
        private String companyUrl;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
