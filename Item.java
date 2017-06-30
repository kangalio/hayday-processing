public class Item {
	// TREE_CROPs have 4 stages which take "time" minutes to pass, and give 2, 3, 4 and 4 crops
	// in each stage, so the total time of them is time * 4 and the total win is not 1 fruit but 13
	public enum Type {
		CROP, TREE_CROP, ANIMAL_GOOD, GOOD, LURE, DIAMOND, ORE, MINING_TOOL, OTHER;
	}
	
	public String name = null;
	public ProductionBuilding source = null;
	public Type type = null;
	public int time = -1, experiencePoints = -1, levelAvailable = -1;
	public int price = -1;
	public Item[] requiredResources = new Item[0];
	public int[] resourceAmounts = new int[0];
	
	public String toString() {
		return name;
	}
	
	public static Type stringToType(String type) {
		if (type.equals("crop")) return Type.CROP;
		else if (type.equals("animal good")) return Type.ANIMAL_GOOD;
		else if (type.equals("good")) return Type.GOOD;
		else if (type.equals("lure")) return Type.LURE;
		else if (type.equals("tree crop")) return Type.TREE_CROP;
		else if (type.equals("diamond")) return Type.DIAMOND;
		else if (type.equals("ore")) return Type.ORE;
		else if (type.equals("mining tool")) return Type.MINING_TOOL;
		else if (type.equals("other")) return Type.OTHER;
		else return null;
	}
	
	public static String typeToString(Type type) {
		if (type == Type.CROP) return "crop";
		else if (type == Type.ANIMAL_GOOD) return "animal good";
		else if (type == Type.GOOD) return "good";
		else if (type == Type.LURE) return "lure";
		else if (type == Type.TREE_CROP) return "tree crop";
		else if (type == Type.DIAMOND) return "diamond";
		else if (type == Type.ORE) return "ore";
		else if (type == Type.MINING_TOOL) return "mining tool";
		else if (type == Type.OTHER) return "other";
		else return null;
	}
	
	public static boolean isSellable(Type type) {
		return (type == Type.CROP || type == Type.TREE_CROP
				|| type == Type.GOOD || type == Type.ANIMAL_GOOD
				|| type == Type.ORE || type == Type.MINING_TOOL);
	}
	
	public double getTotalIngredientValue() {
		if (requiredResources.length == 0) return price;
		
		int value = 0;
		for (int i = 0; i < requiredResources.length; i++) {
			value += requiredResources[i].getTotalIngredientValue() * resourceAmounts[i];
		}
		
		return value;
	}
}
