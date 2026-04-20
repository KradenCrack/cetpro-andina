package pe.edu.cetpro.andina.util;

/**
 * Convierte números a texto español.
 * Ej: 17 -> "Diecisiete (17)"
 */
public class NumeroATexto {

    private static final String[] UNIDADES = {
        "", "Uno", "Dos", "Tres", "Cuatro", "Cinco",
        "Seis", "Siete", "Ocho", "Nueve", "Diez",
        "Once", "Doce", "Trece", "Catorce", "Quince",
        "Dieciséis", "Diecisiete", "Dieciocho", "Diecinueve", "Veinte"
    };

    public static String convertir(int nota) {
        if (nota < 0 || nota > 20) return String.valueOf(nota);
        return UNIDADES[nota] + " (" + nota + ")";
    }

    private NumeroATexto() {}
}
