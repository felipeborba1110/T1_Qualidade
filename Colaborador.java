import org.json.simple.JSONObject;

public class Colaborador {
    private int id;
    private String name;
    private double baseSalary;
    private String type;
    private double finalSalary;

    public Colaborador(int id, String name, String type,double baseSalary, double finalSalary) {
        this.baseSalary = baseSalary;
        this.id = id;
        this.name = name;
        this.type = type;
        this.finalSalary = finalSalary;
    }

    public JSONObject toJSON(){
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("name", name);
        json.put("type", type);
        json.put("baseSalary", baseSalary);
        json.put("finalSalary", finalSalary);
        return json;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getBaseSalary() {
        return baseSalary;
    }

    public void setBaseSalary(double baseSalary) {
        this.baseSalary = baseSalary;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getFinalSalary() {
        return finalSalary;
    }

    public void setFinalSalary(double finalSalary) {
        this.finalSalary = finalSalary;
    }
}
