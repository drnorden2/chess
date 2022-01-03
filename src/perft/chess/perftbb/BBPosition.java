package perft.chess.perftbb;

import static perft.chess.Definitions.*;


import perft.chess.Position;
import perft.chess.core.baseliner.BLIndexedList;
import perft.chess.core.baseliner.BLVariableInt;
import perft.chess.core.baseliner.BLVariableLong;
import perft.chess.core.baseliner.BaseLiner;
import perft.chess.core.datastruct.ArrayStack;
import perft.chess.perftbb.gen.MagicNumberFinder;
import perft.chess.perftmb.Field;
import perft.chess.perftmb.FieldCallback;
import perft.chess.perftmb.Piece;



public class BBPosition implements Position {
	public final int depth = 10;
	private final CheckMask cm = new CheckMask();
	public final BaseLiner bl = new BaseLiner(7928, 20000, 1000, depth, 1000);
	MagicNumberFinder mnf = new MagicNumberFinder();
	private ArrayStack<ContextLevel> contextLevels = new ArrayStack<ContextLevel>(new ContextLevel[depth+2]);


	
	public final BLVariableInt[] fields = new BLVariableInt[64];
	public final BLVariableLong[] allOfOneColor = new BLVariableLong[2];//@todo WTF -stack
	public final BLVariableLong[][] callBacks = new BLVariableLong[64][4];
	public final BLVariableLong[] moveMasks = new BLVariableLong[64];
	
	public final BLVariableLong[] tCallBacks = new BLVariableLong[64];
	public final BLVariableLong[] kings = new BLVariableLong[2];
	public final BLVariableLong[] correctors = new BLVariableLong[2];

	public final BLVariableLong untouched;
	public final BLVariableInt moveCount[] = new BLVariableInt[2];
	public final BLVariableLong enPassantePos;;//@todo WTF -stack

	public BBAnalyzer analyzer = new BBAnalyzer(this);
	public LookUp lookUp = new LookUp();

	private int colorAtTurn = COLOR_WHITE;



	
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
		moveCount[COLOR_WHITE]  = new BLVariableInt(bl,0);
		moveCount[COLOR_BLACK]  = new BLVariableInt(bl,0);
		kings[COLOR_WHITE] = new BLVariableLong(bl, 0L);
		kings[COLOR_BLACK] = new BLVariableLong(bl, 0L);
		
		for(int i=0;i<depth+2;i++) {
			ContextLevel allMoves = new ContextLevel();
			this.contextLevels.add(allMoves);
		}
	}



	
