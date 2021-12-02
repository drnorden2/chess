package perft.chess.core.datastruct;

public class IndexedListInt {
		
		//*** State under BaseLine *************
		private int counter; 
		private int[] elements; 
		private int[] position; 
		
		public IndexedListInt (int amount, int indexRange) {
			counter=0;
			this.elements = new int [amount];
			position = new int[indexRange];
			for(int i=0;i<position.length;i++) {
				position[i]=-1;
			}
		}
		

		public int size(){
			return counter;
		}
		
		public boolean contains(int element){
			return position[element]!=-1;
		}
		
		public int getElement(int i){
			return elements[i];
		}
		
		public int getByElementIndex(int elementIndex){
			return elements[position[elementIndex]];
		}
		
		public boolean add(int element){
			int elementIndex = element;
			if(position[elementIndex]!=-1) {
				return false;
			}else{
				int curCounter = counter;
				elements[curCounter]= element;
				position[elementIndex] =curCounter;
				counter=++curCounter;			
			}
			return true;
		}
		

		public void removeAll() {
			int elementCount = this.size();
			for(int i =0;i<elementCount;i++) {
				position[elements[i]]=-1;
			}
			counter=0;
		}
		
		
		public void removeAllX() {
			int elementCount = this.size();
			for(int i =0;i<elementCount;i++) {
				removeFirstElement();
			}
		}
		public int removeFirstElement() {
			int element = elements[0];
			this.remove(element);
			return element;
		}
			
		public boolean remove(int element) {
			int elementIndex= element;
			int toDeletePosition = position[elementIndex];
			if(toDeletePosition  ==-1) {
				return false;
			}
			
			
			int curCounter =counter;
			if(curCounter == 0) {
				//System.out.println("WARNING: remove from empty List:"+elementIndex);
				return false;
			}else {
				counter=--curCounter;
				int filler = elements[curCounter]; 
			
				//elements.set(curCounter,null); /Stale!
				
				if(filler==-1) {
					System.out.println("WTF! No filler at:"+curCounter);//@todo remove me
				}
				int fillerElementIndex = filler;
				
				if(curCounter>0) {// otherwise last removed
					elements[toDeletePosition]=filler;
					position[fillerElementIndex]=toDeletePosition;
				}
				position[elementIndex]=-1;
			
			}
			return true;
			//checkConsistency("removeEnd");
		}
		
		public static void main(String[] args) {
			int ADD =0;
			int REMOVE =1;
			
			IndexedList <IndexedInt>cList = new <IndexedInt>IndexedList (IndexedInt.class, 16*28, 16*28*64);
//			test(cList,new IndexedInt(1),REMOVE);
			test(cList,new IndexedInt(1),ADD);
			test(cList,new IndexedInt(1),ADD);
			test(cList,new IndexedInt(1),REMOVE);
			test(cList,new IndexedInt(1),ADD);
			test(cList,new IndexedInt(2),ADD);
			test(cList,new IndexedInt(1),REMOVE);
			test(cList,new IndexedInt(1),REMOVE);
			test(cList,new IndexedInt(1),ADD);
		}	
		
		public static void test(IndexedList cList,IndexedInt indexedPiece,int mode) {
			int ADD =0;
			int REMOVE =1;
			if(mode == ADD) {
				System.out.println("adding "+indexedPiece);
				cList.add(indexedPiece);
			}else if(mode == REMOVE) {
				System.out.println("removing "+indexedPiece);
				cList.remove(indexedPiece);
			}
			System.out.println(cList);
		}
		
		
		public String toString() {
			String result =">";
			for(int i =0;i<counter;i++) {
				result += ( elements[i] +", ");
			}
			result +="\n"+position.toString();
			result +="\n"+elements.toString();
			return result;
		}

		static class IndexedInt implements IndexedElement{
			private int myInt;
			public IndexedInt (int myInt) {
				this.myInt = myInt;
			}
			public int getElementIndex() {
				return myInt;
			}
			public String toString () {
				return ""+myInt;
			}
		}
		
		private int testCounter=0;
		public void checkConsistency(String label) {
			if(label.equals("AddStart") && this.elements.length > 1000) {
				testCounter++;
			}
			for(int i=0;i<counter;i++) {
				int filler = elements[i];
				if(filler==-1) {
					System.out.println(label);
					System.out.println("WTF! CONSISTENCY after "+testCounter+ "- No filler at:"+i);//@todo remove me
					System.out.println("Counter:"+counter +" El:"+elements.toString());
					System.exit(0);
				}
			}
			System.out.println("Counter:"+counter +" El:"+elements.toString());
		}

	}



