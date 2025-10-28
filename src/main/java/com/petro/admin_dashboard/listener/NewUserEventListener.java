package com.petro.admin_dashboard.listener;


import com.petro.admin_dashboard.event.NewUserEvent;
import com.petro.admin_dashboard.service.EventService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import static com.petro.admin_dashboard.utils.RequestUtils.getDevice;
import static com.petro.admin_dashboard.utils.RequestUtils.getIpAddress;

@Component
@RequiredArgsConstructor
public class NewUserEventListener {
    private final EventService eventSvc;
    private final HttpServletRequest request;

    @EventListener
    public void onNewUserEvent(NewUserEvent event) {
        eventSvc.addUserEvent(event.getEmail(), event.getType(), getDevice(request), getIpAddress(request));
    }
}
