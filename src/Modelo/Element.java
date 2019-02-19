package Modelo;

import java.util.ArrayList;

public class Element {
    
    public String Name;
    public int cPosX;
    public int cPosY;
    public ArrayList<String> Antecesores;
    public String type;
    
    
    public Element(){
        this.Name = "";
        this.cPosX = 0;
        this.cPosY = 0;
        this.type = "";
        Antecesores = new ArrayList<>();
    }
    
    public Element(String name){
        this.Name = name;
        this.cPosX = 0;
        this.cPosY = 0;
        this.type="";
        Antecesores = new ArrayList<>();
    }
    
    
}
