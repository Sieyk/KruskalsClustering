
/*
	Student Name: Matthew Muller
	Student Number: c3184660
	Course: COMP2230
 */

import java.util.*;
import java.io.*;
import java.text.DecimalFormat;

public class KruskalsClustering {

	public static void main(String[] args) {
		System.out.println("Hello and welcome to Kruskal's Clustering!\n");
		BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in)); //Reads user input
		String str = null; //Used for storing the current working line from the input file
		List<double[]> kruskal = null; //Stores the edge list derived from kruskal's algorithm
		List<double[]> distList = null; //Stores the complete edge list of the graph
		int input = 0; //Stores user input
		String[] tempString = null; //Temporarily stores the split line from the input file
		double[] tempHotspot = null; //Stores the converted split line from tempString
		List<double[]> hotspots = new LinkedList<double[]>(); //Stores all of the hotspots
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(args[0])); //args[0] is expected to be the file
			while((str = br.readLine()) != null){ //Reads a line and then checks if the line was null
				tempString = str.split("\\s+"); //Split on white space
				tempHotspot = new double[3];
				for(int i = 0; i < 3; i++){	//Storing the data read from the file
					tempHotspot[i] = Double.parseDouble(tempString[i]);
				}
				hotspots.add(tempHotspot);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("There are "+hotspots.size()+" hotspots.\n");
		
		distList = new LinkedList<double[]>(); //Stores in this format: [vertex1,vertex2,distance between vertices]
		for(int x = 0; x < hotspots.size(); x++){
			for(int y = 0; y < hotspots.size(); y++){
				if(x != y){
					//Uses the vertex distance formula: sqrt((x2-x1)^2 + (y2-y1)^2)
					//I calculate in the form: 'sqrt(((x2-x1)*(x2-x1))+((y2-y1)*(y2-y1)))' because the square function 'n^2' is slower than 'n*n'
					distList.add(new double[] {hotspots.get(x)[0],hotspots.get(y)[0],
								Math.sqrt(((hotspots.get(y)[1] - hotspots.get(x)[1]) *
								(hotspots.get(y)[1] - hotspots.get(x)[1])) +
								((hotspots.get(y)[2] - hotspots.get(x)[2]) *
								(hotspots.get(y)[2] - hotspots.get(x)[2])))});
				}
			}
		}
		
		Collections.sort(distList,new Comparator<double[]>() { //The Collections sort is being directed to compare on the last element which contains the distances
            public int compare(double[] lengths, double[] otherLengths) {
                return (int)((lengths[2] - otherLengths[2])*10000); //For precision before truncating with int, will break with big enough distances
            }														//Will also sort incorrectly for distances that only differ after 4 decimal places
        });
		
		List<List<Integer>> sets = new LinkedList<List<Integer>>();
		makeSet(hotspots, sets); //Populates sets with the contents of hotspots
		
		kruskal = new LinkedList<double[]>();
		for(int i = 0; i < distList.size(); i++){
			//I am assigning the result of findSet to integers so that I didn't have to call the function four times a loop
			int findSetOne = findSet((int)distList.get(i)[0], sets);
			int findSetTwo = findSet((int)distList.get(i)[1], sets);
			if(findSetOne != findSetTwo){
				union(findSetOne, findSetTwo, sets);
				kruskal.add(distList.get(i));
			}
		}
		
		while(true){
			System.out.println("How many emergency stations would you like?");
			System.out.println("(Enter a number between 1 and "+hotspots.size()+" to place the emergency stations.");
			System.out.println("Enter 0 to exit.)\n");
			try {
				input = Integer.parseInt(userInput.readLine());
			} catch (NumberFormatException | IOException e) {
				e.printStackTrace();
			}
			System.out.println("<"+input+">\n");
			while(input < 0 || input > hotspots.size()){
				System.out.println("Entry not valid.\n");
				System.out.println("How many emergency stations would you like?");
				//This part is imitating the specs, when a user has input incorrectly, it changes to "between 1 and n" as shown in the assignment doc
				System.out.println("(Enter a number between 1 and n to place the emergency stations.");
				System.out.println("Enter 0 to exit.)\n");
				try {
					input = Integer.parseInt(userInput.readLine());	
				} catch (NumberFormatException | IOException e) {
					e.printStackTrace();
				}
			}
			if(input != 0){
				//Quick rundown of what is done in this section:
				//#emergencyStations-1 of the largest edges are selected and considered 'dividers' of the clusters
				//if two vertices are not both in the dividers list (NAND), join the sets of those vertices
				//this results in the clusters for the emergency stations represented by sets
				//the inter-clustering distance is derived along the way, since it will be the smallest edge in the dividers
				List<double[]> dividers = new LinkedList<double[]>(); //The edges that separate the clusters, my idea was to treat the vertices in this list as 'walls'
				double smallest = Double.POSITIVE_INFINITY;
				for(int i = 0; i < input-1; i++){
					double largest = -1.0; //Any distance will be greater than this
					int largestIndex = -1; //Tracks which index swapped last
					for(int j = 0; j < kruskal.size(); j++){
						if(kruskal.get(j)[2] > largest && !dividers.contains(kruskal.get(j))){ //If a new largest is found and this edge is not already a divider
							largest = kruskal.get(j)[2];
							largestIndex = j;
						}
					}
					dividers.add(kruskal.get(largestIndex)); //Adds the new divider
					if(smallest > kruskal.get(largestIndex)[2]){ //Tracks the smallest divider for the InterCD
						smallest = kruskal.get(largestIndex)[2];
					}
				}
				sets = new LinkedList<List<Integer>>(); //Makes a new list
				makeSet(hotspots, sets); //Makes new sets of hotspots
				List<Integer> borders = new LinkedList<Integer>(); //Treats hotspots found in dividers as borders
				for(int i = 0; i < dividers.size(); i++){	//Divides the graph, populating the borders
					if(!borders.contains((int)dividers.get(i)[0])){ //If the divider is not in borders
						borders.add((int)dividers.get(i)[0]);
					}
					if(!borders.contains((int)dividers.get(i)[1])){ //If the divider is not in borders
						borders.add((int)dividers.get(i)[1]);
					}
				}
				for(int i = 0; i < kruskal.size(); i++){	//Forms the clusters
					if(!borders.contains((int)kruskal.get(i)[0]) || !borders.contains((int)kruskal.get(i)[1])){ //NAND for border vertices
						union(findSet((int)kruskal.get(i)[0], sets), findSet((int)kruskal.get(i)[1], sets), sets); //Union of two sets
					}
				}
				DecimalFormat df = new DecimalFormat("#.00"); //Formats the doubles in accordance to the assignment doc
				for(int i = 0; i < sets.size(); i++){ //This loop gets the average of the coordinates for each cluster and handles the output
					double xCoordTotal = 0;
					double yCoordTotal = 0;
					String output = "";
					for(int j = 0; j < hotspots.size(); j++){ //Adds up all of the coordinates for set 
						if(sets.get(i).contains((int)hotspots.get(j)[0])){ //Checks what hotspots are in the set
							output += (int)hotspots.get(j)[0] + ","; //builds the hotspots string
							xCoordTotal += hotspots.get(j)[1]; //Adds the x coordinate to the total
							yCoordTotal += hotspots.get(j)[2]; //Adds the y coordinate to the total
						}
					}
					xCoordTotal = xCoordTotal / sets.get(i).size(); //Calculates the average for the x coordinates
					yCoordTotal = yCoordTotal / sets.get(i).size(); //Calculates the average for the y coordinates
					System.out.println("Station "+(i+1)+":\nCoordinates: ("+df.format(xCoordTotal)+", "+df.format(yCoordTotal)+
							")\nHotspots: {"+output.substring(0, output.length()-1)+"}\n");
				}
				if(smallest != Double.POSITIVE_INFINITY){
					System.out.println("Inter-clustering distance: "+df.format(smallest)+"\n");
				}
				else{
					System.out.println("Inter-clustering distance: Not applicable.\n");
				}
			}
			else if(input == 0){
				System.out.println("<0>");
				System.out.println("Thank you for using Kruskal's Clustering. Bye.");
				System.exit(0);
			}
		}

	}
	
	//Makes a new set (list) for every element in hotspots
	private static void makeSet(List<double[]> hotspots, List<List<Integer>> sets){
		for(int i = 0; i < hotspots.size(); i++){
			List<Integer> tempSetElement = new LinkedList<Integer>();
			tempSetElement.add((int)hotspots.get(i)[0]);
			sets.add(tempSetElement);
		}
	}
	
	//Returns the index of the set containing the element provided, should always return a nonnegative value
	private static int findSet(int element, List<List<Integer>> sets){
		int errorVal = -1;
		for(int i = 0; i < sets.size(); i++){
			for(int j = 0; j < sets.get(i).size(); j++){
				if(element == sets.get(i).get(j)){
					return i;
				}
			}
		}
		return errorVal;
	}
	
	//Combines two sets, will always add the smaller set to the bigger set, if they are equal size
	//it will default to adding the second set to the first set, the smaller set is then removed
	private static void union(int setOne, int setTwo, List<List<Integer>> sets){
		List<Integer> tempSetOne = sets.get(setOne);
		List<Integer> tempSetTwo = sets.get(setTwo);
		if(tempSetOne.size() >= tempSetTwo.size()){
			for(int i = 0; i < tempSetTwo.size(); i++){
				tempSetOne.add(tempSetTwo.get(i));
			}
			sets.remove(tempSetTwo);
		}
		else if(tempSetOne.size() < tempSetTwo.size()){
			for(int i = 0; i < tempSetOne.size(); i++){
				tempSetTwo.add(tempSetOne.get(i));
			}
			sets.remove(tempSetOne);
		}
	}
}
