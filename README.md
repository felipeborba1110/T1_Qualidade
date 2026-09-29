# Sistema de Folha de Pagamento
Sistema desenvolvido em **Java** para cadastro, gerenciamento e cálculo da folha de pagamento de colaboradores.

## Funcionalidades

- Cadastro de colaboradores
- Consulta por ID ou nome
- Alteração de colaboradores
- Exclusão de colaboradores
- Cálculo automático de salários
- Relatório da folha de pagamento
- Resumo da folha de pagamento
- Persistência dos dados em JSON

## Tipos de colaboradores

### Colaborador Padrão

Recebe apenas o salário base.

**Salário Final = Salário Base**

### Colaborador Comissionado

Recebe o salário base mais uma comissão sobre as vendas.

**Comissão = Valor das Vendas × Percentual de Comissão**

**Salário Final = Salário Base + Comissão**

### Colaborador por Produção

Recebe o salário base mais o valor referente à produção.

**Produtividade = Quantidade Produzida × Valor por Unidade**

**Salário Final = Salário Base + Produtividade**

## Regras de negócio

- ID/matrícula não pode ser duplicado.
- Nome é obrigatório.
- Salário base não pode ser negativo.
- Valor de vendas não pode ser negativo.
- Comissão não pode ser negativa.
- Quantidade produzida não pode ser negativa.
- Valor por unidade não pode ser negativo.
- O salário final é calculado automaticamente.

## Tecnologias

- Java
- Programação Orientada a Objetos (POO)
- JSON
- JSON.simple
