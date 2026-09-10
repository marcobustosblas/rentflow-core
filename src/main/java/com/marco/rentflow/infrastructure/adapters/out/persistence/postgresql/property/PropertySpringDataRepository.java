package com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.property;

import com.marco.rentflow.core.domain.property.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PropertySpringDataRepository extends JpaRepository<PropertyJpaEntity, UUID> {
    List<PropertyJpaEntity> findByLandlordId(UUID landlordId);

    // se agrego esto para definir la consulta JPQL con @Query ya que Spring Data JPA fallaba buscando una propiedad 'allAvailable' en PropertyJpaEntity al intentar inferir la query del nombre del metodo
    @Query("SELECT p FROM PropertyJpaEntity p WHERE p.status = :status")
    List<PropertyJpaEntity> findAllAvailable(@Param("status") String status);

    long countByLandlordId(UUID landlordId);
}
