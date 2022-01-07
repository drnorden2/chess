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
	public final BaseLiner bl = new BaseLiner(3, 2000, 1000, depth, 1000);
	MagicNumberFinder mnf = new MagicNumberFinder();
	private ArrayStack<ContextLevel> contextLevels = new ArrayStack<ContextLevel>(new ContextLevel[depth+2]);

	public final BLVariableInt[] fields = new BLVariableInt[64];
	public final BLVariableLong[] callBacks = new BLVariableLong[64];
	public final BLVariableLong[] moveMasks = new BLVariableLong[64];
	public final BLVariableLong[] tCallBacks = new BLVariableLong[64];
	
	public final BLVariableLong[] allOfOneColor = new BLVariableLong[2];//@todo WTF -stack
	public final BLVariableLong[] kings = new BLVariableLong[2];
	public final BLVariableLong[] pawns = new BLVariableLong[2];
	
	public final BLVariableLong[] mLeft = new BLVariableLong[2];
	public final BLVariableLong[] mRight = new BLVariableLong[2];
	public final BLVariableLong[] mOneUp = new BLVariableLong[2];
	public final BLVariableLong[] mTwoUp = new BLVariableLong[2];

	
	
	public final BLVariableLong[] correctors = new BLVariableLong[2];

	public final BLVariableLong untouched;
	public final BLVariableInt moveCount [] = new BLVariableInt[2];
	public final BLVariableLong enPassanteMask;//@todo WTF -stack

	public BBAnalyzer analyzer = new BBAnalyzer(this);
	public LookUp lookUp = new LookUp();

	private int colorAtTurn = COLOR_WHITE;



	
	public BBPosition() {

		for (int i = 0; i < 64; i++) {
			tCallBacks[i] = new BLVariableLong(bl, 0L);
			fields[i] = new BLVariableInt(bl, -1);
			moveMasks[i] = new BLVariableLong(bl, 0L);
			callBacks[i] = new BLVariableLong(bl, 0L);		 					 
		}
		
		
		enPassanteMask = new BLVariableLong(bl, 0);
		untouched = new BLVariableLong(bl, 0L);
		
		for (int i = 0; i < 2; i++) {
			allOfOneColor[i] = new BLVariableLong(bl, 0L);
			correctors[i] = new BLVariableLong(bl, 0L);
			moveCount[i]  = new BLVariableInt(bl,0);
			kings[i] = new BLVariableLong(bl, 0L);
			pawns[i] = new BLVariableLong(bl, 0L);
			mLeft[i] = new BLVariableLong(bl, 0L);
			mRight[i] = new BLVariableLong(bl, 0L);
			mOneUp[i] = new BLVariableLong(bl, 0L);
			mTwoUp[i] = new BLVariableLong(bl, 0L);
		}
		
		for(int i=0;i<depth+2;i++) {
			ContextLevel allMoves = new ContextLevel();
			this.contextLevels.add(allMoves);
		}
	}



  	public void calcNewMoves(int color) {
  		ContextLevel oldList = this.contextLevels.get(getLevel()-2);
  		ContextLevel list = this.contextLevels.get(getLevel());
  		list.reInit();
  		long bits = this.allOfOneColor[color].get();
  		int retVal = Long.bitCount(bits);
  		for(int i=0;i<retVal;i++) {
  			int pos = Long.numberOfTrailingZeros(bits);
			bits &= bits - 1;
			long moveMask = this.moveMasks[pos].get();
			if(moveMask==0) {
				continue;
			}
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
		//System.out.println("Moves for the record:"+counter);
  		list.setLimit(moveCount[colorAtTurn].get());
  		//to identify the pawns affected in next round
  						
  	}


	@Override
	public void initialEval() {
		ContextLevel context = this.contextLevels.get(getLevel()-1);

		for (int i = 0; i < 2; i++) {
			long pawn = pawns[i].get();
			long own = this.allOfOneColor[i].get()&~pawn;
			int retVal = Long.bitCount(own);
			for(int j=0;j<retVal;j++) {
				int pos = Long.numberOfTrailingZeros(own);
				own &= own- 1;			
				int typeColor = fields[pos].get();
				context.allOfOneColor[COLOR_WHITE] = this.allOfOneColor[COLOR_WHITE].get();// @todo WTF too expensive
				context.allOfOneColor[COLOR_BLACK] = this.allOfOneColor[COLOR_BLACK].get();
				context.occ = context.allOfOneColor[COLOR_WHITE]| context.allOfOneColor[COLOR_BLACK];
				context.notOcc = ~(context.occ);
				context.ntchd = untouched.get();
				// if there is no combination remaining set to 0
				if(context.ntchd!=0 && ((context.ntchd & MASK_NOT_ALL_ROOKS) ==0 || (context.ntchd & MASK_ALL_ROOKS)==0)) {
					context.ntchd = 0L;
					untouched.set(0L);
				}
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
					this.tCallBacks[cb].setBit(pos);
					mask|=1L<<cb;
				}
				if(file!=_H) {
					int cb = i==COLOR_WHITE?pos+9:pos-7;
					this.tCallBacks[cb].setBit(pos);
					mask|=1L<<cb;
				}
				this.callBacks[pos].set(mask);
			}
			
			context.pawns[COLOR_WHITE]=this.pawns[COLOR_WHITE].get();
			context.pawns[COLOR_BLACK]=this.pawns[COLOR_BLACK].get();
			updatePawnPseudoMoves(i);
		}
		checkRochade(context,null); 

		//checkMoves() ;
	}

	private void removePseudoMoves(int typeColor, int pos) {
		int color = typeColor >> 6 & 1;
		//remove moves
		this.moveCount[color].subtraction(Long.bitCount(moveMasks[pos].getAndSet(0L)));;
		//remove attacks
		long oldCBs = callBacks[pos].getAndSet(0L);
		int retVal = Long.bitCount(oldCBs);
		for(int i=0;i<retVal;i++) {
			this.tCallBacks[Long.numberOfTrailingZeros(oldCBs)].toggleBit(pos);
			oldCBs &= oldCBs - 1;			
		}
	}

	
	
	private void updateNonPawnPseudoMoves(int typeColor, int pos,boolean isAdded) {
		int color = typeColor >> 6 & 1;
		int type = typeColor >>7;
		
		
		ContextLevel context = this.contextLevels.get(getLevel()-1);
		long callbacks = 0L;
		long moves = 0L;
		long own = this.allOfOneColor[color].get();// @todo WTF too expensive
		boolean needsCBUpdate=true;
		
		if(type<=PIECE_TYPE_KNIGHT) {
			callbacks = lookUp.getMoveMask(pos + typeColor);
			moves = callbacks & ~own;
			needsCBUpdate=isAdded;
		}else {
			switch (type) {
			case PIECE_TYPE_BISHOP:{
				callbacks = mnf.getBishopAttacks(pos, context.occ);
				moves = callbacks & ~own;
				break;
			}
			case PIECE_TYPE_ROOK:{
				callbacks = mnf.getRookAttacks(pos, context.occ);
				moves = callbacks & ~own;
			
				break;
			}
			case PIECE_TYPE_QUEEN: {
				callbacks = mnf.getBishopAttacks(pos, context.occ);
				callbacks|= mnf.getRookAttacks(pos, context.occ);
				moves = (callbacks) & ~own;
				break;
			}
			}
		}
		if(needsCBUpdate) {
			long toggleCallbacks = this.callBacks[pos].get() ^ callbacks;
			int retVal = Long.bitCount(toggleCallbacks);
			if(retVal>0) {
				for(int i=0;i<retVal;i++) {
					int curCB = Long.numberOfTrailingZeros(toggleCallbacks);
					this.tCallBacks[curCB].toggleBit(pos);
					toggleCallbacks &= toggleCallbacks - 1;			
				}
				this.callBacks[pos].set(callbacks);
			}
		}
		
		long oldMoves = this.moveMasks[pos].get() ;
		if(oldMoves!=moves) {
			this.moveMasks[pos].set(moves);
			int delta = Long.bitCount(moves) - Long.bitCount(oldMoves);
			if(delta!=0) {
				this.moveCount[color].addition(delta);
			}
		}
	}
	int counta =0;
	private void updatePawnPseudoMoves(int color) {
		ContextLevel context = this.contextLevels.get(getLevel()-1);
		int otherColor = OTHER_COLOR[color];
		long other = context.allOfOneColor[otherColor];
		long ownPawns = context.pawns[color];
		
		long mLeft,mRight,mOneUp,mTwoUp ,delta;
		if(color==COLOR_WHITE) {
			//out(ownPawns);

			other|=(this.enPassanteMask.get() & MASK_6_RANK);
			//out(other);
			mLeft= (ownPawns <<DIR_UP_LEFT) & MASK_NOT_H_FILE&other;
			//out(mLeft);
			mRight=(ownPawns <<DIR_UP_RIGHT) & MASK_NOT_A_FILE&other;
			//out(mRight);
			mOneUp = (ownPawns << DIR_UP)& context.notOcc ;
			mTwoUp = ((mOneUp  & context.notOcc & MASK_3_RANK) << DIR_UP)& context.notOcc;
			
			//out(mOneUp);
			//out(mTwoUp);
		
			delta = (mLeft^context.mLeftOld[color])>>DIR_UP_LEFT;
			//out(context.mLeftOld[color]);
			//out(delta);
			delta |= (mRight^context.mRightOld[color])>>DIR_UP_RIGHT;
			//out(context.mRightOld[color]);
			//out(delta);
			delta |= (mOneUp^context.mOneUpOld[color])>>DIR_UP;
			//out(delta);
			delta |= (mTwoUp^context.mTwoUpOld[color])>>DIR_2_UP;

			// only pawns that existed in the first place
			delta&=ownPawns;
			
			//out(delta);
			
			int retVal = Long.bitCount(delta);
			int moveCountDelta =0;
			if(retVal>0) {
				for(int i=0;i<retVal;i++) {
					int pos = Long.numberOfTrailingZeros(delta); 
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
						mask|= last>>DIR_UP;
						mask|= last>>DIR_UP;
						mask|= last>>DIR_UP;
					}
					long oldMask = this.moveMasks[pos].getAndSet(mask);
					//out(oldMask);
					moveCountDelta+=Long.bitCount(mask)- Long.bitCount(oldMask);
				}
			}
			this.mLeft[color].set(mLeft);
			this.mRight[color].set(mRight);
			this.mOneUp[color].set(mOneUp);
			this.mTwoUp[color].set(mTwoUp);
			//= Long.bitCount(mOneUp|mTwoUp)+Long.bitCount(mLeft)+Long.bitCount(mRight);
			this.moveCount[color].addition(moveCountDelta);

		}else {
			
			
			//out(ownPawns);
			other|=(this.enPassanteMask.get() & MASK_3_RANK);
			//out(other);
			mLeft= (ownPawns >>DIR_UP_LEFT) & MASK_NOT_A_FILE&other;
			//out(mLeft);
			mRight=(ownPawns >>DIR_UP_RIGHT) & MASK_NOT_H_FILE&other;
			//out(mRight);
			mOneUp = (ownPawns >> DIR_UP) & context.notOcc ;
			//out(mOneUp);
			mTwoUp = (((mOneUp & MASK_6_RANK) >> DIR_UP)& context.notOcc ) ;
			//out(mTwoUp);
			delta = (mLeft^context.mLeftOld[color])<<DIR_UP_LEFT;
			//out(context.mLeftOld[color]);
			//out(delta);
			delta |= (mRight^context.mRightOld[color])<<DIR_UP_RIGHT;
			//out(context.mRightOld[color]);
			//out(delta);
			delta |= (mOneUp^context.mOneUpOld[color])<<DIR_UP;
			//out(context.mOneUpOld[color]);
			//out(delta);
			delta |= (mTwoUp^context.mTwoUpOld[color])<<DIR_2_UP;
		
			// only pawns that existed in the first place
			delta&=ownPawns;
			
			//out(delta);
			
			int retVal = Long.bitCount(delta);
			int moveCountDelta =0;
			if(retVal>0) {
				for(int i=0;i<retVal;i++) {
					int pos = Long.numberOfTrailingZeros(delta); 
					delta &= delta - 1;			
					long mask = this.lookUp.getMoveMask(PIECE_TYPE_X_PAWN[color] +pos);
					//out(mask);
					long pOneMask =mask&mOneUp &MASK_ONE_UP_AND_DOWN[pos];
					long pTwoMask =mask&mTwoUp &~MASK_ONE_UP_AND_DOWN[pos];
					//out(pMask);
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
					long oldMask = this.moveMasks[pos].getAndSet(mask);
					moveCountDelta+=Long.bitCount(mask)- Long.bitCount(oldMask);
				}
				
			}
			this.mLeft[color].set(mLeft);
			this.mRight[color].set(mRight);
			this.mOneUp[color].set(mOneUp);
			this.mTwoUp[color].set(mTwoUp);
			//= Long.bitCount(mOneUp|mTwoUp)+Long.bitCount(mLeft)+Long.bitCount(mRight);
			this.moveCount[color].addition(moveCountDelta);
		}
	}

	
	@Override
	public void setMove(int index) {
		Move move = getMove(index);
	
		bl.startNextLevel();
		ContextLevel context = this.contextLevels.get(getLevel()-1);
		context.pawns[COLOR_WHITE]=this.pawns[COLOR_WHITE].get();
		context.pawns[COLOR_BLACK]=this.pawns[COLOR_BLACK].get();
		context.enpMask = this.enPassanteMask.getAndSet(0);//@todo WTF could happen on level -level
		context.enpPos= Long.numberOfTrailingZeros(context.enpMask);
	
		int color = this.colorAtTurn;
		int otherColor = OTHER_COLOR[color];
		setMove(move,context);
		this.updatePawnPseudoMoves(color);
		this.updatePawnPseudoMoves(otherColor);
 		if(context.ntchd!=0) {
			checkRochade(context,move); 
		}			
		this.pawns[COLOR_WHITE].set(context.pawns[COLOR_WHITE]);
		this.pawns[COLOR_BLACK].set(context.pawns[COLOR_BLACK]);
		checkLegalMoves();
		this.colorAtTurn = OTHER_COLOR[colorAtTurn];
	}
	
	
	/*	
	private void checkMoves() {
		int[] testCount=new int[2];
		for(int i=0;i<64;i++) {
			int typeColor = this.fields[i].get();
			if(typeColor!=-1) {
				long mask = this.moveMasks[i].get();
				int count = Long.bitCount(mask);
				//System.out.println("Pos("+i+"):"+count +" Color is btw:"+(typeColor>>6&1));
				testCount[typeColor>>6&1]+=count;
			}
		}
		int[] refCount = new int[]{this.moveCount[0].get(),this.moveCount[1].get()};
		int totalRef = refCount[0]+refCount[1];
		int totalTest = testCount[0]+testCount[1];
		if(totalRef!=totalTest) {
			System.out.println("WTF:Inventur total error:"+totalTest +"!="+totalRef);
		}
		
		
		if(testCount[0]!=refCount[0]) {
			System.out.println("WTF:Inventur color 0 error:"+testCount[0] +"!="+refCount[0] );
			throw new RuntimeException("WFT");
		}
		if(testCount[1]!=refCount[1]) {
			System.out.println("WTF:Inventur color 1 error:"+testCount[1] +"!="+refCount[1] );
			throw new RuntimeException("WTF");
		}
		
	}*/
	int movecount =0;
	private void setMove(Move move,ContextLevel context ) {
		/*
		
		if(movecount==2152) {
			System.out.println("WTF");
		}
		
		System.out.println("Move("+move+"):"+(movecount++));
		
		//checkMoves() ;
		*/
		//System.out.println("Move("+move+"):"+(movecount++));
		
		
		
		
		
		
		context.ntchd = untouched.get();
		if(context.ntchd!=0) {
			if(((context.ntchd & MASK_NOT_ALL_ROOKS) ==0 || (context.ntchd & MASK_ALL_ROOKS)==0)) {
				context.ntchd = 0L;
				untouched.set(0L);
			}else {
				//out(moveMasks[_E1].get());
				context.oldCastleMovesKQ = moveMasks[_E1].get()& MASK_CASTLE_KING_KQkq;
				//out(context.oldCastleMovesKQ);
				
				//out(moveMasks[_E8].get());
				context.oldCastleMoveskq = moveMasks[_E8].get()& MASK_CASTLE_KING_KQkq;
				//out(context.oldCastleMoveskq);
				
				context.oldCastleMovesKQCount = Long.bitCount(context.oldCastleMovesKQ);
				context.oldCastleMoveskqCount = Long.bitCount(context.oldCastleMoveskq);
			}
		}
		
		
		//@todo use masks for move
		int oldPos = move.getOldPos();
		int newPos = move.getNewPos();
		long oldPosMask = 1L<<oldPos;
		long newPosMask = 1L<<newPos;
		long moveMask=oldPosMask|newPosMask;
		int color = this.colorAtTurn;
		int otherColor = OTHER_COLOR[color];
		int pieceType = move.getPieceType();
		
		long cbsNewPosEmptyBefore=0;
		long cbsNewPosReplace=0;		
		long cbsOldPos=0;
		long cbsEnpPawnRemoved=0; 				

		
		
/*E*/	int otherTypeColor = fields[newPos].get();
		if(otherTypeColor!=-1) {
			//alter moves(ok), moveCount(ok), allOfOneColor(ok), fields(later), callbacks(ok), tcallbacks(ok) and if neccessary pawns!(ok)
/*!!!*/		removePseudoMoves(otherTypeColor,newPos);
			this.allOfOneColor[otherColor].XOR(newPosMask);
			if(otherTypeColor>>7==PIECE_TYPE_PAWN) {
				context.pawns[otherColor]^=newPosMask;
			}
			cbsNewPosReplace= this.tCallBacks[newPos].get();
		}else {
			cbsNewPosEmptyBefore = this.tCallBacks[newPos].get();
		}
		
		
		
		int typeColor = fields[oldPos].getAndSet(-1);

/*!!!*/	removePseudoMoves(typeColor,oldPos);

		if(move.isPromotion()) {
			typeColor = move.getPromotePieceType();
			pieceType = typeColor>>7;
			context.pawns[color]&=~oldPosMask;
		}

		//alter moves(ok), moveCount(ok), allOfOneColor(ok), fields(later), callbacks(ok), tcallbacks(ok) and if neccessary pawns!(ok)
		/*u*/	fields[newPos].set(typeColor);
		// update occupancy
		/*u*/	this.allOfOneColor[color].XOR(moveMask);
		// update moves
		
		cbsOldPos = this.tCallBacks[oldPos].get();
		if(pieceType==PIECE_TYPE_KING) {
			this.kings[color].set(1L<<newPos);
		}
		if(pieceType==PIECE_TYPE_PAWN) {
			context.pawns[color]^=moveMask;
		}
		
		if(context.enpMask!=0L)	{
			if(move.getEnPassanteSquare()==context.enpPos) {
				int enpPawnPos = move.getEnPassantePawnPos();
				cbsEnpPawnRemoved = this.tCallBacks[enpPawnPos].get(); 				
				int enpTypeColor = fields[enpPawnPos].getAndSet(-1);
/*u*/			this.allOfOneColor[otherColor].unsetBit(enpPawnPos);// might be empty anyways
				context.pawns[otherColor]&=~(1L<<enpPawnPos);
				removePseudoMoves(enpTypeColor,enpPawnPos);
			}
		}

			
		if(move.isTwoSquarePush()) {
			int enpSquare= move.getEnPassanteSquare();
			this.enPassanteMask.set(1L<<enpSquare);//@TBD always set (geht mit stack)
		}

		
		context.allOfOneColor[COLOR_WHITE] = this.allOfOneColor[COLOR_WHITE].get();// @todo WTF too expensive
		context.allOfOneColor[COLOR_BLACK] = this.allOfOneColor[COLOR_BLACK].get();
		
		context.occ = context.allOfOneColor[COLOR_WHITE]|context.allOfOneColor[COLOR_BLACK];
		context.notOcc = ~(context.occ);
				
		if(pieceType!=PIECE_TYPE_PAWN) {
			context.move = move; 
			this.updateNonPawnPseudoMoves(typeColor, newPos,true);
		}else {
			//for Pawns just add the two attackers
			int[] attacks = move.getPawnNewAttacks();
			for(int i=0;i<attacks.length;i++) {
				this.tCallBacks[attacks[i]].setBit(newPos);
			}
			

			this.callBacks[newPos].set(move.getPawnNewAttackMask());
		}
		
		if(context.ntchd!=0) {
			// if there is no combination remaining set to 0
			
			if(move.isRochadeDisabler()) { 
				if(move.isRochade()) {
					this.setMove(move.getRookMove(),context);
				}
				// update fields
				long not_moveMask =~moveMask;
				this.untouched.AND(not_moveMask);
				context.ntchd&=not_moveMask;
			}
		}
		long cbs = cbsNewPosEmptyBefore|cbsNewPosReplace |cbsOldPos|cbsEnpPawnRemoved;
		cbs &= (context.occ)&~(moveMask)&~(context.pawns[COLOR_WHITE]|context.pawns[COLOR_BLACK]);//@todo occ var
		long bits = cbs;
		int retVal = Long.bitCount(bits);
		for(int i=0;i<retVal;i++) {
			int cbPos = Long.numberOfTrailingZeros(bits);
			bits &= bits - 1;				
			int cbTypeColor = fields[cbPos].get();
			cbscounter++;
			this.updateNonPawnPseudoMoves(cbTypeColor, cbPos,false);
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
		this.contextLevels.get(getLevel()).resetIterator();
		return moveCount[colorAtTurn].get();
	}
	
	public Move getMove(int index) {
		if(index==0) {
			
			// once for all moves of this level
			this.calcNewMoves(this.colorAtTurn);
			ContextLevel context = this.contextLevels.get(getLevel());
			//@TODO WTF
			//context.enpMask= this.enPassantePos.getAndSet(0L);
			for(int i=0;i<2;i++) {
				context.mLeftOld[i]= mLeft[i].get();
				context.mRightOld[i]= mRight[i].get();
				context.mOneUpOld[i]= mOneUp[i].get();
				context.mTwoUpOld[i]= mTwoUp[i].get();
			}
			
			/*
			context.pawnMovesOld= Long.bitCount(context.mOneUpOld|context.mTwoUpOld)
	  				+Long.bitCount(context.mLeftOld)
	  				+Long.bitCount(context.mRightOld);
	  		*/			
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
		int typeColor =(type * 2 + color) << 6;
		fields[pos].set(typeColor);
		allOfOneColor[color].setBit(pos);
		if(type==PIECE_TYPE_KING) {
			this.kings[color].OR(1L<<pos);
		}
		if(type==PIECE_TYPE_PAWN) {
			this.pawns[color].OR(1L<<pos);
		}
		
	}

	@Override
	public void setUntouched(int rank, int file) {
		untouched.setBit(getPosForFileRank(file, rank));
	}

	@Override
	public void setEnPassantePos(int enpassantePos) {
		this.enPassanteMask.set(enpassantePos);
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
		for (int j = 0; j < 64; j++) {
			attacks[j] = (int)(Long.bitCount(tCallBacks[j].get()&own));
		}
		return attacks;
	}
	
	private void checkRochade(ContextLevel context, Move move) {
		
		long oldE1Mask=this.moveMasks[_E1].get();
		long oldCastleMovesKQ=oldE1Mask&MASK_CASTLE_KING_KQkq;
		int oldCastleMovesKQCount= Long.bitCount(oldCastleMovesKQ);
		
		//out(this.allOfOneColor[0].get());
		long oldE8Mask=this.moveMasks[_E8].get();
		long oldCastleMoveskq=oldE8Mask&MASK_CASTLE_KING_KQkq;;
		int oldCastleMoveskqCount= Long.bitCount(oldCastleMoveskq);
		
		
		long newCastleMovesKQ=0;
		long newCastleMoveskq=0;
		
		//@TODO WTF: Combine E1 and combine with nonPawn stuff
		
		//remove rochade protagonists in occ if touched
		long ntchdOcc= context.occ & MASK_NOT_ALL_ROOKS_KINGS |context.ntchd;
		//out(ntchdOcc);
		//out(context.occ );
		//out(context.ntchd);
		if((context.ntchd & MASK_E1)!=0L) {
			//ntchdOcc and MASK_E1_H1 should be identical in the range of MASK_CASTLE_ALL_K
			if (((ntchdOcc^MASK_E1_H1)& MASK_CASTLE_ALL_K)==0L) {
				newCastleMovesKQ|= MASK_CASTLE_KING_K;
			}
			if (((ntchdOcc^MASK_E1_A1)& MASK_CASTLE_ALL_Q)==0L) {
				newCastleMovesKQ|= MASK_CASTLE_KING_Q;
			}
			long mask = (oldE1Mask&MASK_NOT_CASTLE_KING_KQkq)|newCastleMovesKQ;
			//out(mask);
			//this.conistency();
			//out(this.moveMasks[_E1].get());
			//out(mask);	
			this.moveMasks[_E1].set(mask);//we sometimes need to repair this w. king was updated
			
				//out(newCastleMovesKQ);
				//out(this.moveMasks[_E1].get());
			if(oldCastleMovesKQ !=newCastleMovesKQ) {
				int delta = Long.bitCount(newCastleMovesKQ)-oldCastleMovesKQCount;
				if(delta!=0) {
					this.moveCount[COLOR_WHITE].addition(delta);
				}
			}
			//this.conistency();
		}
		
		
		if((context.ntchd & MASK_E8)!=0L) {
			if (((ntchdOcc^MASK_E8_H8)& MASK_CASTLE_ALL_k)==0L) {
				newCastleMoveskq|= MASK_CASTLE_KING_k;
				//out(newCastleMoveskq);
				//out(ntchdOcc^MASK_E8_H8);
				
			}
			if (((ntchdOcc^MASK_E8_A8)& MASK_CASTLE_ALL_q)==0L) {
				newCastleMoveskq|= MASK_CASTLE_KING_q;
			}
			//boolean change=false;
			//out(newCastleMovesKQ);
			//out(MASK_NOT_CASTLE_KING_KQkq);
			//out(this.moveMasks[_E1].get());
			
			//out(oldE8Mask);
			//out(newCastleMoveskq);
			//out(MASK_NOT_CASTLE_KING_KQkq);
			long mask = (oldE8Mask&MASK_NOT_CASTLE_KING_KQkq)|newCastleMoveskq;
			//out(mask);
			this.moveMasks[_E8].set(mask);//we sometimes need to repair this w. king was updated
			if(oldCastleMoveskq !=newCastleMoveskq) {
				int delta = Long.bitCount(newCastleMoveskq)-oldCastleMoveskqCount;
				if(delta!=0) {
					this.moveCount[COLOR_BLACK].addition(delta);
				}
			}
		}	
		//this.conistency();
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
	static int cbscounter=0;
	
	public static void cbsCount(){
		System.out.println("CBS Coutner!"+cbscounter);
	}
	
	
	
	private void conistencyX() {
	
		for(int color=0;color<2;color++) {
			int ref = moveCount[color].get();
			long bits = allOfOneColor[color].get();
	  		int retVal = Long.bitCount(bits);
	  		int total = 0;
	  		for(int i=0;i<retVal;i++) {
	  			int pos = Long.numberOfTrailingZeros(bits);
				bits &= bits - 1;
				long moveMask = moveMasks[pos].get();
				int retVal2 = Long.bitCount(moveMask);
		  		total +=retVal2 ;		  		
			}
	  		if(ref!=total) {
	  			throw new RuntimeException(total+"!="+ref);
	  		}
		}

	}
		
}
