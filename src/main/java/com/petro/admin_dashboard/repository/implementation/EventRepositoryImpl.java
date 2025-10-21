package com.petro.admin_dashboard.repository.implementation;

import com.petro.admin_dashboard.enumeration.EventType;
import com.petro.admin_dashboard.mapper.UserEventRowMapper;
import com.petro.admin_dashboard.model.UserEvent;
import com.petro.admin_dashboard.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collection;

import static com.petro.admin_dashboard.query.EventQuery.INSERT_EVENT_BY_USER_EMAIL_QUERY;
import static java.util.Map.of;
import static com.petro.admin_dashboard.query.EventQuery.SELECT_EVENTS_BY_USER_ID_QUERY;

@Slf4j
@Repository
@RequiredArgsConstructor
public class EventRepositoryImpl implements EventRepository {
    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public Collection<UserEvent> getEventsByUserId(Long userId) {
        return jdbc.query(SELECT_EVENTS_BY_USER_ID_QUERY, of("userId", userId), new UserEventRowMapper());
    }

    @Override
    public void addUserEvent(String email, EventType eventType, String device, String ipAddress) {
        jdbc.update(INSERT_EVENT_BY_USER_EMAIL_QUERY, of("email", email, "type", eventType.toString(), "device", device, "ipAddress", ipAddress));

    }

    @Override
    public void addUserEvent(Long userId, EventType eventType, String device, String ipAddress) {

    }
}
