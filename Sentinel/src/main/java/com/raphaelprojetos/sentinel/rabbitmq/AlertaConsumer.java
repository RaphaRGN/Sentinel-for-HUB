package com.raphaelprojetos.sentinel.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;

import java.util.logging.Logger;

public class AlertaConsumer {

    private static final String NOME_QUEUE = "alertasQueue"; // Define o nome da fila
    private static final String NOME_EXCHANGE = "alertasExchange";
    private static final Logger LOGGER = Logger.getLogger(AlertaConsumer.class.getName()); //Logger
    private final Channel channel; //Faz o channel

    public AlertaConsumer(ConsumerCallback callback) throws Exception {
        LOGGER.info("Iniciando o consumidor de alertas...");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.0.16");
        factory.setPort(5672);
        factory.setUsername("SentinelHUB");
        factory.setPassword("root");

        Connection connection = factory.newConnection();
        this.channel = connection.createChannel();
        LOGGER.info("Conexão com RabbitMQ estabelecida.");

       channel.exchangeDeclare(NOME_EXCHANGE, "fanout");
        LOGGER.info("Fila '" + NOME_QUEUE + "' declarada com sucesso.");

        String nomeFila = channel.queueDeclare().getQueue();
        LOGGER.info("Fila temporária criada: " + nomeFila);

        // Vincula a fila ao exchange
        channel.queueBind(nomeFila, NOME_EXCHANGE, "");
        LOGGER.info("Fila '" + nomeFila + "' vinculada ao exchange '" + NOME_EXCHANGE + "'.");

        // Consome mensagens da fila
        channel.basicConsume(nomeFila, true, (consumerTag, delivery) -> {
            String mensagem = new String(delivery.getBody(), StandardCharsets.UTF_8);
            LOGGER.info("Mensagem recebida: " + mensagem);
            callback.onMessageReceived(mensagem);
        }, consumerTag -> LOGGER.info("Cancelamento do consumidor: " + consumerTag));
    }


    public interface ConsumerCallback {
        void onMessageReceived(String mensagem);
    }
}