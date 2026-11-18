import java.util.List;
import java.util.Optional;

/**
 * Operações de console sobre colaboradores: cadastro, consulta, alteração, exclusão e relatórios.
 * O cálculo fica nas classes de colaborador e os totais em FolhaPagamento.
 */
public class UtilsColaborador {

    public static final String MSG_NAO_ENCONTRADO_ID = "Nenhum colaborador com este id, tente novamente!";
    public static final String MSG_NAO_ENCONTRADO_NOME = "Nenhum colaborador encontrado!";
    public static final String MSG_OPCAO_INVALIDA = "Opção inválida! Tente novamente.";

    private static final int OPCAO_NOME = 1;
    private static final int OPCAO_SALARIO_BASE = 2;
    private static final int OPCAO_TIPO = 3;
    private static final int PRIMEIRA_OPCAO_ESPECIFICA = 4;

    private final EntradaConsole entrada;
    private final JsonFileManager dados;

    public UtilsColaborador(EntradaConsole entrada, JsonFileManager dados) {
        this.entrada = entrada;
        this.dados = dados;
    }

    /** UC01: solicita os dados; o salário final nunca é perguntado (RN008). */
    public Colaborador criarColaborador() {
        entrada.exibir("------ Adicionando colaborador ------");
        int id = validadorId();
        String nome = entrada.lerNomeObrigatorio("Insira o nome:");
        double salarioBase = entrada.lerDecimalNaoNegativo("Insira o salário base:");
        Colaborador colaborador = ColaboradorFactory.novo(configuradorTipo());
        colaborador.setId(id);
        colaborador.setName(nome);
        colaborador.setBaseSalary(salarioBase);
        colaborador.lerDadosEspecificos(entrada);
        return colaborador;
    }

    /** RN001: solicita a matrícula até receber uma que não esteja em uso. */
    private int validadorId() {
        while (true) {
            int id = entrada.lerInteiro("Insira o id:");
            if (!dados.existeId(id)) {
                return id;
            }
            entrada.exibir(ValidadorColaborador.MSG_MATRICULA_EXISTENTE);
        }
    }

    /** Lista os tipos registrados e devolve o código do escolhido. */
    private String configuradorTipo() {
        List<ColaboradorFactory.TipoColaborador> tipos = ColaboradorFactory.tiposRegistrados();
        StringBuilder opcoes = new StringBuilder("Insira o tipo pelo número:");
        for (int i = 0; i < tipos.size(); i++) {
            opcoes.append('\n').append(i + 1).append("- ").append(tipos.get(i).getDescricao());
        }
        while (true) {
            int opcao = entrada.lerInteiro(opcoes.toString());
            if (opcao >= 1 && opcao <= tipos.size()) {
                return tipos.get(opcao - 1).getCodigo();
            }
            entrada.exibir(MSG_OPCAO_INVALIDA);
        }
    }

    /** UC02: consulta por matrícula ou nome; sem resultado, pergunta de novo e permite voltar (BUG004). */
    public void procurarColaborador() {
        while (true) {
            int opcao = entrada.lerInteiro("""
                    Como deseja procurar por colaborador:
                    1- Por ID
                    2- Por Nome
                    0- Voltar ao menu""");
            if (opcao == 0) {
                return;
            } else if (opcao == 1) {
                Optional<Colaborador> encontrado = dados.buscarPorId(entrada.lerInteiro("Insira o ID do Colaborador:"));
                if (encontrado.isPresent()) {
                    entrada.exibir("------ Colaborador Encontrado -----");
                    imprimirColaborador(encontrado.get());
                    return;
                }
                entrada.exibir(MSG_NAO_ENCONTRADO_ID);
            } else if (opcao == 2) {
                List<Colaborador> encontrados = dados.buscarPorNome(entrada.lerTexto("Insira o Nome do Colaborador:"));
                if (!encontrados.isEmpty()) {
                    entrada.exibir("------ Colaborador Encontrado -----");
                    encontrados.forEach(this::imprimirColaborador);
                    return;
                }
                entrada.exibir(MSG_NAO_ENCONTRADO_NOME);
            } else {
                entrada.exibir(MSG_OPCAO_INVALIDA);
            }
        }
    }

