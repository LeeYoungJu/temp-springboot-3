package blog.controller;

import blog.config.jwt.JwtProperties;
import blog.domain.RefreshToken;
import blog.domain.User;
import blog.dto.CreateAccessTokenRequest;
import blog.repository.RefreshTokenRepository;
import blog.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class TokenApiControllerTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private WebApplicationContext context;
  @Autowired
  private JwtProperties jwtProperties;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private RefreshTokenRepository refreshTokenRepository;

  @BeforeEach
  void mockMvcSetup() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    userRepository.deleteAll();
  }

  @DisplayName("createNewAccessToken: 새로운 엑세스 토큰을 발급한다.")
  @Test
  void createNewAccessToken() throws Exception {
    // given
    final String url = "/api/token";

    User testUser = userRepository.save(User.builder()
            .email("user@gmail.com")
            .password("test")
            .build());

    String refreshToken = createToken(null, null, Map.of("id", testUser.getId()));

    refreshTokenRepository.save(new RefreshToken(testUser.getId(), refreshToken));

    CreateAccessTokenRequest request = new CreateAccessTokenRequest();
    request.setRefreshToken(refreshToken);
    String requestBody = objectMapper.writeValueAsString(request);

    // when
    ResultActions result = mockMvc.perform(
            MockMvcRequestBuilders.post(url)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(requestBody)
    );

    // then
    result
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.accessToken").isNotEmpty());
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