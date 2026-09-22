import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class JsonFileManager {

    private final String filePath = "./T1_Qualidade/data.json";

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
}