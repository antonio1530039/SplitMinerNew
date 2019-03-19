package Controlador;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

public class RepairOutliers {

    LinkedHashMap<ArrayList<ArrayList<Character>>, LinkedHashMap< ArrayList<Character>, Object[]>> V = new LinkedHashMap<>();

    public void Filtering(LinkedHashMap<Integer, ArrayList<Character>> L, int l, int r, int K, double umbral) {
        for (Map.Entry<Integer, ArrayList<Character>> trace : L.entrySet()) {
            ArrayList<Character> ti = trace.getValue();
            ti.add(0, 'I');
            ti.add('O');
        }

        boolean activo = true;
        int j = 0;
        while (activo) {
            int contadorContextos = 0;

            for (Map.Entry<Integer, ArrayList<Character>> trace : L.entrySet()) {
                ArrayList<Character> ti = trace.getValue();

                ArrayList<LinkedHashSet<ArrayList<Character>>> R = new ArrayList<>();
                ArrayList<ArrayList<ArrayList<Character>>> cs = new ArrayList<>();
                ArrayList<Integer> ks = new ArrayList<>();

                for (int k = 0; k <= K; k++) {
                    //Obtener contexto
                    ArrayList<ArrayList<Character>> c = obtenerContexto(ti, j, l, r, k);
                    if (c == null) {
                        break;
                    }

                    if (V.containsKey(c)) {
                        continue;
                    }

                    R.add(covertura(c, K, L));
                    cs.add(c);
                    ks.add(k);

                    contadorContextos++;
                }

                if (contadorContextos == 0) {
                    continue;
                }

                //Agregar contextos
                for (ArrayList<ArrayList<Character>> context : cs) {
                    V.put(context, new LinkedHashMap<>());
                }

                //Obtener frecuencias para cada contexto identificado
                for (int index = 0; index < cs.size(); index++) {
                    int F = 0;
                    
                    LinkedHashMap<ArrayList<Character>, Object[]> coverMap = new LinkedHashMap<>();
                    for (ArrayList<Character> subsequence : R.get(index)) {
                        int f = getFrecuency(cs.get(index), subsequence, K, L);
                        Object[] frecAndPCE = new Object[2];
                        frecAndPCE[0] = (int) f;
                        coverMap.put(subsequence, frecAndPCE);
                        F += f;
                    }

                    V.put(cs.get(index), coverMap);

                    double max = 0.0;
                    ArrayList<Character> maxCover = new ArrayList<>();
                    for (ArrayList<Character> subsequence : R.get(index)) {
                        double PCE = (int) V.get(cs.get(index)).get(subsequence)[0] / (double) F;
                        V.get(cs.get(index)).get(subsequence)[1] = PCE;
                        if (PCE > max) {
                            max = PCE;
                            maxCover = (ArrayList<Character>) subsequence.clone();
                        }
                    }
                    Object[] maxContextObject = new Object[2];
                    maxContextObject[0] = cs.get(index);
                    maxContextObject[1] = maxCover.clone();
                    
                    remplazar(L, R.get(index), maxContextObject, umbral);
                    
                }
            }
            if (contadorContextos == 0) {
                activo = false;
            }
            j += l;
        }

        for (Map.Entry<ArrayList<ArrayList<Character>>, LinkedHashMap<ArrayList<Character>, Object[]>> entry : V.entrySet()) {

            System.out.println("\nContext: " + entry.getKey().toString());
            System.out.println("Covering: ");
            //Calcular probabilidades
            LinkedHashMap< ArrayList<Character>, Object[]> coverturas = V.get(entry.getKey());

            for (Map.Entry<ArrayList<Character>, Object[]> cov : entry.getValue().entrySet()) {
                System.out.println("\ts: " + cov.getKey());
                System.out.println("\t\tF: " + (int) cov.getValue()[0]);
                System.out.println("\t\tPCE: " + (double) cov.getValue()[1]);
            }

        }
    }

