package com.voting.client;

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

/*Manages the client applicatons*/
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

    public void sendVote(Vote vote) throws IOException {
        if (out == null || socket == null || socket.isClosed()) {
            throw new IOException("Não está conectado ao servidor.");
        }
        
        try {
            //sends voting
            out.writeObject(vote);
            out.flush();
        } finally {
            //shut everything 
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
        }
    }
}
