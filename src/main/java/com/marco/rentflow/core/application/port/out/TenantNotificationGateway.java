package com.marco.rentflow.core.application.port.out;

public interface TenantNotificationGateway {
    void sendNotification(String addressee, String message);
}
