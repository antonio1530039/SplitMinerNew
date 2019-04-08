package Vista;

import Modelo.BPMNModel;
import Modelo.Element;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JPanel;

public class gJPanel extends JPanel {

    int ScreenWidth;
    int ScreenHeight;
    int radio;
    
    String ElementSelected = "";
    public HashMap<String, Element> Elements = new HashMap<>();
    HashMap<String, Color> gatewaysColors = new HashMap<>();
    public BPMNModel BPMN;

    public gJPanel(int width, int height, HashMap<String, Element> elements, BPMNModel bpmn) {
        Elements = elements;
        ScreenWidth = width;
        ScreenHeight = height;
        radio = ScreenWidth / 28;
        ElementSelected = "";
        BPMN = bpmn;
        gatewaysColors = new HashMap<>();
        
        
        /*JTextArea notationTxt = new JTextArea();
        notationTxt.setBounds(5, 5, ScreenWidth, ScreenWidth);
        notationTxt.setText(text);
        notationTxt.setFont(notationTxt.getFont().deriveFont(20f));
        notationTxt.setEditable(false);
        add(notationTxt);
        
        */

        //Agregar radios!
        

        setBackground(new Color(255, 255, 255));
        setSize(ScreenWidth, ScreenHeight);

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
            
            if(e.type.equals("Start")){
                g.setColor(Color.green);
                g.drawOval(e.cPosX, e.cPosY, radio, radio);
                g.setColor(Color.black);
                g.drawString("Start", e.cPosX + (radio / 3), e.cPosY + (radio / 2));
            }else if(e.type.equals("End")){
                g.setColor(Color.red);
                g.drawOval(e.cPosX, e.cPosY, radio, radio);
                g.setColor(Color.black);
                g.drawString("End", e.cPosX + (radio / 3), e.cPosY + (radio / 2));
            }else if (e.type.equals("Task")) {
                g.setColor(Color.black);
                g.drawOval(e.cPosX, e.cPosY, radio, radio);
                g.drawString(e.Name, e.cPosX + (radio / 2), e.cPosY + (radio / 2));
            } else if(e.type.equals("Autoloop")){
                g.setColor(Color.black);
                g.fillOval(e.cPosX, e.cPosY, radio, radio);
                g.setColor(Color.white);
                g.drawString(e.Name.charAt(1) + "", e.cPosX + (radio / 2), e.cPosY + (radio / 2));
            
            }else if(e.type.equals("Gateway")){
                
                //obtencion de color de compuerta
                Color color = null;
                String name = ( (e.Name.charAt(0)=='O') ? e.Name.substring(1, e.Name.indexOf("C")) : e.Name.substring(0, e.Name.length() - 1) );
                if (!gatewaysColors.containsKey(name)) {
                    color = new Color((int) (Math.random() * 254), (int) (Math.random() * 254), (int) (Math.random() * 254));
                    gatewaysColors.put(name, color);
                } else {
                    color = gatewaysColors.get(name);
                }
                
                g.setColor(color); //se asigna el color a la compuerta
                
                //g.fillRoundRect(e.cPosX, e.cPosY, radio, radio, radio / 2, radio / 2);

                if(BPMN.Gor.contains(e.Name)){
                    drawOr(g, e.cPosX + (radio / 2), e.cPosY + (radio / 2));
                }else if(BPMN.Gand.contains(e.Name)){
                    drawAnd(g, e.cPosX + (radio / 2), e.cPosY + (radio / 2));
                }else if(BPMN.Gxor.contains(e.Name)){
                    drawXor(g, e.cPosX + (radio / 2), e.cPosY + (radio / 2));
                }
                
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
                for (String antecesor : e.Antecesores) {
                    Element a = null;
                    if(antecesor.charAt(0) == '@')
                        a = Elements.get(antecesor.charAt(1) + "");
                    else{
                        a = Elements.get(antecesor);
                    }
                    
                    if(a!=null)
                        drawArrowLine(g, a.cPosX + (2 * (radio / 2)), a.cPosY + (radio / 2), e.cPosX, e.cPosY + (radio / 2), ScreenWidth / 300, ScreenWidth / 300);
                }
            }
        }
    }

    public void clickAt(int x, int y) { //Dada una posición x, y, verificar si se dio clic a dentro del radio de un elemento
        for (Map.Entry<String, Element> entry : Elements.entrySet()) {
            Element e = entry.getValue(); //get the element
            if (x <= (e.cPosX + radio) && y <= (e.cPosY + radio) && x >= e.cPosX && y >= e.cPosY) {
                if(e.Name.charAt(0) == '@'){
                    ElementSelected = e.Name.charAt(1) + "";
                }else{
                    ElementSelected = e.Name;
                }
                
                break;
            }
        }
    }

    public void dragElementSelected(int x, int y) {
        if (!ElementSelected.equals("")) {
            Elements.get(ElementSelected).cPosX = x - (radio / 2); //reasignar posicion al arrastrar mouse, para que el elemento quede en el centro del cursor
            Elements.get(ElementSelected).cPosY = y - (radio / 2);
            repaint();
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
        g.drawLine( x - (radio/6), y - (radio/6), x + (radio/6), y + (radio/6) );
        g.drawLine( x + (radio/6), y - (radio/6), x - (radio/6), y + (radio/6) );
    }
    
    private void drawOr(Graphics g, int x, int y) {
        Polygon p = drawDiamond(x, y);
        g.fillPolygon(p);
        g.setColor(Color.white);
        g.drawOval(x - (radio/4), y - (radio/4), radio/2, radio/2 );
    }
    
    private void drawAnd(Graphics g, int x, int y) {
        Polygon p = drawDiamond(x, y);
        g.fillPolygon(p);
        g.setColor(Color.white);
        g.drawLine(x, y - (radio/4), x , y + (radio/4) );
        g.drawLine(x - (radio/4), y , x + (radio/4) , y );
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