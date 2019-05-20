package Vista;

import Modelo.BPMNModel;
import Modelo.Element;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;

public class PanelBPMN extends JPanel {

    /*
        Canvas sobre el cual se dibuja el modelo BPMN
     */
    //Dimensiones de pantalla
    int ScreenWidth;
    int ScreenHeight;

    //Esta variable maneja un tamaño unitario obtenido de una división de la dimensión de la pantalla dividido sobre un cierto numero
    //  de esta forma hacemos que el tamaño de los Elementos gráficos sea acorde a la dimensión de la pantalla
    int radio;

    int[] breaks; //Número de quiebres en el modelo gráfico
    String ElementSelected = ""; //Al arrastrar el mouse esta variable contiene el nombre del elemento que se esta arrastrando
    public HashMap<String, Element> Elements = new HashMap<>(); //Mapa de los elementos gráficos a dibujar
    HashMap<String, Color> gatewaysColors = new HashMap<>(); //Mapa que almacena una compuerta de apertura y dibujar sus cierrs del mismo color
    public BPMNModel BPMN;

    HashMap<String, String> lineMode = new HashMap<>(); //Mapa con el que se guarda el modo en el que un elemento,antecesor estan unidos mediante la linea; el modo 1 indica la union normal, el modo 2 indica la union en la parte de arriba del elemento, modo 3 parte derecha del elemento, modo 4 abajo del elemento, si el modo no es un numero entero, entonces son unas coordenadas separadas por ,
    String LineSelected = ""; //Al arrastrar el mouse esta variable contiene el flujo de la flecha que se esta moviendo: Elemento, antecesor o antecesor,Elemento
    // Constructor

    public PanelBPMN(int width, int height, HashMap<String, Element> elements, BPMNModel bpmn, int[] breaks, HashMap<String, String> lineMode) {
        //Tomar dimensiones de la pantalla e inicializar variables
        Elements = elements;
        ScreenWidth = width;
        ScreenHeight = height;
        radio = ScreenWidth / 28;
        ElementSelected = "";
        this.lineMode = lineMode;
        BPMN = bpmn;
        this.breaks = breaks;
        gatewaysColors = new HashMap<>();
        setBackground(new Color(255, 255, 255));
        setSize(ScreenWidth, ScreenHeight);
        //Se procesan los elementos gráficos para mostrarlos de forma correcta, esto para diferenciar de las tareas con autoloops de las tareas normales, etc.
        List<Map.Entry<String, Element>> elems = new ArrayList(Elements.entrySet());
        for (Map.Entry<String, Element> entry : elems) {

            Element e = entry.getValue();
            String eName = e.Name;
            if (e.Name.charAt(0) == '@') {
                eName = e.Name.charAt(1) + "";
            }

            List<Map.Entry<String, ArrayList<Element>>> antes = new ArrayList(e.Antecesores.entrySet());
            for (Map.Entry<String, ArrayList<Element>> registro : antes) {
                String antecesor = registro.getKey();
                if (antecesor.charAt(0) == '@') {
                    antecesor = antecesor.charAt(1) + "";
                }
                // lineMode.put(eName + "," + antecesor, "1"); //definir modo en el que se encuentra unido el antecesor al elemento eName por defecto
                //lineMode.put(antecesor + "," + eName, "3"); //definir modo en el que se encuentra unido Elemento  al elemento eName por defecto
                Element a = Elements.get(antecesor);
                if (a != null) {
                    if (registro.getValue().size() > 0) { //verificar que existan quiebres
                        String aName = "";
                        if (a.Name.charAt(0) == '@') {
                            aName = a.Name.charAt(1) + "";
                        } else {
                            aName = a.Name;
                        }
                        ArrayList<Element> quiebres = registro.getValue();
                        List<Element> quiebresList = new ArrayList(quiebres);
                        for (Element q : quiebresList) {
                            String qName = "";
                            if (q.Name.charAt(0) == '@') {
                                qName = q.Name.charAt(1) + "";
                            } else {
                                qName = q.Name;
                            }

                            this.Elements.put(qName, q);
                        }
                    }
                }
            }

        }

        //Declaración de los eventos del mouse
        this.addMouseListener(new MouseListener() {
            @Override
            public void mousePressed(MouseEvent me) {
                clickAt(me.getX(), me.getY());
            }

            @Override
            public void mouseReleased(MouseEvent me) {
                releaseMouse(me.getX(), me.getY());
                ElementSelected = "";
            }

            @Override
            public void mouseEntered(MouseEvent me) {
            }

            public void mouseExited(MouseEvent me) {
            }

            @Override
            public void mouseClicked(MouseEvent me) {
                //Al dar clic con el mouse se identifica si se toco algún elemento
                clickAt(me.getX(), me.getY());
                if (me.getClickCount() == 2 && !me.isConsumed()) { //Si se realizó un doble clic se verifica si se creo o eliminó un quiebre
                    me.consume();
                    doubleClick(me.getX(), me.getY());
                } else {
                    if (me.getButton() == MouseEvent.BUTTON3) { //right click
                        rightClick();
                    }

                }

            }
        });
        //Declaración de evento mouse
        this.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) { //Al arrastrar
                dragElementSelected(e.getX(), e.getY());
            }

