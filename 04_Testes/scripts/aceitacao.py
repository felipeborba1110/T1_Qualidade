"""Executa os cenários de aceitação e o reteste dos defeitos sobre o executável final
(07_Executavel/folha-pagamento.jar), como um usuário digitando no terminal, e grava a sessão completa.
Uso: python3 aceitacao.py <jar> <pasta_evidencias> <json_resultados>"""
import datetime, json, os, re, shutil, sys, pexpect

JAR, PASTA, SAIDA_JSON = sys.argv[1], sys.argv[2], sys.argv[3]
ENV = {k: v for k, v in os.environ.items() if k not in ("JAVA_TOOL_OPTIONS", "_JAVA_OPTIONS")}
ENV["LANG"] = "C.UTF-8"
M = "opção:"
resultados = []


def cad(i, nome, base, tipo, *extra):
    passos = [(M, "1"), ("id:", i), ("nome:", nome), ("base:", base), ("Produção", tipo)]
    prompts = {"2": ["produtos:", r"50%\)"], "3": ["produzida:", "unidade:"]}.get(tipo, [])
    return passos + list(zip(prompts, extra))


M1 = cad("1", "Ana", "3000", "1")
M2 = cad("2", "Bruno", "2000", "2", "10000", "0,05")
M3 = cad("3", "Carla", "1800", "3", "100", "2,50")


def cenario(codigo, pasta, titulo, requisito, esperado, sessoes, verificar, preparo=None):
    """sessoes: lista de listas de passos (cada lista = uma execução do sistema, para testar reinício)."""
    d = f"/tmp/ac_{codigo}"
    shutil.rmtree(d, ignore_errors=True); os.makedirs(d)
    if preparo:
        preparo(d)
    os.makedirs(f"{PASTA}/{pasta}", exist_ok=True)
    caminho = f"{PASTA}/{pasta}/{codigo}.txt"
    texto_total = ""
    with open(caminho, "w", encoding="utf-8") as log:
        log.write(f"Cenário: {codigo} - {titulo}\nRequisito: {requisito}\nResultado esperado: {esperado}\n")
        log.write(f"Executável: 07_Executavel/folha-pagamento.jar | Data: {datetime.date.today():%d/%m/%Y}\n")
        log.write("Comando: java -jar folha-pagamento.jar (data.json limpo antes do cenário)\n")
        for n, passos in enumerate(sessoes, 1):
            log.write(f"\n=============== EXECUÇÃO {n} DO SISTEMA ===============\n")
            p = pexpect.spawn("java", ["-jar", JAR], cwd=d, env=ENV, encoding="utf-8", timeout=8)
            p.logfile_read = log
            for espera, envio in passos:
                p.expect(espera)
                p.sendline(envio)
            p.expect(pexpect.EOF)
            texto_total += p.before if isinstance(p.before, str) else ""
            p.close()
        dados = open(f"{d}/data.json", encoding="utf-8").read() if os.path.exists(f"{d}/data.json") else "(inexistente)"
        log.write(f"\n=============== CONTEÚDO FINAL DO data.json ===============\n{dados}\n")
    conteudo = open(caminho, encoding="utf-8").read()
    ok, obtido = verificar(conteudo, dados)
    if not ok and not obtido.startswith('Não exibido'):
        obtido = 'Divergência em relação ao esperado (ver sessão registrada)'
    with open(caminho, "a", encoding="utf-8") as log:
        log.write(f"\nResultado obtido: {obtido}\nSituação: {'APROVADO' if ok else 'REPROVADO'}\n")
    resultados.append({"codigo": codigo, "grupo": pasta, "titulo": titulo, "requisito": requisito,
                       "esperado": esperado, "obtido": obtido, "situacao": "Aprovado" if ok else "Reprovado"})
    print(f"{codigo}: {'APROVADO' if ok else 'REPROVADO'} - {obtido}")


def contem(*trechos):
    def v(c, _):
        faltando = [t for t in trechos if t not in c]
        return (not faltando, "Exibido conforme esperado: " + "; ".join(trechos) if not faltando
                else "Não exibido: " + "; ".join(faltando))
    return v


