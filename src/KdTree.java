/*************************************************************************
  *  Compilation:  javac KdTree.java
  *  Execution:    java KdTree
  *  Dependencies: StdIn.java StdOut.java  
  *
  *  KD tree
  * 
  *************************************************************************/
public class KdTree {
	private Node parent;                       // root of KdTree
	private int N;                             // size of tree
	private static final int DIM = 2;          // Dimension, 2 for this application (x,y)
	private Queue<Point2D> pointsInRange;
	
	private static class Node {
		   private Point2D point;              // the point
		   private RectHV rect;                // the axis-aligned rectangle corresponding to this node
		   private Node lb = null;             // the left/bottom subtree
		   private Node rt = null;             // the right/top subtree
		   private int partDim;                // partition dimension, value ranges from 0 to DIM - 1
		                     
		   public Node(Point2D p, int dimension) {
			   this.point = p;
			   this.partDim = dimension;
		   }
		   
		   private int compareTo(Point2D other) {
			   if (this.partDim != 0) return this.point.compareTo(other);
			   if (this.point.x() < other.x()) return -1;
			   if (this.point.x() > other.x()) return +1;
			   if (this.point.y() < other.y()) return -1;
			   if (this.point.y() > other.y()) return +1;
			   return 0;
		   }
	}
	
	public KdTree() {
		this.N = 0;
		this.parent = null;
	}
	
	// is the set empty?
	public boolean isEmpty() {
		return this.N == 0;
	}
	
	// number of points in the set   
	public int size() {
		return this.N;
	}
	
	// add the point p to the set (if it is not already in the set)
	public void insert(Point2D p) {
		if (p == null) return;
		this.parent = insert(this.parent, null, p, 0);
		return;
	}
	// helper method for insert
	private Node insert(Node root, Node parentNode, Point2D p, int currDim) {
		if (root == null) {
			Node n = new Node(p, currDim);
			this.N++;
			if (N == 1) n.rect = new RectHV(0, 0, 1, 1);
			else n.rect = setRect(n, parentNode);
			return n;
		}
		
		int cmp = root.compareTo(p);
		if (cmp == 0) {
			root.point = p;
		}
		else if (cmp > 0) {
			root.lb = insert(root.lb, root, p, (currDim + 1) % DIM);
		}
		else {
			root.rt = insert(root.rt, root, p, (currDim + 1) % DIM);
		}
		return root;
	}
	
	// Set Rectangle for new node being inserted into tree
	private RectHV setRect(Node childNode, Node parentNode) {
		RectHV myRect;
		int cmp = parentNode.compareTo(childNode.point);
		if (cmp > 0) {
			if (parentNode.partDim == 0) {
				myRect = new RectHV(parentNode.rect.xmin(), 
						parentNode.rect.ymin(),
						parentNode.point.x(),
						parentNode.rect.ymax());
			}
			else {
				myRect = new RectHV(parentNode.rect.xmin(),
						parentNode.rect.ymin(),
						parentNode.rect.xmax(),
						parentNode.point.y());
			}
		}
		else {
			if (parentNode.partDim == 0) {
				myRect = new RectHV(parentNode.point.x(),
						parentNode.rect.ymin(),
						parentNode.rect.xmax(),
						parentNode.rect.ymax());		
			}
			else {
				myRect = new RectHV(parentNode.rect.xmin(),
						parentNode.point.y(),
						parentNode.rect.xmax(),
						parentNode.rect.ymax());
			}
	
		}
		return myRect;
	}
	
	// does the set contain the point p?
	public boolean contains(Point2D p) {
		if (this.parent == null) return false;
		if (p == null) return false;
		return contains(this.parent, p);	
	}
	// helper method for contains
	private boolean contains(Node root, Point2D p) {
		if (root == null) return false;
		int cmp = root.compareTo(p);
		if (cmp < 0) return contains(root.rt, p);
		else if (cmp > 0) return contains(root.lb, p);
		else return true;
	}
	
	// all points in the set that are inside the rectangle
	public Iterable<Point2D> range(RectHV rect) {
		pointsInRange = new Queue<Point2D>();
	    range(this.parent, rect);
	    return pointsInRange;
	}
	// helper method for range
	private void range(Node root, RectHV rect) {
		if (root == null) return;
		if (root.rect.intersects(rect)) {
			if (rect.contains(root.point))
				pointsInRange.enqueue(root.point);
			range(root.lb, rect);
			range(root.rt, rect);
		}
		return;
	}
	
