public class ColaboradorComissionado extends Colaborador{
    private float comission;
    private float productValue;
    private double finalSalary;
    
    public ColaboradorComissionado(double baseSalary, int id, String name, String type, float comission, float productValue) {
        super(baseSalary, id, name, type);
        this.comission = comission;
        this.productValue = productValue;
    }

    public float getComission() {
        return comission;
    }

    public void setComission(float comission) {
        this.comission = comission;
    }

    public float getProductValue() {
        return productValue;
    }

    public void setProductValue(float productValue) {
        this.productValue = productValue;
    }

    


    
}
