package loyaltycard.repository;

import loyaltycard.repository.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, UUID> {

    List<TransactionEntity> findAllByCardId(UUID cardId);

    Optional<TransactionEntity> findByIdAndCardId(UUID id, UUID cardId);
}
