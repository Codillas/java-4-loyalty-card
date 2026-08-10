package loyaltycard.controller;

import lombok.RequiredArgsConstructor;
import loyaltycard.controller.dto.CardDto;
import loyaltycard.mapper.CardMapper;
import loyaltycard.service.CardService;
import loyaltycard.service.model.Card;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;
    private final CardMapper cardMapper;

    @Secured("ADMIN")
    @PostMapping("/customers/{customerId}")
    public ResponseEntity<CardDto> createCard(@PathVariable UUID customerId) {

        Card card = cardService.createCard(customerId);
        CardDto cardDto = cardMapper.toDto(card);

        return ResponseEntity.status(HttpStatus.CREATED).body(cardDto);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN') or @cardServiceImpl.isCardOwner(#cardId, authentication.name)")
    @GetMapping("/{cardId}")
    public ResponseEntity<CardDto> getCardById(@PathVariable UUID cardId) {

        Card card = cardService.getCardById(cardId);
        CardDto cardDto = cardMapper.toDto(card);

        return ResponseEntity.ok().body(cardDto);
    }

    @Secured("ADMIN")
    @PutMapping("/{cardId}/activate")
    public ResponseEntity<CardDto> activateCard(@PathVariable UUID cardId) {

        Card card = cardService.activateCard(cardId);
        CardDto cardDto = cardMapper.toDto(card);

        return ResponseEntity.ok().body(cardDto);
    }

    @Secured("ADMIN")
    @PutMapping("/{cardId}/block")
    public ResponseEntity<CardDto> blockCard(@PathVariable UUID cardId) {

        Card card = cardService.blockCard(cardId);
        CardDto cardDto = cardMapper.toDto(card);

        return ResponseEntity.ok().body(cardDto);
    }
}
