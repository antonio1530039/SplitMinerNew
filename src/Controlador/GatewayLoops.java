
package Controlador;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;


public class GatewayLoops {
    
    
    public static LinkedHashSet<String> getAllAntecesores(LinkedHashMap<String, Integer> WFG, String target){
        LinkedHashSet<String> atras = new LinkedHashSet<>();
        
        LinkedHashSet<String> antecesores = getSucesoresOAntecesores(target, WFG, 'a');
        
        atras.addAll(antecesores);
        
        while(!antecesores.isEmpty()){
            for(String a : (LinkedHashSet<String>) antecesores.clone()){
                antecesores = getAllAntecesores(WFG, a);
                atras.addAll(antecesores);
            }
        }
        
        return atras;
        
    }
    
    public static LinkedHashSet<String> getAllSucesores(LinkedHashMap<String, Integer> WFG, String target){
        LinkedHashSet<String> adelante = new LinkedHashSet<>();
        
        LinkedHashSet<String> sucesores = getSucesoresOAntecesores(target, WFG, 's');
        
        adelante.addAll(sucesores);
        
        while(!sucesores.isEmpty()){
            for(String s : (LinkedHashSet<String>) sucesores.clone()){
                sucesores = getAllSucesores(WFG, s);
                adelante.addAll(sucesores);
            }
        }
        
        return adelante;
        
    }
    
     //all nodes following 'task', given the current pruened WFG
   public static LinkedHashSet<String> getSucesoresOAntecesores(String task, LinkedHashMap<String, Integer> WFG, Character type) {
      LinkedHashSet<String> antecesores = new LinkedHashSet<String>();
      for (Map.Entry<String, Integer> entry : WFG.entrySet()) {
         String key = entry.getKey();
         String vals[] = key.split(",");
         String c;
         if(type == 'a'){
             c = vals[1];
         }else{
             c = vals[0];
         }
         if (task.equals(c) ) {
             if(type == 'a')
                antecesores.add(vals[0]);
             else
                 antecesores.add(vals[1]);
         }
      }
   
      return antecesores;
   }
   
    
    
    
    
}
