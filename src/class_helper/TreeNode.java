package class_helper;
import java.util.ArrayList;

public class TreeNode {
	private String symbol;
	private ArrayList<TreeNode> children;
	
	public TreeNode(String symbol, TreeNode left, TreeNode right) {
		children = new ArrayList<TreeNode>();
		this.symbol = symbol;
		if (left != null) {
			children.add(left);
		}
		if (right != null) {
			children.add(right);
		}
	}

	public ArrayList<TreeNode> getChildren() {
		//Returns all children of this node
		return children;
	}

	public void add(TreeNode t) {
		children.add(t);
	}

	public TreeNode(String symbol) {
		this(symbol, null, null);
	}

	public TreeNode getLeft() {
		//Returns the first (leftmost) child
		return getChildren().get(0);
	}

	public boolean hasRight() {
		return getChildren().size() > 1;
	}
	
	public TreeNode getRight() {
		//Returns the second child
		return getChildren().get(1);
	}

	public String toString() {
		return symbol;
	}

	public boolean isLeaf() {
		return children.size() == 0;
	}

	public String getSymbol() {
		return symbol;
	}

}
