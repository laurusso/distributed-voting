package com.voting.server;

import com.voting.common.CPFValidator;
import com.voting.common.Vote;
import com.voting.common.VotingPacket;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/*this class implements the Runnable and it executes in a new thread for each client that connects*/

public class ClientHandler implements Runnable {

    private Socket clientSocket;
    private VotingServer server; 

    public ClientHandler(Socket socket, VotingServer server) {
        this.clientSocket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        String response = "ERRO INTERNO"; //default error

        try {
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());

            //send voting packet
            out.writeObject(server.getCurrentVotingPacket());
            out.flush();

            //wait the vote - client
            Vote receivedVote = (Vote) in.readObject();

            //cpf validator - server side
            if (!CPFValidator.isValidCPF(receivedVote.getCpf())) {
                System.err.println("Voto com CPF invalido recusado: " + receivedVote.getCpf());
                response = "ERRO: CPF invalido"; 
            } else {
                //try register the vote
                boolean success = server.registerVote(receivedVote);
                
                if (success) {
                    response = "SUCESSO"; 
                } else {
                    response = "ERRO: CPF DUPLICADO"; 
                }
            }
            
            //send back the response
            out.writeObject(response);
            out.flush();

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erro no handler do cliente: " + e.getMessage());
        } finally {
            try {
                //shut the streams before socket
                if (out != null) out.close();
                if (in != null) in.close();
                if (clientSocket != null) clientSocket.close();
            } catch (IOException e) {
                
            }
        }
    }
}
