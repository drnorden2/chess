package perft.chess.perftbb;

import static perft.chess.Definitions.*;


import perft.chess.Position;
import perft.chess.core.baseliner.BLIndexedList;
import perft.chess.core.baseliner.BLVariableInt;
import perft.chess.core.baseliner.BLVariableLong;
import perft.chess.core.baseliner.BaseLiner;
import perft.chess.perftbb.gen.MagicNumberFinder;



public class BBPosition implements Position {
	public final int depth = 10;
	public final BaseLiner bl = new BaseLiner(7928, 20000, 1000, depth, 1000);
	MagicNumberFinder mnf = new MagicNumberFinder();
	BLIndexedList<Move>[] allMovesLists=new BLIndexedList[2];
	
	
	public final BLVariableInt[] fields = new BLVariableInt[64];
	public final BLVariableLong[] allOfOneColor = new BLVariableLong[2];//@todo WTF -stack
	public final BLVariableLong[][] callBacks = new BLVariableLong[64][4];
	public final BLVariableLong[] moveMasks = new BLVariableLong[64];
	
	public final BLVariableLong[] tCallBacks = new BLVariableLong[64];
	public final BLVariableLong[] correctors = new BLVariableLong[2];

	public final BLVariableLong untouched;
	public final BLVariableInt moveCount[] = new BLVariableInt[2];
	public final BLVariableLong enPassantePos;;//@todo WTF -stack

	public BBAnalyzer analyzer = new BBAnalyzer(this);
	public LookUp lookUp = new LookUp();

	private int colorAtTurn = COLOR_WHITE;

	private int[] indices1 = new int[64];
	private int[] indices2 = new int[64];
	private int[] indices3 = new int[64];
	private int[] indices4 = new int[64];

	
	public BBPosition() {

		for (int i = 0; i < 64; i++) {
			tCallBacks[i] = new BLVariableLong(bl, 0L);
			moveMasks[i] = new BLVariableLong(bl, 0L);
			
			for(int j=0;j<4;j++) {
				callBacks[i][j] = new BLVariableLong(bl, 0L);
			}
			fields[i] = new BLVariableInt(bl, -1);
					 
		}

		enPassantePos = new BLVariableLong(bl, 0);
		untouched = new BLVariableLong(bl, 0L);
		allOfOneColor[COLOR_WHITE] = new BLVariableLong(bl, 0L);
		allOfOneColor[COLOR_BLACK] = new BLVariableLong(bl, 0L);
		correctors[COLOR_WHITE] = new BLVariableLong(bl, 0L);
		correctors[COLOR_BLACK] = new BLVariableLong(bl, 0L);
		allMovesLists[COLOR_WHITE]  = new BLIndexedList<Move>(bl,28*16,7928);
		allMovesLists[COLOR_BLACK]  = new BLIndexedList<Move>(bl,28*16,7928);
		moveCount[COLOR_WHITE]  = new BLVariableInt(bl,0);
		moveCount[COLOR_BLACK]  = new BLVariableInt(bl,0);
		
		
	}

	@Override
	public void initialAddToBoard(int color, int type, int pos) {
		fields[pos].set((type * 2 + color) * 64);
		allOfOneColor[color].setBit(pos);
	}

	@Override
	public void setUntouched(int rank, int file) {
		untouched.setBit(getPosForFileRank(file, rank));
	}

	@Override
	public void setEnPassantePos(int enpassantePos) {
		this.enPassantePos.set(enpassantePos);
	}

	@Override
	public void unSetMove(int move) {
		// TODO Auto-generated method stub
		bl.undo();
		this.colorAtTurn = OTHER_COLOR[colorAtTurn];
	}

	int getLevel(){
		return bl.getLevel()-1;
	}



	
	
	@Override
	public void setMove(int index) {
		/** MODIFY **/
		Move move = getMove(index);
		bl.startNextLevel();
		setMove(move);		
		this.colorAtTurn = OTHER_COLOR[colorAtTurn];
		int color = this.colorAtTurn;
		//IndexedList<Move> list = allMovesLists[getLevel()];
		//calcNewMoves(list,color);
	}
		
