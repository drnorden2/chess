package perft.chess.core.baseliner;
import perft.chess.core.datastruct.BitBoard;
import perft.chess.core.datastruct.IndexedElement;

public class BLIndexedListBB <T extends IndexedElement>{
	private final BitBoard bitBoard;	
	private final BLVariable<Long> bits;
	private final T[] allElements;
	private final int pieceType;

	public BLIndexedListBB (BaseLiner bl, T[] allElements,int depth,int pieceType) {
		this.bitBoard= new BitBoard(0L);
		this.bits = new BLVariable<Long>(bl,0L);
		this.allElements = allElements;
		this.pieceType = pieceType;
	}
	
	public void load() {
		bitBoard.reset(bits.get());
	}
	public void store() {
		bits.set(bitBoard.getBits());
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
	
	public void toggle(int elementIndex){
		this.bitBoard.toggle(elementIndex);
	}
	
	
	public void removeAll() {
		bitBoard.reset();
	}
	
	public void remove(T element) {
		bitBoard.unset(element.getElementIndex());
	}	
	
	// will be triggered finally by the legalMove loop
	public int size(){
		return bitBoard.popCount();
	}
	
	
	public int selectList(Object[] selection) {
		return bitBoard.selectList(selection,allElements);
	}	
}
