package com.marco.rentflow.core.domain.subscription;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Subscription {
    private final UUID id;
    private final UUID landlordId;
    private PlanType planType;
    private BillingCycle billingCycle;
    private SubscriptionStatus status;
    private int maxProperties;
    private int maxStorageMb;

    // Fechas explícitas del ciclo de facturación actual
    private LocalDateTime currentPeriodStart;
    private LocalDateTime currentPeriodEnd;

    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Subscription(UUID id, UUID landlordId, PlanType planType, BillingCycle billingCycle,
                         SubscriptionStatus status, int maxProperties, int maxStorageMb,
                         LocalDateTime currentPeriodStart, LocalDateTime currentPeriodEnd,
                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = Objects.requireNonNull(id, "Subscription ID cannot be null");
        this.landlordId = Objects.requireNonNull(landlordId, "User ID cannot be null");
        this.planType = Objects.requireNonNull(planType, "PlanType cannot be null");
        this.billingCycle = Objects.requireNonNull(billingCycle, "BillingCycle cannot be null");
        this.status = Objects.requireNonNull(status, "Status cannot be null");
        this.maxProperties = maxProperties;
        this.maxStorageMb = maxStorageMb;
        this.currentPeriodStart = Objects.requireNonNull(currentPeriodStart, "CurrentPeriodStart cannot be null");
        this.currentPeriodEnd = Objects.requireNonNull(currentPeriodEnd, "CurrentPeriodEnd cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "UpdatedAt cannot be null");
    }

    public static Subscription create(UUID landlordId, PlanType planType, BillingCycle billingCycle) {
        LocalDateTime now = LocalDateTime.now();
        return new Subscription(
                UUID.randomUUID(),
                landlordId,
                planType,
                billingCycle,
                SubscriptionStatus.ACTIVE,
                planType.getPropertyLimit(),
                calculateStorageMb(planType),
                now, // Inicio del ciclo
                calculatePeriodEnd(billingCycle, now), // Fin del ciclo
                now,
                now
        );
    }

    public static Subscription reconstitute(UUID id, UUID landlordId, PlanType planType, BillingCycle billingCycle,
                                            SubscriptionStatus status, int maxProperties, int maxStorageMb,
                                            LocalDateTime currentPeriodStart, LocalDateTime currentPeriodEnd,
                                            LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Subscription(
                id, landlordId, planType, billingCycle,
                status, maxProperties, maxStorageMb,
                currentPeriodStart, currentPeriodEnd,
                createdAt, updatedAt
        );
    }

    // MÉTODOS Y REGLAS DE DOMINIO

    public void changePlan(PlanType newPlan, BillingCycle newCycle) {
        this.planType = Objects.requireNonNull(newPlan, "Plan type cannot be null");
        this.billingCycle = Objects.requireNonNull(newCycle, "New billing cycle cannot be null");
        this.maxProperties = newPlan.getPropertyLimit();
        this.maxStorageMb = calculateStorageMb(newPlan);

        // Al cambiar de plan, se reinicia el ciclo desde hoy
        this.currentPeriodStart = LocalDateTime.now();
        this.currentPeriodEnd = calculatePeriodEnd(newCycle, this.currentPeriodStart);
        touch();
    }

    /**
     * Evalúa si un usuario 'activo' puede agregar una nueva propiedad según su cuota contratada.
     */
    public boolean canAddProperty(int currentPropertyCount) {
        return isActive() && currentPropertyCount <= this.maxProperties;
    }

    public boolean isActive() {
        return this.status == SubscriptionStatus.ACTIVE && !isExpired();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.currentPeriodEnd);
    }

    public void renew() {
        this.status = SubscriptionStatus.ACTIVE;
        // El nuevo inicio es hoy (o podría ser el currentPeriodEnd anterior si permites renovación adelantada)
        this.currentPeriodStart = LocalDateTime.now();
        this.currentPeriodEnd = calculatePeriodEnd(this.billingCycle, this.currentPeriodStart);
        touch();
    }

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

    /**
     * PRIVATE HELPER METHODS
     */

    private static int calculateStorageMb(PlanType plan) {
        return switch (plan) {
            case STARTER -> 100;
            case PRO -> 1000;
            case ENTERPRISE -> 10000;
        };
    }

    private static LocalDateTime calculatePeriodEnd(BillingCycle cycle, LocalDateTime startDate) {
        return cycle == BillingCycle.YEARLY ? startDate.plusYears(1) : startDate.plusMonths(1);
    }

    private void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public UUID getLandlordId() { return landlordId; }
    public PlanType getPlanType() { return planType; }
    public BillingCycle getBillingCycle() { return billingCycle; }
    public SubscriptionStatus getStatus() { return status; }
    public int getMaxProperties() { return maxProperties; }
    public int getMaxStorageMb() { return maxStorageMb; }
    public LocalDateTime getCurrentPeriodStart() { return currentPeriodStart; }
    public LocalDateTime getCurrentPeriodEnd() { return currentPeriodEnd; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}

/**
 * Nota importante:
 * VERIFICAR EN UN FUTURO LOS CONDICIONALES para los métodos de cambios de estado
 */