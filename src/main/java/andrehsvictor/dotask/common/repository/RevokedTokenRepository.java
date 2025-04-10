package andrehsvictor.dotask.common.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import andrehsvictor.dotask.common.model.RevokedToken;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, UUID> {

}
