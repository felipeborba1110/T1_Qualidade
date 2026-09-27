import org.json.simple.JSONObject;

public class ColaboradorComissionado extends Colaborador{
    private float commission;
    private float productValue;

    public ColaboradorComissionado(int id, String name, String type, double baseSalary, double finalSalary, float commission, float productValue) {
        super(id, name, type, baseSalary, finalSalary);
        this.commission = commission;
        this.productValue = productValue;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = super.toJSON();
        json.put("commission", commission);
        json.put("productValue", productValue);

        return json;
    }

    public float getCommission() {
        return commission;
    }

    public void setCommission(float commission) {
        this.commission = commission;
    }

    public float getProductValue() {
        return productValue;
    }

    public void setProductValue(float productValue) {
        this.productValue = productValue;
    }
}
