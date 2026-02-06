package atm.util;

import java.util.*;

public final class SimpleJson {
    private SimpleJson(){}

    public static Object parse(String s) {
        return new Parser(s).parseValue();
    }

    public static String stringify(Object obj) {
        StringBuilder sb = new StringBuilder();
        writeValue(sb, obj);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static void writeValue(StringBuilder sb, Object v) {
        if (v == null) {
            sb.append("null");
        } else if (v instanceof String) {
            sb.append('"').append(escape((String) v)).append('"');
        } else if (v instanceof Number || v instanceof Boolean) {
            sb.append(v.toString());
        } else if (v instanceof Map) {
            sb.append("{");
            boolean first=true;
            for (Map.Entry<String,Object> e: ((Map<String,Object>)v).entrySet()) {
                if (!first) sb.append(",");
                first=false;
                sb.append('"').append(escape(e.getKey())).append('"').append(":");
                writeValue(sb, e.getValue());
            }
            sb.append("}");
        } else if (v instanceof List) {
            sb.append("[");
            boolean first=true;
            for (Object o: (List<Object>)v) {
                if (!first) sb.append(",");
                first=false;
                writeValue(sb, o);
            }
            sb.append("]");
        } else {
            sb.append('"').append(escape(v.toString())).append('"');
        }
    }

    private static String escape(String s) {
        return s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","\\r").replace("\t","\\t");
    }

    private static final class Parser {
        private final String s;
        private int i=0;

        Parser(String s){ this.s=s.trim(); }

        Object parseValue() {
            skipWs();
            if (i>=s.length()) throw err("Unexpected end");
            char c = s.charAt(i);
            if (c=='{') return parseObject();
            if (c=='[') return parseArray();
            if (c=='"') return parseString();
            if (c=='t' || c=='f') return parseBoolean();
            if (c=='n') return parseNull();
            if (c=='-' || Character.isDigit(c)) return parseNumber();
            throw err("Unexpected char: "+c);
        }

        private Map<String,Object> parseObject() {
            expect('{');
            Map<String,Object> m = new LinkedHashMap<>();
            skipWs();
            if (peek('}')) { expect('}'); return m; }
            while(true){
                skipWs();
                String key = parseString();
                skipWs();
                expect(':');
                Object val = parseValue();
                m.put(key,val);
                skipWs();
                if (peek('}')) { expect('}'); break; }
                expect(',');
            }
            return m;
        }

        private List<Object> parseArray() {
            expect('[');
            List<Object> a = new ArrayList<>();
            skipWs();
            if (peek(']')) { expect(']'); return a; }
            while(true){
                Object v = parseValue();
                a.add(v);
                skipWs();
                if (peek(']')) { expect(']'); break; }
                expect(',');
            }
            return a;
        }

        private String parseString() {
            expect('"');
            StringBuilder sb = new StringBuilder();
            while(i<s.length()){
                char c=s.charAt(i++);
                if (c=='"') break;
                if (c=='\\'){
                    if (i>=s.length()) throw err("Bad escape");
                    char e=s.charAt(i++);
                    switch(e){
                        case '"': sb.append('"'); break;
                        case '\\': sb.append('\\'); break;
                        case 'n': sb.append('\n'); break;
                        case 'r': sb.append('\r'); break;
                        case 't': sb.append('\t'); break;
                        default: sb.append(e);
                    }
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }

        private Boolean parseBoolean() {
            if (s.startsWith("true", i)) { i+=4; return true; }
            if (s.startsWith("false", i)) { i+=5; return false; }
            throw err("Bad boolean");
        }

        private Object parseNull() {
            if (s.startsWith("null", i)) { i+=4; return null; }
            throw err("Bad null");
        }

        private Number parseNumber() {
            int start=i;
            if (s.charAt(i)=='-') i++;
            while(i<s.length() && Character.isDigit(s.charAt(i))) i++;
            String num=s.substring(start,i);
            try { return Long.parseLong(num); }
            catch(Exception e){ throw err("Bad number"); }
        }

        private void skipWs(){
            while(i<s.length() && Character.isWhitespace(s.charAt(i))) i++;
        }

        private boolean peek(char c){
            return i<s.length() && s.charAt(i)==c;
        }

        private void expect(char c){
            skipWs();
            if (i>=s.length() || s.charAt(i)!=c) throw err("Expected '"+c+"'");
            i++;
        }

        private RuntimeException err(String msg){
            return new RuntimeException(msg+" at pos "+i);
        }
    }
}
