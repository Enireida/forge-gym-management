package com.unyt.gym.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

/**
 * CSRF (Cross-Site Request Forgery) protection interceptor.
 *
 * <p>On every GET/HEAD request: generates a random token per session and
 * stores it both in the session and in the model so Thymeleaf templates
 * can embed it as a hidden field: {@code <input type="hidden" name="_csrf"
 * th:value="${csrfToken}">}.</p>
 *
 * <p>On every state-changing request (POST, PUT, DELETE, PATCH): reads the
 * {@code _csrf} parameter from the request and compares it against the
 * token stored in the session. A mismatch is rejected with HTTP 403.</p>
 *
 * <p>API endpoints ({@code /api/**}) and the kiosk check-in API are
 * excluded — they are protected by the API key mechanism instead.</p>
 */
@Component
public class CsrfInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(CsrfInterceptor.class);
    static final String SESSION_KEY = "_csrf_token";
    static final String PARAM_NAME  = "_csrf";

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String method = request.getMethod().toUpperCase();
        String uri    = request.getRequestURI();

        // Skip CSRF check for safe methods and API endpoints
        if ("GET".equals(method) || "HEAD".equals(method) || "OPTIONS".equals(method)) {
            return true;
        }
        if (uri.startsWith("/api/") || uri.startsWith("/kiosk/")) {
            return true;  // API & kiosk check-in use their own auth
        }

        HttpSession session = request.getSession(false);
        String sessionToken = (session != null) ? (String) session.getAttribute(SESSION_KEY) : null;
        String requestToken = request.getParameter(PARAM_NAME);

        if (sessionToken == null || !sessionToken.equals(requestToken)) {
            log.warn("[CSRF] Token mismatch on {} {} — expected={} got={}",
                    method, uri, sessionToken, requestToken);
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Invalid or missing CSRF token. Please go back and try again.");
            return false;
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler,
                           ModelAndView mav) {

        if (mav == null) return;

        // Ensure a token exists for this session and expose it to the template
        HttpSession session = request.getSession(true);
        String token = (String) session.getAttribute(SESSION_KEY);
        if (token == null) {
            token = UUID.randomUUID().toString();
            session.setAttribute(SESSION_KEY, token);
        }
        mav.addObject("csrfToken", token);
    }
}
