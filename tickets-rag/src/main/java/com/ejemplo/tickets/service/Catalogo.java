package com.ejemplo.tickets.service;

import java.text.Normalizer;
import java.util.List;

/**
 * Valores permitidos para categoria, prioridad y area.
 * Se usan en dos lugares: en el prompt (para decirle al LLM que opciones tiene)
 * y para validar lo que el LLM responde (nunca confiamos ciegamente en su salida).
 */
public final class Catalogo {

    public static final List<String> CATEGORIAS = List.of(
            "Facturación", "Soporte técnico", "Envíos", "Cuenta y acceso",
            "Devoluciones", "Seguridad", "Consulta general");

    public static final List<String> PRIORIDADES = List.of("alta", "media", "baja");

    public static final List<String> AREAS = List.of(
            "Finanzas", "TI", "Logística", "Atención al cliente", "Seguridad");

    private Catalogo() { }

    /**
     * Devuelve el valor oficial de la lista que coincide con "valor" sin importar
     * mayusculas ni acentos ("facturacion" -> "Facturación"), o null si no existe.
     */
    public static String canonico(String valor, List<String> permitidos) {
        if (valor == null) {
            return null;
        }
        String buscado = normalizar(valor);
        for (String permitido : permitidos) {
            if (normalizar(permitido).equals(buscado)) {
                return permitido;
            }
        }
        return null;
    }

    private static String normalizar(String s) {
        return Normalizer.normalize(s.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();
    }
}
