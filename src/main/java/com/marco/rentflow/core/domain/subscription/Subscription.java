package com.marco.rentflow.core.domain.subscription;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Subscription {
    private final UUID id;
    private final UUID userId;
    private PlanType planType;
    private BillingCycle billingCycle;
    private SubscriptionStatus status;
    private int maxProperties;
    private int maxStorageMb;
    private LocalDateTime currentPeriodEnd;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 1. Constructor para NUEVA suscripción (Registro desde 0)
    public Subscription(UUID userId, PlanType planType, BillingCycle billingCycle) {
        this(
                UUID.randomUUID(),
                userId,
                planType,
                billingCycle,
                SubscriptionStatus.ACTIVE,
                planType.getPropertyLimit(),
                calculateStorageMb(planType),
                calculatePeriodEnd(billingCycle, LocalDateTime.now()),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    // 2. Constructor completo (Reconstitución desde la Base de Datos)
    public Subscription(UUID id, UUID userId, PlanType planType, BillingCycle billingCycle,
                        SubscriptionStatus status, int maxProperties, int maxStorageMb,
                        LocalDateTime currentPeriodEnd, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = Objects.requireNonNull(id, "Subscription ID cannot be null");
        this.userId = Objects.requireNonNull(userId, "User ID cannot be null");
        this.planType = Objects.requireNonNull(planType, "PlanType cannot be null");
        this.billingCycle = Objects.requireNonNull(billingCycle, "BillingCycle cannot be null");
        this.status = Objects.requireNonNull(status, "Status cannot be null");
        this.maxProperties = maxProperties;
        this.maxStorageMb = maxStorageMb;
        this.currentPeriodEnd = Objects.requireNonNull(currentPeriodEnd, "CurrentPeriodEnd cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "UpdatedAt cannot be null");
    }

    // MÉTODOS Y REGLAS DE DOMINIO

    /**
     * Evalúa si el usuario puede agregar una nueva propiedad según su cuota contratada.
     */
    public boolean canAddProperty(int currentPropertyCount) {
        return isActive() && currentPropertyCount < this.maxProperties;
    }

    /**
     * Evalúa si la suscripción está activa y no vencida por fecha.
     */
    public boolean isActive() {
        return this.status == SubscriptionStatus.ACTIVE && !isExpired();
    }

    /**
     * Evalúa si el periodo pagado ya venció respecto a la fecha actual.
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.currentPeriodEnd);
    }

    /**
     * Renueva la suscripción por un nuevo periodo al recibir la confirmación de pago.
     */
    public void renew() {
        this.status = SubscriptionStatus.ACTIVE;
        this.currentPeriodEnd = calculatePeriodEnd(this.billingCycle, LocalDateTime.now());
        touch();
    }

    /**
     * Cancela la suscripción.
     */
    public void cancel() {
        this.status = SubscriptionStatus.CANCELLED;
        touch();
    }

    /**
     * Marca la suscripción como morosa/atrasada.
     */
    public void markAsPastDue() {
        this.status = SubscriptionStatus.PAST_DUE;
        touch();
    }

    // Métodos auxiliares privados
    private static int calculateStorageMb(PlanType plan) {
        return switch (plan) {
            case STARTED -> 500;
            case PRO -> 5000;
            case ENTERPRISE -> 50000;
        };
    }

    private static LocalDateTime calculatePeriodEnd(BillingCycle cycle, LocalDateTime startDate) {
        return cycle == BillingCycle.YEARLY ? startDate.plusYears(1) : startDate.plusMonths(1);
    }

    private void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    // GETTERS
    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public PlanType getPlanType() { return planType; }
    public BillingCycle getBillingCycle() { return billingCycle; }
    public SubscriptionStatus getStatus() { return status; }
    public int getMaxProperties() { return maxProperties; }
    public int getMaxStorageMb() { return maxStorageMb; }
    public LocalDateTime getCurrentPeriodEnd() { return currentPeriodEnd; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}