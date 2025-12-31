package com.mentalhealth.backend.websocket;



import com.mentalhealth.backend.model.Message;
import com.mentalhealth.backend.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Controller
public class MessageWebSocketHandler {

    @Autowired
    private MessageService messageService;

    @MessageMapping("/chat.send")
    @SendToUser("/queue/messages")
    public Message sendMessage(@Payload Message message, SimpMessageHeaderAccessor headerAccessor) {
        try {
            return messageService.sendMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @MessageMapping("/chat.typing")
    @SendToUser("/queue/typing")
    public String userTyping(@Payload String userId) {
        return userId + " is typing...";
    }
}