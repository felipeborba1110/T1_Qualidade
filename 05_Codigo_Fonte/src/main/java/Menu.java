/**
 * Menu principal. Usa um laço em vez de recursão (BUG008) e trata falhas do arquivo de dados sem
 * encerrar o sistema (BUG002).
 */
public class Menu {

    public static final String OPCOES = """
            =============================
                        MENU
            =============================
            1- Criar Colaborador
            2- Consultar Colaborador
            3- Relatórios de Folha de Pagamento
            4- Resumo da Folha de Pagamentos
            5- Modificar Colaborador
            6- Mostrar todos Colaboradores
            7- Deletar Colaborador
            0- Sair
            ------------------------------""";

    private final EntradaConsole entrada;
    private final JsonFileManager dados;
    private final UtilsColaborador utilsColaborador;

    public Menu(EntradaConsole entrada, JsonFileManager dados) {
        this.entrada = entrada;
        this.dados = dados;
        this.utilsColaborador = new UtilsColaborador(entrada, dados);
    }

    public void menu() {
        try {
            boolean continuar = true;
            while (continuar) {
                entrada.exibir(OPCOES);
                int opcao = entrada.lerInteiro("Digite o número da sua opção:");
                continuar = executar(opcao);
            }
            entrada.exibir("Sistema encerrado.");
        } catch (EntradaEncerradaException e) {
            entrada.exibir("Fim da entrada. Sistema encerrado.");
        }
    }

    /** Executa a opção escolhida e devolve falso quando o usuário pede para sair. */
    private boolean executar(int opcao) {
        try {
            switch (opcao) {
                case 1:
                    dados.adicionar(utilsColaborador.criarColaborador());
                    entrada.exibir("Colaborador criado com sucesso");
                    break;
                case 2:
                    utilsColaborador.procurarColaborador();
                    break;
                case 3:
                    utilsColaborador.imprimirRelatorio();
                    break;
                case 4:
                    utilsColaborador.imprimirResumo();
                    break;
                case 5:
                    utilsColaborador.modificarColaborador();
                    break;
                case 6:
                    utilsColaborador.imprimirTodosColaboradores();
                    break;
                case 7:
                    utilsColaborador.removerColaborador();
                    break;
                case 0:
                    return false;
                default:
                    entrada.exibir("Opção inválida tente novamente!");
            }
        } catch (PersistenciaException e) {
            entrada.exibir("Erro no arquivo de dados: " + e.getMessage());
            entrada.exibir("Corrija ou remova o arquivo " + dados.getArquivo()
                    + " (faça uma cópia antes) e tente novamente. Nenhum dado foi alterado.");
        }
        return true;
    }
}
