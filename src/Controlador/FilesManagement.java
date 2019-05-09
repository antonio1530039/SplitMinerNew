package Controlador;

import Modelo.BPMNModel;
import java.io.*;
import java.util.*;

//Esta clase lee el archivo.csv del log de eventos y construye el conjunto de trazas de la forma
//(a,b,c,d)
//(a,b,e,f)
//(a,b,c)
public class FilesManagement {

    BPMNModel BPMN;
    boolean Filtering = false;
    boolean Repaired = false;
    int L;
    int R;
    int K;
    double Umbral;
    public StringBuilder contextOutput;

    public LinkedHashMap<String, Character> ActivityList;

    public FilesManagement(BPMNModel bpmn, boolean filtering, boolean repaired, int l, int r, int k, double umbral, StringBuilder contextOutput) {
        BPMN = bpmn;
        Filtering = filtering;
        Repaired = repaired;
        L = l;
        R = r;
        K = k;
        Umbral = umbral;
        this.contextOutput = contextOutput;
    }

    public Object[] readDataInput(String filename) throws Exception {
// public LinkedHashMap<Integer, ArrayList<Character>> readDataInput(String filename) throws Exception {
        ArrayList<ArrayList<String>> bitacora = new ArrayList<ArrayList<String>>();

        int index;
        int i = 0;
        String inputLine = null;
        
      
        
        BufferedReader rd = new BufferedReader(new FileReader(new File(filename)));

        if (filename.contains(".csv")) {
            inputLine = rd.readLine();                    //first line is the header, next lines are the data
            if (inputLine != null) {                      //determinar #columnas y crear ese nuemero de listas (vectores para los valores por columna)
                //String [] temp = inputLine.split(";");

                String[] temp = inputLine.split(";|,");// solo utilizar en caso de tener files con espacio
                for (i = 0; i < temp.length; i++) {
                    bitacora.add(new ArrayList<String>());
                }
            }

            //para todas las lineas subsecuentes, leer los datos por columnas
            do {

                String[] temp = inputLine.split(";|,");
                for (i = 0; i < temp.length; i++) {
                    bitacora.get(i).add(temp[i].trim());
                }

            } while ((inputLine = rd.readLine()) != null);
        } else {
            System.out.println("\tEste modulo solo procesa archivos CSV");
            i = -1;
        }

        rd.close();

        //procede a obtener la lista de actividades
        int numCols = i;

        LinkedHashMap<String, Character> activityList = new LinkedHashMap<String, Character>();
        char value;
        //ascii code in literal a    
        int s = 97;
        String strVal = null;
        ArrayList<String> listValues = null;

        int k = 0;
        while (!bitacora.get(k).get(0).equals("Activity") && (k < numCols)) {
            k++;
        }

        if (k == numCols) {
            System.out.println("\tLos datos de entrada no contienen una columna 'Activity'");
            return null;
        }

        listValues = bitacora.get(k);   //recupera la lista 'Activity' y su tamaño
        int sizeList = listValues.size();

        for (int j = 1; j < sizeList; j++) {
            value = (char) (s);
            String activityName = listValues.get(j);
            if (!(activityList.containsKey(activityName))) {
                //System.out.println(activityName+"  ID:"+value);
                activityList.put(activityName, value);
                s = s + 1;
            }
        }

        System.out.println("\nSe han detectado todos los nombres de las actividades:");

        if (!Repaired) {
            activityList.put("Start", 'I');
            activityList.put("End", 'O');
        }

        //recupera el set de tasks T
        Set<Map.Entry<String, Character>> tasks = activityList.entrySet();
        int tam = activityList.size();
        Character task;

        for (Map.Entry<String, Character> entry : tasks) {
            task = entry.getValue();
            BPMN.T.add(task);
        }

        //procede a calcular la lista de trazas
        LinkedHashMap<Integer, ArrayList<Character>> tracesList = new LinkedHashMap<Integer, ArrayList<Character>>();
        ArrayList<Character> traces = new ArrayList<Character>();

        String key;
        int ID = 0, IDnext = 0;
        String colName = "";
        ArrayList<String> listCASE_ID = null;
        ArrayList<String> listACTIVITY = null;

        int tamList = bitacora.get(0).size();

        //recupera la lista de CASE_ID y ACTIVITY
        for (k = 0; k < numCols; k++) {
            if (bitacora.get(k).get(0).equals("Case ID")) {
                listCASE_ID = bitacora.get(k);
            } else if (bitacora.get(k).get(0).equals("Activity")) {
                listACTIVITY = bitacora.get(k);
            } else {
                continue;
            }
        }

        ID = Integer.parseInt(listCASE_ID.get(1));
        //recorre las listas
        for (int j = 1; j < tamList; j++) {

            IDnext = Integer.parseInt(listCASE_ID.get(j));

            if (IDnext != ID) {
                if(!Repaired){
                    traces.add(0, 'I');
                    traces.add('O');
                }
                
                tracesList.put(ID, traces);
                ID = IDnext;
                traces = new ArrayList<Character>();
            }

            key = listACTIVITY.get(j);
            value = activityList.get(key);
            traces.add(value);
        }

        tracesList.put(ID, traces);  //la ultima
        this.ActivityList = activityList;
        System.out.println("\n\t1. Trazas detectadas: '" + tracesList.size() + "' trazas");

        if (Filtering) {
            LinkedHashMap<Integer, ArrayList<Character>> originalTraces = copyMap(tracesList);
            System.out.println("\n\n\tFILTRADO...");
            RepairOutliers r = new RepairOutliers();
            r.Filtering(tracesList, L, R, K, Umbral, contextOutput);
            System.out.println("");
            System.out.println("");
            System.out.println("\t4. Mostrando TRAZAS DESPUES DEL FILTRADO ");
            for (Map.Entry<Integer, ArrayList<Character>> entry : tracesList.entrySet()) {
                System.out.println("\t\t" + entry.getKey() + " - " + entry.getValue());
            }

            showDataInfo(tracesList);

            Object[] all = new Object[2];
            all[0] = originalTraces.clone();
            all[1] = tracesList;
            return all;
        }

        showDataInfo(tracesList);
        Object[] all = new Object[2];
        all[0] = tracesList;

        return all;

    }

