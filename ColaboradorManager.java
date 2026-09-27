import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ColaboradorManager {
    private final Scanner inputStr = new Scanner(System.in);
    private final Scanner inputNum = new Scanner(System.in);
    public Colaborador criarColaborador(){
        System.out.println("------ Adicionando colaborador ------");

        int id = validadorId();

        System.out.println("Insira o nome:");
        String name = inputStr.nextLine();

        System.out.println("Insira o salário base:");
        double baseSalary = inputNum.nextDouble();

        String type = configuradorTipo();

        double finalSalary;

        switch (type){
            case "padrao":
                finalSalary = baseSalary;
                return new Colaborador(id,name,type,baseSalary,finalSalary);
            case "comissionado":
                System.out.println("Insira o valor de venda dos produtos:");
                float productValue = inputNum.nextFloat();

                System.out.println("Insira o percentual de comissão: (0,50 = 50%)");
                float commission = (inputNum.nextFloat()+1);

                finalSalary = baseSalary + (productValue * commission);

                return new ColaboradorComissionado(id,name,type,baseSalary,finalSalary,commission,productValue);
            case "producao":
                System.out.println("Insira a quantidade produzida:");
                int prodQuant = inputNum.nextInt();

                System.out.println("Insira o valor de cada unidade:");
                float unitValue = inputNum.nextFloat();

                finalSalary = baseSalary + (prodQuant * unitValue);

                return new ColaboradorProducao(id,name,type,baseSalary,finalSalary,prodQuant,unitValue);
            default:
                System.out.println("Erro ao criar colaborador, tente novamente! *Campos incorretos*");
                return criarColaborador();
        }
    }

    private String configuradorTipo(){
        System.out.println("Insira o tipo pelo número:");
        System.out.println("""
                1- Colaborador Padrão
                2- Colaborador Comissionado
                3- Colaborador Produção""");
        int type = inputNum.nextInt();

        switch (type){
            case 1:
                return "padrao";
            case 2:
                return "comissionado";
            case 3:
                return "producao";
            default:
                return configuradorTipo();
        }
    }

    private int validadorId(){
        System.out.println("Insira o id:");
        int id = inputNum.nextInt();

        if(procurarID(id) != null){
            System.out.println("Colaborador com este id já existe, tente novamente!");
            return validadorId();
        } else {
            return id;
        }
    }

    public void procurarColaborador(){
        System.out.println("Como deseja procurar por colaborador:");
        System.out.println("1- Por ID");
        System.out.println("2- Por Nome");
        int option = inputNum.nextInt();

        if (option == 1){
            System.out.println("Insira o ID do Colaborador:");
            int id = inputNum.nextInt();

            if (procurarID(id) == null){
                System.out.println("Nenhum colaborador com este id, tente novamente!");
                procurarColaborador();
            } else {
                System.out.println("------ Colaborador Encontrado -----");
                imprimirColaborador(procurarID(id));
            }
        } else if(option == 2) {
            JsonFileManager json = new JsonFileManager();
            List<JSONObject> colaboradoresEncontrados = new ArrayList<>();

            System.out.println("Insira o Nome do Colaborador:");
            String nome = inputStr.nextLine();

            //passa por todas as entradas de data.json e adiciona em um array os que tiverem o mesmo nome da busca
            for(JSONObject colaborador : json.listaColaboradores()){
                String nomeColaborador = (String) colaborador.get("name");

                if (nome.equalsIgnoreCase(nomeColaborador)){
                    colaboradoresEncontrados.add(colaborador);
                }
            }

            //verifica se há colaboradores com o nome e imprime se tiver
            if (colaboradoresEncontrados.isEmpty()){
                System.out.println("Nenhum colaborador encontrado!");
                procurarColaborador();
            } else {
                System.out.println("------ Colaborador Encontrado -----");
                for(JSONObject colaboradorEncontrado : colaboradoresEncontrados){
                    imprimirColaborador(colaboradorEncontrado);
                }
            }

        } else {
            System.out.println("Opção inválida tente novamente!");
            procurarColaborador();
        }
    }

    private JSONObject procurarID(int id){
        JsonFileManager json = new JsonFileManager();
        JSONArray data = json.read();
        JSONObject resultadoProcura = null;

        //passa por todas as entradas de data.json e retorna o que tiver o mesmo id
        for(Object obj : data){
            JSONObject colaborador = (JSONObject) obj;

            int idColaborador = ((Number) colaborador.get("id")).intValue();
            if (id == idColaborador){
                resultadoProcura = colaborador;
            }
        }

        return resultadoProcura;
    }

    private void imprimirColaborador(JSONObject colaborador){
        if (((String)colaborador.get("type")).equalsIgnoreCase("padrao")){
            System.out.printf("""
                    ID: %s
                    Nome: %s
                    Salário Base: R$ %.02f
                    Tipo: %s
                    Salário Final: R$ %.02f                    
                    """,colaborador.get("id"),colaborador.get("name"),((Number)colaborador.get("baseSalary")).doubleValue(),colaborador.get("type"),((Number)colaborador.get("finalSalary")).doubleValue());
        } else if (((String)colaborador.get("type")).equalsIgnoreCase("comissionado")){
            System.out.printf("""
                    ID: %s
                    Nome: %s
                    Salário Base: R$ %.02f
                    Tipo: %s
                    Salário Final: R$ %.02f
                    Valor do produto: R$ %.02f
                    Comissão: %.0f%%                    
                    """,colaborador.get("id"),colaborador.get("name"),((Number)colaborador.get("baseSalary")).doubleValue(),colaborador.get("type"),((Number)colaborador.get("finalSalary")).doubleValue(),((Number)colaborador.get("productValue")).floatValue(),((((Number)colaborador.get("commission")).floatValue()-1)*100));
        } else {
            System.out.printf("""
                    ID: %s
                    Nome: %s
                    Salário Base: R$ %.02f
                    Tipo: %s
                    Salário Final: R$ %.02f
                    Valor da Unidade: R$ %.02f
                    Quantidade de Unidades: %s                    
                    """,colaborador.get("id"),colaborador.get("name"),((Number)colaborador.get("baseSalary")).doubleValue(),colaborador.get("type"),((Number)colaborador.get("finalSalary")).doubleValue(),((Number)colaborador.get("unitValue")).floatValue(),colaborador.get("prodQuant"));
        }
        System.out.println("------------------------------");
    }
    public void imprimirTodosColaboradores(){
        JsonFileManager json = new JsonFileManager();
        System.out.println("*** Lista de Colaboradores ***");
        for (JSONObject colaborador : json.listaColaboradores()){
            imprimirColaborador(colaborador);
        }
    }

    public void imprimirRelatorio(){
        JsonFileManager json = new JsonFileManager();
        System.out.println("*** Relatório de Folha de Pagamentos ***");
        for (JSONObject colaborador : json.listaColaboradores()){
            System.out.printf("""
                    ID: %s
                    Nome: %s
                    Tipo: %s
                    Salário Final: R$ %.02f
                    ------------------------------                    
                    """,colaborador.get("id"),colaborador.get("name"),colaborador.get("type"),((Number)colaborador.get("finalSalary")).doubleValue());
        }
    }
}
