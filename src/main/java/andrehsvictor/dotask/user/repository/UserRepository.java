package andrehsvictor.dotask.user.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import andrehsvictor.dotask.user.model.User;

public interface UserRepository extends JpaRepository<User, UUID> {

}
