package com.javatodev.finance.service;

import com.javatodev.finance.model.TransactionStatus;
import com.javatodev.finance.model.dto.UtilityPayment;
import com.javatodev.finance.model.entity.UtilityPaymentEntity;
import com.javatodev.finance.model.rest.request.UtilityPaymentRequest;
import com.javatodev.finance.model.rest.response.UtilityPaymentResponse;
import com.javatodev.finance.repository.UtilityPaymentRepository;
import com.javatodev.finance.service.rest.BankingCoreRestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UtilityPaymentServiceTest {

    private UtilityPaymentRepository utilityPaymentRepository;
    private BankingCoreRestClient bankingCoreRestClient;
    private UtilityPaymentService utilityPaymentService;

    @BeforeEach
    void setUp() {
        utilityPaymentRepository = mock(UtilityPaymentRepository.class);
        bankingCoreRestClient = mock(BankingCoreRestClient.class);
        utilityPaymentService = new UtilityPaymentService(utilityPaymentRepository, bankingCoreRestClient);
    }

    @Test
    void utilPayment_success() {
        UtilityPaymentRequest request = new UtilityPaymentRequest();
        request.setProviderId(1L);
        request.setAmount(BigDecimal.valueOf(50));
        request.setReferenceNumber("REF001");
        request.setAccount("ACC001");

        UtilityPaymentEntity savedEntity = new UtilityPaymentEntity();
        savedEntity.setId(1L);
        savedEntity.setProviderId(1L);
        savedEntity.setAmount(BigDecimal.valueOf(50));
        savedEntity.setStatus(TransactionStatus.PROCESSING);

        UtilityPaymentResponse coreResponse = UtilityPaymentResponse.builder()
                .transactionId("TXN123")
                .message("Success")
                .build();

        when(utilityPaymentRepository.save(any(UtilityPaymentEntity.class))).thenReturn(savedEntity);
        when(bankingCoreRestClient.utilityPayment(any(UtilityPaymentRequest.class))).thenReturn(coreResponse);

        UtilityPaymentResponse result = utilityPaymentService.utilPayment(request);

        assertNotNull(result);
        assertEquals("TXN123", result.getTransactionId());
        assertEquals("Utility Payment Successfully Processed", result.getMessage());
        verify(utilityPaymentRepository, times(2)).save(any(UtilityPaymentEntity.class));
    }

    @Test
    void utilPayment_savesWithProcessingStatus() {
        UtilityPaymentRequest request = new UtilityPaymentRequest();
        request.setProviderId(2L);
        request.setAmount(BigDecimal.valueOf(100));
        request.setReferenceNumber("REF002");
        request.setAccount("ACC002");

        UtilityPaymentEntity savedEntity = new UtilityPaymentEntity();
        savedEntity.setId(1L);
        savedEntity.setStatus(TransactionStatus.PROCESSING);

        UtilityPaymentResponse coreResponse = UtilityPaymentResponse.builder()
                .transactionId("TXN456")
                .build();

        when(utilityPaymentRepository.save(any(UtilityPaymentEntity.class))).thenReturn(savedEntity);
        when(bankingCoreRestClient.utilityPayment(any(UtilityPaymentRequest.class))).thenReturn(coreResponse);

        utilityPaymentService.utilPayment(request);

        verify(utilityPaymentRepository, times(2)).save(any(UtilityPaymentEntity.class));
    }

    @Test
    void readPayments_success() {
        UtilityPaymentEntity entity = new UtilityPaymentEntity();
        entity.setId(1L);
        entity.setProviderId(1L);
        entity.setAmount(BigDecimal.valueOf(75));
        entity.setReferenceNumber("REF003");
        entity.setAccount("ACC003");
        entity.setStatus(TransactionStatus.SUCCESS);
        entity.setTransactionId("TXN789");

        List<UtilityPaymentEntity> entities = Collections.singletonList(entity);
        Page<UtilityPaymentEntity> page = new PageImpl<>(entities);

        when(utilityPaymentRepository.findAll(any(Pageable.class))).thenReturn(page);

        List<UtilityPayment> result = utilityPaymentService.readPayments(Pageable.unpaged());

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void readPayments_emptyList() {
        Page<UtilityPaymentEntity> emptyPage = new PageImpl<>(Collections.emptyList());

        when(utilityPaymentRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        List<UtilityPayment> result = utilityPaymentService.readPayments(Pageable.unpaged());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void utilPayment_updatesStatusToSuccess() {
        UtilityPaymentRequest request = new UtilityPaymentRequest();
        request.setProviderId(3L);
        request.setAmount(BigDecimal.valueOf(200));
        request.setReferenceNumber("REF004");
        request.setAccount("ACC004");

        UtilityPaymentEntity savedEntity = new UtilityPaymentEntity();
        savedEntity.setId(1L);
        savedEntity.setStatus(TransactionStatus.PROCESSING);

        UtilityPaymentResponse coreResponse = UtilityPaymentResponse.builder()
                .transactionId("TXN999")
                .build();

        when(utilityPaymentRepository.save(any(UtilityPaymentEntity.class))).thenAnswer(invocation -> {
            UtilityPaymentEntity arg = invocation.getArgument(0);
            return arg;
        });
        when(bankingCoreRestClient.utilityPayment(any(UtilityPaymentRequest.class))).thenReturn(coreResponse);

        UtilityPaymentResponse result = utilityPaymentService.utilPayment(request);

        assertNotNull(result);
        assertEquals("TXN999", result.getTransactionId());
    }

    @Test
    void utilPayment_setsTransactionIdFromResponse() {
        UtilityPaymentRequest request = new UtilityPaymentRequest();
        request.setProviderId(1L);
        request.setAmount(BigDecimal.valueOf(150));
        request.setReferenceNumber("REF005");
        request.setAccount("ACC005");

        UtilityPaymentEntity savedEntity = new UtilityPaymentEntity();
        savedEntity.setId(1L);

        UtilityPaymentResponse coreResponse = UtilityPaymentResponse.builder()
                .transactionId("UNIQUE-TXN-ID")
                .build();

        when(utilityPaymentRepository.save(any(UtilityPaymentEntity.class))).thenReturn(savedEntity);
        when(bankingCoreRestClient.utilityPayment(any(UtilityPaymentRequest.class))).thenReturn(coreResponse);

        UtilityPaymentResponse result = utilityPaymentService.utilPayment(request);

        assertEquals("UNIQUE-TXN-ID", result.getTransactionId());
    }
}
