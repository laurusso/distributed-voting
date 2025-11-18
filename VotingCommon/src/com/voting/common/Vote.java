/*Data model representing a single vote cast by an elector. It contains the elector's 
CPF and the index of the selected option. Implements Serializable to allow transfer over network object streams.*/

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
