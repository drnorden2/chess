package perft.chess.core.baseliner;
import perft.chess.core.datastruct.BitBoard;
import perft.chess.core.datastruct.IndexedElement;

public class BLIndexedListBB <T extends IndexedElement>{
	private final BitBoard bitBoard;	
	private final T[] allElements;
	
	public BLIndexedListBB (BaseLiner bl, T[] allElements,int depth) {
		this.bitBoard= new BitBoard(0L);
		this.allElements = allElements;
	}
	
	// will be triggered finally by the legalMove loop
	public int size(){
		return bitBoard.popCount();
	}
	
	public boolean contains(T element){
		return bitBoard.get(element.getElementIndex());
	}
	
	public boolean contains(int elementIndex){
		return bitBoard.get(elementIndex);
	}
	
	
	public T getByElementIndex(int elementIndex){
		return this.bitBoard.get(elementIndex)?allElements[elementIndex]:null;
	}
	
	public void add(T element){
		this.bitBoard.set(element.getElementIndex());
	}
	
	public void removeAll() {
		bitBoard.reset();
	}
	
	public void remove(T element) {
		bitBoard.unset(element.getElementIndex());
	}	
	
	public int selectList(Object[] selection) {
		return bitBoard.selectList(selection,allElements);
	}	
}
