package Controlador;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

public class RepairOutliers {

    LinkedHashMap<ArrayList<ArrayList<Character>>, LinkedHashMap< ArrayList<Character>, Object[]>> V = new LinkedHashMap<>();

    public void Filtering(LinkedHashMap<Integer, ArrayList<Character>> L, int l, int r, int K, double umbral, StringBuilder contextOutput) {
        for (Map.Entry<Integer, ArrayList<Character>> trace : L.entrySet()) {
            ArrayList<Character> ti = trace.getValue();
           // ti.add(0, 'I');
           // ti.add('O');
        }

        boolean activo = true;
        int j = 0;
        while (activo) {
            int contadorContextos = 0;

            for (Map.Entry<Integer, ArrayList<Character>> trace : L.entrySet()) {
                ArrayList<Character> ti = trace.getValue();

                for (int k = 0; k <= K; k++) {
                    //Obtener contexto
                    ArrayList<ArrayList<Character>> c = obtenerContexto(ti, j, l, r, k);
                    if (c == null) {
                        break;
                    }

                    if (V.containsKey(c)) {
                        continue;
                    }

                    LinkedHashSet<ArrayList<Character>> cov = covertura(c, K, L);
                    contadorContextos++;

                    //Agregar contextos
                    V.put(c, new LinkedHashMap<>());

                    //Obtener frecuencias para el contexto identificado
                    int F = 0;

                    LinkedHashMap<ArrayList<Character>, Object[]> coverMap = new LinkedHashMap<>();

                    for (ArrayList<Character> subsequence : cov) {
                        int f = getFrecuency(c, subsequence, K, L);
                        Object[] frecAndPCE = new Object[2];
                        frecAndPCE[0] = (int) f;
                        coverMap.put(subsequence, frecAndPCE);
                        F += f;
                    }
                    V.put(c, coverMap);

                    double max = 0.0;
                    ArrayList<Character> maxCover = new ArrayList<>();
                    for (ArrayList<Character> subsequence : cov) {
                        double PCE = (int) V.get(c).get(subsequence)[0] / (double) F;
                        V.get(c).get(subsequence)[1] = PCE;
                        if (PCE > max) {
                            max = PCE;
                            maxCover = (ArrayList<Character>) subsequence.clone();
                        }
                    }
                    Object[] maxContextObject = new Object[2];
                    maxContextObject[0] = c;
                    maxContextObject[1] = maxCover.clone();

                    remplazar(L, cov, maxContextObject, umbral);

                }

            }

            if (contadorContextos == 0) {
                activo = false;
            }

            j += l;
        }

        //Imprimir contextos
        for (Map.Entry<ArrayList<ArrayList<Character>>, LinkedHashMap<ArrayList<Character>, Object[]>> entry : V.entrySet()) {
            contextOutput.append("=============================");
            contextOutput.append("\nContext: " + entry.getKey().toString() + "\nCovering:\n");
            LinkedHashMap< ArrayList<Character>, Object[]> coverturas = V.get(entry.getKey());
            for (Map.Entry<ArrayList<Character>, Object[]> cov : entry.getValue().entrySet()) {
                contextOutput.append("\ts: " + cov.getKey() + "\n\tF: " + (int) cov.getValue()[0] + "\n\tPCE: " + (double) cov.getValue()[1] + "\n\n");
            }
            contextOutput.append("=============================\n");
        }
    }

    public int getFrecuency(ArrayList<ArrayList<Character>> c, ArrayList<Character> s2, int k, LinkedHashMap<Integer, ArrayList<Character>> L) {
        int F = 0;
        Character first = c.get(0).get(0);
        for (Map.Entry<Integer, ArrayList<Character>> trace : L.entrySet()) {
            ArrayList<Character> ti = trace.getValue();
            ArrayList<Integer> jList = new ArrayList<>();
            for (int i = 0; i < ti.size(); i++) {
                if (ti.get(i) == first) {
                    jList.add(i);
                }
            }
            for (int kk = 0; kk <= k; kk++) {
                for (Integer j : jList) {
                    //Obtener contexto
                    ArrayList<ArrayList<Character>> cc = obtenerContexto(ti, j, c.get(0).size(), c.get(1).size(), kk);
                    if (c.equals(cc)) { //El contexto existe, ahora obtener covertura
                        ArrayList<Character> s = new ArrayList<>();
                        if (kk == 0) {
                            s.add('O');
                        } else {
                            // c.get(0).size = l (longitud de contexto a la izquierda)
                            for (int i = (j + c.get(0).size()); i < ((j + c.get(0).size()) + kk); i++) {
                                s.add(ti.get(i));
                            }
                        }
                        if (s.equals(s2)) {
                            F++;
                        }
                    }
                }
            }
        }
        return F;
    }

