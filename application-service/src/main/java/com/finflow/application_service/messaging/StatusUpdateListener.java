package com.finflow.application_service.messaging;

import com.finflow.application_service.config.RabbitMQConfig;
import com.finflow.application_service.dto.StatusUpdateMessage;
import com.finflow.application_service.service.ApplicationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StatusUpdateListener {

    @Autowired
    private ApplicationService applicationService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleStatusUpdate(StatusUpdateMessage message) {
        System.out.println("Received async status update via RabbitMQ for application " + message.getApplicationId() + " -> " + message.getStatus());
        applicationService.updateStatus(message.getApplicationId(), message.getStatus());
    }
}