	// a nearest neighbor in the set to p; null if set is empty	
	public Point2D nearest(Point2D q) {
		if (q == null) return null;
		if (this.parent == null) return null;
        return nearest(this.parent, q, 0, null, Double.POSITIVE_INFINITY);
    }
	// nearest helper method
	private Point2D nearest(Node root, Point2D q, int currDim, Point2D currMin, Double currBest) {
        if (root == null) return currMin;
        
        double dist = root.point.distanceSquaredTo(q);  // current distance to query point
        if (dist < currBest ) {                         // if current distance is less than current best, update
        	currMin = root.point;
        	currBest = dist;
        }
        
        int cmp = root.compareTo(q);
        if (cmp == 0) return currMin;
        else if (cmp > 0) {                                  // q is closer to left child
        	currMin = nearest(root.lb, q, (currDim + 1) % DIM, currMin, currBest);
        	if (root.rt != null && getAxisSplit(root.rt, root).distanceSquaredTo(q) < currMin.distanceSquaredTo(q)) {
        		currMin = nearest(root.rt, q, (currDim + 1) % DIM, currMin, currBest);
        	}
        }
        else {                                          // q is closer to right child
        	currMin = nearest(root.rt, q, (currDim + 1) % DIM, currMin, currBest);
        	if (root.lb != null && getAxisSplit(root.lb, root).distanceSquaredTo(q) < currMin.distanceSquaredTo(q)) {
        		currMin = nearest(root.lb, q, (currDim + 1) % DIM, currMin, currBest);
        	}
        }
        return currMin;
	}
	
	// get the axis aligned rectangle of the node that splits the rectangle it resides in
	private RectHV getAxisSplit(Node child, Node root) {
		RectHV axisSplit;
		int cmp = root.compareTo(child.point);
		if (cmp > 0) {
			if (child.partDim == 0) {
				axisSplit = new RectHV(child.point.x(), 
					root.rect.ymin(),
					child.point.x(),
					root.point.y());
			}
			else {
				axisSplit = new RectHV(root.rect.xmin(), 
						child.point.y(),
						root.point.x(),
						child.point.y());
			}
		}
		else {
			if (child.partDim == 0) {
				axisSplit = new RectHV(child.point.x(), 
						root.point.y(),
						child.point.x(),
						root.rect.ymax());	
			}
			else {
				axisSplit = new RectHV(root.point.x(), 
						child.point.y(),
						root.rect.xmax(),
						child.point.y());
			}
		}
		return axisSplit;
	}
	
	// draw all of the points to standard draw
	public void draw() {
		if(this.parent == null) return;
		StdDraw.setPenColor(StdDraw.BLACK);
		StdDraw.setPenRadius();
		StdDraw.setScale(0, 1);
		RectHV box = new RectHV(0, 0, 1, 1);
		box.draw();
		draw(this.parent, null);
	}
	// draw helper method
	private void draw(Node child, Node parent) {
		if (child == null) return;
		
		StdDraw.setPenColor(StdDraw.BLACK);
		StdDraw.setPenRadius(0.01);
		child.point.draw();
		
		if (child.partDim == 0) StdDraw.setPenColor(StdDraw.RED);
		else StdDraw.setPenColor(StdDraw.BLUE);
		
		StdDraw.setPenRadius();

		if (parent == null) {
			RectHV splitLine = new RectHV(child.point.x(), 0, child.point.x(), 1);
			splitLine.draw();
		}
		else {
			RectHV splitLine = getAxisSplit(child, parent);
			splitLine.draw();
		}
		
		draw(child.lb, child);
		draw(child.rt, child);		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
        String filename = args[0];
        In in = new In(filename);

        StdDraw.show();

        // initialize the data structures with N points from standard input
        KdTree kdtree = new KdTree();
        while (!in.isEmpty()) {
            double x = in.readDouble();
            double y = in.readDouble();
            Point2D p = new Point2D(x, y);
            kdtree.insert(p);
        }
        kdtree.draw();
        StdDraw.show();
	}

}