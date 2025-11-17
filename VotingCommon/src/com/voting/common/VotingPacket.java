/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.voting.common;

import java.io.Serializable;
import java.util.List;

/**
 * Esta classe representa o "pacote de votação" que o servidor envia
 * ao cliente. [cite: 26]
 * * Ela precisa implementar 'Serializable' para que seus objetos
 * possam ser enviados por um ObjectStream[cite: 33].
 */
public class VotingPacket implements Serializable {

    // É uma boa prática definir um serialVersionUID para classes Serializable.
    private static final long serialVersionUID = 1L;

    private String question;
    private List<String> options;

    /**
     * Construtor para criar o pacote de votação.
     * @param question A pergunta da eleição (ex: "Qual sua cor favorita?")
     * @param options Uma lista de opções (ex: ["Azul", "Verde", "Vermelho"])
     */
    public VotingPacket(String question, List<String> options) {
        this.question = question;
        this.options = options;
    }

    // Getters (métodos para ler os dados)
    
    public String getQuestion() {
        return question;
    }

    public List<String> getOptions() {
        return options;
    }
}