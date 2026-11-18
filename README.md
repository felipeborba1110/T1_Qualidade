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
- Apache Maven 3.8 ou superior para compilar e executar os testes (opcional para apenas usar o sistema)
- `json-simple-1.1.1` (baixada pelo Maven; também incluída em `05_Codigo_Fonte/lib/` para compilação manual)

## Como executar (usuário)

O executável pronto está em `07_Executavel/folha-pagamento.jar` e já inclui a biblioteca JSON.

```
java -jar 07_Executavel/folha-pagamento.jar
```

Os dados são gravados em `data.json`, na pasta onde o comando é executado. Para usar outro arquivo:

```
java -Dfolha.dados=C:\folha\dados.json -jar folha-pagamento.jar
```

Um exemplo da estrutura está em `06_Banco_Dados/data.exemplo.json`.

## Como compilar e testar (desenvolvimento)

O código-fonte segue a estrutura do Maven em `05_Codigo_Fonte/` (`src/main/java` e `src/test/java`).

```
cd 05_Codigo_Fonte
mvn verify
```

O comando compila, executa os 131 testes (unitários, de integração e de aceitação), gera o relatório de
cobertura em `target/site/jacoco/index.html`, verifica a meta de 80% de cobertura de linhas e gera
`target/folha-pagamento.jar`. Para executar só uma categoria: `mvn test -Dgroups=unitario`
(ou `integracao`, `aceitacao`).

**Sem Maven** (compilação manual do sistema):

```
cd 05_Codigo_Fonte
javac -encoding UTF-8 -cp lib/json-simple-1.1.1.jar -d out src/main/java/*.java
java -cp "out;lib/json-simple-1.1.1.jar" Main      (Windows)
java -cp "out:lib/json-simple-1.1.1.jar" Main      (Linux ou macOS)
```

## Como usar

| Opção | Ação |
|---|---|
| 1 | Criar colaborador |
| 2 | Consultar colaborador (por ID ou por nome; 0 volta ao menu) |
| 3 | Relatório da folha de pagamento |
| 4 | Resumo da folha de pagamento |
| 5 | Modificar colaborador |
| 6 | Mostrar todos os colaboradores |
| 7 | Deletar colaborador |
| 0 | Sair |

Valores decimais aceitam vírgula ou ponto (`0,05` ou `0.05` para 5%), em qualquer idioma do sistema operacional.

## Testes e qualidade

| Item | Local |
|---|---|
| Plano e casos de teste (A1) | `04_Testes/Plano_de_Testes_e_Casos_de_Teste.pdf` |
| Código dos testes (JUnit 5) | `05_Codigo_Fonte/src/test/java` |
| Relatório de execução e XML do JUnit | `04_Testes/relatorios/` |
| Relatório de cobertura (JaCoCo) | `04_Testes/relatorios/cobertura_jacoco/index.html` |
| Evidências de aceitação e reteste de defeitos | `04_Testes/evidencias/` |

Resultado da execução de 18/11/2026: 131 testes, 131 aprovados; cobertura de linhas de 98,8% e de ramos de 95,8%.

## Estrutura do projeto

| Pasta | Conteúdo |
|---|---|
| `01_Requisitos` | Documento de Visão e ERS |
| `02_Analise` | Casos de uso e diagramas UML |
| `03_Projeto` | Documento de Arquitetura |
| `04_Testes` | Plano e casos de teste, relatórios de execução e cobertura, evidências |
| `05_Codigo_Fonte` | Projeto Maven (`pom.xml`, `src/main/java`, `src/test/java`) e `lib/` |
| `06_Banco_Dados` | Exemplo da estrutura do `data.json` |
| `07_Executavel` | `folha-pagamento.jar` executável |
| `08_Manuais` | Manual do usuário e manual técnico |
| `09_Homologacao` | Termo de homologação |
| `10_Qualidade` | PGQ, planejamento, registro de defeitos, métricas, revisão técnica e relatório final |
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
