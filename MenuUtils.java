import java.util.Scanner;

public class MenuUtils {
    Scanner inputStr = new Scanner(System.in);
    Scanner inputNum = new Scanner(System.in);
    public Colaborador criarColaborador(){
        System.out.println("------ Adicionando colaborador ------");

        System.out.println("Insira o id:");
        int id = inputNum.nextInt();

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
                double productValue = inputNum.nextDouble();
                //Não funciona
                System.out.println("Insira o percentual de comissão: (0.50 = 50%)");
                double commission = inputNum.nextDouble();

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
                3- Colaborador Produção
                """);
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
}
