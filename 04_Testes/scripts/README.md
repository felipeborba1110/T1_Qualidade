# Roteiros de execução das evidências

| Script | Uso |
|---|---|
| `aceitacao.py` | Executa os cenários de aceitação e o reteste dos defeitos sobre o executável, digitando as entradas em um terminal real, e grava cada sessão em `04_Testes/evidencias/`. |
| `reproduzir_defeitos_versao_anterior.py` | Reproduz os defeitos BUG001 a BUG007 na versão da A1 (commit `d2c5a11`), compilada em uma pasta à parte. |

Requisitos: Linux ou macOS (o pexpect usa terminal POSIX), Python 3 e `pip install pexpect`.

```
python3 aceitacao.py ../../07_Executavel/folha-pagamento.jar ../evidencias resultados.json
python3 reproduzir_defeitos_versao_anterior.py <pasta com out/ e json-simple-1.1.1.jar> <pasta de logs>
```

No Windows, os mesmos cenários podem ser executados manualmente seguindo as entradas gravadas em cada arquivo de evidência.
