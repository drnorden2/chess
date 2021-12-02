package perft.chess.core.datastruct;

import java.lang.reflect.Array;

public class IndexedList <T extends IndexedElement>{

	
	//*** State under BaseLine *************
	private int counter; 
	private T[] elements; 
	private int[] position; 
	
	public IndexedList (Class<T> clazz, int size , int indexRange) {
		elements = (T[]) Array.newInstance(clazz, size());
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
	
}



