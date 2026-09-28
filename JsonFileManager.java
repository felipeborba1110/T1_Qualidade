import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class JsonFileManager {

    private final String filePath = "data.json";

    public JSONArray read() {
        File file = new File(filePath);

        try {
            if (!file.exists() || file.length() == 0) {
                return new JSONArray();
            }

            FileReader reader = new FileReader(file);

            JSONArray jsonArray = (JSONArray) new JSONParser().parse(reader);

            reader.close();
            return jsonArray;
        } catch (Exception e) {
            throw new RuntimeException("Error reading JSON file", e);
        }
    }

    private void save(JSONArray jsonArray) {
        try {
            FileWriter writer = new FileWriter(filePath);

            writer.write(jsonArray.toJSONString());

            writer.close();
        } catch (Exception e) {
            throw new RuntimeException("Error saving JSON file", e);
        }
    }

    public void add(JSONObject jsonObject) {
        JSONArray jsonArray = read();

        jsonArray.add(jsonObject);

        save(jsonArray);
    }

    public void remove(JSONObject jsonObject){
        JSONArray jsonArray = read();

        jsonArray.remove(jsonObject);

        save(jsonArray);
    }

    private int getIndexById(int id){
        JSONArray data = read();

        for(int i = 0; i < data.size(); i++){
            JSONObject colaborador = (JSONObject) data.get(i);

            int idAtual = ((Number) colaborador.get("id")).intValue();

            if(idAtual == id){
                return i;
            }
        }

        return -1;
    }

    public void update(JSONObject colaborador){
        JSONArray jsonArray = read();

        int id = ((Number) colaborador.get("id")).intValue();

        int index = getIndexById(id);

        if(index != -1){
            jsonArray.set(index, colaborador);
            save(jsonArray);
        } else {
            System.out.println("Erro ao atualizar colaborador, ID inválido");
        }
    }

    public List<JSONObject> listaColaboradores(){
        JsonFileManager json = new JsonFileManager();
        JSONArray jsonArray = json.read();
        List<JSONObject> listaColaboradores = new ArrayList<>();

        for(Object obj : jsonArray){
            JSONObject colaborador = (JSONObject) obj;
            listaColaboradores.add(colaborador);
        }

        return listaColaboradores;
    }
}