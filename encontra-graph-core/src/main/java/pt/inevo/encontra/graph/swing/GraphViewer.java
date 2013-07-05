package pt.inevo.encontra.graph.swing;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import org.apache.commons.collections15.Transformer;
import pt.inevo.encontra.graph.GraphEdge;
import pt.inevo.encontra.graph.GraphNode;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GraphViewer {

    private final String title;
    protected Graph graph;
    protected JFrame frame;
    protected Logger log = Logger.getLogger(GraphViewer.class.toString());

    protected void initialize () {
        int width = 800;
        int height = 600;

        //other layouts available at edu.uci.ics.jung.algorithms.layout
        // TODO create a menu for selecting the desired layout
        Layout<GraphNode, GraphEdge> layout = new CircleLayout(graph);
        layout.setSize(new Dimension(width, height)); // sets the initial size of the space
        VisualizationViewer<GraphNode, GraphEdge> vv =
                new VisualizationViewer<GraphNode, GraphEdge>(layout);
        vv.setPreferredSize(new Dimension(width, height)); //Sets the viewing area size

        //rendering transformers for the graph
        Transformer<GraphNode, Paint> vertexPaint = new Transformer<GraphNode, Paint>() {
            public Paint transform(GraphNode node) {
                if (node.getParent() == null) {
                    return Color.RED;
                } else {
                    return Color.GREEN;
                }
            }
        };

        vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
//        vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller());

        //mouse interaction
        DefaultModalGraphMouse graphMouse = new DefaultModalGraphMouse();
        graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);
        vv.setGraphMouse(graphMouse);

        JComboBox modeBox = graphMouse.getModeComboBox();
        modeBox.addItemListener(graphMouse.getModeListener());
        graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);

        // Create a new JFrame.
        frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Container content = frame.getContentPane();

        JPanel controls = new JPanel();
        controls.add(modeBox);
        content.add(controls, BorderLayout.NORTH);
        content.add(vv, BorderLayout.CENTER);

        frame.pack();

        // Display the frame.
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                closed();
            }
        });

        /*Set the size of the frame*/
        frame.setSize(width, height);
    }

    public GraphViewer(Graph graph, String title) {
        this.graph = graph;
        this.title = title;
        initialize();
        log.log(Level.INFO, "Graph viewer initialized for graph: " + graph.toString());
    }

    public void writeJPEGImage(String filename) {
        int width = 500;
        int height = 500;
        Color bg = Color.WHITE;

        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
        Graphics2D graphics = bi.createGraphics();
        graphics.setColor(bg);
        graphics.fillRect(0, 0, width, height);

        try {
            ImageIO.write(bi, "jpeg", new File(filename));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void show() {
        frame.setVisible(true);
    }

    private synchronized void closed() {
        notify();
    }
}