	private void setMove(Move move) {
		//@todo use masks for move
		//out(this.allMoves[3].get());
		int oldPos = move.getOldPos();
		int newPos = move.getNewPos();
		int color = this.colorAtTurn;
		int otherColor = OTHER_COLOR[color];

		// update fields
/*u*/	this.untouched.unsetBit(oldPos);
		// update fields
/*u*/	this.untouched.unsetBit(newPos);
		
/*E*/	int otherTypeColor = fields[newPos].get();
		if(otherTypeColor!=-1) {
/*E*/			removePseudoMoves(otherTypeColor,newPos);
		}
		int typeColor = fields[oldPos].getAndSet(-1);
		removePseudoMoves(typeColor,oldPos);
		if(move.isPromotion()) {
			typeColor = move.getPromotePieceType();
		}
/*u*/	fields[newPos].set(typeColor);
		// update occupancy
/*u*/	this.allOfOneColor[otherColor].unsetBit(newPos);// might be empty anyways
/*u*/	this.allOfOneColor[color].moveBit(oldPos, newPos);
		// update moves
		
		long cbs = this.tCallBacks[newPos].get() | this.tCallBacks[oldPos].get();

		long enpMask = this.enPassantePos.getAndSet(0L);
		if(enpMask!=0L)	{
			int enpPos = Long.numberOfTrailingZeros(enpMask);
			cbs |= this.tCallBacks[enpPos].get(); 				
			if(move.getEnPassanteSquare()==enpPos) {
				int enpPawnPos = move.getEnPassantePawnPos();
				cbs |= this.tCallBacks[enpPawnPos].get(); 				
				int enpTypeColor = fields[enpPawnPos].get();
/*u*/			this.fields[enpPawnPos].set(-1);
/*u*/			this.allOfOneColor[otherColor].unsetBit(enpPawnPos);// might be empty anyways
				removePseudoMoves(enpTypeColor,enpPawnPos);
			}
		}

		this.updatePseudoMoves(typeColor, newPos);
		
		if(move.isTwoSquarePush()) {
			int enpSquare= move.getEnPassanteSquare();
			this.enPassantePos.set(1L<<enpSquare);//@TBD always set (geht mit stack)
			cbs |= this.tCallBacks[enpSquare].get(); 
		}
		
		cbs &= (this.allOfOneColor[color].get() | this.allOfOneColor[otherColor].get());//@todo occ var
		// get callbacks

		int count = updateIndices(indices4, cbs);
		for (int i = 0; i < count; i++) {
			int cbPos = indices4[i];
			if (cbPos == newPos||cbPos == oldPos) {
				continue;// @todo Wtf
			}
			int cbTypeColor = fields[cbPos].get();
			this.updatePseudoMoves(cbTypeColor, cbPos);
		}
		
		if(move.isRochade()) {
			this.setMove(move.getRookMove());
		}
	}

	@Override
	public void setInitialTurn(int color) {
		colorAtTurn = color;
	}


/*
  	public void calcNewMoves(IndexedList<Move> list, int color) {
 
		list.reset();
		int otherColor = OTHER_COLOR[color];
		
		long own = this.allOfOneColor[color].get();// @todo WTF too expensive
	
		
		// long notOwn = ~own;
		int count1 = updateIndices(indices1, own);
		for (int i = 0; i < count1; i++) {
			int pos = indices1[i];
			int typeColor = fields[pos].get();
			int index = pos + typeColor;
			Move[] moves = lookUp.getMoveMap(index);
			long moveMask = this.moveMasks[pos].get();
			int count2 = updateIndices(indices2, moveMask);
			if((typeColor==0 || typeColor ==64 )&& (moveMask & MASK_OUTER_RANKS)!=0L)  {
				int dir = PAWN_MOVE_DIR[color]*8;
				for (int j = 0; j < count2; j++) {
					int curPos = indices2[j];
					list.add(moves[curPos]);
					curPos = curPos-dir;
					list.add(moves[curPos]);
					curPos = curPos-dir;
					list.add(moves[curPos]);
					curPos = curPos-dir;
					list.add(moves[curPos]);
				}
			}else {
				for (int j = 0; j < count2; j++) {
					list.add(moves[indices2[j]]);
				}	
			}
			
		}
	}
*/
	@Override
	public int getHash() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getNotation(int index) {
		return this.getMove(index).getNotation();
	}

	@Override
	public void initialEval() {
		for (int i = 0; i < 2; i++) {
			long own = this.allOfOneColor[i].get();
			int count1 = updateIndices(indices1, own);
			for (int j = 0; j < count1; j++) {
				int pos = indices1[j];
				int typeColor = fields[pos].get();
				updatePseudoMoves(typeColor, pos);
			}
		}
	}

