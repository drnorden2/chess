package perft.chess.perftbb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import static perft.chess.Definitions.*;


import perft.chess.Position;
import perft.chess.fen.Fen;
import perft.chess.nnue.NNUE;
import perft.chess.perftbb.gen.MagicNumberFinder;



public class BBPosition implements Position {
	ArrayList<String> list = new ArrayList<String>();
	
	private final static int[] ROCHADE = new int[] {32*64+0,32*64+1,32*64+2,32*64+3};
	private final static int ENP_INDEX = 32*64+4;
	
	
	private final long[][] randomBitStr= new long[64][32*68+4+1];
	
	private int totalCount =0;
	
	private int level =4;
	
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
	public long untouched=EMPTY_MASK;
	public long enPassanteMask=EMPTY_MASK;
	public long zobristHash=0;
	public boolean rochades = false;
	public final int[] material = new int[2];
	

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
	public  int moveDelta;
	private boolean _calcDone = false;
	private final long[] fullMaskArray = new long[64];
	
	private NNUE nnue = new NNUE();
	private NNUE nnue2 = new NNUE();
	
	public BBPosition(int depth) {
		// generate an array of random bitStrings
		Random random = new Random(1648119808);
		nnue.nnue_init("C:/Users/afisc/eclipse-workspace6/chess/nn-eba324f53044.nnue");
		//nnue.nnue_init("/home/linux-ml/git/chess/my.nnue");
		
	
		for (int i = 0; i < randomBitStr.length; i++) {
	        for (int j = 0; j < randomBitStr[i].length; j++) {
	        	randomBitStr[i][j] = Long.MAX_VALUE*random.nextLong();

	        }
		}
		this.depth = depth;
		System.out.println("Depth:"+depth);
		contextLevels = new ContextLevel[1000];//WTF (-1)
		for(int i=0;i<this.contextLevels.length;i++) {
			this.contextLevels[i]=new ContextLevel(this,i);
		}
		for(int i=0;i<64;i++) {
			fields[i]=-1;
			fullMaskArray[i]=FULL_MASK;
		}
	}

	@Override
	public void initialAddToBoard(int color, int type, int pos) {
		int typeColor =((type <<1) + color) << 6;
		
		zobristHash^=randomBitStr[pos][typeColor];
		this.fields[pos]= typeColor;
		this.material[color]+=NAIVE_MATERIAL_COUNT[type];		
		
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
		if(enpassantePos!=EMPTY_MASK) {
			zobristHash^=randomBitStr[Long.bitCount(enpassantePos)][ENP_INDEX];
		}
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
  		long tmp = this.untouched;
  		touch(FULL_MASK);
  		this.untouched =tmp;
  		rochades = this.untouched!=EMPTY_MASK;
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
				updateNonPawnPseudoMoves(typeColor,typeColor>>>7,i, pos,true);
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
		if(rochades) {
			checkRochade(context,null); 
		}
		this.colorAtTurn=OTHER_COLOR[this.colorAtTurn];
		_calcDone = false;
	}
	
