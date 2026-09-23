package com.samagan.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDao userDao;

    private UserService underTest;

    @BeforeEach
    void setUp() {
        underTest = new UserService(userDao);
    }

    @Test
    @DisplayName("getAllUsers delegates to the DAO")
    void itShouldGetAllUsers() {
        // Given
        List<User> users = List.of(new User(UUID.randomUUID(), "James"));
        when(userDao.getUsers()).thenReturn(users);

        // When
        List<User> result = underTest.getAllUsers();

        // Then
        assertThat(result).isEqualTo(users);
        verify(userDao).getUsers();
    }

    @Test
    @DisplayName("getUserById returns the user the DAO finds")
    void itShouldGetUserById() {
        // Given
        UUID id = UUID.randomUUID();
        User user = new User(id, "Jamila");
        when(userDao.findUserById(id)).thenReturn(user);

        // When
        User result = underTest.getUserById(id);

        // Then
        assertThat(result).isEqualTo(user);
    }

    @Test
    @DisplayName("getUserById returns null when the user does not exist")
    void itShouldReturnNullWhenUserNotFound() {
        // Given
        UUID id = UUID.randomUUID();
        when(userDao.findUserById(id)).thenReturn(null);

        // When / Then
        assertThat(underTest.getUserById(id)).isNull();
    }

    @Test
    @DisplayName("constructor rejects a null UserDao")
    void itShouldRejectNullDao() {
        assertThatThrownBy(() -> new UserService(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("UserDao cannot be null");
    }
}
