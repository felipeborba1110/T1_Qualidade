import java.util.Scanner;

public class Menu {
    private final JsonFileManager data = new JsonFileManager();
    private final UtilsColaborador utilsColaborador = new UtilsColaborador();
    private final Scanner input = new Scanner(System.in);

    public void menu(){
        System.out.println("""
        =============================
                    MENU
        =============================
        1- Criar Colaborador
        2- Consultar Colaborador
        3- Relatórios de Folha de Pagamento
        4- Resumo da Folha de Pagamentos
        5- Modificar Colaborador
        6- Mostrar todos Colabordores
        7- Deletar Colaborador
        0- Sair
        ------------------------------""");

        System.out.println("Digite o número da sua opção:");
        int option = input.nextInt();

        switch(option){
            case 1:
                data.add(utilsColaborador.criarColaborador().toJSON());
                System.out.println("Colaborador criado com sucesso");
                menu();
                break;
            case 2:
                utilsColaborador.procurarColaborador();
                menu();
                break;
            case 3:
                utilsColaborador.imprimirRelatorio();
                menu();
                break;
            case 4:
                utilsColaborador.imprimirResumo();
                menu();
                break;
            case 5:
                utilsColaborador.modificarColaborador();
                break;
            case 6:
                utilsColaborador.imprimirTodosColaboradores();
                menu();
                break;
            case 7:
                utilsColaborador.removerColaborador();
                menu();
                break;
            case 0:
                break;
            default:
                System.out.println("Opção inválida tente novamente!");
                menu();
        }
    }
}
