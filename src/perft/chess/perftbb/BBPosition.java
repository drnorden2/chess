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
	private int totalCount =0;
	
	private int level =4;
	private boolean isNotLastRound=true;
	
	public final int depth;
	private int colorAtTurn = COLOR_WHITE;

	public final int[] fields = new int[64];
	public final long[] callBacks = new long[64];
	public final long[] moveMasks = new long[64];
	public long[] tCallBacks = new long[64];
	
	public final long[] allOfOneColor = new long[2];
	public final long[] kings = new long[2];
	public final long[] knights = new long[2];
	public final long[] pawns = new long[2];
	public final int moveCount [] = new int[2];
	public long untouched=0;
	public long enPassanteMask=0;

	public BBAnalyzer analyzer = new BBAnalyzer(this);
	public BBMoveManager moveManager = new BBMoveManager();
	private final MagicNumberFinder mnf = new MagicNumberFinder();
	final ContextLevel[] contextLevels;

	//temps
	private long _notOcc; 
	 long _occ;
	private long[] _pinMasks=new long[64];
	private boolean _kingOnly;
	private final int _enpBeaterPos[]=new int[2];
	private int _enpPos=0;
	private long _enpPawnMask=0;
	private int _moveDelta;
	private boolean _noCalc = true;
	private boolean _isLastRound=true;

	
	
	public BBPosition(int depth) {
		this.depth = depth;
		System.out.println("Depth:"+depth);
		contextLevels = new ContextLevel[depth+4+1];//WTF (-1)
		for(int i=0;i<this.contextLevels.length;i++) {
			this.contextLevels[i]=new ContextLevel(this,i, i==depth-1);
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
		if(type==PIECE_TYPE_KNIGHT) {
			this.knights[color]|=SHIFT[pos];
		}
		
		if(type==PIECE_TYPE_PAWN) {
			this.pawns[color]|=SHIFT[pos];
		}
		for(int i=0;i<64;i++) {
			this._pinMasks[i]=FULL_MASK;
		}
		
	}

	@Override
	public void initialUntouched(int rank, int file) {
		untouched|=SHIFT[getPosForFileRank(file, rank)];
	}

	@Override
	public void initialEnPassantePos(int enpassantePos) {
		this.enPassanteMask=SHIFT[enpassantePos];
	}

	@Override
	public void initialTurn(int color) {
		colorAtTurn = color;
	}
  	
  	@Override
	public void initialEval() {
  		this.colorAtTurn=OTHER_COLOR[this.colorAtTurn];
  		int color = this.colorAtTurn;
  		ContextLevel context = this.contextLevels[level];
		if(untouched!=EMPTY_MASK && ((untouched & MASK_NOT_ALL_ROOKS) ==EMPTY_MASK || (untouched & MASK_ALL_ROOKS)==EMPTY_MASK)) {
			untouched=EMPTY_MASK;
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
				long mask = EMPTY_MASK;
				if(file!=_A) {
					int cb = (i==COLOR_WHITE?pos+7:pos-9);
					tCallBacks[cb]|=SHIFT[pos];
					mask|=SHIFT[cb];
				}
				if(file!=_H) {
					int cb = i==COLOR_WHITE?pos+9:pos-7;
					tCallBacks[cb]|=SHIFT[pos];
					mask|=SHIFT[cb];
				}
				callBacks[pos]=mask;
			}
			if(color!=i) {
				updatePawnPseudoMoves(i);
			}
		}
		
		checkLegalMoves();

		checkRochade(context,null,colorAtTurn==COLOR_WHITE); 
		this.colorAtTurn=OTHER_COLOR[this.colorAtTurn];
		_noCalc = true;
	}
	
	public int getMoveCount() {	
		if(this._kingOnly) {
			return this._moveDelta;
		}else {
			return this.moveCount[this.colorAtTurn]-this._moveDelta;
		}		
	}
	
	public Move getMove(int index) {
		if(index==0) {
			this.calcNewMoves(this.colorAtTurn);
			this.contextLevels[level+1].snapshot(); 
			// once for all moves of this level		
			this.contextLevels[level].resetIterator();
				
		}
		return this.contextLevels[level].getMove(index);
	}

	
	@Override
	public void unSetMove(int move) {
		ContextLevel context = this.contextLevels[level];
		context.revertToSnapshot();
		level--;
		this.colorAtTurn = OTHER_COLOR[colorAtTurn];
		if(_noCalc) {// eighter last level or no continuation (checkmat or pat
			for(int i=0;i<64;i++) {
				this._pinMasks[i]=FULL_MASK;
			}	
		}
	}

	
	@Override
	public void setMove(int index) {
		totalCount++;
		Move move = getMove(index);
		level++;
		this.isNotLastRound =  true;//level!=depth+1;
		this._isLastRound = (level>=depth+3);
		ContextLevel context = this.contextLevels[level];

		setMove(move,context);
		
		int color = this.colorAtTurn;
		int otherColor = OTHER_COLOR[color];
		if(!_isLastRound) {
			this.updatePawnPseudoMoves(color);
		}
		this.updatePawnPseudoMoves(otherColor);
		
		this.checkLegalMoves();

		if(untouched!=EMPTY_MASK) {
			checkRochade(context,move,color==COLOR_WHITE); 
		}			
		
		this.colorAtTurn = OTHER_COLOR[colorAtTurn];
		_noCalc=true;
		
	}
	
	
	int movecount =0;
	private void setMove(Move move,ContextLevel context ) {	
		/*
		if(movecount==2152) {
			//System.out.println("WTF");
		}
		
		System.out.println("Move("+move+"):"+(movecount++));
		*/
	
		//@todo use masks for move
		
		int oldPos = move.getOldPos();
		int newPos = move.getNewPos();
		long oldPosMask = SHIFT[oldPos];
		long newPosMask = SHIFT[newPos];
		long moveMask=oldPosMask|newPosMask;
		int color = this.colorAtTurn;
		int otherColor = OTHER_COLOR[color];
		int pieceType = move.getPieceType();
		long cbs; 
		long emptyBefore = EMPTY_MASK;
		context.trigger(newPos);
		context.trigger(oldPos);
		
		
/*E*/	int otherTypeColor = fields[newPos];
		cbs= this.tCallBacks[newPos];
		if(otherTypeColor!=-1) {
			
			//alter moves(ok), moveCount(ok), allOfOneColor(ok), fields(later), callbacks(ok), tcallbacks(ok) and if neccessary pawns!(ok)
/*!!!*/		removePseudoMoves(context,otherTypeColor,newPos);
			allOfOneColor[otherColor]^=newPosMask;
			if(otherTypeColor>>7==PIECE_TYPE_PAWN) {
				this.pawns[otherColor]^=newPosMask;
			}
			if(otherTypeColor>>7==PIECE_TYPE_KNIGHT) {
				this.knights[otherColor]^=newPosMask;
			}

			//cbsNewPosReplace= this.tCallBacks[newPos];
		}else {
			//cbsNewPosEmptyBefore = this.tCallBacks[newPos];
			emptyBefore = cbs&this.allOfOneColor[otherColor];
		}
		
		
		
		//int typeColor = context.getAndSetFields(oldPos,-1);
		int typeColor = fields[oldPos];
		fields[oldPos]=-1;

/*!!!*/	removePseudoMoves(context, typeColor,oldPos);

		if(move.isPromotion()) {
			typeColor = move.getPromotePieceType();
			pieceType = typeColor>>7;
			pawns[color]&=~oldPosMask;
			if(pieceType ==PIECE_TYPE_KNIGHT) {
				this.knights[color]|=newPosMask;
			}
		}else {
			if(pieceType==PIECE_TYPE_KNIGHT) {
				this.knights[color]^=moveMask;
			}
			
			if(pieceType==PIECE_TYPE_PAWN) {
				this.pawns[color]^=moveMask;
			}
		}

		fields[newPos]=typeColor;
		this.allOfOneColor[color]^=moveMask;
		
		
		cbs |= this.tCallBacks[oldPos];
		if(pieceType==PIECE_TYPE_KING) {
			this.kings[color]=newPosMask;
		}
		
		
		if(enPassanteMask!=EMPTY_MASK)	{
			if(move.getEnPassanteSquare()==Long.numberOfTrailingZeros(enPassanteMask)) {
				int enpPawnPos = move.getEnPassantePawnPos();
				context.trigger(enpPawnPos);
				cbs|= this.tCallBacks[enpPawnPos]; 				
				int enpTypeColor = fields[enpPawnPos];
				fields[enpPawnPos]=	-1;
/*u*/			this.allOfOneColor[otherColor]&=~SHIFT[enpPawnPos];// might be empty anyways
				pawns[otherColor]&=~SHIFT[enpPawnPos];
				removePseudoMoves(context,enpTypeColor,enpPawnPos);
			}
			this.enPassanteMask=EMPTY_MASK;
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
				tCallBacks[attacks[i]]|=SHIFT[newPos];
			}
			callBacks[newPos]= move.getPawnNewAttackMask();
		}
		
		if(untouched!=EMPTY_MASK) {
			if(move.isRochadeDisabler()) { 
				if(move.isRochade()) {
					this.setMove(move.getRookMove(),context);
				}
				// update fields
				long not_moveMask =~moveMask;
				this.untouched&=not_moveMask;
			}
		}
		cbs &= (_occ)&~(moveMask)&~(pawns[COLOR_WHITE]|pawns[COLOR_BLACK]);//@todo occ var
		int retVal = Long.bitCount(cbs);
		for(int i=0;i<retVal;i++) {			
			int cbPos = Long.numberOfTrailingZeros(cbs);
			cbs &= cbs- 1;
			int tC=fields[cbPos];
			if(tC>PIECE_TYPE_BLACK_KNIGHT || (emptyBefore&SHIFT[cbPos])==0) { //@todo WTF check performance
				this.updateNonPawnPseudoMoves(tC, cbPos,false);
			}
		}
	}
	
	private void removePseudoMoves(ContextLevel context,int typeColor, int pos) {
		int color = typeColor >> 6 & 1;
		boolean ownPiece = color==colorAtTurn ;
		//remove moves
		if(!ownPiece || isNotLastRound) {
			this.moveCount[color]-=Long.bitCount(moveMasks[pos]);
			moveMasks[pos]=EMPTY_MASK;
		}
		if(ownPiece|| isNotLastRound) {
			//remove attacks
			long oldCBs = callBacks[pos];
			callBacks[pos]= EMPTY_MASK;
			
			int retVal = Long.bitCount(oldCBs);
			for(int i=0;i<retVal;i++) {
				tCallBacks[Long.numberOfTrailingZeros(oldCBs)]^=SHIFT[pos];
				oldCBs &= oldCBs - 1;			
			}
		}
	}

	
	
	private void updateNonPawnPseudoMoves(int typeColor, int pos,boolean isAdded) {
		ContextLevel context = contextLevels[level];
		int color = typeColor >> 6 & 1;
		int type = typeColor >>7;
		
		
		long callbacks = EMPTY_MASK;
		long moves = EMPTY_MASK;
		long own = this.allOfOneColor[color];// @todo WTF too expensive
		boolean needsCBUpdate=true;
		
		if(type<=PIECE_TYPE_KNIGHT) {
			callbacks = BBMoveManager.moveMasks[pos + typeColor];
			needsCBUpdate=isAdded;
		}else {
			switch (type) {
			case PIECE_TYPE_BISHOP:{
				callbacks = mnf.getBishopAttacks(pos, _occ);
				break;
			}
			case PIECE_TYPE_ROOK:{
				callbacks = mnf.getRookAttacks(pos, _occ);
			
				break;
			}
			case PIECE_TYPE_QUEEN: {
				callbacks = mnf.getBishopAttacks(pos, _occ);
				callbacks|= mnf.getRookAttacks(pos, _occ);
				break;
			}
			}
		}
		
		if(colorAtTurn==color || isNotLastRound) {
			if(needsCBUpdate) {
				long toggleCallbacks = this.callBacks[pos] ^ callbacks;
				int retVal = Long.bitCount(toggleCallbacks);
				if(retVal>0) {
					context.trigger(pos);
					for(int i=0;i<retVal;i++) {
						tCallBacks[Long.numberOfTrailingZeros(toggleCallbacks)]^=SHIFT[pos];
						toggleCallbacks &= toggleCallbacks - 1;								
					}
					callBacks[pos]= callbacks;
					
				}
			}
		}
		if(colorAtTurn!=color || isNotLastRound) {
			moves = (callbacks) & ~own;
			long oldMoves = this.moveMasks[pos] ;
			if(oldMoves!=moves) {
				context.trigger(pos);
				moveMasks[pos] = moves;
				int delta = Long.bitCount(moves) - Long.bitCount(oldMoves);
				if(delta!=0) {
					this.moveCount[color]+=delta;
				}
			}
		}
	}
	private void updatePawnPseudoMoves(int color) {
		ContextLevel context = this.contextLevels[level];
		ContextLevel oldContext = this.contextLevels[level-1];
		int otherColor = OTHER_COLOR[color];
		long other = allOfOneColor[otherColor];
		long ownPawns = pawns[color];
		
		long mLeft,mRight,mOneUp,mTwoUp ,delta;
		_enpBeaterPos[0]=-1;
		if(this.enPassanteMask!=EMPTY_MASK) {
			_enpBeaterPos[1]=-1;
			_enpPos = Long.numberOfTrailingZeros(this.enPassanteMask);
			_enpPawnMask = ENP_PAWN_MASK_FOR_X[_enpPos];
		}else {
			_enpPawnMask =EMPTY_MASK;
		}
		int enpBeaterCount =0;
		
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
					delta &= delta - 1;			
					long mask = BBMoveManager.moveMasks[PIECE_TYPE_X_PAWN[color] +pos];
					
					//out(mask);
					long pOneMask = mask&mOneUp & MASK_ONE_UP_AND_DOWN[pos];
					//out(pOneMask);
					long pTwoMask =mask&mTwoUp & ~MASK_ONE_UP_AND_DOWN[pos];
					//out(pTwoMask);
					long aMask=mask&MASK_NOT_X_FILE_FOR_POS[pos];
					//out(aMask);
					mask =(pOneMask|pTwoMask)&MASK_X_FILE_FOR_POS[pos]|aMask&mLeft|aMask&mRight;
					if((mask&this.enPassanteMask)!=EMPTY_MASK) {
						_enpBeaterPos[enpBeaterCount++]=pos;
					}
					long last =mask& MASK_8_RANK;
					
					if((last)!=EMPTY_MASK)  {
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
					long mask = BBMoveManager.moveMasks[PIECE_TYPE_X_PAWN[color] +pos];
	
					//out(mask);
					long pOneMask =mask&mOneUp &MASK_ONE_UP_AND_DOWN[pos];
					long pTwoMask =mask&mTwoUp &~MASK_ONE_UP_AND_DOWN[pos];
					long aMask=mask&MASK_NOT_X_FILE_FOR_POS[pos];
					//out(aMask);
					//out(mLeft);
					//out(mRight);
					mask =((pOneMask|pTwoMask)&MASK_X_FILE_FOR_POS[pos])|aMask&mLeft|aMask&mRight;
					if((mask&this.enPassanteMask)!=EMPTY_MASK) {
						_enpBeaterPos[enpBeaterCount++]=pos;
					}
					
					long last =mask& MASK_1_RANK;
					if((last)!=EMPTY_MASK)  {
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


	private void checkRochade(ContextLevel context, Move move, boolean isWhite) {
		//remove rochade protagonists in occ if touched
		long ntchdOcc= _occ & MASK_NOT_ALL_ROOKS_KINGS |untouched;
		
		if(!_isLastRound||!isWhite) {
					
			
			//@TODO WTF: Combine E1 and combine with nonPawn stuff
			
			if((untouched & MASK_E1)!=EMPTY_MASK) {
				long oldE1Mask=this.moveMasks[_E1];
				long oldCastleMovesKQ=oldE1Mask&MASK_CASTLE_KING_KQkq;
				int oldCastleMovesKQCount= Long.bitCount(oldCastleMovesKQ);
				long newCastleMovesKQ=EMPTY_MASK;
				
				context.trigger(_E1);
				
				if (((ntchdOcc^MASK_E1_H1)& MASK_CASTLE_ALL_K)==EMPTY_MASK) {
					if(((this.tCallBacks[_E1]|this.tCallBacks[_F1]|this.tCallBacks[_G1])&allOfOneColor[COLOR_BLACK])==0) {
						newCastleMovesKQ|= MASK_CASTLE_KING_K;
					}
				}
				if (((ntchdOcc^MASK_E1_A1)& MASK_CASTLE_ALL_Q)==EMPTY_MASK) {
					if(((this.tCallBacks[_C1]|this.tCallBacks[_D1]|this.tCallBacks[_E1])&allOfOneColor[COLOR_BLACK])==0) {
							newCastleMovesKQ|= MASK_CASTLE_KING_Q;
					}
				}
				long mask = (oldE1Mask&MASK_NOT_CASTLE_KING_KQkq)|newCastleMovesKQ;
				moveMasks[_E1]=mask;
				if(oldCastleMovesKQ !=newCastleMovesKQ) {
					int delta = Long.bitCount(newCastleMovesKQ)-oldCastleMovesKQCount;
					if(delta!=0) {
						this.moveCount[COLOR_WHITE]+=delta;
					}
				}
			}
		}
		
		if(!_isLastRound ||isWhite ) {
			
			
			if((untouched & MASK_E8)!=EMPTY_MASK) {
				long oldE8Mask=this.moveMasks[_E8];
				long oldCastleMoveskq=oldE8Mask&MASK_CASTLE_KING_KQkq;;
				int oldCastleMoveskqCount= Long.bitCount(oldCastleMoveskq);
				long newCastleMoveskq=EMPTY_MASK;
				
				context.trigger(_E8);
	
				if (((ntchdOcc^MASK_E8_H8)& MASK_CASTLE_ALL_k)==EMPTY_MASK) {
					if(((this.tCallBacks[_E8]|this.tCallBacks[_F8]|this.tCallBacks[_G8])&allOfOneColor[COLOR_WHITE])==0) {
						newCastleMoveskq|= MASK_CASTLE_KING_k;
					}
				}
				if (((ntchdOcc^MASK_E8_A8)& MASK_CASTLE_ALL_q)==EMPTY_MASK) {
					if(((this.tCallBacks[_C8]|this.tCallBacks[_D8]|this.tCallBacks[_E8])&allOfOneColor[COLOR_WHITE])==0) {
						newCastleMoveskq|= MASK_CASTLE_KING_q;
					}
				}
				long mask = (oldE8Mask&MASK_NOT_CASTLE_KING_KQkq)|newCastleMoveskq;
				moveMasks[_E8]=mask;
				
				if(oldCastleMoveskq !=newCastleMoveskq) {
					int delta = Long.bitCount(newCastleMoveskq)-oldCastleMoveskqCount;
					if(delta!=0) {
						this.moveCount[COLOR_BLACK]+=delta;
					}
				}
			}
			if(((untouched & MASK_NOT_ALL_ROOKS) ==0 || (untouched & MASK_ALL_ROOKS)==0)) {
				untouched = EMPTY_MASK;
				return;
			}
		}
	}
	
	
	@Override
	public void checkLegalMoves (){
		if(totalCount== 45341863) {
			System.out.println("WTF");
		}
		int otherColor = getColorAtTurn();
		int color = OTHER_COLOR[otherColor];
		
		
		
		long kingMask = this.kings[color];
		
		if(kingMask!=EMPTY_MASK) {
			int kingPos = Long.numberOfTrailingZeros(kingMask);
			long otherXRayers = this.allOfOneColor[otherColor]&~pawns[otherColor]&~kings[otherColor]&~knights[otherColor];
 			long own = this.allOfOneColor[color];// @todo WTF too expensive
			long other = this.allOfOneColor[otherColor];
		
			long kingCbs = this.tCallBacks[kingPos]&other;
			int kingAttacks = Long.bitCount(kingCbs);
			long checkMask=FULL_MASK;

			_kingOnly=false;
			if(kingAttacks>0) {
				//one or two attacks
				int kingAttackerPos = -1;
				//What is under Attack by whom? ENP CASE: If attacked by!
				for(int i=0;i<kingAttacks;i++) {
					kingAttackerPos = Long.numberOfTrailingZeros(kingCbs);
					kingCbs&=kingCbs-1;
					if((otherXRayers&SHIFT[kingAttackerPos])!=0) {
						long pinLine= NOT_LINE_FOR_2_POINTS[kingAttackerPos][kingPos]|SHIFT[kingAttackerPos];
						_pinMasks[kingPos]&=pinLine;	
	
					}
				}
				
				if(kingAttacks==1){
					//one attack
					checkMask = CHECK_MASKS[kingPos][kingAttackerPos];
				}else {
					//two attacks
					checkMask = EMPTY_MASK;
					_kingOnly=true;
				}	
			}
			//where can the king still move? //ENP OK
			long kingMovesRemaining = this.moveMasks[kingPos]&_pinMasks[kingPos]&KING_MASKS[kingPos];
			int count = Long.bitCount(kingMovesRemaining );
			for(int i=0;i<count;i++) {
				int pos = Long.numberOfTrailingZeros(kingMovesRemaining);
				kingMovesRemaining &=kingMovesRemaining -1;
				if((tCallBacks[pos]&allOfOneColor[otherColor])!=EMPTY_MASK) {
					_pinMasks[kingPos]&=NOT_SHIFT[pos];
				}
			}
			
			// KingPos finally;
			_moveDelta =Long.bitCount(moveMasks[kingPos]&~_pinMasks[kingPos]);
			
			
			
			boolean attackedByEnpPawn =kingAttacks!=0&&(this._enpPawnMask&checkMask)!=EMPTY_MASK;
			if(kingAttacks<2) {
				//not more than one attack
				//check pins
				//this requires an ENP case
				long all = EMPTY_MASK;
				if(checkMask!=FULL_MASK) {
					all = allOfOneColor[color]&~kingMask;
				}
				
				long inSightOfKing0 = (mnf.getBishopAttacks(kingPos, _occ)|mnf.getRookAttacks(kingPos, _occ));
				long potPins = inSightOfKing0 &own;
				
				
								
				{
					long _occ1= _occ&~inSightOfKing0;
					long inSightOfKing1 = (mnf.getBishopAttacks(kingPos, _occ1)|mnf.getRookAttacks(kingPos, _occ1));
					
					long potPinners = inSightOfKing1 & otherXRayers;
				
					long bits = potPinners;
					
					int counter = Long.bitCount(bits);
					//idea: invert the filter = by pinner instead by  pinned
					for(int i=0;i<counter;i++) {
						int pinnerPos = Long.numberOfTrailingZeros(bits);
						bits&=bits-1;
						long pinLine = LINE_FOR_2_POINTS[kingPos][pinnerPos];
						long pinned = pinLine&potPins;
						if(pinned!=EMPTY_MASK){
							int pinnedPos = Long.numberOfTrailingZeros(pinned);
							if((tCallBacks[pinnedPos]&SHIFT[pinnerPos])!=0) {
								_pinMasks[pinnedPos] =pinLine ;
								all|=SHIFT[pinnedPos];
							}
						}
					}
				}
				
				
				
				if(this._enpBeaterPos[0]!=-1) {
					//remove enpBeaters from moveMask if check remains after sim move
					for(int i=0;i<2;i++) {
						int pos =_enpBeaterPos[i];
						if(pos ==-1)break;
						long simOcc = _occ^(SHIFT[pos]|this.enPassanteMask|this._enpPawnMask);
						// is King in check?
						long attackAfterENP = mnf.getBishopAttacks(kingPos, simOcc)&otherXRayers;
						int counter= Long.bitCount(attackAfterENP);
						boolean found =false;
						for(int j=0;j<counter;j++) {
							int attacker = Long.numberOfTrailingZeros(attackAfterENP);
							attackAfterENP&=attackAfterENP-1;
							if((this.fields[attacker]>>7)!=PIECE_TYPE_ROOK) {
								//hit
								_pinMasks[pos]&=~this.enPassanteMask ;
								all|=SHIFT[pos];
								found=true;
								break;
							}
						}
						if(found) {
							continue;
						}
						attackAfterENP = mnf.getRookAttacks(kingPos, simOcc)&otherXRayers;
						counter= Long.bitCount(attackAfterENP);
						for(int j=0;j<counter;j++) {
							int attacker = Long.numberOfTrailingZeros(attackAfterENP);
							attackAfterENP&=attackAfterENP-1;
							if(this.fields[attacker]>>7!=PIECE_TYPE_BISHOP) {
								_pinMasks[pos]&=~this.enPassanteMask ;
								all|=SHIFT[pos];
								found=true;
								break;
							}
						}
						if(found) {
							continue;
						}
						// we found that there is no check after the enp by the beater
						// if enpPawn is an attacker => 
						if(attackedByEnpPawn) {
							_pinMasks[pos] &=checkMask;
							_pinMasks[pos] |=this.enPassanteMask;
							
					
							all&=NOT_SHIFT[pos];//remove from post processsing! WTF ENSURE PROPPER COUNTING
							_moveDelta +=Long.bitCount(moveMasks[pos]&~_pinMasks[pos]);
						}
					}			
				}

				int cnt = Long.bitCount(all);
				for(int i=0;i<cnt;i++) {
					int pos = Long.numberOfTrailingZeros(all);
					all&=all-1;
					_pinMasks[pos]&=checkMask;
					if(fields[pos]==PIECE_TYPE_WHITE_PAWN && getRankForPos(pos)==_7){
						_pinMasks[pos]&=MASK_8_RANK;
						long last =moveMasks[pos]&_pinMasks[pos] ;
						if((last)!=EMPTY_MASK)  {
							last|= last>>DIR_UP;
							last|= last>>DIR_UP;
							last|= last>>DIR_UP;
							_pinMasks[pos]|=last;
						}
					}
					
					if(fields[pos]==PIECE_TYPE_BLACK_PAWN && getRankForPos(pos)==_2) {
						_pinMasks[pos]&=MASK_8_RANK;
						long last =moveMasks[pos]&_pinMasks[pos] ;
						if((last)!=EMPTY_MASK)  {
							last|= last<<DIR_UP;
							last|= last<<DIR_UP;
							last|= last<<DIR_UP;
							_pinMasks[pos]|=last;
						}
					}
					_moveDelta +=Long.bitCount(moveMasks[pos]&~_pinMasks[pos]);

				}
			}
		
		} 		
		
	}
  	
	
	public void calcNewMoves(int color) {
		if(!_noCalc) return;
  		//System.out.print("Calculating MoveCount in step:"+this.wtfcount);
  		_noCalc = false;
  		ContextLevel oldestContext = this.contextLevels[level-4];
  		ContextLevel oldContext = this.contextLevels[level-2];
  		ContextLevel context = this.contextLevels[level];
  		context.init();
  		
  		// we need this (eighter because of king moves that remain in check or if no check because of pins
  		long bits;
  		if(_kingOnly ) {
  			bits = this.kings[color];
		}else {
			bits = this.allOfOneColor[color];
		}
		
  		int retVal = Long.bitCount(bits);
  		for(int i=0;i<retVal;i++) {
  			int pos = Long.numberOfTrailingZeros(bits);
			bits &= bits - 1;
		
			long moveMask = this.moveMasks[pos];
			long pinMask = _pinMasks[pos];
			_pinMasks[pos]=FULL_MASK;
			moveMask&=pinMask;	
			if(moveMask==0) {
				continue;
			}
			
			int typeColor = fields[pos];
			if(context.checkForReuseMoves(pos, moveMask, typeColor)) {
				continue;
			}
			Move[] moves = oldContext.getMoves(pos, moveMask, typeColor);
			if(moves==null) {
				moves = oldestContext.getMoves(pos, moveMask, typeColor);
			}
			if(moves==null) {
				moves =BBMoveManager.moves[pos+typeColor];
				context.extractFromRawMoves(pos, moveMask, typeColor, moves);
			}else {
				context.addMoves(pos, moveMask, typeColor, moves);
			}
		}
  		context.setLimit(this.getMoveCount());
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
	
	@Override
	public int getTotalCount() {
		return totalCount;
	}

	
}
