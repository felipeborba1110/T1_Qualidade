import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Operações monetárias com arredondamento em centavos.
 * Os cálculos usam BigDecimal para evitar a imprecisão de ponto flutuante (BUG001).
 */
public final class Dinheiro {

    public static final Locale LOCALE_BRASIL = new Locale("pt", "BR");
    private static final int CASAS_DECIMAIS = 2;

    private Dinheiro() {
    }

    public static double multiplicar(double valor, double fator) {
        return arredondar(BigDecimal.valueOf(valor).multiply(BigDecimal.valueOf(fator)));
    }

    public static double somar(double valor, double parcela) {
        return arredondar(BigDecimal.valueOf(valor).add(BigDecimal.valueOf(parcela)));
    }

    private static double arredondar(BigDecimal valor) {
        return valor.setScale(CASAS_DECIMAIS, RoundingMode.HALF_UP).doubleValue();
    }

    /** Formata no padrão brasileiro, por exemplo "R$ 3.000,00", independentemente do idioma do sistema. */
    public static String formatar(double valor) {
        return String.format(LOCALE_BRASIL, "R$ %,.2f", valor);
    }

    /** Formata um percentual informado como fração, por exemplo 0.025 vira "2,5%" (BUG007). */
    public static String formatarPercentual(double fracao) {
        DecimalFormat formato = new DecimalFormat("#,##0.##", DecimalFormatSymbols.getInstance(LOCALE_BRASIL));
        formato.setRoundingMode(RoundingMode.HALF_UP);
        return formato.format(BigDecimal.valueOf(fracao).multiply(BigDecimal.valueOf(100))) + "%";
    }
}
