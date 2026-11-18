import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Folha de pagamento consolidada: relatório detalhado (RF007) e resumo com totais (RF008).
 * Os totais vêm de chamadas polimórficas, sem tratamento específico por tipo (RNF004).
 */
public class FolhaPagamento {

    public static final String SEPARADOR = "------------------------------";

    private final List<Colaborador> colaboradores;

    public FolhaPagamento(List<Colaborador> colaboradores) {
        this.colaboradores = new ArrayList<>(colaboradores);
    }

    public List<Colaborador> getColaboradores() {
        return Collections.unmodifiableList(colaboradores);
    }

    public int getQuantidade() {
        return colaboradores.size();
    }

    public double getTotalGeral() {
        double total = 0;
        for (Colaborador colaborador : colaboradores) {
            total = Dinheiro.somar(total, colaborador.calcularSalarioFinal());
        }
        return total;
    }

    /** Total por código de tipo, com todos os tipos registrados (zero quando não há colaborador). */
    public Map<String, Double> getTotalPorTipo() {
        Map<String, Double> totais = new LinkedHashMap<>();
        for (ColaboradorFactory.TipoColaborador tipo : ColaboradorFactory.tiposRegistrados()) {
            totais.put(tipo.getCodigo(), 0.0);
        }
        for (Colaborador colaborador : colaboradores) {
            double atual = totais.getOrDefault(colaborador.getType(), 0.0);
            totais.put(colaborador.getType(), Dinheiro.somar(atual, colaborador.calcularSalarioFinal()));
        }
        return totais;
    }

    public String gerarRelatorio() {
        StringBuilder texto = new StringBuilder("*** Relatório de Folha de Pagamentos ***\n");
        if (colaboradores.isEmpty()) {
            texto.append("Nenhum colaborador cadastrado.\n");
        }
        for (Colaborador colaborador : colaboradores) {
            texto.append("ID: ").append(colaborador.getId()).append('\n')
                    .append("Nome: ").append(colaborador.getName()).append('\n')
                    .append("Tipo: ").append(colaborador.getDescricaoTipo()).append('\n')
                    .append("Salário Base: ").append(Dinheiro.formatar(colaborador.getBaseSalary())).append('\n')
                    .append("Adicionais: ").append(Dinheiro.formatar(colaborador.calcularAdicional())).append('\n')
                    .append("Salário Final: ").append(Dinheiro.formatar(colaborador.calcularSalarioFinal())).append('\n')
                    .append(SEPARADOR).append('\n');
        }
        return texto.toString();
    }

    public String gerarResumo() {
        StringBuilder texto = new StringBuilder("*** Resumo da Folha de Pagamentos ***\n");
        texto.append("Quantidade de colaboradores: ").append(getQuantidade()).append('\n');
        Map<String, Double> totais = getTotalPorTipo();
        for (ColaboradorFactory.TipoColaborador tipo : ColaboradorFactory.tiposRegistrados()) {
            texto.append("Total de Pagamento para ").append(tipo.getDescricao()).append(": ")
                    .append(Dinheiro.formatar(totais.get(tipo.getCodigo()))).append('\n');
        }
        texto.append("Total da Folha de Pagamentos: ").append(Dinheiro.formatar(getTotalGeral())).append('\n')
                .append(SEPARADOR).append('\n');
        return texto.toString();
    }
}
