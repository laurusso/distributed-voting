/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */

/**
 *
 * @author l235826
 */
package com.voting.client;

import com.voting.common.Vote;
import com.voting.common.VotingPacket;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import javax.swing.*; // Importa todos os componentes Swing
import java.awt.event.ActionEvent;

/**
 * Interface Gráfica (GUI) principal do Cliente.
 * Usamos JFrame para criar a janela.
 */
public class ClientGUI extends JFrame {

    // --- Componentes da GUI ---
    private JTextField txtIpServidor;
    private JTextField txtPorta;
    private JButton btnConectar;
    
    private JLabel lblPergunta;
    private JComboBox<String> comboOpcoes; // Dropdown para as opções
    private JTextField txtCpf;
    private JButton btnVotar;
    
    private JLabel lblStatus;

    // --- Componentes de Rede ---
    private ClientNetworkManager networkManager;
    private VotingPacket votingPacket; // Pacote recebido do servidor

    /**
     * Construtor
     */
    public ClientGUI() {
        // Configurações básicas da janela
        setTitle("Cliente de Votacao");
        setSize(450, 400); // Largura x Altura
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null); // Layout manual (mais simples para este exemplo)
        
        // --- NOVO: Barra de Menu ---
        JMenuBar menuBar = new JMenuBar();
        
        JMenu helpMenu = new JMenu("Ajuda");
        JMenuItem aboutItem = new JMenuItem("Sobre...");
        JMenuItem creditsItem = new JMenuItem("Créditos");
        
        helpMenu.add(aboutItem);
        helpMenu.add(creditsItem);
        menuBar.add(helpMenu);
        
        // Define a barra de menu para esta janela
        setJMenuBar(menuBar);

        // Ação do item "Sobre"
        aboutItem.addActionListener((e) -> {
            JOptionPane.showMessageDialog(this,
                "Cliente de Votação Distribuída\nVersão 1.0",
                "Sobre",
                JOptionPane.INFORMATION_MESSAGE);
        });

        // Ação do item "Créditos"
        creditsItem.addActionListener((e) -> {
            JOptionPane.showMessageDialog(this,
                "Desenvolvido por:\n Guilherme Freitas Costa 235946\nLaura Rodrigues Russo 235826\nLucas de Oliveira Lopes Cardoso 269538\nMaria Clara Marsola Paulini 219443\nWesley Henrique Batista Sant'Anna 284045\nDisciplina: SI400B",
                "Créditos",
                JOptionPane.INFORMATION_MESSAGE);
        });
        // --- FIM DA Barra de Menu ---

        // --- Seção de Conexão ---
        add(new JLabel("IP Servidor:")).setBounds(10, 10, 80, 25);
        txtIpServidor = new JTextField("127.0.0.1"); // "127.0.0.1" é o localhost (esta máquina)
        txtIpServidor.setBounds(90, 10, 100, 25);
        add(txtIpServidor);

        add(new JLabel("Porta:")).setBounds(200, 10, 40, 25);
        txtPorta = new JTextField("5000");
        txtPorta.setBounds(240, 10, 60, 25);
        add(txtPorta);

        btnConectar = new JButton("Conectar");
        btnConectar.setBounds(310, 10, 100, 25);
        add(btnConectar);

        // --- Seção de Votação ---
        lblPergunta = new JLabel("Pergunta: (conecte-se para carregar)");
        lblPergunta.setBounds(10, 50, 380, 25);
        add(lblPergunta);
        
        comboOpcoes = new JComboBox<>();
        comboOpcoes.setBounds(10, 80, 380, 25);
        add(comboOpcoes);

        add(new JLabel("Seu CPF:")).setBounds(10, 120, 80, 25);
        txtCpf = new JTextField();
        txtCpf.setBounds(90, 120, 150, 25);
        add(txtCpf);

        btnVotar = new JButton("Votar");
        btnVotar.setBounds(250, 120, 80, 25);
        add(btnVotar);

