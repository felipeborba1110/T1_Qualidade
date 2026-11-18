/**
 * Colaborador que recebe apenas o salário base (RF002).
 */
public class ColaboradorPadrao extends Colaborador {

    public static final String TIPO = "padrao";

    public ColaboradorPadrao() {
    }

    public ColaboradorPadrao(int id, String name, double baseSalary) {
        super(id, name, baseSalary);
    }

    @Override
    public String getType() {
        return TIPO;
    }

    @Override
    public String getDescricaoTipo() {
        return "Padrão";
    }

    @Override
    public double calcularAdicional() {
        return 0;
    }
}
