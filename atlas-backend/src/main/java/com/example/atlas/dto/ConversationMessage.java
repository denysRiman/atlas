package com.example.atlas.dto;


import com.example.atlas.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ConversationMessage {
    private Role role;
    private String content;
}
