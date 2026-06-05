package com.cryptomining.platform.security;

import com.cryptomining.platform.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter implements Filter {

    private final int requestsPerMinute;
    private final Map<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> windowStarts = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RateLimitFilter(@Value("${app.rate-limit.requests-per-minute:100}") int requestsPerMinute) {
        this.requestsPerMinute = requestsPerMinute;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String clientIp = getClientIp(httpRequest);
        long now = System.currentTimeMillis();

        windowStarts.compute(clientIp, (ip, start) -> {
            if (start == null || now - start > 60_000) {
                requestCounts.put(ip, new AtomicInteger(0));
                return now;
            }
            return start;
        });

        int count = requestCounts.computeIfAbsent(clientIp, k -> new AtomicInteger(0)).incrementAndGet();
        if (count > requestsPerMinute) {
            httpResponse.setStatus(429);
            httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(httpResponse.getWriter(),
                ApiResponse.error("Rate limit exceeded. Try again later."));
            return;
        }
        chain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
