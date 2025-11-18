/*The administrative GUI for the server. It allows the user to configure the election, 
start the listening network process, display real-time server logs and partial results,
and generate a final report upon election conclusion.*/

package com.voting.server;

import com.voting.common.Vote;
import com.voting.common.VotingPacket;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections; 
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder; 

public class ServerGUI extends JFrame {

    //components
    private JTextArea logArea;
    private DefaultListModel<String> listModelResults;
    private JList<String> listResults;
    
    private JPanel configPanel;
    private JTextField txtQuestion;
    private JTextField txtNewOption;
    private JButton btnAddOption;
    private JButton btnStartElection;
    private DefaultListModel<String> listModelOptions;
    private JList<String> listOptions;

    //control button
    private JButton btnEndElection;

    private VotingServer serverCore;
    private ServerNetworkManager networkManager;

    public ServerGUI() {
        setTitle("Servidor de Votação");
        setSize(700, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        //menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenu helpMenu = new JMenu("Ajuda");
        JMenuItem aboutItem = new JMenuItem("Sobre");
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
        creditsItem.addActionListener((e) -> JOptionPane.showMessageDialog(this, 
                "Desenvolvido por:\nGuilherme Freitas Costa 235946\nLaura Rodrigues Russo 235826\nLucas de Oliveira Lopes Cardoso 269538\nMaria Clara Marsola Paulini 219443\nWesley Henrique Batista Sant'Anna 284045\nDisciplina: SI400B", 
                "Créditos", JOptionPane.INFORMATION_MESSAGE));
        
        //config panel
        configPanel = new JPanel(new BorderLayout(10, 10));
        configPanel.setBorder(new TitledBorder(new EmptyBorder(10, 10, 10, 10), "Configuração da Eleição"));
        
        //question area
        txtQuestion = new JTextField("Qual sua linguagem favorita?"); //default question
        configPanel.add(txtQuestion, BorderLayout.NORTH);
        
        //options list
        listModelOptions = new DefaultListModel<>();
        listOptions = new JList<>(listModelOptions);
        JScrollPane optionsScrollPane = new JScrollPane(listOptions);
        optionsScrollPane.setPreferredSize(new Dimension(200, 100));
        configPanel.add(optionsScrollPane, BorderLayout.CENTER);
        
        //add options panel
        JPanel addPanel = new JPanel(new BorderLayout());
        txtNewOption = new JTextField();
        btnAddOption = new JButton("Adicionar Opção");
        addPanel.add(txtNewOption, BorderLayout.CENTER);
        addPanel.add(btnAddOption, BorderLayout.EAST);
        configPanel.add(addPanel, BorderLayout.SOUTH);
        
        //start button
        btnStartElection = new JButton("INICIAR ELEIÇÃO");
        btnStartElection.setFont(new Font("SansSerif", Font.BOLD, 14));
        configPanel.add(btnStartElection, BorderLayout.EAST);

        add(configPanel, BorderLayout.NORTH);

        //log panel and results
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.6); 
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(new EmptyBorder(10, 10, 10, 10)); 
        logPanel.add(new JLabel("Log do Servidor:"), BorderLayout.NORTH);
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane logScrollPane = new JScrollPane(logArea);
        logPanel.add(logScrollPane, BorderLayout.CENTER);
        JPanel resultsPanel = new JPanel(new BorderLayout());
        resultsPanel.setBorder(new EmptyBorder(10, 10, 10, 10)); 
        resultsPanel.add(new JLabel("Resultados Parciais:"), BorderLayout.NORTH);
        listModelResults = new DefaultListModel<>();
        listResults = new JList<>(listModelResults);
        JScrollPane resultsScrollPane = new JScrollPane(listResults);
        resultsPanel.add(resultsScrollPane, BorderLayout.CENTER);
        splitPane.setLeftComponent(logPanel);
        splitPane.setRightComponent(resultsPanel);
        
        add(splitPane, BorderLayout.CENTER);

        //buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); 
        btnEndElection = new JButton("Encerrar e Salvar Relatório");
        btnEndElection.setEnabled(false);
        buttonPanel.add(btnEndElection);
        
        add(buttonPanel, BorderLayout.SOUTH);

        btnAddOption.addActionListener((e) -> {
            String newOption = txtNewOption.getText().trim();
            if (!newOption.isEmpty()) {
                listModelOptions.addElement(newOption);
                txtNewOption.setText("");
            }
        });
        
        btnStartElection.addActionListener((e) -> {
            startElection();
        });
        
        btnEndElection.addActionListener((e) -> {
            endElection();
        });
        
        addLogMessage("--- Servidor pronto. Configure a eleição e clique em 'Iniciar'. ---");
    }

