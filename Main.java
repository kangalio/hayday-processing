import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.xml.parsers.ParserConfigurationException;
import java.util.HashMap;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.OutputKeys;

import java.io.BufferedReader;
import java.io.FileReader;

public class Main {
	public static void main(String[] args) {
		System.out.print("Reading file... ");
		HayDay root = parseFile(new File("/home/kangalioo/devel/java/hayday processing/hayday.xml"));
		Item[] items = root.items;
		ProductionBuilding[] buildings = root.productionBuildings;
		Tree[] trees = root.trees;
		System.out.println("done");
		System.out.print("Processing tree structure... ");
		
		
		System.out.println("done");
		System.out.print("Saving file... ");
		writeFile(new File("result.xml"), root);
		System.out.println("done");
	}
	
	public static Item findItem(Item[] items, String name) {
		for (Item item : items) {
			if (item.name.equals(name)) {
				return item;
			}
		}
		return null;
	}
	
	public static HayDay parseFile(File xmlFile) {
		Document doc =  null;
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlFile);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Element root = doc.getDocumentElement();
		root.normalize();
		
		NodeList nodeList;
		Element e;
		
		nodeList = ((Element) root.getElementsByTagName("productionBuildingList").item(0)).getElementsByTagName("productionBuilding");
		ProductionBuilding[] buildings = new ProductionBuilding[nodeList.getLength()];
		HashMap<String, ProductionBuilding> buildingMap = new HashMap<>();
		
		e = (Element) nodeList.item(0);
		for (int i = 0; i < nodeList.getLength(); e = ((Element) nodeList.item(++i))) {
			ProductionBuilding building = new ProductionBuilding();
			buildings[i] = building;
			building.name = e.getAttribute("name"); // REMEMBER
			building.buildTime = Integer.parseInt(e.getAttribute("buildTime"));
			building.cost = Integer.parseInt(e.getAttribute("cost"));
			building.experiencePoints = Integer.parseInt(e.getAttribute("experiencePoints"));
			building.levelAvailable = Integer.parseInt(e.getAttribute("levelAvailable"));
			building.initialSlots = Integer.parseInt(e.getAttribute("initialSlots"));
			buildingMap.put(building.name, building);
		}
		
		nodeList = ((Element) root.getElementsByTagName("treeList").item(0)).getElementsByTagName("tree");
		Tree[] trees = new Tree[nodeList.getLength()];
		HashMap<String, Tree> treeMap = new HashMap<>();
		
		e = (Element) nodeList.item(0);
		for (int i = 0; i < nodeList.getLength(); e = ((Element) nodeList.item(++i))) {
			Tree tree = new Tree();
			trees[i] = tree;
			tree.name = e.getAttribute("name");
			treeMap.put(tree.name, tree);
			tree.type = e.getAttribute("type").equals("tree") ? Tree.Type.TREE : Tree.Type.BUSH;
			tree.cost = Integer.parseInt(e.getAttribute("cost"));
			tree.levelAvailable = Integer.parseInt(e.getAttribute("levelAvailable"));
			tree.growTime = Integer.parseInt(e.getAttribute("growTime"));
			tree.fruit = new Item();
			tree.fruit.name = e.getAttribute("fruit");
		}
		
		nodeList = ((Element) root.getElementsByTagName("itemList").item(0)).getElementsByTagName("item");
		Item[] items = new Item[nodeList.getLength()];
		HashMap<String, Item> itemMap = new HashMap<>();
		
