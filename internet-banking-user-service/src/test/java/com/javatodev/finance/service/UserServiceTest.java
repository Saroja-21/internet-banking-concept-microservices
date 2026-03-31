package com.javatodev.finance.service;

import com.javatodev.finance.exception.EntityNotFoundException;
import com.javatodev.finance.exception.InvalidEmailException;
import com.javatodev.finance.exception.UserAlreadyRegisteredException;
import com.javatodev.finance.model.dto.Status;
import com.javatodev.finance.model.dto.User;
import com.javatodev.finance.model.dto.UserUpdateRequest;
import com.javatodev.finance.model.entity.UserEntity;
import com.javatodev.finance.model.repository.UserRepository;
import com.javatodev.finance.model.rest.response.UserResponse;
import com.javatodev.finance.service.rest.BankingCoreRestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private KeycloakUserService keycloakUserService;
    private UserRepository userRepository;
    private BankingCoreRestClient bankingCoreRestClient;
    private UserService userService;

    @BeforeEach
    void setUp() {
        keycloakUserService = mock(KeycloakUserService.class);
        userRepository = mock(UserRepository.class);
        bankingCoreRestClient = mock(BankingCoreRestClient.class);
        userService = new UserService(keycloakUserService, userRepository, bankingCoreRestClient);
    }

    @Test
    void readUser_found() {
        UserEntity entity = new UserEntity();
        entity.setId(1L);
        entity.setAuthId("auth123");
        entity.setIdentification("ID123");
        entity.setStatus(Status.APPROVED);

        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));

        User result = userService.readUser(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("auth123", result.getAuthId());
        assertEquals("ID123", result.getIdentification());
        assertEquals(Status.APPROVED, result.getStatus());
    }

    @Test
    void readUser_notFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.readUser(1L));
    }

    @Test
    void readUsers_success() {
        UserEntity entity = new UserEntity();
        entity.setId(1L);
        entity.setAuthId("auth123");
        entity.setIdentification("ID123");
        entity.setStatus(Status.APPROVED);

        List<UserEntity> entities = Collections.singletonList(entity);
        Page<UserEntity> page = new PageImpl<>(entities);

        UserRepresentation userRep = new UserRepresentation();
        userRep.setEmail("test@test.com");

        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(keycloakUserService.readUser("auth123")).thenReturn(userRep);

        List<User> result = userService.readUsers(Pageable.unpaged());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("test@test.com", result.get(0).getEmail());
    }

    @Test
    void createUser_emailAlreadyRegistered() {
        User user = new User();
        user.setEmail("existing@test.com");
        user.setIdentification("ID123");

        UserRepresentation existingUser = new UserRepresentation();
        existingUser.setEmail("existing@test.com");

        when(keycloakUserService.readUserByEmail("existing@test.com"))
                .thenReturn(Collections.singletonList(existingUser));

        assertThrows(UserAlreadyRegisteredException.class, () -> userService.createUser(user));
    }

    @Test
    void createUser_invalidEmail() {
        User user = new User();
        user.setEmail("wrong@test.com");
        user.setIdentification("ID123");

        UserResponse bankingUser = new UserResponse();
        bankingUser.setId(1);
        bankingUser.setEmail("correct@test.com");

        when(keycloakUserService.readUserByEmail("wrong@test.com")).thenReturn(Collections.emptyList());
        when(bankingCoreRestClient.readUser("ID123")).thenReturn(bankingUser);

        assertThrows(InvalidEmailException.class, () -> userService.createUser(user));
    }

    @Test
    void updateUser_success() {
        UserEntity entity = new UserEntity();
        entity.setId(1L);
        entity.setAuthId("auth123");
        entity.setStatus(Status.PENDING);

        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setStatus(Status.APPROVED);

        UserRepresentation userRep = new UserRepresentation();
        userRep.setEnabled(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(keycloakUserService.readUser("auth123")).thenReturn(userRep);
        when(userRepository.save(any(UserEntity.class))).thenReturn(entity);

        User result = userService.updateUser(1L, updateRequest);

        assertNotNull(result);
        verify(keycloakUserService).updateUser(any(UserRepresentation.class));
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void updateUser_notFound() {
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setStatus(Status.APPROVED);

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.updateUser(1L, updateRequest));
    }
}
