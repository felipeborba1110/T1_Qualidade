import java.util.Collections;
import java.util.List;

import org.json.simple.JSONObject;

/**
 * Classe base de todos os tipos de colaborador.
 * O salário final é sempre calculado (RN008): salário base + adicional da categoria.
 * Para incluir um novo tipo basta criar uma subclasse e registrá-la em ColaboradorFactory (RNF004).
 */
public abstract class Colaborador {

    private int id;
    private String name;
    private double baseSalary;

    protected Colaborador() {
    }

    protected Colaborador(int id, String name, double baseSalary) {
        setId(id);
        setName(name);
        setBaseSalary(baseSalary);
    }

    /** Código do tipo gravado no campo "type" do data.json (ex.: "padrao"). */
    public abstract String getType();

    /** Nome do tipo exibido ao usuário (ex.: "Padrão"). */
    public abstract String getDescricaoTipo();

    /** Valor que a categoria acrescenta ao salário base. */
    public abstract double calcularAdicional();

    public final double calcularSalarioFinal() {
        return Dinheiro.somar(baseSalary, calcularAdicional());
    }

    @SuppressWarnings("unchecked")
    public final JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("name", name);
        json.put("type", getType());
        json.put("baseSalary", baseSalary);
        json.put("finalSalary", calcularSalarioFinal());
        preencherJSON(json);
        return json;
    }

    /** Grava os campos específicos da categoria. */
    protected void preencherJSON(JSONObject json) {
    }

    /** Lê os campos específicos da categoria a partir do registro salvo. */
    protected void carregarJSON(JSONObject json) {
    }

    /** Solicita ao usuário os campos específicos da categoria no cadastro. */
    public void lerDadosEspecificos(EntradaConsole entrada) {
    }

    /** Nomes dos campos específicos que podem ser alterados, na ordem do menu de alteração. */
    public List<String> getCamposEspecificos() {
        return Collections.emptyList();
    }

    /** Altera o campo específico de índice informado (0 = primeiro de getCamposEspecificos()). */
    public void alterarCampoEspecifico(int indice, EntradaConsole entrada) {
        throw new IllegalArgumentException("Campo inexistente para o tipo " + getDescricaoTipo());
    }

    /** Linhas com os dados específicos exibidas na consulta. */
    public List<String> getDetalhes() {
        return Collections.emptyList();
    }

    protected static double lerNumero(JSONObject json, String campo) {
        Object valor = json.get(campo);
        if (!(valor instanceof Number)) {
            throw new IllegalArgumentException("Campo \"" + campo + "\" ausente ou inválido no registro");
        }
        return ((Number) valor).doubleValue();
    }

    public int getId() {
        return id;
    }

    public final void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public final void setName(String name) {
        this.name = ValidadorColaborador.validarNome(name);
    }

    public double getBaseSalary() {
        return baseSalary;
    }

    public final void setBaseSalary(double baseSalary) {
        this.baseSalary = ValidadorColaborador.validarNaoNegativo(baseSalary, "Salário base");
    }

    public double getFinalSalary() {
        return calcularSalarioFinal();
    }
}
