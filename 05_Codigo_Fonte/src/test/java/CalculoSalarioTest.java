import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Testes unitários do cálculo de salário por categoria (RF002, RF003, RF004, RF006, RN008).
 */
@Tag("unitario")
@DisplayName("Cálculo de salário por categoria")
class CalculoSalarioTest {

    private static final double DELTA = 0.0001;

    @Nested
    @DisplayName("Colaborador padrão")
    class Padrao {

        @Test
        @DisplayName("CT002 - salário final igual ao salário base (RF002)")
        void ct002SalarioFinalIgualAoBase() {
            ColaboradorPadrao ana = MassaDados.m1();
            assertAll(
                    () -> assertEquals(0.0, ana.calcularAdicional(), DELTA),
                    () -> assertEquals(3000.00, ana.calcularSalarioFinal(), DELTA),
                    () -> assertEquals("padrao", ana.getType()),
                    () -> assertEquals("Padrão", ana.getDescricaoTipo()));
        }

        @Test
        @DisplayName("CT013 - salário base zero é aceito e gera salário final zero (RN003, valor-limite)")
        void ct013SalarioBaseZero() {
            assertEquals(0.0, new ColaboradorPadrao(9, "Zé", 0).calcularSalarioFinal(), DELTA);
        }
    }

    @Nested
    @DisplayName("Colaborador comissionado")
    class Comissionado {

        @Test
        @DisplayName("CT003 - comissão de R$ 500,00 e salário final de R$ 2.500,00 (RF003)")
        void ct003CalculoComissao() {
            ColaboradorComissionado bruno = MassaDados.m2();
            assertAll(
                    () -> assertEquals(500.00, bruno.calcularAdicional(), DELTA),
                    () -> assertEquals(2500.00, bruno.calcularSalarioFinal(), DELTA));
        }

        @Test
        @DisplayName("CT006 - salário final recalculado ao alterar as vendas para R$ 20.000,00 (RF006)")
        void ct006RecalculoAposAlteracao() {
            ColaboradorComissionado bruno = MassaDados.m2();
            bruno.setProductValue(20000.0);
            assertEquals(3000.00, bruno.calcularSalarioFinal(), DELTA);
            bruno.setCommission(0.10);
            assertEquals(4000.00, bruno.getFinalSalary(), DELTA);
        }

        @ParameterizedTest(name = "base {0} + vendas {1} × {2} = {3}")
        @DisplayName("Partições de equivalência e valores-limite da comissão")
        @CsvSource({
            "2000, 0, 0.05, 2000.00",
            "2000, 10000, 0, 2000.00",
            "0, 10000, 0.05, 500.00",
            "1500.50, 3333.33, 0.03, 1600.50",
            "0, 1234567.89, 1, 1234567.89"
        })
        void particoesComissao(double base, double vendas, double percentual, double esperado) {
            ColaboradorComissionado c = new ColaboradorComissionado(1, "Teste", base, vendas, percentual);
            assertEquals(esperado, c.calcularSalarioFinal(), DELTA);
        }

        @Test
        @DisplayName("BUG001 - regressão: valores com centavos não perdem precisão")
        void bug001SemImprecisaoDeCentavos() {
            ColaboradorComissionado c = new ColaboradorComissionado(1, "Teste", 0.1, 0.2, 1);
            assertEquals(0.30, c.calcularSalarioFinal(), 0.0);
        }
    }

    @Nested
    @DisplayName("Colaborador por produção")
    class Producao {

        @Test
        @DisplayName("CT004 - produtividade de R$ 250,00 e salário final de R$ 2.050,00 (RF004)")
        void ct004CalculoProducao() {
            ColaboradorProducao carla = MassaDados.m3();
            assertAll(
                    () -> assertEquals(250.00, carla.calcularAdicional(), DELTA),
                    () -> assertEquals(2050.00, carla.calcularSalarioFinal(), DELTA));
        }

        @ParameterizedTest(name = "base {0} + {1} un × {2} = {3}")
        @DisplayName("Partições de equivalência e valores-limite da produção")
        @CsvSource({
            "1800, 0, 2.50, 1800.00",
            "1800, 100, 0, 1800.00",
            "0, 1, 0.01, 0.01",
            "1000, 333, 1.11, 1369.63"
        })
        void particoesProducao(double base, int quantidade, double valorUnidade, double esperado) {
            ColaboradorProducao c = new ColaboradorProducao(1, "Teste", base, quantidade, valorUnidade);
            assertEquals(esperado, c.calcularSalarioFinal(), DELTA);
        }

        @Test
        @DisplayName("RF006 - salário recalculado ao alterar quantidade e valor por unidade")
        void recalculoProducao() {
            ColaboradorProducao carla = MassaDados.m3();
            carla.setProdQuant(200);
            assertEquals(2300.00, carla.calcularSalarioFinal(), DELTA);
            carla.setUnitValue(3.0);
            assertEquals(2400.00, carla.calcularSalarioFinal(), DELTA);
        }
    }

    @Nested
    @DisplayName("Regras de negócio aplicadas no modelo")
    class RegrasNoModelo {

        @Test
        @DisplayName("CT012 - nome vazio é recusado pelo modelo (RN002)")
        void ct012NomeVazio() {
            assertThrows(IllegalArgumentException.class, () -> new ColaboradorPadrao(1, "  ", 100));
            ColaboradorPadrao ana = MassaDados.m1();
            assertThrows(IllegalArgumentException.class, () -> ana.setName(""));
            assertEquals("Ana", ana.getName());
        }

        @Test
        @DisplayName("CT013 a CT017 - valores negativos são recusados pelo modelo (RN003 a RN007)")
        void ct013a017ValoresNegativos() {
            assertAll(
                    () -> assertThrows(IllegalArgumentException.class, () -> new ColaboradorPadrao(1, "A", -100)),
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> new ColaboradorComissionado(1, "A", 2000, -1000, 0.05)),
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> new ColaboradorComissionado(1, "A", 2000, 10000, -0.05)),
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> new ColaboradorProducao(1, "A", 1800, -10, 2.5)),
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> new ColaboradorProducao(1, "A", 1800, 100, -2.5)));
        }

        @Test
        @DisplayName("Valor inválido na alteração não muda o dado anterior")
        void alteracaoInvalidaPreservaDado() {
            ColaboradorComissionado bruno = MassaDados.m2();
            assertThrows(IllegalArgumentException.class, () -> bruno.setProductValue(-1));
            assertEquals(10000.0, bruno.getProductValue(), DELTA);
            assertEquals(2500.0, bruno.calcularSalarioFinal(), DELTA);
        }
    }
}
