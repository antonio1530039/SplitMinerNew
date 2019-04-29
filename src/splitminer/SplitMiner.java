package splitminer;

import Vista.*;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SplitMiner extends HttpServlet{
    
    public void doGet(HttpServletRequest req, HttpServletResponse response) throws IOException{
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        
        
        out.println("<html>");
        out.println("<body>");
        out.println("<applet code='Vista.ProcessViewer.class' width='350' height='350'>");
        out.println("<param name='message' value='Welcome to the world of Applet'>>");
        out.println("</applet>");
        out.println("</body>");
        out.println("</html>");
        
        /*
        
        <!DOCTYPE html>

<html>

<head>

<meta charset="ISO-8859-1">

<title>Insert title here</title>

</head>

<body>

<applet code="AppletDemo.class"width="350"height="350">

<param name="message"value="Welcome to the world of Applet">

</applet>

</body>

</html>
        
        
        */
        
        
        
       // ProcessViewer processViewer = new ProcessViewer();
    }
    
    

    public static void main(String[] args) {
        /*//P1.txt NOTATION: a AND{  XOR{  c, h}, b XOR{  e, g}} d f
        final String filename = "P1.txt";
        double umbral = 0.4; //descarta edges con frecuencia menor a este umbral he manejado hasta 25
        LinkedHashMap<Integer, ArrayList<Character>> tracesList; //lista de trazas
        LinkedHashMap<String, Integer> WFG = new LinkedHashMap<>(); //Grafo
        BPMNModel BPMN = new BPMNModel(); //Modelo BPMN

        FilesManagement f = new FilesManagement(BPMN);
        ///////
        System.out.println("PASO 1: LEER TRAZAS DEL ARCHIVO DE ENTRADA '" + filename + "' E IDENTIFICAR TAREAS.");

        try {
            if (filename.endsWith(".txt")) {
                tracesList = f.readDataInputTrazas(filename);
            } else if (filename.endsWith(".csv")) {
                tracesList = f.readDataInput(filename);
            } else {
                System.out.println("El tipo de archivo de entrada no es valido...EXIT");
                return;
            }
        } catch (Exception e) {
            System.out.println("El archivo '" + filename + "' no se puede abrir. Exit");
            return;
        }

        System.out.println("\t3. Mostrando TRAZAS IDENTIFICADAS  en el archivo '" + filename + "'.");
        for (Map.Entry<Integer, ArrayList<Character>> entry : tracesList.entrySet()) {
            System.out.println("\t\t" + entry.getKey() + " - " + entry.getValue());
        }

        
        
        ///////
        System.out.println("\n");
        System.out.println("PASO2: INCIANDO LA CONSTRUCCION DEL GRAFO QUE MODELA EL CONJUNTO DE TRAZAS.\n");

        GenerarGrafo generarGrafo = new GenerarGrafo();

        generarGrafo.computeGraph(tracesList, WFG);
 
        
        ///////
        System.out.println("\nPASO 3: PREPROCESAMIENTO DEL GRAFO");

        PreProcesarGrafo preprocesarGrafo = new PreProcesarGrafo(BPMN, WFG, tracesList);

        
        
        Filtering filtering = new Filtering(BPMN, umbral, WFG, generarGrafo.firsts, generarGrafo.lasts);

        
        /////////
        System.out.println("\nPASO 4: CONSTRUCCION DEL MODELO BPMN");

        SplitsFinder crearModelo = new SplitsFinder(BPMN, generarGrafo.firsts, generarGrafo.lasts, WFG, preprocesarGrafo.parallelRelations);
        
        /////////

        System.out.println("\nPASO 5: POST-PROCESAMIENTO");
      
        //g1.postProcesamiento(BPMN);
        
        PostProcesarGrafo postprocesamiento = new PostProcesarGrafo(BPMN, WFG, preprocesarGrafo.autoLoops);
        
        ProcessViewer processViewer = new ProcessViewer();
        
        System.out.println("Notacion al final: " + postprocesamiento.notation);*/
        
        ProcessViewer processViewer = new ProcessViewer();

    }
    
}
