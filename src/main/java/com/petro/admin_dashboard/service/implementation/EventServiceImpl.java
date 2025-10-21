package com.petro.admin_dashboard.service.implementation;

import com.petro.admin_dashboard.enumeration.EventType;
import com.petro.admin_dashboard.model.UserEvent;
import com.petro.admin_dashboard.repository.EventRepository;
import com.petro.admin_dashboard.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepo;

    @Override
    public Collection<UserEvent> getEventsByUserId(Long userId) {
        return eventRepo.getEventsByUserId(userId);
    }

    @Override
    public void addUserEvent(String email, EventType eventType, String device, String ipAddress) {
        eventRepo.addUserEvent(email, eventType, device, ipAddress);
    }

    @Override
    public void addUserEvent(Long userId, EventType eventType, String device, String ipAddress) {

    }
}
