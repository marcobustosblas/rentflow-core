package com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.property;

import com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.bankaccount.BankAccountJpaEntity;
import com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.user.UserJpaEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "properties")
public class PropertyJpaEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "landlord_id", nullable = false)
    private UserJpaEntity landlord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payout_account_id")
    private BankAccountJpaEntity payoutAccount;

    @Column(name = "address", nullable = false, length = 500)
    private String address;

    @Column(name = "status", nullable = false, length = 50)
    private String status; // Mapeado desde PropertyStatus (AVAILABLE, RENTED)

    @Column(name = "base_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal basePrice;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency; // Para el Value Object Money

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    protected PropertyJpaEntity() {}

    public PropertyJpaEntity(UUID id, UserJpaEntity landlord, BankAccountJpaEntity payoutAccount, String address, String status, BigDecimal basePrice, String currency) {
        this.id = id;
        this.landlord = landlord;
        this.payoutAccount = payoutAccount;
        this.address = address;
        this.status = status;
        this.basePrice = basePrice;
        this.currency = currency;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = ZonedDateTime.now();
        this.updatedAt = ZonedDateTime.now();
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

    public UserJpaEntity getLandlord() {
        return landlord;
    }

    public void setLandlord(UserJpaEntity landlord) {
        this.landlord = landlord;
    }

    public BankAccountJpaEntity getPayoutAccount() {
        return payoutAccount;
    }

    public void setPayoutAccount(BankAccountJpaEntity payoutAccount) {
        this.payoutAccount = payoutAccount;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
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
