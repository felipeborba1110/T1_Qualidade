import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Testes unitários da folha consolidada e da formatação monetária (RF007, RF008).
 */
@Tag("unitario")
@DisplayName("Folha de pagamento e formatação")
class FolhaPagamentoTest {

    private final FolhaPagamento folha = new FolhaPagamento(List.of(MassaDados.m1(), MassaDados.m2(), MassaDados.m3()));

    @Test
    @DisplayName("CT008 - quantidade 3, totais por categoria e total geral de R$ 7.550,00 (RF008)")
    void ct008TotaisDaFolha() {
        Map<String, Double> totais = folha.getTotalPorTipo();
        assertAll(
                () -> assertEquals(3, folha.getQuantidade()),
                () -> assertEquals(3000.0, totais.get("padrao")),
                () -> assertEquals(2500.0, totais.get("comissionado")),
                () -> assertEquals(2050.0, totais.get("producao")),
                () -> assertEquals(7550.0, folha.getTotalGeral()));
    }

    @Test
    @DisplayName("CT007 - relatório lista base, adicionais e salário final de cada colaborador (RF007)")
    void ct007RelatorioDetalhado() {
        String relatorio = folha.gerarRelatorio();
        assertAll(
                () -> assertTrue(relatorio.contains("Nome: Ana\nTipo: Padrão\nSalário Base: R$ 3.000,00\n"
                        + "Adicionais: R$ 0,00\nSalário Final: R$ 3.000,00")),
                () -> assertTrue(relatorio.contains("Nome: Bruno\nTipo: Comissionado\nSalário Base: R$ 2.000,00\n"
                        + "Adicionais: R$ 500,00\nSalário Final: R$ 2.500,00")),
                () -> assertTrue(relatorio.contains("Nome: Carla\nTipo: Produção\nSalário Base: R$ 1.800,00\n"
                        + "Adicionais: R$ 250,00\nSalário Final: R$ 2.050,00")));
    }

    @Test
    @DisplayName("Folha vazia: quantidade zero, totais zerados e aviso no relatório")
    void folhaVazia() {
        FolhaPagamento vazia = new FolhaPagamento(List.of());
        assertEquals(0, vazia.getQuantidade());
        assertEquals(0.0, vazia.getTotalGeral());
        vazia.getTotalPorTipo().values().forEach(total -> assertEquals(0.0, total));
        assertTrue(vazia.gerarRelatorio().contains("Nenhum colaborador cadastrado."));
        assertTrue(vazia.gerarResumo().contains("Total da Folha de Pagamentos: R$ 0,00"));
    }

    @Test
    @DisplayName("A lista da folha não pode ser alterada por fora")
    void listaImutavel() {
        assertThrows(UnsupportedOperationException.class, () -> folha.getColaboradores().clear());
    }

    @Test
    @DisplayName("Formatação monetária e percentual no padrão brasileiro (BUG007)")
    void formatacao() {
        assertAll(
                () -> assertEquals("R$ 3.000,00", Dinheiro.formatar(3000)),
                () -> assertEquals("R$ 1.234.567,89", Dinheiro.formatar(1234567.89)),
                () -> assertEquals("R$ 0,00", Dinheiro.formatar(0)),
                () -> assertEquals("5%", Dinheiro.formatarPercentual(0.05)),
                () -> assertEquals("2,5%", Dinheiro.formatarPercentual(0.025)),
                () -> assertEquals("12,35%", Dinheiro.formatarPercentual(0.12345)),
                () -> assertEquals(0.3, Dinheiro.somar(0.1, 0.2), 0.0),
                () -> assertEquals(0.01, Dinheiro.multiplicar(0.005, 1), 0.0));
    }
}