	private void removePseudoMoves(int typeColor, int pos) {
		int color = typeColor >> 6 & 1;
		switch (typeColor) {
			case PIECE_TYPE_WHITE_PAWN: 
			case PIECE_TYPE_BLACK_PAWN: {
				long correction =this.correctors[color].get();
				correction  &= ~this.callBacks[pos][1].get();
				this.correctors[color].set(correction);
				break;
			}
			case PIECE_TYPE_BLACK_KING: 
			case PIECE_TYPE_WHITE_KING: {
				long correction =this.correctors[color].get();
				correction&= ~MASK_1_RANK; //delete old corrections
				this.correctors[color].set(correction);
				break;
			}
		}
		//@TODO WTF 
		long oldCBs = callBacks[pos][0].get()|callBacks[pos][1].get();
		this.callBacks[pos][0].set(0L);
		this.callBacks[pos][1].set(0L);
		long oldMoves = moveMasks[pos].get();
		this.moveCount[color].subtraction(Long.bitCount(moveMasks[pos].getAndSet(0L)));;
		
		Move[] moveObjs = lookUp.getMoveMap(pos + typeColor);
		BLIndexedList <Move> list = this.allMovesLists[color];
							
		int count = updateIndices(indices3, oldCBs&~oldMoves);
		for (int j = 0; j < count; j++) {
			this.tCallBacks[indices3[j]].toggleBit(pos);
		}
		
		count = updateIndices(indices3, oldCBs&oldMoves);
		for (int j = 0; j < count; j++) {
			int index = indices3[j];
			this.tCallBacks[index].toggleBit(pos);
			list.remove(moveObjs[index]);

		}
		
		count = updateIndices(indices3, ~oldCBs&oldMoves);
		for (int j = 0; j < count; j++) {
			list.remove(moveObjs[indices3[j]]);		
		}
	}

