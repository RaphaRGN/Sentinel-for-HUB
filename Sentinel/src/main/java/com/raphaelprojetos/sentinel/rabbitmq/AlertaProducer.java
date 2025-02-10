package com.raphaelprojetos.sentinel.rabbitmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class AlertaProducer {

    private final RabbitTemplate rabbitTemplate;

    public AlertaProducer (RabbitTemplate rabbitTemplate){
        this.rabbitTemplate = rabbitTemplate;

    }

    public void enviarAlertas (String alertaJson){
        rabbitTemplate.convertAndSend(RabbitMQConfig.NOME_EXCHANGE, "", alertaJson);
        System.out.println("Alerta enviado: " + alertaJson);

    }
}
