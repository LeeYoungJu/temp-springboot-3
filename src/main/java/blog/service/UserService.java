package blog.service;

import blog.domain.User;
import blog.dto.AddUserRequest;
import blog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  public Long save(AddUserRequest request) {
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    return userRepository.save(User.builder()
                    .email(request.getEmail())
                    .password(encoder.encode(request.getPassword()))
                    .build()
            )
            .getId();
  }

  public User findById(Long userId) {
    return userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Unexpected user"));
  }

  public User findByEmail(String email) {
    return userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Unexpected user"));
  }
}
