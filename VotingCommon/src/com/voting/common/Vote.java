/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.voting.common;

import java.io.Serializable;

/**
 * Esta classe representa o voto que o cliente envia ao servidor.
 * * Ela também precisa implementar 'Serializable'[cite: 33].
 */
public class Vote implements Serializable {

    private static final long serialVersionUID = 1L;

    private String cpf;
    private int optionIndex; // O índice da opção escolhida (ex: 0 para "Azul", 1 para "Verde")

    /**
     * Construtor para o voto.
     * @param cpf O CPF do eleitor [cite: 27]
     * @param optionIndex O índice da opção que ele escolheu na lista.
     */
    public Vote(String cpf, int optionIndex) {
        this.cpf = cpf;
        this.optionIndex = optionIndex;
    }

    // Getters
    
    public String getCpf() {
        return cpf;
    }

    public int getOptionIndex() {
        return optionIndex;
    }
}
