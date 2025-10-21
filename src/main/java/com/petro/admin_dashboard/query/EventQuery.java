package com.petro.admin_dashboard.query;

public class EventQuery {
    public static final String SELECT_EVENTS_BY_USER_ID_QUERY = "SELECT ue.id, ue.device, ue.ip_address, ev.type, ev.description, ue.created_at FROM UserEvents ue JOIN Events ev ON ue.event_id = ev.id JOIN Users u ON ue.user_id = u.id WHERE u.id = :userId ORDER BY ue.created_at DESC LIMIT 10";
    public static final String INSERT_EVENT_BY_USER_EMAIL_QUERY = "INSERT INTO UserEvents (user_id, event_id, device, ip_address) VALUES ((SELECT id FROM Users WHERE email = :email), (SELECT id FROM Events WHERE type = :type), :device, :ipAddress)";
    public static final String INSERT_EVENT_BY_USER_ID_QUERY = "INSERT INTO UserEvents (user_id, event_id, device, ip_address) VALUES (:userId, (SELECT id FROM Events WHERE type = :type), :device, :ipAddress)";
}