        // --- Seção de Status ---
        lblStatus = new JLabel("Status: Desconectado.");
        lblStatus.setBounds(10, 280, 380, 25);
        add(lblStatus);

        // --- Ações dos Botões ---
        
        // Ação do botão Conectar
        btnConectar.addActionListener((ActionEvent e) -> {
            conectarAoServidor();
        });

        // Ação do botão Votar
        btnVotar.addActionListener((ActionEvent e) -> {
            enviarVoto();
        });

        // Habilita/Desabilita campos no início
        setVotingEnabled(false);
    }

    /**
     * Tenta se conectar ao servidor.
     * Isso deve rodar em uma thread separada para não travar a GUI.
     */
    private void conectarAoServidor() {
        String ip = txtIpServidor.getText();
        int porta = Integer.parseInt(txtPorta.getText());

        lblStatus.setText("Status: Conectando...");

        // Cria o nosso gerenciador de rede
        this.networkManager = new ClientNetworkManager(); 

        new Thread(() -> {
            try {
                // 1. Conectar e receber o pacote (usando o "encanador")
                votingPacket = networkManager.connectAndGetPacket(ip, porta);

                // 2. Atualizar a GUI
                SwingUtilities.invokeLater(() -> {
                    preencherDadosVotacao(votingPacket);
                    lblStatus.setText("Status: Conectado! Pronto para votar.");
                    setVotingEnabled(true);
                    btnConectar.setEnabled(false);
                });

            } catch (IOException | ClassNotFoundException ex) {
                SwingUtilities.invokeLater(() -> {
                    lblStatus.setText("Erro: " + ex.getMessage());
                });
            }
        }).start();
    }

    /**
     * Preenche a GUI com os dados recebidos do servidor.
     */
    private void preencherDadosVotacao(VotingPacket packet) {
        lblPergunta.setText(packet.getQuestion());
        
        // Limpa opções antigas e adiciona as novas
        comboOpcoes.removeAllItems();
        List<String> options = packet.getOptions();
        for (String option : options) {
            comboOpcoes.addItem(option);
        }
    }
    
    /**
     * Tenta enviar o voto para o servidor.
     * Isso também deve rodar em uma thread separada.
     */
    private void enviarVoto() {
        String cpf = txtCpf.getText();
        int selectedIndex = comboOpcoes.getSelectedIndex();

        if (cpf.isEmpty() || selectedIndex == -1) {
            lblStatus.setText("Status: Preencha o CPF e escolha uma opcao.");
            return;
        }

        if (!CPFValidator.isValidCPF(cpf)) {
            lblStatus.setText("Status: CPF invalido! Verifique os digitos.");
            return;
        }

        lblStatus.setText("Status: Enviando voto...");
        setVotingEnabled(false);

        new Thread(() -> {
            try {
                // 1. Criar o objeto de Voto
                Vote vote = new Vote(cpf, selectedIndex);

                // 2. Enviar o voto (usando o "encanador")
                networkManager.sendVote(vote);

                // 3. Atualizar GUI
                SwingUtilities.invokeLater(() -> {
                    lblStatus.setText("Status: Voto enviado com sucesso! Desconectado.");
                    btnVotar.setEnabled(false);
                });

            } catch (IOException ex) {
                SwingUtilities.invokeLater(() -> {
                    lblStatus.setText("Erro ao enviar voto: " + ex.getMessage());
                    setVotingEnabled(true);
                });
            }
        }).start();
    }
    
    /**
     * Habilita/desabilita os campos de votação.
     */
    private void setVotingEnabled(boolean enabled) {
        txtCpf.setEnabled(enabled);
        comboOpcoes.setEnabled(enabled);
        btnVotar.setEnabled(enabled);
    }
    
    /**
     * Método Main para iniciar a GUI.
     */
    public static void main(String args[]) {
        // Roda a criação da GUI na Thread de Eventos do Swing
        SwingUtilities.invokeLater(() -> {
            new ClientGUI().setVisible(true);
        });
    }
}