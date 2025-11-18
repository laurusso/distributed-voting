package com.voting.client;

import com.voting.common.Vote;
import com.voting.common.VotingPacket;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/*manages the client applicatons*/
public class ClientNetworkManager {

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public VotingPacket connectAndGetPacket(String ip, int port) throws IOException, ClassNotFoundException {
        //connect the server
        this.socket = new Socket(ip, port);
        
        //sets the streams
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
        
        //receive voting packet
        VotingPacket packet = (VotingPacket) in.readObject();
        return packet;
    }

    public String sendVote(Vote vote) throws IOException {
        if (out == null || socket == null || socket.isClosed()) {
            throw new IOException("Não está conectado ao servidor.");
        }
        
        try {
            //send vote
            out.writeObject(vote);
            out.flush();

            //wait the server
            Object response = in.readObject(); 
            if (response instanceof String) {
                return (String) response; 
            } else {
                throw new IOException("Resposta do servidor em formato inesperado.");
            }

        } catch (ClassNotFoundException ex) {
             throw new IOException("Erro ao ler resposta do servidor: " + ex.getMessage());
        } finally {
            //shut down
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
        }
    }
}
