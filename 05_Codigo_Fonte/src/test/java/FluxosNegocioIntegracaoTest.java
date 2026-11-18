import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Integração entre Menu, UtilsColaborador, EntradaConsole, JsonFileManager e o arquivo de dados,
 * percorrendo os fluxos de negócio pelo menu, como o usuário faria.
 */
@Tag("integracao")
@DisplayName("Fluxos de negócio pelo menu")
class FluxosNegocioIntegracaoTest {

    @TempDir
    Path pasta;

    private Path arquivo;

    @BeforeEach
    void prepararArquivo() {
        arquivo = pasta.resolve("data.json");
    }

    private String executar(String[]... partes) {
        return SessaoConsole.executarMenu(arquivo, SessaoConsole.concatenar(partes));
    }

    private static String[] linhas(String... linhas) {
        return linhas;
    }

    private void cadastrarMassaCompleta() {
        executar(SessaoConsole.cadastroM1(), SessaoConsole.cadastroM2(), SessaoConsole.cadastroM3(), linhas("0"));
    }

    private List<Colaborador> gravados() {
        return new JsonFileManager(arquivo).listarColaboradores();
    }

    @Test
    @DisplayName("CT001 - cadastro pelo menu grava o colaborador e a listagem o exibe (RF001)")
    void ct001CadastroPeloMenu() {
        String saida = executar(SessaoConsole.cadastroM1(), linhas("6", "0"));
        assertTrue(saida.contains("Colaborador criado com sucesso"));
        assertTrue(saida.contains("Nome: Ana\nSalário Base: R$ 3.000,00\nTipo: Padrão\nSalário Final: R$ 3.000,00"));
        assertEquals(1, gravados().size());
    }

    @Test
    @DisplayName("CT005 - consulta por id, por nome, listagem geral e id inexistente (RF005)")
    void ct005Consultas() {
        cadastrarMassaCompleta();
        String saida = executar(linhas("2", "1", "2", "2", "2", "Bruno", "6", "2", "1", "99", "0", "0"));

        assertEquals(3, SessaoConsole.ocorrencias(saida, "Nome: Bruno"));
        assertTrue(saida.contains("Nome: Ana"));
        assertTrue(saida.contains("Nome: Carla"));
        assertTrue(saida.contains(UtilsColaborador.MSG_NAO_ENCONTRADO_ID));
        assertTrue(saida.contains("Sistema encerrado."));
    }

    @Test
    @DisplayName("CT005 - nome inexistente informa e permite nova busca")
    void ct005NomeInexistente() {
        cadastrarMassaCompleta();
        String saida = executar(linhas("2", "2", "Zé", "2", "carla", "0"));
        assertTrue(saida.contains(UtilsColaborador.MSG_NAO_ENCONTRADO_NOME));
        assertTrue(saida.contains("Nome: Carla"));
    }

    @Test
    @DisplayName("BUG004 - consulta com cadastro vazio permite voltar ao menu (opção 0)")
    void bug004ConsultaSemCadastro() {
        String saida = executar(linhas("2", "1", "99", "1", "1", "0", "0"));
        assertEquals(2, SessaoConsole.ocorrencias(saida, UtilsColaborador.MSG_NAO_ENCONTRADO_ID));
        assertTrue(saida.contains("Sistema encerrado."));
    }

    @Test
    @DisplayName("CT006 - alterar as vendas recalcula o salário final para R$ 3.000,00 (RF006)")
    void ct006RecalculoPeloMenu() {
        executar(SessaoConsole.cadastroM2(), linhas("0"));
        String saida = executar(linhas("5", "2", "4", "20000", "2", "1", "2", "0"));

        assertTrue(saida.contains("Colaborador modificado com sucesso!"));
        assertTrue(saida.contains("Salário Final: R$ 3.000,00"));
        assertEquals(3000.0, gravados().get(0).calcularSalarioFinal());
    }

    @Test
    @DisplayName("RF006 - alterar comissão, salário base, unidade e quantidade recalcula o salário")
    void recalculoDosDemaisCampos() {
        cadastrarMassaCompleta();
        executar(linhas("5", "2", "5", "0,10", "5", "1", "2", "3500",
                "5", "3", "4", "3", "5", "3", "5", "200", "0"));
        List<Colaborador> lista = gravados();
        assertEquals(3500.0, lista.get(0).calcularSalarioFinal());
        assertEquals(3000.0, lista.get(1).calcularSalarioFinal());
        assertEquals(2400.0, lista.get(2).calcularSalarioFinal());
    }

