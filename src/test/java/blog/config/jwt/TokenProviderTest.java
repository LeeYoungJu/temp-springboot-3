package blog.config.jwt;

import blog.domain.User;
import blog.repository.UserRepository;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
class TokenProviderTest {

  @Autowired
  private TokenProvider tokenProvider;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private JwtProperties jwtProperties;

  @DisplayName("generateToken(): 유저 정보와 만료 기간을 전달해 토큰을 만들 수 있다.")
  @Test
  void generateToken() {
    // given
    User user = userRepository.save(User.builder()
            .email("user@gmail.com")
            .password("test")
            .build());

    // when
    String token = tokenProvider.generateToken(user, Duration.ofDays(14));

    // then
    Long userId = Jwts.parser()
            .setSigningKey(jwtProperties.getSecretKey())
            .parseClaimsJws(token)
            .getBody()
            .get("id", Long.class);

    assertThat(userId).isEqualTo(user.getId());
  }

  @DisplayName("validToken(): 만료된 토큰인 때에 유효성 검증에 실패한다.")
  @Test
  void validToken_invalidToken() {
    // given
    String token = createToken("test@email.com", new Date(new Date().getTime() - Duration.ofDays(7).toMillis()), null);

    // when
    boolean result = tokenProvider.validToken(token);

    // then
    assertThat(result).isFalse();
  }

  @DisplayName("validToken(): 유효한 토큰인 때에 유효성 검증에 성공한다.")
  @Test
  void validToken_validToken() {
    //given
    String token = createDefaultToken();

    // when
    boolean result = tokenProvider.validToken(token);

    // then
    assertThat(result).isTrue();
  }

  @DisplayName("getAuthentication(): 토큰 기반으로 인증 정보를 가져올 수 있다.")
  @Test
  void getAuthentication() {
    // given
    String userEmail = "user@gmail.com";
    String token = createToken(userEmail, null, null);

    // when
    Authentication authentication = tokenProvider.getAuthentication(token);

    // then
    assertThat(((UserDetails)authentication.getPrincipal()).getUsername()).isEqualTo(userEmail);
  }

  @DisplayName("getUserId(): 토큰으로 유저 ID를 가져올 수 있다.")
  @Test
  void getUserId() {
    // given
    Long userId = 1L;
    String token = createToken(null, null, Map.of("id", userId));

    // when
    Long userIdByToken = tokenProvider.getUserId(token);

    // then
    assertThat(userIdByToken).isEqualTo(userId);
  }

  private String createDefaultToken() {
    return createToken(null, null, null);
  }

  private String createToken(String email, Date expiration, Map<String, Object> claims) {
    return Jwts.builder()
            .setSubject(email != null ? email : "test@email.com")
            .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
            .setIssuer(jwtProperties.getIssuer())
            .setIssuedAt(new Date())
            .setExpiration(expiration != null ? expiration : new Date(new Date().getTime() + Duration.ofDays(14).toMillis()))
            .addClaims(claims != null ? claims : Collections.emptyMap())
            .signWith(SignatureAlgorithm.HS256, jwtProperties.getSecretKey())
            .compact();
  }
}