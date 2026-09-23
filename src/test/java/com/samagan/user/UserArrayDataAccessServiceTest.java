package com.samagan.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserArrayDataAccessServiceTest {

    private static final UUID JAMES_ID = UUID.fromString("8ca51d2b-aa40-42cb-b73a-46329e3423b5");
    private static final UUID JAMILA_ID = UUID.fromString("b10d126a-3608-4980-9819-ac1b0587e471");

    private UserArrayDataAccessService underTest;

    @BeforeEach
    void setUp() {
        underTest = new UserArrayDataAccessService();
    }

    @Test
    @DisplayName("getUsers loads the seed users from users.csv on the classpath")
    void itShouldGetAllUsers() {
        // When
        List<User> users = underTest.getUsers();

        // Then
        assertThat(users)
                .hasSize(2)
                .extracting(User::getName)
                .containsExactly("James", "Jamila");
    }

    @Test
    @DisplayName("findUserById returns the matching user")
    void itShouldFindUserById() {
        // When
        User user = underTest.findUserById(JAMILA_ID);

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(JAMILA_ID);
        assertThat(user.getName()).isEqualTo("Jamila");
    }

    @Test
    @DisplayName("findUserById returns null for an unknown id")
    void itShouldReturnNullForUnknownId() {
        assertThat(underTest.findUserById(UUID.randomUUID())).isNull();
    }

    @Test
    @DisplayName("findUserById returns null for a null id")
    void itShouldReturnNullForNullId() {
        assertThat(underTest.findUserById(null)).isNull();
    }

    @Test
    @DisplayName("the returned list is a defensive copy")
    void itShouldReturnAnImmutableList() {
        List<User> users = underTest.getUsers();
        assertThat(users).isUnmodifiable();
        assertThat(underTest.findUserById(JAMES_ID)).isNotNull();
    }
}
