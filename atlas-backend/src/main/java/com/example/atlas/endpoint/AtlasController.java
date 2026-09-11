package com.example.atlas.endpoint;

import com.example.atlas.dto.IncomeMessage;
import com.example.atlas.dto.OutcomeMessage;
import com.example.atlas.service.AtlasService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AtlasController {

    private final AtlasService atlasService;

    @PostMapping("/inference")
    public ResponseEntity<OutcomeMessage> inference(@Valid @RequestBody IncomeMessage message) {
        return ResponseEntity.ok(atlasService.inferentMessage(message));
    }

}
