package Modelo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;

public class Element {
    
    public String Name;
    public int cPosX;
    public int cPosY;
    //public LinkedHashSet<String> Antecesores; 
    public HashMap<String, ArrayList<Element>> Antecesores;
    
    public String type;
    
    
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
    
    
}
