// Delele a linha 'package' se você estiver no <default package>

import com.voting.common.Vote;
import com.voting.common.VotingPacket;
import java.awt.BorderLayout;
import java.awt.Dimension; // Import necessário
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList; // Import necessário
import java.util.Collections; // Import necessário
import java.util.List; // Import necessário
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder; // Import necessário

public class ServerGUI extends JFrame {

    // --- Componentes de Log/Resultado ---
    private JTextArea logArea;
    private DefaultListModel<String> listModelResultados;
    private JList<String> listResultados;
    
    // --- Componentes de Configuração (NOVOS) ---
    private JPanel configPanel;
    private JTextField txtQuestion;
    private JTextField txtNewOption;
    private JButton btnAddOption;
    private JButton btnStartElection;
    private DefaultListModel<String> listModelOptions;
    private JList<String> listOptions;

    // --- Botão de Controle ---
    private JButton btnEndElection;

    // --- Módulos Lógicos ---
    private VotingServer serverCore;
    private ServerNetworkManager networkManager;

    /**
     * Construtor da GUI
     */
    public ServerGUI() {
        setTitle("Servidor de Votação");
        setSize(700, 600); // Aumentei a altura
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- Barra de Menu ---
        JMenuBar menuBar = new JMenuBar();
        JMenu helpMenu = new JMenu("Ajuda");
        JMenuItem aboutItem = new JMenuItem("Sobre...");
        JMenuItem creditsItem = new JMenuItem("Créditos");
        helpMenu.add(aboutItem);
        helpMenu.add(creditsItem);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
        aboutItem.addActionListener((e) -> JOptionPane.showMessageDialog(this, "Servidor de Votação...", "Sobre", JOptionPane.INFORMATION_MESSAGE));
        creditsItem.addActionListener((e) -> JOptionPane.showMessageDialog(this, 
                "Desenvolvido por:\n Guilherme Freitas Costa 235946\nLaura Rodrigues Russo 235826\nLucas de Oliveira Lopes Cardoso 269538\nMaria Clara Marsola Paulini 219443\nWesley Henrique Batista Sant'Anna 284045\nDisciplina: SI400B", 
                "Créditos", JOptionPane.INFORMATION_MESSAGE));
        
        // --- NOVO PAINEL DE CONFIGURAÇÃO (Topo) ---
        configPanel = new JPanel(new BorderLayout(10, 10));
        configPanel.setBorder(new TitledBorder(new EmptyBorder(10, 10, 10, 10), "Configuração da Eleição"));
        
        // Campo da Pergunta
        txtQuestion = new JTextField("Qual sua linguagem favorita?");
        configPanel.add(txtQuestion, BorderLayout.NORTH);
        
        // Lista de Opções Adicionadas
        listModelOptions = new DefaultListModel<>();
        listOptions = new JList<>(listModelOptions);
        JScrollPane optionsScrollPane = new JScrollPane(listOptions);
        optionsScrollPane.setPreferredSize(new Dimension(200, 100)); // Tamanho da lista
        configPanel.add(optionsScrollPane, BorderLayout.CENTER);
        
        // Painel para adicionar opções
        JPanel addPanel = new JPanel(new BorderLayout());
        txtNewOption = new JTextField();
        btnAddOption = new JButton("Adicionar Opção");
        addPanel.add(txtNewOption, BorderLayout.CENTER);
        addPanel.add(btnAddOption, BorderLayout.EAST);
        configPanel.add(addPanel, BorderLayout.SOUTH);
        
        // Botão para INICIAR
        btnStartElection = new JButton("INICIAR ELEIÇÃO");
        btnStartElection.setFont(new Font("SansSerif", Font.BOLD, 14));
        configPanel.add(btnStartElection, BorderLayout.EAST);

        add(configPanel, BorderLayout.NORTH); // Adiciona o painel de config no topo

        // --- Painel de Log e Resultados (Centro) ---
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
        listModelResultados = new DefaultListModel<>();
        listResultados = new JList<>(listModelResultados);
        JScrollPane resultsScrollPane = new JScrollPane(listResultados);
        resultsPanel.add(resultsScrollPane, BorderLayout.CENTER);
        splitPane.setLeftComponent(logPanel);
        splitPane.setRightComponent(resultsPanel);
        
        add(splitPane, BorderLayout.CENTER); // Adiciona o painel de log/resultado no centro

        // --- Painel de Botões (Sul) ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); 
        btnEndElection = new JButton("Encerrar e Salvar Relatório");
        btnEndElection.setEnabled(false); // Desabilitado no início
        buttonPanel.add(btnEndElection);
        
        add(buttonPanel, BorderLayout.SOUTH); // Adiciona o painel de encerrar no final

        // --- AÇÕES ---
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

    /**
     * NOVO MÉTODO: Inicia o servidor com os dados da GUI.
     */
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

        // Converte o modelo da lista em uma List<String>
        List<String> options = Collections.list(listModelOptions.elements());
        
        // Cria o pacote de eleição
        VotingPacket packet = new VotingPacket(question, options);
        
        // Inicia o "cérebro" e a "rede"
        this.serverCore = new VotingServer(this, packet);
        this.networkManager = new ServerNetworkManager(this.serverCore, this);
        
        // Inicia a thread de rede
        new Thread(this.networkManager).start();
        
        // Atualiza a GUI
        setElectionState(true); // Bloqueia a config, habilita o encerramento
    }

    /**
     * Ação do botão "Encerrar e Salvar Relatório"
     */
    private void endElection() {
        addLogMessage("--- Votação Encerrada. ---");

        if (networkManager != null) {
            networkManager.stopListening();
        }

        if (serverCore != null) {
            VotingPacket packet = serverCore.getCurrentVotingPacket();
            Map<String, Vote> votes = serverCore.getReceivedVotes();
            String reportContent = generateReport(packet, votes);
            saveReportToFile(reportContent);
        }
        
        // Reseta a GUI
        setElectionState(false);
        listModelOptions.clear(); // Limpa as opções antigas
        listModelResultados.clear(); // Limpa os resultados antigos
    }
    
    /**
     * NOVO MÉTODO: Habilita/desabilita os painéis de controle
     */
    private void setElectionState(boolean isRunning) {
        configPanel.setEnabled(!isRunning);
        txtQuestion.setEnabled(!isRunning);
        txtNewOption.setEnabled(!isRunning);
        btnAddOption.setEnabled(!isRunning);
        btnStartElection.setEnabled(!isRunning);
        listOptions.setEnabled(!isRunning);
        
        btnEndElection.setEnabled(isRunning);
    }
    
    //
    // --- Métodos de Relatório e Log (sem modificações) ---
    //
    
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
            listModelResultados.clear();
            for (int i = 0; i < options.size(); i++) {
                String optionName = options.get(i);
                int count = contagem[i];
                listModelResultados.addElement(optionName + ": " + count + " voto(s)");
            }
        });
    }

    /**
     * MÉTODO MAIN - PONTO DE PARTIDA
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ServerGUI gui = new ServerGUI();
            gui.setVisible(true);
        });
    }
}