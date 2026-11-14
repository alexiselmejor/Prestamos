package edu.universidad.presta.model;

import java.util.List;

/** Grupo de oficinas para el &lt;optgroup&gt; del formulario de solicitud. */
public class GrupoOficinas {

    private final String etiqueta;
    private final String area;
    private final List<String> oficinas;

    public GrupoOficinas(String etiqueta, String area, List<String> oficinas) {
        this.etiqueta = etiqueta;
        this.area = area;
        this.oficinas = oficinas;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String getArea() {
        return area;
    }

    public List<String> getOficinas() {
        return oficinas;
    }
}
