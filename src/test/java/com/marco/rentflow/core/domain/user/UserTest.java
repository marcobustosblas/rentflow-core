package com.marco.rentflow.core.domain.user;

import com.marco.rentflow.core.domain.user.exception.UserNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User Aggregate Root Domain Tests")
public class UserTest {

    @Nested
    @DisplayName("Constructor and Instantiation Tests")
    class InstantiationTests {

        @Test
        @DisplayName("Should create new user with auto-generated UUID and default ACTIVE status")
        void shouldCreateNewUserSuccessfully() {
            User user = User.registerNew(
                    "marco@rentflow.com", "hash123",
                    "Marco Bustos", "12345678-9", "+56912345678", Role.LANDLORD);
            assertNotNull(user.getId());
            assertEquals("marco@rentflow.com", user.getEmail());
            assertEquals("hash123", user.getPasswordHash());
            assertEquals("Marco Bustos", user.getFullName());
            assertEquals("+56912345678", user.getPhoneNumber());
            assertEquals(UserStatus.ACTIVE, user.getStatus());
            assertEquals(1, user.getRoles().size());
            assertTrue(user.isLandlord());
            assertFalse(user.isTenant());
            assertNotNull(user.getCreatedAt());
            assertNotNull(user.getUpdatedAt());
        }

        @Test
        @DisplayName("Should reconstitute existing user with specific UUID and full attributes")
        void shouldReconstituteExistingUserSuccessfully() {
            UUID id = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();
            Set<Role> roles = Set.of(Role.LANDLORD, Role.TENANT);

            User user = User.reconstitute(id, "marco@rentflow.com", "hash123",
                    "Marco Bustos", "12345678-9", "+56912345678", roles, UserStatus.BLOCKED, now, now);

            assertEquals(id, user.getId());
            assertEquals(UserStatus.BLOCKED, user.getStatus());
            assertEquals(2, user.getRoles().size());
            assertTrue(user.isLandlord());
            assertTrue(user.isTenant());
        }

