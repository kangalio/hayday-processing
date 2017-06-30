// This class is also used for bushes
public class Tree {
	public enum Type {
		TREE, BUSH;
	}
	
	public int cost = -1, growTime = -1, levelAvailable = -1;
	public String name = null;
	public Type type = null;
	public Item fruit = null;
	
	public static String typeToString(Type type) {
		return type.toString().toLowerCase();
	}
}
