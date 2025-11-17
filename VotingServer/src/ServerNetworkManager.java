// Delele a linha 'package' se você estiver no <default package>

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Esta é a "primitiva de rede" do Servidor.
 * Sua única função é ouvir na porta e aceitar novas conexões.
 */
public class ServerNetworkManager implements Runnable {

    private static final int PORT = 5000;
    private ServerSocket serverSocket;
    private VotingServer serverCore; // O "cérebro" (para passar ao ClientHandler)
    private ServerGUI gui; // A interface (para logar)

    /**
     * Construtor
     * (ESTE É O CONSTRUTOR QUE O ERRO DIZIA ESTAR FALTANDO)
     * @param serverCore O "cérebro" com a lógica de votação
     * @param gui A interface para enviar logs
     */
    public ServerNetworkManager(VotingServer serverCore, ServerGUI gui) {
        this.serverCore = serverCore;
        this.gui = gui;
    }

    /**
     * Este é o método que a Thread executará.
     * Contém o loop principal de escuta.
     */
    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(PORT);
            gui.addLogMessage("Servidor ouvindo na porta " + PORT);

            // Loop infinito para aceitar clientes
            while (true) {
                // 1. Espera um cliente se conectar
                Socket clientSocket = serverSocket.accept();
                
                gui.addLogMessage("Novo cliente conectado: " + clientSocket.getInetAddress());

                // 2. Cria um "atendente" (Handler) para este cliente
                // Passa o socket e o "cérebro"
                ClientHandler handler = new ClientHandler(clientSocket, serverCore);

                // 3. Inicia o atendente em uma nova Thread
                new Thread(handler).start();
            }
        } catch (SocketException e) {
            // Isso acontece QUANDO chamamos stopListening(). É normal.
            gui.addLogMessage("Servidor parou de aceitar conexões.");
        } catch (IOException e) {
            gui.addLogMessage("Erro no Gerenciador de Rede: " + e.getMessage());
        }
    }

    /**
     * Para de aceitar novas conexões de clientes (fecha o socket).
     */
    public void stopListening() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            gui.addLogMessage("Erro ao fechar o socket do servidor: " + e.getMessage());
        }
    }
}