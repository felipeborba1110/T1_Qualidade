/**
 * Massa de dados do Plano de Testes (M1, M2 e M3).
 */
final class MassaDados {

    private MassaDados() {
    }

    static ColaboradorPadrao m1() {
        return new ColaboradorPadrao(1, "Ana", 3000.0);
    }

    static ColaboradorComissionado m2() {
        return new ColaboradorComissionado(2, "Bruno", 2000.0, 10000.0, 0.05);
    }

    static ColaboradorProducao m3() {
        return new ColaboradorProducao(3, "Carla", 1800.0, 100, 2.5);
    }
}
