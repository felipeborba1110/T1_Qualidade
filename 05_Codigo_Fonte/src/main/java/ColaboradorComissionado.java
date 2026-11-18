import java.util.List;

import org.json.simple.JSONObject;

/**
 * Colaborador que recebe comissão sobre as vendas (RF003).
 * Comissão = valor das vendas × percentual de comissão.
 */
public class ColaboradorComissionado extends Colaborador {

    public static final String TIPO = "comissionado";
    private static final String MSG_VENDAS = "Insira o valor de venda dos produtos:";
    private static final String MSG_COMISSAO = "Insira o percentual de comissão: (0,50 = 50%)";

    private double commission;
    private double productValue;

    public ColaboradorComissionado() {
    }

    public ColaboradorComissionado(int id, String name, double baseSalary, double productValue, double commission) {
        super(id, name, baseSalary);
        setProductValue(productValue);
        setCommission(commission);
    }

    @Override
    public String getType() {
        return TIPO;
    }

    @Override
    public String getDescricaoTipo() {
        return "Comissionado";
    }

    @Override
    public double calcularAdicional() {
        return Dinheiro.multiplicar(productValue, commission);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void preencherJSON(JSONObject json) {
        json.put("productValue", productValue);
        json.put("commission", commission);
    }

    @Override
    protected void carregarJSON(JSONObject json) {
        setProductValue(lerNumero(json, "productValue"));
        setCommission(lerNumero(json, "commission"));
    }

    @Override
    public void lerDadosEspecificos(EntradaConsole entrada) {
        setProductValue(entrada.lerDecimalNaoNegativo(MSG_VENDAS));
        setCommission(entrada.lerDecimalNaoNegativo(MSG_COMISSAO));
    }

    @Override
    public List<String> getCamposEspecificos() {
        return List.of("Valor do Produto", "Comissão");
    }

    @Override
    public void alterarCampoEspecifico(int indice, EntradaConsole entrada) {
        switch (indice) {
            case 0:
                setProductValue(entrada.lerDecimalNaoNegativo("Insira o novo valor de venda dos produtos:"));
                break;
            case 1:
                setCommission(entrada.lerDecimalNaoNegativo("Insira a nova comissão: (0,50 = 50%)"));
                break;
            default:
                super.alterarCampoEspecifico(indice, entrada);
        }
    }

    @Override
    public List<String> getDetalhes() {
        return List.of(
                "Valor do produto: " + Dinheiro.formatar(productValue),
                "Comissão: " + Dinheiro.formatarPercentual(commission) + " (" + Dinheiro.formatar(calcularAdicional()) + ")");
    }

    public double getCommission() {
        return commission;
    }

    public final void setCommission(double commission) {
        this.commission = ValidadorColaborador.validarNaoNegativo(commission, "Percentual de comissão");
    }

    public double getProductValue() {
        return productValue;
    }

    public final void setProductValue(double productValue) {
        this.productValue = ValidadorColaborador.validarNaoNegativo(productValue, "Valor das vendas");
    }
}
