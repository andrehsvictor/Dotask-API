package andrehsvictor.dotask.user;

import org.springframework.stereotype.Service;

import andrehsvictor.dotask.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(User.class, "email", email));
    }
}
