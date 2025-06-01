package com.raphaelprojetos.sentinel.tray;

import com.raphaelprojetos.sentinel.dao.AlertaDAO;
import com.raphaelprojetos.sentinel.dao.UsuarioDAO;
import com.raphaelprojetos.sentinel.dto.AlertaDTO;
import com.raphaelprojetos.sentinel.dto.UsuarioDTO;
import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXTable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;

public class SwingTools {


    public void atualizarTabelaAlertas(UsuarioDTO usuarioLogado, JXTable tabelaAlertas) {
        DefaultTableModel model = (DefaultTableModel) tabelaAlertas.getModel();

        if (usuarioLogado == null || !usuarioLogado.isAdmin()) {
            model.setRowCount(0);

            return;
        }

        try {
            AlertaDAO alertaDAO = new AlertaDAO();
            List<AlertaDTO> alertas = alertaDAO.buscarUltimosAlertas(10);

            model.setRowCount(0);

            for (AlertaDTO alerta : alertas) {
                model.addRow(new Object[]{
                        alerta.getCodigo(),
                        alerta.getTitulo(),
                        alerta.getDescricao(),
                        alerta.getTempoFormatado()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao atualizar alertas: " + e.getMessage());
        }
    }

    public void adicionarTabelaUsuarios(UsuarioDTO usuarioLogado, JXTable tabelaUsuarios){
        DefaultTableModel model = (DefaultTableModel) tabelaUsuarios.getModel();

        if (usuarioLogado == null || !usuarioLogado.isAdmin()) {
            model.setRowCount(0);
            return;
        }
        try{
            UsuarioDAO usuarios = new UsuarioDAO();
            List<UsuarioDTO> listaUsuarios = usuarios.buscarTodosUsuarios();

            model.setRowCount(0);

            for (UsuarioDTO usuarioDTO : listaUsuarios){

                model.addRow(new Object[]{
                        usuarioDTO.getId(),
                        usuarioDTO.getNome(),
                        usuarioDTO.isAdmin(),
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao checar usuários: " + e.getMessage());

        }
    }


    public void autenticacaoBotoes(ArrayList<JXButton> botoes, UsuarioDTO usuarioLogado){

        if(usuarioLogado == null){

            for (JXButton botao : botoes){

                botao.setEnabled(false);

            }

        }

        if(usuarioLogado != null) {

            for (JXButton botao : botoes) {

                botao.setEnabled(true);
            }
        }
    }

    public void atualizarNomeBotao(JXButton botao, UsuarioDTO usuarioLogado, JPanel cardPanel){
        if(usuarioLogado == null){
            botao.setText("Login");
            cardPanel.repaint();
            cardPanel.revalidate();

        }
        else{
            botao.setText(usuarioLogado.getNome());
            cardPanel.repaint();
            cardPanel.revalidate();

        }
    }
}