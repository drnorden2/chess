package perft.chess.perftbb;

import static perft.chess.Definitions.*;


import perft.chess.Position;
import perft.chess.core.baseliner.BLVariableInt;
import perft.chess.core.baseliner.BLVariableLong;
import perft.chess.core.baseliner.BaseLiner;
import perft.chess.core.datastruct.ArrayStack;
import perft.chess.core.o.O;
import perft.chess.perftbb.gen.MagicNumberFinder;



public class BBPosition implements Position {
	private int level =2;
	public final int depth = 10;
	private int colorAtTurn = COLOR_WHITE;


	public final int[] fields = new int[64];
	public final long[] callBacks = new long[64];
	public final long[] moveMasks = new long[64];
	public final long[] tCallBacks = new long[64];
	
	public final long[] allOfOneColor = new long[2];
	public final long[] kings = new long[2];
	public final long[] pawns = new long[2];
	public final int moveCount [] = new int[2];
	public long untouched=0;
	public long enPassanteMask=0;

		
	public BBAnalyzer analyzer = new BBAnalyzer(this);
	public LookUp lookUp = new LookUp();
	private final CheckMask cm = new CheckMask();
	private final MagicNumberFinder mnf = new MagicNumberFinder();
	 final ContextLevel[] contextLevels = new ContextLevel[depth+2];

	//temps
	private long _notOcc; 
	private long _occ;
	


	public BBPosition() {
		for(int i=0;i<depth+2;i++) {
			this.contextLevels[i]=new ContextLevel(this,i);
		}
		for(int i=0;i<64;i++) {
			fields[i]=-1;
		}
	}


	@Override
	public void initialAddToBoard(int color, int type, int pos) {
		int typeColor =((type <<1) + color) << 6;
	
		this.fields[pos]= typeColor;
		allOfOneColor[color]|=SHIFT[pos];
		if(type==PIECE_TYPE_KING) {
			this.kings[color]|=SHIFT[pos];
		}
		if(type==PIECE_TYPE_PAWN) {
			this.pawns[color]|=SHIFT[pos];
		}
		
	}

	@Override
	public void initialUntouched(int rank, int file) {
		untouched|=SHIFT[getPosForFileRank(file, rank)];
	}

	@Override
	public void initialEnPassantePos(int enpassantePos) {
		this.enPassanteMask=enpassantePos;
	}

	@Override
	public void initialTurn(int color) {
		colorAtTurn = color;
	}
  	
  	@Override
	public void initialEval() {
		ContextLevel context = this.contextLevels[level];
		if(untouched!=0 && ((untouched & MASK_NOT_ALL_ROOKS) ==0 || (untouched & MASK_ALL_ROOKS)==0)) {
			untouched=0L;
		}
		_occ = allOfOneColor[COLOR_WHITE]| allOfOneColor[COLOR_BLACK];
		_notOcc = ~(_occ);
	
		for (int i = 0; i < 2; i++) {
			long pawn = pawns[i];
			long own = this.allOfOneColor[i]&~pawn;
			int retVal = Long.bitCount(own);
			for(int j=0;j<retVal;j++) {
				int pos = Long.numberOfTrailingZeros(own);
				own &= own- 1;			
				int typeColor = fields[pos];
				// if there is no combination remaining set to 0
				updateNonPawnPseudoMoves(typeColor, pos,true);
			}
			retVal = Long.bitCount(pawn);
			for(int j=0;j<retVal;j++) {
				int pos = Long.numberOfTrailingZeros(pawn);
				pawn &= pawn- 1;			
				int file = getFileForPos(pos);
				long mask = 0L;
				if(file!=_A) {
					int cb = i==COLOR_WHITE?pos+7:pos-9;
					//context.setBitTCallBacks(cb,pos);
					tCallBacks[cb]|=SHIFT[pos];
					mask|=SHIFT[cb];
				}
				if(file!=_H) {
					int cb = i==COLOR_WHITE?pos+9:pos-7;
					//context.setBitTCallBacks(cb,pos);
					tCallBacks[cb]|=SHIFT[pos];
					mask|=SHIFT[cb];
				}
				//context.setCallBacks(pos,mask);
				callBacks[pos]=mask;
			}
			updatePawnPseudoMoves(i);
		}
		checkRochade(context,null); 

		//checkMoves() ;
	}
	
