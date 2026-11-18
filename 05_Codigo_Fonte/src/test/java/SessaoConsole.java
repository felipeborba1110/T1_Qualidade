import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * Apoio aos testes: executa o menu com uma sequência de entradas digitadas e captura a saída.
 */
final class SessaoConsole {

    private SessaoConsole() {
    }

    /** Executa o menu completo; cada argumento corresponde a uma linha digitada pelo usuário. */
    static String executarMenu(Path arquivo, String... linhas) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream saida = new PrintStream(bytes, true, StandardCharsets.UTF_8);
        EntradaConsole entrada = new EntradaConsole(entradaDe(linhas), saida);
        new Menu(entrada, new JsonFileManager(arquivo)).menu();
        return normalizarQuebras(bytes.toString(StandardCharsets.UTF_8));
    }

    /** No Windows o println usa \r\n; os testes comparam sempre com \n, em qualquer sistema. */
    static String normalizarQuebras(String texto) {
        return texto.replace("\r\n", "\n");
    }

    static EntradaConsole entrada(PrintStream saida, String... linhas) {
        return new EntradaConsole(entradaDe(linhas), saida);
    }

    static ByteArrayInputStream entradaDe(String... linhas) {
        String texto = linhas.length == 0 ? "" : String.join("\n", linhas) + "\n";
        return new ByteArrayInputStream(texto.getBytes(StandardCharsets.UTF_8));
    }

    /** Linhas para cadastrar a massa M1 (Ana, padrão, R$ 3.000,00) pelo menu. */
    static String[] cadastroM1() {
        return new String[] {"1", "1", "Ana", "3000", "1"};
    }

    /** Linhas para cadastrar a massa M2 (Bruno, comissionado, base 2.000, vendas 10.000, 5%). */
    static String[] cadastroM2() {
        return new String[] {"1", "2", "Bruno", "2000", "2", "10000", "0,05"};
    }

    /** Linhas para cadastrar a massa M3 (Carla, produção, base 1.800, 100 unidades a 2,50). */
    static String[] cadastroM3() {
        return new String[] {"1", "3", "Carla", "1800", "3", "100", "2,50"};
    }

    static String[] concatenar(String[]... partes) {
        int tamanho = 0;
        for (String[] parte : partes) {
            tamanho += parte.length;
        }
        String[] resultado = new String[tamanho];
        int posicao = 0;
        for (String[] parte : partes) {
            System.arraycopy(parte, 0, resultado, posicao, parte.length);
            posicao += parte.length;
        }
        return resultado;
    }

    /** Conta quantas vezes um trecho aparece na saída. */
    static int ocorrencias(String texto, String trecho) {
        int total = 0;
        int indice = texto.indexOf(trecho);
        while (indice >= 0) {
            total++;
            indice = texto.indexOf(trecho, indice + trecho.length());
        }
        return total;
    }
}
