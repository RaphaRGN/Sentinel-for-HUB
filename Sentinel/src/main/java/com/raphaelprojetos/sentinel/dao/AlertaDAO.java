package com.raphaelprojetos.sentinel.dao;
import com.raphaelprojetos.sentinel.config.DotEnvConfig;
import com.raphaelprojetos.sentinel.dto.AlertaDTO;

import javax.swing.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.raphaelprojetos.sentinel.database.Database.getConnection;

public class AlertaDAO {


    public void salvarAlerta(AlertaDTO alerta) {
        String sql = "INSERT INTO alertas (codigo, titulo, tempo, descricao) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, alerta.getCodigo());
            stmt.setString(2, alerta.getTitulo());
            stmt.setObject(3, LocalDateTime.now());
            stmt.setString(4, alerta.getDescricao());

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    alerta.setId(generatedKeys.getLong(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<AlertaDTO> buscarTodosAlertas() {
        String sql = "SELECT id, codigo, titulo, tempo, descricao FROM alertas ORDER BY tempo DESC";
        List<AlertaDTO> alertas = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                AlertaDTO alerta = new AlertaDTO();
                alerta.setId(rs.getLong("id"));
                alerta.setCodigo(rs.getString("codigo"));
                alerta.setTitulo(rs.getString("titulo"));
                alerta.setTempoFormatado(rs.getTimestamp("tempo").toLocalDateTime().toString());
                alerta.setDescricao(rs.getString("descricao"));

                alertas.add(alerta);

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return alertas;
    }

    // Método para buscar um alerta por ID
    public AlertaDTO buscarAlertaPorId(Long id) {
        String sql = "SELECT id, codigo, titulo, tempo, descricao FROM alertas WHERE id = ?";
        AlertaDTO alerta = null;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    alerta = new AlertaDTO();
                    alerta.setId(rs.getLong("id"));
                    alerta.setCodigo(rs.getString("codigo"));
                    alerta.setTitulo(rs.getString("titulo"));
                    alerta.setTempoFormatado(rs.getTimestamp("tempo").toLocalDateTime().toString());
                    alerta.setDescricao(rs.getString("descricao"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return alerta;
    }

    // Método para deletar um alerta pelo ID
    public boolean deletarAlertaPorId(Long id) {
        String sql = "DELETE FROM alertas WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void deletarTodosAlertas(){
        String sql = "DELETE FROM alertas";
        try (Connection conn = getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)){

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas > 0){
                JOptionPane.showMessageDialog(null, "Alertas deletados !");

            }
            else{
                JOptionPane.showMessageDialog(null, "Nenhum alerta a deletar");

            }
        }
        catch (SQLException e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erro ao deletar todos alertas");
        }
    }

    public List<AlertaDTO> buscarUltimosAlertas(int limite) {
        String sql = "SELECT id, codigo, titulo, descricao, tempo FROM alertas ORDER BY tempo DESC LIMIT ?";
        List<AlertaDTO> alertas = new ArrayList<>();

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, limite);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    AlertaDTO alerta = new AlertaDTO();
                    alerta.setId(rs.getLong("id"));
                    alerta.setCodigo(rs.getString("codigo"));
                    alerta.setTitulo(rs.getString("titulo"));
                    alerta.setDescricao(rs.getString("descricao"));
                    alerta.setTempoFormatado(rs.getTimestamp("tempo").toLocalDateTime().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH-mm-ss"))
                    );
                    alertas.add(alerta);

                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar últimos alertas: " + e.getMessage(), e);
        }

        return alertas;
    }
}