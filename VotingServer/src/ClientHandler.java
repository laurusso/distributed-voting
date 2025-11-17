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

/**
 * Esta classe implementa Runnable e será executada em uma nova
 * thread para cada cliente que se conectar. 
 */
public class ClientHandler implements Runnable {

    private Socket clientSocket;
    private VotingServer server; // Referência ao servidor principal

    public ClientHandler(Socket socket, VotingServer server) {
        this.clientSocket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        // Usamos try-with-resources para garantir que tudo (streams e socket)
        // seja fechado automaticamente no final.
        try (
            ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())
        ) {
            // 1. Enviar o pacote de votação para o cliente
            out.writeObject(server.getCurrentVotingPacket());
            out.flush();

            // 2. Esperar para receber o voto do cliente
            Vote receivedVote = (Vote) in.readObject();

            // --- NOVA VALIDAÇÃO ---
            // 3. Validar o CPF no lado do Servidor
            if (!CPFValidator.isValidCPF(receivedVote.getCpf())) {
                // Se for inválido, nós simplesmente ignoramos o voto
                // e fechamos a conexão. Não registramos nada.
                // (Numa versão robusta, enviaríamos uma msg de erro ao cliente)
                System.err.println("Voto com CPF invalido recusado: " + receivedVote.getCpf());
                return; // Sai do método run(), fechando a conexão
            }
            // --- FIM DA MODIFICAÇÃO ---

            // 3. Validar e registrar o voto (incluindo duplicidade) 
            server.registerVote(receivedVote);
            
            // (Numa versão mais robusta, enviaríamos uma confirmação ao cliente)

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erro no handler do cliente: " + e.getMessage());
        } finally {
            try {
                clientSocket.close(); // Garante que o socket seja fechado
            } catch (IOException e) {
                // ignora
            }
        }
    }
}
