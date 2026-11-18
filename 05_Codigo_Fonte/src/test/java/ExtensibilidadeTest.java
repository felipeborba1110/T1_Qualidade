import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

import org.json.simple.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * REV-01 / RNF004: um tipo novo (horista), criado apenas nesta classe de teste, funciona no cadastro,
 * na persistência, na folha e no resumo sem nenhuma alteração no código dos tipos existentes.
 */
@Tag("integracao")
@DisplayName("REV-01 - Extensibilidade para novos tipos de remuneração (RNF004)")
class ExtensibilidadeTest {

    /** Tipo hipotético da inspeção REV-01: salário base + horas trabalhadas × valor da hora. */
    static class ColaboradorHorista extends Colaborador {
        private int horas;
        private double valorHora;

        @Override
        public String getType() {
            return "horista";
        }

        @Override
        public String getDescricaoTipo() {
            return "Horista";
        }

        @Override
        public double calcularAdicional() {
            return Dinheiro.multiplicar(horas, valorHora);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void preencherJSON(JSONObject json) {
            json.put("horas", horas);
            json.put("valorHora", valorHora);
        }

        @Override
        protected void carregarJSON(JSONObject json) {
            horas = (int) lerNumero(json, "horas");
            valorHora = lerNumero(json, "valorHora");
        }

        @Override
        public void lerDadosEspecificos(EntradaConsole entrada) {
            horas = entrada.lerInteiroNaoNegativo("Insira as horas trabalhadas:");
            valorHora = entrada.lerDecimalNaoNegativo("Insira o valor da hora:");
        }
    }

    @TempDir
    Path pasta;

    @BeforeEach
    void registrarHorista() {
        ColaboradorFactory.registrar("horista", "Colaborador Horista", ColaboradorHorista::new);
    }

    @AfterEach
    void removerHorista() {
        ColaboradorFactory.removerRegistro("horista");
    }

    @Test
    @DisplayName("Horista cadastrado pelo menu aparece na folha e no resumo junto dos tipos existentes")
    void horistaNoFluxoCompleto() {
        Path arquivo = pasta.resolve("data.json");
        String saida = SessaoConsole.executarMenu(arquivo, SessaoConsole.concatenar(
                SessaoConsole.cadastroM1(), SessaoConsole.cadastroM2(), SessaoConsole.cadastroM3(),
                new String[] {"1", "4", "Hugo", "1000", "4", "10", "50", "3", "4", "0"}));

        assertTrue(saida.contains("4- Colaborador Horista"));
        assertTrue(saida.contains("Nome: Hugo\nTipo: Horista\nSalário Base: R$ 1.000,00\n"
                + "Adicionais: R$ 500,00\nSalário Final: R$ 1.500,00"));
        assertTrue(saida.contains("Total de Pagamento para Colaborador Horista: R$ 1.500,00"));
        assertTrue(saida.contains("Total da Folha de Pagamentos: R$ 9.050,00"));

        List<Colaborador> gravados = new JsonFileManager(arquivo).listarColaboradores();
        assertEquals(4, gravados.size());
        assertEquals(List.of(3000.0, 2500.0, 2050.0, 1500.0),
                List.of(gravados.get(0).calcularSalarioFinal(), gravados.get(1).calcularSalarioFinal(),
                        gravados.get(2).calcularSalarioFinal(), gravados.get(3).calcularSalarioFinal()));
    }
}
