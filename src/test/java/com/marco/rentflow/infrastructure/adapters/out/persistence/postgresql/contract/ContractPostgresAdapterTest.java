package com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.contract;

import com.marco.rentflow.core.domain.common.Currency;
import com.marco.rentflow.core.domain.common.Money;
import com.marco.rentflow.core.domain.contract.ContractStatus;
import com.marco.rentflow.core.domain.contract.RentalContract;
import com.marco.rentflow.core.domain.property.Property;
import com.marco.rentflow.core.domain.user.Role;
import com.marco.rentflow.core.domain.user.User;
import com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.AbstractIntegrationTest;
import com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.property.PropertyPostgresAdapter;
import com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.user.UserPostgresAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
public class ContractPostgresAdapterTest extends AbstractIntegrationTest {

    @Autowired
    private ContractPostgresAdapter contractPostgresAdapter;

    // se agrego esto para persistir previamente el Landlord, Tenant y Property requeridos por las claves foraneas (FKs) en la tabla contracts
    @Autowired
    private UserPostgresAdapter userPostgresAdapter;

    @Autowired
    private PropertyPostgresAdapter propertyPostgresAdapter;

    private User createAndPersistUser(String email, Role role) {
        User user = User.registerNew(
                email,
                "password_hash_123",
                "Usuario Test",
                "12345678-5",
                "+56912345678",
                role
        );
        return userPostgresAdapter.save(user);
    }

    private Property createAndPersistProperty(User landlord, String address) {
        Money price = new Money(new BigDecimal("500000.0000"), Currency.CLP);
        Property property = Property.registerNew(address, price, landlord.getId());
        return propertyPostgresAdapter.save(property);
    }

    @Test
    @DisplayName("Debe guardar un contrato de arriendo en PostgreSQL y recuperarlo con sus atributos intactos")
    void shouldSaveAndRetrieveContract() {
        // Arrange - persistir entidades dependientes por FK
        User landlord = createAndPersistUser("landlord.contract@rentflow.com", Role.LANDLORD);
        User tenant = createAndPersistUser("tenant.contract@rentflow.com", Role.TENANT);
        Property property = createAndPersistProperty(landlord, "Av. Siempre Viva 742");

        Money rent = new Money(new BigDecimal("500000.0000"), Currency.CLP);
        Money deposit = new Money(new BigDecimal("500000.0000"), Currency.CLP);
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusYears(1);

        RentalContract newContract = RentalContract.create(
                property.getId(),
                tenant.getId(),
                landlord.getId(),
                rent,
                deposit,
                5, // due day
                start,
                end
        );

        // Act
        RentalContract savedContract = contractPostgresAdapter.save(newContract);

        // Assert - se agrego esto para validar la persistencia e integridad referencial en PostgreSQL real
        assertThat(savedContract).isNotNull();
        assertThat(savedContract.getId()).isNotNull();

        Optional<RentalContract> retrieved = contractPostgresAdapter.findById(savedContract.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getPropertyId()).isEqualTo(property.getId());
        assertThat(retrieved.get().getTenantId()).isEqualTo(tenant.getId());
        assertThat(retrieved.get().getLandlordId()).isEqualTo(landlord.getId());
        assertThat(retrieved.get().getMonthlyRent().getAmount()).isEqualByComparingTo(new BigDecimal("500000.0000"));
        assertThat(retrieved.get().getDepositAmount().getAmount()).isEqualByComparingTo(new BigDecimal("500000.0000"));
        assertThat(retrieved.get().getPaymentDueDay()).isEqualTo(5);
        assertThat(retrieved.get().getStatus()).isEqualTo(ContractStatus.ACTIVE);
        assertThat(retrieved.get().getCreatedAt()).isNotNull();
        assertThat(retrieved.get().getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Debe buscar todos los contratos asociados a un Tenant especifico")
    void shouldFindContractsByTenantId() {
        // Arrange
        User landlord = createAndPersistUser("landlord.c2@rentflow.com", Role.LANDLORD);
        User tenant = createAndPersistUser("tenant.c2@rentflow.com", Role.TENANT);

        Property prop1 = createAndPersistProperty(landlord, "Depto 101");
        Property prop2 = createAndPersistProperty(landlord, "Depto 102");

        Money rent = new Money(new BigDecimal("400000.0000"), Currency.CLP);
        Money deposit = new Money(new BigDecimal("400000.0000"), Currency.CLP);
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusMonths(6);

        RentalContract contract1 = RentalContract.create(prop1.getId(), tenant.getId(), landlord.getId(), rent, deposit, 1, start, end);
        RentalContract contract2 = RentalContract.create(prop2.getId(), tenant.getId(), landlord.getId(), rent, deposit, 1, start, end);

        contractPostgresAdapter.save(contract1);
        contractPostgresAdapter.save(contract2);

        // Act - se agrego esto para comprobar el filtrado por ID de inquilino (tenantId)
        List<RentalContract> contracts = contractPostgresAdapter.findByTenantId(tenant.getId());

        // Assert
        assertThat(contracts).hasSize(2);
    }

    @Test
    @DisplayName("Debe buscar contratos asociados a un Landlord navegando la propiedad")
    void shouldFindContractsByPropertyLandlordId() {
        // Arrange
        User landlord = createAndPersistUser("landlord.c3@rentflow.com", Role.LANDLORD);
        User tenant = createAndPersistUser("tenant.c3@rentflow.com", Role.TENANT);
        Property property = createAndPersistProperty(landlord, "Local Comercial 5");

        Money rent = new Money(new BigDecimal("800000.0000"), Currency.CLP);
        Money deposit = new Money(new BigDecimal("800000.0000"), Currency.CLP);
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusYears(2);

        RentalContract contract = RentalContract.create(property.getId(), tenant.getId(), landlord.getId(), rent, deposit, 10, start, end);
        contractPostgresAdapter.save(contract);

        // Act - se agrego esto para validar la consulta relacional property.landlord.id
        List<RentalContract> contracts = contractPostgresAdapter.findByPropertyLandlordId(landlord.getId());

        // Assert
        assertThat(contracts).hasSize(1);
        // se agrego esto para acceder al primer elemento mediante get(0) asegurando compatibilidad en la aserción
        assertThat(contracts.get(0).getLandlordId()).isEqualTo(landlord.getId());
    }
}
