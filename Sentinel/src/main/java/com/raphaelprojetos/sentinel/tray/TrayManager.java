package com.raphaelprojetos.sentinel.tray;

import com.raphaelprojetos.sentinel.rabbitmq.AlertaConsumer;

import javax.swing.*;
import java.awt.*;

public class TrayManager {

    private final SwingManager swingManager = new SwingManager();

    public void initTray() {

        if (!SystemTray.isSupported()) {
            System.out.println("Funcionalidade de ícone de bandeja não suportada");

            return;
        }

        try {
            AlertaConsumer alertaConsumer = new AlertaConsumer(swingManager);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

        SystemTray tray = SystemTray.getSystemTray();
        PopupMenu popupMenu = new PopupMenu();

        ImageIcon icon = new ImageIcon(getClass().getResource("/images/SimboloBrigada.png"));
        Image image = icon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);

        TrayIcon trayIcon = new TrayIcon(image, "Sentinel", popupMenu);

        MenuItem abrirOSentinel = new MenuItem("Abrir o Sentinel");
        popupMenu.add(abrirOSentinel);
        abrirOSentinel.addActionListener(e -> swingManager.showInterface());

        MenuItem fecharAplicacao = new MenuItem("Sair");
        popupMenu.add(fecharAplicacao);
        fecharAplicacao.addActionListener(e -> System.exit(0));

        try {
            tray.add(trayIcon);
            System.out.println("Ícone adicionado à bandeja");

        }
        catch (AWTException e) {
            System.err.println("Erro ao adicionar ícone à bandeja");

        }
    }
}