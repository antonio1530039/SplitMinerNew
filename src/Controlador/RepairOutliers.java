package Controlador;

import java.util.ArrayList;


public class RepairOutliers {
    
    public static ArrayList<ArrayList<Character>> context(ArrayList<Character> secuence, ArrayList<Character> trace, int l, int r ){
        //Dada una secuencia de tareas y una traza; encontrar el contexto de la secuencia...
        //...según el número de vecinos requeridos a la izquierda y a la derecha indicados en los parámetros 'l' y 'r' respectivamente
        ArrayList<Character> leftNeighbour = new ArrayList<>();
        ArrayList<Character> rightNeighbour = new ArrayList<>();
        ArrayList<ArrayList<Character>> context = new ArrayList<>();
        
        int subLeft = trace.indexOf(secuence.get(0)); //posición de la traza del primer elemento de la secuencia
        int subRight = trace.indexOf(secuence.get(secuence.size()-1) ); //indice de la traza del ultimo elemento en la secuencia
        
        //verificamos que la posición en la traza del primer elemento en la secuencia menos el número de elementos requeridos hacia la izquierda desde sea mayor a 0
        if( (subLeft - l) >= 0){ 
            //ciclo inicia en la posición: posición en la traza del primer elemento de la secuencia menos el número de elementos requeridos, 
            //lo anterior es para agregar los elementos del vecino izquierda de forma ordenada al arraylist, 
            //ciclo se detiene hasta que i es mayor a la posicion en la traza del primer elemento en la secuencia menos 1
            for(int i = (subLeft - l) ; i <=  subLeft - 1; i++){ 
                leftNeighbour.add(trace.get(i));
            }
        }
        
        //Verificamos que la posición en la traza del ultimo elemento en la secuencia mas el número de elementos requeridos hacia la derecha sea menor al número de elementos en la traza
        if( (subRight + r) < trace.size()){
            //el ciclo inicia en la posicion: indice del ultimo elemento de la secuencia mas 1
            //ciclo se detiene hasta que i es mayor a la posición en la traza del ultimo elemento en la secuencia más el número de elementos requeridos
            for(int i = (subRight + 1); i <= (subRight + r); i++){
                rightNeighbour.add(trace.get(i));
            }
        }
        context.add(leftNeighbour);
        context.add(rightNeighbour);
        return context;
    }   


    public static ArrayList<Character> covering(ArrayList<Character> trace, ArrayList<Character> leftNeighbour, ArrayList<Character> rightNeighbour ){
        ArrayList<Character> covering = new ArrayList<>();
        
        if(leftNeighbour.isEmpty() || rightNeighbour.isEmpty()) //PENDIENTE.. Duda, en caso de detectar vecinos vacios que se retorna?
            return covering;
        //el ciclo inicia en la posición en la traza del último elemento  de la secuencia correspondiente al vecino izquierdo más 1
        //el ciclo termina hasta que i es igual a la posición en la traza del primer elemento de la secuencia correspondiente al vecino derecho
        for(int i = trace.indexOf(leftNeighbour.get(leftNeighbour.size()-1)) + 1; i < trace.indexOf(rightNeighbour.get(0)); i++){
            covering.add(trace.get(i));
        }
        return covering;
    }
}