	public int getMoveCount() {
		this.contextLevels[level].resetIterator();
		return moveCount[colorAtTurn];
	}
	
	public Move getMove(int index) {
		if(index==0) {
			// once for all moves of this level		
			this.calcNewMoves(this.colorAtTurn);
			this.contextLevels[level+1].snapshot(); 
		}
		return this.contextLevels[level].getMove(index);
	}

	
	@Override
	public void unSetMove(int move) {
		ContextLevel context = this.contextLevels[level];
		context.revertToSnapshot();
		level--;
		
		this.colorAtTurn = OTHER_COLOR[colorAtTurn];
		
	}

	

	@Override
	public void setMove(int index) {

		Move move = getMove(index);
		level++;
		ContextLevel context = this.contextLevels[level];
//		O._push("* ");
//		O.UT("Try Move:"+move.toString() +" in level "+level);
		setMove(move,context);
//		String board = this.toString();
//		O.UT("Successfully Did first part of move:)"+move+board);
		
		
		
		int color = this.colorAtTurn;
		int otherColor = OTHER_COLOR[color];
		this.updatePawnPseudoMoves(color);
		this.updatePawnPseudoMoves(otherColor);
		if(untouched!=0) {
			checkRochade(context,move); 
		}			
		checkLegalMoves();
		this.colorAtTurn = OTHER_COLOR[colorAtTurn];
	}
	
	
	int movecount =0;
	private void setMove(Move move,ContextLevel context ) {
		
		/*
		
		if(movecount==2152) {
			//System.out.println("WTF");
		}
		
		//System.out.println("Move("+move+"):"+(movecount++));
		
		//checkMoves() ;
		*/
		////System.out.println("Move("+move+"):"+(movecount++));
		
		
		
		//@todo use masks for move
		int oldPos = move.getOldPos();
		context.trigger(oldPos);
		int newPos = move.getNewPos();
		context.trigger(newPos);
		long oldPosMask = SHIFT[oldPos];
		long newPosMask = SHIFT[newPos];
		long moveMask=oldPosMask|newPosMask;
		int color = this.colorAtTurn;
		int otherColor = OTHER_COLOR[color];
		int pieceType = move.getPieceType();
		
		long cbsNewPosEmptyBefore=0;
		long cbsNewPosReplace=0;		
		long cbsOldPos=0;
		long cbsEnpPawnRemoved=0; 				
		
/*E*/	int otherTypeColor = fields[newPos];
		if(otherTypeColor!=-1) {
			//alter moves(ok), moveCount(ok), allOfOneColor(ok), fields(later), callbacks(ok), tcallbacks(ok) and if neccessary pawns!(ok)
/*!!!*/		removePseudoMoves(context,otherTypeColor,newPos);
			allOfOneColor[otherColor]^=newPosMask;
			if(otherTypeColor>>7==PIECE_TYPE_PAWN) {
				pawns[otherColor]^=newPosMask;
			}
			cbsNewPosReplace= this.tCallBacks[newPos];
		}else {
			cbsNewPosEmptyBefore = this.tCallBacks[newPos];
		}
		
		
		
		//int typeColor = context.getAndSetFields(oldPos,-1);
		int typeColor = fields[oldPos];
		fields[oldPos]=-1;

/*!!!*/	removePseudoMoves(context, typeColor,oldPos);

		if(move.isPromotion()) {
			typeColor = move.getPromotePieceType();
			pieceType = typeColor>>7;
			pawns[color]&=~oldPosMask;
		}

		//alter moves(ok), moveCount(ok), allOfOneColor(ok), fields(later), callbacks(ok), tcallbacks(ok) and if neccessary pawns!(ok)
		//alter moves(ok), moveCount(ok), allOfOneColor(ok), fields(later), callbacks(ok), tcallbacks(ok) and if neccessary pawns!(ok)
		
		///	context.setFields(newPos,typeColor);
		 fields[newPos]=typeColor;
		// update occupancy
		/*u*/	this.allOfOneColor[color]^=moveMask;
		// update moves
		
		
		cbsOldPos = this.tCallBacks[oldPos];
		if(pieceType==PIECE_TYPE_KING) {
			this.kings[color]=newPosMask;
		}
		if(pieceType==PIECE_TYPE_PAWN) {
			pawns[color]^=moveMask;
		}
		
		if(enPassanteMask!=0L)	{
			if(move.getEnPassanteSquare()==Long.numberOfTrailingZeros(enPassanteMask)) {
				int enpPawnPos = move.getEnPassantePawnPos();
				context.trigger(enpPawnPos);
				cbsEnpPawnRemoved = this.tCallBacks[enpPawnPos]; 				
				//int enpTypeColor = context.getAndSetFields(enpPawnPos,-1);
				int enpTypeColor = fields[enpPawnPos];
				fields[enpPawnPos]=	-1;
/*u*/			this.allOfOneColor[otherColor]&=~SHIFT[enpPawnPos];// might be empty anyways
				pawns[otherColor]&=~SHIFT[enpPawnPos];
				removePseudoMoves(context,enpTypeColor,enpPawnPos);
			}
			this.enPassanteMask=0;
		}

			
		if(move.isTwoSquarePush()) {
			this.enPassanteMask=SHIFT[move.getEnPassanteSquare()];//@TBD always set (geht mit stack)
		}

		
		_occ = allOfOneColor[COLOR_WHITE]|allOfOneColor[COLOR_BLACK];
		_notOcc = ~_occ;
				
		if(pieceType!=PIECE_TYPE_PAWN) {
			this.updateNonPawnPseudoMoves(typeColor, newPos,true);
		}else {
			//for Pawns just add the two attackers
			int[] attacks = move.getPawnNewAttacks();
			for(int i=0;i<attacks.length;i++) {
				//context.setBitTCallBacks(attacks[i],newPos);
				tCallBacks[attacks[i]]|=SHIFT[newPos];
				
			}
			///context.setCallBacks(newPos,move.getPawnNewAttackMask());
			callBacks[newPos]= move.getPawnNewAttackMask();
		}
		
		if(untouched!=0) {
			// if there is no combination remaining set to 0
			if(move.isRochadeDisabler()) { 
				if(move.isRochade()) {
					this.setMove(move.getRookMove(),context);
				}
				// update fields
				long not_moveMask =~moveMask;
				this.untouched&=not_moveMask;
			}
		}
		long cbs = cbsNewPosEmptyBefore|cbsNewPosReplace |cbsOldPos|cbsEnpPawnRemoved;
		cbs &= (_occ)&~(moveMask)&~(pawns[COLOR_WHITE]|pawns[COLOR_BLACK]);//@todo occ var
		long bits = cbs;
		int retVal = Long.bitCount(bits);
		for(int i=0;i<retVal;i++) {
			int cbPos = Long.numberOfTrailingZeros(bits);
			bits &= bits - 1;				
			int cbTypeColor = fields[cbPos];
			this.updateNonPawnPseudoMoves(cbTypeColor, cbPos,false);
		}
	
	}
	
