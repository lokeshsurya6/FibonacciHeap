import java.util.Hashtable;

// Node Structure
class Node {
	int value;
	String keyword;

	Node nextSibling;
	Node prevSibling;
	
	Node child;
	Node parent;
	int degree;
	boolean childCut = false;
	
	Node(int val, String keyword) { 
		this.value = val;
		this.keyword = keyword;
		this.nextSibling = null;
		this.prevSibling = null;
		this.child = null;
		this.parent = null;
	}
}

public class FibonacciHeap {
	Node heapMax;	// Points to the maximum node of the heap
	Hashtable<String, Node> hashTable = new Hashtable<>();
	int numberOfNodes = 0;	// Number of nodes in the heap
	
	/* Checks the hashTable if a node with the keyname is already present. 
	 * If yes, increases its value. Else, adds a new node to the heap
	 */
	public void addToHeap(int val, String keyname) {
		Node matchedNode = hashTable.get(keyname);
		if(matchedNode == null) {
			Node newNode = new Node(val, keyname);
			add(newNode);
			numberOfNodes++;
			return;
		}
		
		increaseValue(val, keyname, matchedNode);
	}
	
	//Adds new element to the top list [after heapMax]
	private void add(Node newNode) { 		
		if(heapMax == null) {
			heapMax = newNode;
			heapMax.nextSibling = heapMax;
			heapMax.prevSibling = heapMax;
			heapMax.child = null;
			heapMax.parent = null;
			
			hashTable.put(newNode.keyword, newNode);
			return;
		}
		
		//Add new node after the heapMax 
		Node next = heapMax.nextSibling;
		heapMax.nextSibling = newNode;
		newNode.prevSibling = heapMax;
		newNode.nextSibling = next;
		next.prevSibling = newNode;
		newNode.parent = null;
		
		//Update max pointer of the top list if necessary
		if(heapMax.value < newNode.value) {
			heapMax = newNode;
		}
		
		// Update hashtable with new record
		hashTable.put(newNode.keyword, newNode);
	}

	/*
	// Unused function which can be used for increasing value of a node without performing cascading cuts
	private void increaseValue_withoutCascadingCut(int val, String keyname, Node matchedNode) {
		matchedNode.value += val;		
		Node parentNode = matchedNode.parent;
		if(parentNode != null && parentNode.value < matchedNode.value) {
			if(parentNode.degree == 1) {
				parentNode.child = null;
			} else {
				if(parentNode.child == matchedNode) {
					parentNode.child = matchedNode.nextSibling;
				}
				
				Node temp = matchedNode.prevSibling;
				temp.nextSibling = matchedNode.nextSibling;
				matchedNode.nextSibling.prevSibling = temp;
				matchedNode.prevSibling = matchedNode;
				matchedNode.nextSibling = matchedNode;
			}
			parentNode.degree = parentNode.degree - 1;
			matchedNode.parent = null;
			meldToTopList(matchedNode);
		}
		// Update max if needed
		if(heapMax.value < matchedNode.value) {
			heapMax = matchedNode;
		}
	}
	*/
	
	/* Increases the value of a node by 'val'
	 * Cuts the node from its parent when the new value is greater than that of its parent
	 * Performs cascading cut on its parent
	 */
	private void increaseValue(int val, String keyname, Node matchedNode) {
		matchedNode.value += val;		
		
		Node parentNode = matchedNode.parent;
		if(parentNode != null && parentNode.value < matchedNode.value) {
			makeCut(matchedNode, parentNode);
			makeCascadingCut(parentNode);
		}
		
		// Update max if needed
		if(heapMax.value < matchedNode.value) {
			heapMax = matchedNode;
		}
	}

