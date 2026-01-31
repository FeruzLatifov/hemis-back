package uz.hemis.app.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Filter to strip trailing slashes from request URIs.
 *
 * OLD-HEMIS PHP clients send URLs with trailing slashes (e.g. /v2/services/student/gpa/)
 * but Spring Boot controllers map without trailing slash.
 * This filter removes trailing slashes to ensure backward compatibility.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TrailingSlashFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String uri = httpRequest.getRequestURI();

        if (uri.length() > 1 && uri.endsWith("/")) {
            String newUri = uri.substring(0, uri.length() - 1);
            HttpServletRequest wrappedRequest = new HttpServletRequestWrapper(httpRequest) {
                @Override
                public String getRequestURI() {
                    return newUri;
                }

                @Override
                public String getServletPath() {
                    String servletPath = super.getServletPath();
                    if (servletPath.length() > 1 && servletPath.endsWith("/")) {
                        return servletPath.substring(0, servletPath.length() - 1);
                    }
                    return servletPath;
                }
            };
            chain.doFilter(wrappedRequest, response);
        } else {
            chain.doFilter(request, response);
        }
    }
}