            @Override
            public void mouseMoved(MouseEvent e) {
            }
        });

    }

    /**
     * Procedimiento que realiza el pintado de los Elementos gráficos
     *
     * @param g
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);

        for (Map.Entry<String, Element> entry : Elements.entrySet()) {
            Element e = entry.getValue();

            if (e.Hide) {
                continue;
            }

            String eName = e.Name;
            if (e.Name.charAt(0) == '@') {
                eName = e.Name.charAt(1) + "";
            }

            //En base al tipo del elemento se realizan diferentes pintados, ya sea evento o tarea o autoloop o incluso quiebres, o compuertas
            if (e.type.equals("Start")) {
                g.setColor(Color.green);
                g.drawOval(e.cPosX, e.cPosY, radio, radio);
                g.setColor(Color.black);
                g.drawString("Start", e.cPosX + (radio / 3), e.cPosY + (radio / 2));
            } else if (e.type.equals("End")) {
                g.setColor(Color.red);
                g.drawOval(e.cPosX, e.cPosY, radio, radio);
                g.setColor(Color.black);
                g.drawString("End", e.cPosX + (radio / 3), e.cPosY + (radio / 2));
            } else if (e.type.equals("Task")) {
                g.setColor(Color.black);
                //g.drawOval(e.cPosX, e.cPosY, radio, radio);
                g.drawRoundRect(e.cPosX, e.cPosY, radio, radio, radio / 2, radio / 2);
                g.drawString(e.Name, e.cPosX + (radio / 2), e.cPosY + (radio / 2));
            } else if (e.type.equals("Autoloop")) {
                g.setColor(Color.black);
                g.fillOval(e.cPosX, e.cPosY, radio, radio);
                g.setColor(Color.white);
                g.drawString(e.Name, e.cPosX + (radio / 2), e.cPosY + (radio / 2));

            } else if (e.type.equals("Break")) {
                g.setColor(Color.black);
                //g.fillOval(e.cPosX - (radio / 16), e.cPosY - (radio / 16), radio / 4, radio / 4);
                g.fillOval(e.cPosX- ((radio/4)/2), e.cPosY - ((radio/4)/2), radio / 4, radio / 4);
                g.setColor(Color.white);
                //g.drawString("", e.cPosX + (radio / 2), e.cPosY + (radio / 2));

            } else if (e.type.equals("Gateway")) {

                //obtencion de color de compuerta
                Color color = null;
                String name = ((e.Name.charAt(0) == 'O') ? e.Name.substring(1, e.Name.indexOf("C")) : e.Name.substring(0, e.Name.length() - 1));
                if (!gatewaysColors.containsKey(name)) {
                    color = new Color((int) (Math.random() * 254), (int) (Math.random() * 254), (int) (Math.random() * 254));
                    gatewaysColors.put(name, color);
                } else {
                    color = gatewaysColors.get(name);
                }

                g.setColor(color); //se asigna el color a la compuerta

                //g.fillRoundRect(e.cPosX, e.cPosY, radio, radio, radio / 2, radio / 2);
                if (BPMN.Gor.contains(e.Name)) {
                    drawOr(g, e.cPosX + (radio / 2), e.cPosY + (radio / 2));
                } else if (BPMN.Gand.contains(e.Name)) {
                    drawAnd(g, e.cPosX + (radio / 2), e.cPosY + (radio / 2));
                } else if (BPMN.Gxor.contains(e.Name)) {
                    drawXor(g, e.cPosX + (radio / 2), e.cPosY + (radio / 2));
                }

                //g.drawString(e.Name, e.cPosX + (radio / 2) - (radio / 4), e.cPosY + (radio / 2));
                //Obtencion de color de texto de la compuerta (para colores fuertes se utiliza color blanco de texto, en caso contrario negro)
                /*if (color.getRed() <= 255 / 2 && color.getGreen() <= 255 / 2 && color.getBlue() <= 255 / 2) {
                    g.setColor(Color.white);
                } else {
                    g.setColor(Color.black);
                }
                //Set texto de la compuerta
                g.drawString(e.Name, e.cPosX + (radio / 2) - (radio / 4), e.cPosY + (radio / 2));
                 */
            }

            //Dibujar arcos y quiebres
            if (!e.Antecesores.isEmpty()) {
                g.setColor(Color.black);
                Graphics2D g2 = (Graphics2D) g;
                g2.setStroke(new BasicStroke(2));
                for (Map.Entry<String, ArrayList<Element>> registro : e.Antecesores.entrySet()) {

                    String aName = registro.getKey();
                    if (aName.charAt(0) == '@') {
                        aName = aName.charAt(1) + "";
                    }
                    Element a = Elements.get(aName);
                    
                    if (a != null) {
                        if (a.Hide) {
                            continue;
                        }
                        //Aqui se realiza la flecha entre el antecesor,Elemento
                        //Primero calculamos los 4 puntos del Elemento
                        //Obtener los 4 puntos del Elemento, izq, arr, derech, abaj,
                        int izqX = e.cPosX;
                        int izqY = e.cPosY + (radio / 2);
                        int arrX = e.cPosX + (radio / 2);
                        int arrY = e.cPosY;
                        int derX = e.cPosX + radio;
                        int derY = e.cPosY + (radio / 2);
                        int abaX = e.cPosX + (radio / 2);
                        int abaY = e.cPosY + radio;
                        //Definimos en que lado debe dibujarse la linea
                        int xFinal = 0, yFinal = 0;

                        //Obtenemos el modo en el que se encuentra unido el antecesor al elemento
                        String modeString = this.lineMode.get(eName + "," + aName);

                        String[] modeVals = modeString.split(",");
                        if (modeVals.length == 1) { //Verificamos que no exista una coordenada, entonces es un modo, por lo que se selecciona su posicion
                            int modeAntecesorElemento = Integer.parseInt(modeString);

                            //Definimos en que lado debe dibujarse la linea
                            switch (modeAntecesorElemento) {
                                case 1:
                                    xFinal = izqX;
                                    yFinal = izqY;
                                    break;
                                case 2:
                                    xFinal = arrX;
                                    yFinal = arrY;
                                    break;
                                case 3:
                                    xFinal = derX;
                                    yFinal = derY;
                                    break;
                                case 4:
                                    xFinal = abaX;
                                    yFinal = abaY;
                                    break;
                            }
                        } else { //Si el valor en lineMode es una coordenada entonces simplemente asignarla
                            xFinal = Integer.parseInt(modeVals[0]);
                            yFinal = Integer.parseInt(modeVals[1]);
                        }

                        //Aqui se realiza la flecha entre el Elemento,antecesor
                        //Primero calculamos los 4 puntos del Elemento
                        //Obtener los 4 puntos del Elemento, izq, arr, derech, abaj,
                        int AizqX = a.cPosX;
                        int AizqY = a.cPosY + (radio / 2);
                        int AarrX = a.cPosX + (radio / 2);
                        int AarrY = a.cPosY;
                        int AderX = a.cPosX + radio;
                        int AderY = a.cPosY + (radio / 2);
                        int AabaX = a.cPosX + (radio / 2);
                        int AabaY = a.cPosY + radio;
                        //Obtenemos el modo en el que se encuentra unido el antecesor al elemento
                        int x1 = 0, y1 = 0;

                        String modeElementoAntecesorString = this.lineMode.get(aName + "," + eName);
                        String[] modeElementoAntecesorVals = modeElementoAntecesorString.split(",");

                        if (modeElementoAntecesorVals.length == 1) {
                            int modeElementoAntecesor = Integer.parseInt(modeElementoAntecesorString);

                            switch (modeElementoAntecesor) {//Verificamos que no exista una coordenada, entonces es un modo, por lo que se selecciona su posicion
                                case 1:
                                    x1 = AizqX;
                                    y1 = AizqY;
                                    break;
                                case 2:
                                    x1 = AarrX;
                                    y1 = AarrY;
                                    break;
                                case 3:
                                    x1 = AderX;
                                    y1 = AderY;
                                    break;
                                case 4:
                                    x1 = AabaX;
                                    y1 = AabaY;
                                    break;
                            }
                        } else {//Si el valor en lineMode es una coordenada entonces simplemente asignarla
                            x1 = Integer.parseInt(modeElementoAntecesorVals[0]);
                            y1 = Integer.parseInt(modeElementoAntecesorVals[1]);
                        }
                        if (registro.getValue().size() > 0) { //verificar que existan quiebres
                            ArrayList<Element> quiebres = registro.getValue();
                            //Dibujado de quiebres
                            for (int i = 0; i < quiebres.size(); i++) {
                                int x2 = quiebres.get(i).cPosX, y2 = quiebres.get(i).cPosY;
                                g2.drawLine(x1, y1, x2, y2);
                                x1 = x2;
                                y1 = y2;
                            }
                            drawArrowLine(g2, x1, y1, xFinal, yFinal, ScreenWidth / 300, ScreenWidth / 300);
                        } else {
                            drawArrowLine(g2, x1, y1, xFinal, yFinal, ScreenWidth / 300, ScreenWidth / 300);
                        }
                    }

                }
                g2.setStroke(new BasicStroke(0));
            }
        }
    }

    public double distancia(int x1, int y1, int x2, int y2) {
        return Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow(y2 - y1, 2));
    }

    /**
     * Procedimiento que se ejecuta cada que se da un clic, se verifica si se
     * seleccionó un elemento del modelo gráfico
     *
     * @param x
     * @param y
     */
    public void clickAt(int x, int y) { //Dada una posición x, y, verificar si se dio clic a dentro del radio de un elemento
        ElementSelected = "";
        for (Map.Entry<String, Element> entry : Elements.entrySet()) {
            Element e = entry.getValue(); //get the element
            if (e.Hide) {
                continue;
            }
            String eName = e.Name;
            
            if (e.Name.charAt(0) == '@') {
                eName = e.Name.charAt(1) + "";
            }
            
            if(e.type.equals("Break")){
                if (x <= e.cPosX + ((radio/4)/2)
                        && x >= e.cPosX - ((radio/4)/2) 
                        && y >= e.cPosY - ((radio/4)/2)
                        && y <= e.cPosY + ((radio/4)/2)){
                    ElementSelected = eName;
                    break;
                    
                }
                
                //g.fillOval(e.cPosX - ((radio/4)/2), e.cPosY - ((radio/4)/2), radio / 4, radio / 4);
            }
            
            if (x <= (e.cPosX + radio) && y <= (e.cPosY + radio) && x >= e.cPosX && y >= e.cPosY) {
                ElementSelected = eName;
                break;
            }

            //Verificar si se dio clic en una Flecha
            //Recorrer los antecesores de este elemento
            for (Map.Entry<String, ArrayList<Element>> registro : e.Antecesores.entrySet()) {
                String aName = registro.getKey();
                if (aName.charAt(0) == '@') {
                    aName = aName.charAt(1) + "";
                }
                Element a = Elements.get(aName);

                if (a != null) {

                    String modeAntecesorElementoString = this.lineMode.get(eName + "," + aName);
                    String[] modeAntecesorElementoVals = modeAntecesorElementoString.split(",");
                    if (modeAntecesorElementoVals.length == 1) {

                        //Primero calculamos los 4 puntos del Elemento
                        //Obtener los 4 puntos del Elemento, izq, arr, derech, abaj,
                        int izqX = e.cPosX;
                        int izqY = e.cPosY + (radio / 2);
                        int arrX = e.cPosX + (radio / 2);
                        int arrY = e.cPosY;
                        int derX = e.cPosX + radio;
                        int derY = e.cPosY + (radio / 2);
                        int abaX = e.cPosX + (radio / 2);
                        int abaY = e.cPosY + radio;

                        //Obtenemos el modo en el que se encuentra unido el antecesor al elemento
                        int modeAntecesorElemento = Integer.parseInt(modeAntecesorElementoString);

                        //Definimos en que lado debe dibujarse la linea
                        int xFinal = 0, yFinal = 0;
                        switch (modeAntecesorElemento) {
                            case 1:
                                xFinal = izqX;
                                yFinal = izqY;
                                break;
                            case 2:
                                xFinal = arrX;
                                yFinal = arrY;
                                break;
                            case 3:
                                xFinal = derX;
                                yFinal = derY;
                                break;
                            case 4:
                                xFinal = abaX;
                                yFinal = abaY;
                                break;
                        }

                        if (x <= (xFinal + radio / 4) && y <= (yFinal + radio / 4) && x >= xFinal - (radio / 4) && y >= yFinal - (radio / 4)) {
                            LineSelected = eName + "," + aName;
                            break;
                        }
                    }

                    //Aqui se realiza la flecha entre el Elemento,antecesor
                    //Primero calculamos los 4 puntos del Elemento
                    //Obtener los 4 puntos del Elemento, izq, arr, derech, abaj,
                    int AizqX = a.cPosX;
                    int AizqY = a.cPosY + (radio / 2);
                    int AarrX = a.cPosX + (radio / 2);
                    int AarrY = a.cPosY;
                    int AderX = a.cPosX + radio;
                    int AderY = a.cPosY + (radio / 2);
                    int AabaX = a.cPosX + (radio / 2);
                    int AabaY = a.cPosY + radio;

                    String modeElementoAntecesorString = this.lineMode.get(aName + "," + eName);

                    String[] modeElementoAntecesorVals = modeElementoAntecesorString.split(",");

                    if (modeElementoAntecesorVals.length == 1) {
                        //Obtenemos el modo en el que se encuentra unido el antecesor al elemento
                        int modeElementoAntecesor = Integer.parseInt(modeElementoAntecesorString);
                        int x1 = 0, y1 = 0;
                        switch (modeElementoAntecesor) {
                            case 1:
                                x1 = AizqX;
                                y1 = AizqY;
                                break;
                            case 2:
                                x1 = AarrX;
                                y1 = AarrY;
                                break;
                            case 3:
                                x1 = AderX;
                                y1 = AderY;
                                break;
                            case 4:
                                x1 = AabaX;
                                y1 = AabaY;
                                break;
                        }

                        if (x <= (x1 + radio / 4) && y <= (y1 + radio / 4) && x >= x1 - radio / 4 && y >= y1 - radio / 4) {
                            LineSelected = aName + "," + eName;
                            break;
                        }
                    }

                }

            }

        }
    }

    /**
     * Procedimiento que se realiza al dar clic derecho se verifica si se dio
     * clic derecho en un quiebre, si es así, el quiebre se elimina
     */
    public void rightClick() {
        if (ElementSelected.contains("break")) {
            Element elementSelected = Elements.get(ElementSelected);
            for (Map.Entry<String, Element> entry : Elements.entrySet()) {
                Element e = entry.getValue();
                for (Map.Entry<String, ArrayList<Element>> ant : e.Antecesores.entrySet()) {
                    ant.getValue().remove(elementSelected);
                }
            }
            Elements.remove(ElementSelected);
            repaint();
        }
    }

    /**
     * Procedimiento que se ejecuta al realizar doble clic Se verifica si se dio
     * doble clic en los límites de un arco, de ser así se crea un quiebre Se
     * verifica también si se dió doble clic en un quiebre, entonces el quiebre
     * es ocultado
     *
     * @param x
     * @param y
     */
    public void doubleClick(int x, int y) {
        if (ElementSelected.contains("break")) {
            Elements.remove(ElementSelected);
            repaint();
            return;
        }
        //Verificar si es un quiebre
        boolean breakhappened = false;
        //Verificar si dio clic en una linea
        List<Map.Entry<String, Element>> entries = new ArrayList(Elements.entrySet());
        for (Map.Entry<String, Element> entry : entries) {
            Element e = entry.getValue(); //get the element
            String eName = "";
            if (e.Name.charAt(0) == '@') {
                eName = e.Name.charAt(1) + "";
            } else {
                eName = e.Name;
            }

            for (Map.Entry<String, ArrayList<Element>> registro : e.Antecesores.entrySet()) {
                String antecesor = registro.getKey();
                Element a = null;
                if (antecesor.charAt(0) == '@') {
                    a = Elements.get(antecesor.charAt(1) + "");
                } else {
                    a = Elements.get(antecesor);
                }
                if (a != null) {
                    if (registro.getValue().size() > 0) { //verificar que existan quiebres
                        int x1 = a.cPosX + radio;
                        int y1 = a.cPosY + (radio / 2);

                        for (Element quiebre : registro.getValue()) {
                            int x2 = quiebre.cPosX, y2 = quiebre.cPosY + (radio / 2);

                            if (x1 >= x2) {
                                if (x <= x1 && x >= x2) {
                                    if (y1 >= y2) {
                                        if (y <= y1 && y >= y2) {
                                            //quiebre
                                            realizarQuiebre(eName, antecesor, x, y);
                                            breakhappened = true;
                                            break;
                                        }

                                    } else {
                                        if (y <= y2 && y >= y1) {
                                            //quiebre
                                            realizarQuiebre(eName, antecesor, x, y);
                                            breakhappened = true;
                                            break;
                                        }
                                    }
                                }
                            } else {
                                if (x <= x2 && x >= x1) {
                                    if (y1 >= y2) {
                                        if (y <= y1 && y >= y2) {
                                            //quiebre
                                            realizarQuiebre(eName, antecesor, x, y);
                                            breakhappened = true;
                                            break;
                                        }

                                    } else {
                                        if (y <= y2 && y >= y1) {
                                            //quiebre
                                            realizarQuiebre(eName, antecesor, x, y);
                                            breakhappened = true;
                                            break;
                                        }
                                    }
                                }
                            }

                            x1 = x2;
                            y1 = y2;
                        }

                        int x2 = e.cPosX;
                        int y2 = e.cPosY + (radio / 2);
                        if (x1 >= x2) {
                            if (x <= x1 && x >= x2) {
                                if (y1 >= y2) {
                                    if (y <= y1 && y >= y2) {
                                        //quiebre
                                        realizarQuiebre(eName, antecesor, x, y);
                                        breakhappened = true;
                                        break;
                                    }

                                } else {
                                    if (y <= y2 && y >= y1) {
                                        //quiebre
                                        realizarQuiebre(eName, antecesor, x, y);
                                        breakhappened = true;
                                        break;
                                    }
                                }
                            }
                        } else {
                            if (x <= x2 && x >= x1) {
                                if (y1 >= y2) {
                                    if (y <= y1 && y >= y2) {
                                        //quiebre
                                        realizarQuiebre(eName, antecesor, x, y);
                                        breakhappened = true;
                                        break;
                                    }

                                } else {
                                    if (y <= y2 && y >= y1) {
                                        //quiebre
                                        realizarQuiebre(eName, antecesor, x, y);
                                        breakhappened = true;
                                        break;
                                    }
                                }
                            }
                        }

                    } else {

                        //verificar limite
                        int x1 = a.cPosX + radio;
                        int y1 = a.cPosY + (radio / 2);
                        int x2 = e.cPosX;
                        int y2 = e.cPosY + (radio / 2);

                        if (x1 >= x2) {
                            if (x <= x1 && x >= x2) {
                                if (y1 >= y2) {
                                    if (y <= y1 && y >= y2) {
                                        //quiebre
                                        realizarQuiebre(eName, antecesor, x, y);
                                        breakhappened = true;
                                        break;
                                    }

                                } else {
                                    if (y <= y2 && y >= y1) {
                                        //quiebre
                                        realizarQuiebre(eName, antecesor, x, y);
                                        breakhappened = true;
                                        break;
                                    }
                                }
                            }
                        } else {
                            if (x <= x2 && x >= x1) {
                                if (y1 >= y2) {
                                    if (y <= y1 && y >= y2) {
                                        //quiebre
                                        realizarQuiebre(eName, antecesor, x, y);
                                        breakhappened = true;
                                        break;
                                    }

                                } else {
                                    if (y <= y2 && y >= y1) {
                                        //quiebre
                                        realizarQuiebre(eName, antecesor, x, y);
                                        breakhappened = true;
                                        break;
                                    }
                                }
                            }
                        }

                    }
                    //Linea normal drawArrowLine(g, a.cPosX + radio), a.cPosY + (radio / 2), e.cPosX, e.cPosY + (radio / 2), ScreenWidth / 300, ScreenWidth / 300);
                }
            }
            if (breakhappened) {
                break;
            }
        }
    }

    public void releaseMouse(int x, int y) {
        if (!LineSelected.equals("")) { //Mover flecha
            String[] vals = LineSelected.split(",");
            Element e = Elements.get(vals[0]);
            Element a = Elements.get(vals[1]);

            //Aqui se realiza la flecha entre el antecesor,Elemento
            //Primero calculamos los 4 puntos del Elemento
            //Obtener los 4 puntos del Elemento, izq, arr, derech, abaj,
            int izqX = e.cPosX;
            int izqY = e.cPosY + (radio / 2);
            int arrX = e.cPosX + (radio / 2);
            int arrY = e.cPosY;
            int derX = e.cPosX + radio;
            int derY = e.cPosY + (radio / 2);
            int abaX = e.cPosX + (radio / 2);
            int abaY = e.cPosY + radio;

            //Obtener distancias entre dos puntos de x,y y los 4 puntos del elemento
            double d1 = distancia(x, y, izqX, izqY);
            double d2 = distancia(x, y, arrX, arrY);
            double d3 = distancia(x, y, derX, derY);
            double d4 = distancia(x, y, abaX, abaY);

            if (d1 < d2 && d1 < d3 && d1 < d4) {
                //d1 menor
                lineMode.put(LineSelected, "1");
            } else if (d2 < d1 && d2 < d3 && d2 < d4) {
                //d2 menor
                lineMode.put(LineSelected, "2");
            } else if (d3 < d1 && d3 < d2 && d3 < d4) {
                //d3 menor
                lineMode.put(LineSelected, "3");
            } else {
                //d4 menor
                lineMode.put(LineSelected, "4");
            }
            repaint();

            LineSelected = "";
        }

    }

    /**
     * Procedimiento que crea un Elemento que representa un quiebre, asigna sus
     * propiedades y posiciones en el canvas
     *
     * @param eName Nombre del elemento
     * @param antecesor Nombre del antecesor al que pertenece el quiebre
     * @param x
     * @param y
     */
    public void realizarQuiebre(String eName, String antecesor, int x, int y) {
        Element elemento = new Element();
        elemento.Name = "break" + breaks[0];
        elemento.type = "Break";
        elemento.cPosX = x;
        elemento.cPosY = y;
        this.Elements.put("break" + breaks[0], elemento);
        ArrayList<Element> br = Elements.get(eName).Antecesores.get(antecesor);
        if (br != null) {
            br.add(elemento);
        } else {
            ArrayList<Element> br2 = new ArrayList<>();
            br2.add(elemento);
            Elements.get(eName).Antecesores.put(antecesor, br2);
        }
        breaks[0]++;
        repaint();
    }

    /**
     * Procedimiento que se ejecuta al arrastrar un elemento, se actualiza su
     * posición x y y, para visualizar un arrastre del mismo en la pantalla
     *
     * @param x
     * @param y
     */
    public void dragElementSelected(int x, int y) {
        if (!LineSelected.equals("")) {
            lineMode.put(LineSelected, x + "," + y);
            repaint();
            return;
        }

        if (!ElementSelected.equals("")) {
            Element e = Elements.get(ElementSelected);
            if (e != null) {
                if (ElementSelected.contains("break")) {
                    Elements.get(ElementSelected).cPosX = x - (radio / 16); //reasignar posicion al arrastrar mouse, para que el elemento quede en el centro del cursor
                    Elements.get(ElementSelected).cPosY = y - (radio / 16);
                } else {
                    Elements.get(ElementSelected).cPosX = x - (radio / 2); //reasignar posicion al arrastrar mouse, para que el elemento quede en el centro del cursor
                    Elements.get(ElementSelected).cPosY = y - (radio / 2);
                }
                repaint();
            }

        }
    }

    /**
     * Función que crea un diamante en la pos x,y
     *
     * @param x
     * @param y
     * @return
     */
    private Polygon drawDiamond(int x, int y) {
        Polygon p = new Polygon();
        p.addPoint(x, y - (radio / 2));
        p.addPoint(x - (radio / 2), y);
        p.addPoint(x, y + (radio / 2));
        p.addPoint(x + (radio / 2), y);
        return p;
    }

    /**
     * Procedimiento que dibuja una compuerta Xor en la posicion dada
     *
     * @param g
     * @param x
     * @param y
     */
    private void drawXor(Graphics g, int x, int y) {
        Polygon p = drawDiamond(x, y);
        g.fillPolygon(p);
        g.setColor(Color.white);
        g.drawLine(x - (radio / 6), y - (radio / 6), x + (radio / 6), y + (radio / 6));
        g.drawLine(x + (radio / 6), y - (radio / 6), x - (radio / 6), y + (radio / 6));
    }

    /**
     * Procedimiento que dibuja una compuerta OR en al posición dada
     *
     * @param g
     * @param x
     * @param y
     */
    private void drawOr(Graphics g, int x, int y) {
        Polygon p = drawDiamond(x, y);
        g.fillPolygon(p);
        g.setColor(Color.white);
        g.drawOval(x - (radio / 4), y - (radio / 4), radio / 2, radio / 2);
    }

    /**
     * Procedimiento que dibuja una compuerta And en la posición dada
     *
     * @param g
     * @param x
     * @param y
     */
    private void drawAnd(Graphics g, int x, int y) {
        Polygon p = drawDiamond(x, y);
        g.fillPolygon(p);
        g.setColor(Color.white);
        g.drawLine(x, y - (radio / 4), x, y + (radio / 4));
        g.drawLine(x - (radio / 4), y, x + (radio / 4), y);
    }

    /**
     * procedimiento obtenido de stackoverflow
     * https://stackoverflow.com/questions/2027613/how-to-draw-a-directed-arrow-line-in-java
     */
    private void drawArrowLine(Graphics g, int x1, int y1, int x2, int y2, int d, int h) {
        int dx = x2 - x1, dy = y2 - y1;
        double D = Math.sqrt(dx * dx + dy * dy);
        double xm = D - d, xn = xm, ym = h, yn = -h, x;
        double sin = dy / D, cos = dx / D;

        x = xm * cos - ym * sin + x1;
        ym = xm * sin + ym * cos + y1;
        xm = x;

        x = xn * cos - yn * sin + x1;
        yn = xn * sin + yn * cos + y1;
        xn = x;

        int[] xpoints = {x2, (int) xm, (int) xn};
        int[] ypoints = {y2, (int) ym, (int) yn};

        g.drawLine(x1, y1, x2, y2);
        g.fillPolygon(xpoints, ypoints, 3);
    }

}