        @Test
        @DisplayName("Should throw NullPointerException when required fields in constructor are null")
        void shouldThrowExceptionWhenRequiredFieldsAreNull() {
            assertThrows(NullPointerException.class, () -> User.registerNew(null, "hash", "Name", "12345678-9", "123", Role.LANDLORD));
            assertThrows(NullPointerException.class, () -> User.registerNew("email@test.com", null, "Name", "12345678-9", "123", Role.LANDLORD));
            assertThrows(NullPointerException.class, () -> User.registerNew("email@test.com", "hash", null, "12345678-9",  "123", Role.LANDLORD));
            assertThrows(NullPointerException.class, () -> User.registerNew("email@test.com", "hash", "Name", "12345678-9", "123", (Role) null));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when roles set is empty in full constructor")
        void shouldThrowExceptionWhenRolesSetIsEmpty() {
            UUID id = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();

            assertThrows(IllegalArgumentException.class, () ->
                    User.reconstitute(id, "email@test.com", "hash", "Name", "12345678-9", "123", Set.of(), UserStatus.ACTIVE, now, now)
            );
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when rut does not match regex")
        void shouldThrowExceptionWhenRutDoesNotMatchRegex() {
            assertThrows(IllegalArgumentException.class, () -> {
                User.registerNew("marco@rentflow.com", "hash123", "Marco Bustos", "123456789", "+56912345678", Role.LANDLORD);
            });
        }

        @ParameterizedTest
        @NullAndEmptySource // Prueba automáticamente con null y con ""
        @ValueSource(strings = {"   ", "      "}) // Prueba con espacios en blanco
        @DisplayName("Should allow creating a user and assign null when RUT is missing or blank")
        void shouldAssignNullWhenRutIsBlank(String invalidRut) {
            // invalidRut tomará los valores: null, "", "   ", "      "
            User user = User.registerNew("marco@rentflow.com", "hash123", "Marco Bustos", invalidRut, "+56912345678", Role.LANDLORD);

            // Verificamos que el dominio perdonó el espacio vacío/nulo y lo transformó en null
            assertNull(user.getRut());
            assertEquals("marco@rentflow.com", user.getEmail());
        }
    }

    @Nested
    @DisplayName("Email, Password, and Name Mutation Tests")
    class AttributeMutationTests {

        @Test
        @DisplayName("Should update email successfully when valid")
        void shouldChangeEmailSuccessfully() {
            User user = User.registerNew("marco@rentflow.com", "hash123", "Marco Bustos", "12345678-9", "+56912345678", Role.LANDLORD);

            user.changeEmail("nuevo@rentflow.com");

            assertEquals("nuevo@rentflow.com", user.getEmail());
        }

        @Test
        @DisplayName("Should throw exception when new email is null or empty")
        void shouldThrowExceptionWhenEmailIsInvalid() {
            User user = User.registerNew("marco@rentflow.com", "hash123", "Marco Bustos", "12345678-9", "+56912345678", Role.LANDLORD);

            assertThrows(NullPointerException.class, () -> user.changeEmail(null));
            assertThrows(IllegalArgumentException.class, () -> user.changeEmail(""));
            assertThrows(IllegalArgumentException.class, () -> user.changeEmail("   "));
        }

        @Test
        @DisplayName("Should update password hash successfully when valid")
        void shouldChangePasswordSuccessfully() {
            User user = User.registerNew("marco@rentflow.com", "hash123", "Marco Bustos", "12345678-9", "+56912345678", Role.LANDLORD);

            user.changePassword("newhash456");

            assertEquals("newhash456", user.getPasswordHash());
        }

        @Test
        @DisplayName("Should throw exception when new password hash is null or empty")
        void shouldThrowExceptionWhenPasswordIsInvalid() {
            User user = User.registerNew("marco@rentflow.com", "hash123", "Marco Bustos", "12345678-9", "+56912345678", Role.LANDLORD);

            assertThrows(NullPointerException.class, () -> user.changePassword(null));
            assertThrows(IllegalArgumentException.class, () -> user.changePassword(""));
            assertThrows(IllegalArgumentException.class, () -> user.changePassword("   "));
        }

        @Test
        @DisplayName("Should update full name successfully when valid")
        void shouldUpdateFullNameSuccessfully() {
            User user = User.registerNew("marco@rentflow.com", "hash123", "Marco Bustos", "12345678-9", "+56912345678", Role.LANDLORD);

            user.updateFullName("Marco Orlando Bustos");

            assertEquals("Marco Orlando Bustos", user.getFullName());
        }

        @Test
        @DisplayName("Should throw exception when new full name is null or empty")
        void shouldThrowExceptionWhenFullNameIsInvalid() {
            User user = User.registerNew("marco@rentflow.com", "hash123", "Marco Bustos", "12345678-9", "+56912345678", Role.LANDLORD);

            assertThrows(NullPointerException.class, () -> user.updateFullName(null));
            assertThrows(IllegalArgumentException.class, () -> user.updateFullName(""));
            assertThrows(IllegalArgumentException.class, () -> user.updateFullName("   "));
        }
    }

    @Nested
    @DisplayName("Status State Transition Tests")
    class StatusTransitionTests {

        @Test
        @DisplayName("Should transition states between ACTIVE, INACTIVE, and BLOCKED")
        void shouldTransitionStatesCorrectly() {
            User user = User.registerNew("marco@rentflow.com", "hash123", "Marco Bustos", "12345678-9", "+56912345678", Role.LANDLORD);

            user.block();
            assertEquals(UserStatus.BLOCKED, user.getStatus());

            user.activate();
            assertEquals(UserStatus.ACTIVE, user.getStatus());

            user.deactivate();
            assertEquals(UserStatus.INACTIVE, user.getStatus());
        }
    }

    @Nested
    @DisplayName("Role Management Tests")
    class RoleManagementTests {

        @Test
        @DisplayName("Should add new role successfully")
        void shouldAddRoleSuccessfully() {
            User user = User.registerNew("marco@rentflow.com", "hash123", "Marco Bustos", "12345678-9", "+56912345678", Role.TENANT);

            user.addRole(Role.LANDLORD);

            assertTrue(user.isTenant());
            assertTrue(user.isLandlord());
            assertEquals(2, user.getRoles().size());
        }

        @Test
        @DisplayName("Should remove role successfully when user has more than one role")
        void shouldRemoveRoleSuccessfully() {
            User user = User.registerNew("marco@rentflow.com", "hash123",
                    "Marco Bustos", "12345678-9", "+56912345678", Role.TENANT);
            user.addRole(Role.LANDLORD);

            user.removeRole(Role.TENANT);

            assertFalse(user.isTenant());
            assertTrue(user.isLandlord());
            assertEquals(1, user.getRoles().size());
        }

        @Test
        @DisplayName("Should throw IllegalStateException when attempting to remove the last remaining role")
        void shouldThrowExceptionWhenRemovingLastRole() {
            User user = User.registerNew("marco@rentflow.com", "hash123",
                    "Marco Bustos", "12345678-9", "+56912345678", Role.LANDLORD);

            assertThrows(IllegalStateException.class, () -> user.removeRole(Role.LANDLORD));
        }

        @Test
        @DisplayName("Should return unmodifiable set of roles to protect encapsulation")
        void shouldReturnUnmodifiableRoleSet() {
            User user = User.registerNew("marco@rentflow.com", "hash123",
                    "Marco Bustos", "12345678-9", "+56912345678", Role.LANDLORD);

            assertThrows(UnsupportedOperationException.class, () -> user.getRoles().add(Role.ADMIN));
        }

        @Test
        @DisplayName("Should do nothing or handle gracefully when attempting to remove a role the user does not possess")
        void shouldNotRemoveNonExistentRoleWhenSizeIsOne() {
            User user = User.registerNew("marco@rentflow.com", "hash123", "Marco Bustos", "12345678-9", "+56912345678", Role.LANDLORD);

            // El usuario tiene LANDLORD, pero intento remover TENANT.
            // El size es 1, pero contains(TENANT) es false. No debe lanzar excepción, simplemente no hace nada.
            user.removeRole(Role.TENANT);

            assertEquals(1, user.getRoles().size());
            assertTrue(user.isLandlord());
        }
    }

    @Test
    @DisplayName("Should change phone number successfully")
    void shouldChangePhoneNumberSuccessfully() {
        User user = User.registerNew("marco@rentflow.com", "hash123", "Marco Bustos", "12345678-9", "+56912345678", Role.LANDLORD);

        user.changePhoneNumber("+56987654321"); // Asumiendo que creaste este método

        assertEquals("+56987654321", user.getPhoneNumber());
    }

    @Test
    @DisplayName("Should instantiate domain exceptions")
    void shouldInstantiateExceptions() {
        assertNotNull(new UserNotFoundException(UUID.randomUUID()));
        assertNotNull(new UserNotFoundException("marco@rentflow.com"));
    }

    @Test
    @DisplayName("Should update RUT successfully using updateRut method")
    void shouldUpdateRutSuccessfully() {
        User user = User.registerNew("marco@rentflow.com", "hash123", "Marco Bustos", null, "+56912345678", Role.LANDLORD);
        user.updateRut("18765432-1");
        assertEquals("18765432-1", user.getRut());
    }

    @Test
    @DisplayName("Should throw exception when updateRut receives null or empty")
    void shouldThrowExceptionWhenUpdateRutIsInvalid() {
        User user = User.registerNew("marco@rentflow.com", "hash123", "Marco Bustos", null, "+56912345678", Role.LANDLORD);
        assertThrows(NullPointerException.class, () -> user.updateRut(null));
        assertThrows(IllegalArgumentException.class, () -> user.updateRut("   "));
        assertThrows(IllegalArgumentException.class, () -> user.updateRut("123456789"));
    }

    @Test
    @DisplayName("Should throw exception when phone number is null or empty")
    void shouldThrowExceptionWhenPhoneNumberIsInvalid() {
        User user = User.registerNew("marco@rentflow.com", "hash123", "Marco Bustos", "12345678-9", "+56912345678", Role.LANDLORD);
        assertThrows(NullPointerException.class, () -> user.changePhoneNumber(null));
        assertThrows(IllegalArgumentException.class, () -> user.changePhoneNumber(""));
        assertThrows(IllegalArgumentException.class, () -> user.changePhoneNumber("   "));
    }

    @Test
    @DisplayName("Should reconstitute User with and without RUT (Coverage)")
    void shouldReconstituteWithAndWithoutRut() {
        //Rama TRUE (Con RUT)
        User userWithRut = User.reconstitute(
                UUID.randomUUID(), "a@a.com", "hash", "Marco",
                "12345678-9", "123", Set.of(Role.TENANT), UserStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now());
        assertNotNull(userWithRut.getRut());

        //Rama FALSE (Sin RUT, pasamos null)
        User userWithoutRut = User.reconstitute(
                UUID.randomUUID(), "b@b.com", "hash", "Pedro",
                null, "123", Set.of(Role.TENANT), UserStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now());
        assertNull(userWithoutRut.getRut());
    }

}
