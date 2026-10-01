import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class UtilsColaborador {

    private final Scanner inputStr = new Scanner(System.in);
    private final Scanner inputNum = new Scanner(System.in);

    public Colaborador criarColaborador() {
        System.out.println("------ Adicionando colaborador ------");

        int id = validadorId();

        String name = lerNomeObrigatorio("Insira o nome:");

        double baseSalary = lerDoubleNaoNegativo("Insira o salário base:");

        String type = configuradorTipo();

        double finalSalary;

        switch (type) {
            case "padrao":
                finalSalary = baseSalary;
                return new Colaborador(id, name, type, baseSalary, finalSalary);
            case "comissionado":
                float productValue = lerFloatNaoNegativo("Insira o valor de venda dos produtos:");
                float commission = lerFloatNaoNegativo("Insira o percentual de comissão: (0,50 = 50%)");

                finalSalary = baseSalary + (productValue * commission);

                return new ColaboradorComissionado(id, name, type, baseSalary, finalSalary, commission, productValue);
            case "producao":
                int prodQuant = lerIntNaoNegativo("Insira a quantidade produzida:");
                float unitValue = lerFloatNaoNegativo("Insira o valor de cada unidade:");

                finalSalary = baseSalary + (prodQuant * unitValue);

                return new ColaboradorProducao(id, name, type, baseSalary, finalSalary, prodQuant,unitValue);
            default:
                System.out.println("Erro ao criar colaborador, tente novamente! *Campos incorretos*");
                return criarColaborador();
        }
    }
        private String lerNomeObrigatorio(String mensagem) {
        String nome;
        do {
            System.out.println(mensagem);
            nome = inputStr.nextLine().trim();
            if (nome.isEmpty()) {
                System.out.println("O nome é obrigatório. Tente novamente!");
            }
        } while (nome.isEmpty());
        return nome;
    }
    private double lerDoubleNaoNegativo(String mensagem) {
        double valor;
        do {
            System.out.println(mensagem);
            valor = inputNum.nextDouble();
            if (valor < 0) {
                System.out.println("O valor não pode ser negativo. Tente novamente!");
            }
        } while (valor < 0);
        return valor;
    }
    private float lerFloatNaoNegativo(String mensagem) {
        float valor;
        do {
            System.out.println(mensagem);
            valor = inputNum.nextFloat();
            if (valor < 0) {
                System.out.println("O valor não pode ser negativo. Tente novamente!");
            }
        } while (valor < 0);
        return valor;
    }
    private int lerIntNaoNegativo(String mensagem) {
        int valor;
        do {
            System.out.println(mensagem);
            valor = inputNum.nextInt();
            if (valor < 0) {
                System.out.println("O valor não pode ser negativo. Tente novamente!");
            }
        } while (valor < 0);
        return valor;
    }
    private String configuradorTipo() {
        System.out.println("Insira o tipo pelo número:");
        System.out.println("""
                1- Colaborador Padrão
                2- Colaborador Comissionado
                3- Colaborador Produção""");

        int type = inputNum.nextInt();

        switch (type) {
            case 1:
                return "padrao";
            case 2:
                return "comissionado";
            case 3:
                return "producao";
            default:
                System.out.println("Opção inválida! Tente novamente.");
                return configuradorTipo();
        }
    }

    private int validadorId() {
        System.out.println("Insira o id:");
        int id = inputNum.nextInt();

        if (procurarID(id) != null) {
            System.out.println("Colaborador com este id já existe, tente novamente!");

            return validadorId();
        } else {
            return id;
        }
    }

    public void procurarColaborador() {
        System.out.println("Como deseja procurar por colaborador:");
        System.out.println("1- Por ID");
        System.out.println("2- Por Nome");

        int option = inputNum.nextInt();

        if (option == 1) {
            System.out.println("Insira o ID do Colaborador:");
            int id = inputNum.nextInt();
            if (procurarID(id) == null) {
                System.out.println("Nenhum colaborador com este id, tente novamente!");

                procurarColaborador();
            } else {
                System.out.println("------ Colaborador Encontrado -----");

                imprimirColaborador(procurarID(id));
            }
        } else if (option == 2) {
            JsonFileManager json = new JsonFileManager();
            List<JSONObject> colaboradoresEncontrados = new ArrayList<>();

            System.out.println("Insira o Nome do Colaborador:");
            String nome = inputStr.nextLine();

            // Passa por todas as entradas do data.json
            // e adiciona os colaboradores com o mesmo nome.
            for (JSONObject colaborador : json.listaColaboradores()) {
                String nomeColaborador = (String) colaborador.get("name");

                if (nome.equalsIgnoreCase(nomeColaborador)) {
                    colaboradoresEncontrados.add(colaborador);
                }
            }

            // Verifica se encontrou algum colaborador.
            if (colaboradoresEncontrados.isEmpty()) {
                System.out.println("Nenhum colaborador encontrado!");

                procurarColaborador();
            } else {
                System.out.println("------ Colaborador Encontrado -----");

                for (JSONObject colaboradorEncontrado : colaboradoresEncontrados) {
                    imprimirColaborador(colaboradorEncontrado);
                }
            }
        } else {
            System.out.println("Opção inválida tente novamente!");

            procurarColaborador();
        }
    }

    private JSONObject procurarID(int id) {
        JsonFileManager json = new JsonFileManager();
        JSONArray data = json.read();
        JSONObject resultadoProcura = null;

        // Passa por todas as entradas do data.json
        // e retorna o colaborador com o ID informado.
        for (Object obj : data) {
            JSONObject colaborador = (JSONObject) obj;

            int idColaborador = ((Number) colaborador.get("id")).intValue();

            if (id == idColaborador) {
                resultadoProcura = colaborador;
            }
        }

        return resultadoProcura;
    }

    private void imprimirColaborador(JSONObject colaborador) {

        if (((String) colaborador.get("type")).equalsIgnoreCase("padrao")) {
            System.out.printf("""
                    ID: %s
                    Nome: %s
                    Salário Base: R$ %,.2f
                    Tipo: %s
                    Salário Final: R$ %,.2f
                    """, colaborador.get("id"), colaborador.get("name"), ((Number) colaborador.get("baseSalary")).doubleValue(), colaborador.get("type"), ((Number) colaborador.get("finalSalary")).doubleValue());

        } else if (((String) colaborador.get("type")).equalsIgnoreCase("comissionado")) {
            System.out.printf("""
                    ID: %s
                    Nome: %s
                    Salário Base: R$ %,.2f
                    Tipo: %s
                    Salário Final: R$ %,.2f
                    Valor do produto: R$ %,.2f
                    Comissão: %.0f%%
                    """, colaborador.get("id"), colaborador.get("name"), ((Number) colaborador.get("baseSalary")).doubleValue(), colaborador.get("type"), ((Number) colaborador.get("finalSalary")).doubleValue(), ((Number) colaborador.get("productValue")).floatValue(), ((Number) colaborador.get("commission")).floatValue() * 100);
        } else {
            System.out.printf("""
                    ID: %s
                    Nome: %s
                    Salário Base: R$ %,.2f
                    Tipo: %s
                    Salário Final: R$ %,.2f
                    Valor da Unidade: R$ %,.2f
                    Quantidade de Unidades: %s
                    """, colaborador.get("id"), colaborador.get("name"), ((Number) colaborador.get("baseSalary")).doubleValue(), colaborador.get("type"), ((Number) colaborador.get("finalSalary")).doubleValue(), ((Number) colaborador.get("unitValue")).floatValue(), colaborador.get("prodQuant"));
        }

        System.out.println("------------------------------");
    }
    public void imprimirTodosColaboradores() {
        JsonFileManager json = new JsonFileManager();
        System.out.println("*** Lista de Colaboradores ***");
        for (JSONObject colaborador : json.listaColaboradores()) {
            imprimirColaborador(colaborador);
        }
    }

    public void imprimirRelatorio() {
        JsonFileManager json = new JsonFileManager();
        System.out.println("*** Relatório de Folha de Pagamentos ***");

        for (JSONObject colaborador : json.listaColaboradores()) {
            double baseSalary = ((Number) colaborador.get("baseSalary")).doubleValue();
            double finalSalary = ((Number) colaborador.get("finalSalary")).doubleValue();

            System.out.printf("""
                    ID: %s
                    Nome: %s
                    Tipo: %s
                    Salário Base: R$ %,.2f
                    Adicionais: R$ %,.2f
                    Salário Final: R$ %,.2f
                    ------------------------------
                    """, colaborador.get("id"), colaborador.get("name"), colaborador.get("type"), baseSalary, finalSalary - baseSalary, finalSalary);
        }
    }

    public void imprimirResumo() {
        JsonFileManager json = new JsonFileManager();

        double pagamentosTotal = 0;
        double pagamentosTotalPadrao = 0;
        double pagamentosTotalComissionado = 0;
        double pagamentosTotalProducao = 0;

        System.out.println("*** Resumo da Folha de Pagamentos ***");

        for (JSONObject colaborador : json.listaColaboradores()) {
            double finalSalary = ((Number) colaborador.get("finalSalary")).doubleValue();
            String type = (String) colaborador.get("type");

            if (type.equalsIgnoreCase("padrao")) {
                pagamentosTotalPadrao += finalSalary;
            } else if (type.equalsIgnoreCase("producao")) {
                pagamentosTotalProducao += finalSalary;
            } else if (type.equalsIgnoreCase("comissionado")) {
                pagamentosTotalComissionado += finalSalary;
            }

            pagamentosTotal += finalSalary;
        }

        System.out.println("Quantidade de colaboradores: " + json.listaColaboradores().size());
        System.out.printf("Total de Pagamento para Colaboradores Padrão: R$ %,.2f\n", pagamentosTotalPadrao);
        System.out.printf("Total de Pagamento para Colaboradores Comissionado: R$ %,.2f\n", pagamentosTotalComissionado);
        System.out.printf("Total de Pagamento para Colaboradores de Produção: R$ %,.2f\n", pagamentosTotalProducao);
        System.out.printf("Total da Folha de Pagamentos: R$ %,.2f\n", pagamentosTotal);
        System.out.println("------------------------------");
    }

    public void modificarColaborador() {
        JsonFileManager json = new JsonFileManager();
        System.out.println("Insira o ID do Colaborador:");
        int id = inputNum.nextInt();

        JSONObject colaborador = procurarID(id);
        if (colaborador == null) {
            System.out.println("Nenhum colaborador com este id, tente novamente!");
            return;
        }

        System.out.println("------ Colaborador Encontrado -----");
        imprimirColaborador(colaborador);
        System.out.println("Qual informação deseja alterar?");

        String type = (String) colaborador.get("type");

        switch (type) {
            case "padrao":
                System.out.println("""
                        1- Nome
                        2- Salário Base
                        3- Tipo""");
                break;
            case "comissionado":
                System.out.println("""
                        1- Nome
                        2- Salário Base
                        3- Tipo
                        4- Valor do Produto
                        5- Comissão""");
                break;
            case "producao":
                System.out.println("""
                        1- Nome
                        2- Salário Base
                        3- Tipo
                        4- Valor da Unidade
                        5- Quantidade de Unidades""");
                break;
            default:
                System.out.println("ERRO com o tipo do colaborador!");
        }

        System.out.print("Opção:");
        int option = inputNum.nextInt();

        switch (option) {
            // Nome
            case 1:
                String nome = lerNomeObrigatorio("Insira o novo nome:");

                colaborador.put("name", nome);
                break;
            // Salário base
            case 2:
                double salario = lerDoubleNaoNegativo("Insira o novo salário base:");
                colaborador.put("baseSalary", salario);

                recalcularSalario(colaborador);
                break;
            // Tipo
            case 3:
                System.out.println("Selecione o novo tipo:");

                String novoTipo = configuradorTipo();

                colaborador.remove("productValue");
                colaborador.remove("commission");
                colaborador.remove("unitValue");
                colaborador.remove("prodQuant");

                colaborador.put("type", novoTipo);

                if (novoTipo.equals("padrao")) {
                    recalcularSalario(colaborador);
                } else if (novoTipo.equals("comissionado")) {
                    float productValue = lerFloatNaoNegativo("Insira o valor de venda dos produtos:");
                    float commission = lerFloatNaoNegativo("Insira o percentual de comissão: (0,50 = 50%)");

                    colaborador.put("productValue", productValue);
                    colaborador.put("commission", commission);

                    recalcularSalario(colaborador);
                } else if (novoTipo.equals("producao")) {
                    int prodQuant = lerIntNaoNegativo("Insira a quantidade produzida:");

                    float unitValue = lerFloatNaoNegativo("Insira o valor de cada unidade:");

                    colaborador.put("prodQuant", prodQuant);
                    colaborador.put("unitValue", unitValue);

                    recalcularSalario(colaborador);
                }
                break;
            // Produto ou Unidade
            case 4:
                if (type.equals("comissionado")) {
                    float productValue = lerFloatNaoNegativo("Insira o novo valor de venda dos produtos:");

                    colaborador.put("productValue", productValue);

                    recalcularSalario(colaborador);
                } else if (type.equals("producao")) {
                    float unitValue = lerFloatNaoNegativo("Insira o novo valor da unidade:");

                    colaborador.put("unitValue", unitValue);

                    recalcularSalario(colaborador);
                }
                break;
            // Comissão ou Quantidade
            case 5:
                if (type.equals("comissionado")) {
                    float commission = lerFloatNaoNegativo("Insira a nova comissão: (0,50 = 50%)");

                    colaborador.put("commission", commission);

                    recalcularSalario(colaborador);
                } else if (type.equals("producao")) {
                    int prodQuant = lerIntNaoNegativo("Insira a nova quantidade produzida:");

                    colaborador.put("prodQuant", prodQuant);

                    recalcularSalario(colaborador);
                }
                break;
            default:
                System.out.println("Opção inválida!");
                modificarColaborador();
                return;
        }

        json.update(colaborador);

        System.out.println("Colaborador modificado com sucesso!");
        System.out.println("------ Dados atualizados -----");
        imprimirColaborador(colaborador);
    }

    private void recalcularSalario(JSONObject colaborador) {
        String type = (String) colaborador.get("type");
        double baseSalary = ((Number) colaborador.get("baseSalary")).doubleValue();
        double finalSalary;

        switch (type) {
            case "padrao":
                finalSalary = baseSalary;
                break;
            case "comissionado":
                double productValue = ((Number) colaborador.get("productValue")).doubleValue();
                double commission = ((Number) colaborador.get("commission")).doubleValue();
                finalSalary = baseSalary + (productValue * commission);
                break;
            case "producao":
                int prodQuant = ((Number) colaborador.get("prodQuant")).intValue();
                double unitValue = ((Number) colaborador.get("unitValue")).doubleValue();
                finalSalary = baseSalary + (prodQuant * unitValue);
                break;
            default:
                System.out.println("Tipo inválido!");
                return;
        }

        colaborador.put("finalSalary", finalSalary);
    }

    public void removerColaborador() {
        JsonFileManager json = new JsonFileManager();

        System.out.println("Insira o ID do Colaborador:");
        int id = inputNum.nextInt();

        if (procurarID(id) == null) {
            System.out.println("Nenhum colaborador com este id, tente novamente!");
        } else {
            System.out.println("------ Colaborador Encontrado -----");
            JSONObject colaborador = procurarID(id);
            imprimirColaborador(colaborador);

            System.out.println("Realmente deseja excluir este colaborador? <S/N>");
            String resposta = inputStr.nextLine();

            if (resposta.equalsIgnoreCase("S")) {
                json.remove(colaborador);
                System.out.println("Colaborador removido com sucesso");
            } else {
                System.out.println("Retornando ao menu...");
            }
        }
    }
}
