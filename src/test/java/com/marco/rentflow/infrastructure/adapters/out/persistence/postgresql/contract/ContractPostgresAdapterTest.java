package com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.contract;

import com.marco.rentflow.core.domain.common.Currency;
import com.marco.rentflow.core.domain.common.Money;
import com.marco.rentflow.core.domain.contract.ContractSource;
import com.marco.rentflow.core.domain.contract.ContractStatus;
import com.marco.rentflow.core.domain.contract.FinancialTerms;
import com.marco.rentflow.core.domain.contract.RentalContract;
import com.marco.rentflow.core.domain.contract.TenantInfo;
import com.marco.rentflow.core.domain.property.Property;
import com.marco.rentflow.core.domain.user.Role;
import com.marco.rentflow.core.domain.user.User;
import com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.AbstractIntegrationTest;
import com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.property.PropertyPostgresAdapter;
import com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.user.UserPostgresAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ContractPostgresAdapterTest extends AbstractIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ContractPostgresAdapter contractPostgresAdapter;

    @Autowired
    private UserPostgresAdapter userPostgresAdapter;

    @Autowired
    private PropertyPostgresAdapter propertyPostgresAdapter;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("TRUNCATE TABLE tenant_invitations CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE contracts CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE properties CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE users CASCADE");
    }

    private User createAndPersistUser(String email, Role role) {
        String uniqueRut = String.format("%08d-5", Math.abs(email.hashCode()) % 100_000_000);
        User user = User.registerNew(
                "Usuario Test",
                email,
                "password_hash_123",
                uniqueRut,
                "+56912345678",
                Set.of(role)
        );
        return userPostgresAdapter.save(user);
    }

    private Property createAndPersistProperty(User landlord, String address) {
        Money price = new Money(new BigDecimal("500000.0000"), Currency.CLP);
        Property property = Property.registerNew(address, price, landlord.getId());
        return propertyPostgresAdapter.save(property);
    }

    private RentalContract createContract(Property property, User landlord, User tenant, Money rent, Money deposit, int dueDay, LocalDate start, LocalDate end) {
        TenantInfo tenantInfo = new TenantInfo(
                tenant.getId(),
                tenant.getEmail(),
                tenant.getFullName(),
                tenant.getRut());
        FinancialTerms terms = new FinancialTerms(rent, deposit, dueDay, new BigDecimal("0.01"));
        return RentalContract.register(
                property.getId(),
                landlord.getId(),
                tenantInfo,
                terms,
                start,
                end,
                null,
                ContractSource.MANUAL
        );
    }

    @Test
    @DisplayName("Debe guardar un contrato de arriendo en PostgreSQL y recuperarlo con sus atributos intactos")
    void shouldSaveAndRetrieveContract() {
        // Arrange
        User landlord = createAndPersistUser("landlord.contract@rentflow.com", Role.LANDLORD);
        User tenant = createAndPersistUser("tenant.contract@rentflow.com", Role.TENANT);
        Property property = createAndPersistProperty(landlord, "Av. Siempre Viva 742");

        Money rent = new Money(new BigDecimal("500000.0000"), Currency.CLP);
        Money deposit = new Money(new BigDecimal("500000.0000"), Currency.CLP);
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusYears(1);

        RentalContract newContract = createContract(property, landlord, tenant, rent, deposit, 5, start, end);

        // Act
        RentalContract savedContract = contractPostgresAdapter.save(newContract);

        // Assert
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

        RentalContract contract1 = createContract(prop1, landlord, tenant, rent, deposit, 1, start, end);
        RentalContract contract2 = createContract(prop2, landlord, tenant, rent, deposit, 1, start, end);

        contractPostgresAdapter.save(contract1);
        contractPostgresAdapter.save(contract2);

        // Act
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

        RentalContract contract = createContract(property, landlord, tenant, rent, deposit, 10, start, end);
        contractPostgresAdapter.save(contract);

        // Act
        List<RentalContract> contracts = contractPostgresAdapter.findByPropertyLandlordId(landlord.getId());

        // Assert
        assertThat(contracts).hasSize(1);
        assertThat(contracts.get(0).getLandlordId()).isEqualTo(landlord.getId());
    }
}
