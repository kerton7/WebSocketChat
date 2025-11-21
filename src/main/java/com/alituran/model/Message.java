package com.alituran.model;


import com.alituran.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Message {


    private String sender;
    private String content;
    private MessageType messageType;
    private String receiver;
}
