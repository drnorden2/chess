package perft.chess.mailbox;

import perft.chess.core.baseliner.BLVariable;
import perft.chess.core.baseliner.BLVariableInt;
import perft.chess.core.baseliner.BaseLiner;
import perft.chess.core.datastruct.IndexedElement;
import perft.chess.core.o.O;
import static perft.chess.Definitions.*;

public class Piece implements IndexedElement{

	private final int elementIndex;
	private final BaseLiner bl;
	private final int color;
	private boolean isTouchedFromBeginning;
	private static final int INDEX_Counter_ID =1;
	
	/**under base line**/
	 final BLVariableInt pos;
	private final BLVariableInt moveIndex;
	private final BLVariableInt type;
	private final MBPosition position;
	
	//private IndexedList<Move>allMoves ;
	
	public Piece(BaseLiner bl, MBPosition position,int type, int color) {
		this(bl, position, type, color, false);
	}

	public Piece(BaseLiner bl, MBPosition position, int type, int color, boolean touched) {
		elementIndex = bl.incrementIndexCounter(Piece.INDEX_Counter_ID);
		this.bl = bl;
		this.color = color;
		this.pos = new BLVariableInt(bl);
		this.moveIndex= new BLVariableInt(bl);		
		this.type = new BLVariableInt(bl,type);
		this.position= position;
		if(type == PIECE_TYPE_KING) {
			this.position.setKing(color,this);
		}
		this.isTouchedFromBeginning = touched;
	}
	public void isUntouched() {
		this.isTouchedFromBeginning = false;
	}
	
	private void totalMoves() {
		// TODO Auto-generated method stub

	}
	private void validMoves() {
		// TODO Auto-generated method stub

	}
	public int getElementIndex() {
		return this.elementIndex;
	}
	
	public int getColor() {
		return this.color;
	}
	public int getType() {
		return this.type.get();
	}
	public void setType(int type) {
		this.type.set(type);
	}
	
	
	public int getPosition() {
		return this.pos.get();
		
	}
	public void setPosition(int pos) {
		this.pos.set(pos);
		//keep moveIndex for further unstaging
		if(pos!=-1) {
			this.moveIndex.set(pos+(type.get() * 2 + color) * 64);
		}
	}
	
	public int getMoveIndex() {
		return this.moveIndex.get();
	}
	public void addMove(Move move) {
	//	this.allMoves.add(move);
	}
	
	// smart logic to minimize additional state data
	public boolean isTouched() {
		if(isTouchedFromBeginning) {
			return true;
		}else {
			int i = pos.getChanges();//DELETE ME! @TODO
			return (pos.getChanges() !=1); 
		}
		
	}
	public final String toString() {
		return getPieceName()+" Type:"+type+" color:"+color+" pos:"+pos.get()+" moveIndex:"+moveIndex.get();
	}
	
	private final String getPieceName() {
		String name ="";
		switch (type.get()) {
		case PIECE_TYPE_ROOK:
			name ="ROOK";
			break;
		case PIECE_TYPE_KNIGHT:
			name ="KNIGHT";
			break;
		case PIECE_TYPE_BISHOP:
			name ="BISHOP";
			break;
		case PIECE_TYPE_QUEEN:
			name ="QUEEN";
			break;
		case PIECE_TYPE_KING:
			name ="KING";
			break;
		case PIECE_TYPE_PAWN:
			name ="PAWN";
			break;
			
		}
		return name;
	}
}
