
public class PointSET {
	private SET<Point2D> mySet;
	
	public PointSET() {
		// construct an empty set of points
	    this.mySet = new SET<Point2D>();
	}
	   
	public boolean isEmpty() {
		// is the set empty?
		return this.mySet.isEmpty();
	}
	   
	public int size() {
		// number of points in the set
		return this.mySet.size();
	}
	
	public void insert(Point2D p) {
		if (p == null) return;
		
		// add the point p to the set (if it is not already in the set)
		if (!this.mySet.contains(p)) this.mySet.add(p);
		
		return;
	}
	   
	public boolean contains(Point2D p) {
		// does the set contain the point p?
		return this.mySet.contains(p);
	   }
	   
	public void draw() {
		// draw all of the points to standard draw
		for(Point2D p : this.mySet) {
			p.draw();
		}	
	}
	
	public Iterable<Point2D> range(RectHV rect) {
		// all points in the set that are inside the rectangle
		if (rect == null) return null;
		
		Queue<Point2D> insideRect = new Queue<Point2D>();
		
		for (Point2D p : this.mySet) {
			if (rect.contains(p)) insideRect.enqueue(p);
		}
		return insideRect;
	}
	
	public Point2D nearest(Point2D p) {
		// a nearest neighbor in the set to p; null if set is empty
		if (this.mySet == null || this.mySet.isEmpty() || p == null) return null;
		
		Point2D min = this.mySet.max();
		
		for (Point2D currPoint : this.mySet) {
			if (currPoint.distanceTo(p) < min.distanceTo(p)) min = currPoint;
		}
	    return min;
	}
	
//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//		PointSET pset = new PointSET();
//		Point2D p = new Point2D(0.2, 0.3);
//		RectHV rect = new RectHV(0.2, 0.2, 0.6, 0.6);
//		pset.insert(p);
//		for (int i = 0; i < 50; i++)
//			pset.insert(new Point2D(StdRandom.uniform(), StdRandom.uniform()));
//		rect.draw();
//		StdDraw.circle(p.x(), p.y(), p.distanceTo(pset.nearest(p)));
//		pset.draw();
//		StdDraw.show(0);
//		StdOut.println("Nearest to " + p.toString() + " = " + pset.nearest(p));
//		for (Point2D point : pset.range(rect))
//			StdOut.println("In Range: " + point.toString());
//
//	}

}
