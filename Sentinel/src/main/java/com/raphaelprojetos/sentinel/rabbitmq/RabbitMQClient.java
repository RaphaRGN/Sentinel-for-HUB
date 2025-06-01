package com.raphaelprojetos.sentinel.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.raphaelprojetos.sentinel.config.DotEnvConfig;


public class RabbitMQClient {

    public static final String NOME_EXCHANGE = "alertasExchange";

    public void enviarAlerta(String alertaRabbit) throws Exception{
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(DotEnvConfig.getEnvVar("CENTRAL_IP"));
        factory.setPort(5672);
        factory.setUsername(DotEnvConfig.getEnvVar("RABBITMQ_USERNAME"));
        factory.setPassword(DotEnvConfig.getEnvVar("RABBITMQ_PASSWORD"));

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            channel.exchangeDeclare(NOME_EXCHANGE, "fanout");
            channel.basicPublish(NOME_EXCHANGE,"", null, alertaRabbit.getBytes());
            System.out.println("Mensagem enviada: " + alertaRabbit);
        }
        catch (Exception e){

            e.printStackTrace();
        }
    }
}
