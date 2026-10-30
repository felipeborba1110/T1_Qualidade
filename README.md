# Sistema de Folha de Pagamento

Trabalho Integrador · Unidade Curricular: Gestão e Qualidade de Software

Sistema em **Java** para cadastro, gerenciamento e cálculo da folha de pagamento de colaboradores, com persistência em arquivo JSON e interface de console.

## Funcionalidades

- Cadastro, consulta (por matrícula ou nome), alteração e exclusão de colaboradores
- Cálculo automático de salários por categoria
- Relatório da folha de pagamento e resumo (quantidade, total por categoria e total geral)
- Persistência dos dados em JSON

## Tipos de colaborador

| Tipo | Cálculo |
|---|---|
| Padrão | Salário Final = Salário Base |
| Comissionado | Comissão = Valor das Vendas × Percentual de Comissão<br>Salário Final = Salário Base + Comissão |
| Produção | Produtividade = Quantidade Produzida × Valor por Unidade<br>Salário Final = Salário Base + Produtividade |

## Regras de negócio

- A matrícula (ID) não pode ser duplicada.
- O nome é obrigatório.
- Salário base, valor de vendas, percentual de comissão, quantidade produzida e valor por unidade não podem ser negativos (zero é permitido).
- O salário final é calculado automaticamente.

## Requisitos

- Java 15 ou superior (recomendado JDK 21)
- `json-simple-1.1.1.jar` (incluído em `05_Codigo_Fonte/`)

## Como compilar e executar

O código-fonte fica em `05_Codigo_Fonte/`. Entre nessa pasta antes de rodar os comandos.

**Compilar**

```
cd 05_Codigo_Fonte
javac -cp json-simple-1.1.1.jar -d out Main.java Menu.java Colaborador.java ColaboradorComissionado.java ColaboradorProducao.java JsonFileManager.java UtilsColaborador.java
```

**Executar no Windows**

```
java -cp "out;json-simple-1.1.1.jar" Main
```

**Executar no Linux ou macOS**

```
java -cp "out:json-simple-1.1.1.jar" Main
```

Os dados são gravados em `data.json`, criado na pasta onde o sistema é executado. Um exemplo da estrutura está em `06_Banco_Dados/data.exemplo.json`.

## Como usar

| Opção | Ação |
|---|---|
| 1 | Criar colaborador |
| 2 | Consultar colaborador (por ID ou por nome) |
| 3 | Relatório da folha de pagamento |
| 4 | Resumo da folha de pagamento |
| 5 | Modificar colaborador |
| 6 | Mostrar todos os colaboradores |
| 7 | Deletar colaborador |
| 0 | Sair |

Decimais usam vírgula no Windows em português (exemplo: `0,05` para 5%).

## Testes

**Hoje:** os casos de teste (CT001 a CT021 e REV-01) estão em `04_Testes/Plano_de_Testes_e_Casos_de_Teste.pdf` e são executados manualmente, pelo menu, seguindo os passos e a massa de dados (M1, M2 e M3) do documento.

**Planejado:** testes automatizados com JUnit 5, cobertura com JaCoCo e build com Maven (`mvn test`). O `pom.xml` **ainda não existe** no repositório; a tarefa, o responsável e o prazo estão na seção "Estado da Infraestrutura de Testes" do Plano de Testes.

## Estrutura do projeto

| Pasta | Conteúdo |
|---|---|
| `01_Requisitos` | Documento de Visão e ERS |
| `02_Analise` | Casos de uso e diagramas UML |
| `03_Projeto` | Documento de Arquitetura |
| `04_Testes` | Plano de Testes e Casos de Teste |
| `05_Codigo_Fonte` | Código-fonte e `json-simple-1.1.1.jar` |
| `06_Banco_Dados` | Exemplo da estrutura do `data.json` |
| `07_Executavel` | Reservada para o pacote executável |
| `08_Manuais` | Reservada para os manuais |
| `09_Homologacao` | Reservada para o termo de homologação |
| `10_Qualidade` | PGQ e planejamento do projeto |
| `11_Apresentacao` | Material da apresentação |

## Padrão de commits

Formato `tipo: descrição`, em português, citando o requisito quando houver (exemplo: `fix: valida nome obrigatório (RN002)`). Tipos: `feat`, `fix`, `docs`, `test`, `refactor` e `chore`. Toda mudança entra na `main` por Pull Request revisado por outro integrante.

## Equipe

| Integrante | RA | Papel |
|---|---|---|
| Matheus Danoski Petuco | 1292513125 | Documentação |
| Felipe de Miranda Borba | 1292418436 | Desenvolvimento |
| João Gabriel da Cunha | 1292420048 | Testes |
| João Paulo Teobaldo da Silva | 1292621882 | Testes |
| André Sergeievich Jaeger | 1292624768 | Qualidade e repositório |
