package Controlador;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class RepairOutliers {

    LinkedHashMap<ArrayList<ArrayList<Character>>, LinkedHashMap< ArrayList<Character>, Object[]>> V = new LinkedHashMap<>();

    public void Filtering(LinkedHashMap<Integer, ArrayList<Character>> tracesList, int l, int r, int K, double umbral) {
        for (Map.Entry<Integer, ArrayList<Character>> trace : tracesList.entrySet()) {
            ArrayList<Character> ti = trace.getValue();
            ti.add(0, 'I');
            ti.add('O');
        }

        boolean activo = true;
        int j = 0;

        while (activo) {
            for (Map.Entry<Integer, ArrayList<Character>> trace : tracesList.entrySet()) {
                
                ArrayList<Character> ti = trace.getValue();
                //int contadorContextos = 0;
                
                for (int k = 0; k <= K; k++) {
                    System.out.println("\n\n\tPara K = " + k);
                    
                    ArrayList<ArrayList<Character>> c = obtenerContexto(ti, j, l, r, k);
                   
                    if (c == null) {
                        break;
                    }
                    LinkedHashMap<ArrayList<Character>, Object[]> cov = covertura(c, k, tracesList);
                    
                    V.put(c, cov);
                    
                    //contadorContextos++;
                    System.out.println("\t\tContexto: " + c.toString());
                    System.out.println("\t\t\tCovering: ");
                    for(Map.Entry<ArrayList<Character>, Object[]> entry : cov.entrySet()){
                        System.out.println("\t\t\t\ts: " + entry.getKey());
                        System.out.println("\t\t\t\t\tF: " +  (int)entry.getValue()[0]);
                        System.out.println("\t\t\t\t\tPCE: " +  entry.getValue()[1]);
                    }
                    
                    
                }
                //if(contadorContextos == 0){
                //    continue;
                //}
                
                
            }
            activo = false;
        }

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

    public LinkedHashMap<ArrayList<Character>, Object[]> covertura(ArrayList<ArrayList<Character>> c, int k, LinkedHashMap<Integer, ArrayList<Character>> tracesList) {
        LinkedHashMap<ArrayList<Character>, Object[]> cov = new LinkedHashMap();
        
        for (Map.Entry<Integer, ArrayList<Character>> trace : tracesList.entrySet()) {
            ArrayList<Character> ti = trace.getValue();

            int j = ti.indexOf( c.get(0).get(0));
            
            if(j == -1)
                continue;
            
            //Obtener contexto
            ArrayList<ArrayList<Character>> cc = obtenerContexto(ti, j, c.get(0).size(), c.get(1).size(), k);
            
            if(c.equals(cc)){ //El contexto existe, ahora obtener covertura
                ArrayList<Character> s = new ArrayList<>();
                
                if(k == 0){
                    s.add('O');
                }else{
                    // c.get(0).size = l (longitud de contexto a la izquierda)
                    for(int i= (j + c.get(0).size()); i < ( (j + c.get(0).size()) + k); i++){
                        s.add(ti.get(i));
                    }
                }
                
                if(cov.containsKey(s)){
                   Object[] freqAndPCE = cov.get(s);
                   Object[] newFreqAndPCE = new Object[2];
                   newFreqAndPCE[0] = ((int) freqAndPCE[0]) + 1;
                   cov.put(s, newFreqAndPCE);
                }else{
                   Object[] freqAndPCE = new Object[2];
                   freqAndPCE[0] = 1;
                   cov.put(s, freqAndPCE);
                }
                
                //R.add(s);
            }
        }
        return cov;
    }

}
