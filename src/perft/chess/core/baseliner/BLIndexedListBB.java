package perft.chess.core.baseliner;
import perft.chess.core.datastruct.BitBoard;
import perft.chess.core.datastruct.IndexedElement;

public class BLIndexedListBB <T extends IndexedElement>{
	private final BitBoard[] bitBoards;	
	private final int[] indices = new int[65];
	private final T[] allElements;
	private final BaseLiner bl;
	private int level;
	
	
	public BLIndexedListBB (BaseLiner bl, T[] allElements,int depth) {
		this.bitBoards= new BitBoard[depth];
		for(int i=0;i<bitBoards.length;i++) {
			bitBoards[i]= new BitBoard(0L);
		}
		level = bl.level;
		this.allElements = allElements;
		this.bl = bl;
	}
	
	// will be triggered finally by the legalMove loop
	public int size(){
		bitBoards[level].updateIndices(indices);
		return bitBoards[level].popCount();
	}
	
	//has to be triggered prior to the addRemovePseudoMove
	public void reload() {
		if(indices[0]!=-1) {
			if(level>bl.level) {
				//sibling
				level = bl.level;	
				this.bitBoards[level]=this.bitBoards[level-1];
				bitBoards[level].updateIndices(indices);
			}else {
				//one deeper
				indices[0]=-1;
				level = bl.level;	
				this.bitBoards[level]=this.bitBoards[level-1];
			}
		}
	}
	
	public boolean contains(T element){
		return bitBoards[level].get(element.getElementIndex());
	}
	
	public boolean contains(int elementIndex){
		return bitBoards[level].get(elementIndex);
	}
	
	public T getElement(int index){
		reload();
		bitBoards[level].updateIndices(indices);
		return allElements[indices[index+1]];
	}
	
	public T getByElementIndex(int elementIndex){
		return this.bitBoards[level].get(elementIndex)?allElements[elementIndex]:null;
	}
	
	public void add(T element){
		this.bitBoards[level].set(element.getElementIndex());
	}
	
	public void removeAll() {
		bitBoards[level].reset();
		this.indices[0]=-1;
	}
		
	public void remove(T element) {
		bitBoards[level].unset(element.getElementIndex());
	}	
}
