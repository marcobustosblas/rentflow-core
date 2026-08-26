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
    @DisplayName("Should create active subscription with default limits for STARTED plan")
    void shouldCreateNewSubscriptionSuccessfully() {
        Subscription subscription = Subscription.create(userId, PlanType.PLAN_STARTED, BillingCycle.MONTHLY);

        assertNotNull(subscription.getId());
        assertEquals(userId, subscription.getUserId());
        assertEquals(PlanType.PLAN_STARTED, subscription.getPlanType());
        assertEquals(BillingCycle.MONTHLY, subscription.getBillingCycle());
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());
        assertEquals(5, subscription.getMaxProperties());
        assertEquals(500, subscription.getMaxStorageMb());
        assertTrue(subscription.isActive());
        assertFalse(subscription.isExpired());
    }

    @Test
    @DisplayName("Should allow adding property when current count is below max limit")
    void shouldAllowAddingPropertyWhenBelowLimit() {
        Subscription subscription = Subscription.create(userId, PlanType.PLAN_STARTED, BillingCycle.MONTHLY);

        // STARTED permite 5. Si tiene 4, aún puede agregar.
        assertTrue(subscription.canAddProperty(4));
    }

    @Test
    @DisplayName("Should deny adding property when current count exceeds limit")
    void shouldDenyAddingPropertyWhenLimitReached() {
        Subscription subscription = Subscription.create(userId, PlanType.PLAN_STARTED, BillingCycle.MONTHLY);

        // STARTED permite 5. Si ya tiene 5, no puede agregar más.
        assertTrue(subscription.canAddProperty(5));
        assertFalse(subscription.canAddProperty(6));
    }

    @Test
    @DisplayName("Should deny adding property when subscription is not active")
    void shouldDenyAddingPropertyWhenInactiveOrCancelled() {
        Subscription subscription = Subscription.create(userId, PlanType.PLAN_STARTED, BillingCycle.MONTHLY);
        subscription.cancel();

        assertFalse(subscription.canAddProperty(2));
    }

    @Test
    @DisplayName("Should update limits and period when changing plan (Upgrade)")
    void shouldUpdateLimitsOnPlanChange() {
        Subscription subscription = Subscription.create(userId, PlanType.PLAN_STARTED, BillingCycle.MONTHLY);

        subscription.changePlan(PlanType.PLAN_PRO, BillingCycle.YEARLY);

        assertEquals(PlanType.PLAN_PRO, subscription.getPlanType());
        assertEquals(BillingCycle.YEARLY, subscription.getBillingCycle());
        assertEquals(20, subscription.getMaxProperties());
        assertEquals(5000, subscription.getMaxStorageMb());
    } // I like this test because it made me think about an important thing in business

    @Test
    @DisplayName("Should transition states correctly: cancel, past due, and renew")
    void shouldHandleStateTransitions() {
        Subscription subscription = Subscription.create(userId, PlanType.PLAN_STARTED, BillingCycle.MONTHLY);

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
                UUID.randomUUID(), userId, PlanType.PLAN_STARTED, BillingCycle.MONTHLY,
                SubscriptionStatus.ACTIVE, 5, 500,
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
                PlanType.PLAN_ENTERPRISE,
                BillingCycle.MONTHLY
        );

        assertEquals(50000, enterpriseSubscription.getMaxStorageMb());

        assertNotNull(enterpriseSubscription.getCurrentPeriodEnd());
        assertNotNull(enterpriseSubscription.getCreatedAt());
        assertNotNull(enterpriseSubscription.getUpdatedAt());
    }

}