    public void imprimirColaborador(Colaborador colaborador) {
        entrada.exibir("ID: " + colaborador.getId());
        entrada.exibir("Nome: " + colaborador.getName());
        entrada.exibir("Salário Base: " + Dinheiro.formatar(colaborador.getBaseSalary()));
        entrada.exibir("Tipo: " + colaborador.getDescricaoTipo());
        entrada.exibir("Salário Final: " + Dinheiro.formatar(colaborador.calcularSalarioFinal()));
        colaborador.getDetalhes().forEach(entrada::exibir);
        entrada.exibir(FolhaPagamento.SEPARADOR);
    }

    public void imprimirTodosColaboradores() {
        entrada.exibir("*** Lista de Colaboradores ***");
        List<Colaborador> colaboradores = dados.listarColaboradores();
        if (colaboradores.isEmpty()) {
            entrada.exibir("Nenhum colaborador cadastrado.");
        }
        colaboradores.forEach(this::imprimirColaborador);
    }

    /** UC03 / RF007. */
    public void imprimirRelatorio() {
        entrada.saida().print(new FolhaPagamento(dados.listarColaboradores()).gerarRelatorio());
    }

    /** UC04 / RF008. */
    public void imprimirResumo() {
        entrada.saida().print(new FolhaPagamento(dados.listarColaboradores()).gerarResumo());
    }

    /** UC05: altera um campo; o salário final é recalculado automaticamente (RF006). */
    public void modificarColaborador() {
        Optional<Colaborador> encontrado = dados.buscarPorId(entrada.lerInteiro("Insira o ID do Colaborador:"));
        if (encontrado.isEmpty()) {
            entrada.exibir(MSG_NAO_ENCONTRADO_ID);
            return;
        }
        Colaborador colaborador = encontrado.get();
        entrada.exibir("------ Colaborador Encontrado -----");
        imprimirColaborador(colaborador);

        List<String> camposEspecificos = colaborador.getCamposEspecificos();
        StringBuilder opcoes = new StringBuilder("Qual informação deseja alterar?\n1- Nome\n2- Salário Base\n3- Tipo");
        for (int i = 0; i < camposEspecificos.size(); i++) {
            opcoes.append('\n').append(PRIMEIRA_OPCAO_ESPECIFICA + i).append("- ").append(camposEspecificos.get(i));
        }
        int ultimaOpcao = OPCAO_TIPO + camposEspecificos.size();
        int opcao = entrada.lerInteiro(opcoes + "\nOpção:");
        while (opcao < OPCAO_NOME || opcao > ultimaOpcao) {
            entrada.exibir(MSG_OPCAO_INVALIDA);
            opcao = entrada.lerInteiro("Opção:");
        }

        if (opcao == OPCAO_NOME) {
            colaborador.setName(entrada.lerNomeObrigatorio("Insira o novo nome:"));
        } else if (opcao == OPCAO_SALARIO_BASE) {
            colaborador.setBaseSalary(entrada.lerDecimalNaoNegativo("Insira o novo salário base:"));
        } else if (opcao == OPCAO_TIPO) {
            entrada.exibir("Selecione o novo tipo:");
            Colaborador novo = ColaboradorFactory.novo(configuradorTipo());
            novo.setId(colaborador.getId());
            novo.setName(colaborador.getName());
            novo.setBaseSalary(colaborador.getBaseSalary());
            novo.lerDadosEspecificos(entrada);
            colaborador = novo;
        } else {
            colaborador.alterarCampoEspecifico(opcao - PRIMEIRA_OPCAO_ESPECIFICA, entrada);
        }

        dados.atualizar(colaborador);
        entrada.exibir("Colaborador modificado com sucesso!");
        entrada.exibir("------ Dados atualizados -----");
        imprimirColaborador(colaborador);
    }

    /** UC06: exclui somente após a confirmação S. */
    public void removerColaborador() {
        Optional<Colaborador> encontrado = dados.buscarPorId(entrada.lerInteiro("Insira o ID do Colaborador:"));
        if (encontrado.isEmpty()) {
            entrada.exibir(MSG_NAO_ENCONTRADO_ID);
            return;
        }
        entrada.exibir("------ Colaborador Encontrado -----");
        imprimirColaborador(encontrado.get());
        if (entrada.confirmar("Realmente deseja excluir este colaborador? <S/N>")) {
            dados.remover(encontrado.get().getId());
            entrada.exibir("Colaborador removido com sucesso");
        } else {
            entrada.exibir("Retornando ao menu...");
        }
    }
}
