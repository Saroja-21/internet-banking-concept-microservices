package com.javatodev.finance.service;

import com.javatodev.finance.exception.EntityNotFoundException;
import com.javatodev.finance.model.dto.User;
import com.javatodev.finance.model.entity.UserEntity;
import com.javatodev.finance.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new UserService(userRepository);
    }

    private UserEntity createUser() {
        UserEntity entity = new UserEntity();
        entity.setId(1L);
        entity.setIdentificationNumber("ID123");
        entity.setFirstName("John");
        entity.setLastName("Doe");
        entity.setEmail("john.doe@example.com");
        entity.setAccounts(new ArrayList<>());
        return entity;
    }

    @Test
    void readUser_found() {
        UserEntity entity = createUser();
        when(userRepository.findByIdentificationNumber("ID123"))
                .thenReturn(Optional.of(entity));

        User result = userService.readUser("ID123");

        assertNotNull(result);
    }

    @Test
    void readUser_notFound() {
        when(userRepository.findByIdentificationNumber("ID123"))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> userService.readUser("ID123"));
    }

    @Test
    void readUsers_success() {
        UserEntity entity = createUser();
        Page<UserEntity> page =
                new PageImpl<>(Collections.singletonList(entity));

        when(userRepository.findAll(any(Pageable.class)))
                .thenReturn(page);

        List<User> result = userService.readUsers(PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
