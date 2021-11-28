package perft.chess.core.baseliner;

import perft.chess.core.datastruct.IndexedElement;

public class BLIndexedList <T extends IndexedElement>{

	private final BaseLiner bl;
	
	//*** State under BaseLine *************
	private BLVariableInt counter; 
	private BLArray<T> elements; 
	private BLArrayInt position; 
	
	public BLIndexedList (BaseLiner bl, int maxAmount, int indexRange) {
		this.bl = bl;
		counter=new BLVariableInt(this.bl,0);
		elements = new BLArray<T>(bl, maxAmount+1);
		position = new BLArrayInt(bl, indexRange,-1);
	}
	

	public int size(){
		return counter.get();
	}
	
	public boolean contains(T element){
		return position.get(element.getElementIndex())!=-1;
	}
	public boolean contains(int elementIndex){
		return position.get(elementIndex)!=-1;
	}
	
	public T getElement(int index){
		return (T)elements.get(index);
	}
	
	public T getByElementIndex(int elementIndex){
		return (T)elements.get(position.get(elementIndex));
	}
	
	public boolean add(T element){
		//if(debug)System.out.println("add("+element.getElementIndex()+")");
		//checkConsistency("AddStart");
		int elementIndex = element.getElementIndex();
		int curCounter = counter.get();
		if(position.get(elementIndex )!=-1) {
			return false;
			//System.out.println("WARNING: already added:"+elementIndex );return;
			//throw new RuntimeException("WARNING: already added:"+indexedPiece);
		}else{
			elements.set(curCounter,element);
			position.set(elementIndex ,curCounter);
			counter.set(++curCounter);			
		}
		//checkConsistency("Add End");
		return true;
	}
	

	public void removeAll() {
		int elementCount = this.size();
		for(int i =0;i<elementCount;i++) {
			position.set(elements.get(i).getElementIndex(),-1);
		}
		counter.set(0);
	}
	
	
	public void removeAllX() {
		int elementCount = this.size();
		for(int i =0;i<elementCount;i++) {
			removeFirstElement();
		}
	}
	public T removeFirstElement() {
		T element = elements.get(0);
		if(element==null) {
			System.out.println("WTF-remove of first Element failed@");
		}
		this.remove(element);
		return element;
	}
		
	public boolean remove(T element) {
		//if(debug)System.out.println("remove("+element.getElementIndex()+")");
		//checkConsistency("removeStart");
		int elementIndex= element.getElementIndex();
		int toDeletePosition = position.get(elementIndex);
		if(toDeletePosition  ==-1) {
			return false;
		}
		
		
		int curCounter =counter.get();
		if(curCounter == 0) {
			//System.out.println("WARNING: remove from empty List:"+elementIndex);
			return false;
		}else {
			counter.set(--curCounter);//--
			T filler = elements.get(curCounter); 
		
			//elements.set(curCounter,null); /Stale!
			
			if(filler==null) {
				System.out.println("WTF! No filler at:"+curCounter);//@todo remove me
			}
			int fillerElementIndex = filler.getElementIndex();
			
			if(curCounter>0) {// otherwise last removed
				elements.set(toDeletePosition,filler);
				position.set(fillerElementIndex,toDeletePosition);
			}
			position.set(elementIndex,-1);
		
		}
		return true;
		//checkConsistency("removeEnd");
	}
	
	
	public static void mainX(String[] args) {
		BaseLiner bl = new BaseLiner(1000,10,1000,10);
		BLIndexedList <IndexedInt>il = new <IndexedInt>BLIndexedList (bl, 3, 10);
			System.out.println("After intit: "+ il);
		bl.startNextLevel();
			System.out.println("Baseline 1: "+ il);
		il.add(new IndexedInt(4));
			System.out.println("After adding 1: "+ il);
		bl.startNextLevel();
			System.out.println("Baseline 2: "+ il);
		il.add(new IndexedInt(5));
			System.out.println("After adding 2: "+ il);
		bl.undo();
			System.out.println("back to Baseline 1: "+ il);
		il.remove(new IndexedInt(3));
			System.out.println("After removing 1: "+ il);
	}
	
	
	public static void mainOld(String[] args) {
		int ADD =0;
		int REMOVE =1;
		BaseLiner bl = new BaseLiner(100,10,1000,100);
		
		BLIndexedList <IndexedInt>cList = new <IndexedInt>BLIndexedList (bl, 16*28, 16*28*64);
		test(cList,new IndexedInt(1),REMOVE);
		test(cList,new IndexedInt(1),ADD);
		test(cList,new IndexedInt(1),ADD);
		test(cList,new IndexedInt(1),REMOVE);
		test(cList,new IndexedInt(1),ADD);
		test(cList,new IndexedInt(2),ADD);
		test(cList,new IndexedInt(1),REMOVE);
		test(cList,new IndexedInt(1),REMOVE);
		test(cList,new IndexedInt(1),ADD);
	}	
	
	public static void test(BLIndexedList cList,IndexedInt indexedPiece,int mode) {
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
		for(int i =0;i<counter.get();i++) {
			result += ( elements.get(i).getElementIndex() +", ");
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
		if(label.equals("AddStart")&& this.elements.length()>1000) {
			testCounter++;
			
		}
		for(int i=0;i<counter.get();i++) {
			T filler = elements.get(i);
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