	/* Cuts the 'matchedNode' from its parent which is 'parentNode'
	 * Then melds the 'matchedNode' into the top level circular list of the heap
	 */
	private void makeCut(Node matchedNode, Node parentNode) {
		// When parentNode has only one child
		if(parentNode.degree == 1) {
			parentNode.child = null;
			matchedNode.prevSibling = matchedNode;
			matchedNode.nextSibling = matchedNode;
		} else {	// When parentNode has multiple children. Maintains the circular list of the parent's children
			if(parentNode.child == matchedNode) {
				parentNode.child = matchedNode.nextSibling;
			}
			
			Node temp = matchedNode.prevSibling;
			temp.nextSibling = matchedNode.nextSibling;
			matchedNode.nextSibling.prevSibling = temp;
			matchedNode.prevSibling = matchedNode;
			matchedNode.nextSibling = matchedNode;
		}
		
		parentNode.degree = parentNode.degree - 1;
		matchedNode.parent = null;
		// Meld the node which is cut into the top level circular list of the heap
		meldToTopList(matchedNode);		
		matchedNode.childCut = false;
	}
	
	// Recursively performs cascading cuts at the node until the parent's childCut is found to be false
	private void makeCascadingCut(Node node) {
		Node parent = node.parent;
		if(parent != null) {
			if(parent.childCut == false) {
				parent.childCut = true;
			} else {
				makeCut(node, parent);
				makeCascadingCut(parent);
			}
		}
	}
	
	/* Removes the maximum node of the heap
	 * Melds the children of the max node (if any) to the top level circular list of heap
	 * Calls 'pairwiseCombine()' to combine nodes having same degrees in the top list until 
	 * 	the top list contains of only nodes having different degrees
	 */
	public Node removeMax() {
		Node max = null;
		// If heap has no elements
		if(heapMax == null) {
			return max;
		}
		
		max = new Node(heapMax.value, heapMax.keyword);
		// If heapMax has no children
		if(heapMax.child == null) {
			// If the heap has only one element [top level list has only one node]
			if(heapMax.nextSibling == heapMax) {
				heapMax = null;
			} else {	
				// If top level list has 1+ nodes. Make the heapMax as the node next to the previous max node temporarily
				Node temp = heapMax.prevSibling;
				temp.nextSibling = heapMax.nextSibling;
				heapMax.nextSibling.prevSibling = temp;
				heapMax = temp;
				pairwiseCombine();				
			}
		} else {	// If heapMAx has 1 or more children
			Node childOfMax = heapMax.child;
			// When top level list of heap has only 1 node
			if(heapMax.nextSibling == heapMax) {
				heapMax = null;
			} else {	// When top level list has 2 or more nodes. Make the node next to the heapMax as max temporarily
				Node temp = heapMax.prevSibling;
				temp.nextSibling = heapMax.nextSibling;
				heapMax.nextSibling.prevSibling = temp;
				heapMax = temp;				
			}
			// Meld the children of heapMax to the top level list of the heap
			meldToTopList(childOfMax);
			pairwiseCombine();
		}
		
		numberOfNodes--;
		hashTable.remove(max.keyword);
		return max;
	}

