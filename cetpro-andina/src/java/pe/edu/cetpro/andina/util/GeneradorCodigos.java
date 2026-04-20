package pe.edu.cetpro.andina.util;

import pe.edu.cetpro.andina.entity.Certificado;

/**
 * Genera códigos institucionales según reglas:
 *   MODULAR:         0001-1CM1-2024   (correlativo-periodoModulo-año)
 *   CAPACITACION:    00320             (correlativo simple)
 *   AUXILIAR_TECNICO:0001AT-DNI-2024
 *   TECNICO:         0001T-DNI-2024
 */
public class GeneradorCodigos {

    public static String generar(Certificado.Tipo tipo, int correlativo,
                                 int anio, String codigoModulo, String dni) {
        String corr = pad(correlativo, 4);
        switch (tipo) {
            case MODULAR:
                // Periodo: I=1, II=2. Por defecto 1.
                String periodo = "1";
                return corr + "-" + periodo + codigoModulo + "-" + anio;
            case CAPACITACION:
                return pad(correlativo, 5);
            case AUXILIAR_TECNICO:
                return corr + "AT-" + dni + "-" + anio;
            case TECNICO:
                return corr + "T-" + dni + "-" + anio;
            default:
                throw new IllegalArgumentException("Tipo no soportado");
        }
    }

    private static String pad(int n, int len) {
        return String.format("%0" + len + "d", n);
    }

    private GeneradorCodigos() {}
}
