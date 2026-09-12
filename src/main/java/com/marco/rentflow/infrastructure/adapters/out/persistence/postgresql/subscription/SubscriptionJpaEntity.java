package com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.subscription;

import com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.user.UserJpaEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "subscriptions")
public class SubscriptionJpaEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "landlord_id", nullable = false)
    private UserJpaEntity landlord;

    @Column(name = "plan_type", nullable = false, length = 50)
    private String planType; // STARTER, PRO, ENTERPRISE

    @Column(name = "billing_cycle", nullable = false, length = 50)
    private String billingCycle; // MONTHLY, ANNUALLY

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    // Conexión exacta con: max_properties INT NOT NULL
    @Column(name = "max_properties", nullable = false)
    private int maxProperties;

    // Conexión exacta con: max_storage_mb INT NOT NULL
    @Column(name = "max_storage_mb", nullable = false)
    private int maxStorageMb;

    // Conexión exacta con: current_period_start TIMESTAMP NOT NULL
    @Column(name = "current_period_start", nullable = false)
    private LocalDateTime currentPeriodStart;

    // Conexión exacta con: current_period_end TIMESTAMP NOT NULL
    @Column(name = "current_period_end", nullable = false)
    private LocalDateTime currentPeriodEnd;

    // Conexión exacta con: created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Conexión exacta con: updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected SubscriptionJpaEntity() {}

    public SubscriptionJpaEntity(UUID id, UserJpaEntity landlord, String planType, String billingCycle,
                                 String status, int maxProperties, int maxStorageMb,
                                 LocalDateTime currentPeriodStart, LocalDateTime currentPeriodEnd,
                                 LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.landlord = landlord;
        this.planType = planType;
        this.billingCycle = billingCycle;
        this.status = status;
        this.maxProperties = maxProperties;
        this.maxStorageMb = maxStorageMb;
        this.currentPeriodStart = currentPeriodStart;
        this.currentPeriodEnd = currentPeriodEnd;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UserJpaEntity getLandlord() {
        return landlord;
    }

    public void setLandlord(UserJpaEntity landlord) {
        this.landlord = landlord;
    }

    public String getPlanType() {
        return planType;
    }

    public void setPlanType(String planType) {
        this.planType = planType;
    }

    public String getBillingCycle() {
        return billingCycle;
    }

    public void setBillingCycle(String billingCycle) {
        this.billingCycle = billingCycle;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getMaxProperties() {
        return maxProperties;
    }

    public void setMaxProperties(int maxProperties) {
        this.maxProperties = maxProperties;
    }

    public int getMaxStorageMb() {
        return maxStorageMb;
    }

    public void setMaxStorageMb(int maxStorageMb) {
        this.maxStorageMb = maxStorageMb;
    }

    public LocalDateTime getCurrentPeriodStart() {
        return currentPeriodStart;
    }

    public void setCurrentPeriodStart(LocalDateTime currentPeriodStart) {
        this.currentPeriodStart = currentPeriodStart;
    }

    public LocalDateTime getCurrentPeriodEnd() {
        return currentPeriodEnd;
    }

    public void setCurrentPeriodEnd(LocalDateTime currentPeriodEnd) {
        this.currentPeriodEnd = currentPeriodEnd;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
