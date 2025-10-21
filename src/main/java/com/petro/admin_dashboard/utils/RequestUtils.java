package com.petro.admin_dashboard.utils;

import jakarta.servlet.http.HttpServletRequest;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;

public class RequestUtils {

    public static final String USER_AGENT = "user-agent";
    public static final String FORWARDED_FOR_HEADER = "X-FORWARDED-FOR";

    public static String getIpAddress(HttpServletRequest request) {
        String ipAddress = "Unknown";

        if (request != null) {
            ipAddress = request.getHeader(FORWARDED_FOR_HEADER);
            if (ipAddress == null || ipAddress.isEmpty()) {
                ipAddress = request.getRemoteAddr();
            }
        }
        return ipAddress;
    }

    public static String getDevice(HttpServletRequest request) {
        UserAgentAnalyzer analyzer = UserAgentAnalyzer.newBuilder().hideMatcherLoadStats().withCache(10000).build();
        UserAgent agent = analyzer.parse(request.getHeader(USER_AGENT));

        return agent.getValue(UserAgent.AGENT_NAME) + " - " + agent.getValue(UserAgent.DEVICE_NAME);
    }
}
