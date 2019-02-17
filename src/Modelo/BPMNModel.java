package Modelo;
import splitminer.*;
import java.io.*;
import java.util.*;


public class BPMNModel {

   public LinkedList<Character> T;
   public Character i;
   public Character o;
   public LinkedHashMap<String,HashSet<String>> ANDs;
   public LinkedHashMap<String,HashSet<String>> XORs;

   public LinkedList<String> Gand;
   public LinkedList<String> Gxor;
   public LinkedList<String> Gor;
   
   public BPMNModel(){
      T = new LinkedList<Character>();
      ANDs = new LinkedHashMap<String,HashSet<String>>();
      XORs= new LinkedHashMap<String,HashSet<String>>();
      Gand = new LinkedList<String>();
      Gxor = new LinkedList<String>();
      Gor = new LinkedList<String>();
   
   }
   
}