A = "aceitacao"
cenario("AC-RF001", A, "Cadastrar colaborador padrão", "RF001",
        "Colaborador gravado em data.json e exibido na consulta com os mesmos dados",
        [cad("1", "Ana", "2000", "1") + [(M, "2"), ("Nome", "1"), ("Colaborador:", "1"), (M, "0")]],
        lambda c, d: ('"name":"Ana"' in d and "Nome: Ana\nSalário Base: R$ 2.000,00\nTipo: Padrão" in c,
                      "Consulta exibiu ID 1, Ana, R$ 2.000,00, Padrão; data.json contém o registro"))
cenario("AC-RF002", A, "Salário do colaborador padrão", "RF002", "Salário final R$ 3.000,00, sem adicionais",
        [M1 + [(M, "3"), (M, "0")]], contem("Adicionais: R$ 0,00", "Salário Final: R$ 3.000,00"))
cenario("AC-RF003", A, "Salário do colaborador comissionado", "RF003",
        "Comissão de R$ 500,00 e salário final de R$ 2.500,00",
        [M2 + [(M, "3"), (M, "0")]], contem("Adicionais: R$ 500,00", "Salário Final: R$ 2.500,00"))
cenario("AC-RF004", A, "Salário do colaborador por produção", "RF004",
        "Produtividade de R$ 250,00 e salário final de R$ 2.050,00",
        [M3 + [(M, "3"), (M, "0")]], contem("Adicionais: R$ 250,00", "Salário Final: R$ 2.050,00"))
cenario("AC-RF005", A, "Consultar por matrícula, nome, listagem e inexistente", "RF005 / CT005",
        "Dados exibidos; inexistente informa mensagem e não encerra",
        [M1 + M2 + M3 + [(M, "2"), ("Nome", "1"), ("Colaborador:", "2"), (M, "2"), ("Nome", "2"),
                         ("Colaborador:", "Bruno"), (M, "6"), (M, "2"), ("Nome", "1"), ("Colaborador:", "99"),
                         ("Nome", "0"), (M, "0")]],
        contem("Nome: Bruno", "Nome: Ana", "Nome: Carla", "Nenhum colaborador com este id, tente novamente!",
               "Sistema encerrado."))
cenario("AC-RF006", A, "Recalcular salário após alteração", "RF006 / CT006",
        "Salário calculado R$ 2.500,00 e recalculado para R$ 3.000,00",
        [M2 + [(M, "5"), ("Colaborador:", "2"), ("Opção:", "4"), ("produtos:", "20000"), (M, "0")]],
        contem("Salário Final: R$ 2.500,00", "Colaborador modificado com sucesso!", "Salário Final: R$ 3.000,00"))
cenario("AC-RF007", A, "Folha de pagamento com os três tipos", "RF007 / CT007",
        "Folha lista Ana R$ 3.000,00; Bruno 2.000 + 500 = 2.500; Carla 1.800 + 250 = 2.050",
        [M1 + M2 + M3 + [(M, "3"), (M, "0")]],
        contem("Nome: Ana\nTipo: Padrão\nSalário Base: R$ 3.000,00\nAdicionais: R$ 0,00\nSalário Final: R$ 3.000,00",
               "Nome: Bruno\nTipo: Comissionado\nSalário Base: R$ 2.000,00\nAdicionais: R$ 500,00\nSalário Final: R$ 2.500,00",
               "Nome: Carla\nTipo: Produção\nSalário Base: R$ 1.800,00\nAdicionais: R$ 250,00\nSalário Final: R$ 2.050,00"))
cenario("AC-RF008", A, "Resumo da folha", "RF008 / CT008",
        "Quantidade 3; totais por categoria; total geral R$ 7.550,00",
        [M1 + M2 + M3 + [(M, "4"), (M, "0")]],
        contem("Quantidade de colaboradores: 3", "Colaborador Padrão: R$ 3.000,00",
               "Colaborador Comissionado: R$ 2.500,00", "Colaborador Produção: R$ 2.050,00",
               "Total da Folha de Pagamentos: R$ 7.550,00"))
