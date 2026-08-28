package com.sandbox.server.banking.web;

import com.sandbox.server.banking.application.CardApplicationService;
import com.sandbox.server.banking.web.dto.CardDto;
import com.sandbox.server.banking.web.mapper.CardWebMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CardController {

    private final CardApplicationService cardApplicationService;
    private final CardWebMapper mapper;

    @GetMapping("/cards")
    public ResponseEntity<PagedModel<CardDto>> cards(
            @PageableDefault(size = 20, sort = "cardNumber") Pageable pageable) {

        return ResponseEntity.ok(new PagedModel<>(cardApplicationService.listCards(pageable).map(mapper::toDto)));
    }
}
