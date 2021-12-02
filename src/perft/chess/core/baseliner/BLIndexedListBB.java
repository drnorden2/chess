package perft.chess.core.baseliner;
import perft.chess.core.datastruct.BitBoard;
import perft.chess.core.datastruct.IndexedElement;

public class BLIndexedListBB <T extends IndexedElement>{
	private final BitBoard curBB;
	private final BLVariable<Long> bits;
	
	private final int[] indices = new int[65];
	private final T[] allElements;
	private final BaseLiner bl;
	
	public BLIndexedListBB (BaseLiner bl, T[] allElements,int deep) {
		this.bits = new BLVariable<Long>(bl);
		curBB = new BitBoard(0L);
		this.allElements = allElements;
		this.bl = bl;
	}
	
	// will be triggered finally by the legalMove loop
	public int size(){
		Long curBits = curBB.getBits();
		if(!curBits.equals(this.bits.get())){
			this.bits.set(curBits);
			curBB.updateIndices(indices);
		}
		return curBB.popCount();
	}
	//has to be triggered prior to the addRemovePseudoMove
	public void reload() {
		curBB.reset(bits.get());//if new level reload same // if next move=>reset.
	}
	
	public boolean contains(T element){
		return curBB.get(element.getElementIndex());
	}
	public boolean contains(int elementIndex){
		return curBB.get(elementIndex);
	}
	
	public T getElement(int index){
		return allElements[indices[indices[index]]];
	}
	
	public T getByElementIndex(int elementIndex){
		return this.curBB.get(elementIndex)?allElements[elementIndex]:null;
	}
	
	public void add(T element){
		this.curBB.set(element.getElementIndex());
	}
	

	public void removeAll() {
		curBB.reset();
		this.indices[0]=0;
	}
		
	public void remove(T element) {
		curBB.unset(element.getElementIndex());
	}	
}
