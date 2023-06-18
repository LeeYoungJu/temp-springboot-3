package blog.service;

import blog.domain.RefreshToken;
import blog.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
  private final RefreshTokenRepository refreshTokenRepository;

  public RefreshToken findByRefreshToken(String refreshToken) {
    return refreshTokenRepository.findByRefreshToken(refreshToken)
            .orElseThrow(() -> new IllegalArgumentException("Unexpected token"));
  }
}
