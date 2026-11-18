import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Testes unitários das regras de validação (RN001 a RN007), parte unitária dos casos CT011 a CT017.
 */
@Tag("unitario")
@DisplayName("Validações das regras de negócio")
class ValidadorColaboradorTest {

    @Test
    @DisplayName("CT011 - matrícula repetida não está disponível; matrícula nova está (RN001)")
    void ct011MatriculaUnica() {
        List<Colaborador> existentes = List.of(MassaDados.m1(), MassaDados.m2());
        assertFalse(ValidadorColaborador.matriculaDisponivel(1, existentes));
        assertTrue(ValidadorColaborador.matriculaDisponivel(4, existentes));
        assertTrue(ValidadorColaborador.matriculaDisponivel(1, Collections.emptyList()));
    }

    @ParameterizedTest(name = "nome inválido: \"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t"})
    @DisplayName("CT012 - nome nulo, vazio ou só com espaços é inválido (RN002)")
    void ct012NomeInvalido(String nome) {
        assertFalse(ValidadorColaborador.nomeValido(nome));
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> ValidadorColaborador.validarNome(nome));
        assertEquals(ValidadorColaborador.MSG_NOME_OBRIGATORIO, erro.getMessage());
    }

    @Test
    @DisplayName("CT012 - nome válido é aceito sem espaços nas pontas (RN002)")
    void ct012NomeValido() {
        assertTrue(ValidadorColaborador.nomeValido("Davi"));
        assertEquals("Ana Maria", ValidadorColaborador.validarNome("  Ana Maria "));
    }

    @ParameterizedTest(name = "valor {0} é recusado")
    @ValueSource(doubles = {-0.01, -1, -2.5, -100, -1000})
    @DisplayName("CT013 a CT017 - valores negativos são recusados (RN003 a RN007)")
    void ct013a017Negativos(double valor) {
        assertFalse(ValidadorColaborador.naoNegativo(valor));
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> ValidadorColaborador.validarNaoNegativo(valor, "Campo"));
        assertTrue(erro.getMessage().contains(ValidadorColaborador.MSG_VALOR_NEGATIVO));
    }

    @ParameterizedTest(name = "valor {0} é aceito")
    @ValueSource(doubles = {0, 0.01, 2.5, 10000})
    @DisplayName("CT013 a CT017 - zero e positivos são aceitos (valor-limite)")
    void ct013a017NaoNegativos(double valor) {
        assertTrue(ValidadorColaborador.naoNegativo(valor));
        assertEquals(valor, ValidadorColaborador.validarNaoNegativo(valor, "Campo"));
    }
}
