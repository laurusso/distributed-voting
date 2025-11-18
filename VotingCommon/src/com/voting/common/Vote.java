package com.voting.common;

import java.io.Serializable;

public class Vote implements Serializable {

    private static final long serialVersionUID = 1L;

    private String cpf;
    private int optionIndex;

    public Vote(String cpf, int optionIndex) {
        this.cpf = cpf;
        this.optionIndex = optionIndex;
    }
    
    public String getCpf() {
        return cpf;
    }

    public int getOptionIndex() {
        return optionIndex;
    }
}
