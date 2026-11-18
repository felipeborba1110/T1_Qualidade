import java.nio.file.Path;

public class Main {

    /** Permite indicar outro arquivo de dados: java -Dfolha.dados=caminho/arquivo.json -jar folha-pagamento.jar */
    public static final String PROPRIEDADE_ARQUIVO = "folha.dados";

    public static void main(String[] args) {
        Path arquivo = Path.of(System.getProperty(PROPRIEDADE_ARQUIVO, JsonFileManager.ARQUIVO_PADRAO));
        Menu app = new Menu(new EntradaConsole(System.in, System.out), new JsonFileManager(arquivo));
        app.menu();
    }
}