    private void startElection() {
        String question = txtQuestion.getText().trim();
        if (question.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, digite uma pergunta.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (listModelOptions.getSize() < 2) {
            JOptionPane.showMessageDialog(this, "Por favor, adicione pelo menos DUAS opções.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<String> options = Collections.list(listModelOptions.elements());
        
        //creates voting packet
        VotingPacket packet = new VotingPacket(question, options);
        
        this.serverCore = new VotingServer(this, packet);
        this.networkManager = new ServerNetworkManager(this.serverCore, this);
        
        //network thread
        new Thread(this.networkManager).start();
        
        //update GUI
        setElectionState(true); 
    }

    private void endElection() {
        addLogMessage("--- Votação Encerrada ---");

        if (networkManager != null) {
            networkManager.stopListening();
        }

        if (serverCore != null) {
            VotingPacket packet = serverCore.getCurrentVotingPacket();
            Map<String, Vote> votes = serverCore.getReceivedVotes();
            String reportContent = generateReport(packet, votes);
            saveReportToFile(reportContent);
        }
        
        //reset GUI
        setElectionState(false);
        listModelOptions.clear();
        listModelResults.clear();
    }
    
    private void setElectionState(boolean isRunning) {
        configPanel.setEnabled(!isRunning);
        txtQuestion.setEnabled(!isRunning);
        txtNewOption.setEnabled(!isRunning);
        btnAddOption.setEnabled(!isRunning);
        btnStartElection.setEnabled(!isRunning);
        listOptions.setEnabled(!isRunning);
        
        btnEndElection.setEnabled(isRunning);
    }
    
    //voting report
    private String generateReport(VotingPacket packet, Map<String, Vote> votes) {
        StringBuilder report = new StringBuilder();
        report.append("--- Relatório Final da Votação ---\n\n");
        report.append("Pergunta: ").append(packet.getQuestion()).append("\n\n");
        report.append("--- Resultados ---\n");
        List<String> options = packet.getOptions();
        int[] contagem = new int[options.size()];
        for (Vote vote : votes.values()) {
            int optionIndex = vote.getOptionIndex();
            if (optionIndex >= 0 && optionIndex < options.size()) {
                contagem[optionIndex]++;
            }
        }
        for (int i = 0; i < options.size(); i++) {
            report.append(options.get(i)).append(": ").append(contagem[i]).append(" voto(s)\n");
        }
        report.append("\n--- Votantes ---\n");
        report.append("Total de votos: ").append(votes.size()).append("\n");
        report.append("CPFs registrados:\n");
        for (String cpf : votes.keySet()) {
            report.append("- ").append(cpf).append("\n");
        }
        return report.toString();
    }
    
    //voting report as txt
    private void saveReportToFile(String reportContent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salvar Relatório");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Arquivos de Texto (.txt)", "txt"));
        fileChooser.setSelectedFile(new File("relatorio_votacao.txt")); 
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.endsWith(".txt")) {
                fileToSave = new File(filePath + ".txt");
            }
            try (FileWriter writer = new FileWriter(fileToSave)) {
                writer.write(reportContent);
                addLogMessage("Relatório salvo com sucesso em: " + fileToSave.getAbsolutePath());
                JOptionPane.showMessageDialog(this, "Relatório salvo com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                addLogMessage("Erro ao salvar relatório: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, "Erro ao salvar o relatório.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void addLogMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public void updateResults(List<String> options, Map<String, Vote> votes) {
        int[] contagem = new int[options.size()];
        for (Vote vote : votes.values()) {
            int optionIndex = vote.getOptionIndex();
            if (optionIndex >= 0 && optionIndex < options.size()) {
                contagem[optionIndex]++;
            }
        }
        SwingUtilities.invokeLater(() -> {
            listModelResults.clear();
            for (int i = 0; i < options.size(); i++) {
                String optionName = options.get(i);
                int count = contagem[i];
                listModelResults.addElement(optionName + ": " + count + " voto(s)");
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ServerGUI gui = new ServerGUI();
            gui.setVisible(true);
        });
    }
}