import java.util.Collection;

/**
 * Regras de negócio de validação, independentes do console (RN001 a RN007).
 */
public final class ValidadorColaborador {

    public static final String MSG_NOME_OBRIGATORIO = "O nome é obrigatório. Tente novamente!";
    public static final String MSG_VALOR_NEGATIVO = "O valor não pode ser negativo. Tente novamente!";
    public static final String MSG_MATRICULA_EXISTENTE = "Colaborador com este id já existe, tente novamente!";

    private ValidadorColaborador() {
    }

    /** RN002: o nome não pode ser nulo, vazio ou formado apenas por espaços. */
    public static boolean nomeValido(String nome) {
        return nome != null && !nome.trim().isEmpty();
    }

    /** RN003 a RN007: valores numéricos não podem ser negativos; zero é permitido. */
    public static boolean naoNegativo(double valor) {
        return valor >= 0;
    }

    /** RN001: a matrícula só está disponível se nenhum colaborador a utiliza. */
    public static boolean matriculaDisponivel(int id, Collection<Colaborador> existentes) {
        for (Colaborador colaborador : existentes) {
            if (colaborador.getId() == id) {
                return false;
            }
        }
        return true;
    }

    public static String validarNome(String nome) {
        if (!nomeValido(nome)) {
            throw new IllegalArgumentException(MSG_NOME_OBRIGATORIO);
        }
        return nome.trim();
    }

    public static double validarNaoNegativo(double valor, String campo) {
        if (!naoNegativo(valor)) {
            throw new IllegalArgumentException(campo + ": " + MSG_VALOR_NEGATIVO);
        }
        return valor;
    }
}
