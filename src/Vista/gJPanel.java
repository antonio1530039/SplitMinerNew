package Vista;

import Modelo.BPMNModel;
import Modelo.Element;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;

public class gJPanel extends JPanel {

    int ScreenWidth;
    int ScreenHeight;
    int radio;

    int[] breaks;
    String ElementSelected = "";
    public HashMap<String, Element> Elements = new HashMap<>();
    HashMap<String, Color> gatewaysColors = new HashMap<>();
    public BPMNModel BPMN;

    public gJPanel(int width, int height, HashMap<String, Element> elements, BPMNModel bpmn, int[] breaks) {
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

        List<Map.Entry<String, Element>> elems = new ArrayList(Elements.entrySet());
        for (Map.Entry<String, Element> entry : elems) {
            Element e = entry.getValue();

            List<Map.Entry<String, ArrayList<Element>>> antes = new ArrayList(e.Antecesores.entrySet());
            for (Map.Entry<String, ArrayList<Element>> registro : antes) {
                String antecesor = registro.getKey();
                Element a = null;
                if (antecesor.charAt(0) == '@') {
                    a = Elements.get(antecesor.charAt(1) + "");
                } else {
                    a = Elements.get(antecesor);
                }
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

        this.addMouseListener(new MouseListener() {
            @Override
            public void mousePressed(MouseEvent me) {
                clickAt(me.getX(), me.getY());
            }

            @Override
            public void mouseReleased(MouseEvent me) {
                ElementSelected = "";
            }

            @Override
            public void mouseEntered(MouseEvent me) {
            }

            public void mouseExited(MouseEvent me) {
            }

            @Override
            public void mouseClicked(MouseEvent me) {
                clickAt(me.getX(), me.getY());
                if (me.getClickCount() == 2 && !me.isConsumed()) {
                    me.consume();
                    doubleClick(me.getX(), me.getY());
                } else {
                    if (me.getButton() == MouseEvent.BUTTON3) { //right click
                        rightClick();
                    }

                   
                }

            }
        });

        this.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                dragElementSelected(e.getX(), e.getY());
            }

            @Override
            public void mouseMoved(MouseEvent e) {
            }
        });

    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        for (Map.Entry<String, Element> entry : Elements.entrySet()) {
            Element e = entry.getValue();

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
                g.drawOval(e.cPosX, e.cPosY, radio, radio);
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

            //Dibujar lineas
            if (!e.Antecesores.isEmpty()) {
                g.setColor(Color.black);

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

                            ArrayList<Element> quiebres = registro.getValue();
                            //Collections.sort(quiebres, Collections.reverseOrder());

                            int x1 = a.cPosX + (2 * (radio / 2));
                            int y1 = a.cPosY + (radio / 2);

                            /*for (int i = quiebres.size() - 1; i > -1; i--) {
                                int x2 = quiebres.get(i).cPosX, y2 = quiebres.get(i).cPosY;
                                //drawArrowLine(g, x1, y1, x2, y2, ScreenWidth / 300, ScreenWidth / 300);
                                g.drawLine(x1, y1, x2, y2);
                                x1 = x2;
                                y1 = y2;
                            }*/
                            
                            for (int i = 0; i < quiebres.size(); i++) {
                                int x2 = quiebres.get(i).cPosX, y2 = quiebres.get(i).cPosY;
                                //drawArrowLine(g, x1, y1, x2, y2, ScreenWidth / 300, ScreenWidth / 300);
                                g.drawLine(x1, y1, x2, y2);
                                x1 = x2;
                                y1 = y2;
                            }
                            
                            
                            
                            drawArrowLine(g, x1, y1, e.cPosX, e.cPosY + (radio / 2), ScreenWidth / 300, ScreenWidth / 300);
                        } else {
                            drawArrowLine(g, a.cPosX + (2 * (radio / 2)), a.cPosY + (radio / 2), e.cPosX, e.cPosY + (radio / 2), ScreenWidth / 300, ScreenWidth / 300);
                        }
                    }

                }
            }
        }
    }

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
        }

    }
    
    public void rightClick(){
        if (ElementSelected.contains("break")) {

            Element elementSelected = Elements.get(ElementSelected);

            //public HashMap<String, Element> Elements = new HashMap<>();
            for (Map.Entry<String, Element> entry : Elements.entrySet()) {
                Element e = entry.getValue();
                //public HashMap<String, ArrayList<Element>> Antecesores;
                for (Map.Entry<String, ArrayList<Element>> ant : e.Antecesores.entrySet()) {
                    ant.getValue().remove(elementSelected);
                }
            }

            Elements.remove(ElementSelected);
            repaint();
        }
    }

    public void doubleClick(int x, int y) {

        if (ElementSelected.contains("break")) {

            Elements.remove(ElementSelected);
            repaint();
            return;
        }

        //if (ElementSelected.equals("")) {
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

                        //drawArrowLine(g, x1, y1, e.cPosX, e.cPosY + (radio / 2), ScreenWidth / 300, ScreenWidth / 300);
                    } else {

                        //verificar limite
                        // if(checkForLineInaPoint( Double.parseDouble((a.cPosX + (2 * (radio / 2))) + "") , Double.parseDouble( (a.cPosY + (radio / 2)) + "" ), Double.parseDouble((e.cPosX) + "") , Double.parseDouble((e.cPosY + (radio / 2))+ ""), Double.parseDouble(x + "" ), Double.parseDouble(y + "") )){
                        // if (isPointInLine(x, y, a.cPosX + (2 * (radio / 2)), a.cPosY + (radio / 2), e.cPosX, e.cPosY + (radio / 2))) {
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

        //}
    }

    public void realizarQuiebre(String eName, String antecesor, int x, int y) {
        Element elemento = new Element();
        elemento.Name = "break" + breaks[0];
        elemento.type = "Break";
        elemento.cPosX = x;
        elemento.cPosY = y;
        this.Elements.put("break" + breaks[0], elemento);
        ArrayList<Element> br = Elements.get(eName).Antecesores.get(antecesor);

        /*if(br.size() > 1){
            this.Elements.remove("break" + breaks[0]);
            return;
        }*/
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

    public void dragElementSelected(int x, int y) {
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

    private Polygon drawDiamond(int x, int y) {
        Polygon p = new Polygon();
        p.addPoint(x, y - (radio / 2));
        p.addPoint(x - (radio / 2), y);
        p.addPoint(x, y + (radio / 2));
        p.addPoint(x + (radio / 2), y);
        return p;
    }

    private void drawXor(Graphics g, int x, int y) {
        Polygon p = drawDiamond(x, y);
        g.fillPolygon(p);
        g.setColor(Color.white);
        g.drawLine(x - (radio / 6), y - (radio / 6), x + (radio / 6), y + (radio / 6));
        g.drawLine(x + (radio / 6), y - (radio / 6), x - (radio / 6), y + (radio / 6));
    }

    private void drawOr(Graphics g, int x, int y) {
        Polygon p = drawDiamond(x, y);
        g.fillPolygon(p);
        g.setColor(Color.white);
        g.drawOval(x - (radio / 4), y - (radio / 4), radio / 2, radio / 2);
    }

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
