package com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.contract;

import com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.property.PropertyJpaEntity;
import com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.user.UserJpaEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "contracts")
public class ContractJpaEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id", nullable = false)
    private PropertyJpaEntity property;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private UserJpaEntity tenant;

    @Column(name = "status", nullable = false, length = 50)
    private String status; // ACTIVE, EXPIRED, TERMINATED

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "due_day", nullable = false)
    private Integer dueDay;

    @Column(name = "rent_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal rentAmount;

    @Column(name = "deposit_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal depositAmount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "daily_penalty", nullable = false, precision = 19, scale = 4)
    private BigDecimal dailyPenalty;

    @Column(name = "last_readjustment_date")
    private LocalDate lastReadjustmentDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    protected ContractJpaEntity() {}

    public ContractJpaEntity(UUID id, PropertyJpaEntity property, UserJpaEntity tenant, String status, LocalDate startDate, LocalDate endDate, Integer dueDay, BigDecimal rentAmount, BigDecimal depositAmount, String currency, BigDecimal dailyPenalty, LocalDate lastReadjustmentDate) {
        this(id, property, tenant, status, startDate, endDate, dueDay, rentAmount, depositAmount, currency, dailyPenalty, lastReadjustmentDate, null, null);
    }

    // se agrego esto para permitir inicializar createdAt y updatedAt desde el mapper al convertir de Dominio a JPA
    public ContractJpaEntity(UUID id, PropertyJpaEntity property, UserJpaEntity tenant, String status, LocalDate startDate, LocalDate endDate, Integer dueDay, BigDecimal rentAmount, BigDecimal depositAmount, String currency, BigDecimal dailyPenalty, LocalDate lastReadjustmentDate, ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.id = id;
        this.property = property;
        this.tenant = tenant;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.dueDay = dueDay;
        this.rentAmount = rentAmount;
        this.depositAmount = depositAmount;
        this.currency = currency;
        this.dailyPenalty = dailyPenalty;
        this.lastReadjustmentDate = lastReadjustmentDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        // se agrego esto para evitar sobrescribir las fechas si ya fueron provistas desde el objeto de dominio
        if (this.createdAt == null) {
            this.createdAt = ZonedDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = ZonedDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = ZonedDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public PropertyJpaEntity getProperty() {
        return property;
    }

    public void setProperty(PropertyJpaEntity property) {
        this.property = property;
    }

    public UserJpaEntity getTenant() {
        return tenant;
    }

    public void setTenant(UserJpaEntity tenant) {
        this.tenant = tenant;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Integer getDueDay() {
        return dueDay;
    }

    public void setDueDay(Integer dueDay) {
        this.dueDay = dueDay;
    }

    public BigDecimal getRentAmount() {
        return rentAmount;
    }

    public void setRentAmount(BigDecimal rentAmount) {
        this.rentAmount = rentAmount;
    }

    public BigDecimal getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(BigDecimal depositAmount) {
        this.depositAmount = depositAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getDailyPenalty() {
        return dailyPenalty;
    }

    public void setDailyPenalty(BigDecimal dailyPenalty) {
        this.dailyPenalty = dailyPenalty;
    }

    public LocalDate getLastReadjustmentDate() {
        return lastReadjustmentDate;
    }

    public void setLastReadjustmentDate(LocalDate lastReadjustmentDate) {
        this.lastReadjustmentDate = lastReadjustmentDate;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}