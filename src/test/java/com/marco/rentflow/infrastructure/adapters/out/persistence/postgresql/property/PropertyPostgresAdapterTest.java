package com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.property;

import com.marco.rentflow.core.domain.common.Currency;
import com.marco.rentflow.core.domain.common.Money;
import com.marco.rentflow.core.domain.property.Property;
import com.marco.rentflow.core.domain.property.PropertyStatus;
import com.marco.rentflow.core.domain.user.Role;
import com.marco.rentflow.core.domain.user.User;
import com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.AbstractIntegrationTest;
import com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.user.UserPostgresAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
public class PropertyPostgresAdapterTest extends AbstractIntegrationTest {

    @Autowired
    private PropertyPostgresAdapter propertyPostgresAdapter;

    // se agrego esto para persistir previamente el Landlord requerido por la clave foranea (FK landlord_id) en la tabla properties
    @Autowired
    private UserPostgresAdapter userPostgresAdapter;

    private User createAndPersistLandlord(String email) {
        User landlord = User.registerNew(
                email,
                "password_hash_123",
                "Arrendador Propietario",
                "11111111-1",
                "+56911112222",
                Role.LANDLORD
        );
        return userPostgresAdapter.save(landlord);
    }

    @Test
    @DisplayName("Debe guardar una propiedad en PostgreSQL y recuperarla con sus atributos intactos")
    void shouldSaveAndRetrieveProperty() {
        // Arrange - persistir primero el arrendador (FK en BD)
        User landlord = createAndPersistLandlord("landlord1@rentflow.com");
        Money price = new Money(new BigDecimal("450000.0000"), Currency.CLP);
        Property newProperty = Property.registerNew("Av. Providencia 1234, Depto 502", price, landlord.getId());

        // Act
        Property savedProperty = propertyPostgresAdapter.save(newProperty);

        // Assert - se agrego esto para verificar la persistencia y recuperacion via ID desde PostgreSQL real
        assertThat(savedProperty).isNotNull();
        assertThat(savedProperty.getId()).isNotNull();

        Optional<Property> retrieved = propertyPostgresAdapter.findById(savedProperty.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getAddress()).isEqualTo("Av. Providencia 1234, Depto 502");
        assertThat(retrieved.get().getLandlordId()).isEqualTo(landlord.getId());
        assertThat(retrieved.get().getBasePrice().getAmount()).isEqualByComparingTo(new BigDecimal("450000.0000"));
        assertThat(retrieved.get().getBasePrice().getCurrency()).isEqualTo(Currency.CLP);
        assertThat(retrieved.get().getStatus()).isEqualTo(PropertyStatus.AVAILABLE);
        assertThat(retrieved.get().getCreatedAt()).isNotNull();
        assertThat(retrieved.get().getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Debe listar todas las propiedades pertenecientes a un Landlord especifico")
    void shouldFindPropertiesByLandlordId() {
        // Arrange
        User landlord = createAndPersistLandlord("landlord2@rentflow.com");
        Property prop1 = Property.registerNew("Calle Los Olivos 100", new Money(new BigDecimal("300000.0000"), Currency.CLP), landlord.getId());
        Property prop2 = Property.registerNew("Calle Los Olivos 102", new Money(new BigDecimal("350000.0000"), Currency.CLP), landlord.getId());

        propertyPostgresAdapter.save(prop1);
        propertyPostgresAdapter.save(prop2);

        // Act - se agrego esto para validar la consulta filtrada por ID del arrendador
        List<Property> properties = propertyPostgresAdapter.findByLandlordId(landlord.getId());

        // Assert
        assertThat(properties).hasSize(2);
        assertThat(properties).extracting(Property::getAddress)
                .containsExactlyInAnyOrder("Calle Los Olivos 100", "Calle Los Olivos 102");
    }

    @Test
    @DisplayName("Debe listar unicamenet las propiedades con estado AVAILABLE")
    void shouldFindAllAvailableProperties() {
        // Arrange
        User landlord = createAndPersistLandlord("landlord3@rentflow.com");

        Property availableProp = Property.registerNew("Disponible 1", new Money(new BigDecimal("500000.0000"), Currency.CLP), landlord.getId());
        Property rentedProp = Property.registerNew("Arrendada 1", new Money(new BigDecimal("600000.0000"), Currency.CLP), landlord.getId());
        rentedProp.markAsRented();

        propertyPostgresAdapter.save(availableProp);
        propertyPostgresAdapter.save(rentedProp);

        // Act - se agrego esto para probar la consulta de propiedades disponibles usando la query JPQL personalizada
        List<Property> availableProperties = propertyPostgresAdapter.findByStatus(PropertyStatus.AVAILABLE.name());

        // Assert
        assertThat(availableProperties).extracting(Property::getId)
                .contains(availableProp.getId())
                .doesNotContain(rentedProp.getId());
    }

    @Test
    @DisplayName("Debe contar correctamente la cantidad de propiedades de un Landlord")
    void shouldCountPropertiesByLandlordId() {
        // Arrange
        User landlord = createAndPersistLandlord("landlord4@rentflow.com");
        Property prop1 = Property.registerNew("Prop 1", new Money(new BigDecimal("200000.0000"), Currency.CLP), landlord.getId());
        Property prop2 = Property.registerNew("Prop 2", new Money(new BigDecimal("250000.0000"), Currency.CLP), landlord.getId());

        propertyPostgresAdapter.save(prop1);
        propertyPostgresAdapter.save(prop2);

        // Act & Assert - se agrego esto para verificar el conteo aggregate por landlordId
        int count = propertyPostgresAdapter.countByLandlordId(landlord.getId());
        assertThat(count).isEqualTo(2);
    }
}
