package andrehsvictor.dotask.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import andrehsvictor.dotask.model.RevokedToken;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, UUID> {

}