	/* Pairwise combines the nodes in top level circular list which have same degree
	 * Ends when there are unique degree nodes in the top level list
	 * Finally, re-creates the heap by updating pointers and setting the heapMax to maximum node
	 */
	private void pairwiseCombine() {
		/* Size of log(numberOfNodes in the heap) to base 1.6 is taken for the degree table as this serves 
		* as an upper bound to the maximum degree of a given number of nodes in the heap according to 
		* CLRS and many other textbooks
		*/
		int size = (int) (Math.log(numberOfNodes) / Math.log(1.6));
		Node[] degreeList = new Node[size];
		for(int i=0; i<size; i++) {
			degreeList[i] = null;
		}
		
		// Count the number of nodes in the top level circular list
		Node t = heapMax;
		int numOfNodesInTopList = 1;
		while(t.nextSibling != heapMax) {
			t = t.nextSibling;
			numOfNodesInTopList++;
		}
		
		// Iterate for all the nodes in the top level circular list of the heap
		Node rootNode = heapMax;
		for(int y=0; y<numOfNodesInTopList; y++) {
			Node temp = rootNode;
			Node placeHolder = temp.nextSibling;
			int degree = temp.degree;
			
			// Run until there is no entry in the degree table that has the same degree as that of the current node in iteration
			while(degreeList[degree] != null) {
				Node a = degreeList[degree];
				// Determine which node needs to be made a child and which one the parent according to node value
				if(a.value < temp.value) {
				} else {
					// swap a and temp
					Node x = temp;
					temp = a;
					a = x;
				}
				// Combine nodes that have same degrees by making one node a child to another
				makeChild(temp,a);
				degreeList[degree] = null;
				
				degree++;
			}
			// Set the node with degree 'x' into the degreeTable at index 'x'
			degreeList[degree] = temp;			
			rootNode = placeHolder;
		}
		
		// Merge the contents of the degree table and update the heapMax whenever required
		heapMax = null;
		for(int i =0; i<size; i++) {
			Node node = degreeList[i];
			if(node != null) {
				// If heapMax is null, point it to the node
				if(heapMax == null) {
					heapMax = node;
					heapMax.nextSibling = heapMax;
					heapMax.prevSibling = heapMax;
				} else {	// If heapMax is not null, simply add this node to the top level circular list
					Node heapNext = heapMax.nextSibling;
					heapMax.nextSibling = node;
					node.prevSibling = heapMax;
					node.nextSibling = heapNext;
					heapNext.prevSibling = node;
					// Update the heapMax pointer whenever needed
					if(node.value > heapMax.value) {
						heapMax = node;
					}
				}
			}
		}
	}

	// Makes the second node a child to the first node
	private void makeChild(Node parentNode, Node childToBe) {		
		// Remove childToBe from the top level circular list of heap
		Node t = childToBe.prevSibling;
		t.nextSibling = childToBe.nextSibling;
		childToBe.nextSibling.prevSibling = t;
		childToBe.nextSibling = null;
		childToBe.prevSibling = null;
		
		// Make it child of parentNode
		if(parentNode.child == null) {	// If parent has no children
			parentNode.child = childToBe;
			childToBe.nextSibling = childToBe;
			childToBe.prevSibling = childToBe;
		} else {	// If parent has children, add the childToBe into the circular list of the child
			Node c = parentNode.child;
			Node temp = c.nextSibling;
			c.nextSibling = childToBe;
			childToBe.prevSibling = c;
			temp.prevSibling = childToBe;
			childToBe.nextSibling = temp;
		}
		
		childToBe.parent = parentNode;
		parentNode.degree = parentNode.degree + 1;
		childToBe.childCut = false;
	}

	// Melds the argument into the top level circular list of the heap
	private void meldToTopList(Node nodeToMeld) {
		nodeToMeld.parent = null;
		
		// When heapMax is null, point the heapMax to the input argument
		if(heapMax == null) {
			heapMax = nodeToMeld;
			
			Node next = heapMax;
			// If nodeToMeld is a circular list, make all of its siblings' parents as null and childCut 
			// 		as false as per the rules for the top level circular list
			while(next.nextSibling != heapMax) {
				next = next.nextSibling;
				next.parent = null;
				next.childCut = false;
			}
			return;
		}

		// Make 'last' point to the last element of the circular list that 'nodeToMeld' belongs
		Node last = nodeToMeld;
		while(last.nextSibling != nodeToMeld) {
			last = last.nextSibling;
			last.parent = null;
			last.childCut = false;
		}
		
		//Meld the input circular list next to the current heapMax and update pointers accordingly
		Node heapNext = heapMax.nextSibling;
		heapMax.nextSibling = nodeToMeld;		
		nodeToMeld.prevSibling = heapMax;
		last.nextSibling = heapNext;
		heapNext.prevSibling = last;
	}
	
	// Extract the top x nodes of the heap, create a new list to be returned. Re-insert the extracted nodes back into the heap
	public Node[] getTopX(int x) {
		Node[] resultNodes = new Node[x];
		int index = 0;
		for(int i=0; i<x; i++) {
			Node n = removeMax();
				resultNodes[i] = n;
		}

		// Re-insert the removed nodes back into the heap
		for(Node node : resultNodes) {
			addToHeap(node.value, node.keyword);
		}
		return resultNodes;
	}
}
