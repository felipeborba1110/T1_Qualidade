"""Reproduz, no código original (commit d2c5a11), os defeitos suspeitos.
Cada cenário roda em um diretório limpo e grava o log da sessão."""
import os, shutil, subprocess, sys, pexpect

BASE = sys.argv[1]          # pasta com out/ e o jar
OUT = sys.argv[2]           # pasta de logs
os.makedirs(OUT, exist_ok=True)
CP = f"{BASE}/out:{BASE}/json-simple-1.1.1.jar"
PT = ["-Duser.language=pt", "-Duser.country=BR"]


def sessao(nome, passos, locale_pt=True, preparo=None, timeout=4):
    d = f"/tmp/bl_{nome}"
    shutil.rmtree(d, ignore_errors=True); os.makedirs(d)
    if preparo:
        preparo(d)
    args = (PT if locale_pt else []) + ["-cp", CP, "Main"]
    log = open(f"{OUT}/{nome}.log", "w")
    p = pexpect.spawn("java", args, cwd=d, encoding="utf-8", timeout=timeout)
    p.logfile_read = log
    status = "ok"
    try:
        for espera, envio in passos:
            if espera:
                p.expect(espera)
            if envio is not None:
                p.sendline(envio)
        p.expect(pexpect.EOF)
    except pexpect.TIMEOUT:
        status = "TIMEOUT (sistema preso aguardando entrada)"
    except pexpect.EOF:
        status = "EOF inesperado (sistema encerrou)"
    p.close(force=True)
    log.write(f"\n### RESULTADO DO ROTEIRO: {status}\n")
    log.close()
    print(nome, "->", status)
    return d


M = "opção:"
cad_padrao = lambda i, n, s: [(M, "1"), ("id:", i), ("nome:", n), ("base:", s), ("Produção", "1")]

# BUG: consulta com cadastro vazio prende o usuário
sessao("bug_consulta_vazia", [(M, "2"), ("Nome", "1"), ("Colaborador:", "99"),
                              ("Nome", "1"), ("Colaborador:", "1"), ("Nome", None)])

# BUG: decimal com vírgula recusado em SO com locale en (ex.: Linux/macOS, Windows em inglês)
sessao("bug_locale_virgula", [(M, "1"), ("id:", "1"), ("nome:", "Bruno"), ("base:", "2000"),
                              ("Produção", "2"), ("produtos:", "10000"), ("50%\\)", "0,05"),
                              ("números", None)], locale_pt=False)

# BUG: entrada redirecionada (pipe) - vários Scanner sobre System.in
d = "/tmp/bl_bug_pipe"; shutil.rmtree(d, ignore_errors=True); os.makedirs(d)
r = subprocess.run(["java"] + PT + ["-cp", CP, "Main"], cwd=d, input="1\n1\nAna\n3000\n1\n6\n0\n",
                   capture_output=True, text=True, timeout=20)
with open(f"{OUT}/bug_pipe.log", "w") as f:
    f.write("ENTRADA: 1,1,Ana,3000,1,6,0 (via pipe)\n--- STDOUT ---\n" + r.stdout + "\n--- STDERR ---\n" + r.stderr)
    f.write(f"\n### data.json existe: {os.path.exists(d + '/data.json')}\n")
print("bug_pipe -> exit", r.returncode, "| data.json:", os.path.exists(d + "/data.json"))

# BUG: data.json corrompido encerra o sistema
sessao("bug_json_corrompido", [(M, "6"), (None, None)],
       preparo=lambda d: open(f"{d}/data.json", "w").write("{isto nao e json"))

# BUG: opção 4 em colaborador padrão aceita sem efeito e informa sucesso
sessao("bug_modificar_opcao_invalida", cad_padrao("1", "Ana", "3000") +
       [("sucesso", None), (M, "5"), ("Colaborador:", "1"), ("Opção:", "4"), ("sucesso", None), (M, "0")])

# BUG: percentual fracionário exibido arredondado (2,5% vira 3%)
sessao("bug_percentual_arredondado", [(M, "1"), ("id:", "2"), ("nome:", "Bruno"), ("base:", "2000"),
       ("Produção", "2"), ("produtos:", "10000"), ("50%\\)", "0,025"), (M, "6"), (M, "0")])

# BUG: imprecisão de float nos centavos
sessao("bug_float_centavos", [(M, "1"), ("id:", "2"), ("nome:", "Bruno"), ("base:", "0"),
       ("Produção", "2"), ("produtos:", "1234567,89"), ("50%\\)", "1"), (M, "4"), (M, "0")])

# BUG: menu recursivo (pilha cresce a cada operação) – evidência por inspeção: ver Menu.java
