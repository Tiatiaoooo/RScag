/*
 * Scagnostics
 *
 * Leland Wilkinson and Anushka Anand (University of Illinois at Chicago)
 * This program accompanies the following paper:
 
 * Wilkinson L., Anand, A., and Grossman, R. (2006). High-Dimensional visual analytics: 
 *   Interactive exploration guided by pairwise views of point distributions. 
 *   IEEE Transactions on Visualization and Computer Graphics, November/December 2006 (Vol. 12, No. 6) pp. 1363-1372.
 *
 * Permission to use, copy, modify, and distribute this software for any
 * purpose without fee is hereby granted, provided that this entire notice
 * is included in all copies of any software which is or includes a copy
 * or modification of this software.
 * Supporting documentation must also include a citation of
 * the abovementioned article.
 * THIS SOFTWARE IS BEING PROVIDED "AS IS", WITHOUT ANY EXPRESS OR IMPLIED
 * WARRANTY.  IN PARTICULAR, THE AUTHORS MAKE NO
 * REPRESENTATION OR WARRANTY OF ANY KIND CONCERNING THE MERCHANTABILITY
 * OF THIS SOFTWARE OR ITS FITNESS FOR ANY PARTICULAR PURPOSE.
 */
package scagnostics;

import javax.swing.text.html.HTMLDocument;
import java.util.*;
import java.util.List;

class Node {
    protected int x, y;          // coordinate X,Y
    protected double count;        // number of points aggregated at this node
    protected Edge anEdge;     // an edge which starts from this node
    protected List neighbors;   // nearest Delaunay neighbors list
    protected boolean onMST;
    protected boolean onHull = false;
    protected boolean isVisited = false;
    protected int mstDegree;
    protected int pointID;
    protected int nodeID;
    protected int degree;
    protected boolean isVisitedonGraph=false;
    protected boolean isOver=false;

    protected Node(int x, int y, double count, int pointID) {
        this.x = x;
        this.y = y;
        this.count = count;
        anEdge = null;
        neighbors = new ArrayList();
        this.pointID = pointID;
    }

    protected double distToNode(double px, double py) {
        double dx = px - x;
        double dy = py - y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    protected void setNeighbor(Edge neighbor) {
        neighbors.add(neighbor);
    }

    protected Iterator getNeighborIterator() {
        return neighbors.iterator();
    }

    protected Edge shortestEdge(boolean mst) {
        Edge emin = null;
        if (neighbors != null) {
            Iterator it = neighbors.iterator();
            double wmin = Double.MAX_VALUE;
            while (it.hasNext()) {
                Edge e = (Edge) it.next();
                if (mst || !e.otherNode(this).onMST) {
                    double wt = e.weight;
                    if (wt < wmin) {
                        wmin = wt;
                        emin = e;
                    }
                }
            }
        }
        return emin;
    }
    protected int getMstDegree(){
        degree=0;
        if (neighbors!=null)
        {
            Iterator it = neighbors.iterator();
            while (it.hasNext())
            {
                Edge e = (Edge)it.next();
                if (e.onMST)
                {
                    degree++;
                }
            }
        }
        return degree;
    }
    protected int getOriMstDegree(){
        degree=0;
        if (neighbors!=null)
        {
            Iterator it = neighbors.iterator();
            while (it.hasNext())
            {
                Edge e = (Edge)it.next();
                if (e.onoriMST)
                {
                    degree++;
                }
            }
        }
        return degree;
    }
    public Vector<Edge> findPathto(Node p){
        Stack<Node> path = new Stack<>();
        path.push(this);
        this.isVisitedonGraph = true;
        dfsLoop(p,path);
        Vector<Edge> edge_path = new Vector<>();
        for(int i=1;i<path.size();i++)
        {
            Edge cur_edge = new Edge(path.get(i-1),path.get(i));
            edge_path.add(cur_edge);
        }
        return edge_path;
    }
    public boolean dfsLoop(Node target,Stack<Node> path){
        boolean findtarget = false;
        if(path.empty())
        {
            return findtarget;
        }
        Node p = path.peek();
        if(p.distToNode(target.x,target.y)==0)
        {
            findtarget = true;
        }
        Iterator it = p.neighbors.iterator();
        List<Node> neigh_node=new ArrayList<>();
        while (it.hasNext())
        {
            Edge e = (Edge) it.next();
            if(e.onoriMST)
            {
                Node p2 = e.otherNode(p);
                neigh_node.add(p2);
            }
        }
        int nei_sz = neigh_node.size();
        int i;
        for(i =0 ;i<nei_sz;i++){
            Node cur_nd = neigh_node.get(i);
            if(!cur_nd.isVisitedonGraph)
            {
                path.push(cur_nd);
                cur_nd.isVisitedonGraph=true;
                findtarget=dfsLoop(target,path);
            }if(findtarget)
            {
                break;
            }
        }
        if(!findtarget)
        {
            path.pop();
        }
        return findtarget;
    }
    public int getMSTChildren(double cutoff, double[] maxLength, Vector<Node> childNodes, Edge[] maxEdge) {
        int count = 0;
        if (isVisited)
            return count;
        isVisited = true;
        Iterator it = neighbors.iterator();
        while (it.hasNext()) {
            Edge e = (Edge) it.next();
            if (e.onMST) {
                if (e.weight <= cutoff) {
                    if (!e.otherNode(this).isVisited) {
                        count += e.otherNode(this).getMSTChildren(cutoff, maxLength, childNodes, maxEdge);
                        double el = e.weight;
                        if (el > maxLength[0])
                        {
                            maxLength[0] = el;
                            maxEdge[0] = e;
                        }
                    }
                }
            }
        }
        count += this.count; // add count for this node
        childNodes.add(this);
        return count;
    }
    public int getMSTCount(Vector<Node> nodes)
    {
        int count=0;
        if(isVisited)
        {
            System.out.println("Visited");
            return count;
        }
        isVisited = true;
        Iterator it = neighbors.iterator();
        while (it.hasNext())
        {
            Edge e = (Edge)it.next();
            if(e.onMST)
            {
                if(!e.otherNode(this).isVisited)
                {
                    count+=e.otherNode(this).getMSTCount(nodes);
                }
            }
        }
        count+=this.count;
        nodes.add(this);
        return count;
    }
}