	private void removePseudoMoves(ContextLevel context,int typeColor, int pos) {
		int color = typeColor >> 6 & 1;
		//remove moves
		//this.moveCount[color]-=Long.bitCount(context.getAndSetMoveMasks(pos,0L));
		this.moveCount[color]-=Long.bitCount(moveMasks[pos]);
		moveMasks[pos]=0L;
		
		
		//remove attacks
		//long oldCBs = context.getAndSetCallBacks(pos,0L);
		long oldCBs = callBacks[pos];
		callBacks[pos]= 0L;
		
		int retVal = Long.bitCount(oldCBs);
		for(int i=0;i<retVal;i++) {
			//context.toggleBitTCallBacks(Long.numberOfTrailingZeros(oldCBs),pos);
			tCallBacks[Long.numberOfTrailingZeros(oldCBs)]^=SHIFT[pos];
			oldCBs &= oldCBs - 1;			
		}
	}

	
	
	private void updateNonPawnPseudoMoves(int typeColor, int pos,boolean isAdded) {
		ContextLevel context = contextLevels[level];
		int color = typeColor >> 6 & 1;
		int type = typeColor >>7;
		
		
		long callbacks = 0L;
		long moves = 0L;
		long own = this.allOfOneColor[color];// @todo WTF too expensive
		boolean needsCBUpdate=true;
		
		if(type<=PIECE_TYPE_KNIGHT) {
			callbacks = lookUp.getMoveMask(pos + typeColor);
			moves = callbacks & ~own;
			needsCBUpdate=isAdded;
		}else {
			switch (type) {
			case PIECE_TYPE_BISHOP:{
				callbacks = mnf.getBishopAttacks(pos, _occ);
				moves = callbacks & ~own;
				break;
			}
			case PIECE_TYPE_ROOK:{
				callbacks = mnf.getRookAttacks(pos, _occ);
				moves = callbacks & ~own;
			
				break;
			}
			case PIECE_TYPE_QUEEN: {
				callbacks = mnf.getBishopAttacks(pos, _occ);
				callbacks|= mnf.getRookAttacks(pos, _occ);
				moves = (callbacks) & ~own;
				break;
			}
			}
		}
		if(needsCBUpdate) {
			long toggleCallbacks = this.callBacks[pos] ^ callbacks;
			int retVal = Long.bitCount(toggleCallbacks);
			if(retVal>0) {
				context.trigger(pos);
				for(int i=0;i<retVal;i++) {
					tCallBacks[Long.numberOfTrailingZeros(toggleCallbacks)]^=SHIFT[pos];
					toggleCallbacks &= toggleCallbacks - 1;								
				}
				//context.setCallBacks(pos, callbacks);
				callBacks[pos]= callbacks;
				
			}
		}
		
		long oldMoves = this.moveMasks[pos] ;
		if(oldMoves!=moves) {
			context.trigger(pos);
			//context.setMoveMasks(pos, moves);
			moveMasks[pos] = moves;
			int delta = Long.bitCount(moves) - Long.bitCount(oldMoves);
			if(delta!=0) {
				this.moveCount[color]+=delta;
			}
		}
	}
	private void updatePawnPseudoMoves(int color) {
		//System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1counter"+counter++);
		ContextLevel context = this.contextLevels[level];
		ContextLevel oldContext = this.contextLevels[level-1];
		int otherColor = OTHER_COLOR[color];
		long other = allOfOneColor[otherColor];
		long ownPawns = pawns[color];
		
		long mLeft,mRight,mOneUp,mTwoUp ,delta;
		if(color==COLOR_WHITE) {
			//out(ownPawns);

			other|=(this.enPassanteMask & MASK_6_RANK);
			//out(other);
			mLeft= (ownPawns <<DIR_UP_LEFT) & MASK_NOT_H_FILE&other;
			//out(mLeft);
			mRight=(ownPawns <<DIR_UP_RIGHT) & MASK_NOT_A_FILE&other;
			//out(mRight);
			mOneUp = (ownPawns << DIR_UP)& _notOcc ;
			mTwoUp = ((mOneUp  & _notOcc & MASK_3_RANK) << DIR_UP)& _notOcc;
			
			//out(mOneUp);
			//out(mTwoUp);
		
			delta = (mLeft^oldContext._mLeft[color])>>DIR_UP_LEFT;
			//out(oldContext._mLeft[color]);
			//out(delta);
			delta |= (mRight^oldContext._mRight[color])>>DIR_UP_RIGHT;
			//out(oldContext._mRight[color]);
			//out(delta);
			delta |= (mOneUp^oldContext._mOneUp[color])>>DIR_UP;
			//out(delta);
			delta |= (mTwoUp^oldContext._mTwoUp[color])>>DIR_2_UP;

			// only pawns that existed in the first place
			delta&=ownPawns;
			
			//out(delta);
			
			int retVal = Long.bitCount(delta);
			int moveCountDelta =0;
			if(retVal>0) {
				for(int i=0;i<retVal;i++) {
					int pos = Long.numberOfTrailingZeros(delta); 
					context.trigger(pos);
					context.trigger(pos);
					delta &= delta - 1;			
					long mask = this.lookUp.getMoveMask(PIECE_TYPE_X_PAWN[color] +pos);
					//out(mask);
					long pOneMask = mask&mOneUp & MASK_ONE_UP_AND_DOWN[pos];
					//out(pOneMask);
					long pTwoMask =mask&mTwoUp & ~MASK_ONE_UP_AND_DOWN[pos];
					//out(pTwoMask);
					long aMask=mask&MASK_NOT_X_FILE_FOR_POS[pos];
					//out(aMask);
					mask =(pOneMask|pTwoMask)&MASK_X_FILE_FOR_POS[pos]|aMask&mLeft|aMask&mRight;
					long last =mask& MASK_8_RANK;
					if((last)!=0L)  {
						last|= last>>DIR_UP;
						last|= last>>DIR_UP;
						mask|= last>>DIR_UP;
					}
					//long oldMask = context.getAndSetMoveMasks(pos,mask);
					long oldMask = moveMasks[pos];
					moveMasks[pos]=mask;
					
					////out(oldMask);
					moveCountDelta+=Long.bitCount(mask)- Long.bitCount(oldMask);
				}
			}
			context._mLeft[color] = mLeft;
			context._mRight[color]= mRight;
			context._mOneUp[color]= mOneUp;
			context._mTwoUp[color] = mTwoUp;
			//= Long.bitCount(mOneUp|mTwoUp)+Long.bitCount(mLeft)+Long.bitCount(mRight);
			this.moveCount[color]+=moveCountDelta;

		}else {
			
			
			//out(ownPawns);
			other|=(this.enPassanteMask & MASK_3_RANK);
			//out(other);
			mLeft= (ownPawns >>DIR_UP_LEFT) & MASK_NOT_A_FILE&other;
			//out(mLeft);
			mRight=(ownPawns >>DIR_UP_RIGHT) & MASK_NOT_H_FILE&other;
			//out(mRight);
			mOneUp = (ownPawns >> DIR_UP) & _notOcc ;
			//out(mOneUp);
			mTwoUp = (((mOneUp & MASK_6_RANK) >> DIR_UP)& _notOcc ) ;
			//out(mTwoUp);
			delta = (mLeft^oldContext._mLeft[color])<<DIR_UP_LEFT;
			//out(oldContext._mLeft[color]);
			//out(delta);
			delta |= (mRight^oldContext._mRight[color])<<DIR_UP_RIGHT;
			//out(oldContext._mRight[color]);
			//out(delta);
			delta |= (mOneUp^oldContext._mOneUp[color])<<DIR_UP;
			//out(delta);
			delta |= (mTwoUp^oldContext._mTwoUp[color])<<DIR_2_UP;
		
			// only pawns that existed in the first place
			delta&=ownPawns;
			
			//out(delta);
			
			int retVal = Long.bitCount(delta);
			int moveCountDelta =0;
			if(retVal>0) {
				for(int i=0;i<retVal;i++) {
					int pos = Long.numberOfTrailingZeros(delta); 
					context.trigger(pos);
					
					delta &= delta - 1;			
					long mask = this.lookUp.getMoveMask(PIECE_TYPE_X_PAWN[color] +pos);
					//out(mask);
					long pOneMask =mask&mOneUp &MASK_ONE_UP_AND_DOWN[pos];
					long pTwoMask =mask&mTwoUp &~MASK_ONE_UP_AND_DOWN[pos];
					long aMask=mask&MASK_NOT_X_FILE_FOR_POS[pos];
					//out(aMask);
					//out(mLeft);
					//out(mRight);
					mask =((pOneMask|pTwoMask)&MASK_X_FILE_FOR_POS[pos])|aMask&mLeft|aMask&mRight;
					long last =mask& MASK_1_RANK;
					if((last)!=0L)  {
						last|= last<<DIR_UP;
						last|= last<<DIR_UP;
						mask|= last<<DIR_UP;
					}
					//out(mask);
					//long oldMask = context.getAndSetMoveMasks(pos,mask);
					long oldMask = moveMasks[pos];
					moveMasks[pos]=mask;
					
					moveCountDelta+=Long.bitCount(mask)- Long.bitCount(oldMask);
					//out(oldMask);
					//out(mask);
				}
				
			}
			context._mLeft[color]= mLeft;
			context._mRight[color]=mRight;
			context._mOneUp[color]=mOneUp;
			context._mTwoUp[color]=mTwoUp;
			//= Long.bitCount(mOneUp|mTwoUp)+Long.bitCount(mLeft)+Long.bitCount(mRight);
			this.moveCount[color]+=moveCountDelta;
		}
	}


