package pt.inevo.encontra.graph.swing;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.event.GraphEvent;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.GraphMouseListener;
import edu.uci.ics.jung.visualization.control.ScalingControl;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class GraphViewer {

    VisualizationViewer _viewer;

    Graph _graph;

    String _vertex_datum = null;
    String _edge_datum = null;

    // The frame.
    public JFrame frame;

    // The status label.
    protected JLabel label = new JLabel();

    public GraphViewer(Graph graph) {
        _graph = graph;

//        PluggableRenderer pr = new PluggableRenderer();
//
//        _viewer = new VisualizationViewer(new ISOMLayout(graph), pr);
//        _viewer.setPickSupport(new ShapePickSupport());
//        pr.setEdgeShapeFunction(new EdgeShape.QuadCurve());
//
//        VertexStringer vertex_stringer = new VertexStringer() {
//            public String getLabel(ArchetypeVertex v) {
//                return v.toString();
//            }
//        };
//        pr.setVertexStringer(vertex_stringer);
//
//        _viewer.addGraphMouseListener(new TestGraphMouseListener());
//
//        // add my listener for ToolTips
//        ToolTipFunction tooltip = new ToolTipFunction() {
//            public String getToolTipText(Edge e) {
//                if (_edge_datum != null) {
//                    if (e.getUserDatum(_edge_datum) != null)
//                        return e.getUserDatum(_edge_datum).toString();
//                }
//                return e.toString();
//            }
//
//            public String getToolTipText(Vertex v) {
//                if (_vertex_datum != null)
//                    return (String) v.getUserDatum(_vertex_datum);
//                return v.toString();
//            }
//
//            public String getToolTipText(MouseEvent event) {
//                // TODO Auto-generated method stub
//                return null;
//            }
//
//        };
//        _viewer.setToolTipFunction(tooltip);

        // Create a new JFrame.
        frame = new JFrame("JUNG");

        // Add components to the frame.
        createComponents();

        // Display the frame.
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                closed();
            }
        });
        frame.setSize(400, 400);
    }

    public void writeJPEGImage(String filename) {
        int width = 500;//_viewer.getWidth();
        int height = 500;//_viewer.getHeight();
        Color bg = Color.WHITE;

        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
        Graphics2D graphics = bi.createGraphics();
        graphics.setColor(bg);
        graphics.fillRect(0, 0, width, height);
        _viewer.paintComponents(graphics);

        try {
            ImageIO.write(bi, "jpeg", new File(filename));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void Show() {
        frame.setVisible(true);
    }

    public JComponent createComponents() {
        Container content = frame.getContentPane();
        final GraphZoomScrollPane panel = new GraphZoomScrollPane(_viewer);
        content.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final VisualizationViewer.GraphMouse graphMouse = new DefaultModalGraphMouse();
        _viewer.setGraphMouse(graphMouse);
        JMenuBar menu = new JMenuBar();
        menu.add(((DefaultModalGraphMouse) graphMouse).getModeMenu());
        panel.setCorner(menu);
        final ScalingControl scaler = new CrossoverScalingControl();

        JButton plus = new JButton("+");
        plus.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scaler.scale(_viewer, 1.1f, _viewer.getCenter());
            }
        });
        JButton minus = new JButton("-");
        minus.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scaler.scale(_viewer, 1 / 1.1f, _viewer.getCenter());
            }
        });
        JButton reset = new JButton("reset");
        reset.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
//                _viewer.getLayoutTransformer().setToIdentity();
//                _viewer.getViewTransformer().setToIdentity();

            }
        });

        JPanel controls = new JPanel();
        controls.add(plus);
        controls.add(minus);
        controls.add(reset);
        content.add(controls, BorderLayout.SOUTH);

        frame.pack();

        return panel;
    }

    /**
     * A nested class to demo the GraphMouseListener finding the
     * right vertices after zoom/pan
     */
    static class TestGraphMouseListener implements GraphMouseListener {

        public void graphClicked(GraphEvent.Vertex v, MouseEvent me) {
            System.err.println("Vertex " + v + " was clicked at (" + me.getX() + "," + me.getY() + ")");
        }

        public void graphPressed(GraphEvent.Vertex v, MouseEvent me) {
            System.err.println("Vertex " + v + " was pressed at (" + me.getX() + "," + me.getY() + ")");
        }

        public void graphReleased(GraphEvent.Vertex v, MouseEvent me) {
            System.err.println("Vertex " + v + " was released at (" + me.getX() + "," + me.getY() + ")");
        }

        @Override
        public void graphClicked(Object o, MouseEvent mouseEvent) {
        }

        @Override
        public void graphPressed(Object o, MouseEvent mouseEvent) {
        }

        @Override
        public void graphReleased(Object o, MouseEvent mouseEvent) {
        }
    }

    public void setEdgeDataToShow(String string) {
        _edge_datum = string;
    }

    public void setVertexDataToShow(String string) {
        _vertex_datum = string;
    }

    private synchronized void closed() {
        notify();
    }

    public synchronized void waitUntilClosed() {
        try {
            wait();
        } catch (InterruptedException ex) {
            // stop waiting on interruption
        }
    }
}

