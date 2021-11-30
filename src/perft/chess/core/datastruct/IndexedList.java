package perft.chess.core.datastruct;

public class IndexedList <T extends IndexedElement>{

	
	//*** State under BaseLine *************
	private int counter; 
	private T[] elements; 
	private int[] position; 
	
	public IndexedList ( T[]elements, int indexRange) {
		counter=0;
		this.elements = elements;
		position = new int[indexRange];

		for(int i=0;i<position.length;i++) {
			position[i]=-1;
		}
	}
	
	public int size(){
		return counter;
	}
	
	public boolean contains(T element){
		return position[element.getElementIndex()]!=-1;
	}
	public boolean contains(int elementIndex){
		return position[elementIndex]!=-1;
	}
	
	public T getElement(int index){
		return (T)elements[index];
	}
	
	public T getByElementIndex(int elementIndex){
		return (T)elements[position[elementIndex]];
	}
	
	public boolean add(T element){
		int elementIndex = element.getElementIndex();
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
			position[elements[i].getElementIndex()]=-1;
		}
		counter=0;
	}
	
	
	public T removeFirstElement() {
		T element = elements[0];
		this.remove(element);
		return element;
	}
		
	public boolean remove(T element) {
		int elementIndex= element.getElementIndex();
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
			T filler = elements[curCounter]; 
		
			//elements.set(curCounter,null); /Stale!
			
			if(filler==null) {
				System.out.println("WTF! No filler at:"+curCounter);//@todo remove me
			}
			int fillerElementIndex = filler.getElementIndex();
			
			if(curCounter>0) {// otherwise last removed
				elements[toDeletePosition]=filler;
				position[fillerElementIndex]=toDeletePosition;
			}
			position[elementIndex]=-1;
		
		}
		return true;
		//checkConsistency("removeEnd");
	}
	
	public static void mainX(String[] args) {
		int ADD =0;
		int REMOVE =1;
		
		IndexedList <IndexedInt>cList = new <IndexedInt>IndexedList (new IndexedInt [16*28], 16*28*64);
//		test(cList,new IndexedInt(1),REMOVE);
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
			result += ( elements[i].getElementIndex() +", ");
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
			T filler = elements[i];
			if(filler==null) {
				System.out.println(label);
				System.out.println("WTF! CONSISTENCY after "+testCounter+ "- No filler at:"+i);//@todo remove me
				System.out.println("Counter:"+counter +" El:"+elements.toString());
				System.exit(0);
			}
		}
		System.out.println("Counter:"+counter +" El:"+elements.toString());
	}

}