    public ArrayList<ArrayList<Character>> obtenerContexto(ArrayList<Character> ti, int j, int l, int r, int k1) {
        if ((j + l + k1 + r - 1) < ti.size()) {
            ArrayList<ArrayList<Character>> contexto = new ArrayList<>();
            ArrayList<Character> izquierda = new ArrayList<>();
            ArrayList<Character> derecha = new ArrayList<>();

            for (int i = j; i < j + l; i++) {
                izquierda.add(ti.get(i));
            }

            for (int i = (j + l + k1); i < (j + l + k1 + r); i++) {
                derecha.add(ti.get(i));
            }

            contexto.add(izquierda);
            contexto.add(derecha);

            return contexto;

        }
        return null;
    }

    public LinkedHashSet<ArrayList<Character>> covertura(ArrayList<ArrayList<Character>> c, int k, LinkedHashMap<Integer, ArrayList<Character>> L) {

        LinkedHashSet<ArrayList<Character>> cov = new LinkedHashSet();
        Character first = c.get(0).get(0);
        for (Map.Entry<Integer, ArrayList<Character>> trace : L.entrySet()) {
            ArrayList<Character> ti = trace.getValue();
            ArrayList<Integer> jList = new ArrayList<>();

            for (int i = 0; i < ti.size(); i++) {
                if (ti.get(i) == first) {
                    jList.add(i);
                }
            }
            for (int kk = 0; kk <= k; kk++) {
                for (Integer j : jList) {
                    //Obtener contexto
                    ArrayList<ArrayList<Character>> cc = obtenerContexto(ti, j, c.get(0).size(), c.get(1).size(), kk);

                    if (c.equals(cc)) { //El contexto existe, ahora obtener covertura
                        ArrayList<Character> s = new ArrayList<>();

                        if (kk == 0) {
                            s.add('O');
                        } else {
                            // c.get(0).size = l (longitud de contexto a la izquierda)
                            for (int i = (j + c.get(0).size()); i < ((j + c.get(0).size()) + kk); i++) {
                                s.add(ti.get(i));
                            }
                        }
                        cov.add(s);
                    }
                }
            }

        }
        return cov;
    }

    public void remplazar(LinkedHashMap<Integer, ArrayList<Character>> L, LinkedHashSet<ArrayList<Character>> R, Object[] maxContext, double umbral) {
        ArrayList<ArrayList<Character>> c = (ArrayList<ArrayList<Character>>) maxContext[0];
        LinkedHashMap<ArrayList<Character>, Object[]> subs = V.get(c);//Obtenemos subsecuencias del contexto
        ArrayList<Character> maxSecuence = new ArrayList<>();
        maxSecuence.addAll(c.get(0)); //vecino izquierdo maxContext
        maxSecuence.addAll((ArrayList<Character>) maxContext[1]); //covertura maxContext
        maxSecuence.addAll(c.get(1)); //vecino derecho maxContext

        for (Map.Entry<Integer, ArrayList<Character>> ti : L.entrySet()) {
            ArrayList<Character> trace = ti.getValue();
            for (ArrayList<Character> s : R) {
                //obtener valores de frecuencia y pce de la subsecuencia analizada s
                Object[] p = subs.get(s);
                double PCE = (double) p[1];
                if (PCE < umbral) {
                    ArrayList<Character> fullSecuence = new ArrayList<>();
                    fullSecuence.addAll(c.get(0));

                    for (Character ch : s) {
                        if (ch != 'O') {
                            fullSecuence.add(ch);
                        }
                    }

                    fullSecuence.addAll(c.get(1));

                    //Verificar que existe en la traza
                    ArrayList<Integer> jList = new ArrayList<>();
                    for (int i = 0; i < trace.size(); i++) {
                        if (trace.get(i) == fullSecuence.get(0)) {
                            jList.add(i);
                        }
                    }

                    boolean existe = (jList.isEmpty()) ? false : true;
                    int x = 0;
                    int J = -1;
                    for (Integer j : jList) {
                        int i = j;
                        while (x < fullSecuence.size()) {
                            if (trace.get(i) != fullSecuence.get(x)) {
                                existe = false;
                                break;
                            }
                            x++;
                            i++;
                        }
                        if (existe) {
                            J = j;
                            break;
                        }

                    }

                    if (existe) {
                       // System.out.println("Trace replacement...\n" + "\t\tSecuence: " + fullSecuence.toString() + " < umbral and exists in trace: " + trace.toString() + "\n" + "\t\tMaxContext: " + maxSecuence.toString() + "\n" + "\tTrace before change: " + trace.toString());
                        int iterator = 0;
                        while (iterator < maxSecuence.size()) {
                            if (trace.get(J) != maxSecuence.get(iterator)) {
                                if (maxSecuence.get(iterator) == 'O' || maxSecuence.get(iterator) == 'I') {
                                    trace.remove(J);
                                    J--;
                                } else if (maxSecuence.size() > fullSecuence.size()) {
                                    trace.add(J, maxSecuence.get(iterator));
                                } else {
                                    trace.remove(J);
                                    trace.add(J, maxSecuence.get(iterator));
                                }
                            }
                            J++;
                            iterator++;
                        }
                        //System.out.println("\tTrace after change: " + trace.toString() + "\n");
                    }
                }
            }
        }
    }
}
