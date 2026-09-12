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

    public PropertyJpaEntity() {}

    public PropertyJpaEntity(UUID id, UserJpaEntity landlord, BankAccountJpaEntity payoutAccount, String address, String status, BigDecimal basePrice, String currency) {
        this(id, landlord, payoutAccount, address, status, basePrice, currency, null, null);
    }

    // se agrego esto para permitir inicializar createdAt y updatedAt desde el mapper al convertir de Dominio a JPA
    public PropertyJpaEntity(UUID id, UserJpaEntity landlord, BankAccountJpaEntity payoutAccount, String address, String status, BigDecimal basePrice, String currency, ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.id = id;
        this.landlord = landlord;
        this.payoutAccount = payoutAccount;
        this.address = address;
        this.status = status;
        this.basePrice = basePrice;
        this.currency = currency;
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
