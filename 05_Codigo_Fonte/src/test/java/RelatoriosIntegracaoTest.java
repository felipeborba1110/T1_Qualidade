import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Relatórios emitidos pelo menu a partir dos dados gravados no arquivo (RF007, RF008).
 */
@Tag("integracao")
@DisplayName("Relatórios da folha a partir do arquivo")
class RelatoriosIntegracaoTest {

    @TempDir
    Path pasta;

    private Path arquivo;

    @BeforeEach
    void cadastrarMassa() {
        arquivo = pasta.resolve("data.json");
        JsonFileManager dados = new JsonFileManager(arquivo);
        dados.adicionar(MassaDados.m1());
        dados.adicionar(MassaDados.m2());
        dados.adicionar(MassaDados.m3());
    }

    @Test
    @DisplayName("CT007 - folha com os três tipos, base, adicionais e salário final (RF007)")
    void ct007FolhaDePagamento() {
        String saida = SessaoConsole.executarMenu(arquivo, "3", "0");
        assertTrue(saida.contains("*** Relatório de Folha de Pagamentos ***"));
        assertTrue(saida.contains("ID: 1\nNome: Ana\nTipo: Padrão\nSalário Base: R$ 3.000,00\n"
                + "Adicionais: R$ 0,00\nSalário Final: R$ 3.000,00"));
        assertTrue(saida.contains("ID: 2\nNome: Bruno\nTipo: Comissionado\nSalário Base: R$ 2.000,00\n"
                + "Adicionais: R$ 500,00\nSalário Final: R$ 2.500,00"));
        assertTrue(saida.contains("ID: 3\nNome: Carla\nTipo: Produção\nSalário Base: R$ 1.800,00\n"
                + "Adicionais: R$ 250,00\nSalário Final: R$ 2.050,00"));
    }

    @Test
    @DisplayName("CT008 - resumo com quantidade 3, totais por categoria e total geral R$ 7.550,00 (RF008)")
    void ct008ResumoDaFolha() {
        String saida = SessaoConsole.executarMenu(arquivo, "4", "0");
        assertTrue(saida.contains("Quantidade de colaboradores: 3\n"
                + "Total de Pagamento para Colaborador Padrão: R$ 3.000,00\n"
                + "Total de Pagamento para Colaborador Comissionado: R$ 2.500,00\n"
                + "Total de Pagamento para Colaborador Produção: R$ 2.050,00\n"
                + "Total da Folha de Pagamentos: R$ 7.550,00"));
    }

    @Test
    @DisplayName("Consulta exibe os dados específicos e o percentual com decimais (BUG007)")
    void consultaComDetalhes() {
        new JsonFileManager(arquivo).adicionar(new ColaboradorComissionado(4, "Duda", 2000, 10000, 0.025));
        String saida = SessaoConsole.executarMenu(arquivo, "6", "0");
        assertTrue(saida.contains("Valor do produto: R$ 10.000,00\nComissão: 5% (R$ 500,00)"));
        assertTrue(saida.contains("Valor da Unidade: R$ 2,50\nQuantidade de Unidades: 100\nProdutividade: R$ 250,00"));
        assertTrue(saida.contains("Comissão: 2,5% (R$ 250,00)"));
    }
}