		e = (Element) nodeList.item(0);
		for (int i = 0; i < nodeList.getLength(); e = ((Element) nodeList.item(++i))) {
			Item item = new Item();
			item.type = Item.stringToType(e.getAttribute("type"));
			item.name = e.getAttribute("name");
			String source = e.getAttribute("source");
			if (item.type == Item.Type.GOOD) {
				item.source = buildingMap.get(source);
				if (item.source == null) System.out.println(item.name);
			} else {
				item.source = new ProductionBuilding();
				item.source.name = source;
			}
 			item.levelAvailable = Integer.parseInt(e.getAttribute("levelAvailable"));
			item.time = Integer.parseInt(e.getAttribute("time"));
			item.experiencePoints = Integer.parseInt(e.getAttribute("experiencePoints"));
			if (Item.isSellable(item.type)) {
				item.price = Integer.parseInt(e.getAttribute("price"));
			}
			
			items[i] = item;
			itemMap.put(item.name, item);
			
			NodeList nodeList2 = e.getElementsByTagName("requiredResource");
			item.requiredResources = new Item[nodeList2.getLength()];
			item.resourceAmounts = new int[nodeList2.getLength()];
			for (int j = 0; j < nodeList2.getLength(); j++) {
				Element elem = (Element) nodeList2.item(j);
				if (elem.hasAttribute("name")) {
					Item nameItem = new Item();
					nameItem.name = elem.getAttribute("name");
					item.requiredResources[j] = nameItem;
				} else {
					Item typeItem = new Item();
					typeItem.type = Item.stringToType(elem.getAttribute("type"));
					item.requiredResources[j] = typeItem;
				}
				String amountString = elem.getAttribute("amount");
				item.resourceAmounts[j] = amountString.equals("") ? -1 : Integer.parseInt(amountString);
			}
		}
		
		for (Tree tree : trees) {
			tree.fruit = itemMap.get(tree.fruit.name);
		}
		
		for (Item item : items) {
			for (int i = 0; i < item.requiredResources.length; i++) {
				if (item.requiredResources[i].name != null) {
					item.requiredResources[i] = itemMap.get(item.requiredResources[i].name);
				}
			}
		}
		
		return new HayDay(items, buildings, trees);
	}
	
	public static void writeFile(File xmlFile, HayDay hayDay) {
		Item[] items = hayDay.items;
		ProductionBuilding[] buildings = hayDay.productionBuildings;
		Tree[] trees = hayDay.trees;
		Document doc = null;
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		
		Element root = doc.createElement("hayDay");
		doc.appendChild(root);
		
		Element itemList = doc.createElement("itemList");
		root.appendChild(itemList);
		
		for (Item item : items) {
			Element elem = doc.createElement("item");
			elem.setAttribute("type", Item.typeToString(item.type));
			elem.setAttribute("name", item.name);
			elem.setAttribute("time", Integer.toString(item.time));
			elem.setAttribute("levelAvailable", Integer.toString(item.levelAvailable));
			if (Item.isSellable(item.type)) elem.setAttribute("price", Integer.toString(item.price));
			elem.setAttribute("experiencePoints", Integer.toString(item.experiencePoints));
			elem.setAttribute("source", item.source.name);
			itemList.appendChild(elem);
			
			if (item.type == Item.Type.GOOD || item.type == Item.Type.ANIMAL_GOOD) {
				for (int i = 0; i < item.requiredResources.length; i++) {
					Element resource = doc.createElement("requiredResource");
					if (item.requiredResources[i].name == null) {
						String type = Item.typeToString(item.requiredResources[i].type);
						resource.setAttribute("type", type);
					} else {
						resource.setAttribute("name", item.requiredResources[i].name);
					}
					resource.setAttribute("amount", Integer.toString(item.resourceAmounts[i]));
					elem.appendChild(resource);
				}
			}
		}
		
		Element buildingList = doc.createElement("productionBuildingList");
		root.appendChild(buildingList);
		
		for (ProductionBuilding building : buildings) {
			Element elem = doc.createElement("productionBuilding");
			elem.setAttribute("name", building.name);
			elem.setAttribute("buildTime", Integer.toString(building.buildTime));
			elem.setAttribute("cost", Integer.toString(building.cost));
			elem.setAttribute("experiencePoints", Integer.toString(building.experiencePoints));
			elem.setAttribute("levelAvailable", Integer.toString(building.levelAvailable));
			elem.setAttribute("initialSlots", Integer.toString(building.initialSlots));
			buildingList.appendChild(elem);
		}
		
		Element treeList = doc.createElement("treeList");
		root.appendChild(treeList);
		
		for (Tree tree : trees) {
			Element elem = doc.createElement("tree");
			elem.setAttribute("name", tree.name);
			elem.setAttribute("type", Tree.typeToString(tree.type));
			elem.setAttribute("cost", Integer.toString(tree.cost));
			elem.setAttribute("growTime", Integer.toString(tree.growTime));
			elem.setAttribute("levelAvailable", Integer.toString(tree.levelAvailable));
			elem.setAttribute("fruit", tree.fruit.name);
			treeList.appendChild(elem);
		}
		
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATIO‌​N, "yes");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(xmlFile);
			
			transformer.transform(source, result);
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}
}
