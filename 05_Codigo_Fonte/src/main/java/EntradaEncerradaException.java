/**
 * Indica que a entrada terminou (fim do arquivo ou Ctrl+D/Ctrl+Z). O menu encerra o sistema sem erro.
 */
public class EntradaEncerradaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EntradaEncerradaException() {
        super("Fim da entrada");
    }
}
