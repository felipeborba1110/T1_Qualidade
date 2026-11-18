import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Única fonte de leitura do console. Lê sempre linhas inteiras e valida cada entrada antes de
 * devolvê-la (RNF002). Um só Scanner sobre a entrada evita a perda de dados redirecionados (BUG003).
 */
public class EntradaConsole {

    public static final String MSG_ENTRADA_INVALIDA = "Entrada inválida. Digite apenas números:";

    private static final Pattern INTEIRO = Pattern.compile("-?\\d+");
    private static final Pattern DECIMAL = Pattern.compile("-?(\\d+(\\.\\d*)?|\\.\\d+)");

    private final Scanner scanner;
    private final PrintStream saida;

    public EntradaConsole(InputStream entrada, PrintStream saida) {
        this.scanner = new Scanner(entrada, StandardCharsets.UTF_8);
        this.saida = saida;
    }

    public PrintStream saida() {
        return saida;
    }

    public void exibir(String texto) {
        saida.println(texto);
    }

    /** Lê a próxima linha; o fim da entrada encerra o sistema de forma controlada. */
    public String lerLinha() {
        try {
            return scanner.nextLine();
        } catch (NoSuchElementException e) {
            throw new EntradaEncerradaException();
        }
    }

    public String lerTexto(String mensagem) {
        exibir(mensagem);
        return lerLinha().trim();
    }

    /** RN002: repete a pergunta até receber um nome não vazio. */
    public String lerNomeObrigatorio(String mensagem) {
        while (true) {
            String nome = lerTexto(mensagem);
            if (ValidadorColaborador.nomeValido(nome)) {
                return nome;
            }
            exibir(ValidadorColaborador.MSG_NOME_OBRIGATORIO);
        }
    }

    public int lerInteiro(String mensagem) {
        exibir(mensagem);
        while (true) {
            Integer valor = interpretarInteiro(lerLinha());
            if (valor != null) {
                return valor;
            }
        }
    }

    public double lerDecimal(String mensagem) {
        exibir(mensagem);
        while (true) {
            Double valor = interpretarDecimal(lerLinha());
            if (valor != null) {
                return valor;
            }
        }
    }

    /** RN003 a RN007: recusa valores negativos e solicita novamente. */
    public double lerDecimalNaoNegativo(String mensagem) {
        while (true) {
            double valor = lerDecimal(mensagem);
            if (ValidadorColaborador.naoNegativo(valor)) {
                return valor;
            }
            exibir(ValidadorColaborador.MSG_VALOR_NEGATIVO);
        }
    }

    public int lerInteiroNaoNegativo(String mensagem) {
        while (true) {
            int valor = lerInteiro(mensagem);
            if (ValidadorColaborador.naoNegativo(valor)) {
                return valor;
            }
            exibir(ValidadorColaborador.MSG_VALOR_NEGATIVO);
        }
    }

    /** Retorna verdadeiro apenas para a resposta S (maiúscula ou minúscula). */
    public boolean confirmar(String mensagem) {
        return lerTexto(mensagem).equalsIgnoreCase("S");
    }

    /**
     * Devolve null para linha vazia (o sistema continua aguardando, sem mensagem) e exibe a mensagem de
     * entrada inválida para texto não numérico.
     */
    private Integer interpretarInteiro(String linha) {
        String texto = linha.trim();
        if (texto.isEmpty()) {
            return null;
        }
        Integer valor = converterInteiro(texto);
        if (valor == null) {
            exibir(MSG_ENTRADA_INVALIDA);
        }
        return valor;
    }

    private Double interpretarDecimal(String linha) {
        String texto = linha.trim();
        if (texto.isEmpty()) {
            return null;
        }
        Double valor = converterDecimal(texto);
        if (valor == null) {
            exibir(MSG_ENTRADA_INVALIDA);
        }
        return valor;
    }

    static Integer converterInteiro(String texto) {
        if (!INTEIRO.matcher(texto).matches()) {
            return null;
        }
        try {
            return Integer.valueOf(texto);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Aceita vírgula ou ponto como separador decimal, independentemente do idioma do sistema (BUG005).
     * Quando há os dois, o ponto é separador de milhar: "1.234,56" vira 1234.56.
     */
    static Double converterDecimal(String texto) {
        String normalizado = texto.replace(" ", "");
        if (normalizado.contains(",")) {
            normalizado = normalizado.replace(".", "").replace(',', '.');
        }
        if (!DECIMAL.matcher(normalizado).matches()) {
            return null;
        }
        return Double.valueOf(normalizado);
    }
}
