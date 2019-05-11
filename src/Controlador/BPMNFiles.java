package Controlador;

import Modelo.BPMNModel;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.bpmn.instance.Process;

public class BPMNFiles {

    //Instancia de modelo BPMN
    BpmnModelInstance modelInstance;
    //Mapa de elementos BPMN, la clave representa el nombre del elemento, es decir tarea ej: a, compuerta ej: X1A, etc.
    HashMap<String, BpmnModelElementInstance> Elements;
    BPMNModel BPMN;
    String Path; //Ruta de almacenamiento (donde se guardará el archivo)

    
    public BPMNFiles(LinkedHashMap<String, Integer> WFG, BPMNModel bpmn, String path) { //falta BPMNModel instance
        Elements = new HashMap<>();
        BPMN = bpmn;
        Path = path;
        buildBPMNModel(WFG);

    }

    
    /**
     * Procedimiento que crea la instancia del modelo BPMN, se crea el proceso y se agregan los elementos al modelo, así como el flujo del modelo
     * @param WFG Grafo
     */
    public void buildBPMNModel(LinkedHashMap<String, Integer> WFG) { //falta BPMNModel instance
        // Create an Empty Model
        modelInstance = Bpmn.createEmptyModel();
        Definitions definitions = modelInstance.newInstance(Definitions.class);
        //definitions.setTargetNamespace("http://camunda.org/examples");
        definitions.setTargetNamespace("xMiner");
        modelInstance.setDefinitions(definitions);
        // create the process
        Process process = createElement(definitions, "BPMNModel", Process.class);
        
        //Crear evento de inicio
        StartEvent startEvent = createElement(process, "start", StartEvent.class);
        //Crear tarea inicial
        BpmnModelElementInstance i = processElement(BPMN.i.toString(), process);
        //Flujo de secuencia desde evento inicio hasta tarea inicial
        createSequenceFlow(process, startEvent, (UserTask) i);

        for (Map.Entry<String, Integer> entry : WFG.entrySet()) {
            String keySplit[] = entry.getKey().split(",");
            String a = keySplit[0];
            String s = keySplit[1];
            
            if(a.charAt(0) == '@'){
                a = ""+a.charAt(1);
            }
            
            if(s.charAt(0) == '@'){
                s = ""+s.charAt(1);
            }
            
            //Crear elementos a y s (en caso de no existir en la lista) y agregarlos a la lista
            BpmnModelElementInstance actual = null;
            
            if (!Elements.containsKey(a)) {
                actual = processElement(a, process);
            } else {
                actual = Elements.get(a);
            }

            BpmnModelElementInstance sucesor = null;

            if (!Elements.containsKey(s)) {
                sucesor = processElement(s, process);
            } else {
                sucesor = Elements.get(s);
            }
            
            //Crear flujo de secuencia desde a hasta s
            createSequenceFlow(process, (FlowNode) actual, (FlowNode) sucesor);
        }

        EndEvent endEvent = createElement(process, "end", EndEvent.class);
        createSequenceFlow(process, (FlowNode) Elements.get(BPMN.o.toString()), endEvent);

        if (export(Path + "_" + new Date().getTime())) {
            System.out.println("File exported!");
        }
    }

    /**
     * Función que recibe un nombre de un nodo y crea el Elemento del BPMN según sea una tarea, o compuerta.
     * @param e Nombre del nodo
     * @param process Proceso BPMN
     * @return 
     */
    public BpmnModelElementInstance processElement(String e, Process process) {
        if (BPMN.T.contains(e.trim().charAt(0))) { //Es una tarea
            UserTask task = createElement(process, e, UserTask.class);
            task.setName(e);
            Elements.put(e, task);
            return task;
        } else { //Tarea
            Gateway g = null;
            if (BPMN.Gand.contains(e)) {
                //Compuerta paralela AND
                g = createElement(process, e, ParallelGateway.class);

            } else if (BPMN.Gxor.contains(e)) {
                //Compuerta exclusiva Xor
                g = createElement(process, e, ExclusiveGateway.class);
            } else if (BPMN.Gor.contains(e)) {
                //Compuerta inclusiva or
                g = createElement(process, e, InclusiveGateway.class);
            }
            Elements.put(e, g);
            return g;
        }
    }

    /**
     * Función que valida el modelo BPMN y crea un archivo .bpmn del modelo
     * @param fileName Nombre de archivo
     * @return 
     */
    public boolean export(String fileName) {
        // validate and write model to file
        Bpmn.validateModel(modelInstance);
        File file = new File(fileName + ".bpmn");
        try {
            if (file.createNewFile()) {
                Bpmn.writeModelToFile(file, modelInstance);
                return true;
            }

        } catch (IOException ex) {
            System.out.println("Error tratando de exportar el archivo");
        }
        return false;
    }

    /**
     * Función que dado un elemento padre, un id y una clase de elemento, crea el elemento BPMN y asigna sus propiedades, así como el elemento padre al que va conectado.
     * @param <T>
     * @param parentElement
     * @param id
     * @param elementClass
     * @return 
     */
    protected <T extends BpmnModelElementInstance> T createElement(BpmnModelElementInstance parentElement, String id, Class<T> elementClass) {
        T element = modelInstance.newInstance(elementClass);
        element.setAttributeValue("id", id, true);
        parentElement.addChildElement(element);
        return element;
    }

    /**
     * Función que crea un flujo de secuencia desde un Elemento from hasta un elemento to
     * @param process
     * @param from
     * @param to
     * @return 
     */
    public SequenceFlow createSequenceFlow(Process process, FlowNode from, FlowNode to) {
        String identifier = from.getId() + "-" + to.getId();
        SequenceFlow sequenceFlow = createElement(process, identifier, SequenceFlow.class);
        process.addChildElement(sequenceFlow);
        sequenceFlow.setSource(from);
        from.getOutgoing().add(sequenceFlow);
        sequenceFlow.setTarget(to);
        to.getIncoming().add(sequenceFlow);
        return sequenceFlow;
    }
}
