import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.stream.Collectors;

import org.json.simple.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Testes unitários da fábrica de colaboradores e da conversão JSON (RN008, RNF003, RNF004).
 */
@Tag("unitario")
@DisplayName("Fábrica de colaboradores e conversão JSON")
class ColaboradorFactoryTest {

    @Test
    @DisplayName("Tipos registrados na ordem do menu: padrão, comissionado e produção")
    void tiposRegistrados() {
        List<String> codigos = ColaboradorFactory.tiposRegistrados().stream()
                .map(ColaboradorFactory.TipoColaborador::getCodigo).collect(Collectors.toList());
        assertEquals(List.of("padrao", "comissionado", "producao"), codigos.subList(0, 3));
    }

    @Test
    @DisplayName("toJSON e fromJSON preservam todos os dados de cada tipo")
    void idaEVoltaJson() {
        for (Colaborador original : List.of(MassaDados.m1(), MassaDados.m2(), MassaDados.m3())) {
            Colaborador lido = ColaboradorFactory.fromJSON(original.toJSON());
            assertEquals(original.getClass(), lido.getClass());
            assertEquals(original.toJSON(), lido.toJSON());
        }
    }

    @Test
    @DisplayName("toJSON grava os campos do registro, incluindo o salário final calculado")
    void camposDoJson() {
        JSONObject json = MassaDados.m2().toJSON();
        assertEquals(2, json.get("id"));
        assertEquals("Bruno", json.get("name"));
        assertEquals("comissionado", json.get("type"));
        assertEquals(2500.0, json.get("finalSalary"));
        assertEquals(10000.0, json.get("productValue"));
        assertEquals(0.05, json.get("commission"));
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("RN008 - salário final gravado no arquivo é ignorado e recalculado")
    void rn008SalarioFinalRecalculado() {
        JSONObject json = MassaDados.m3().toJSON();
        json.put("finalSalary", 999999.0);
        Colaborador lido = ColaboradorFactory.fromJSON(json);
        assertInstanceOf(ColaboradorProducao.class, lido);
        assertEquals(2050.0, lido.calcularSalarioFinal());
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("Registro sem tipo, com tipo desconhecido ou sem campo obrigatório é recusado")
    void registrosInvalidos() {
        JSONObject semTipo = MassaDados.m1().toJSON();
        semTipo.remove("type");
        JSONObject tipoDesconhecido = MassaDados.m1().toJSON();
        tipoDesconhecido.put("type", "estagiario");
        JSONObject semVendas = MassaDados.m2().toJSON();
        semVendas.remove("productValue");
        JSONObject semNome = MassaDados.m1().toJSON();
        semNome.remove("name");

        assertThrows(IllegalArgumentException.class, () -> ColaboradorFactory.fromJSON(semTipo));
        assertThrows(IllegalArgumentException.class, () -> ColaboradorFactory.fromJSON(tipoDesconhecido));
        assertThrows(IllegalArgumentException.class, () -> ColaboradorFactory.fromJSON(semVendas));
        assertThrows(IllegalArgumentException.class, () -> ColaboradorFactory.fromJSON(semNome));
        assertThrows(IllegalArgumentException.class, () -> ColaboradorFactory.novo("inexistente"));
    }

    @Test
    @DisplayName("Tipo base não oferece campos específicos para alteração")
    void padraoSemCamposEspecificos() {
        ColaboradorPadrao ana = MassaDados.m1();
        assertEquals(List.of(), ana.getCamposEspecificos());
        assertEquals(List.of(), ana.getDetalhes());
        assertThrows(IllegalArgumentException.class, () -> ana.alterarCampoEspecifico(0, null));
        assertThrows(IllegalArgumentException.class, () -> MassaDados.m2().alterarCampoEspecifico(5, null));
        assertThrows(IllegalArgumentException.class, () -> MassaDados.m3().alterarCampoEspecifico(5, null));
    }
}
