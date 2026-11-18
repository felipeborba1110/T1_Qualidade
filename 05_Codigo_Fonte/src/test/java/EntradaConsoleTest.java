import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Testes unitários da leitura e validação das entradas do usuário (RNF002, RN002 a RN007).
 */
@Tag("unitario")
@DisplayName("Leitura e validação de entradas")
class EntradaConsoleTest {

    private ByteArrayOutputStream bytes;
    private PrintStream saida;

    @BeforeEach
    void prepararSaida() {
        bytes = new ByteArrayOutputStream();
        saida = new PrintStream(bytes, true, StandardCharsets.UTF_8);
    }

    private String textoExibido() {
        return bytes.toString(StandardCharsets.UTF_8);
    }

    @ParameterizedTest(name = "\"{0}\" = {1}")
    @CsvSource(delimiter = ';', value = {
        "0,05;0.05", "0.05;0.05", "2,50;2.5", "3000;3000", "1.234,56;1234.56", "-5;-5", ",5;0.5", "10000,;10000"
    })
    @DisplayName("BUG005 - aceita vírgula ou ponto como separador decimal")
    void bug005ConverteDecimal(String texto, double esperado) {
        assertEquals(esperado, EntradaConsole.converterDecimal(texto));
    }

    @ParameterizedTest(name = "\"{0}\" é inválido")
    @ValueSource(strings = {"abc", "1a", "R$10", "1,2,3", "NaN", "Infinity", "--1", "."})
    @DisplayName("RNF002 - texto não numérico não é convertido")
    void rnf002DecimalInvalido(String texto) {
        assertNull(EntradaConsole.converterDecimal(texto));
    }

    @ParameterizedTest(name = "\"{0}\" é inválido")
    @ValueSource(strings = {"abc", "1.5", "2,0", "99999999999", " "})
    @DisplayName("RNF002 - inteiro inválido ou fora da faixa não é convertido")
    void rnf002InteiroInvalido(String texto) {
        assertNull(EntradaConsole.converterInteiro(texto));
    }

    @Test
    @DisplayName("CT020 - texto exibe mensagem e a entrada é solicitada novamente")
    void ct020TextoNoCampoNumerico() {
        EntradaConsole entrada = SessaoConsole.entrada(saida, "abc", "42");
        assertEquals(42, entrada.lerInteiro("Insira o id:"));
        assertTrue(textoExibido().contains(EntradaConsole.MSG_ENTRADA_INVALIDA));
    }

    @Test
    @DisplayName("CT020 - Enter sem dado: o sistema continua aguardando, sem mensagem de erro")
    void ct020EnterSemDado() {
        EntradaConsole entrada = SessaoConsole.entrada(saida, "", "   ", "7,5");
        assertEquals(7.5, entrada.lerDecimal("Valor:"));
        assertFalse(textoExibido().contains(EntradaConsole.MSG_ENTRADA_INVALIDA));
    }

    @Test
    @DisplayName("CT013 - valor negativo é recusado e solicitado novamente; zero é aceito (RN003)")
    void ct013NegativoDepoisZero() {
        EntradaConsole entrada = SessaoConsole.entrada(saida, "-100", "0");
        assertEquals(0.0, entrada.lerDecimalNaoNegativo("Insira o salário base:"));
        assertEquals(1, SessaoConsole.ocorrencias(textoExibido(), ValidadorColaborador.MSG_VALOR_NEGATIVO));
        assertEquals(2, SessaoConsole.ocorrencias(textoExibido(), "Insira o salário base:"));
    }

    @Test
    @DisplayName("CT016 - quantidade negativa é recusada e solicitada novamente (RN006)")
    void ct016QuantidadeNegativa() {
        EntradaConsole entrada = SessaoConsole.entrada(saida, "-10", "100");
        assertEquals(100, entrada.lerInteiroNaoNegativo("Insira a quantidade produzida:"));
        assertTrue(textoExibido().contains(ValidadorColaborador.MSG_VALOR_NEGATIVO));
    }

    @Test
    @DisplayName("CT012 - nome vazio é recusado e solicitado novamente (RN002)")
    void ct012NomeVazio() {
        EntradaConsole entrada = SessaoConsole.entrada(saida, "", "   ", "Davi");
        assertEquals("Davi", entrada.lerNomeObrigatorio("Insira o nome:"));
        assertEquals(2, SessaoConsole.ocorrencias(textoExibido(), ValidadorColaborador.MSG_NOME_OBRIGATORIO));
    }

    @Test
    @DisplayName("Confirmação aceita somente S ou s")
    void confirmacao() {
        assertTrue(SessaoConsole.entrada(saida, "S").confirmar("?"));
        assertTrue(SessaoConsole.entrada(saida, "s").confirmar("?"));
        assertFalse(SessaoConsole.entrada(saida, "N").confirmar("?"));
        assertFalse(SessaoConsole.entrada(saida, "sim").confirmar("?"));
    }

    @Test
    @DisplayName("BUG003 - fim da entrada gera EntradaEncerradaException em vez de erro inesperado")
    void bug003FimDaEntrada() {
        EntradaConsole entrada = SessaoConsole.entrada(saida);
        assertThrows(EntradaEncerradaException.class, () -> entrada.lerInteiro("Opção:"));
    }
}
