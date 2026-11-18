import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Critérios de aceitação da ERS verificados de ponta a ponta, pelo ponto de entrada Main, observando
 * apenas o que o usuário vê no console e o arquivo data.json (caixa-preta).
 */
@Tag("aceitacao")
@DisplayName("Critérios de aceitação da ERS (ponta a ponta)")
class CriteriosAceitacaoTest {

    @TempDir
    Path pasta;

    private Path arquivo;
    private InputStream entradaOriginal;
    private PrintStream saidaOriginal;

    @BeforeEach
    void configurar() {
        arquivo = pasta.resolve("data.json");
        System.setProperty(Main.PROPRIEDADE_ARQUIVO, arquivo.toString());
        entradaOriginal = System.in;
        saidaOriginal = System.out;
    }

    @AfterEach
    void restaurar() {
        System.clearProperty(Main.PROPRIEDADE_ARQUIVO);
        System.setIn(entradaOriginal);
        System.setOut(saidaOriginal);
    }

    /** Inicia o sistema como o usuário faria e devolve tudo o que foi exibido. */
    private String sistema(String... linhas) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        System.setIn(SessaoConsole.entradaDe(linhas));
        System.setOut(new PrintStream(bytes, true, StandardCharsets.UTF_8));
        Main.main(new String[0]);
        System.setOut(saidaOriginal);
        return SessaoConsole.normalizarQuebras(bytes.toString(StandardCharsets.UTF_8));
    }

    private String sistema(String[]... partes) {
        return sistema(SessaoConsole.concatenar(partes));
    }

    @Test
    @DisplayName("RF001 - matrícula 1, Ana, R$ 2.000,00, Padrão: gravado e exibido com os mesmos dados")
    void rf001() throws Exception {
        String saida = sistema("1", "1", "Ana", "2000", "1", "2", "1", "1", "0");
        assertTrue(saida.contains("ID: 1\nNome: Ana\nSalário Base: R$ 2.000,00\nTipo: Padrão"));
        assertTrue(Files.readString(arquivo).contains("\"name\":\"Ana\""));
    }

    @Test
    @DisplayName("RF002 - Padrão com base R$ 3.000,00: salário final R$ 3.000,00, sem adicionais")
    void rf002() {
        String saida = sistema(SessaoConsole.cadastroM1(), new String[] {"3", "0"});
        assertTrue(saida.contains("Adicionais: R$ 0,00\nSalário Final: R$ 3.000,00"));
    }

    @Test
    @DisplayName("RF003 - base 2.000, vendas 10.000, 0,05: comissão R$ 500,00 e final R$ 2.500,00")
    void rf003() {
        String saida = sistema(SessaoConsole.cadastroM2(), new String[] {"3", "0"});
        assertTrue(saida.contains("Adicionais: R$ 500,00\nSalário Final: R$ 2.500,00"));
    }

    @Test
    @DisplayName("RF004 - base 1.800, 100 un a 2,50: produtividade R$ 250,00 e final R$ 2.050,00")
    void rf004() {
        String saida = sistema(SessaoConsole.cadastroM3(), new String[] {"3", "0"});
        assertTrue(saida.contains("Adicionais: R$ 250,00\nSalário Final: R$ 2.050,00"));
    }

    @Test
    @DisplayName("RF005 - consulta por matrícula, por nome, listagem e inexistente sem encerrar")
    void rf005() {
        String saida = sistema(SessaoConsole.cadastroM1(), SessaoConsole.cadastroM2(),
                new String[] {"2", "1", "1", "2", "2", "Bruno", "6", "2", "2", "Zé", "0", "0"});
        assertTrue(saida.contains("Nome: Ana\nSalário Base: R$ 3.000,00\nTipo: Padrão\nSalário Final: R$ 3.000,00"));
        assertTrue(saida.contains("Nome: Bruno\nSalário Base: R$ 2.000,00\nTipo: Comissionado\nSalário Final: R$ 2.500,00"));
        assertTrue(saida.contains("Nenhum colaborador encontrado!"));
        assertTrue(saida.contains("Sistema encerrado."));
    }

    @Test
    @DisplayName("RF006 - salário calculado (R$ 2.500,00) e recalculado após vendas = 20.000 (R$ 3.000,00)")
    void rf006() {
        String saida = sistema(SessaoConsole.cadastroM2(), new String[] {"6", "5", "2", "4", "20000", "0"});
        assertTrue(saida.contains("Salário Final: R$ 2.500,00"));
        assertTrue(saida.contains("Dados atualizados -----\nID: 2\nNome: Bruno\nSalário Base: R$ 2.000,00\n"
                + "Tipo: Comissionado\nSalário Final: R$ 3.000,00"));
    }

    @Test
    @DisplayName("RF007 - folha com os três tipos, cada um com base, adicionais e final")
    void rf007() {
        String saida = sistema(SessaoConsole.cadastroM1(), SessaoConsole.cadastroM2(), SessaoConsole.cadastroM3(),
                new String[] {"3", "0"});
        assertEquals(3, SessaoConsole.ocorrencias(saida, "Adicionais: R$"));
        assertTrue(saida.contains("Tipo: Padrão") && saida.contains("Tipo: Comissionado")
                && saida.contains("Tipo: Produção"));
    }

    @Test
    @DisplayName("RF008 - quantidade 3, total por categoria e total geral de R$ 7.550,00")
    void rf008() {
        String saida = sistema(SessaoConsole.cadastroM1(), SessaoConsole.cadastroM2(), SessaoConsole.cadastroM3(),
                new String[] {"4", "0"});
        assertTrue(saida.contains("Quantidade de colaboradores: 3"));
        assertTrue(saida.contains("Padrão: R$ 3.000,00"));
        assertTrue(saida.contains("Comissionado: R$ 2.500,00"));
        assertTrue(saida.contains("Produção: R$ 2.050,00"));
        assertTrue(saida.contains("Total da Folha de Pagamentos: R$ 7.550,00"));
    }

    @Test
    @DisplayName("RF009 - alterar nome, excluir com S e com N, e persistência após reiniciar")
    void rf009() {
        sistema(SessaoConsole.cadastroM1(), SessaoConsole.cadastroM2(), SessaoConsole.cadastroM3(),
                new String[] {"5", "1", "1", "Ana Maria", "7", "3", "S", "7", "2", "N", "0"});
        String aposReiniciar = sistema("6", "0");
        assertTrue(aposReiniciar.contains("Nome: Ana Maria"));
        assertTrue(aposReiniciar.contains("Nome: Bruno"));
        assertFalse(aposReiniciar.contains("Nome: Carla"));
    }

    @Test
    @DisplayName("RN001 a RN007 - entradas inválidas recusadas com mensagem e solicitadas novamente")
    void regrasDeNegocio() {
        String saida = sistema(SessaoConsole.cadastroM1(), new String[] {
            "1", "1", "2", "", "Bruno", "-100", "2000", "2", "-1000", "10000", "-0,05", "0,05",
            "1", "3", "Carla", "1800", "3", "-10", "100", "-2,50", "2,50", "4", "0"});
        assertTrue(saida.contains("Colaborador com este id já existe, tente novamente!"));
        assertTrue(saida.contains("O nome é obrigatório. Tente novamente!"));
        assertEquals(5, SessaoConsole.ocorrencias(saida, "O valor não pode ser negativo. Tente novamente!"));
        assertTrue(saida.contains("Total da Folha de Pagamentos: R$ 7.550,00"));
    }

    @Test
    @DisplayName("RN008 - nenhuma tela solicita o salário final")
    void rn008() {
        String saida = sistema(SessaoConsole.cadastroM1(), SessaoConsole.cadastroM2(), SessaoConsole.cadastroM3(),
                new String[] {"0"});
        assertFalse(saida.toLowerCase().matches("(?s).*(insira|informe|digite) o salário final.*"));
    }

    @Test
    @DisplayName("RNF002 - texto, negativo e campo vazio não encerram o sistema nem geram cálculo")
    void rnf002() throws Exception {
        String saida = sistema("abc", "1", "abc", "1", "Ana", "", "abc", "-5", "3000", "1", "0");
        assertTrue(saida.contains("Entrada inválida. Digite apenas números:"));
        assertTrue(saida.contains("O valor não pode ser negativo. Tente novamente!"));
        assertTrue(saida.contains("Sistema encerrado."));
        assertTrue(Files.readString(arquivo).contains("\"baseSalary\":3000.0"));
    }

    @Test
    @DisplayName("RNF003 - cadastrar, sair (opção 0), abrir de novo: colaborador disponível")
    void rnf003() {
        sistema(SessaoConsole.cadastroM1(), new String[] {"0"});
        String aposReiniciar = sistema("6", "0");
        assertTrue(aposReiniciar.contains("Nome: Ana\nSalário Base: R$ 3.000,00"));
    }
}
