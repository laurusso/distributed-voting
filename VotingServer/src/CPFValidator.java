/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author l235826
 */
/**
 * Classe "ferramenta" para validar CPF.
 */
public class CPFValidator {

    /**
     * Valida um CPF.
     * @param cpf O CPF como String (pode conter pontos e traço)
     * @return true se for válido, false se não.
     */
    public static boolean isValidCPF(String cpf) {
        // Remove pontuação
        cpf = cpf.replaceAll("[^0-9]", "");

        // 1. Verifica se tem 11 dígitos
        if (cpf.length() != 11) {
            return false;
        }

        // 2. Verifica se todos os dígitos são iguais (ex: "111.111.111-11")
        // Isso é uma regra de invalidez do CPF.
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

        // 3. Cálculo do primeiro dígito verificador
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += (cpf.charAt(i) - '0') * (10 - i);
        }
        int digit1 = 11 - (sum % 11);
        if (digit1 > 9) {
            digit1 = 0;
        }

        // 4. Cálculo do segundo dígito verificador
        sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += (cpf.charAt(i) - '0') * (11 - i);
        }
        int digit2 = 11 - (sum % 11);
        if (digit2 > 9) {
            digit2 = 0;
        }

        // 5. Verifica se os dígitos calculados batem com os dígitos do CPF
        return (digit1 == (cpf.charAt(9) - '0') && digit2 == (cpf.charAt(10) - '0'));
    }
}