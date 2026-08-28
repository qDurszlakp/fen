package com.sandbox.server.banking.web.mapper;

import com.sandbox.server.banking.application.CardView;
import com.sandbox.server.banking.web.dto.CardDto;
import org.springframework.stereotype.Component;

@Component
public class CardWebMapper {

    public CardDto toDto(CardView view) {
        CardDto dto = new CardDto();
        dto.setId(view.card().id().value());
        dto.setVersion(view.card().version());
        dto.setCardNumber(view.card().cardNumber().value());
        dto.setAccountNumber(view.accountNumber() != null ? view.accountNumber().value() : null);
        return dto;
    }
}
