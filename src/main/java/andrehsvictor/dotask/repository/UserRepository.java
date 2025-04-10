package andrehsvictor.dotask.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import andrehsvictor.dotask.model.User;

public interface UserRepository extends JpaRepository<User, UUID> {

}
