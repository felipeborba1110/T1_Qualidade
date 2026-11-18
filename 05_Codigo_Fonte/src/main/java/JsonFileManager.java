import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Persistência dos colaboradores em um array JSON (RNF003).
 * O arquivo é lido por inteiro, alterado em memória e regravado a cada operação.
 */
public class JsonFileManager {

    public static final String ARQUIVO_PADRAO = "data.json";

    private final Path arquivo;

    public JsonFileManager() {
        this(Path.of(ARQUIVO_PADRAO));
    }

    public JsonFileManager(Path arquivo) {
        this.arquivo = arquivo;
    }

    public Path getArquivo() {
        return arquivo;
    }

    /** Arquivo inexistente ou vazio equivale a uma lista vazia. */
    public JSONArray read() {
        try {
            if (!Files.exists(arquivo) || Files.size(arquivo) == 0) {
                return new JSONArray();
            }
            try (Reader reader = Files.newBufferedReader(arquivo, StandardCharsets.UTF_8)) {
                Object conteudo = new JSONParser().parse(reader);
                if (!(conteudo instanceof JSONArray)) {
                    throw new PersistenciaException("O arquivo " + arquivo + " não contém uma lista de colaboradores");
                }
                return (JSONArray) conteudo;
            }
        } catch (IOException | ParseException e) {
            throw new PersistenciaException("Não foi possível ler o arquivo " + arquivo + " (conteúdo inválido)", e);
        }
    }

    /** Grava em arquivo temporário e substitui o original, para não deixar o data.json pela metade. */
    private void save(JSONArray jsonArray) {
        try {
            Path pasta = arquivo.toAbsolutePath().getParent();
            Path temporario = Files.createTempFile(pasta, "data", ".tmp");
            try (Writer writer = Files.newBufferedWriter(temporario, StandardCharsets.UTF_8)) {
                writer.write(jsonArray.toJSONString());
            }
            Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new PersistenciaException("Não foi possível gravar o arquivo " + arquivo, e);
        }
    }

    public List<Colaborador> listarColaboradores() {
        List<Colaborador> colaboradores = new ArrayList<>();
        for (Object registro : read()) {
            try {
                colaboradores.add(ColaboradorFactory.fromJSON((JSONObject) registro));
            } catch (ClassCastException | IllegalArgumentException e) {
                throw new PersistenciaException("Registro inválido no arquivo " + arquivo + ": " + e.getMessage(), e);
            }
        }
        return colaboradores;
    }

    public Optional<Colaborador> buscarPorId(int id) {
        for (Colaborador colaborador : listarColaboradores()) {
            if (colaborador.getId() == id) {
                return Optional.of(colaborador);
            }
        }
        return Optional.empty();
    }

    public List<Colaborador> buscarPorNome(String nome) {
        List<Colaborador> encontrados = new ArrayList<>();
        String procurado = nome == null ? "" : nome.trim();
        for (Colaborador colaborador : listarColaboradores()) {
            if (colaborador.getName().equalsIgnoreCase(procurado)) {
                encontrados.add(colaborador);
            }
        }
        return encontrados;
    }

    public boolean existeId(int id) {
        return buscarPorId(id).isPresent();
    }

    /** RN001: recusa a inclusão de matrícula repetida mesmo fora do fluxo do console. */
    @SuppressWarnings("unchecked")
    public void adicionar(Colaborador colaborador) {
        if (!ValidadorColaborador.matriculaDisponivel(colaborador.getId(), listarColaboradores())) {
            throw new IllegalArgumentException(ValidadorColaborador.MSG_MATRICULA_EXISTENTE);
        }
        JSONArray jsonArray = read();
        jsonArray.add(colaborador.toJSON());
        save(jsonArray);
    }

    @SuppressWarnings("unchecked")
    public void atualizar(Colaborador colaborador) {
        JSONArray jsonArray = read();
        int indice = indicePorId(jsonArray, colaborador.getId());
        if (indice == -1) {
            throw new IllegalArgumentException("Nenhum colaborador com o id " + colaborador.getId());
        }
        jsonArray.set(indice, colaborador.toJSON());
        save(jsonArray);
    }

    public boolean remover(int id) {
        JSONArray jsonArray = read();
        int indice = indicePorId(jsonArray, id);
        if (indice == -1) {
            return false;
        }
        jsonArray.remove(indice);
        save(jsonArray);
        return true;
    }

    private static int indicePorId(JSONArray jsonArray, int id) {
        for (int i = 0; i < jsonArray.size(); i++) {
            Object idAtual = ((JSONObject) jsonArray.get(i)).get("id");
            if (idAtual instanceof Number && ((Number) idAtual).intValue() == id) {
                return i;
            }
        }
        return -1;
    }
}
