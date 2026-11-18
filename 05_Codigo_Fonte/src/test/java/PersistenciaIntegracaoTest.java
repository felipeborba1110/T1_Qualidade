import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Integração entre o modelo, a fábrica e o arquivo JSON real em pasta temporária (RNF003, RF009, RN001).
 */
@Tag("integracao")
@DisplayName("Persistência em arquivo JSON")
class PersistenciaIntegracaoTest {

    @TempDir
    Path pasta;

    private Path arquivo;
    private JsonFileManager dados;

    @BeforeEach
    void prepararArquivo() {
        arquivo = pasta.resolve("data.json");
        dados = new JsonFileManager(arquivo);
    }

    @Test
    @DisplayName("CT001 - colaborador gravado no data.json e lido com os mesmos dados (RF001)")
    void ct001GravaELe() throws IOException {
        dados.adicionar(MassaDados.m1());

        assertTrue(Files.exists(arquivo));
        String conteudo = Files.readString(arquivo, StandardCharsets.UTF_8);
        assertTrue(conteudo.contains("\"name\":\"Ana\""));
        assertTrue(conteudo.contains("\"finalSalary\":3000.0"));
        Colaborador lido = dados.buscarPorId(1).orElseThrow();
        assertEquals("Ana", lido.getName());
        assertEquals(3000.0, lido.calcularSalarioFinal());
    }

    @Test
    @DisplayName("CT021 - dados permanecem após encerrar e reabrir (nova instância lê o mesmo arquivo) (RNF003)")
    void ct021DadosPermanecem() {
        dados.adicionar(MassaDados.m1());
        dados.adicionar(MassaDados.m2());
        dados.adicionar(MassaDados.m3());

        List<Colaborador> relidos = new JsonFileManager(arquivo).listarColaboradores();
        assertEquals(3, relidos.size());
        assertEquals(7550.0, new FolhaPagamento(relidos).getTotalGeral());
    }

    @Test
    @DisplayName("CT011 - a persistência recusa matrícula repetida (RN001)")
    void ct011MatriculaRepetida() {
        dados.adicionar(MassaDados.m1());
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> dados.adicionar(new ColaboradorPadrao(1, "Outra", 100)));
        assertEquals(ValidadorColaborador.MSG_MATRICULA_EXISTENTE, erro.getMessage());
        assertEquals(1, dados.listarColaboradores().size());
    }

    @Test
    @DisplayName("CT009 - alteração gravada e mantida após reabrir (RF009)")
    void ct009AlteracaoPersistida() {
        dados.adicionar(MassaDados.m1());
        Colaborador ana = dados.buscarPorId(1).orElseThrow();
        ana.setName("Ana Maria");
        dados.atualizar(ana);

        Colaborador relida = new JsonFileManager(arquivo).buscarPorId(1).orElseThrow();
        assertEquals("Ana Maria", relida.getName());
        assertEquals(3000.0, relida.getBaseSalary());
    }

    @Test
    @DisplayName("CT010 - exclusão remove só o colaborador informado (RF009)")
    void ct010Exclusao() {
        dados.adicionar(MassaDados.m1());
        dados.adicionar(MassaDados.m2());
        dados.adicionar(MassaDados.m3());

        assertTrue(dados.remover(3));
        assertFalse(dados.remover(99));
        assertEquals(List.of(1, 2), List.of(dados.listarColaboradores().get(0).getId(),
                dados.listarColaboradores().get(1).getId()));
    }

    @Test
    @DisplayName("Atualizar matrícula inexistente é recusado")
    void atualizarInexistente() {
        assertThrows(IllegalArgumentException.class, () -> dados.atualizar(MassaDados.m1()));
    }

    @Test
    @DisplayName("Consulta por nome ignora maiúsculas e espaços nas pontas (RF005)")
    void buscaPorNome() {
        dados.adicionar(MassaDados.m1());
        dados.adicionar(MassaDados.m2());
        assertEquals(1, dados.buscarPorNome("  bruno ").size());
        assertTrue(dados.buscarPorNome("Zé").isEmpty());
        assertTrue(dados.buscarPorNome(null).isEmpty());
    }

    @Test
    @DisplayName("Arquivo inexistente ou vazio equivale a lista vazia")
    void arquivoInexistenteOuVazio() throws IOException {
        assertTrue(dados.listarColaboradores().isEmpty());
        Files.writeString(arquivo, "");
        assertTrue(dados.listarColaboradores().isEmpty());
    }

    @Test
    @DisplayName("BUG002 - arquivo corrompido gera PersistenciaException e não é sobrescrito")
    void bug002ArquivoCorrompido() throws IOException {
        Files.writeString(arquivo, "{isto nao e json");
        assertThrows(PersistenciaException.class, () -> dados.listarColaboradores());
        assertThrows(PersistenciaException.class, () -> dados.adicionar(MassaDados.m1()));
        assertEquals("{isto nao e json", Files.readString(arquivo));
    }

    @Test
    @DisplayName("JSON que não é lista ou com registro inválido gera PersistenciaException")
    void conteudoInesperado() throws IOException {
        Files.writeString(arquivo, "{\"id\":1}");
        assertThrows(PersistenciaException.class, () -> dados.listarColaboradores());
        Files.writeString(arquivo, "[{\"id\":1,\"name\":\"Ana\",\"type\":\"estagiario\",\"baseSalary\":1}]");
        assertThrows(PersistenciaException.class, () -> dados.listarColaboradores());
        Files.writeString(arquivo, "[1, 2]");
        assertThrows(PersistenciaException.class, () -> dados.listarColaboradores());
    }

    @Test
    @DisplayName("Arquivo gravado pela versão anterior (06_Banco_Dados/data.exemplo.json) continua legível")
    void compatibilidadeComArquivoAnterior() throws IOException {
        Path exemplo = Path.of("..", "06_Banco_Dados", "data.exemplo.json");
        Files.copy(exemplo, arquivo);
        List<Colaborador> colaboradores = dados.listarColaboradores();
        assertEquals(3, colaboradores.size());
        assertEquals(7550.0, new FolhaPagamento(colaboradores).getTotalGeral());
    }

    @Test
    @DisplayName("Nomes com acentuação são gravados e lidos em UTF-8")
    void acentuacao() {
        dados.adicionar(new ColaboradorPadrao(7, "João Conceição", 1000));
        assertEquals("João Conceição", new JsonFileManager(arquivo).buscarPorId(7).orElseThrow().getName());
    }

    @Test
    @DisplayName("Pasta inexistente gera PersistenciaException na gravação")
    void falhaDeGravacao() {
        JsonFileManager invalido = new JsonFileManager(pasta.resolve("nao-existe").resolve("data.json"));
        assertThrows(PersistenciaException.class, () -> invalido.adicionar(MassaDados.m1()));
    }
}