    public String[][] showDataInfo(LinkedHashMap<Integer, ArrayList<Character>> tracesList) {

        System.out.println("\n\t2. Datos del dataset ");
        System.out.println("\t --Actividades--  ");
        for (Map.Entry<String, Character> entry1 : ActivityList.entrySet()) {
            System.out.println("\t\t" + entry1.getKey() + " - " + entry1.getValue());
        }

        List<ArrayList<Character>> uno = new ArrayList<ArrayList<Character>>(tracesList.values());
        uno.get(0);

        int min = uno.size();
        int max = -1;
        int suma = 0;
        int total = 0;

        for (Map.Entry<Integer, ArrayList<Character>> entry : tracesList.entrySet()) {
            int t = entry.getValue().size();
            total = total + t;
            if (t < min) {
                min = t;
            } else {
                if (max < t) {
                    max = t;
                }
            }

            suma = suma + t;
        }
        float average = (float) suma / tracesList.size();

        String[][] data = new String[][]{{"Activities", "" + ActivityList.size()},
        {"Traces", "" + tracesList.size()},
        {"Events", "" + total},
        {"Minimum events per trace", "" + min},
        {"Maximum events per trace", "" + max},
        {"Trace size average", "" + average}};
        System.out.println("\n\t # Actividades: " + ActivityList.size());
        System.out.println("\t # Trazas: " + tracesList.size());
        System.out.println("\t # Eventos: " + total);
        System.out.println("\t Minimo de eventos por traza: " + min);
        System.out.println("\t Maximo de eventos por traza:  " + max);
        System.out.println("\t Promedio del tamanio de traza: " + average);
        return data;

    }

