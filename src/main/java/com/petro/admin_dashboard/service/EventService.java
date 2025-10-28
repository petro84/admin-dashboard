package com.petro.admin_dashboard.service;

import com.petro.admin_dashboard.enumeration.EventType;
import com.petro.admin_dashboard.model.UserEvent;

import java.util.Collection;

public interface EventService {

    Collection<UserEvent> getEventsByUserId(Long userId);
    void addUserEvent(String email, EventType eventType, String device, String ipAddress);
    void addUserEvent(Long userId, EventType eventType, String device, String ipAddress);

}
