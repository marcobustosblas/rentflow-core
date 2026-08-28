package com.marco.rentflow.core.domain.subscription;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.UUID;

@DisplayName("Subscription Aggregate Domain Tests")
public class SubscriptionTest {

    private final UUID userId = UUID.randomUUID();

    @Test
    @DisplayName("Should create active subscription with default limits for STARTER plan")
    void shouldCreateNewSubscriptionSuccessfully() {
        Subscription subscription = Subscription.create(userId, PlanType.STARTER, BillingCycle.MONTHLY);

        assertNotNull(subscription.getId());
        assertEquals(userId, subscription.getUserId());
        assertEquals(PlanType.STARTER, subscription.getPlanType());
        assertEquals(BillingCycle.MONTHLY, subscription.getBillingCycle());
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());
        assertEquals(3, subscription.getMaxProperties());
        assertEquals(100, subscription.getMaxStorageMb());
        assertTrue(subscription.isActive());
        assertFalse(subscription.isExpired());
    }

    @Test
    @DisplayName("Should allow adding property when current count is below max limit")
    void shouldAllowAddingPropertyWhenBelowLimit() {
        Subscription subscription = Subscription.create(userId, PlanType.STARTER, BillingCycle.MONTHLY);

        // STARTER permite 3. Si tiene 2, aún puede agregar.
        assertTrue(subscription.canAddProperty(2));
    }

    @Test
    @DisplayName("Should deny adding property when current count exceeds limit")
    void shouldDenyAddingPropertyWhenLimitReached() {
        Subscription subscription = Subscription.create(userId, PlanType.STARTER, BillingCycle.MONTHLY);

        // STARTER permite 3. Si ya tiene 3, puede estar al límite (3<=3), pero con 4 no puede agregar más.
        assertTrue(subscription.canAddProperty(3));
        assertFalse(subscription.canAddProperty(4));
    }

    @Test
    @DisplayName("Should deny adding property when subscription is not active")
    void shouldDenyAddingPropertyWhenInactiveOrCancelled() {
        Subscription subscription = Subscription.create(userId, PlanType.STARTER, BillingCycle.MONTHLY);
        subscription.cancel();

        assertFalse(subscription.canAddProperty(1));
    }

    @Test
    @DisplayName("Should update limits and period when changing plan (Upgrade)")
    void shouldUpdateLimitsOnPlanChange() {
        Subscription subscription = Subscription.create(userId, PlanType.STARTER, BillingCycle.MONTHLY);

        subscription.changePlan(PlanType.PRO, BillingCycle.YEARLY);

        assertEquals(PlanType.PRO, subscription.getPlanType());
        assertEquals(BillingCycle.YEARLY, subscription.getBillingCycle());
        assertEquals(7, subscription.getMaxProperties());
        assertEquals(1000, subscription.getMaxStorageMb());
    }

    @Test
    @DisplayName("Should transition states correctly: cancel, past due, and renew")
    void shouldHandleStateTransitions() {
        Subscription subscription = Subscription.create(userId, PlanType.STARTER, BillingCycle.MONTHLY);

        subscription.markAsPastDue();
        assertEquals(SubscriptionStatus.PAST_DUE, subscription.getStatus());
        assertFalse(subscription.isActive());

        subscription.renew();
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());
        assertTrue(subscription.isActive());

        subscription.cancel();
        assertEquals(SubscriptionStatus.CANCELLED, subscription.getStatus());
    }

    @Test
    @DisplayName("Should identify expired subscription when currentPeriodEnd is in the past")
    void shouldIdentifyExpiredSubscription() {
        LocalDateTime pastDate = LocalDateTime.now().minusDays(1);
        Subscription expiredSubscription = Subscription.reconstitute(
                UUID.randomUUID(), userId, PlanType.STARTER, BillingCycle.MONTHLY,
                SubscriptionStatus.ACTIVE, 3, 100,
                pastDate, // su fin de periodo fue AYER (pastDate)
                LocalDateTime.now().minusMonths(1), LocalDateTime.now()
        );

        assertTrue(expiredSubscription.isExpired());
        assertFalse(expiredSubscription.isActive());
    }

    @Test
    @DisplayName("Should create Enterprise plan and execute all date getters (Coverage)")
    void shouldCreateEnterprisePlanAndTestRemainingGetters() {
        Subscription enterpriseSubscription = Subscription.create(
                userId,
                PlanType.ENTERPRISE,
                BillingCycle.MONTHLY
        );

        assertEquals(13, enterpriseSubscription.getMaxProperties());
        assertEquals(10000, enterpriseSubscription.getMaxStorageMb());

        assertNotNull(enterpriseSubscription.getCurrentPeriodEnd());
        assertNotNull(enterpriseSubscription.getCreatedAt());
        assertNotNull(enterpriseSubscription.getUpdatedAt());
    }

}
