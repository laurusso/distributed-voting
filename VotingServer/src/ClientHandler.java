/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author l235826
 */
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
        try (
            ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())
        ) {
            //send the voting packet to the client
            out.writeObject(server.getCurrentVotingPacket());
            out.flush();

            //wait the vote from client
            Vote receivedVote = (Vote) in.readObject();

            //cpf validator - server side 
            if (!CPFValidator.isValidCPF(receivedVote.getCpf())) {
                System.err.println("Voto com CPF invalido recusado: " + receivedVote.getCpf());
                return;
            }

            //validates and register voting
            server.registerVote(receivedVote);

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erro no handler do cliente: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
 
            }
        }
    }
}