	private void checkRochade(ContextLevel context, Move move) {
		
		long oldE1Mask=this.moveMasks[_E1];
		long oldCastleMovesKQ=oldE1Mask&MASK_CASTLE_KING_KQkq;
		int oldCastleMovesKQCount= Long.bitCount(oldCastleMovesKQ);
		
		////out(this.allOfOneColor[0]);
		long oldE8Mask=this.moveMasks[_E8];
		long oldCastleMoveskq=oldE8Mask&MASK_CASTLE_KING_KQkq;;
		int oldCastleMoveskqCount= Long.bitCount(oldCastleMoveskq);
		
		
		long newCastleMovesKQ=0;
		long newCastleMoveskq=0;
		
		//@TODO WTF: Combine E1 and combine with nonPawn stuff
		
		//remove rochade protagonists in occ if touched
		long ntchdOcc= _occ & MASK_NOT_ALL_ROOKS_KINGS |untouched;
		////out(ntchdOcc);
		////out(context.occ );
		////out(context.ntchd);
		if((untouched & MASK_E1)!=0L) {
			context.trigger(_E1);
			
			//ntchdOcc and MASK_E1_H1 should be identical in the range of MASK_CASTLE_ALL_K
			if (((ntchdOcc^MASK_E1_H1)& MASK_CASTLE_ALL_K)==0L) {
				newCastleMovesKQ|= MASK_CASTLE_KING_K;
			}
			if (((ntchdOcc^MASK_E1_A1)& MASK_CASTLE_ALL_Q)==0L) {
				newCastleMovesKQ|= MASK_CASTLE_KING_Q;
			}
			long mask = (oldE1Mask&MASK_NOT_CASTLE_KING_KQkq)|newCastleMovesKQ;
			////out(mask);
			//this.conistency();
			////out(this.moveMasks[_E1]);
			////out(mask);	
			
			//context.setMoveMasks(_E1,mask);//we sometimes need to repair this w. king was updated
			moveMasks[_E1]=mask;
				////out(newCastleMovesKQ);
				////out(this.moveMasks[_E1]);
			if(oldCastleMovesKQ !=newCastleMovesKQ) {
				int delta = Long.bitCount(newCastleMovesKQ)-oldCastleMovesKQCount;
				if(delta!=0) {
					this.moveCount[COLOR_WHITE]+=delta;
				}
			}
			//this.conistency();
		}
		
		
		if((untouched & MASK_E8)!=0L) {
			context.trigger(_E8);

			if (((ntchdOcc^MASK_E8_H8)& MASK_CASTLE_ALL_k)==0L) {
				newCastleMoveskq|= MASK_CASTLE_KING_k;
				////out(newCastleMoveskq);
				////out(ntchdOcc^MASK_E8_H8);
				
			}
			if (((ntchdOcc^MASK_E8_A8)& MASK_CASTLE_ALL_q)==0L) {
				newCastleMoveskq|= MASK_CASTLE_KING_q;
			}
			long mask = (oldE8Mask&MASK_NOT_CASTLE_KING_KQkq)|newCastleMoveskq;
			//context.setMoveMasks(_E8,mask);//we sometimes need to repair this w. king was updated
			moveMasks[_E8]=mask;
			
			if(oldCastleMoveskq !=newCastleMoveskq) {
				int delta = Long.bitCount(newCastleMoveskq)-oldCastleMoveskqCount;
				if(delta!=0) {
					this.moveCount[COLOR_BLACK]+=delta;
				}
			}
		}
		if(((untouched & MASK_NOT_ALL_ROOKS) ==0 || (untouched & MASK_ALL_ROOKS)==0)) {
			untouched = 0L;
			return;
		}
		
	}
	
	
	@Override
	public void checkLegalMoves (){
	/*	
		int color = getColorAtTurn();
		int otherColor = OTHER_COLOR[color];
		long kingMask = this.kings[color];
		int kingPos = Long.numberOfTrailingZeros(kingMask);
		long enpassantePos = enPassantePos;
		long own = this.allOfOneColor[color];// @todo WTF too expensive
		long other = this.allOfOneColor[otherColor];

		long kingCbs = this.tCallBacks[kingPos]&other;
		int kingCBCount = Long.bitCount(kingCbs);
		int kingAttacks = kingCBCount -this.correctors[otherColor].getBitAsInt(kingPos);
		
		int kingAttackerPos = -1;
		int attackerTypeColor = -1;
		
		long checkMask = ~0L;
		if(kingAttacks==1) {
			for(int i=0;i<kingCBCount;i++) {
				kingAttackerPos = Long.numberOfTrailingZeros(kingCbs);
				if(this.moveMasks[kingAttackerPos].getBit(kingPos)) {
					attackerTypeColor = this.fields[kingAttackerPos];
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
  	public void calcNewMoves(int color) {
  		ContextLevel oldContext = this.contextLevels[level-2];
  		ContextLevel context = this.contextLevels[level];
  		context.init();
  		
  		long bits = this.allOfOneColor[color];
  		int retVal = Long.bitCount(bits);
  		for(int i=0;i<retVal;i++) {
  			int pos = Long.numberOfTrailingZeros(bits);
			bits &= bits - 1;
			long moveMask = this.moveMasks[pos];
			if(moveMask==0) {
				continue;
			}
			int typeColor = fields[pos];
			if(context.checkForReuseMoves(pos, moveMask, typeColor)) {
				continue;
			}
			Move[] moves = oldContext.getMoves(pos, moveMask, typeColor);
			if(moves==null) {
				moves =lookUp.getMoveMap(pos+typeColor);
				context.extractFromRawMoves(pos, moveMask, typeColor, moves);
			}else {
				context.addMoves(pos, moveMask, typeColor, moves);
			}
		}
  		context.setLimit(moveCount[colorAtTurn]);
  	}


	

	
	@Override
	public int getColorAtTurn() {
		return this.colorAtTurn;
	}

	@Override
	public void checkGameState(int colorAtTurn) {
		// TODO Auto-generated method stub
	}

	
	public String toString() {
		String str = analyzer.toString();
		if(analyzer.checkAlarm()) {
			System.out.println(str);
			throw new RuntimeException("Alarm");
		}
		return analyzer.toString();
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


	@Override
	public int[] getAttacks(int color) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
