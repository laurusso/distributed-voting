package com.voting.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class ServerNetworkManager implements Runnable {

    private static final int PORT = 5000;
    private ServerSocket serverSocket;
    private VotingServer serverCore; 
    private ServerGUI gui; 

    public ServerNetworkManager(VotingServer serverCore, ServerGUI gui) {
        this.serverCore = serverCore;
        this.gui = gui;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(PORT);
            gui.addLogMessage("Servidor ouvindo na porta " + PORT);

            while (true) {
                //hold for a client to connect
                Socket clientSocket = serverSocket.accept();
                
                gui.addLogMessage("Novo cliente conectado: " + clientSocket.getInetAddress());

                //creates a handler to the client
                ClientHandler handler = new ClientHandler(clientSocket, serverCore);

                //starts the handler in a new thread
                new Thread(handler).start();
            }
        } catch (SocketException e) {
            gui.addLogMessage("Servidor parou de aceitar conexões.");
        } catch (IOException e) {
            gui.addLogMessage("Erro no Gerenciador de Rede: " + e.getMessage());
        }
    }
 
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