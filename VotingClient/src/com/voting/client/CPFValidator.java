package com.voting.client;

public class CPFValidator {
    public static boolean isValidCPF(String cpf) {
        //removes everything escept for numbers
        cpf = cpf.replaceAll("[^0-9]", "");

        //verifies 11 caracters
        if (cpf.length() != 11) {
            return false;
        }

        //verify if every number is the same (like: 11111111111) - that is invalid
        boolean allEqual = true;
        for (int i = 1; i < 11; i++) {
            if (cpf.charAt(i) != cpf.charAt(0)) {
                allEqual = false;
                break;
            }
        }
        if (allEqual) {
            return false;
        }

        //verifies the first id digit
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += (cpf.charAt(i) - '0') * (10 - i);
        }
        int digit1 = 11 - (sum % 11);
        if (digit1 > 9) {
            digit1 = 0;
        }

        //verifies the second id digit
        sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += (cpf.charAt(i) - '0') * (11 - i);
        }
        int digit2 = 11 - (sum % 11);
        if (digit2 > 9) {
            digit2 = 0;
        }

        //checks if it is really valid considering the id digits
        return (digit1 == (cpf.charAt(9) - '0') && digit2 == (cpf.charAt(10) - '0'));
    }
}