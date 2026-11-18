import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Testes unitários dos campos específicos de cada categoria: leitura no cadastro, alteração e exibição
 * (RF003, RF004, RF006, RN004 a RN007).
 */
@Tag("unitario")
@DisplayName("Campos específicos das categorias")
class CamposEspecificosTest {

    private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    private final PrintStream saida = new PrintStream(bytes, true, StandardCharsets.UTF_8);

    private String exibido() {
        return bytes.toString(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("CT014 e CT015 - comissionado lê vendas e percentual, recusando negativos (RN004, RN005)")
    void comissionadoLeDados() {
        ColaboradorComissionado c = new ColaboradorComissionado();
        c.lerDadosEspecificos(SessaoConsole.entrada(saida, "-1000", "10000", "-0,05", "0,05"));
        assertEquals(10000.0, c.getProductValue());
        assertEquals(0.05, c.getCommission());
        assertEquals(2, SessaoConsole.ocorrencias(exibido(), ValidadorColaborador.MSG_VALOR_NEGATIVO));
    }

    @Test
    @DisplayName("CT016 e CT017 - produção lê quantidade e valor por unidade, recusando negativos (RN006, RN007)")
    void producaoLeDados() {
        ColaboradorProducao c = new ColaboradorProducao();
        c.lerDadosEspecificos(SessaoConsole.entrada(saida, "-10", "100", "-2,50", "2,50"));
        assertEquals(100, c.getProdQuant());
        assertEquals(2.5, c.getUnitValue());
        assertEquals(2, SessaoConsole.ocorrencias(exibido(), ValidadorColaborador.MSG_VALOR_NEGATIVO));
    }

    @Test
    @DisplayName("CT006 - alterar cada campo do comissionado recalcula o salário (RF006)")
    void comissionadoAlteraCampos() {
        ColaboradorComissionado c = MassaDados.m2();
        assertEquals(List.of("Valor do Produto", "Comissão"), c.getCamposEspecificos());
        c.alterarCampoEspecifico(0, SessaoConsole.entrada(saida, "20000"));
        assertEquals(3000.0, c.calcularSalarioFinal());
        c.alterarCampoEspecifico(1, SessaoConsole.entrada(saida, "0,10"));
        assertEquals(4000.0, c.calcularSalarioFinal());
    }

    @Test
    @DisplayName("RF006 - alterar cada campo da produção recalcula o salário")
    void producaoAlteraCampos() {
        ColaboradorProducao c = MassaDados.m3();
        assertEquals(List.of("Valor da Unidade", "Quantidade de Unidades"), c.getCamposEspecificos());
        c.alterarCampoEspecifico(0, SessaoConsole.entrada(saida, "3"));
        assertEquals(2100.0, c.calcularSalarioFinal());
        c.alterarCampoEspecifico(1, SessaoConsole.entrada(saida, "200"));
        assertEquals(2400.0, c.calcularSalarioFinal());
    }

    @Test
    @DisplayName("Detalhes exibidos na consulta de cada categoria")
    void detalhes() {
        assertEquals(List.of("Valor do produto: R$ 10.000,00", "Comissão: 5% (R$ 500,00)"), MassaDados.m2().getDetalhes());
        assertEquals(List.of("Valor da Unidade: R$ 2,50", "Quantidade de Unidades: 100", "Produtividade: R$ 250,00"),
                MassaDados.m3().getDetalhes());
        assertTrue(MassaDados.m1().getDetalhes().isEmpty());
    }
}
