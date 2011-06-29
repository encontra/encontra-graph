package pt.inevo.encontra.graph;

import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.EigenvalueDecomposition;
import edu.uci.ics.jung.graph.SparseGraph;
import pt.inevo.encontra.common.distance.HasDistance;
import pt.inevo.encontra.storage.IEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Graph<V extends GraphNode, E extends GraphEdge> extends SparseGraph<V, E> implements IEntity<Long> {

    //!< # of IDs stored in front of the feature vector. (in this case: graph, subgraph and level)
    public static int NIDS = 3;

    /**
     * Used to divide the descriptor-entries by to make sure none of the entries is > 1.
     * This value is untested and actually based on nothing.
     */
    public static float MAXVAL = 250;
    public static float MAXID = 999;    //!< ? A Node's id cannot be bigger than MAXID?
    public static int MIN_NODES = 2;    //!< We don't compute descriptors for subgraphs with less then MIN_NODES
    public static int MAXLEVEL = 10;    //!< Maximum depth to calculate the adjacency for.

    private List<V> nodesList = new ArrayList<V>();
    private long id;

    public Graph() {
        id = -1;
    }

    public Graph(Long id) {
        this.id = id;
    }

    public V createNode(Long id) {
        return createNode(id, null);
    }

    public V createNode(Long id, V p) {
        V n1 = null;

        // Make sure there is no Node with the given id
        if ((n1 = findNode(id)) == null) {

            ////System.out.println("[++] createNode: creating Node with id [" + id + "]");

//            n1 = new V(id);
            n1.setId(id);
            n1.setData(p);

            nodesList.add(n1); // Maintain Node List for ordering purposes !
            addVertex(n1);
            n1.setGraph(this);
        }

        return (V)n1;
    }

    public boolean addNode(V node) {
        boolean result = addVertex(node);
        node.setGraph(this);
        return result;
    }

    public boolean addAllNodes(List<V> nodes) {
        boolean result = true;
        for (V n : nodes) {
            result &= addVertex(n);
            n.setGraph(this);
        }
        return result;
    }

    public void removeNode(V n) {

        nodesList.remove(n); // Maintain Node List for ordering purposes !
        removeVertex(n);
    }

    public boolean addChild(Long currentNodeId, Long childNodeId) {
        V parentNode = null;
        V childNode = null;

        // Try to find the node corresponding to currentNodeId.
        parentNode = findNode(currentNodeId);
        if (parentNode == null) {
            //System.out.println( "[--] addChild: Could not find parent");
        }

        // Try to find the node corresponding to childNodeId.
        childNode = findNode(childNodeId);
        if (childNode == null) {
            //System.out.println( "[--] Graph::addChild: Could not find child");
        }

        // Insert the child into the parent's incList and update its pointer
        // to its parent node.
        if (parentNode != null && childNode != null) {
            //System.out.println( "[++] Graph::addChild: adding child " + childNodeId + " to parent " + currentNodeId );

            // TODO - check if we can find the parent childNode->setParent(parentNode);

            boolean res = parentNode.addIncLink(childNode);

            return res;
        }

        return false;
    }

    /**
     * Adds an adjacency link.
     * <p/>
     * This is done by looking for both nodes specified in the ids and joining them together.
     * <p/>
     * TODO:	Add parameter to make the joining work both ways. This is now done by the function
     * that computes the topology-graph.
     * TODO:	addAdjLink does not check for duplicate additions.
     *
     * @param currentNodeId The id of the 'current' Node.
     * @param siblingNodeId The id of the sibling Node to be joined to the 'current' Node.
     * @return Returns true iff the addition was succesful, false otherwise.
     */
    public boolean addSibling(Long currentNodeId, Long siblingNodeId) {
        GraphNode currentNode = null;
        GraphNode siblingNode = null;

        // Check if the ids belonging to the nodes to be joined exist.
        if ((currentNode = findNode(currentNodeId)) == null || (siblingNode = findNode(siblingNodeId)) == null) {
            return false;
        }

        // Create the adjacency link between the two nodes.
        // TODO Check if this test is needed - if (currentNode && siblingNode) {
        currentNode.addAdjLink(siblingNode);

        //System.out.println( "[++] addSibling: adding sibling " + currentNodeId + " to " + siblingNodeId);
        return true;
        //}

        //return false;
    }

    /**
     * Calculates the Graph's single- or multilevel-descriptor.
     * A descriptor is a list of Eigenvalues.
     *
     * @param levels         If true descriptors for all levels will be calculated.
     * @param subGraphRootId The id of the root of the subgraph to find.
     * @return CIList of RowVectors. Each RowVector contains the descriptor for a level.
     *         <p/>
     *         public List<DoubleMatrix1D> descriptors(boolean levels) {
     *         <p/>
     *         //System.out.println( "[++] descriptors: for graph " + id);
     *         <p/>
     *         //if (levels)
     *         //System.out.println( " and its subgraphs" );
     *         <p/>
     *         <p/>
     *         List<DoubleMatrix1D> descriptorSet = new ArrayList<DoubleMatrix1D>();
     *         <p/>
     *         if (levels) {
     *         // Isolate al subgraphs to a certain depth/level and store them in _subgraphs
     *         <p/>
     *         //System.out.println( "Graph:::descriptors - Isolating subgraphs to a level of " + MAXLEVEL);
     *         //isolateAllSubGraphs(MAXLEVEL);
     *         //System.out.println( "Graph:::descriptors - Isolating subgraphs");
     *         List<List<GraphNode>> subgraphs=isolateAllSubGraphs();
     *         <p/>
     *         // Compute descriptors for all subgraphs
     *         int j;
     *         for(j=0; j < subgraphs.size(); j++) {
     *         //System.out.println("Graph:::descriptors - Computing descriptors for subgraph " + j );
     *         subgraphDescSet(subgraphs.get(j), j, descriptorSet);
     *         <p/>
     *         }
     *         <p/>
     *         } else {
     *         // Compute the descriptor for the entire Graph
     *         subgraphDescSet(getVerticesList(), descriptorSet);
     *         }
     *         <p/>
     *         return descriptorSet;
     *         }
     *         <p/>
     *         /**
     *         Finds a subgraph and returns a pointer to the subgraph's root.
     * @result Returns a pointer the the subgraph's rootNode if the subgraph
     * is found, NULL otherwise.
     */
    List<GraphNode> findSubGraph(int subGraphRootId, List<List<GraphNode>> subgraphs) {
        //System.out.println("[++] findSubGraph: finding subgraph with root " + subGraphRootId);

        int i;
        for (i = 0; i < subgraphs.size(); i++) {
            if (subgraphs.get(i).get(0).getId() == subGraphRootId) {
                break;
            }
        }

        if (i < subgraphs.size()) {
            return subgraphs.get(i);
        } else {
            return null;
        }
    }

    /**
     * Find a node in the Graph.
     * Scans the nodeList for a Node with id nodeId. If the Node
     * is found, the argument "node" points to the correct Node
     * and the index of the Node in the nodeList is returned.
     * If the Node is *not* found, -1 is returned and "node" is set
     * to NULL.
     *
     * @param list   The list containing the Nodes.
     * @param nodeId The id of the Node to find.
     * @return Returns the index of the Node with nodeId if the Node
     *         is found, -1 otherwise.
     */
    V findNode(List<V> list, Long nodeId) {

        ////System.out.println("[++] findNode: looking for node " + nodeId );


        for (int i = 0; i < list.size(); i++) {
            ////System.out.println("[++] findNode: trying node" + list.get(i).getId());

            if (list.get(i).getId() == nodeId) {
                ////System.out.println("[++] findNode: found node");

                return list.get(i);
            }
        }

        return null;
    }

    public V findNode(Long nodeId) {
        return findNode(getVerticesList(), nodeId); // TODO - Should we use adjacency list byu default ?!
    }


    /**
     * Sets a Node's parent. This is done by first determining the old parent
     * and removing the child from the old parent's incList if possible.
     * The child is then added to the new parents incList.
     *
     * @param nodeId      The id of the Node to change the parent for.
     * @param newParentId The id of the new parent's Node.
     * @return Returns true iff the new parent was succesfully set.
     */
    public boolean setParent(Long nodeId, Long newParentId) {
        //System.out.println("[++] Graph::setParent - Setting the parent of [" + nodeId + "] to [" + newParentId + "]" );

        // Try to find the Nodes with id nodeId and newParentId
        GraphNode child = findNode(nodeId);
        GraphNode parent = null;
        GraphNode new_parent = findNode(newParentId);

        // If child and new_parent are found in nodesList, try to determine
        // the *parent* of the Node with id nodeId and remove the child from
        // this Node's incList.

        if (child != null && new_parent != null) {
            parent = findParent(nodeId, getVerticesList().get(0));

            if (parent != null) {

                //System.out.println("     Graph::setParent - Removing Node from inclist of Node(" + parent.getId() + ")");
                parent.remIncLink(child);

                // Add the child to the new parent's incList.
                //System.out.println("     Graph::setParent - Adding Node to inclist of Node(" + newParentId + ")");
                new_parent.addIncLink(child);

                // Setting the child's parent Node
                // TODO - Check if this is necessary - child.setParent(new_parent);

                return true;

            }
        }

        return false;
    }

    /**
     * Finds the parent of a Node by checking all incLists.
     *
     * @param nodeId Id of the Node to find the parent for.
     * @return Pointer to the parent-Node
     */
    GraphNode findParent(Long nodeId, GraphNode node) {
        GraphNode test = null;
        int res;

        // if nodeId is found in the incList, *this* node is the parent.
        test = findNode(node.getIncList(), nodeId);

        if (test != null)
            return node;


        // if nodeId was not found .. it should be somewhere in the
        // incList of one of the children. check all the children
        // of the current node
        test = null;
        int i;

        for (i = 0; i < node.getInclusionLinkCount(); i++) {
            test = findParent(nodeId, (GraphNode) node.getIncList().get(i));

            if (test != null)
                return test;
        }

        return null;

    }

    public void clearVerticesList() {
        nodesList = new ArrayList<V>();
    }

    public List<V> getVerticesList() {
        //CIList<GraphNode> list=new CIList<GraphNode>();
        //Set<GraphNode> vertexes=this.getVertices();
        //for(GraphNode v:vertexes) {
        //		list.push(v);
        //}
        return nodesList;
    }

    /**
     * Calculates the Graph's single- or multilevel-descriptor.
     * A descriptor is a list of Eigenvalues.
     *
     * @param levels If true descriptors for all levels will be calculated.
     * @return CIList of RowVectors. Each RowVector contains the descriptor for a level.
     *         <p/>
     *         public List<DoubleMatrix1D> getDescriptors(boolean levels) {
     *         <p/>
     *         //System.out.println( "[++] descriptors: for graph " + id);
     *         <p/>
     *         //if (levels)
     *         //System.out.println(" and its subgraphs");
     *         <p/>
     *         <p/>
     *         List<DoubleMatrix1D> descriptorSet = new ArrayList<DoubleMatrix1D>();
     *         <p/>
     *         if (levels) {
     *         // Isolate al subgraphs to a certain depth/level and store them in _subgraphs
     *         <p/>
     *         //System.out.println( "Graph:::descriptors - Isolating subgraphs to a level of " + MAXLEVEL );
     *         //isolateAllSubGraphs(MAXLEVEL);
     *         //System.out.println( "Graph:::descriptors - Isolating subgraphs" );
     *         List<List<GraphNode>> subgraphs=isolateAllSubGraphs();
     *         <p/>
     *         // Compute descriptors for all subgraphs
     *         int j;
     *         for(j=0; j < subgraphs.size(); j++) {
     *         //System.out.println(  "Graph:::descriptors - Computing descriptors for subgraph " + j );
     *         subgraphDescSet(subgraphs.get(j), j, descriptorSet);
     *         <p/>
     *         }
     *         <p/>
     *         } else {
     *         // Compute the descriptor for the entire Graph
     *         subgraphDescSet(getVerticesList(), descriptorSet);
     *         }
     *         <p/>
     *         return descriptorSet;
     *         }
     *         <p/>
     *         /**
     *         Prints the adjacency matrix for this graph.
     */
    public void printAdjacencyMatrix() {

        //System.out.println( "[++] printAdjacencyMatrix: created new symmetric matrix of size " + getVerticesList().getNumItems());

        DoubleMatrix2D sm = adjacencyMatrix(getVerticesList());

        //System.out.println(  "Printing adjacency matrix of graph: " + id);

        int i, j;
        for (i = 0; i < sm.rows(); i++) {
            for (j = 0; j < sm.columns(); j++) {
                // Element access is 0-based
                System.out.print(sm.get(i, j));
            }

        }

    }

    public List<List<V>> isolateAllSubGraphs() {
        return isolateAllSubGraphs(-1);
    }

    /**
     * Separate the graph into several subgraphs.
     * Only sub-graphs with more than MIN_NODES nodes are identified.
     */
    List<List<V>> isolateAllSubGraphs(int maxLevel) {
        int idx = 0;
        List<List<V>> subgraphs = new ArrayList<List<V>>();

        // Push the root Node
        subgraphs.add(getVerticesList());

        while (idx < subgraphs.size())
            isolateSubGraphs(subgraphs.get(idx++), maxLevel, subgraphs); // TODO - Check if subgraphs is changed

        return subgraphs;
    }

    /**
     * Separates the input graph into several subgraphs.
     * It only considers sub-graphs with more than MIN_NODES nodes.
     *
     * @param graphList List of Nodes that make up the Graph.
     */
    void isolateSubGraphs(List<V> graphList, int maxLevel, List<List<V>> subgraphs) {
        // Get the level1List of this subgraph. This list contains Nodes that are directly
        // under the root of the current subgraph.
        List<V> level1List = graphList.get(0).getIncList();

        // Iterate over all the nodes in the level1List. Check for each of them whether it
        // contains enough nodes to be a subgraph itself.
        int i;
        for (i = 0; i < level1List.size(); i++) {
            List<V> subList = new ArrayList<V>();

            V n1 = level1List.get(i);

            subList.add(n1);

            // A maxLevel of -1 indicates no attention should be paid to it. If the level of the
            // node is smaller then maxLevel, its children are processed.
            if ((maxLevel == -1) || (n1.getLevel() < maxLevel)) {
                int idx = 0;

                do {
                    subList.addAll(subList.get(idx++).getIncList()); //subList.joinListAfter(  );
                } while (idx < subList.size());

                if (subList.size() >= MIN_NODES)
                    subgraphs.add(subList);

            }
        }
    }

    /**
     * Computes a descriptor for the graph, using all eigenvalues.
     *
     * @param graphList    List containing the subgraphs for wich the descriptors
     *					have to be computed.
     *
    void subgraphDescSet(List<GraphNode> graphList,List<DoubleMatrix1D> descriptorSet) {
    levelDescriptor(graphList, 0, 0, descriptorSet);
    }


    /**
     * Computes the set of descriptors (one for each level) for a subgraph.
     *
     * The subgraph is specified by the list of nodes (graphList).
     *
     * @param graphList        List containing the subgraphs for wich the descriptors
     *						have to be computed.
     * @param subGraphID    The id of the subGraph for wich the descriptors
     *						have to be computed. (CHECK!)
     *
    void subgraphDescSet(List<GraphNode> graphList, int subGraphID, List<DoubleMatrix1D> descriptorSet) {

    int level=0;

    List<GraphNode> currSubGraph = new ArrayList<GraphNode> ();

    currSubGraph.add(graphList.get(0));  // adds the root node of the subgraph
    List<GraphNode> level1List = graphList.get(0).getIncList();
    currSubGraph.addAll(level1List);

    levelDescriptor(currSubGraph, level, subGraphID,descriptorSet);

    while (currSubGraph.size() != graphList.size()) {
    level++;
    List<GraphNode> newLevel = new ArrayList<GraphNode>();

    int i;
    for(i = 0; i < level1List.size(); i++) {
    newLevel.addAll( level1List.get(i).getIncList() );
    }

    currSubGraph.addAll(newLevel);

    levelDescriptor(currSubGraph, level, subGraphID, descriptorSet);

    if (level > 1) {
    //delete level1List;
    }

    level1List = newLevel;
    }
    }*/

    /**
     * Constructs the adjacency matrix of the Graph described by a
     * list of Nodes.
     *
     * @param list The Graph/Subgraph.
     */
    public static DoubleMatrix2D adjacencyMatrix(List<? extends GraphNode> list) {
        DoubleMatrix2D adjMatrix = DoubleFactory2D.sparse.make(list.size(), list.size());

        // Set all entries in the matrix to zero. This is necessary because not all
        // positions will be set by inclusion/adjacency values.
        //System.out.println( "Graph::adjacencyMatrix - Cleaning matrix" );

        // TODO - Do we need to clean the Matrix ? cleanMatrix(adjMatrix);

        // Check the matrix is of the correct size.
        assert (adjMatrix.rows() == adjMatrix.columns());
        assert (adjMatrix.rows() == list.size());


        // Define counters
        int i = 0;
        int j = 0;

        //* This map will contain the matrix row column indices for each node. *//*
        HashMap<GraphNode, Integer> indexMap = new HashMap<GraphNode, Integer>();

        // Assign indices to each Node*, these will be used to seed the graph with.
        for (i = 0; i < list.size(); i++) {
            GraphNode node = list.get(i);
            indexMap.put(node, i);
        }

        /* check each node in the graph
           * - for inclusion by every other node in the graph
           * - for adjacency with every other node in the graph
           */
        for (i = 0; i < list.size(); i++) {
            // Set the currentNode and get its Id
            GraphNode currentNode = list.get(i);

            int currentIndex = 0;

            //Iterator<GraphNode> it=indexMap.keySet().iterator();

            //while(it.hasNext() && it.next()!=currentNode); //it = indexMap.find(currentNode)

            if (indexMap.containsKey(currentNode)) {
                currentIndex = indexMap.get(currentNode);
                //System.out.println( "[++] adjacencyMatrix: current node with index " + currentIndex );
            }

            // Get the currentNode's primitive
            HasDistance onePrimitive = (HasDistance) currentNode.getData();  // TODO - enforce the data object to implement HasDistance
            //Double number = 1.0;

            /* Calculate the features of the current node's primitive
               if (onePrimitive.getNumPoints() > 0) {
                   Geometry g=new Geometry();
                   g.newScribble();
                   g.newStroke();

                   for (j = 0; j < onePrimitive.getNumPoints(); j++) {
                       g.addPoint(onePrimitive.getPoint(j).x, onePrimitive.getPoint(j).y);
                   }
                   ArrayList <Double> fVector = g.geometricFeatures(true);

                   // These features have to be processed into a number
                   number = featuresToNumber(fVector);

                   adjMatrix.set(currentIndex, currentIndex, 0);

                   *//*
				std::cout << "[adjacencyMatrix] calculated the number " << number << " for primitive " << currentIndex << std::endl;
							*//*
			} */

            // Do the inclusions ...
            List<GraphNode> includedNodes = currentNode.getIncList();

            for (j = 0; j < includedNodes.size(); j++) {
                GraphNode includedNode = includedNodes.get(j);

                if (indexMap.containsKey(includedNode)) {
                    int includedIndex = indexMap.get(includedNode);

                    //System.out.println("[++] adjacencyMatrix: found included node with index " + includedIndex );

                    try {
                        adjMatrix.set(currentIndex, includedIndex, 1);
                        adjMatrix.set(includedIndex, currentIndex, 1);
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                }
            }

            // Do the adjacencies
            List<GraphNode> adjacentNodes = currentNode.getAdjList();

            for (j = 0; j < adjacentNodes.size(); j++) {
                GraphNode adjacentNode = adjacentNodes.get(j);
                if (indexMap.containsKey(adjacentNode)) {
                    int adjacentIndex = indexMap.get(adjacentNode);

                    //System.out.println( "[++] adjacencyMatrix: found adjacent node with index " + adjacentIndex );

                    double seedValue = 0.0;

                    // primitive of current node already retrieved in enclosing scope
                    HasDistance otherPrimitive = (HasDistance) adjacentNode.getData();

                    if (onePrimitive != null && otherPrimitive != null) {
                        double dist = onePrimitive.getDistance(otherPrimitive);
                        seedValue = dist;
                    }

                    adjMatrix.set(currentIndex, adjacentIndex, seedValue);
                    adjMatrix.set(adjacentIndex, currentIndex, seedValue);
                }
            }
        }

        //printAdjacencyMatrix(adjMatrix);
        //System.out.println("[++] adjacencyMatrix - DONE" );
        return adjMatrix;
    }

    /**
     * Computes the eigenvalues of a symmetric matrix.
     */
    public static DoubleMatrix1D subgraphEigenvalues(DoubleMatrix2D matrix) { //, RowVector* &eigenVals
        //System.out.println( "subgraphEigenvalues: computing eigenvalues for " + matrix.rows()+ "x" +matrix.columns() + " matrix" );

        //RowVector vals(matrix->Ncols());
        //DiagonalMatrix D;

        DoubleMatrix1D vals = DoubleFactory1D.sparse.make(matrix.columns());
        EigenvalueDecomposition eigenValues = new EigenvalueDecomposition(matrix); //EigenValues(*matrix, D);

        DoubleMatrix2D D = eigenValues.getD();
        //System.out.println("subgraphEigenvalues: computed eigenvalues!");

        int c = 0;

        int i;
        for (i = 0; i < D.rows(); i++) {
            vals.set(i, Math.abs(D.get(i, i)) / MAXVAL);

            // vals(i) cannot be bigger then 1 since NBtree expects its values to be between (0,1).
            if (vals.get(i) > 1) {
                //System.out.println( "[!!] Graph::subgraphEigenvalues - Warning! Value bigger than 1 !! - " + vals.get(i));
                //exit(1);
            }

            if (vals.get(i) < 0.001 / MAXVAL) {
                vals.set(i, 0);
            } else {
                c++;
            }
        }

        // descending order
        return vals.viewSorted().viewFlip();

        /*
          eigenVals = new RowVector(c);

          for (i=1; i <= c; i++) {
              (*eigenVals)(i) = vals(i);
          }*/
    }

    /**
     * Transforms the passed feature vector to a single number.
     *
     * @param fVector the vector to transform (with on its first position a special value)
     * @return the magic number
     */
    Double featuresToNumber(ArrayList<Double> fVector) {
        Double returnValue = 0.0;

        //int numberOfNumbers = fVector.get(0).intValue();
        int i;
        for (i = 0; i < fVector.size(); i++)
            returnValue += (i * fVector.get(i));

        return returnValue;
    }

    /**
     * Computes a descriptor for a level.
     *
     * @param currSubGraph The current subgraph
     * @param level        The level to compute?
     * @param subGraphID   The id of the subgraph for which to compute the descriptor?
     *                     <p/>
     *                     void levelDescriptor(List<GraphNode> currSubGraph, int level, int subGraphID, List<DoubleMatrix1D> descriptorSet) {
     *                     //System.out.println("[++] Graph::levelDescriptor - Computing leveldescriptor for level [" + level + "] with subGraphID [" + subGraphID + "]");
     *                     //System.out.println("[++] Graph::levelDescriptor - getNumItems =" + currSubGraph.getNumItems());
     *                     <p/>
     *                     <p/>
     *                     if (currSubGraph.size() > 1) {
     *                     <p/>
     *                     DoubleMatrix2D adjMatrix;
     *                     //(currSubGraph->getNumItems());
     *                     <p/>
     *                     <p/>
     *                     //System.out.println("[++] levelDescriptor: computing adjacency matrix" );
     *                     adjMatrix=adjacencyMatrix(currSubGraph);
     *                     <p/>
     *                     DoubleMatrix1D eig=	subgraphEigenvalues(adjMatrix);
     *                     <p/>
     *                     DoubleMatrix1D vals=DoubleFactory1D.sparse.make(eig.size() + NIDS);
     *                     <p/>
     *                     vals.set(1, id);
     *                     vals.set(2, subGraphID);
     *                     vals.set(3, level);
     *                     <p/>
     *                     //System.out.println("[++] Graph::levelDescriptor - creating RowVector id " +  id + " subGraphID " + subGraphID + " level " + level);
     *                     <p/>
     *                     int j;
     *                     for(j = 0; j < eig.size(); j++) {
     *                     vals.set(j+NIDS,eig.get(j));
     *                     //System.out.println("[++] levelDescriptor: pushing value " + eig.get(j) + " to RowVector" );
     *                     }
     *                     <p/>
     *                     descriptorSet.push(vals);
     *                     <p/>
     *                     //delete adjMatrix;
     *                     //delete eig;
     *                     }
     *                     <p/>
     *                     }
     */

    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public void FloydWarshall() {
        // TODO Auto-generated method stub

    }

    public Graph copy() {
        Graph newGraph = null;
        try {
            newGraph = (Graph) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Couldn't copy the Graph");
        }

        // Clear the old list!
        newGraph.clearVerticesList();

        for (int i = 0; i < nodesList.size(); i++) {
            GraphNode n = nodesList.get(i);
            for (Object o : newGraph.getVertices()) {
                if (((GraphNode) o).getId() == n.getId()) {
                    newGraph.getVerticesList().add((GraphNode) o);
                }
            }
        }

        return newGraph;
    }
}