int calcCounter=0;

  	public void calcNewMoves(int color) {
  		ContextLevel oldList = this.contextLevels.get(getLevel()-2);
  		ContextLevel list = this.contextLevels.get(getLevel());
  		list.reInit();

  		long bits = this.allOfOneColor[color].get();// @todo WTF too expensive
  		int retVal = Long.bitCount(bits);
		for(int i=0;i<retVal;i++) {
			int pos = Long.numberOfTrailingZeros(bits);
			bits &= bits - 1;			
			long moveMask = this.moveMasks[pos].get();
			int typeColor = fields[pos].get();
			if(list.checkForReuseMoves(pos, moveMask, typeColor)) {
				continue;
			}
			Move[] moves = oldList.getMoves(pos, moveMask, typeColor);
			if(moves==null) {
				moves =lookUp.getMoveMap(pos+typeColor);
				list.extractFromRawMoves(pos, moveMask, typeColor, moves);
			}else {
				list.addMoves(pos, moveMask, typeColor, moves);
			}
		}
		list.resetIterator();
  	}


	@Override
	public void initialEval() {
		ContextLevel context = this.contextLevels.get(getLevel()-1);

		for (int i = 0; i < 2; i++) {
			long own = this.allOfOneColor[i].get();
			int retVal = Long.bitCount(own);
			for(int j=0;j<retVal;j++) {
				int pos = Long.numberOfTrailingZeros(own);
				own &= own- 1;			
				int typeColor = fields[pos].get();
				if(typeColor==PIECE_TYPE_BLACK_KING||typeColor==PIECE_TYPE_WHITE_KING) {
					this.kings[i].set(1L<<pos);
				}
				context.white = this.allOfOneColor[i].get();// @todo WTF too expensive
				context.black = this.allOfOneColor[OTHER_COLOR[i]].get();
				context.occ = context.white| context.black;
				context.notOcc = ~(context.occ);
				context.ntchd = untouched.get();
				context.correction =this.correctors[i].get();

				updatePseudoMoves(typeColor, pos);
			}
		}
	}

	private void removePseudoMoves(int typeColor, int pos) {
		int color = typeColor >> 6 & 1;
		switch (typeColor) {
			case PIECE_TYPE_WHITE_PAWN: 
			case PIECE_TYPE_BLACK_PAWN: {
				long correction =this.callBacks[pos][1].get();
				if(correction!=0) {
					this.correctors[color].AND_NOT(correction);
				}
				break;
			}
			case PIECE_TYPE_BLACK_KING: 
			case PIECE_TYPE_WHITE_KING: {
				long correction = this.correctors[color].get() & MASK_NOT_BASE_RANK[color];
				if(correction!=0) {
					this.correctors[color].AND_NOT(correction);
				}
				break;
			}
		}
		//@TODO WTF 
		long oldCBs = callBacks[pos][0].get()|callBacks[pos][1].get();
		long bits = oldCBs ;
		int retVal = Long.bitCount(bits);
		for(int i=0;i<retVal;i++) {
			this.tCallBacks[Long.numberOfTrailingZeros(bits)].toggleBit(pos);
			bits &= bits - 1;			
		}

		this.callBacks[pos][0].set(0L);
		this.callBacks[pos][1].set(0L);
		this.moveCount[color].subtraction(Long.bitCount(moveMasks[pos].getAndSet(0L)));;
		
	}

	private long[] callbacks = new long[4];
	
	private void updatePseudoMoves(int typeColor, int pos) {
		
			/*, int moveColor, 
			boolean newPosEmptyBefore,
			boolean newPosReplace, 
			boolean oldPos, 
			boolean enpPawnRemoved,	
			boolean enpOptRemoved,
			boolean enpOptCreated) {*/
		
		int color = typeColor >> 6 & 1;
		
		
		ContextLevel context = this.contextLevels.get(getLevel()-1);
		callbacks[0] = 0L;
		callbacks[1] = 0L;
		long moves = 0L;
		int otherColor = OTHER_COLOR[color];
		long own = this.allOfOneColor[color].get();// @todo WTF too expensive
		long other = this.allOfOneColor[otherColor].get();


		switch (typeColor) {
		case PIECE_TYPE_WHITE_PAWN: {
		//	int file = getFileForPos(pos);

			long oneUp = (1L << pos) << DIR_UP;
			long twoUp= oneUp|((oneUp  & context.notOcc & MASK_3_RANK) << DIR_UP) ;

			callbacks[0] = lookUp.getMoveMask(pos + typeColor) & MASK_NOT_X_FILE_FOR_POS[pos];
			callbacks[1] = twoUp;
			
			moves = callbacks[0] & (other | (this.enPassantePos.get() & MASK_6_RANK));
			moves |= callbacks[1] & context.notOcc;
			
			if((moves & MASK_OUTER_RANKS)!=0L)  {
				moves|= moves>>DIR_UP;
				moves|= moves>>DIR_UP;
				moves|= moves>>DIR_UP;
			}
			
			long delta = this.callBacks[pos][1].get()^twoUp;
			if(delta!=0) {
				this.correctors[color].XOR(delta);
			}

			break;
		}

		case PIECE_TYPE_BLACK_PAWN: {
			
			
			long oneUp = (1L << pos) >> DIR_UP;
			long twoUp= oneUp|((oneUp  & context.notOcc & MASK_6_RANK)>> DIR_UP) ;
			
			
			callbacks[0] = lookUp.getMoveMask(pos + typeColor) &  MASK_NOT_X_FILE_FOR_POS[pos];
			callbacks[1] = twoUp;
	
			moves = callbacks[0] & (other | (this.enPassantePos.get() & MASK_3_RANK));
			moves |= callbacks[1] & context.notOcc;
			
			if((moves & MASK_OUTER_RANKS)!=0L)  {
				moves|= moves<<DIR_UP;
				moves|= moves<<DIR_UP;
				moves|= moves<<DIR_UP;
			}
			long delta = this.callBacks[pos][1].get()^twoUp;
			if(delta!=0) {
				this.correctors[color].XOR(delta);
			}
			break;
		}
		case PIECE_TYPE_BLACK_BISHOP:
		case PIECE_TYPE_WHITE_BISHOP: {
			callbacks[0] = mnf.getBishopAttacks(pos, context.occ);
			moves = callbacks[0] & ~own;
			break;
		}
		case PIECE_TYPE_BLACK_ROOK:
		case PIECE_TYPE_WHITE_ROOK: {
			callbacks[0] = mnf.getRookAttacks(pos, context.occ);
			moves = callbacks[0] & ~own;
			break;
		}
		case PIECE_TYPE_BLACK_QUEEN:
		case PIECE_TYPE_WHITE_QUEEN: {
			callbacks[0] = mnf.getBishopAttacks(pos, context.occ);
			callbacks[1] = mnf.getRookAttacks(pos, context.occ);
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
			
			long scan =0L;
			if ((context.ntchd & MASK_E1_H1) ==MASK_E1_H1) {
				if ((context.occ & MASK_CASTLE_OCC_K) == 0) {
					moves |= MASK_CASTLE_KING_K;
				}
				scan = MASK_CASTLE_ALL_K;
			}
			if ((context.ntchd & MASK_E1_A1) ==MASK_E1_A1) {
				if ((context.occ & MASK_CASTLE_OCC_Q) == 0) {
					moves |= MASK_CASTLE_KING_Q;
				}
				scan |= MASK_CASTLE_ALL_Q;
			}
			if(scan!=0) {
				callbacks[1] |= scan;
			}
	
			long delta = (context.correction & MASK_NOT_1_RANK)^scan;
			if(delta!=0) {
				this.correctors[color].XOR(delta);
			}
				
			break;	
		}
		case PIECE_TYPE_BLACK_KING: {
			callbacks[0] = lookUp.getMoveMask(pos + typeColor);
			moves = callbacks[0] & ~own;
			long scan =0;
			if ((context.ntchd & MASK_E8_H8) ==MASK_E8_H8) {
				if ((context.occ & MASK_CASTLE_OCC_k) == 0L) {
					moves |= MASK_CASTLE_KING_k;
				}
				scan = MASK_CASTLE_ALL_k;
			}
			if ((context.ntchd & MASK_E8_A8) ==MASK_E8_A8) {
				if ((context.occ & MASK_CASTLE_OCC_q) == 0L) {
					moves |= MASK_CASTLE_KING_q;
				}
				scan |= MASK_CASTLE_ALL_q;
			}
			if(scan!=0) {
				callbacks[1] |= scan;
			}
	
			long delta = (context.correction & MASK_NOT_1_RANK)^scan;
			if(delta!=0) {
				this.correctors[color].XOR(delta);
			}
			break;
			
		}
		}
		
		long toggleCallbacks = this.callBacks[pos][0].get() ^ callbacks[0];
		toggleCallbacks|= this.callBacks[pos][1].get() ^ callbacks[1];
	
		int retVal = Long.bitCount(toggleCallbacks);
		if(retVal>0) {
			for(int i=0;i<retVal;i++) {
				this.tCallBacks[Long.numberOfTrailingZeros(toggleCallbacks)].toggleBit(pos);
				toggleCallbacks &= toggleCallbacks - 1;			
			}
			this.callBacks[pos][0].set(callbacks[0]);
			this.callBacks[pos][1].set(callbacks[1]);
		}
		
		long oldMoves = this.moveMasks[pos].get() ;
		if(oldMoves!=moves) {
			int delta = Long.bitCount(moves) - Long.bitCount(oldMoves);
			if(delta!=0) {
				this.moveCount[color].addition(delta);
			}
			this.moveMasks[pos].set(moves);
		}
	}

	
	@Override
	public void setMove(int index) {
		Move move = getMove(index);
	
		bl.startNextLevel();
		setMove(move);		
		checkLegalMoves();
		this.colorAtTurn = OTHER_COLOR[colorAtTurn];
	}
		
	private void setMove(Move move) {
		ContextLevel context = this.contextLevels.get(getLevel()-1);
		//@todo use masks for move
		int oldPos = move.getOldPos();
		int newPos = move.getNewPos();
		long oldPosMask = 1L<<oldPos;
		long newPosMask = 1L<<newPos;
		long moveMask=oldPosMask|newPosMask;
		int color = this.colorAtTurn;
		int otherColor = OTHER_COLOR[color];
		long cbsNewPosEmptyBefore=0;
		long cbsNewPosReplace=0;
		
		long cbsOldPos=0;
		long cbsEnpPawnRemoved=0; 				
		long cbsEnpOptCreated=0; 
		long cbsEnpOptRemoved=0; 				

		
		if(move.getRochadeDisabler()+context.ntchd!=0) { 
			// update fields
	/*u*/	this.untouched.unsetBit(oldPos);
			// update fields
	/*u*/	this.untouched.unsetBit(newPos);
		}
		
/*E*/	int otherTypeColor = fields[newPos].get();
		if(otherTypeColor!=-1) {
/*!!!*/		removePseudoMoves(otherTypeColor,newPos);
			cbsNewPosReplace= this.tCallBacks[newPos].get();
			this.allOfOneColor[otherColor].XOR(newPosMask);
		}else {
			cbsNewPosEmptyBefore = this.tCallBacks[newPos].get();
		}
		int typeColor = fields[oldPos].getAndSet(-1);
/*!!!*/	removePseudoMoves(typeColor,oldPos);
		if(move.isPromotion()) {
			typeColor = move.getPromotePieceType();
		}
/*u*/	fields[newPos].set(typeColor);
		// update occupancy
/*u*/	this.allOfOneColor[color].XOR(moveMask);
		// update moves
		
		cbsOldPos = this.tCallBacks[oldPos].get();
		if(move.getPieceType()==PIECE_TYPE_BLACK_KING) {
			this.kings[color].set(1L<<newPos);
		}
		
		if(context.enpMask!=0L)	{
			cbsEnpOptRemoved = this.tCallBacks[context.enpPos].get(); 				
			if(move.getEnPassanteSquare()==context.enpPos) {
				int enpPawnPos = move.getEnPassantePawnPos();
				cbsEnpPawnRemoved = this.tCallBacks[enpPawnPos].get(); 				
				int enpTypeColor = fields[enpPawnPos].get();
/*u*/			this.fields[enpPawnPos].set(-1);
/*u*/			this.allOfOneColor[otherColor].unsetBit(enpPawnPos);// might be empty anyways
/*!!*/			removePseudoMoves(enpTypeColor,enpPawnPos);
			}
		}

			
		if(move.isTwoSquarePush()) {
			int enpSquare= move.getEnPassanteSquare();
			this.enPassantePos.set(1L<<enpSquare);//@TBD always set (geht mit stack)
			cbsEnpOptCreated= this.tCallBacks[enpSquare].get(); 
		}

		
		context.white = this.allOfOneColor[color].get();// @todo WTF too expensive
		context.black = this.allOfOneColor[otherColor].get();
		context.occ = context.white| context.black;
		context.notOcc = ~(context.occ);
		context.ntchd = untouched.get();
		context.correction =this.correctors[color].get();

		this.updatePseudoMoves(typeColor, newPos);
	
		long cbs = cbsNewPosEmptyBefore|cbsNewPosReplace|cbsOldPos|cbsEnpPawnRemoved|cbsEnpOptRemoved|cbsEnpOptCreated;
		cbs &= (context.occ)&~(moveMask);//@todo occ var

		
		int retVal = Long.bitCount(cbs);
			retVal = Long.bitCount(cbs);
		for(int i=0;i<retVal;i++) {
			int cbPos = Long.numberOfTrailingZeros(cbs);
			//long cbPosMask = 1L<<cbPos;
			cbs &= cbs - 1;				
			int cbTypeColor = fields[cbPos].get();
			this.updatePseudoMoves(cbTypeColor, cbPos);
				/*,color, 
					(cbsNewPosEmptyBefore&cbPosMask)!=0, 
					(cbsNewPosReplace&cbPosMask)!=0, 
					(cbsOldPos&cbPosMask)!=0, 
					(cbsEnpPawnRemoved&cbPosMask)!=0,
					(cbsEnpOptRemoved&cbPosMask)!=0,
					(cbsEnpOptCreated&cbPosMask)!=0);*/
		}

		
		if(move.isRochade()) {
			this.setMove(move.getRookMove());
		}
	}
	
	@Override
	public void unSetMove(int move) {
		// TODO Auto-generated method stub
		bl.undo();
		this.colorAtTurn = OTHER_COLOR[colorAtTurn];
	}

	@Override
	public void setInitialTurn(int color) {
		colorAtTurn = color;
	}
	
	@Override
	public int getColorAtTurn() {
		return this.colorAtTurn;
	}

	@Override
	public void checkGameState(int colorAtTurn) {
		// TODO Auto-generated method stub

	}

	
	public int getMoves() {
		return moveCount[colorAtTurn].get();
	}
	
	public Move getMove(int index) {
		if(index==0) {
			
			this.calcNewMoves(this.colorAtTurn);
			ContextLevel context = this.contextLevels.get(getLevel());
			context.enpMask= this.enPassantePos.getAndSet(0L);
			context.enpPos = Long.numberOfTrailingZeros(context.enpMask);
			
		}
		return this.contextLevels.get(getLevel()).getMove(index);
	}

	int getLevel(){
		return bl.getLevel()+1;
	}

	public String toString() {
		return analyzer.toString();
	}
	
	
	@Override
	public void initialAddToBoard(int color, int type, int pos) {
		fields[pos].set((type * 2 + color) << 6);
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
	public int getHash() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getNotation(int index) {
		return this.getMove(index).getNotation();
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
	
	@Override
	public void checkLegalMoves (){
	/*	
		int color = getColorAtTurn();
		int otherColor = OTHER_COLOR[color];
		long kingMask = this.kings[color].get();
		int kingPos = Long.numberOfTrailingZeros(kingMask);
		long enpassantePos = enPassantePos.get();
		long own = this.allOfOneColor[color].get();// @todo WTF too expensive
		long other = this.allOfOneColor[otherColor].get();

		long kingCbs = this.tCallBacks[kingPos].get()&other;
		int kingCBCount = Long.bitCount(kingCbs);
		int kingAttacks = kingCBCount -this.correctors[otherColor].getBitAsInt(kingPos);
		
		int kingAttackerPos = -1;
		int attackerTypeColor = -1;
		
		long checkMask = ~0L;
		if(kingAttacks==1) {
			for(int i=0;i<kingCBCount;i++) {
				kingAttackerPos = Long.numberOfTrailingZeros(kingCbs);
				if(this.moveMasks[kingAttackerPos].getBit(kingPos)) {
					attackerTypeColor = this.fields[kingAttackerPos].get();
					break;
				}
			}
			checkMask = cm.checkMasks[kingPos][kingAttackerPos];
		}else if(kingAttacks>1) {
			checkMask=0L;
		}
		
		for(int i=0;i<allPiecesOfCurCol.size();i++) {
			Piece piece = allPiecesOfCurCol.getElement(i);	
			int oldPos = piece.getPosition();
			Field field = position.fields[oldPos];
			//int pseudoMoveCount =  field.getPseudoMoveList(pseudoMoves);
			int pseudoMoveCount =  field.pseudoMoves.size();
			
			if(pseudoMoveCount!=0) {
				
				if(piece.getType()==PIECE_TYPE_KING) {
					//private void handleKingPiece(Field oldKingField, int oldKingPos, int otherColor, int oldKingPosAttacks, int pseudoMoveCount,int level) {
					this.handleKingPiece(field, oldPos, otherColor, kingAttacks,pseudoMoveCount);
				}else {
					if(kingAttacks<2) {
						//private void handleOtherPiece(Field field, Piece piece, int oldPos, int curColor, int otherColor, int kingPos, int kingAttacks, int level, int pseudoMoveCount) {
						this.handleOtherPiece(field, piece, oldPos, curColor, otherColor, kingPos, kingAttacks,kingAttackerCB,pseudoMoveCount,enpassantePos);
					}//otherwise there is no hope!
				}
			}
		}
		this.rescueMap=0L;
		*/
	}

}