	public int getMoveCount() {	
		if(this._kingOnly) {
			return this.moveDelta;
		}else {
			return this.moveCount[this.colorAtTurn]-this.moveDelta;
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

	
	int counter=0;
	@Override
	
	public void unSetMove(int move) {
		ContextLevel context = this.contextLevels[level];
		context.revertToSnapshot();
		level--;
		this.colorAtTurn = OTHER_COLOR[colorAtTurn];
		if(!_calcDone) {// eighter last level or no continuation (checkmat or pat
			System.arraycopy(fullMaskArray, 0, _pinMasks, 0, 64);	
			_calcDone=true;
		}
		//list.remove(list.size()-1);
	}
	public static String moveNotation="";
	@Override
	public void setMove(int index,boolean isSim, boolean isLast) {
		
		totalCount++;
		Move move = getMove(index);
		evaluate(index);
		/*
		moveNotation = move.getNotation();
		list.add(totalCount+"\n"+move.toString()+this.toStringDebug()+"MoveCount:"+this.getMoveCount());
		System.out.println(totalCount);
		if(totalCount == 382) {
			System.out.println("Help");
			System.out.println(list);
		}*/
		
		//System.out.println(this);
		
		//System.out.println("Move:"+index+"/"+this.getMoveCount()+"  ("+move+") - "+(totalCount)+" moveDelta:"+this.moveDelta);
		
	
		
		
		level++;
		ContextLevel context = this.contextLevels[level];
		
		if(!isLast) {
			setMove(move,context);
			
			int color = this.colorAtTurn;
			int otherColor = OTHER_COLOR[color];
			this.updatePawnPseudoMoves(color);
			this.updatePawnPseudoMoves(otherColor); 
			
			this.checkLegalMoves();
			
			if(rochades) {
				checkRochade(context,move); 
			}
			
			this.colorAtTurn = OTHER_COLOR[colorAtTurn];
			_calcDone=false;
		}else {
			last_setMove(move,context);
			
			int color = this.colorAtTurn;
			int otherColor = OTHER_COLOR[color];
			this.last_updatePawnPseudoMoves(otherColor); 
			
			this.last_checkLegalMoves();
			
			if(rochades) {
				last_checkRochade(context,move,color==COLOR_WHITE); 
			}			
			
			this.colorAtTurn = OTHER_COLOR[colorAtTurn];
			_calcDone=false;
			
		}
		//evaluate(index);
		if(!isSim) {
			/*for(int i=4;i<1;i--) {
				ContextLevel tmp = this.contextLevels[level-i];
				this.contextLevels[level-i]= this.contextLevels[level-(i-1)];
				this.contextLevels[level-(i-1)]=tmp;
			}
			level=4;
			*/
			
		}
		
	}
	
	private void setMove(Move move,ContextLevel context ) {	
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
		
		
		cbs= this.tCallBacks[newPos];
/*E*/	int otherTypeColor = fields[newPos];
		if(otherTypeColor!=-1) {
			zobristHash^=randomBitStr[newPos][otherTypeColor];
			this.material[otherColor]-=NAIVE_MATERIAL_COUNT[otherTypeColor>>>7];		
					
			//alter moves(ok), moveCount(ok), allOfOneColor(ok), fields(later), callbacks(ok), tcallbacks(ok) and if neccessary pawns!(ok)
/*!!!*/		removePseudoMoves(otherColor,newPos);
			allOfOneColor[otherColor]^=newPosMask;
			if(otherTypeColor>>>7==PIECE_TYPE_PAWN) {
				this.pawns[otherColor]^=newPosMask;
			}
			if(otherTypeColor>>>7==PIECE_TYPE_KNIGHT) {
				this.knights[otherColor]^=newPosMask;
			}

			//cbsNewPosReplace= this.tCallBacks[newPos];
		}else {
			//cbsNewPosEmptyBefore = this.tCallBacks[newPos];
			emptyBefore = cbs&this.allOfOneColor[otherColor];
		}
		
		
		
		//int typeColor = context.getAndSetFields(oldPos,-1);
		int typeColor = fields[oldPos];
	
		zobristHash^=randomBitStr[oldPos][typeColor];
		this.material[color]-=NAIVE_MATERIAL_COUNT[typeColor>>>7];		
		fields[oldPos]=-1;
		
/*!!!*/	removePseudoMoves(color,oldPos);

		if(move.isPromotion()) {
			typeColor = move.getPromotePieceType();
			this.material[color]-=NAIVE_MATERIAL_COUNT[PIECE_TYPE_PAWN];		
			pieceType = typeColor>>>7;
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
		zobristHash^=randomBitStr[newPos][typeColor];
		this.material[color]+=NAIVE_MATERIAL_COUNT[typeColor>>>7];		

		this.allOfOneColor[color]^=moveMask;
		
		
		cbs |= this.tCallBacks[oldPos];
		if(pieceType==PIECE_TYPE_KING) {
			this.kings[color]=newPosMask;
		}
		
		
		if(enPassanteMask!=EMPTY_MASK)	{
			int enpPos = Long.numberOfTrailingZeros(enPassanteMask);
			if(move.getEnPassanteSquare()==enpPos) {
				int enpPawnPos = move.getEnPassantePawnPos();
				context.trigger(enpPawnPos);
				cbs|= this.tCallBacks[enpPawnPos]; 	
				
				zobristHash^=randomBitStr[enpPawnPos][fields[enpPawnPos]];
				this.material[otherColor]-=NAIVE_MATERIAL_COUNT[PIECE_TYPE_PAWN];		

				fields[enpPawnPos]=	-1;
/*u*/			
				this.allOfOneColor[otherColor]&=~SHIFT[enpPawnPos];// might be empty anyways
				pawns[otherColor]&=~SHIFT[enpPawnPos];
				removePseudoMoves(otherColor,enpPawnPos);
			}
			this.enPassanteMask=EMPTY_MASK;
			zobristHash^=randomBitStr[enpPos][ENP_INDEX];
		}

			
		if(move.isTwoSquarePush()) {
			zobristHash^=randomBitStr[move.getEnPassanteSquare()][ENP_INDEX];
			this.enPassanteMask=SHIFT[move.getEnPassanteSquare()];//@TBD always set (geht mit stack)
		}

		
		_occ = allOfOneColor[COLOR_WHITE]|allOfOneColor[COLOR_BLACK];
		_notOcc = ~_occ;
				
		if(pieceType!=PIECE_TYPE_PAWN) {
			this.updateNonPawnPseudoMoves(typeColor, pieceType,color,newPos,true);
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
				touch(moveMask);
			}
		}
		cbs &= (_occ)&~(moveMask)&~(pawns[COLOR_WHITE]|pawns[COLOR_BLACK]);//@todo occ var
		int retVal = Long.bitCount(cbs);
		for(int i=0;i<retVal;i++) {			
			int cbPos = Long.numberOfTrailingZeros(cbs);
			cbs &= cbs- 1;
			int tC=fields[cbPos];
			int type = tC>>>7;
			int col = tC >>> 6 & 1;
		
			if(type>PIECE_TYPE_KNIGHT || (emptyBefore&SHIFT[cbPos])==0) { //@todo WTF check performance
				this.updateNonPawnPseudoMoves(tC, type, col, cbPos,false);
			}
		}
	}
	
	private void removePseudoMoves(int color, int pos) {
		//remove moves
		this.moveCount[color]-=Long.bitCount(moveMasks[pos]);
		moveMasks[pos]=EMPTY_MASK;
		//remove attacks
		long oldCBs = callBacks[pos];
		callBacks[pos]= EMPTY_MASK;
		
		int retVal = Long.bitCount(oldCBs);
		for(int i=0;i<retVal;i++) {
			tCallBacks[Long.numberOfTrailingZeros(oldCBs)]^=SHIFT[pos];
			oldCBs &= oldCBs - 1;			
		}
	
	}

	
	
	private void updateNonPawnPseudoMoves(int typeColor, int type, int color ,int pos,boolean isAdded) {
		ContextLevel context = contextLevels[level];
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
		
			delta = (mLeft^oldContext._mLeft[color])>>>DIR_UP_LEFT;
			//out(oldContext._mLeft[color]);
			//out(delta);
			delta |= (mRight^oldContext._mRight[color])>>>DIR_UP_RIGHT;
			//out(oldContext._mRight[color]);
			//out(delta);
			delta |= (mOneUp^oldContext._mOneUp[color])>>>DIR_UP;
			//out(delta);
			delta |= (mTwoUp^oldContext._mTwoUp[color])>>>DIR_2_UP;

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
						last|= last>>>DIR_UP;
						last|= last>>>DIR_UP;
						mask|= last>>>DIR_UP;
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
			mLeft= (ownPawns >>>DIR_UP_LEFT) & MASK_NOT_A_FILE&other;
			//out(mLeft);
			mRight=(ownPawns >>>DIR_UP_RIGHT) & MASK_NOT_H_FILE&other;
			//out(mRight);
			mOneUp = (ownPawns >>> DIR_UP) & _notOcc ;
			//out(mOneUp);
			mTwoUp = (((mOneUp & MASK_6_RANK) >>> DIR_UP)& _notOcc ) ;
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


	private void checkRochade(ContextLevel context, Move move) {
		//remove rochade protagonists in occ if touched
		long ntchdOcc= _occ & MASK_NOT_ALL_ROOKS_KINGS |untouched;
					
			
		//@TODO WTF: Combine E1 and combine with nonPawn stuff
		if(fields[_E1]==PIECE_TYPE_WHITE_KING) {
				
			long oldE1Mask=this.moveMasks[_E1];
			
			if((untouched & MASK_E1)!=EMPTY_MASK||(oldE1Mask&MASK_CASTLE_KING_KQkq)!=EMPTY_MASK) {
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
					this.moveCount[COLOR_WHITE]+=Long.bitCount(newCastleMovesKQ)-oldCastleMovesKQCount;
				}
			}
		}
		
		if(fields[_E8]==PIECE_TYPE_BLACK_KING) {
				
			long oldE8Mask=this.moveMasks[_E8];		
			if((untouched & MASK_E8)!=EMPTY_MASK ||(oldE8Mask&MASK_CASTLE_KING_KQkq)!=EMPTY_MASK) {
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
					this.moveCount[COLOR_BLACK]+=Long.bitCount(newCastleMoveskq)-oldCastleMoveskqCount;
				}
			}
		}
		
		if(untouched==EMPTY_MASK) {
			rochades =false;
		}
	}
	
	
	@Override
	public void checkLegalMoves (){
		/*
		if(totalCount== 3090386) {
			System.out.println("WTF");
		}*/
		int otherColor = getColorAtTurn();
		int color = OTHER_COLOR[otherColor];
		
		
		
		long kingMask = this.kings[color];
		
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
		if(_kingOnly) {
			//calculate the final amount 
			moveDelta =Long.bitCount(moveMasks[kingPos]&_pinMasks[kingPos]);
		}else {
			//calculate the delta but neglect the rochades!
			moveDelta =Long.bitCount(KING_MASKS[kingPos]&moveMasks[kingPos]&~_pinMasks[kingPos]);
		}
		
		
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
						if((this.fields[attacker]>>>7)!=PIECE_TYPE_ROOK) {
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
						if(this.fields[attacker]>>>7!=PIECE_TYPE_BISHOP) {
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
						moveDelta +=Long.bitCount(moveMasks[pos]&~_pinMasks[pos]);
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
						last|= last>>>DIR_UP;
						last|= last>>>DIR_UP;
						last|= last>>>DIR_UP;
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
				moveDelta +=Long.bitCount(moveMasks[pos]&~_pinMasks[pos]);

			}
		}
		
		
	}
  	
	int cnt=0;
	public void calcNewMoves(int color) {
		if(_calcDone) return;
		_calcDone = true;
		
		
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
			Move[] moves=null;
			
			if(context.checkForReuseMoves(pos, moveMask, typeColor)) {
				continue;
			}
			
			moves = oldContext.getMoves(pos, moveMask, typeColor);
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
		//System.out.println("Level"+level+" Calced w."+this.getMoveCount());
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
		return str;
	}
	public String toStringDebug() {
		return analyzer.toStringDebug();
	}

	
	@Override
	public long getHash() {
		return this.zobristHash;
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

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	private void last_setMove(Move move,ContextLevel context ) {	
		
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
		
		
		cbs= this.tCallBacks[newPos];
		int otherTypeColor = fields[newPos];
		if(otherTypeColor!=-1) {
			this.material[otherColor]-=NAIVE_MATERIAL_COUNT[otherTypeColor>>>7];		

			zobristHash^=randomBitStr[newPos][otherTypeColor];
			last_removeOtherPseudoMoves(otherColor,newPos);
			//removePseudoMoves(otherColor,newPos);//!!!!!!!!!!!!!!!!!!!!!!!!!!11
		
			allOfOneColor[otherColor]^=newPosMask;
			if(otherTypeColor>>>7==PIECE_TYPE_PAWN) {
				this.pawns[otherColor]^=newPosMask;
			}
			if(otherTypeColor>>>7==PIECE_TYPE_KNIGHT) {
				this.knights[otherColor]^=newPosMask;
			}
		}else {
			emptyBefore = cbs&this.allOfOneColor[otherColor];
		}
		
		
		
		//int typeColor = context.getAndSetFields(oldPos,-1);
		int typeColor = fields[oldPos];
		
		this.material[color]-=NAIVE_MATERIAL_COUNT[typeColor>>>7];		
		zobristHash^=randomBitStr[oldPos][typeColor];
		fields[oldPos]=-1;
		
		last_removeOwnPseudoMoves(color,oldPos);

		if(move.isPromotion()) {
			typeColor = move.getPromotePieceType();
			this.material[color]-=NAIVE_MATERIAL_COUNT[PIECE_TYPE_PAWN];		
			pieceType = typeColor>>>7;
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
		
		this.material[color]+=NAIVE_MATERIAL_COUNT[typeColor>>>7];		
		zobristHash^=randomBitStr[newPos][typeColor];
		fields[newPos]=typeColor;
		
		
		this.allOfOneColor[color]^=moveMask;
		
		
		cbs |= this.tCallBacks[oldPos];
		if(pieceType==PIECE_TYPE_KING) {
			this.kings[color]=newPosMask;
		}
		
		
		if(enPassanteMask!=EMPTY_MASK)	{
			int enpPos = Long.numberOfTrailingZeros(enPassanteMask);
			if(move.getEnPassanteSquare()==enpPos) {
				int enpPawnPos = move.getEnPassantePawnPos();
				context.trigger(enpPawnPos);
				cbs|= this.tCallBacks[enpPawnPos]; 
				zobristHash^=randomBitStr[enpPawnPos][fields[enpPawnPos]];
				this.material[otherColor]-=NAIVE_MATERIAL_COUNT[PIECE_TYPE_PAWN];		
				fields[enpPawnPos]=	-1;
/*u*/			this.allOfOneColor[otherColor]&=~SHIFT[enpPawnPos];// might be empty anyways
				pawns[otherColor]&=~SHIFT[enpPawnPos];
				//removePseudoMoves(otherColor,enpPawnPos);
				last_removeOtherPseudoMoves(otherColor,enpPawnPos);
			}
			this.enPassanteMask=EMPTY_MASK;
			zobristHash^=randomBitStr[enpPos][ENP_INDEX];
		}

			
		if(move.isTwoSquarePush()) {
			zobristHash^=randomBitStr[move.getEnPassanteSquare()][ENP_INDEX];
			this.enPassanteMask=SHIFT[move.getEnPassanteSquare()];//@TBD always set (geht mit stack)
		}

		
		_occ = allOfOneColor[COLOR_WHITE]|allOfOneColor[COLOR_BLACK];
		_notOcc = ~_occ;
				
		if(pieceType!=PIECE_TYPE_PAWN) {
			this.last_updateOwnNonPawnPseudoMoves(typeColor,pieceType, color,newPos);
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
					this.last_setMove(move.getRookMove(),context);
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
			int tC = fields[cbPos];
			int type  = tC>>>7;
			int col = tC >>> 6 & 1;
			
			if(col==color) {
				if(type>PIECE_TYPE_KNIGHT) {
					this.last_updateOwnNonPawnPseudoMoves(tC, type, col, cbPos);
				}
			}else {
				if( type>PIECE_TYPE_KNIGHT || (emptyBefore&SHIFT[cbPos])==0) { //!!!!!!!!!!!!!@todo WTF check performance
					this.last_updateOtherNonPawnPseudoMoves(tC,type, col, cbPos);
				}
			}
		
		}
	}
	
	private void last_removeOwnPseudoMoves(int color, int pos) {
		//remove attacks
		long oldCBs = callBacks[pos];
		callBacks[pos]= EMPTY_MASK;
		
		

		
		int retVal = Long.bitCount(oldCBs);
		for(int i=0;i<retVal;i++) {
			tCallBacks[Long.numberOfTrailingZeros(oldCBs)]^=SHIFT[pos];
			oldCBs &= oldCBs - 1;			
		}
	
	}
	
	
	
	
	
	
	
	private void last_removeOtherPseudoMoves(int color,int pos) {
		//remove moves
		this.moveCount[color]-=Long.bitCount(moveMasks[pos]);
		moveMasks[pos]=EMPTY_MASK;
		//remove attacks
		long oldCBs = callBacks[pos];
		callBacks[pos]= EMPTY_MASK;
		
		int retVal = Long.bitCount(oldCBs);
		for(int i=0;i<retVal;i++) {
			tCallBacks[Long.numberOfTrailingZeros(oldCBs)]^=SHIFT[pos];
			oldCBs &= oldCBs - 1;			
		}
	
	}
	
	private void last_updateOwnNonPawnPseudoMoves(int typeColor, int type, int color,int pos) {
		ContextLevel context = contextLevels[level];
		long callbacks = EMPTY_MASK;
		if(type<=PIECE_TYPE_KNIGHT) {
			callbacks = BBMoveManager.moveMasks[pos + typeColor];
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
	
	
	private void last_updateOtherNonPawnPseudoMoves(int typeColor,int type, int color, int pos) {
		ContextLevel context = contextLevels[level];
		long callbacks = EMPTY_MASK;
			
		long moves = EMPTY_MASK;
		long own = this.allOfOneColor[color];// @todo WTF too expensive
		
		if(type<=PIECE_TYPE_KNIGHT) {
			callbacks = BBMoveManager.moveMasks[pos + typeColor];
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
	
	private void last_updatePawnPseudoMoves(int color) {
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
		
			delta = (mLeft^oldContext._mLeft[color])>>>DIR_UP_LEFT;
			//out(oldContext._mLeft[color]);
			//out(delta);
			delta |= (mRight^oldContext._mRight[color])>>>DIR_UP_RIGHT;
			//out(oldContext._mRight[color]);
			//out(delta);
			delta |= (mOneUp^oldContext._mOneUp[color])>>>DIR_UP;
			//out(delta);
			delta |= (mTwoUp^oldContext._mTwoUp[color])>>>DIR_2_UP;

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
						last|= last>>>DIR_UP;
						last|= last>>>DIR_UP;
						mask|= last>>>DIR_UP;
					}
					//long oldMask = context.getAndSetMoveMasks(pos,mask);
					long oldMask = moveMasks[pos];
					moveMasks[pos]=mask;
					
					////out(oldMask);
					moveCountDelta+=Long.bitCount(mask)- Long.bitCount(oldMask);
				}
			}
			this.moveCount[color]+=moveCountDelta;

		}else {
			
			
			//out(ownPawns);
			other|=(this.enPassanteMask & MASK_3_RANK);
			//out(other);
			mLeft= (ownPawns >>>DIR_UP_LEFT) & MASK_NOT_A_FILE&other;
			//out(mLeft);
			mRight=(ownPawns >>>DIR_UP_RIGHT) & MASK_NOT_H_FILE&other;
			//out(mRight);
			mOneUp = (ownPawns >>> DIR_UP) & _notOcc ;
			//out(mOneUp);
			mTwoUp = (((mOneUp & MASK_6_RANK) >>> DIR_UP)& _notOcc ) ;
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
			this.moveCount[color]+=moveCountDelta;
		}
	}

	
	
	
	public void last_checkLegalMoves (){
		/*
		if(totalCount== 3090386) {
			System.out.println("WTF");
		}*/
		int otherColor = getColorAtTurn();
		int color = OTHER_COLOR[otherColor];
		
		
		
		long kingMask = this.kings[color];
		
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
		if(_kingOnly) {
			//calculate the final amount 
			moveDelta =Long.bitCount(moveMasks[kingPos]&_pinMasks[kingPos]);
		}else {
			//calculate the delta but neglect the rochades!
			moveDelta =Long.bitCount(KING_MASKS[kingPos]&moveMasks[kingPos]&~_pinMasks[kingPos]);
		}
		
		
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
						if((this.fields[attacker]>>>7)!=PIECE_TYPE_ROOK) {
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
						if(this.fields[attacker]>>>7!=PIECE_TYPE_BISHOP) {
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
						moveDelta +=Long.bitCount(moveMasks[pos]&~_pinMasks[pos]);
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
						last|= last>>>DIR_UP;
						last|= last>>>DIR_UP;
						last|= last>>>DIR_UP;
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
				moveDelta +=Long.bitCount(moveMasks[pos]&~_pinMasks[pos]);

			}
		}
	}

	private void last_checkRochade(ContextLevel context, Move move, boolean isWhite) {
		//remove rochade protagonists in occ if touched
		long ntchdOcc= _occ & MASK_NOT_ALL_ROOKS_KINGS |untouched;
		
		if(!isWhite) {
			if(fields[_E1]==PIECE_TYPE_WHITE_KING) {
				//@TODO WTF: Combine E1 and combine with nonPawn stuff
				
				long oldE1Mask=this.moveMasks[_E1];
				long oldCastleMovesKQ=oldE1Mask&MASK_CASTLE_KING_KQkq;
				if((untouched & MASK_E1)!=EMPTY_MASK||oldCastleMovesKQ!=EMPTY_MASK) {
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
					if(oldCastleMovesKQ !=newCastleMovesKQ) {
						this.moveCount[COLOR_WHITE]+=Long.bitCount(newCastleMovesKQ)-oldCastleMovesKQCount;
					}
				}
			}
		}else {
			if(fields[_E8]==PIECE_TYPE_BLACK_KING){
				long oldE8Mask=this.moveMasks[_E8];
				long oldCastleMoveskq=oldE8Mask&MASK_CASTLE_KING_KQkq;;
				if((untouched & MASK_E8)!=EMPTY_MASK||oldCastleMoveskq!=EMPTY_MASK) {
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
					if(oldCastleMoveskq !=newCastleMoveskq) {
						this.moveCount[COLOR_BLACK]+=Long.bitCount(newCastleMoveskq)-oldCastleMoveskqCount;
					}
				}
			}
		}
	}
	private void touch(long mask){
		if((untouched&mask) != EMPTY_MASK) {
			long before = untouched;
			untouched&=~mask;
			if((untouched & MASK_E1) ==EMPTY_MASK ||(untouched & MASK_A1_H1)==EMPTY_MASK){
				untouched &=MASK_NOT_1_RANK;
			}
			if((untouched & MASK_E8) ==EMPTY_MASK ||(untouched & MASK_A8_H8)==EMPTY_MASK){
				untouched &=MASK_NOT_8_RANK;
			}
			long delta = before^untouched;
			if((MASK_A1&delta)!=EMPTY_MASK){
				zobristHash^=randomBitStr[_A1][ROCHADE[0]];
			}
			if((MASK_H1&delta)!=EMPTY_MASK){
				zobristHash^=randomBitStr[_H1][ROCHADE[1]];
			}
			if((MASK_A8&delta)!=EMPTY_MASK){
				zobristHash^=randomBitStr[_A8][ROCHADE[2]];
			}
			if((MASK_H8&delta)!=EMPTY_MASK){
				zobristHash^=randomBitStr[_H8][ROCHADE[3]];
			}
		}
	}
	public boolean isCheck() {
		return this.tCallBacks[Long.numberOfLeadingZeros(this.kings[OTHER_COLOR[colorAtTurn]])]!=0;
	}
	public boolean isUntouched(int index) {
		return (this.untouched&SHIFT[index])!=0;
	}

	public int getEnpassantePos(){
		return Long.bitCount(this.enPassanteMask);
	}
		
	public int getPiece(int index) {
		return fields[index];
	}
	/*
	
	public double evaluate(int i) {
		if(i==-1) {
			//for current color
			if(isCheck()) {
				return SIGN_BY_COLOR[colorAtTurn]* (level - 10_000);//keep in mind this is one level before the nxt move
			}else {
				return SIGN_BY_COLOR[colorAtTurn]* 1.5;//Patt
			}
		}else {
			Move move = this.getMove(i);
			int delta = 0;
			if(this.enPassanteMask!=EMPTY_MASK && Long.bitCount(this.enPassanteMask)== move.getEnPassanteSquare()) {
				delta = 1;
			}else if(move.isPromotion()){
				delta = NAIVE_MATERIAL_COUNT[move.getPromotePieceType()>>>7]-NAIVE_MATERIAL_COUNT[PIECE_TYPE_PAWN];
			}else {
				int typeColor = fields[move.getNewPos()];
				if(typeColor!=-1) {
					delta = NAIVE_MATERIAL_COUNT[typeColor>>>7];
				}
			}
			
			//test(delta, delta+material[colorAtTurn]-material[OTHER_COLOR[colorAtTurn]]);
			
			return SIGN_BY_COLOR[colorAtTurn]* (material[OTHER_COLOR[colorAtTurn]]-material[colorAtTurn]-delta);//+(double)(Math.random()*0.1);
		}
	}
	private void test(int delta, int ref) {
		int sum = delta;
		for(int i=0;i<fields.length;i++) {
			
			int typeColor = fields[i];
			if(typeColor==-1)continue;
			int color = typeColor >>> 6 & 1;
			int type = typeColor>>>7;
 			if(color==colorAtTurn) {
				sum += NAIVE_MATERIAL_COUNT[type];
			}else {
				sum -= NAIVE_MATERIAL_COUNT[type];
			}
		}
		if(sum!=ref) {
			System.out.println("HEy: "+sum +"!="+ref);
		}
	}*/
	

	public double evaluate1(int i) {
		double retVal;
		if(i==-1) {
			//for current color
			if(isCheck()) {
				retVal= (10_000-(level));
			}else {
				retVal= 1.5;//Patt
			}
			return SIGN_BY_COLOR[colorAtTurn]*retVal;
		}else {
			Move move = this.getMove(i);
			int delta = 0;
			if(this.enPassanteMask!=EMPTY_MASK && Long.bitCount(this.enPassanteMask)== move.getEnPassanteSquare()) {
				delta = 1;
			}else if(move.isPromotion()){
				delta = NAIVE_MATERIAL_COUNT[move.getPromotePieceType()>>>7]-NAIVE_MATERIAL_COUNT[PIECE_TYPE_PAWN];
			}else {
				int typeColor = fields[move.getNewPos()];
				if(typeColor!=-1) {
					delta = NAIVE_MATERIAL_COUNT[typeColor>>>7];
				}
			}
			retVal=(material[0]-material[1]);//+(double)(Math.random()*0.1);
			return SIGN_BY_COLOR[colorAtTurn]*(retVal+delta);
		}
	}

	public double evaluateX(int i) {
		double retVal;
		if(this.getMoveCount()==0) {
			//for current color
			if(isCheck()) {
				retVal= (10_000-(level));
			}else {
				retVal= 1.5;//Patt
			}
		}else {
			retVal=(material[0]-material[1]);//+(double)(Math.random()*0.1);
		}
		return SIGN_BY_COLOR[colorAtTurn]*retVal;
	}

	public double evaluateClassic(int i) {
		double retVal;
		if(i==-1) {
			//for current color
			if(isCheck()) {
				retVal= (10_000-(level));
			}else {
				retVal= 1.5;//Patt
			}
		} else {
			Move move = this.getMove(i);
			retVal=(material[1]-material[0]);//+(double)(Math.random()*0.1);
			int delta = 0;
			if(this.enPassanteMask!=EMPTY_MASK && Long.bitCount(this.enPassanteMask)== move.getEnPassanteSquare()) {
				delta = 1;
			}else if(move.isPromotion()){
				delta = NAIVE_MATERIAL_COUNT[move.getPromotePieceType()>>>7]-NAIVE_MATERIAL_COUNT[PIECE_TYPE_PAWN];
			}else {
				int typeColor = fields[move.getNewPos()];
				if(typeColor!=-1) {
					delta = NAIVE_MATERIAL_COUNT[typeColor>>>7];
				}
			}
			delta*=SIGN_BY_COLOR[colorAtTurn];
			retVal-=delta;
		}
		return SIGN_BY_COLOR[colorAtTurn]*(retVal+(Math.random()*0.01));
	}
	/* Moves are only simulated
	public double evaluate(int i) {
		double retVal;
		if(i==-1) {
			//for current color
			if(isCheck()) {
				retVal= (10_000-(level));
			}else {
				retVal= 1.5;//Patt
			}
		} else {
			Move move = this.getMove(i);
			int oldTypeColor =fields[move.getOldPos()];
			int newTypeColor =fields[move.getNewPos()];
			fields[move.getOldPos()]=-1;
			fields[move.getNewPos()]=oldTypeColor;
			
			
			int[] pieces = new int[35];
			int[] squares=new int[35];
			int counter =2;
			for(int j=0;j<64;j++) {
				int typeColor = fields[j];
				if(typeColor!=-1) {
					int nnuePiece = NNUE_PIECE_LOOKUP[typeColor];
					if(nnuePiece == 1) {
						pieces[0]=1;
						squares[0]=j;
					}else if(nnuePiece == 7) {
						pieces[1]=7;
						squares[1]=j;
					}else {
						pieces[counter]= nnuePiece;
						squares[counter]=j;
						counter++;
					}
				}
			}
			//System.out.println(Arrays.toString(pieces));
			
			fields[move.getOldPos()]=oldTypeColor;
			fields[move.getNewPos()]=newTypeColor;
			//@TODO: enpassante, promotion, rochade
			
			//if(!nnue.testInitialFEN(this.colorAtTurn, pieces, squares)) {
			//	System.exit(-1);
			//}
			retVal= nnue.nnue_evaluate(OTHER_COLOR[this.colorAtTurn], pieces, squares);
			//System.out.println("NNUE"+retVal);		
			
		}
		return SIGN_BY_COLOR[colorAtTurn]*retVal; //Needed because of negamax 
	}*/
	
	
	public double evaluate(int i) {
		double retVal;
		if(i==-1) {
			//for current color
			if(isCheck()) {
				retVal= SIGN_BY_COLOR[colorAtTurn]*((1_000_000-(level))*-1);
			}else {
				retVal= SIGN_BY_COLOR[colorAtTurn]*100*-1;//Patt
			}
		} else {
			
			
			int[] pieces = new int[35];
			int[] squares=new int[35];
			int counter =2;
			for(int j=0;j<64;j++) {
				int typeColor = fields[j];
				if(typeColor!=-1) {
					int nnuePiece = NNUE_PIECE_LOOKUP[typeColor];

					
					if(nnuePiece == 1) {
						pieces[0]=1;
						squares[0]=j;
					}else if(nnuePiece == 7) {
						pieces[1]=7;
						squares[1]=j;
					}else {
						pieces[counter]= nnuePiece;
						squares[counter]=j;
						counter++;
					}
				}
			}
			if(this._enpPos!=-1) {
				squares[counter]= Long.bitCount(this.enPassanteMask);
			}
			retVal= nnue.nnue_evaluate(OTHER_COLOR[this.colorAtTurn], pieces, squares)*-1;
			//int retVal2 = nnue2.nnue_evaluate_incremental(OTHER_COLOR[this.colorAtTurn], pieces, squares)*-1;
			
			/*String fenStr = fen.getFEN(this);
			int retVal2=nnue.nnue_evaluate_fen(fenStr);
			
			if(retVal!=retVal2) {
				System.out.println("Mismatch:"+retVal +" vs "+retVal2);
			}
			//System.out.println("\n\n");*/
		}
		return SIGN_BY_COLOR[colorAtTurn]*retVal; //Needed because of negamax 
	}
	Fen fen = new Fen();
}