    public Object[] readDataInputTrazas(String filename) throws Exception {

        LinkedHashMap<Integer, ArrayList<Character>> tracesList = new LinkedHashMap<Integer, ArrayList<Character>>();
        LinkedHashMap<String, Character> activityList = new LinkedHashMap<String, Character>();

        int ID = 0;

        String inputLine = null;
        BufferedReader rd = new BufferedReader(new FileReader(new File(filename)));

        ArrayList<Character> traza = null;

        /*PARA MANEJAR TRAZAS*/
 /*while( (inputLine = rd.readLine()) != null){
       traza = new ArrayList<Character>();
       String [] temp = inputLine.split(";");
       for (int i = 0; i < temp.length; i++){
          Character task = temp[i].trim().charAt(0);
          traza.add(task);
          activityList.put(""+task, task);
       }
       tracesList.put(ID,traza);  //la ultima
       ID++;
    } */
        //
        //CONVERTIR ACTIVIDADES A TRAZAS
        int s = 97;
        while ((inputLine = rd.readLine()) != null) {
            traza = new ArrayList<Character>();
            //String [] temp = inputLine.split(";|,| "); solo en caso de tener archivos con espacio
            String[] temp = inputLine.split(";");

            for (int i = 0; i < temp.length; i++) {

                Character task = (char) (s);
                String clave = "" + temp[i].trim();
                if (!(activityList.containsKey(clave))) {
                    activityList.put("" + temp[i].trim(), task);
                    traza.add(task);
                    s = s + 1;
                } else {
                    Character value = activityList.get(clave);

                    // System.out.println("CLAVE: "+clave+"   VALOR:"+value);
                    traza.add(value);
                }

            }
            if(!Repaired){
                traza.add(0, 'I');
                traza.add('O');
            }
            
            tracesList.put(ID, traza);  //la ultima
            ID++;

        }
        if (!Repaired) {
            activityList.put("Start", 'I');
            activityList.put("End", 'O');
        }

        this.ActivityList = activityList;
        rd.close();

        //recupera el set de tasks T
        Set<Map.Entry<String, Character>> tasks = activityList.entrySet();
        int tam = activityList.size();
        Character task;

        for (Map.Entry<String, Character> entry : tasks) {
            task = entry.getValue();
            BPMN.T.add(task);
        }

        System.out.println("\n\t1. Trazas detectadas: '" + tracesList.size() + "' trazas");

        if (Filtering) {
            LinkedHashMap<Integer, ArrayList<Character>> originalTraces = copyMap(tracesList);
            
            System.out.println("\n\n\tFILTRADO...");
            RepairOutliers r = new RepairOutliers();
            r.Filtering(tracesList, L, R, K, Umbral, contextOutput);
            System.out.println("");
            System.out.println("");
            System.out.println("\t4. Mostrando TRAZAS DESPUES DEL FILTRADO ");
            for (Map.Entry<Integer, ArrayList<Character>> entry : tracesList.entrySet()) {
                System.out.println("\t\t" + entry.getKey() + " - " + entry.getValue());
            }
            showDataInfo(tracesList);

            Object[] all = new Object[2];
            all[0] = originalTraces;
            all[1] = tracesList;
            return all;
        }

        showDataInfo(tracesList);
        Object[] all = new Object[2];
        all[0] = tracesList;

        return all;

    }
    
    public LinkedHashMap<Integer, ArrayList<Character>> copyMap(LinkedHashMap<Integer, ArrayList<Character>> map){
        LinkedHashMap<Integer, ArrayList<Character>> newMap = new LinkedHashMap<>();
        for(Map.Entry<Integer, ArrayList<Character>> entry : map.entrySet()){
            newMap.put(entry.getKey(), (ArrayList<Character>) entry.getValue().clone());
        }
        return newMap;
    }

}