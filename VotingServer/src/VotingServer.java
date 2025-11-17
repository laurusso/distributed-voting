// Delele a linha 'package' se você estiver no <default package>

import com.voting.common.Vote;
import com.voting.common.VotingPacket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Esta é a classe "cérebro" do servidor.
 * Ela agora recebe a eleição (VotingPacket) em seu construtor.
 */
public class VotingServer {

    private VotingPacket currentVotingPacket;
    private Map<String, Vote> receivedVotes;
    private ServerGUI gui;

    /**
     * Construtor
     * @param gui A interface gráfica
     * @param electionPacket O pacote de eleição carregado do arquivo
     */
    public VotingServer(ServerGUI gui, VotingPacket electionPacket) {
        this.gui = gui;
        this.receivedVotes = new ConcurrentHashMap<>();
        
        // --- MODIFICAÇÃO ---
        // A eleição agora vem de fora, não é mais "hardcoded"
        this.currentVotingPacket = electionPacket;

        // Envia os dados iniciais para a GUI
        gui.addLogMessage("--- Nova Eleição Carregada ---");
        gui.addLogMessage("Pergunta: " + currentVotingPacket.getQuestion());
        gui.updateResults(currentVotingPacket.getOptions(), receivedVotes);
    }

    /**
     * Tenta adicionar um voto. (Esta lógica não muda)
     */
    public boolean registerVote(Vote vote) {
        Vote existing = receivedVotes.putIfAbsent(vote.getCpf(), vote);
        
        if (existing == null) {
            gui.addLogMessage("Voto registrado: " + vote.getCpf());
            gui.updateResults(currentVotingPacket.getOptions(), receivedVotes);
            return true;
        } else {
            gui.addLogMessage("Voto duplicado recusado: " + vote.getCpf());
            return false;
        }
    }
    
    /**
     * Retorna o mapa de votos para o relatório.
     */
    public Map<String, Vote> getReceivedVotes() {
        return receivedVotes;
    }
    
    /**
     * Retorna o pacote de votação atual.
     */
    public VotingPacket getCurrentVotingPacket() {
        return currentVotingPacket;
    }
}