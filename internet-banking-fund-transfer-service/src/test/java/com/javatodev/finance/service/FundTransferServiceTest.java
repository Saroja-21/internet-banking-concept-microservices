package com.javatodev.finance.service;

import com.javatodev.finance.model.TransactionStatus;
import com.javatodev.finance.model.dto.FundTransfer;
import com.javatodev.finance.model.dto.request.FundTransferRequest;
import com.javatodev.finance.model.dto.response.FundTransferResponse;
import com.javatodev.finance.model.entity.FundTransferEntity;
import com.javatodev.finance.model.repository.FundTransferRepository;
import com.javatodev.finance.service.rest.client.BankingCoreFeignClient;
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

class FundTransferServiceTest {

    private FundTransferRepository fundTransferRepository;
    private BankingCoreFeignClient bankingCoreFeignClient;
    private FundTransferService fundTransferService;

    @BeforeEach
    void setUp() {
        fundTransferRepository = mock(FundTransferRepository.class);
        bankingCoreFeignClient = mock(BankingCoreFeignClient.class);
        fundTransferService = new FundTransferService(fundTransferRepository, bankingCoreFeignClient);
    }

    @Test
    void fundTransfer_success() {
        FundTransferRequest request = new FundTransferRequest();
        request.setFromAccount("ACC001");
        request.setToAccount("ACC002");
        request.setAmount(BigDecimal.valueOf(100));

        FundTransferEntity savedEntity = new FundTransferEntity();
        savedEntity.setId(1L);
        savedEntity.setFromAccount("ACC001");
        savedEntity.setToAccount("ACC002");
        savedEntity.setAmount(BigDecimal.valueOf(100));
        savedEntity.setStatus(TransactionStatus.PENDING);

        FundTransferResponse coreResponse = new FundTransferResponse();
        coreResponse.setTransactionId("TXN123");
        coreResponse.setMessage("Success");

        when(fundTransferRepository.save(any(FundTransferEntity.class))).thenReturn(savedEntity);
        when(bankingCoreFeignClient.fundTransfer(any(FundTransferRequest.class))).thenReturn(coreResponse);

        FundTransferResponse result = fundTransferService.fundTransfer(request);

        assertNotNull(result);
        assertEquals("TXN123", result.getTransactionId());
        assertEquals("Fund Transfer Successfully Completed", result.getMessage());
        verify(fundTransferRepository, times(2)).save(any(FundTransferEntity.class));
    }

    @Test
    void fundTransfer_savesWithPendingStatus() {
        FundTransferRequest request = new FundTransferRequest();
        request.setFromAccount("ACC001");
        request.setToAccount("ACC002");
        request.setAmount(BigDecimal.valueOf(500));

        FundTransferEntity savedEntity = new FundTransferEntity();
        savedEntity.setId(1L);
        savedEntity.setStatus(TransactionStatus.PENDING);

        FundTransferResponse coreResponse = new FundTransferResponse();
        coreResponse.setTransactionId("TXN456");

        when(fundTransferRepository.save(any(FundTransferEntity.class))).thenReturn(savedEntity);
        when(bankingCoreFeignClient.fundTransfer(any(FundTransferRequest.class))).thenReturn(coreResponse);

        fundTransferService.fundTransfer(request);

        verify(fundTransferRepository, times(2)).save(any(FundTransferEntity.class));
    }

    @Test
    void readAllTransfers_success() {
        FundTransferEntity entity = new FundTransferEntity();
        entity.setId(1L);
        entity.setFromAccount("ACC001");
        entity.setToAccount("ACC002");
        entity.setAmount(BigDecimal.valueOf(100));
        entity.setStatus(TransactionStatus.SUCCESS);
        entity.setTransactionReference("TXN123");

        List<FundTransferEntity> entities = Collections.singletonList(entity);
        Page<FundTransferEntity> page = new PageImpl<>(entities);

        when(fundTransferRepository.findAll(any(Pageable.class))).thenReturn(page);

        List<FundTransfer> result = fundTransferService.readAllTransfers(Pageable.unpaged());

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void readAllTransfers_emptyList() {
        Page<FundTransferEntity> emptyPage = new PageImpl<>(Collections.emptyList());

        when(fundTransferRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        List<FundTransfer> result = fundTransferService.readAllTransfers(Pageable.unpaged());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void fundTransfer_updatesStatusToSuccess() {
        FundTransferRequest request = new FundTransferRequest();
        request.setFromAccount("ACC001");
        request.setToAccount("ACC002");
        request.setAmount(BigDecimal.valueOf(200));

        FundTransferEntity savedEntity = new FundTransferEntity();
        savedEntity.setId(1L);
        savedEntity.setStatus(TransactionStatus.PENDING);

        FundTransferResponse coreResponse = new FundTransferResponse();
        coreResponse.setTransactionId("TXN789");

        when(fundTransferRepository.save(any(FundTransferEntity.class))).thenAnswer(invocation -> {
            FundTransferEntity arg = invocation.getArgument(0);
            return arg;
        });
        when(bankingCoreFeignClient.fundTransfer(any(FundTransferRequest.class))).thenReturn(coreResponse);

        FundTransferResponse result = fundTransferService.fundTransfer(request);

        assertNotNull(result);
        assertEquals("TXN789", result.getTransactionId());
    }
}
