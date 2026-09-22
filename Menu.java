
import org.json.simple.JSONObject;

import java.util.Scanner;

public class Menu {

    private JsonFileManager export = new JsonFileManager();
    private JSONObject json = new JSONObject();
    private MenuUtils menuUtils = new MenuUtils();
    private Colaborador colaborador;
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
                6- Deletar Colaborador
                0- Sair
                ------------------------------
        """);

        System.out.println("Digite o número da sua opção:");
        int option = input.nextInt();

        switch(option){
            case 1:
                colaborador = menuUtils.criarColaborador();
                json.put("id", colaborador.getId());
                json.put("name", colaborador.getName());
                json.put("type", colaborador.getType());
                json.put("baseSalary", colaborador.getBaseSalary());
                json.put("finalSalary", colaborador.getFinalSalary());
                if (colaborador instanceof ColaboradorComissionado comissionado) {
                    json.put("commission", comissionado.getCommission());
                    json.put("productValue", comissionado.getProductValue());
                }
                if (colaborador instanceof ColaboradorProducao producao) {
                    json.put("prodQuant", producao.getProdQuant());
                    json.put("unitValue", producao.getUnitValue());
                }
                export.add(json);
                break;
            case 2:
                break;
            case 3:
                break;
            case 4:
                break;
            case 5:
                break;
            case 6:
                break;
            case 0:
                break;
            default:
                System.out.println("Opção inválida tente novamente!");
                menu();
        }
    }
}