    @Test
    @DisplayName("RF009 - alterar o tipo de padrão para produção pede os novos dados e recalcula")
    void alteracaoDeTipo() {
        executar(SessaoConsole.cadastroM1(), linhas("0"));
        String saida = executar(linhas("5", "1", "3", "3", "10", "5", "0"));
        assertTrue(saida.contains("Selecione o novo tipo:"));
        Colaborador ana = gravados().get(0);
        assertInstanceOf(ColaboradorProducao.class, ana);
        assertEquals(3050.0, ana.calcularSalarioFinal());
    }

    @Test
    @DisplayName("BUG006 - opção de alteração que não existe para o tipo é recusada")
    void bug006OpcaoInexistenteParaOTipo() {
        executar(SessaoConsole.cadastroM1(), linhas("0"));
        String saida = executar(linhas("5", "1", "4", "1", "Ana Paula", "0"));
        assertTrue(saida.contains(UtilsColaborador.MSG_OPCAO_INVALIDA));
        assertEquals("Ana Paula", gravados().get(0).getName());
    }

    @Test
    @DisplayName("Alterar ou excluir matrícula inexistente informa o erro e volta ao menu")
    void alterarExcluirInexistente() {
        String saida = executar(linhas("5", "42", "7", "42", "0"));
        assertEquals(2, SessaoConsole.ocorrencias(saida, UtilsColaborador.MSG_NAO_ENCONTRADO_ID));
    }

    @Test
    @DisplayName("CT009 - nome alterado para Ana Maria permanece após reiniciar (RF009)")
    void ct009AlteracaoDeNome() {
        executar(SessaoConsole.cadastroM1(), linhas("0"));
        executar(linhas("5", "1", "1", "Ana Maria", "0"));
        String saidaAposReiniciar = executar(linhas("2", "1", "1", "0"));
        assertTrue(saidaAposReiniciar.contains("Nome: Ana Maria\nSalário Base: R$ 3.000,00"));
    }

    @Test
    @DisplayName("CT010 - exclusão com S remove; com N mantém (RF009)")
    void ct010ExclusaoComConfirmacao() {
        cadastrarMassaCompleta();
        String saida = executar(linhas("7", "3", "S", "7", "2", "N", "6", "0"));

        assertTrue(saida.contains("Colaborador removido com sucesso"));
        assertTrue(saida.contains("Retornando ao menu..."));
        List<Colaborador> lista = gravados();
        assertEquals(2, lista.size());
        assertEquals("Ana", lista.get(0).getName());
        assertEquals("Bruno", lista.get(1).getName());
    }

    @Test
    @DisplayName("CT011 - id repetido é recusado e solicitado novamente; id 4 é aceito (RN001)")
    void ct011IdRepetido() {
        executar(SessaoConsole.cadastroM1(), linhas("0"));
        String saida = executar(linhas("1", "1", "4", "Davi", "1000", "1", "0"));
        assertTrue(saida.contains(ValidadorColaborador.MSG_MATRICULA_EXISTENTE));
        assertEquals(2, gravados().size());
        assertEquals(1, gravados().stream().filter(c -> c.getId() == 1).count());
    }

    @Test
    @DisplayName("CT012 - nome vazio recusado no cadastro e na alteração (RN002)")
    void ct012NomeVazio() {
        executar(SessaoConsole.cadastroM1(), linhas("0"));
        String saida = executar(linhas("1", "5", "", "Davi", "1000", "1", "5", "1", "1", "", "Ana", "0"));
        assertEquals(2, SessaoConsole.ocorrencias(saida, ValidadorColaborador.MSG_NOME_OBRIGATORIO));
        assertEquals("Davi", new JsonFileManager(arquivo).buscarPorId(5).orElseThrow().getName());
    }

    @Test
    @DisplayName("CT013 - salário base -100 recusado no cadastro e na alteração; 0 aceito (RN003)")
    void ct013SalarioBaseNegativo() {
        String saida = executar(linhas("1", "8", "Eva", "-100", "0", "1", "5", "8", "2", "-100", "50", "0"));
        assertEquals(2, SessaoConsole.ocorrencias(saida, ValidadorColaborador.MSG_VALOR_NEGATIVO));
        assertTrue(saida.contains("Colaborador criado com sucesso"));
        assertEquals(50.0, gravados().get(0).calcularSalarioFinal());
    }

