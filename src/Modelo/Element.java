package Modelo;

import java.util.ArrayList;
import java.util.HashMap;

public class Element{
    /*
        Esta clase representa un elemento gráfico y es utilizada para mostrar el modelo gráfico del BPMN
    */
    public String Name; //Nombre del elemento
    public int cPosX; //Posición X del elemento en el Canvas
    public int cPosY; //Posición Y del elemento en el canvas
    public HashMap<String, ArrayList<Element>> Antecesores; //Mapa de antecesores donde la clave es el antecesor de este elemento y el valor es la lista de quiebres que existen entre ese arco
    public String type; //Tipo del elemento (tarea, compuerta, evento inicio, evento fin, etc)
    
    
    public Element(){
        this.Name = "";
        this.cPosX = 0;
        this.cPosY = 0;
        this.type = "";
        Antecesores = new HashMap<>();
    }
    
    public Element(String name){
        this.Name = name;
        this.cPosX = 0;
        this.cPosY = 0;
        this.type="";
        Antecesores = new HashMap<>();
    }
    
    @Override
    public String toString(){
        return "Name: " + Name;
    }
    
    
}