cenario("AC-RF009", A, "Alterar, excluir (S e N) e persistir", "RF009 / CT009 / CT010",
        "Nome alterado; id 3 removido com S; id 2 mantido com N; mudanças mantidas após reiniciar",
        [M1 + M2 + M3 + [(M, "5"), ("Colaborador:", "1"), ("Opção:", "1"), ("nome:", "Ana Maria"),
                         (M, "7"), ("Colaborador:", "3"), ("<S/N>", "S"), (M, "7"), ("Colaborador:", "2"),
                         ("<S/N>", "N"), (M, "0")],
         [(M, "6"), (M, "0")]],
        lambda c, d: (c.split("EXECUÇÃO 2")[1].count("Nome: ") == 2 and "Nome: Ana Maria" in c.split("EXECUÇÃO 2")[1]
                      and "Colaborador removido com sucesso" in c and "Retornando ao menu..." in c,
                      "Após reiniciar a listagem exibe Ana Maria e Bruno; Carla removida; Bruno mantido com N"))
cenario("AC-RN001", A, "Matrícula repetida", "RN001 / CT011",
        "Cadastro recusado com mensagem e matrícula solicitada novamente",
        [M1 + [(M, "1"), ("id:", "1"), ("existe", "4"), ("nome:", "Davi"), ("base:", "1000"), ("Produção", "1"), (M, "6"), (M, "0")]],
        contem("Colaborador com este id já existe, tente novamente!", "ID: 4"))
cenario("AC-RN002", A, "Nome obrigatório no cadastro e na alteração", "RN002 / CT012",
        "Entrada recusada com mensagem de nome obrigatório e solicitada novamente",
        [M1 + [(M, "1"), ("id:", "5"), ("nome:", ""), ("Tente novamente!", "Davi"), ("base:", "1000"), ("Produção", "1"),
               (M, "5"), ("Colaborador:", "1"), ("Opção:", "1"), ("nome:", ""), ("Tente novamente!", "Ana"), (M, "0")]],
        lambda c, d: (c.count("O nome é obrigatório. Tente novamente!") == 2,
                      "Mensagem exibida 2 vezes (cadastro e alteração); Davi aceito"))
cenario("AC-RN003-007", A, "Valores negativos recusados; zero aceito", "RN003 a RN007 / CT013 a CT017",
        "Cada valor negativo recusado com mensagem e solicitado novamente; salário base 0 aceito",
        [[(M, "1"), ("id:", "8"), ("nome:", "Eva"), ("base:", "-100"), ("negativo", "0"), ("Produção", "1"),
          (M, "1"), ("id:", "2"), ("nome:", "Bruno"), ("base:", "2000"), ("Produção", "2"), ("produtos:", "-1000"),
          ("negativo", "10000"), (r"50%\)", "-0,05"), ("negativo", "0,05"),
          (M, "1"), ("id:", "3"), ("nome:", "Carla"), ("base:", "1800"), ("Produção", "3"), ("produzida:", "-10"),
          ("negativo", "100"), ("unidade:", "-2,50"), ("negativo", "2,50"), (M, "4"), (M, "0")]],
        lambda c, d: (c.count("O valor não pode ser negativo. Tente novamente!") == 5
                      and "Total da Folha de Pagamentos: R$ 4.550,00" in c,
                      "5 mensagens de valor negativo; Eva com R$ 0,00; total R$ 4.550,00"))
cenario("CT018", A, "Salário final nunca informado pelo usuário", "RN008",
        "Nenhuma tela solicita o salário final; ele aparece calculado na consulta e na folha",
        [M1 + M2 + M3 + [(M, "6"), (M, "3"), (M, "0")]],
        lambda c, d: (not re.search(r"(insira|informe|digite) o salário final", c, re.I) and c.count("Salário Final: R$") == 6,
                      "Nenhum pedido de salário final; valor calculado exibido 6 vezes (consulta e folha)"))
cenario("CT020", A, "Entradas inválidas não encerram o sistema", "RNF002",
        "Texto: mensagem e nova solicitação; negativo: mensagem; Enter vazio: continua aguardando; nada gravado inválido",
        [[(M, "abc"), ("números:", "1"), ("id:", "abc"), ("números:", "1"), ("nome:", "Ana"), ("base:", "abc"),
          ("números:", ""), ("", "3000"), ("Produção", "1"), (M, "1"), ("id:", "2"), ("nome:", "Bruno"),
          ("base:", "-5"), ("negativo", "2000"), ("Produção", "1"), (M, "6"), (M, "0")]],
        lambda c, d: (c.count("Entrada inválida. Digite apenas números:") == 3 and "Sistema encerrado." in c
                      and '"baseSalary":3000.0' in d and '"baseSalary":2000.0' in d,
                      "3 mensagens de entrada inválida, 1 de valor negativo; sistema não encerrou; só valores válidos gravados"))
