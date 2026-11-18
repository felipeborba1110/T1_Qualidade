import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.json.simple.JSONObject;

/**
 * Registro dos tipos de colaborador e criação de objetos a partir do código do tipo ou do data.json.
 * Um novo tipo é incluído com uma subclasse de Colaborador e uma chamada a registrar() (RNF004).
 */
public final class ColaboradorFactory {

    /** Tipo de colaborador disponível no sistema. */
    public static final class TipoColaborador {
        private final String codigo;
        private final String descricao;
        private final Supplier<Colaborador> criador;

        TipoColaborador(String codigo, String descricao, Supplier<Colaborador> criador) {
            this.codigo = codigo;
            this.descricao = descricao;
            this.criador = criador;
        }

        public String getCodigo() {
            return codigo;
        }

        public String getDescricao() {
            return descricao;
        }

        Colaborador criar() {
            return criador.get();
        }
    }

    private static final Map<String, TipoColaborador> TIPOS = new LinkedHashMap<>();

    static {
        registrar(ColaboradorPadrao.TIPO, "Colaborador Padrão", ColaboradorPadrao::new);
        registrar(ColaboradorComissionado.TIPO, "Colaborador Comissionado", ColaboradorComissionado::new);
        registrar(ColaboradorProducao.TIPO, "Colaborador Produção", ColaboradorProducao::new);
    }

    private ColaboradorFactory() {
    }

    public static synchronized void registrar(String codigo, String descricao, Supplier<Colaborador> criador) {
        TIPOS.put(codigo, new TipoColaborador(codigo, descricao, criador));
    }

    /** Usado apenas pelos testes de extensibilidade para desfazer um registro temporário. */
    static synchronized void removerRegistro(String codigo) {
        TIPOS.remove(codigo);
    }

    public static synchronized List<TipoColaborador> tiposRegistrados() {
        return new ArrayList<>(TIPOS.values());
    }

    public static synchronized Colaborador novo(String codigo) {
        TipoColaborador tipo = TIPOS.get(codigo);
        if (tipo == null) {
            throw new IllegalArgumentException("Tipo de colaborador desconhecido: " + codigo);
        }
        return tipo.criar();
    }

    /** Reconstrói o colaborador gravado; o salário final é recalculado e não lido do arquivo (RN008). */
    public static Colaborador fromJSON(JSONObject json) {
        Object tipo = json.get("type");
        if (!(tipo instanceof String)) {
            throw new IllegalArgumentException("Registro sem o campo \"type\"");
        }
        Colaborador colaborador = novo((String) tipo);
        colaborador.setId((int) Colaborador.lerNumero(json, "id"));
        Object nome = json.get("name");
        colaborador.setName(nome == null ? null : nome.toString());
        colaborador.setBaseSalary(Colaborador.lerNumero(json, "baseSalary"));
        colaborador.carregarJSON(json);
        return colaborador;
    }
}
