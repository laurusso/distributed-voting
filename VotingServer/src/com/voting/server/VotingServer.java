/*The core logic component of the election server. It maintains the election state, 
including the VotingPacket and the map of received votes (using CPF as key to enforce uniqueness). 
It processes new votes and notifies the ServerGUI of result updates.*/
package com.voting.server;

import com.voting.common.Vote;
import com.voting.common.VotingPacket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VotingServer {

    private VotingPacket currentVotingPacket;
    private Map<String, Vote> receivedVotes;
    private ServerGUI gui;

    public VotingServer(ServerGUI gui, VotingPacket electionPacket) {
        this.gui = gui;
        this.receivedVotes = new ConcurrentHashMap<>();
        
        this.currentVotingPacket = electionPacket;

        //send initial data to gui
        gui.addLogMessage("--- Nova Eleição Carregada ---");
        gui.addLogMessage("Pergunta: " + currentVotingPacket.getQuestion());
        gui.updateResults(currentVotingPacket.getOptions(), receivedVotes);
    }

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
    
    //return voting map to report
    public Map<String, Vote> getReceivedVotes() {
        return receivedVotes;
    }
    
    public VotingPacket getCurrentVotingPacket() {
        return currentVotingPacket;
    }
}