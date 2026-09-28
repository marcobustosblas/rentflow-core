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

    /**
     * The tenant is nullable: a contract is registered before the tenant
     * accepts the invitation. Once accepted, the tenantId is assigned. =D
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private UserJpaEntity tenant;

    /**
     * Tenant data as captured from the signed legal contract.
     * Present from the moment the contract is registered.
     */
    @Column(name = "tenant_email", nullable = false, length = 255)
    private String tenantEmail;

    @Column(name = "tenant_full_name", nullable = false, length = 255)
    private String tenantFullName;

    @Column(name = "tenant_rut", nullable = false, length = 50)
    private String tenantRut;

    @Column(name = "contract_document_url", length = 2000)
    private String contractDocumentUrl;

    /**
     * ContractSource: 'MANUAL' | 'AI_EXTRACTED'
     */
    @Column(name = "source", nullable = false, length = 50)
    private String source;

    /**
     * ContractStatus: 'PENDING_TENANT_SIGNUP' | 'ACTIVE' | 'TERMINATED' | 'EXPIRED'
     */
    @Column(name = "status", nullable = false, length = 50)
    private String status;

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

    /** JPA requires a no-args constructor. */
    protected ContractJpaEntity() {}

    /**
     * Full constructor used by the persistence mapper.
     * Timestamps are passed explicitly to preserve domain lifecycle values.
     */
    public ContractJpaEntity(UUID id,
                             PropertyJpaEntity property,
                             UserJpaEntity tenant,
                             String tenantEmail,
                             String tenantFullName,
                             String tenantRut,
                             String contractDocumentUrl,
                             String source,
                             String status,
                             LocalDate startDate,
                             LocalDate endDate,
                             Integer dueDay,
                             BigDecimal rentAmount,
                             BigDecimal depositAmount,
                             String currency,
                             BigDecimal dailyPenalty,
                             LocalDate lastReadjustmentDate,
                             ZonedDateTime createdAt,
                             ZonedDateTime updatedAt) {
        this.id = id;
        this.property = property;
        this.tenant = tenant;
        this.tenantEmail = tenantEmail;
        this.tenantFullName = tenantFullName;
        this.tenantRut = tenantRut;
        this.contractDocumentUrl = contractDocumentUrl;
        this.source = source;
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
        // Respect timestamps provided by the mapper; fallback to now() if absent.
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

    // --- Getters ---
    public UUID getId() { return id; }
    public PropertyJpaEntity getProperty() { return property; }
    public UserJpaEntity getTenant() { return tenant; }
    public String getTenantEmail() { return tenantEmail; }
    public String getTenantFullName() { return tenantFullName; }
    public String getTenantRut() { return tenantRut; }
    public String getContractDocumentUrl() { return contractDocumentUrl; }
    public String getSource() { return source; }
    public String getStatus() { return status; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public Integer getDueDay() { return dueDay; }
    public BigDecimal getRentAmount() { return rentAmount; }
    public BigDecimal getDepositAmount() { return depositAmount; }
    public String getCurrency() { return currency; }
    public BigDecimal getDailyPenalty() { return dailyPenalty; }
    public LocalDate getLastReadjustmentDate() { return lastReadjustmentDate; }
    public ZonedDateTime getCreatedAt() { return createdAt; }
    public ZonedDateTime getUpdatedAt() { return updatedAt; }

    // --- Setters ---
    public void setId(UUID id) { this.id = id; }
    public void setProperty(PropertyJpaEntity property) { this.property = property; }
    public void setTenant(UserJpaEntity tenant) { this.tenant = tenant; }
    public void setTenantEmail(String tenantEmail) { this.tenantEmail = tenantEmail; }
    public void setTenantFullName(String tenantFullName) { this.tenantFullName = tenantFullName; }
    public void setTenantRut(String tenantRut) { this.tenantRut = tenantRut; }
    public void setContractDocumentUrl(String contractDocumentUrl) { this.contractDocumentUrl = contractDocumentUrl; }
    public void setSource(String source) { this.source = source; }
    public void setStatus(String status) { this.status = status; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public void setDueDay(Integer dueDay) { this.dueDay = dueDay; }
    public void setRentAmount(BigDecimal rentAmount) { this.rentAmount = rentAmount; }
    public void setDepositAmount(BigDecimal depositAmount) { this.depositAmount = depositAmount; }
    public void setCurrency(String currency) { this.currency = currency; }
    public void setDailyPenalty(BigDecimal dailyPenalty) { this.dailyPenalty = dailyPenalty; }
    public void setLastReadjustmentDate(LocalDate lastReadjustmentDate) { this.lastReadjustmentDate = lastReadjustmentDate; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }
}