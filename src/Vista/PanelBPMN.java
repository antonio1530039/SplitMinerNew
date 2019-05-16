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

    HashMap<String, Integer> antecesoresMode = new HashMap<>(); //Mapa que almacena un antecesor y el modo en el que se encuentra de la forma; llave: elemento, antecesor // valor : modo
    HashMap<String, Integer> actualMode = new HashMap<>(); //Mapa que almacena un antecesor y el modo en el que se encuentra de la forma; llave: elemento, antecesor // valor : modo

    String AntecesorSelected = "";
    String LineActualSelected = "";

    // Constructor
    public PanelBPMN(int width, int height, HashMap<String, Element> elements, BPMNModel bpmn, int[] breaks) {
        //Tomar dimensiones de la pantalla e inicializar variables
        Elements = elements;
        ScreenWidth = width;
        ScreenHeight = height;
        radio = ScreenWidth / 28;
        ElementSelected = "";
        BPMN = bpmn;
        this.breaks = breaks;
        gatewaysColors = new HashMap<>();
        setBackground(new Color(255, 255, 255));
        setSize(ScreenWidth, ScreenHeight);

        //Se procesan los elementos gráficos para mostrarlos de forma correcta, esto para diferenciar de las tareas con autoloops de las tareas normales, etc.
        List<Map.Entry<String, Element>> elems = new ArrayList(Elements.entrySet());
        for (Map.Entry<String, Element> entry : elems) {
            Element e = entry.getValue();

            List<Map.Entry<String, ArrayList<Element>>> antes = new ArrayList(e.Antecesores.entrySet());

            for (Map.Entry<String, ArrayList<Element>> registro : antes) {  
                Element ant = Elements.get(registro.getKey());
                if(ant!=null){
                    Element elem = new Element("Start");
                    elem.type = "Line";
                    elem.cPosX = ant.cPosX + radio;
                    elem.cPosY = ant.cPosY + radio / 2;
                    
                    ArrayList<Element> lista = e.Antecesores.get(registro.getKey());
                    if(lista!=null){
                        if(lista.size()>0)
                            if(!lista.get(0).Name.equals("Start")){
                                System.out.println("Antecesor: " + ant.Name + " se le agrego Start");
                                lista.add(0, elem);
                            }
                                
                    }
                    
                    /*if (!e.Antecesores.get(registro.getKey()).get(0).Name.equals("Start")) {
                        e.Antecesores.get(registro.getKey()).add(0, elem);
                    }*/
                }
                
                

            }

            antes = new ArrayList(e.Antecesores.entrySet());
            for (Map.Entry<String, ArrayList<Element>> registro : antes) {
                String antecesor = registro.getKey();
                Element a = null;
                if (antecesor.charAt(0) == '@') {
                    a = Elements.get(antecesor.charAt(1) + "");
                } else {
                    a = Elements.get(antecesor);
                }
                if (a != null) {
                    String aName = "";
                    if (a.Name.charAt(0) == '@') {
                        aName = a.Name.charAt(1) + "";
                    } else {
                        aName = a.Name;
                    }
                    String eName = "";
                    if (e.Name.charAt(0) == '@') {
                        eName = e.Name.charAt(1) + "";
                    } else {
                        eName = e.Name;
                    }
                    antecesoresMode.put(eName + "," + aName, 1); //poner en el mapa el modo en el que se encuentra
                    actualMode.put(eName + "," + aName, 3); //poner en el mapa el modo en el que se encuentra
                    
                    ArrayList<Element> lista = registro.getValue();
                    if(lista!=null)
                    if (lista.size() > 0) { //verificar que existan quiebres

                        ArrayList<Element> quiebres = registro.getValue();
                        List<Element> quiebresList = new ArrayList(quiebres);
                        for (Element q : quiebresList) {

                            if (q.type.equals("Line")) {
                                continue;
                            }

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
        
        System.out.println("actualMode: " + actualMode.toString());

        //Declaración de los eventos del mouse
        this.addMouseListener(new MouseListener() {
            @Override
            public void mousePressed(MouseEvent me) {
                clickAt(me.getX(), me.getY());
            }

            @Override
            public void mouseReleased(MouseEvent me) {
                try {
                    releaseMouse(me.getX(), me.getY());
                } catch (Exception e) {
                    ElementSelected = "";
                    AntecesorSelected = "";
                    LineActualSelected = "";
                }

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

            if (e.type.equals("Line")) {
                continue;
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
                g.fillOval(e.cPosX - (radio / 16), e.cPosY - (radio / 16), radio / 4, radio / 4);
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
                    String antecesor = registro.getKey();
                    Element a = null;
                    if (antecesor.charAt(0) == '@') {
                        a = Elements.get(antecesor.charAt(1) + "");
                    } else {
                        a = Elements.get(antecesor);
                    }

                    if (a != null) {
                        ArrayList<Element> lista = registro.getValue();
                        if(lista!=null)
                        if (lista.size() > 0) { //verificar que existan quiebres
                            ArrayList<Element> quiebres = registro.getValue();

                            /*int x1 = a.cPosX + (2 * (radio / 2));
                            int y1 = a.cPosY + (radio / 2);*/
                            int x1 = quiebres.get(0).cPosX;
                            int y1 = quiebres.get(0).cPosY;
                            
                            //Dibujado de quiebres
                            for (int i = 1; i < quiebres.size(); i++) {
                                int x2 = quiebres.get(i).cPosX, y2 = quiebres.get(i).cPosY;
                                
                                if (i == quiebres.size() - 1) {
                                    drawArrowLine(g2, x1, y1, x2, y2, ScreenWidth / 300, ScreenWidth / 300);
                                } else {
                                    g2.drawLine(x1, y1, x2, y2);
                                }
                                x1 = x2;
                                y1 = y2;
                            }
                            //drawArrowLine(g2, x1, y1, e.cPosX, e.cPosY + (radio / 2), ScreenWidth / 300, ScreenWidth / 300);
                        }
                    }

                }
                g2.setStroke(new BasicStroke(0));
            }
        }
    }

    /**
     * Procedimiento que verifica si se estaba moviendo una flecha del modelo
     * para ajustar la union en el nodo
     *
     * @param x
     * @param y
     */
    public void releaseMouse(int x, int y) {

        if (!AntecesorSelected.equals("")) {
            //Obtener Elemento al que pertenece el antecesor
            Element e = Elements.get(ElementSelected);

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
                e.Antecesores.get(AntecesorSelected).get(e.Antecesores.get(AntecesorSelected).size() - 1).cPosX = izqX;
                e.Antecesores.get(AntecesorSelected).get(e.Antecesores.get(AntecesorSelected).size() - 1).cPosY = izqY;
                antecesoresMode.put(ElementSelected + "," + AntecesorSelected, 1); //poner en el mapa el modo en el que se encuentra
            } else if (d2 < d1 && d2 < d3 && d2 < d4) {
                //d2 menor
                e.Antecesores.get(AntecesorSelected).get(e.Antecesores.get(AntecesorSelected).size() - 1).cPosX = arrX;
                e.Antecesores.get(AntecesorSelected).get(e.Antecesores.get(AntecesorSelected).size() - 1).cPosY = arrY;
                antecesoresMode.put(ElementSelected + "," + AntecesorSelected, 2); //poner en el mapa el modo en el que se encuentra
            } else if (d3 < d1 && d3 < d2 && d3 < d4) {
                //d3 menor
                e.Antecesores.get(AntecesorSelected).get(e.Antecesores.get(AntecesorSelected).size() - 1).cPosX = derX;
                e.Antecesores.get(AntecesorSelected).get(e.Antecesores.get(AntecesorSelected).size() - 1).cPosY = derY;
                antecesoresMode.put(ElementSelected + "," + AntecesorSelected, 3); //poner en el mapa el modo en el que se encuentra
            } else {
                //d4 menor
                e.Antecesores.get(AntecesorSelected).get(e.Antecesores.get(AntecesorSelected).size() - 1).cPosX = abaX;
                e.Antecesores.get(AntecesorSelected).get(e.Antecesores.get(AntecesorSelected).size() - 1).cPosY = abaY;
                antecesoresMode.put(ElementSelected + "," + AntecesorSelected, 4); //poner en el mapa el modo en el que se encuentra
            }
            repaint();
            AntecesorSelected = "";
            ElementSelected = "";
            return;
        }

        if (!LineActualSelected.equals("")) {
            //Obtener Elemento al que pertenece el antecesor
            Element e = Elements.get(ElementSelected);
            Element actual = Elements.get(LineActualSelected);
            //Obtener los 4 puntos del Elemento, izq, arr, derech, abaj,
            int izqX = actual.cPosX;
            int izqY = actual.cPosY + (radio / 2);
            int arrX = actual.cPosX + (radio / 2);
            int arrY = actual.cPosY;
            int derX = actual.cPosX + radio;
            int derY = actual.cPosY + (radio / 2);
            int abaX = actual.cPosX + (radio / 2);
            int abaY = actual.cPosY + radio;

            //Obtener distancias entre dos puntos de x,y y los 4 puntos del elemento
            double d1 = distancia(x, y, izqX, izqY);
            double d2 = distancia(x, y, arrX, arrY);
            double d3 = distancia(x, y, derX, derY);
            double d4 = distancia(x, y, abaX, abaY);

            if (d1 < d2 && d1 < d3 && d1 < d4) {
                //d1 menor
                e.Antecesores.get(LineActualSelected).get(0).cPosX = izqX;
                e.Antecesores.get(LineActualSelected).get(0).cPosY = izqY;
                actualMode.put(ElementSelected + "," + LineActualSelected, 1); //poner en el mapa el modo en el que se encuentra
            } else if (d2 < d1 && d2 < d3 && d2 < d4) {
                //d2 menor
                e.Antecesores.get(LineActualSelected).get(0).cPosX = arrX;
                e.Antecesores.get(LineActualSelected).get(0).cPosY = arrY;
                actualMode.put(ElementSelected + "," + LineActualSelected, 2); //poner en el mapa el modo en el que se encuentra
            } else if (d3 < d1 && d3 < d2 && d3 < d4) {
                //d3 menor
                e.Antecesores.get(LineActualSelected).get(0).cPosX = derX;
                e.Antecesores.get(LineActualSelected).get(0).cPosY = derY;
                actualMode.put(ElementSelected + "," + LineActualSelected, 3); //poner en el mapa el modo en el que se encuentra
            } else {
                //d4 menor
                e.Antecesores.get(LineActualSelected).get(0).cPosX = abaX;
                e.Antecesores.get(LineActualSelected).get(0).cPosY = abaY;
                actualMode.put(ElementSelected + "," + LineActualSelected, 4); //poner en el mapa el modo en el que se encuentra
            }
            repaint();
            LineActualSelected = "";
            ElementSelected = "";
            return;
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
            if (x <= (e.cPosX + radio) && y <= (e.cPosY + radio) && x >= e.cPosX && y >= e.cPosY) {
                if (e.Name.charAt(0) == '@') {
                    ElementSelected = e.Name.charAt(1) + "";
                } else {
                    ElementSelected = e.Name;
                }

                break;
            }

            List<Map.Entry<String, ArrayList<Element>>> ants = new ArrayList(e.Antecesores.entrySet());
            for (Map.Entry<String, ArrayList<Element>> reg : ants) {
                ArrayList<Element> list = reg.getValue();
                if(list==null)
                    continue;
                if (x <= (list.get(list.size() - 1).cPosX + radio / 4) && y <= (list.get(list.size() - 1).cPosY + radio / 4) && x >= list.get(list.size() - 1).cPosX - radio / 2 && y >= list.get(list.size() - 1).cPosY - radio / 4) {
                    if (e.Name.charAt(0) == '@') {
                        ElementSelected = e.Name.charAt(1) + "";
                    } else {
                        ElementSelected = e.Name;
                    }
                    AntecesorSelected = reg.getKey();
                    LineActualSelected = "";
                    System.out.println("Line Antecesor selected: " + AntecesorSelected);
                    break;
                }

                if (x <= (list.get(0).cPosX + radio / 4) && y <= (list.get(0).cPosY + radio / 4) && x >= list.get(0).cPosX - radio / 4 && y >= list.get(0).cPosY - radio / 4) {
                    if (e.Name.charAt(0) == '@') {
                        ElementSelected = e.Name.charAt(1) + "";
                    } else {
                        ElementSelected = e.Name;
                    }
                    LineActualSelected = reg.getKey();
                    AntecesorSelected = "";
                    System.out.println("LineActual selected: " + LineActualSelected);
                    break;
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
        System.out.println("Break: " + elemento.Name);
        ArrayList<Element> br = Elements.get(eName).Antecesores.get(antecesor);
        if (br != null) {
            br.add(br.size() - 1, elemento);
            breaks[0]++;
            repaint();
        }

    }

    /**
     * Procedimiento que se ejecuta al arrastrar un elemento, se actualiza su
     * posición x y y, para visualizar un arrastre del mismo en la pantalla
     *
     * @param x
     * @param y
     */
    public void dragElementSelected(int x, int y) {

        if (!AntecesorSelected.equals("")) {
            Element e = Elements.get(ElementSelected);
            if (e != null) {
                ArrayList<Element> lista = e.Antecesores.get(AntecesorSelected);
                if (lista != null) {
                    Element a = lista.get(e.Antecesores.get(AntecesorSelected).size() - 1);
                    if (a != null) {
                        a.cPosX = x;
                        a.cPosY = y;
                        repaint();
                    }
                }

            }
            return;
        }

        if (!LineActualSelected.equals("")) {
            Element e = Elements.get(ElementSelected);
            if (e != null) {
                ArrayList<Element> lista = e.Antecesores.get(LineActualSelected);
                if (lista != null) {
                    Element a = lista.get(0);
                    if (a != null) {
                        a.cPosX = x;
                        a.cPosY = y;
                        repaint();
                    }
                }

            }
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

                    List<Map.Entry<String, ArrayList<Element>>> ants = new ArrayList(Elements.get(ElementSelected).Antecesores.entrySet());
                    for (Map.Entry<String, ArrayList<Element>> entry : ants) {
                        ArrayList<Element> list = entry.getValue();

                        String eName = "";
                        if (e.Name.charAt(0) == '@') {
                            eName = e.Name.charAt(1) + "";
                        } else {
                            eName = e.Name;
                        }

                        String aName = "";
                        String antecesor = entry.getKey();
                        if (antecesor.charAt(0) == '@') {
                            aName = antecesor.charAt(1) + "";
                        } else {
                            aName = antecesor;
                        }

                        //Obtener los 4 puntos del Elemento, izq (modo1), arr (modo2), derech(modo3), abaj(modo4),
                        int izqX = e.cPosX;
                        int izqY = e.cPosY + (radio / 2);
                        int arrX = e.cPosX + (radio / 2);
                        int arrY = e.cPosY;
                        int derX = e.cPosX + radio;
                        int derY = e.cPosY + (radio / 2);
                        int abaX = e.cPosX + (radio / 2);
                        int abaY = e.cPosY + radio;

                        if (antecesoresMode.containsKey(eName + "," + aName)) {
                            int mode = antecesoresMode.get(eName + "," + aName);
                            switch (mode) {
                                case 1:
                                    list.get(list.size() - 1).cPosX = izqX;
                                    list.get(list.size() - 1).cPosY = izqY;
                                    break;
                                case 2:
                                    list.get(list.size() - 1).cPosX = arrX;
                                    list.get(list.size() - 1).cPosY = arrY;
                                    break;
                                case 3:
                                    list.get(list.size() - 1).cPosX = derX;
                                    list.get(list.size() - 1).cPosY = derY;
                                    break;
                                case 4:
                                    list.get(list.size() - 1).cPosX = abaX;
                                    list.get(list.size() - 1).cPosY = abaY;
                                    break;
                            }
                        }
                    }
                    
                    
                    
                    
                    for (Map.Entry<String, Element> elements : Elements.entrySet()) {
                        Element element = elements.getValue();
                        String eName = "";
                        if (e.Name.charAt(0) == '@') {
                            eName = element.Name.charAt(1) + "";
                        } else {
                            eName = element.Name;
                        }
                        if(element.Antecesores.containsKey(ElementSelected)){
                            String aName = "";
                            if (ElementSelected.charAt(0) == '@') {
                                aName = ElementSelected.charAt(1) + "";
                            } else {
                                aName = ElementSelected;
                            }
                            
                            //Obtener los 4 puntos del Elemento, izq (modo1), arr (modo2), derech(modo3), abaj(modo4),
                            int izqX = e.cPosX;
                            int izqY = e.cPosY + (radio / 2);
                            int arrX = e.cPosX + (radio / 2);
                            int arrY = e.cPosY;
                            int derX = e.cPosX + radio;
                            int derY = e.cPosY + (radio / 2);
                            int abaX = e.cPosX + (radio / 2);
                            int abaY = e.cPosY + radio;
                            
                            if (actualMode.containsKey(eName + "," + aName)) {
                                int mode = actualMode.get(eName + "," + aName);
                                switch (mode) {
                                    case 1:
                                        element.Antecesores.get(ElementSelected).get(0).cPosX = izqX;
                                        element.Antecesores.get(ElementSelected).get(0).cPosY = izqY;
                                        break;
                                    case 2:
                                        element.Antecesores.get(ElementSelected).get(0).cPosX = arrX;
                                        element.Antecesores.get(ElementSelected).get(0).cPosY = arrY;
                                        break;
                                    case 3:
                                        element.Antecesores.get(ElementSelected).get(0).cPosX = derX;
                                        element.Antecesores.get(ElementSelected).get(0).cPosY = derY;
                                        break;
                                    case 4:
                                        element.Antecesores.get(ElementSelected).get(0).cPosX = abaX;
                                        element.Antecesores.get(ElementSelected).get(0).cPosY = abaY;
                                        break;
                                }
                            }
                            
                            
                            
                        }
                    }
                    

                   /* for (Map.Entry<String, ArrayList<Element>> entry : ants) {
                        ArrayList<Element> list = entry.getValue();

                        String eName = "";
                        if (e.Name.charAt(0) == '@') {
                            eName = e.Name.charAt(1) + "";
                        } else {
                            eName = e.Name;
                        }

                        String aName = "";
                        String antecesor = entry.getKey();
                        if (antecesor.charAt(0) == '@') {
                            aName = antecesor.charAt(1) + "";
                        } else {
                            aName = antecesor;
                        }

                        //Obtener los 4 puntos del Elemento, izq (modo1), arr (modo2), derech(modo3), abaj(modo4),
                        int izqX = e.cPosX;
                        int izqY = e.cPosY + (radio / 2);
                        int arrX = e.cPosX + (radio / 2);
                        int arrY = e.cPosY;
                        int derX = e.cPosX + radio;
                        int derY = e.cPosY + (radio / 2);
                        int abaX = e.cPosX + (radio / 2);
                        int abaY = e.cPosY + radio;

                        if (antecesoresMode.containsKey(eName + "," + aName)) {
                            int mode = antecesoresMode.get(eName + "," + aName);
                            switch (mode) {
                                case 1:
                                    list.get(list.size() - 1).cPosX = izqX;
                                    list.get(list.size() - 1).cPosY = izqY;
                                    break;
                                case 2:
                                    list.get(list.size() - 1).cPosX = arrX;
                                    list.get(list.size() - 1).cPosY = arrY;
                                    break;
                                case 3:
                                    list.get(list.size() - 1).cPosX = derX;
                                    list.get(list.size() - 1).cPosY = derY;
                                    break;
                                case 4:
                                    list.get(list.size() - 1).cPosX = abaX;
                                    list.get(list.size() - 1).cPosY = abaY;
                                    break;
                            }
                        }

                        for (Map.Entry<String, Element> elements : Elements.entrySet()) {
                            List<Map.Entry<String, ArrayList<Element>>> antecesores = new ArrayList(elements.getValue().Antecesores.entrySet());
                            for (Map.Entry<String, ArrayList<Element>> registro : antecesores) {
                                if (registro.getKey().equals(ElementSelected)) {
                                    String EName = "";
                                    if (elements.getKey().charAt(0) == '@') {
                                        EName = elements.getKey().charAt(1) + "";
                                    } else {
                                        EName = elements.getKey();
                                    }
                                    if (actualMode.containsKey(EName + "," + ElementSelected)) {
                                        int mode = actualMode.get(EName + "," + ElementSelected);
                                        switch (mode) {
                                            case 1:
                                                registro.getValue().get(0).cPosX = izqX;
                                                registro.getValue().get(0).cPosY = izqY;
                                                break;
                                            case 2:
                                                registro.getValue().get(0).cPosX = arrX;
                                                registro.getValue().get(0).cPosY = arrY;
                                                break;
                                            case 3:
                                                registro.getValue().get(0).cPosX = derX;
                                                registro.getValue().get(0).cPosY = derY;
                                                break;
                                            case 4:
                                                registro.getValue().get(0).cPosX = abaX;
                                                registro.getValue().get(0).cPosY = abaY;
                                                break;
                                        }
                                    }

                                }

                            }

                        }
                       

                    }*/
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
