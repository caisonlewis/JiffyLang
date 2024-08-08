package class_helper;
import java.util.ArrayList;

public class ParseTree {
	private TreeNode root = null;

	public ParseTree(TreeNode t) {
		root = t;
	}

	public TreeNode getRootNode() {
		return root;
	}

	public String toString() {
		if (root != null)
			return pretty_print(root, 0);
		else
			return "No tree";
	}

	private String pretty_print(TreeNode current_node, int offset) {
		int spacer = 5;
		String print_string = "";

		if (current_node.isLeaf()) { // base case
			String indent = makeString(offset, " ");
			String dots = current_node.isLeaf() ? "" : makeString(spacer, ".");

			print_string += "\n" + indent + current_node.getSymbol() + dots;
			return print_string;
		} else {
			ArrayList<TreeNode> c = current_node.getChildren();
			int i = c.size() - 1;
			while (i >= 0) {
				TreeNode tn = c.get(i); // start with the rightmost child
				if (tn != null)
					print_string += pretty_print(tn, offset + spacer + current_node.getSymbol().length() - 1);

				if (i == c.size() / 2) { // roughly midway through children, add parent node
					String indent = makeString(offset, " ");
					String dots = current_node.isLeaf() ? "" : makeString(spacer, ".");

					print_string += "\n" + indent + current_node.getSymbol() + dots;
				}
				i = i - 1; // next child

			}
			return print_string;
		}
	}

	private String makeString(int length, String s) {
		String result = "";

		for (int i = 0; i < length; i++) {
			if (s == "." && i == length - 1)
				result += "|";
			else
				result += s;
		}

		return result;
	}

}
