package com.voting.client;

import com.voting.common.CPFValidator;
import com.voting.common.Vote;
import com.voting.common.VotingPacket;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import javax.swing.*; 
import java.awt.event.ActionEvent;

/*Client GUI - interface */
public class ClientGUI extends JFrame {

    //components
    private JTextField txtIpServer;
    private JTextField txtPort;
    private JButton btnConnect;
    
    private JLabel lblQuestion;
    private JComboBox<String> optionsSelect; 
    private JTextField txtCpf;
    private JButton btnVote;
    
    private JLabel lblStatus;

    private ClientNetworkManager networkManager;
    private VotingPacket votingPacket; 


    public ClientGUI() {
        setTitle("Cliente de Votacao");
        setSize(450, 400); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        
        //menu bar
        JMenuBar menuBar = new JMenuBar();
        
        JMenu helpMenu = new JMenu("Ajuda");
        JMenuItem aboutItem = new JMenuItem("Sobre...");
        JMenuItem creditsItem = new JMenuItem("Créditos");
        
        helpMenu.add(aboutItem);
        helpMenu.add(creditsItem);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);

        aboutItem.addActionListener((e) -> {
            JOptionPane.showMessageDialog(this,
                "Projeto Votação Distribuída em Java - SI400B\n\n" +
                "Esta aplicação representa um sistema de votação eletrônica distribuída, " +
                "desenvolvido com o objetivo de aplicar e demonstrar conceitos de sistemas " +
                "distribuídos e comunicação de rede.\n\n" +

                "Arquitetura:\n" +
                "O sistema opera sob o modelo Cliente-Servidor, utilizando o protocolo TCP/IP " +
                "para garantir a comunicação confiável. A troca de informações (pacotes de " +
                "votação e votos) é realizada através de fluxos de objetos (Object Streams).\n\n" +

                "Funcionalidades Principais:\n" +
                "- Servidor Multithread: O servidor é implementado com multithreading para " +
                "gerenciar e atender a múltiplas conexões de clientes simultaneamente, " +
                "garantindo eficiência.\n" +
                "- Segurança e Integridade: O sistema valida o CPF do eleitor e implementa " +
                "mecanismos no servidor para prevenir a duplicação de votos, assegurando a " +
                "integridade da eleição.\n" +
                "- Monitoramento em Tempo Real: O servidor acompanha e exibe os resultados " +
                "parciais da votação em tempo real, além de gerar um relatório final.\n\n" +

                "O projeto foca na segurança, integridade e facilidade de uso em uma solução " +
                "prática de votação eletrônica.",
                "Sobre", JOptionPane.INFORMATION_MESSAGE);
        });

        creditsItem.addActionListener((e) -> {
            JOptionPane.showMessageDialog(this,
                "Desenvolvido por:\nGuilherme Freitas Costa 235946\nLaura Rodrigues Russo 235826\nLucas de Oliveira Lopes Cardoso 269538\nMaria Clara Marsola Paulini 219443\nWesley Henrique Batista Sant'Anna 284045\nDisciplina: SI400B",
                "Créditos",
                JOptionPane.INFORMATION_MESSAGE);
        });

        //connection section
        add(new JLabel("IP Servidor:")).setBounds(10, 10, 80, 25);
        txtIpServer = new JTextField("127.0.0.1"); // "127.0.0.1" - dafault
        txtIpServer.setBounds(90, 10, 100, 25);
        add(txtIpServer);

        add(new JLabel("Porta:")).setBounds(200, 10, 40, 25);
        txtPort = new JTextField("5000");
        txtPort.setBounds(240, 10, 60, 25);
        add(txtPort);

        btnConnect = new JButton("Conectar");
        btnConnect.setBounds(310, 10, 100, 25);
        add(btnConnect);

        //voting section
        lblQuestion = new JLabel("Pergunta: (conecte-se para carregar)");
        lblQuestion.setBounds(10, 50, 380, 25);
        add(lblQuestion);
        
        optionsSelect = new JComboBox<>();
        optionsSelect.setBounds(10, 80, 380, 25);
        add(optionsSelect);

        add(new JLabel("Seu CPF:")).setBounds(10, 120, 80, 25);
        txtCpf = new JTextField();
        txtCpf.setBounds(90, 120, 150, 25);
        add(txtCpf);

        btnVote = new JButton("Votar");
        btnVote.setBounds(250, 120, 80, 25);
        add(btnVote);

        //status section
        lblStatus = new JLabel("Status: Desconectado.");
        lblStatus.setBounds(10, 280, 380, 25);
        add(lblStatus);
        
        btnConnect.addActionListener((ActionEvent e) -> {
            connectServer();
        });

        btnVote.addActionListener((ActionEvent e) -> {
            sendVote();
        });

        setVotingEnabled(false);
    }

    private void connectServer() {
        String ip = txtIpServer.getText();
        int porta = Integer.parseInt(txtPort.getText());

        lblStatus.setText("Status: Conectando...");

        //creates the manager
        this.networkManager = new ClientNetworkManager(); 

        new Thread(() -> {
            try {
                //connect and receive packet
                votingPacket = networkManager.connectAndGetPacket(ip, porta);

                //update GUI
                SwingUtilities.invokeLater(() -> {
                    fillVotingData(votingPacket);
                    lblStatus.setText("Status: Conectado! Pronto para votar.");
                    setVotingEnabled(true);
                    btnConnect.setEnabled(false);
                });

            } catch (IOException | ClassNotFoundException ex) {
                SwingUtilities.invokeLater(() -> {
                    lblStatus.setText("Erro: " + ex.getMessage());
                });
            }
        }).start();
    }

    private void fillVotingData(VotingPacket packet) {
        lblQuestion.setText(packet.getQuestion());
        
        optionsSelect.removeAllItems();
        List<String> options = packet.getOptions();
        for (String option : options) {
            optionsSelect.addItem(option);
        }
    }
    
    private void sendVote() {
        String cpf = txtCpf.getText();
        int selectedIndex = optionsSelect.getSelectedIndex();

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

        lblStatus.setText("Status: Enviando voto...");
        setVotingEnabled(false);

        new Thread(() -> {
            try {
                Vote vote = new Vote(cpf, selectedIndex);

                // send vote and receive status message
                String serverResponse = networkManager.sendVote(vote); 

                SwingUtilities.invokeLater(() -> {
                    //process the answer
                    if (serverResponse.equalsIgnoreCase("SUCESSO")) {
                        lblStatus.setText("Status: Voto enviado com sucesso! Desconectado.");
                        btnVote.setEnabled(false);
                    } else if (serverResponse.contains("DUPLICADO")) {
                        lblStatus.setText("Status: Erro! CPF já votou.");
                        setVotingEnabled(true);
                    } else {
                        lblStatus.setText("Status: Erro do servidor: " + serverResponse);
                        setVotingEnabled(true);
                    }
                });

            } catch (IOException ex) {
                SwingUtilities.invokeLater(() -> {
                    lblStatus.setText("Erro de Conexão/Rede: " + ex.getMessage());
                    setVotingEnabled(true);
                });
            }
        }).start();
    }
    
    private void setVotingEnabled(boolean enabled) {
        txtCpf.setEnabled(enabled);
        optionsSelect.setEnabled(enabled);
        btnVote.setEnabled(enabled);
    }
    
    public static void main(String args[]) {
        SwingUtilities.invokeLater(() -> {
            new ClientGUI().setVisible(true);
        });
    }
}