    public int getFrecuency(ArrayList<ArrayList<Character>> c, ArrayList<Character> s2, int k, LinkedHashMap<Integer, ArrayList<Character>> L) {
        int F = 0;
        
        LinkedHashSet<ArrayList<Character>> cov = new LinkedHashSet();

        for (Map.Entry<Integer, ArrayList<Character>> trace : L.entrySet()) {
            ArrayList<Character> ti = trace.getValue();
            ArrayList<Integer> jList = new ArrayList<>();

            Character first = c.get(0).get(0);
            
            for (int i = 0; i < ti.size(); i++) {
                if (ti.get(i) == first) {
                    jList.add(i);
                }
            }
            for(int kk = 0; kk <= k; kk++){
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
                        if(s.equals(s2)){
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

        for (Map.Entry<Integer, ArrayList<Character>> trace : L.entrySet()) {
            ArrayList<Character> ti = trace.getValue();
            ArrayList<Integer> jList = new ArrayList<>();
            Character first = c.get(0).get(0); 
            for (int i = 0; i < ti.size(); i++) {
                if (ti.get(i) == first) {
                    jList.add(i);
                }
            }
            for(int kk = 0; kk <= k; kk++){
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
    
    public void remplazar(LinkedHashMap<Integer, ArrayList<Character>> L, LinkedHashSet<ArrayList<Character>> R, Object[] maxContext, double umbral){
        ArrayList<ArrayList<Character>> c = (ArrayList<ArrayList<Character>>) maxContext[0];
        
        
        for (Map.Entry<Integer, ArrayList<Character>> ti : L.entrySet()) {
            for(ArrayList<Character> s : R){
                //Obtenemos subsecuencias del contexto
                LinkedHashMap<ArrayList<Character>, Object[]> subs = V.get(c);
                if(subs!= null){
                    //obtener valores de frecuencia y pce de la subsecuencia analizada s
                    Object[] p = subs.get(s);
                    if(p != null){
                        double PCE = (double) p[1];
                        if(PCE < umbral){
                            ArrayList<Character> trace = ti.getValue();
                            
                            ArrayList<Character> fullSecuence = new ArrayList<>();
                            fullSecuence.addAll(c.get(0));
                            if(s.size()==1 && s.get(0) != 'I' && s.get(0) != 'O')
                                fullSecuence.addAll(s);
                            fullSecuence.addAll(c.get(1));
                            
                            //Verificar que existe en la traza
                            ArrayList<Integer> jList = new ArrayList<>();
                            for(int i= 0; i < trace.size(); i++){
                                if(trace.get(i) == fullSecuence.get(0)){
                                    jList.add(i);
                                }
                            }
                            
                            boolean existe = (jList.isEmpty()) ? false : true;
                            int x = 0;
                            int J = -1;
                            for(Integer j : jList){
                                int i = j;
                                while(x < fullSecuence.size()){
                                    if(trace.get(i) != fullSecuence.get(x)){
                                        existe = false;
                                        break;
                                    }
                                    x++;
                                    i++;
                                }
                                if(existe){
                                    J = j;
                                    break;
                                }
                                    
                            }
                            
                            if(existe){
                                ArrayList<Character> maxSecuence = new ArrayList<>();
                                maxSecuence.addAll(c.get(0)); //vecino izquierdo maxContext
                                maxSecuence.addAll((ArrayList<Character>) maxContext[1]); //covertura maxContext
                                maxSecuence.addAll(c.get(1)); //vecino derecho maxContext
                                System.out.println("Trace replacement...");
                                System.out.println("\t\tSecuence: " + fullSecuence.toString() + " < umbral and exists in trace: " + trace.toString());
                                System.out.println("\t\tMaxContext: " + maxSecuence.toString());                                
                                System.out.println("\tTrace before change: " + trace.toString());
                                int iterator = 0;
                                while(iterator < maxSecuence.size()){
                                    if(trace.get(J) != maxSecuence.get(iterator)){
                                        if(maxSecuence.get(iterator) == 'O' || maxSecuence.get(iterator) == 'I' ){
                                            trace.remove(J);
                                            J--;
                                        }else if(maxSecuence.size() > fullSecuence.size()){
                                            trace.add(J, maxSecuence.get(iterator));
                                        }else{
                                            trace.remove(J);
                                            trace.add(J, maxSecuence.get(iterator));
                                        }
                                    }
                                    J++;
                                    iterator++;
                                }
                                System.out.println("\tTrace after change: " + trace.toString());
                                System.out.println("");
                            }
                            
                        }
                    }
                }
                
            }
            
        }
    }

}
