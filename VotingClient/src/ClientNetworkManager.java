/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author l235826
 */
package com.voting.client; // Garante que está no mesmo pacote

import com.voting.common.Vote;
import com.voting.common.VotingPacket;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Esta é a "primitiva de rede" do cliente.
 * Ela gerencia o Socket e os Streams, escondendo-os da GUI.
 */
public class ClientNetworkManager {

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    /**
     * Conecta ao servidor, baixa o pacote de votação e mantém a conexão aberta.
     */
    public VotingPacket connectAndGetPacket(String ip, int port) throws IOException, ClassNotFoundException {
        // 1. Conectar ao servidor
        this.socket = new Socket(ip, port);
        
        // 2. Preparar streams
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
        
        // 3. Receber o pacote de votação
        VotingPacket packet = (VotingPacket) in.readObject();
        return packet;
    }

    /**
     * Envia o voto e fecha a conexão.
     */
    public void sendVote(Vote vote) throws IOException {
        if (out == null || socket == null || socket.isClosed()) {
            throw new IOException("Não está conectado ao servidor.");
        }
        
        try {
            // 1. Enviar o voto
            out.writeObject(vote);
            out.flush();
        } finally {
            // 2. Fechar tudo
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
        }
    }
}
