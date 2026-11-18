import java.util.List;

import org.json.simple.JSONObject;

/**
 * Colaborador que recebe por unidade produzida (RF004).
 * Produtividade = quantidade produzida × valor por unidade.
 */
public class ColaboradorProducao extends Colaborador {

    public static final String TIPO = "producao";
    private static final String MSG_QUANTIDADE = "Insira a quantidade produzida:";
    private static final String MSG_VALOR_UNIDADE = "Insira o valor de cada unidade:";

    private int prodQuant;
    private double unitValue;

    public ColaboradorProducao() {
    }

    public ColaboradorProducao(int id, String name, double baseSalary, int prodQuant, double unitValue) {
        super(id, name, baseSalary);
        setProdQuant(prodQuant);
        setUnitValue(unitValue);
    }

    @Override
    public String getType() {
        return TIPO;
    }

    @Override
    public String getDescricaoTipo() {
        return "Produção";
    }

    @Override
    public double calcularAdicional() {
        return Dinheiro.multiplicar(prodQuant, unitValue);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void preencherJSON(JSONObject json) {
        json.put("prodQuant", prodQuant);
        json.put("unitValue", unitValue);
    }

    @Override
    protected void carregarJSON(JSONObject json) {
        setProdQuant((int) lerNumero(json, "prodQuant"));
        setUnitValue(lerNumero(json, "unitValue"));
    }

    @Override
    public void lerDadosEspecificos(EntradaConsole entrada) {
        setProdQuant(entrada.lerInteiroNaoNegativo(MSG_QUANTIDADE));
        setUnitValue(entrada.lerDecimalNaoNegativo(MSG_VALOR_UNIDADE));
    }

    @Override
    public List<String> getCamposEspecificos() {
        return List.of("Valor da Unidade", "Quantidade de Unidades");
    }

    @Override
    public void alterarCampoEspecifico(int indice, EntradaConsole entrada) {
        switch (indice) {
            case 0:
                setUnitValue(entrada.lerDecimalNaoNegativo("Insira o novo valor da unidade:"));
                break;
            case 1:
                setProdQuant(entrada.lerInteiroNaoNegativo("Insira a nova quantidade produzida:"));
                break;
            default:
                super.alterarCampoEspecifico(indice, entrada);
        }
    }

    @Override
    public List<String> getDetalhes() {
        return List.of(
                "Valor da Unidade: " + Dinheiro.formatar(unitValue),
                "Quantidade de Unidades: " + prodQuant,
                "Produtividade: " + Dinheiro.formatar(calcularAdicional()));
    }

    public int getProdQuant() {
        return prodQuant;
    }

    public final void setProdQuant(int prodQuant) {
        this.prodQuant = (int) ValidadorColaborador.validarNaoNegativo(prodQuant, "Quantidade produzida");
    }

    public double getUnitValue() {
        return unitValue;
    }

    public final void setUnitValue(double unitValue) {
        this.unitValue = ValidadorColaborador.validarNaoNegativo(unitValue, "Valor por unidade");
    }
}
