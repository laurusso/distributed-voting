package com.voting.common;

import java.io.Serializable;
import java.util.List;

public class VotingPacket implements Serializable {

    private static final long serialVersionUID = 1L;

    private String question;
    private List<String> options;

    public VotingPacket(String question, List<String> options) {
        this.question = question;
        this.options = options;
    }
    
    public String getQuestion() {
        return question;
    }

    public List<String> getOptions() {
        return options;
    }
}