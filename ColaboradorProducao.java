public class ColaboradorProducao extends Colaborador{
    private int prodQuant;
    private float unitValue;
    public ColaboradorProducao(int id, String name, String type, double baseSalary, double finalSalary, int prodQuant, float unitValue) {
        super(id, name, type, baseSalary, finalSalary);
        this.prodQuant = prodQuant;
        this.unitValue = unitValue;
    }

    public int getProdQuant() {
        return prodQuant;
    }

    public void setProdQuant(int prodQuant) {
        this.prodQuant = prodQuant;
    }

    public float getUnitValue() {
        return unitValue;
    }

    public void setUnitValue(float unitValue) {
        this.unitValue = unitValue;
    }
}
