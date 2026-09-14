package com.example.atlas.api;

import com.example.atlas.api.dto.InferenceRequest;
import com.example.atlas.api.dto.InferenceResponse;
import com.example.atlas.application.AtlasService;
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
    public ResponseEntity<InferenceResponse> inference(@Valid @RequestBody InferenceRequest message) {
        return ResponseEntity.ok(atlasService.inferentMessage(message));
    }

}
