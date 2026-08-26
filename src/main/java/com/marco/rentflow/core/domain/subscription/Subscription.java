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
    private LocalDateTime subscriptionPeriodEnd;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 1. CONSTRUCTOR PRIVADO (El Guardián Absoluto)
    private Subscription(UUID id, UUID userId, PlanType planType, BillingCycle billingCycle,
                         SubscriptionStatus status, int maxProperties, int maxStorageMb,
                         LocalDateTime currentPeriodEnd, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = Objects.requireNonNull(id, "Subscription ID cannot be null");
        this.userId = Objects.requireNonNull(userId, "User ID cannot be null");
        this.planType = Objects.requireNonNull(planType, "PlanType cannot be null");
        this.billingCycle = Objects.requireNonNull(billingCycle, "BillingCycle cannot be null");
        this.status = Objects.requireNonNull(status, "Status cannot be null");
        this.maxProperties = maxProperties;
        this.maxStorageMb = maxStorageMb;
        this.subscriptionPeriodEnd = Objects.requireNonNull(currentPeriodEnd, "CurrentPeriodEnd cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "UpdatedAt cannot be null");
    }

    // 2. FACTORY METHOD PARA NUEVOS (Capa de Aplicación)
    public static Subscription create(UUID userId, PlanType planType, BillingCycle billingCycle) {
        return new Subscription(
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

    // 3. FACTORY METHOD PARA MAPEO DE BD (Capa de Infraestructura)
    public static Subscription reconstitute(UUID id, UUID userId, PlanType planType, BillingCycle billingCycle,
                                        SubscriptionStatus status, int maxProperties, int maxStorageMb,
                                        LocalDateTime currentPeriodEnd, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Subscription(
                id, userId, planType, billingCycle,
                status, maxProperties, maxStorageMb,
                currentPeriodEnd, createdAt, updatedAt
        );
    }

    // MÉTODOS Y REGLAS DE DOMINIO

    public void changePlan(PlanType newPlan, BillingCycle newCycle) {
        this.planType = Objects.requireNonNull(newPlan, "Plan type cannot be null");
        this.billingCycle = Objects.requireNonNull(newCycle, "New billing cycle cannot be null");
        this.maxProperties = newPlan.getPropertyLimit();
        this.maxStorageMb = calculateStorageMb(newPlan);
        this.subscriptionPeriodEnd = calculatePeriodEnd(newCycle, LocalDateTime.now());
        touch();
    }

    /**
     * Evalúa si un usuario 'activo' puede agregar una nueva propiedad según su cuota contratada.
     */
    public boolean canAddProperty(int currentPropertyCount) {
        return isActive() && currentPropertyCount <= this.maxProperties;
        // Permite que sean 5 exactos con <= (con 1 cambio me ahorre problemas con el cliente)
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
        // ¿El momento actual (AHORA) es DESPUÉS del fin del periodo contratado?
        return LocalDateTime.now().isAfter(this.subscriptionPeriodEnd);
    }

    /**
     * Renueva la suscripción por un nuevo periodo al recibir la confirmación de pago.
     */
    public void renew() {
        this.status = SubscriptionStatus.ACTIVE;
        this.subscriptionPeriodEnd = calculatePeriodEnd(this.billingCycle, LocalDateTime.now());
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
            case PLAN_STARTED -> 500;
            case PLAN_PRO -> 5000;
            case PLAN_ENTERPRISE -> 50000;
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
    public LocalDateTime getCurrentPeriodEnd() { return subscriptionPeriodEnd; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}