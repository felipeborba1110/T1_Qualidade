public class ColaboradorComissionado extends Colaborador{
    private double commission;
    private double productValue;

    public ColaboradorComissionado(int id, String name, String type, double baseSalary, double finalSalary, double commission, double productValue) {
        super(id, name, type, baseSalary, finalSalary);
        this.commission = commission;
        this.productValue = productValue;
    }

    public double getCommission() {
        return commission;
    }

    public void setCommission(float commission) {
        this.commission = commission;
    }

    public double getProductValue() {
        return productValue;
    }

    public void setProductValue(float productValue) {
        this.productValue = productValue;
    }
}
