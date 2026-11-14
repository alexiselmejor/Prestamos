package edu.universidad.presta.ai;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Parser JSON pequeño para evitar dependencias adicionales en el despliegue WAR. */
public final class MiniJson {
    private MiniJson() {}

    public static Object parse(String json) {
        if (json == null) return null;
        Parser p = new Parser(json);
        Object value = p.value();
        p.space();
        if (!p.end()) throw new IllegalArgumentException("JSON incompleto");
        return value;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> object(Object value) {
        return value instanceof Map ? (Map<String, Object>) value : Map.of();
    }

    @SuppressWarnings("unchecked")
    public static List<Object> array(Object value) {
        return value instanceof List ? (List<Object>) value : List.of();
    }

    private static final class Parser {
        private final String s;
        private int i;
        Parser(String s) { this.s = s; }
        boolean end() { return i >= s.length(); }
        void space() { while (!end() && Character.isWhitespace(s.charAt(i))) i++; }
        Object value() {
            space();
            if (end()) throw new IllegalArgumentException("Valor JSON ausente");
            char c = s.charAt(i);
            if (c == '{') return objectValue();
            if (c == '[') return arrayValue();
            if (c == '"') return stringValue();
            if (c == 't' && literal("true")) return Boolean.TRUE;
            if (c == 'f' && literal("false")) return Boolean.FALSE;
            if (c == 'n' && literal("null")) return null;
            return numberValue();
        }
        private boolean literal(String text) {
            if (s.startsWith(text, i)) { i += text.length(); return true; }
            return false;
        }
        private Map<String, Object> objectValue() {
            Map<String, Object> out = new LinkedHashMap<>(); i++; space();
            if (!end() && s.charAt(i) == '}') { i++; return out; }
            while (!end()) {
                space(); String key = stringValue(); space();
                if (end() || s.charAt(i++) != ':') throw new IllegalArgumentException("Falta ':' en JSON");
                out.put(key, value()); space();
                if (end()) break;
                char c = s.charAt(i++);
                if (c == '}') return out;
                if (c != ',') throw new IllegalArgumentException("Falta ',' en JSON");
            }
            throw new IllegalArgumentException("Objeto JSON sin cerrar");
        }
        private List<Object> arrayValue() {
            List<Object> out = new ArrayList<>(); i++; space();
            if (!end() && s.charAt(i) == ']') { i++; return out; }
            while (!end()) {
                out.add(value()); space();
                if (end()) break;
                char c = s.charAt(i++);
                if (c == ']') return out;
                if (c != ',') throw new IllegalArgumentException("Falta ',' en arreglo JSON");
            }
            throw new IllegalArgumentException("Arreglo JSON sin cerrar");
        }
        private String stringValue() {
            if (end() || s.charAt(i++) != '"') throw new IllegalArgumentException("Cadena JSON inválida");
            StringBuilder out = new StringBuilder();
            while (!end()) {
                char c = s.charAt(i++);
                if (c == '"') return out.toString();
                if (c != '\\') { out.append(c); continue; }
                if (end()) break;
                char e = s.charAt(i++);
                switch (e) {
                    case '"': out.append('"'); break;
                    case '\\': out.append('\\'); break;
                    case '/': out.append('/'); break;
                    case 'b': out.append('\b'); break;
                    case 'f': out.append('\f'); break;
                    case 'n': out.append('\n'); break;
                    case 'r': out.append('\r'); break;
                    case 't': out.append('\t'); break;
                    case 'u':
                        if (i + 4 > s.length()) throw new IllegalArgumentException("Escape Unicode inválido");
                        out.append((char) Integer.parseInt(s.substring(i, i + 4), 16)); i += 4; break;
                    default: throw new IllegalArgumentException("Escape JSON inválido");
                }
            }
            throw new IllegalArgumentException("Cadena JSON sin cerrar");
        }
        private Number numberValue() {
            int start = i;
            if (!end() && s.charAt(i) == '-') i++;
            while (!end() && Character.isDigit(s.charAt(i))) i++;
            if (!end() && s.charAt(i) == '.') { i++; while (!end() && Character.isDigit(s.charAt(i))) i++; }
            if (!end() && (s.charAt(i) == 'e' || s.charAt(i) == 'E')) {
                i++; if (!end() && (s.charAt(i) == '+' || s.charAt(i) == '-')) i++;
                while (!end() && Character.isDigit(s.charAt(i))) i++;
            }
            if (start == i) throw new IllegalArgumentException("Número JSON inválido");
            String n = s.substring(start, i);
            return n.contains(".") || n.contains("e") || n.contains("E") ? Double.valueOf(n) : Long.valueOf(n);
        }
    }
}