	private long[] callbacks = new long[4];
	private void updatePseudoMoves(int typeColor, int pos) {
		callbacks[0] = 0L;
		callbacks[1] = 0L;
		long moves = 0L;
		
		int color = typeColor >> 6 & 1;
			
		long correction = this.correctors[color].get();
		
		int otherColor = OTHER_COLOR[color];
		long own = this.allOfOneColor[color].get();// @todo WTF too expensive

		switch (typeColor) {
		case PIECE_TYPE_WHITE_PAWN: {
			long other = this.allOfOneColor[otherColor].get();
			long notOcc = ~(own | other);
			int file = getFileForPos(pos);
			
			
			long oneUp = (1L << pos) << DIR_UP;
			long twoUp= oneUp|((oneUp  & notOcc & MASK_3_RANK) << DIR_UP) ;
			
			correction &= ~(this.callBacks[pos][1].get());
			correction |= twoUp;
			
			callbacks[0] = lookUp.getMoveMask(pos + typeColor) & MASK_NOT_X_FILE[file];
			callbacks[1] = twoUp;
			
			moves = callbacks[0] & (other | (this.enPassantePos.get() & MASK_6_RANK));
			moves |= callbacks[1] & notOcc;
			
			if((moves & MASK_OUTER_RANKS)!=0L)  {
				moves|= moves>>DIR_UP;
				moves|= moves>>DIR_UP;
				moves|= moves>>DIR_UP;
			}
			break;
		}

		case PIECE_TYPE_BLACK_PAWN: {
			long other = this.allOfOneColor[otherColor].get();
			long notOcc = ~(own | other);
			int file = getFileForPos(pos);
			
			
			long oneUp = (1L << pos) >> DIR_UP;
			long twoUp= oneUp|((oneUp  & notOcc & MASK_6_RANK)>> DIR_UP) ;
			
			correction &= ~(this.callBacks[pos][1].get());
			correction |= twoUp;
			
			callbacks[0] = lookUp.getMoveMask(pos + typeColor) & MASK_NOT_X_FILE[file];
			callbacks[1] = twoUp;
	
			moves = callbacks[0] & (other | (this.enPassantePos.get() & MASK_3_RANK));
			moves |= callbacks[1] & notOcc;
			
			if((moves & MASK_OUTER_RANKS)!=0L)  {
				moves|= moves<<DIR_UP;
				moves|= moves<<DIR_UP;
				moves|= moves<<DIR_UP;
			}
			
			break;
		}
		case PIECE_TYPE_BLACK_BISHOP:
		case PIECE_TYPE_WHITE_BISHOP: {
			long other = this.allOfOneColor[otherColor].get();
			long occ = own | other;
			callbacks[0] = mnf.getBishopAttacks(pos, occ);
			moves = callbacks[0] & ~own;
			break;
		}
		case PIECE_TYPE_BLACK_ROOK:
		case PIECE_TYPE_WHITE_ROOK: {
			long other = this.allOfOneColor[otherColor].get();
			long occ = own | other;
			callbacks[0] = mnf.getRookAttacks(pos, occ);
			moves = callbacks[0] & ~own;
			break;
		}
		case PIECE_TYPE_BLACK_QUEEN:
		case PIECE_TYPE_WHITE_QUEEN: {
			long other = this.allOfOneColor[otherColor].get();
			long occ = own | other;
			callbacks[0] = mnf.getBishopAttacks(pos, occ);
			callbacks[1] = mnf.getRookAttacks(pos, occ);
			moves = (callbacks[0]|callbacks[1]) & ~own;
			break;
		}
		case PIECE_TYPE_WHITE_KNIGHT:
		case PIECE_TYPE_BLACK_KNIGHT: {
			callbacks[0] = lookUp.getMoveMask(pos + typeColor);
			moves = callbacks[0] & ~own;
			break;
		}
		
		case PIECE_TYPE_WHITE_KING: {
			callbacks[0] = lookUp.getMoveMask(pos + typeColor);
			moves = callbacks[0] & ~own;
			long ntchd = untouched.get();
			long other = this.allOfOneColor[otherColor].get();
			long occ = own | other;
			correction &= ~MASK_1_RANK; //delete old corrections
			if ((ntchd & MASK_E1) !=0L) {
				moves &= ~MASK_CASTLE_KING_K&~MASK_CASTLE_KING_Q; // callbacks are still just the 8 direct fields				
				long scan =0;
				if ((ntchd & MASK_H1) !=0L) {
					if ((occ & MASK_CASTLE_OCC_K) == 0L) {
						moves |= MASK_CASTLE_KING_K;
					}
					scan = MASK_CASTLE_ALL_K;
				}
				if ((ntchd & MASK_A1) !=0L) {
					if ((occ & MASK_CASTLE_OCC_Q) == 0L) {
						moves |= MASK_CASTLE_KING_Q;
					}
					scan |= MASK_CASTLE_ALL_Q;
				}
				scan &= ~callbacks[0];
				correction |= scan;
				callbacks[1] |= scan;

			}
			break;
			
			
			
			
		}
		case PIECE_TYPE_BLACK_KING: {
			callbacks[0] = lookUp.getMoveMask(pos + typeColor);
			moves = callbacks[0] & ~own;
			long other = this.allOfOneColor[otherColor].get();
			long occ = own | other;
			long ntchd = untouched.get();
			correction&= ~MASK_8_RANK; //delete old corrections
			if ((ntchd & MASK_E8) !=0L) {
				long scan =0;
				if ((ntchd & MASK_H8) !=0L) {
					if ((occ & MASK_CASTLE_OCC_k) == 0L) {
						moves |= MASK_CASTLE_KING_k;
					}
					scan = MASK_CASTLE_ALL_k;
				}
				if ((ntchd & MASK_A8) !=0L) {
					
					if ((occ & MASK_CASTLE_OCC_q) == 0L) {
						moves |= MASK_CASTLE_KING_q;
					}
					scan |= MASK_CASTLE_ALL_q;
				}
				scan &= ~callbacks[0];
				correction |= scan;
				callbacks[1] |= scan;
				break;
			}
		}
		}
		Move[] moveObjs = lookUp.getMoveMap(pos + typeColor);
		
		long toggleCallbacks = this.callBacks[pos][0].get() ^ callbacks[0];
		toggleCallbacks|= this.callBacks[pos][1].get() ^ callbacks[1];
		long oldMoves = this.moveMasks[pos].get() ;
		long toggleMoves = oldMoves ^ moves;
		int delta = Long.bitCount(moves) - Long.bitCount(oldMoves);
		this.moveCount[color].addition(delta);;
		int count = updateIndices(indices3, toggleCallbacks & ~toggleMoves);
		for (int j = 0; j < count; j++) {
			this.tCallBacks[indices3[j]].toggleBit(pos);
		}
		BLIndexedList <Move> list = this.allMovesLists[color];
		long mask = toggleCallbacks & toggleMoves;
		count = updateIndices(indices3, mask);
		for (int j = 0; j < count; j++) {
			this.tCallBacks[indices3[j]].toggleBit(pos);
			int index = indices3[j];
			list.toggle(moveObjs[index]);
		}
			
		mask = ~toggleCallbacks & toggleMoves;
		count = updateIndices(indices3, mask);
		for (int j = 0; j < count; j++) {
			int index = indices3[j];
			list.toggle(moveObjs[index]);
		}
		
		
		this.callBacks[pos][0].set(callbacks[0]);
		this.callBacks[pos][1].set(callbacks[1]);
		
		this.moveMasks[pos].set(moves);
		this.correctors[color].set(correction);
	}

	@Override
	public int getColorAtTurn() {
		return this.colorAtTurn;
	}

	@Override
	public void checkGameState(int colorAtTurn) {
		// TODO Auto-generated method stub

	}

	@Override
	public void checkLegalMoves() {
		// TODO Auto-generated method stub

	}

	public String toString() {
		return analyzer.toString();
	}
	
	public int[] getAttacks (int color){
		System.out.println("+");
		int[] attacks = new int[64];
		long own = allOfOneColor[color].get();
		long correction = correctors[color].get();
		for (int j = 0; j < 64; j++) {
			attacks[j] = (int)(Long.bitCount(tCallBacks[j].get()&own)
					- (( correction>> j) & 1 ));
		}
		return attacks;
	}

	public int getMoves() {
		return moveCount[colorAtTurn].get();
	}
	
	public Move getMove(int index) {
		return this.allMovesLists[colorAtTurn].getElement(index);
	}
}
