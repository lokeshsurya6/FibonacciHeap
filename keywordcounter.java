import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Timestamp;
import java.util.Date;

public class keywordcounter {
	public static void main(String[] args) {
		String inputFilePath = args[0];
		if(inputFilePath == null) {
			return;
		}
		
		Timestamp start = new Timestamp((new Date()).getTime());
		
		FibonacciHeap fHeap = new FibonacciHeap();
		StringBuilder resultBuilder = new StringBuilder();		
		BufferedReader reader = null;
		try {
			// Read the contents of the input file line by line
			reader = new BufferedReader(new FileReader(inputFilePath));
			String line = reader.readLine();

			while(line != null) {
				String[] lineArray = line.split(" ");
				
				// If line contains only one word
				if(lineArray.length == 1) {
					String s = lineArray[0];

					// Stop the program when the keyword 'stop' is read
					if(s.equals("stop")) {
						break;
					} else {	// Query the top x keywords from the heap
						int topX = Integer.parseInt(s);
						Node[] nodeList = fHeap.getTopX(topX);
						
						// Maintain the top x keywords in a StringBuilder which finally writes the output to the file
						StringBuilder tempResult = new StringBuilder();
						for(Node node : nodeList) {
							if(node != null) {
								tempResult.append(node.keyword);
								tempResult.append(",");
							}
						}
						if(!tempResult.toString().isEmpty()) {
							tempResult = tempResult.deleteCharAt(tempResult.length() - 1);
							tempResult.append(System.lineSeparator());
						}
						
						resultBuilder.append(tempResult.toString());
					}

				} else if(lineArray.length == 2) {	// If line has 2 words, parse them and input them into the heap

					String name = lineArray[0].substring(1, lineArray[0].length());
					int value = Integer.parseInt(lineArray[1]);
					fHeap.addToHeap(value, name);
				}

				line = reader.readLine();
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if(reader != null)
				try {reader.close();} catch (Exception ex) {ex.printStackTrace();}
		}
		    
		// Create a new file named 'output_file.txt' and write the StringBuilder string to it
		Writer writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream("output_file.txt"), "utf-8"));
			writer.write(resultBuilder.toString()); 
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if(writer != null)
				try {writer.close();} catch (Exception ex) {ex.printStackTrace();}
		}
		
		Timestamp end = new Timestamp((new Date()).getTime());
		long timeTaken = end.getTime() -start.getTime();
		System.out.println("The result is written into output_file.txt in " + timeTaken + " milliseconds.");
		    		
	}	
}