cenario("CT021", A, "Persistência após reiniciar", "RNF003",
        "Ana exibida com os mesmos dados após encerrar (opção 0) e abrir de novo",
        [M1 + [(M, "0")], [(M, "6"), (M, "0")]],
        lambda c, d: ("Nome: Ana\nSalário Base: R$ 3.000,00" in c.split("EXECUÇÃO 2")[1],
                      "Na segunda execução a listagem exibe Ana, R$ 3.000,00"))

R = "reteste_defeitos"
cenario("BUG001", R, "Imprecisão de centavos (float)", "RF003 / RN008",
        "Vendas R$ 1.234.567,89 × 1 resultam em total R$ 1.234.567,89",
        [[(M, "1"), ("id:", "2"), ("nome:", "Bruno"), ("base:", "0"), ("Produção", "2"),
          ("produtos:", "1234567,89"), (r"50%\)", "1"), (M, "4"), (M, "0")]],
        contem("Total da Folha de Pagamentos: R$ 1.234.567,89"))
cenario("BUG002", R, "data.json corrompido encerrava o sistema", "RNF003",
        "Mensagem de erro, sistema continua no menu, arquivo preservado",
        [[(M, "6"), (M, "0")]],
        lambda c, d: ("Erro no arquivo de dados" in c and "Sistema encerrado." in c and d == "{isto nao e json",
                      "Mensagem de erro exibida; sistema voltou ao menu; arquivo não foi alterado"),
        preparo=lambda d: open(f"{d}/data.json", "w").write("{isto nao e json"))
cenario("BUG004", R, "Consulta com cadastro vazio prendia o usuário", "RF005",
        "Após 'não encontrado', a opção 0 volta ao menu",
        [[(M, "2"), ("Nome", "1"), ("Colaborador:", "99"), ("Nome", "0"), (M, "0")]],
        contem("Nenhum colaborador com este id, tente novamente!", "0- Voltar ao menu", "Sistema encerrado."))
cenario("BUG005", R, "Vírgula decimal recusada conforme idioma do SO", "RNF002",
        "0,05 e 0.05 aceitos em qualquer idioma do sistema",
        [[(M, "1"), ("id:", "2"), ("nome:", "Bruno"), ("base:", "2000"), ("Produção", "2"), ("produtos:", "10000"),
          (r"50%\)", "0,05"), (M, "1"), ("id:", "3"), ("nome:", "Beto"), ("base:", "2000"), ("Produção", "2"),
          ("produtos:", "10000"), (r"50%\)", "0.05"), (M, "4"), (M, "0")]],
        contem("Colaborador Comissionado: R$ 5.000,00"))
cenario("BUG006", R, "Opção de alteração inexistente informava sucesso", "RF009",
        "Opção 4 em colaborador padrão é recusada e solicitada novamente",
        [M1 + [(M, "5"), ("Colaborador:", "1"), ("Opção:", "4"), ("Opção:", "1"),
               ("nome:", "Ana"), (M, "0")]],
        contem("Opção inválida! Tente novamente."))
cenario("BUG007", R, "Percentual fracionário exibido arredondado", "RF005",
        "Comissão 0,025 exibida como 2,5%",
        [[(M, "1"), ("id:", "2"), ("nome:", "Bruno"), ("base:", "2000"), ("Produção", "2"), ("produtos:", "10000"),
          (r"50%\)", "0,025"), (M, "6"), (M, "0")]],
        contem("Comissão: 2,5% (R$ 250,00)"))
cenario("BUG009", R, "Textos de tela (erro de digitação e tipo sem acento)", "RNF001",
        "Menu exibe 'Mostrar todos Colaboradores'; tipo exibido como 'Padrão'",
        [M1 + [(M, "3"), (M, "0")]], contem("6- Mostrar todos Colaboradores", "Tipo: Padrão"))

json.dump(resultados, open(SAIDA_JSON, "w"), ensure_ascii=False, indent=1)
