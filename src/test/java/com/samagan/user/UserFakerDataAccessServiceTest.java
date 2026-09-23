package com.samagan.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The generated data is random, so this only checks the shape of what JavaFaker
 * produces and that the lookups work against it.
 */
class UserFakerDataAccessServiceTest {

    private UserFakerDataAccessService underTest;

    @BeforeEach
    void setUp() {
        underTest = new UserFakerDataAccessService();
    }

    @Test
    @DisplayName("getUsers generates 20 users with names and unique ids")
    void itShouldGenerateTwentyUsers() {
        // When
        List<User> users = underTest.getUsers();

        // Then
        assertThat(users).hasSize(20);
        assertThat(users).allSatisfy(user -> {
            assertThat(user.getId()).isNotNull();
            assertThat(user.getName()).isNotBlank();
        });
        assertThat(users).extracting(User::getId).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("findUserById finds a generated user and returns null otherwise")
    void itShouldFindGeneratedUserById() {
        // Given
        User expected = underTest.getUsers().get(0);

        // When / Then
        assertThat(underTest.findUserById(expected.getId())).isEqualTo(expected);
        assertThat(underTest.findUserById(UUID.randomUUID())).isNull();
        assertThat(underTest.findUserById(null)).isNull();
    }
}
