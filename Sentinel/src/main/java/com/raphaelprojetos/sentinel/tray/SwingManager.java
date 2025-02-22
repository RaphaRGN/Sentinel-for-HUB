    package com.raphaelprojetos.sentinel.tray;

    import com.fasterxml.jackson.databind.ObjectMapper;
    import com.formdev.flatlaf.FlatLightLaf;
    import com.raphaelprojetos.sentinel.dao.AlertaDAO;
    import com.raphaelprojetos.sentinel.dao.UsuarioDAO;
    import com.raphaelprojetos.sentinel.dto.AlertaDTO;
    import com.raphaelprojetos.sentinel.dto.UsuarioDTO;
    import com.raphaelprojetos.sentinel.rabbitmq.AlertaConsumer;
    import com.raphaelprojetos.sentinel.rabbitmq.RabbitMQClient;
    import com.raphaelprojetos.sentinel.report.ExcelReportGenerator;
    import com.raphaelprojetos.sentinel.report.PdfReportGenerator;
    import org.jdesktop.swingx.JXButton;
    import org.jdesktop.swingx.JXTable;
    import org.jdesktop.swingx.JXTextField;
    import org.springframework.stereotype.Component;

    import javax.swing.*;
    import javax.swing.table.DefaultTableModel;
    import java.awt.*;
    import java.awt.event.*;
    import java.lang.reflect.InvocationTargetException;
    import java.time.LocalDateTime;
    import java.util.ArrayList;
    import java.util.List;

    @Component
    public class SwingManager extends JFrame implements AlertaConsumer.ConsumerCallback {

        private JPanel cardPanel;
        private AlertaDTO alerta;
        private CardLayout cardLayout;
        private UsuarioDTO usuarioLogado;
        private JXTable tabelaAlertas;
        private JXTable tabelaUsuarios;
        private AlertaConsumer alertaConsumer;
        private JFrame popupFrame;
        private ArrayList<JXButton> botoesParaAutenticar = new ArrayList<>();
        private final ExcelReportGenerator excelGenerator = new ExcelReportGenerator();
        private final PdfReportGenerator pdfGenerator = new PdfReportGenerator();
        private final RabbitMQClient rabbitMQClient = new RabbitMQClient();


        //Método principal
        public void initApplication() {
            JFrame telaPrincipal = new JFrame("Sentinel");
            telaPrincipal.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            telaPrincipal.setSize(1000, 800);
            telaPrincipal.setLocationRelativeTo(null);

            cardLayout = new CardLayout();
            cardPanel = new JPanel(cardLayout);
            telaPrincipal.add(cardPanel);

            JPanel mainPanel = createMainPanel(); //Main
            JPanel loginPanel = createLoginPanel(); // Tela de login
            JPanel configPanel = createConfigPanel(); // Configuração
            JPanel activeUsersPanel = createactiveUsersPanel(); //Tabela usuários
            JPanel newUserPanel = createnewUserPanel(); // Criar novo usuário
            JPanel reportsPanel = createReportsPanel(); // Excel, PDF etc.


            cardPanel.add(mainPanel, "Main");
            cardPanel.add(loginPanel, "Login");
            cardPanel.add(configPanel, "Config");
            cardPanel.add(activeUsersPanel, "activeUsers");
            cardPanel.add(reportsPanel, "Reports");
            cardPanel.add(newUserPanel, "newUser");


            telaPrincipal.setVisible(true);
        }

        public static AlertaDTO desserializarAlerta(String mensagemJson) throws Exception {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(mensagemJson, AlertaDTO.class);
        }

        public void showInterface() {
            SwingUtilities.invokeLater(this::initApplication);
            try {
                UIManager.setLookAndFeel(new FlatLightLaf()); //Carrega o Look and feel
                alertaConsumer = new AlertaConsumer(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onMessageReceived(String mensagemJson) {
            SwingUtilities.invokeLater(() -> {
                try {
                    AlertaDTO alerta = desserializarAlerta(mensagemJson);

                    mostrarPopup(alerta);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                atualizarTabelaAlertas();
            });
        }

        private void mostrarPopup(AlertaDTO alerta) {
            if (popupFrame != null && popupFrame.isVisible()) {
                popupFrame.dispose();
            }
            JFrame bloqueioFrame = new JFrame();
            bloqueioFrame.setUndecorated(true);
            bloqueioFrame.setAlwaysOnTop(true);
            bloqueioFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);


            bloqueioFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            bloqueioFrame.setBackground(new Color(151, 149, 149, 200)); // Fundo semitransparente

                String colorName = alerta.getCodigo();

                   switch (colorName){

                       case "Vermelho":
                           bloqueioFrame.setBackground(new Color(228, 56, 56, 200));

                           break;

                       case "Amarelo":
                           bloqueioFrame.setBackground(new Color(204, 204, 68, 200));

                           break;
                       case "Rosa":
                           bloqueioFrame.setBackground(new Color(227, 141, 227, 200));

                            break;
                       case "Verde":
                           bloqueioFrame.setBackground(new Color(70, 216, 70, 200));

                           break;
                       default:
                           bloqueioFrame.setBackground(new Color(151, 149, 149, 200));

                           break;
                   }

            String caminhoImagemBrigada = getClass().getResource("/images/SimboloBrigada.png").toString();
            JLabel mensagemLabel = new JLabel("<html>" +
                    "<div style='text-align:center;'>" +
                    "<img src='" + caminhoImagemBrigada + "' width='200' height='200'>" +
                    "</div>" +
                    "<h1 style='text-align:center; font-size:25px;'><strong>Código: </strong>"  + alerta.getCodigo() + "</h1>" +
                    "<p style='font-size:25px; text-align:center;'><strong>Setor: </strong>" + alerta.getTitulo() + "</p>" +
                    "<p style='font-size:30px; text-align:center;'><strong>Descrição: </strong>" + alerta.getDescricao() + "</p>" +
                    "<p style='font-size:20px; text-align:center;'><strong>Data/Hora: </strong>" + alerta.getTempoFormatado() + "</p>" +
                    "</html>");
            mensagemLabel.setHorizontalAlignment(SwingConstants.CENTER);
            bloqueioFrame.add(mensagemLabel);

            bloqueioFrame.setVisible(true);

            Timer cooldownTimer = new Timer(30000, e -> {
                bloqueioFrame.dispose(); // Fecha a tela de bloqueio
            });
            cooldownTimer.setRepeats(false); // Garante execução única
            cooldownTimer.start();
        }

        SwingWorker <Void, Void> consumerWorker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                alertaConsumer = new AlertaConsumer(SwingManager.this);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Erro ao iniciar o consumidor: " + e.getMessage());
                }
                consumerWorker.execute();
            }
        };


        private JPanel createMainPanel() {
            JPanel panelMain = new JPanel();
            panelMain.setLayout(new BorderLayout());
            panelMain.setBorder(null);

            // Painel superior para botões de navegação
            JPanel panelSuperior = new JPanel();
            panelSuperior.setLayout(new BoxLayout(panelSuperior, BoxLayout.X_AXIS));
            panelSuperior.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JXButton botaoLogin = new JXButton();
            atualizarNomeBotao(botaoLogin);
            botaoLogin.addActionListener(e -> {
                if (usuarioLogado == null) {
                    cardLayout.show(cardPanel, "Login");
                } else {
                    int confirmacao = JOptionPane.showOptionDialog(null, "Deseja sair do usuário ?", "Confirmação",
                            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{"Sim", "Não"}, "Não");

                    if (confirmacao == JOptionPane.YES_OPTION) {
                        usuarioLogado = null;
                        JOptionPane.showMessageDialog(null, "Você foi desconectado!");
                        atualizarTabelaAlertas();
                        cardLayout.show(cardPanel, "Main");
                        atualizarNomeBotao(botaoLogin);
                        autenticacaoBotoes(botoesParaAutenticar);
                    }
                }
            });

            JXButton botaoConfiguracao = new JXButton("Configurações");
            botaoConfiguracao.addActionListener(e -> cardLayout.show(cardPanel, "Config"));

            JXButton botaoReports = new JXButton("Gerar relatórios");
            botaoReports.addActionListener(e -> cardLayout.show(cardPanel, "Reports"));

            botoesParaAutenticar.add(botaoReports);
            botoesParaAutenticar.add(botaoConfiguracao);
            autenticacaoBotoes(botoesParaAutenticar);



            panelSuperior.add(Box.createHorizontalGlue());
            panelSuperior.add(botaoReports);
            panelSuperior.add(Box.createHorizontalStrut(10));
            panelSuperior.add(botaoConfiguracao);
            panelSuperior.add(Box.createHorizontalStrut(10));
            panelSuperior.add(botaoLogin);

            // Painel central para a tabela
            JPanel panelCentral = new JPanel();
            panelCentral.setLayout(new BorderLayout());
            panelCentral.setBorder(null);

            tabelaAlertas = new JXTable(new DefaultTableModel(new Object[]{"Código", "Local", "Descrição", "Tempo"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            });
            JScrollPane scrollPane = new JScrollPane(tabelaAlertas);
            panelCentral.add(scrollPane, BorderLayout.CENTER);

            JPanel panelInferior = new JPanel();
            panelInferior.setLayout(new BoxLayout(panelInferior, BoxLayout.Y_AXIS));
            panelInferior.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JPanel panelCampos = new JPanel();
            panelCampos.setLayout(new BoxLayout(panelCampos, BoxLayout.X_AXIS));
            panelCampos.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

            JXTextField campoTituloOcorrencia = new JXTextField();
            campoTituloOcorrencia.setPrompt("Digite o local da ocorrência...");
            panelCampos.add(campoTituloOcorrencia);
            panelCampos.add(Box.createHorizontalStrut(10));

            JXTextField campoCodigoOcorrencia = new JXTextField();
            campoCodigoOcorrencia.setPrompt("Selecione o código da ocorrência...");
            campoCodigoOcorrencia.setEditable(false);
            panelCampos.add(campoCodigoOcorrencia);
            panelCampos.add(Box.createHorizontalStrut(10));

            String[] opcoesCodigo = {"Amarelo", "Vermelho", "Verde", "Rosa"};
            JComboBox<String> seletor = new JComboBox<>(opcoesCodigo);
            seletor.addActionListener(e -> campoCodigoOcorrencia.setText((String) seletor.getSelectedItem()));
            panelCampos.add(seletor);
            panelCampos.add(Box.createHorizontalStrut(10));

            JXTextField campoDescricaoOcorrencia = new JXTextField();
            campoDescricaoOcorrencia.setPrompt("Digite uma descrição...");
            panelCampos.add(campoDescricaoOcorrencia);

            JButton botaoEnviarOcorrencia = new JButton("Enviar alerta");
            botaoEnviarOcorrencia.setToolTipText("Clique aqui para enviar o alerta");
            botaoEnviarOcorrencia.addActionListener(e -> {
                if (usuarioLogado == null || !usuarioLogado.isAdmin()) {
                    JOptionPane.showMessageDialog(null, "Acesso negado. Apenas administradores podem enviar alertas.");
                    return;
                }
                SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() {
                        try {
                            String titulo = campoTituloOcorrencia.getText();
                            String codigo = campoCodigoOcorrencia.getText();
                            String descricao = campoDescricaoOcorrencia.getText();

                            if (campoCodigoOcorrencia.getText().trim().isEmpty() || campoTituloOcorrencia.getText().trim().isEmpty()) {
                                JOptionPane.showMessageDialog(null, "Preencha os campos obrigatórios 'código' e 'título'");
                                return null;
                            }
                            AlertaDTO alerta = new AlertaDTO(codigo, titulo, LocalDateTime.now(), descricao);

                            AlertaDAO alertaDAO = new AlertaDAO();
                            alertaDAO.salvarAlerta(alerta);

                            rabbitMQClient.enviarAlerta(alerta.toJson());

                            SwingUtilities.invokeLater(()-> atualizarTabelaAlertas());

                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(null, "Erro ao enviar o alerta: " + ex.getMessage());
                        }
                        return null;
                    }

                    @Override
                    protected void done() {
                        // Chama após o término de doInBackground()
                        try {
                            get(); // Pode lançar exceção, que deve ser tratada
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(null, "Erro durante a execução: " + e.getMessage());
                        }
                    }
                };
                worker.execute();
            });

            panelInferior.add(panelCampos);
            panelInferior.add(Box.createVerticalStrut(10));
            panelInferior.add(botaoEnviarOcorrencia);

            // Adiciona os painéis ao painel principal
            panelMain.add(panelSuperior, BorderLayout.NORTH);
            panelMain.add(panelCentral, BorderLayout.CENTER);
            panelMain.add(panelInferior, BorderLayout.SOUTH);

            atualizarTabelaAlertas();
            return panelMain;
        }

        private JPanel createConfigPanel() {
            JPanel panelConfig = new JPanel(new BorderLayout());
            panelConfig.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            // Painel para layout centralizado
            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            panelConfig.add(contentPanel, BorderLayout.CENTER);

            // Botão para verificar usuários
            JButton botaoUsuarios = new JButton("Ver usuários");
            botaoUsuarios.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
            botaoUsuarios.setPreferredSize(new Dimension(200, 40));
            botaoUsuarios.addActionListener(e -> cardLayout.show(cardPanel, "activeUsers"));
            contentPanel.add(botaoUsuarios);
            contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));


            // Ajuste do layout final
            panelConfig.add(Box.createVerticalGlue(), BorderLayout.NORTH);
            panelConfig.add(contentPanel, BorderLayout.CENTER);
            panelConfig.add(Box.createVerticalGlue(), BorderLayout.SOUTH);

            return panelConfig;

        }

        private JPanel createLoginPanel() {
            JPanel panelLogin = new JPanel();
            panelLogin.setLayout(new BorderLayout());

            // Criar painel de campos com BoxLayout
            JPanel panelCampos = new JPanel();
            panelCampos.setLayout(new BoxLayout(panelCampos, BoxLayout.Y_AXIS));
            panelCampos.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // Ajuste do border

            // Criar painel de botões com BoxLayout
            JPanel panelBotoes = new JPanel();
            panelBotoes.setLayout(new BoxLayout(panelBotoes, BoxLayout.Y_AXIS));
            panelBotoes.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // Ajuste do border

            // Configurar campo de usuário
            JXTextField campoUsuario = new JXTextField();
            campoUsuario.setPreferredSize(new Dimension(200, 30)); // Tamanho ajustado
            campoUsuario.setMaximumSize(new Dimension(200, 50));
            campoUsuario.setPrompt("Digite seu usuário...");
            panelCampos.add(campoUsuario);
            panelCampos.add(Box.createVerticalStrut(10));

            // Configurar campo de senha
            JPasswordField campoSenha = new JPasswordField();
            campoSenha.setPreferredSize(new Dimension(200, 30)); // Tamanho ajustado
            campoSenha.setMaximumSize(new Dimension(200, 50));
            panelCampos.add(campoSenha);
            panelCampos.add(Box.createVerticalStrut(10));

            // Configurar botão de login
            JButton botaoFazerLogin = new JButton("Fazer login");
            botaoFazerLogin.addActionListener(e -> {
                String nome = campoUsuario.getText();
                String senha = new String(campoSenha.getPassword());

                UsuarioDAO usuarioDAO = new UsuarioDAO();
                UsuarioDTO usuario = usuarioDAO.autenticarUsuario(nome, senha);

                if (usuario != null) {
                    usuarioLogado = usuario;
                    JOptionPane.showMessageDialog(null, "Bem-vindo, " + usuario.getNome() + " ! \r Feche a aplicação para atualizá-la ! ");

                    cardLayout.show(cardPanel, "Main");
                   autenticacaoBotoes(botoesParaAutenticar);
                    atualizarTabelaAlertas();
                    adicionarTabelaUsuarios();


                } else {
                    JOptionPane.showMessageDialog(null, "Usuário ou senha inválidos.");
                }
            });
            panelBotoes.add(botaoFazerLogin);
            panelBotoes.add(Box.createVerticalStrut(10));

            // Configurar botão de voltar
            JXButton botaoVoltarParaAlertas = new JXButton("Voltar");
            panelBotoes.add(botaoVoltarParaAlertas);
            botaoVoltarParaAlertas.addActionListener(e -> cardLayout.show(cardPanel, "Main"));
            panelBotoes.add(Box.createVerticalStrut(10));

            // Centralizar os painéis
            panelLogin.add(panelCampos, BorderLayout.CENTER);
            panelLogin.add(panelBotoes, BorderLayout.SOUTH);

            // Centralizar o painelLogin dentro do container
            panelLogin.setLayout(new GridBagLayout()); // Usando GridBagLayout para centralização
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.CENTER;
            panelLogin.add(panelCampos, gbc);

            gbc.gridy = 1;
            panelLogin.add(panelBotoes, gbc);

            return panelLogin;
        }


        private JPanel createactiveUsersPanel() {
            JPanel panelUsuariosAtivos = new JPanel();
            GroupLayout layout = new GroupLayout(panelUsuariosAtivos);
            panelUsuariosAtivos.setLayout(layout);

            tabelaUsuarios = new JXTable(new DefaultTableModel(new Object[]{"ID", "Nome", "Administrador"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false; //Não permite editar a tabela
                }
            });

            JScrollPane scrollPane = new JScrollPane(tabelaUsuarios);

            JXButton botaoVoltarMain = new JXButton("Voltar para a tela principal");
            botaoVoltarMain.addActionListener(e -> cardLayout.show(cardPanel, "Main"));

            JPopupMenu popupMenuConfig = new JPopupMenu();
            JMenuItem item1Config = new JMenuItem("Criar novo usuário");
            popupMenuConfig.add(item1Config);
            tabelaUsuarios.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    showPopup(e);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    showPopup(e);
                }

                private void showPopup(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        popupMenuConfig.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            });

            item1Config.addActionListener(e -> cardLayout.show(cardPanel, "newUser"));

            JMenuItem item2EditarUser = new JMenuItem("Editar usuário");
            popupMenuConfig.add(item2EditarUser);

            item2EditarUser.addActionListener(e -> {
                int linhaSelecionada = tabelaUsuarios.getSelectedRow();
                if (tabelaUsuarios.getSelectedRow() == -1) {
                    return;
                }
                Long idUsuario = (Long) tabelaUsuarios.getValueAt(linhaSelecionada, 0);

                JPanel editUserPanel = createEditUserPanel(idUsuario);
                cardPanel.add(editUserPanel, "Edit");
                cardLayout.show(cardPanel, "Edit");
            });

            JMenuItem item3EditarUser = new JMenuItem("Deletar usuário");
            popupMenuConfig.add(item3EditarUser);

            item3EditarUser.addActionListener(e -> {
                UsuarioDAO usuarioDao = new UsuarioDAO();
                int linhaSelecionada = tabelaUsuarios.getSelectedRow();

                if (tabelaUsuarios.getSelectedRow() == -1) {
                    return;
                }
                Long idUsuario = (Long) tabelaUsuarios.getValueAt(linhaSelecionada, 0);

                int confirmacao = JOptionPane.showOptionDialog(null, "Deseja deletar o usuário?", "Confirmação",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{"Sim", "Não"}, "Não");

                if (confirmacao == JOptionPane.YES_NO_OPTION) {
                    usuarioDao.deletarUsuarioPorId(idUsuario);
                    adicionarTabelaUsuarios();
                    JOptionPane.showMessageDialog(null, "Usuário deletado com sucesso!");
                }
            });

            layout.setAutoCreateGaps(true);
            layout.setAutoCreateContainerGaps(true);

            layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(scrollPane)
                    .addGroup(layout.createSequentialGroup()
                            .addGap(0, 0, Short.MAX_VALUE)
                            .addComponent(botaoVoltarMain)
                    )
            );

            layout.setVerticalGroup(layout.createSequentialGroup()
                    .addComponent(scrollPane)
                    .addGap(20)
                    .addComponent(botaoVoltarMain)
            );
            adicionarTabelaUsuarios();
            return panelUsuariosAtivos;
            
        }

        private JPanel createnewUserPanel() {
            JPanel panelCriacaoUsuario = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0;
            gbc.gridy = 0;

            JXTextField campoNovoUsuario = new JXTextField();
            campoNovoUsuario.setPrompt("Digite o nome do novo usuário...");
            panelCriacaoUsuario.add(new JLabel("Nome do usuário:"), gbc);

            gbc.gridy++;
            panelCriacaoUsuario.add(campoNovoUsuario, gbc);

            JPasswordField campoSenhaUsuario = new JPasswordField();
            gbc.gridy++;
            panelCriacaoUsuario.add(new JLabel("Senha do usuário:"), gbc);

            gbc.gridy++;
            panelCriacaoUsuario.add(campoSenhaUsuario, gbc);

            JCheckBox checkboxAdmin = new JCheckBox("Usuário administrador");
            gbc.gridy++;
            panelCriacaoUsuario.add(checkboxAdmin, gbc);

            JXButton botaoCriarUsuario = new JXButton("Criar usuário");
            gbc.gridy++;
            panelCriacaoUsuario.add(botaoCriarUsuario, gbc);

            JButton botaoVoltar = new JButton("Voltar");
            gbc.gridy++;
            panelCriacaoUsuario.add(botaoVoltar, gbc);

            botaoCriarUsuario.addActionListener(e -> {
                String nomeUsuario = campoNovoUsuario.getText();
                String senhaUsuario = new String(campoSenhaUsuario.getPassword());
                boolean novoAdmin = checkboxAdmin.isSelected();

                if (nomeUsuario.trim().isEmpty() || senhaUsuario.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Por favor, preencha os campos!");
                    return;
                }
                try {
                    UsuarioDTO novoUsuario = new UsuarioDTO();
                    novoUsuario.setNome(nomeUsuario);
                    novoUsuario.setSenha(senhaUsuario);
                    novoUsuario.setAdmin(novoAdmin);

                    UsuarioDAO usuarioDAO = new UsuarioDAO();
                    usuarioDAO.salvarUsuario(novoUsuario);

                    JOptionPane.showMessageDialog(null, "Usuário criado com sucesso!");
                    adicionarTabelaUsuarios();
                    cardLayout.show(cardPanel, "activeUsers");

                } catch (Exception exception) {
                    JOptionPane.showMessageDialog(null, "Erro ao criar usuário! " + exception.getMessage());
                }
            });

            botaoVoltar.addActionListener(e -> cardLayout.show(cardPanel, "activeUsers"));

            return panelCriacaoUsuario;
        }

        private JPanel createEditUserPanel(Long idUsuario) {
            JPanel panelEditarUserAtual = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0;
            gbc.gridy = 0;

            UsuarioDAO usuarioDAO = new UsuarioDAO();
            UsuarioDTO usuarioSelecionado = usuarioDAO.buscarUsuarioPorId(idUsuario);

            if (usuarioSelecionado == null) {
                JOptionPane.showMessageDialog(null, "Usuário não encontrado.");
                return panelEditarUserAtual;
            }

            JXTextField campoUsuarioAtual = new JXTextField();
            campoUsuarioAtual.setText(usuarioSelecionado.getNome());
            panelEditarUserAtual.add(new JLabel("Nome do usuário:"), gbc);

            gbc.gridy++;
            panelEditarUserAtual.add(campoUsuarioAtual, gbc);

            JPasswordField campoSenhaUsuarioAtual = new JPasswordField();
            gbc.gridy++;
            panelEditarUserAtual.add(new JLabel("Senha do usuário:"), gbc);

            gbc.gridy++;
            panelEditarUserAtual.add(campoSenhaUsuarioAtual, gbc);

            JCheckBox checkboxAdmin = new JCheckBox("Usuário administrador");
            checkboxAdmin.setSelected(usuarioSelecionado.isAdmin());
            gbc.gridy++;
            panelEditarUserAtual.add(checkboxAdmin, gbc);

            JXButton botaoSalvarUsuario = new JXButton("Salvar Usuário");
            gbc.gridy++;
            panelEditarUserAtual.add(botaoSalvarUsuario, gbc);

            JXButton botaoVoltar = new JXButton("Voltar");
            gbc.gridy++;
            panelEditarUserAtual.add(botaoVoltar, gbc);

            botaoSalvarUsuario.addActionListener(e -> {
                String nome = campoUsuarioAtual.getText();
                String senha = new String(campoSenhaUsuarioAtual.getPassword());
                boolean isAdmin = checkboxAdmin.isSelected();

                if (nome.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "O campo 'nome' não pode ser vazio!");
                    return;
                }

                if (senha.isEmpty()) {
                    senha = usuarioSelecionado.getSenha();
                }

                usuarioSelecionado.setNome(nome);
                usuarioSelecionado.setSenha(senha);
                usuarioSelecionado.setAdmin(isAdmin);

                usuarioDAO.atualizarUsuario(usuarioSelecionado);
                adicionarTabelaUsuarios();

                JOptionPane.showMessageDialog(null, "Usuário salvo com sucesso!");
                cardLayout.show(cardPanel, "activeUsers");
            });

            botaoVoltar.addActionListener(e -> cardLayout.show(cardPanel, "activeUsers"));

            return panelEditarUserAtual;
        }

        private JPanel createReportsPanel() {

            JPanel panelReports = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0;
            gbc.gridy = 0;

            JXButton botaoGerarPDF = new JXButton("Gerar PDF");
            panelReports.add(botaoGerarPDF, gbc);

            gbc.gridy++;
            JXButton botaoGerarExcel = new JXButton("Gerar Excel");
            panelReports.add(botaoGerarExcel, gbc);


            gbc.gridy++;
            JXButton botaoVoltarMain = new JXButton("Voltar para a tela principal");
            panelReports.add(botaoVoltarMain, gbc);

            botaoGerarExcel.addActionListener(e -> excelGenerator.gerarRelatorioExcel(10));
            botaoGerarPDF.addActionListener(e-> pdfGenerator.gerarRelatorioPDF(10));
            botaoVoltarMain.addActionListener(e -> cardLayout.show(cardPanel, "Main"));

            return panelReports;
        }



       //MÉTODOS UNIVERSAIS

        private void atualizarTabelaAlertas() {
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

        private void adicionarTabelaUsuarios(){
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


        private void autenticacaoBotoes(ArrayList<JXButton> botoes){

            if(usuarioLogado == null){

                for (JXButton botao : botoes){

                    botao.setEnabled(false);

                    System.out.println("Iterei aqui ein FALSE");

                }

            }

            if(usuarioLogado != null) {

                for (JXButton botao : botoes) {

                    botao.setEnabled(true);
                    System.out.println("Iterei aqui ein TRUE");
                }
            }
        }

        private void atualizarNomeBotao(JXButton botao){
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