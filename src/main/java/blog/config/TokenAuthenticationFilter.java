package blog.config;

import blog.config.jwt.TokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {
  private final TokenProvider tokenProvider;
  private final static String HEADER_AUTHORIZATION = "Authorization";
  private final static String TOKEN_PREFIX = "Bearer ";

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    String header = request.getHeader(HEADER_AUTHORIZATION);
    String token = getAccessToken(header);

    if(tokenProvider.validToken(token)) {
      Authentication authentication = tokenProvider.getAuthentication(token);
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    filterChain.doFilter(request, response);
  }

  private String getAccessToken(String header) {
    if(header != null && header.startsWith(TOKEN_PREFIX)) {
      return header.substring(TOKEN_PREFIX.length());
    }
    return null;
  }
}