    @Test
    @DisplayName("CT014 e CT015 - vendas e percentual negativos recusados (RN004, RN005)")
    void ct014Ct015ComissionadoNegativos() {
        String saida = executar(linhas("1", "2", "Bruno", "2000", "2", "-1000", "10000", "-0,05", "0,05", "0"));
        assertEquals(2, SessaoConsole.ocorrencias(saida, ValidadorColaborador.MSG_VALOR_NEGATIVO));
        assertEquals(2500.0, gravados().get(0).calcularSalarioFinal());
    }

    @Test
    @DisplayName("CT016 e CT017 - quantidade e valor por unidade negativos recusados (RN006, RN007)")
    void ct016Ct017ProducaoNegativos() {
        String saida = executar(linhas("1", "3", "Carla", "1800", "3", "-10", "100", "-2,50", "2,50", "0"));
        assertEquals(2, SessaoConsole.ocorrencias(saida, ValidadorColaborador.MSG_VALOR_NEGATIVO));
        assertEquals(2050.0, gravados().get(0).calcularSalarioFinal());
    }

    @Test
    @DisplayName("CT018 - nenhuma tela pede o salário final; ele aparece calculado (RN008)")
    void ct018SalarioFinalNuncaInformado() {
        String saida = executar(SessaoConsole.cadastroM1(), SessaoConsole.cadastroM2(), SessaoConsole.cadastroM3(),
                linhas("6", "3", "0"));
        assertFalse(saida.toLowerCase().contains("insira o salário final"));
        assertFalse(saida.toLowerCase().contains("informe o salário final"));
        assertEquals(6, SessaoConsole.ocorrencias(saida, "Salário Final: R$"));
    }

    @Test
    @DisplayName("CT020 - texto, negativo e Enter vazio não encerram nem gravam dado inválido (RNF002)")
    void ct020EntradasInvalidas() {
        String saida = executar(linhas("abc", "1", "abc", "1", "Ana", "abc", "", "3000", "1",
                "1", "2", "Bruno", "-5", "2000", "x", "1", "6", "0"));
        assertTrue(SessaoConsole.ocorrencias(saida, EntradaConsole.MSG_ENTRADA_INVALIDA) >= 4);
        assertTrue(saida.contains(ValidadorColaborador.MSG_VALOR_NEGATIVO));
        assertTrue(saida.contains("Sistema encerrado."));
        List<Colaborador> lista = gravados();
        assertEquals(2, lista.size());
        assertEquals(2000.0, lista.get(1).getBaseSalary());
    }

    @Test
    @DisplayName("Opção inexistente no menu principal e no tipo é informada sem encerrar")
    void opcoesInexistentes() {
        String saida = executar(linhas("9", "1", "1", "Ana", "100", "7", "1", "2", "9", "0", "0"));
        assertTrue(saida.contains("Opção inválida tente novamente!"));
        assertEquals(2, SessaoConsole.ocorrencias(saida, UtilsColaborador.MSG_OPCAO_INVALIDA));
        assertEquals(1, gravados().size());
    }

    @Test
    @DisplayName("BUG002 - data.json corrompido: o menu informa o erro, não encerra e não altera o arquivo")
    void bug002ArquivoCorrompidoNoMenu() throws IOException {
        Files.writeString(arquivo, "{isto nao e json");
        String saida = executar(linhas("6", "3", "4", "0"));
        assertEquals(3, SessaoConsole.ocorrencias(saida, "Erro no arquivo de dados"));
        assertTrue(saida.contains("Sistema encerrado."));
        assertEquals("{isto nao e json", Files.readString(arquivo));
    }

    @Test
    @DisplayName("BUG003 - fim da entrada redirecionada encerra o sistema sem exceção")
    void bug003FimDaEntrada() {
        String saida = executar(SessaoConsole.cadastroM1());
        assertTrue(saida.contains("Fim da entrada. Sistema encerrado."));
        assertEquals(1, gravados().size());
    }

    @Test
    @DisplayName("Listagem e relatório com cadastro vazio exibem aviso")
    void cadastroVazio() {
        String saida = executar(linhas("6", "3", "0"));
        assertEquals(2, SessaoConsole.ocorrencias(saida, "Nenhum colaborador cadastrado."));
    }
}
