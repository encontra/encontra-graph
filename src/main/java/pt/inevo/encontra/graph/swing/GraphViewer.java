package pt.inevo.encontra.graph.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;


import edu.uci.ics.jung.graph.ArchetypeEdge;
import edu.uci.ics.jung.graph.ArchetypeVertex;
import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.decorators.DefaultToolTipFunction;
import edu.uci.ics.jung.graph.decorators.EdgeShape;
import edu.uci.ics.jung.graph.decorators.EdgeStringer;
import edu.uci.ics.jung.graph.decorators.ToolTipFunction;
import edu.uci.ics.jung.graph.decorators.VertexStringer;
import edu.uci.ics.jung.visualization.FRLayout;
import edu.uci.ics.jung.visualization.GraphLabelRenderer;
import edu.uci.ics.jung.visualization.GraphMouseListener;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.ISOMLayout;
import edu.uci.ics.jung.visualization.PluggableRenderer;
import edu.uci.ics.jung.visualization.ShapePickSupport;
import edu.uci.ics.jung.visualization.SpringLayout;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.VisualizationViewer.GraphMouse;
import edu.uci.ics.jung.visualization.contrib.CircleLayout;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;

public class GraphViewer {

	VisualizationViewer _viewer;

	Graph _graph;

	String _vertex_datum=null;
	String _edge_datum=null;

	// The frame.
    public JFrame frame;

 // The status label.
    protected JLabel label = new JLabel();

    public GraphViewer(Graph graph) {
    	_graph=graph;
    	 PluggableRenderer pr = new PluggableRenderer();

    	 _viewer =  new VisualizationViewer(new ISOMLayout(graph), pr);
    	 _viewer.setPickSupport(new ShapePickSupport());
         pr.setEdgeShapeFunction(new EdgeShape.QuadCurve());

         /*
         EdgeStringer edge_stringer = new EdgeStringer(){
             public String getLabel(ArchetypeEdge e) {
                 return e.toString();
             }
         };
         pr.setEdgeStringer(edge_stringer);*/

         VertexStringer vertex_stringer = new VertexStringer(){
             public String getLabel(ArchetypeVertex v) {
                 return v.toString();
             }
         };
         pr.setVertexStringer(vertex_stringer);

         _viewer.addGraphMouseListener(new TestGraphMouseListener());

         // add my listener for ToolTips
         ToolTipFunction tooltip=new ToolTipFunction(){
        	 public String getToolTipText(Edge e) {
        		 if(_edge_datum!=null) {
        			if(e.getUserDatum(_edge_datum)!=null)
        				return e.getUserDatum(_edge_datum).toString();
        		 }
        		return e.toString();
        	}

			public String getToolTipText(Vertex v) {
				if(_vertex_datum!=null)
					return (String) v.getUserDatum(_vertex_datum);
				return v.toString();
			}

			public String getToolTipText(MouseEvent event) {
				// TODO Auto-generated method stub
				return null;
			}

         };
         _viewer.setToolTipFunction(tooltip);

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

        BufferedImage bi = new BufferedImage(width,height,BufferedImage.TYPE_INT_BGR);
        Graphics2D graphics = bi.createGraphics();
        graphics.setColor(bg);
        graphics.fillRect(0,0, width, height);
        _viewer.paintComponents(graphics);

        try{
           ImageIO.write(bi,"jpeg",new File(filename));
        }catch(Exception e){e.printStackTrace();}
    }

	public void Show() {


		frame.setVisible(true);

	}

	public JComponent createComponents() {
		Container content = frame.getContentPane();
        final GraphZoomScrollPane panel = new GraphZoomScrollPane(_viewer);
        content.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final GraphMouse graphMouse = new DefaultModalGraphMouse();
        _viewer.setGraphMouse(graphMouse);
        JMenuBar menu = new JMenuBar();
        menu.add(((DefaultModalGraphMouse)graphMouse).getModeMenu());
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
                scaler.scale(_viewer, 1/1.1f, _viewer.getCenter());
            }
        });
        JButton reset = new JButton("reset");
        reset.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				_viewer.getLayoutTransformer().setToIdentity();
				_viewer.getViewTransformer().setToIdentity();

			}});

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

    		public void graphClicked(Vertex v, MouseEvent me) {
    		    System.err.println("Vertex "+v+" was clicked at ("+me.getX()+","+me.getY()+")");
    		}
    		public void graphPressed(Vertex v, MouseEvent me) {
    		    System.err.println("Vertex "+v+" was pressed at ("+me.getX()+","+me.getY()+")");
    		}
    		public void graphReleased(Vertex v, MouseEvent me) {
    		    System.err.println("Vertex "+v+" was released at ("+me.getX()+","+me.getY()+")");
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

