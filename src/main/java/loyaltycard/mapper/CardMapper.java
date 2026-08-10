package loyaltycard.mapper;

import loyaltycard.controller.dto.CardDto;
import loyaltycard.controller.dto.StatusDto;
import loyaltycard.repository.entity.CardEntity;
import loyaltycard.repository.entity.StatusEntity;
import loyaltycard.service.model.Card;
import loyaltycard.service.model.Status;
import org.springframework.stereotype.Component;

@Component
public class CardMapper {

    public Card toDomain(CardEntity cardEntity) {

        Card card = new Card();
        card.setId(cardEntity.getId());
        card.setCustomerId(cardEntity.getCustomerId());
        card.setBalance(cardEntity.getBalance());
        card.setStatus(Status.valueOf(cardEntity.getStatusEntity().name()));
        card.setCreatedAt(cardEntity.getCreatedAt());
        card.setUpdatedAt(cardEntity.getUpdatedAt());

        return card;
    }

    public CardDto toDto(Card card) {

        CardDto cardDto = new CardDto();
        cardDto.setId(card.getId());
        cardDto.setCustomerId(card.getCustomerId());
        cardDto.setBalance(card.getBalance());
        cardDto.setStatusDto(StatusDto.valueOf(card.getStatus().name()));
        cardDto.setCreatedAt(card.getCreatedAt());
        cardDto.setUpdatedAt(card.getUpdatedAt());

        return cardDto;
    }

    public CardEntity toEntity(Card card) {

        CardEntity cardEntity = new CardEntity();
        cardEntity.setId(card.getId());
        cardEntity.setCustomerId(card.getCustomerId());
        cardEntity.setBalance(card.getBalance());
        cardEntity.setStatusEntity(StatusEntity.valueOf(card.getStatus().name()));
        cardEntity.setCreatedAt(card.getCreatedAt());
        cardEntity.setUpdatedAt(card.getUpdatedAt());

        return cardEntity;
    }
}
