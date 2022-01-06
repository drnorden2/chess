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
	public final BLVariableInt moveCount[] = new BLVariableInt[2];
	public final BLVariableLong enPassantePos;;//@todo WTF -stack

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
		
		
		enPassantePos = new BLVariableLong(bl, 0);
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
				updateNonPawnPseudoMoves(typeColor, pos);
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

	
	private void updateNonPawnPseudoMoves(int typeColor, int pos) {
		int color = typeColor >> 6 & 1;
		
		ContextLevel context = this.contextLevels.get(getLevel()-1);
		long callbacks = 0L;
		long moves = 0L;
		long own = this.allOfOneColor[color].get();// @todo WTF too expensive
		

		switch (typeColor) {
		case PIECE_TYPE_BLACK_BISHOP:
		case PIECE_TYPE_WHITE_BISHOP: {
			callbacks = mnf.getBishopAttacks(pos, context.occ);
			moves = callbacks & ~own;
			break;
		}
		case PIECE_TYPE_BLACK_ROOK:
		case PIECE_TYPE_WHITE_ROOK: {
			callbacks = mnf.getRookAttacks(pos, context.occ);
			moves = callbacks & ~own;
			break;
		}
		case PIECE_TYPE_BLACK_QUEEN:
		case PIECE_TYPE_WHITE_QUEEN: {
			callbacks = mnf.getBishopAttacks(pos, context.occ);
			callbacks|= mnf.getRookAttacks(pos, context.occ);
			moves = (callbacks) & ~own;
			break;
		}
		case PIECE_TYPE_WHITE_KNIGHT:
		case PIECE_TYPE_BLACK_KNIGHT: {
			callbacks = lookUp.getMoveMask(pos + typeColor);
			moves = callbacks & ~own;
			break;
		}
		
		case PIECE_TYPE_WHITE_KING: {
			callbacks = lookUp.getMoveMask(pos + typeColor);
			moves = callbacks & ~own;
			break;	
		}
		case PIECE_TYPE_BLACK_KING: {
			callbacks = lookUp.getMoveMask(pos + typeColor);
			moves = callbacks & ~own;
			break;	
		}
		}
		
		long toggleCallbacks = this.callBacks[pos].get() ^ callbacks;
		
		int retVal = Long.bitCount(toggleCallbacks);
		if(retVal>0) {
			for(int i=0;i<retVal;i++) {
				this.tCallBacks[Long.numberOfTrailingZeros(toggleCallbacks)].toggleBit(pos);
				toggleCallbacks &= toggleCallbacks - 1;			
			}
			this.callBacks[pos].set(callbacks);
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
	
	private void updatePawnPseudoMoves(int color) {
		ContextLevel context = this.contextLevels.get(getLevel()-1);
		int otherColor = OTHER_COLOR[color];
		long other = this.allOfOneColor[otherColor].get();
		long ownPawns = context.pawns[color];
		
		long mLeft,mRight,mOneUp,mTwoUp,mAllUp,delta;
		if(color==COLOR_WHITE) {
			other|=(this.enPassantePos.get() & MASK_6_RANK);
			mLeft= (ownPawns <<DIR_UP_LEFT) & MASK_NOT_H_FILE&other;
			mRight=(ownPawns <<DIR_UP_RIGHT) & MASK_NOT_A_FILE&other;
			mOneUp = (ownPawns << DIR_UP)& context.notOcc ;
			mTwoUp = ((mOneUp  & context.notOcc & MASK_3_RANK) << DIR_UP)& context.notOcc;
			mAllUp = mOneUp|mTwoUp;
			
			//out(ownPawns);
			//out(mOneUp);
			//out(context.mOneUpOld[color]);
		
			delta = (mLeft^context.mLeftOld[color])>>DIR_UP_LEFT;
			delta |= (mRight^context.mRightOld[color])>>DIR_UP_RIGHT;
			delta |= (mOneUp^context.mOneUpOld[color])>>DIR_UP;
			delta |= (mTwoUp^context.mTwoUpOld[color])>>DIR_2_UP;
			//out(delta);
			if((mAllUp & MASK_8_RANK)!=0L)  {
				mAllUp|= mAllUp>>DIR_UP;
				mAllUp|= mAllUp>>DIR_UP;
				mAllUp|= mAllUp>>DIR_UP;
			}
			if((mLeft & MASK_8_RANK)!=0L)  {
				mLeft|= mLeft>>DIR_UP;
				mLeft|= mLeft>>DIR_UP;
				mLeft|= mLeft>>DIR_UP;
			}
			if((mRight & MASK_8_RANK)!=0L)  {
				mRight|= mRight>>DIR_UP;
				mRight|= mRight>>DIR_UP;
				mRight|= mRight>>DIR_UP;
			}

		}else {
			//out(ownPawns);
			other|=(this.enPassantePos.get() & MASK_3_RANK);
			//out(other);
			mLeft= (ownPawns >>DIR_UP_LEFT) & MASK_NOT_H_FILE&other;
			//out(mLeft);
			mRight=(ownPawns >>DIR_UP_RIGHT) & MASK_NOT_A_FILE&other;
			//out(mRight);
			mOneUp = (ownPawns >> DIR_UP) & context.notOcc ;
			//out(mOneUp);
			mTwoUp = (((mOneUp & MASK_6_RANK) >> DIR_UP)& context.notOcc ) ;
			//out(mTwoUp);
			mAllUp = mOneUp|mTwoUp;
			//out(mAllUp);
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
			//out(context.mTwoUpOld[color]);
			//out(delta);
			//@TODO WTF IDEA=> implement in Magic bitboard Lookup
			if((mAllUp & MASK_1_RANK)!=0L)  {
				mAllUp|= mAllUp<<DIR_UP;
				mAllUp|= mAllUp<<DIR_UP;
				mAllUp|= mAllUp<<DIR_UP;
			}
			if((mLeft & MASK_1_RANK)!=0L)  {
				mLeft|= mLeft<<DIR_UP;
				mLeft|= mLeft<<DIR_UP;
				mLeft|= mLeft<<DIR_UP;
			}
			if((mRight & MASK_1_RANK)!=0L)  {
				mRight|= mRight<<DIR_UP;
				mRight|= mRight<<DIR_UP;
				mRight|= mRight<<DIR_UP;
			}
		}
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
				long pMask =mask&mAllUp&MASK_X_FILE_FOR_POS[pos];
				//out(pMask);
				long aMask=mask&MASK_NOT_X_FILE_FOR_POS[pos];
				//out(aMask);
				mask =pMask|aMask&mLeft|aMask&mRight;
				
				
				//out(mask);
				/*if(pos==24) {
					System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
					System.out.println("For Pos 24: TypeColor:"+fields[24]+"");
					out(mask);
					System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				}*/
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
  		
	}

	
	@Override
	public void setMove(int index) {
		Move move = getMove(index);
	
		bl.startNextLevel();
		ContextLevel context = this.contextLevels.get(getLevel()-1);
		context.pawns[COLOR_WHITE]=this.pawns[COLOR_WHITE].get();
		context.pawns[COLOR_BLACK]=this.pawns[COLOR_BLACK].get();
		setMove(move,context);		
		this.pawns[COLOR_WHITE].set(context.pawns[COLOR_WHITE]);
		this.pawns[COLOR_BLACK].set(context.pawns[COLOR_BLACK]);
		checkLegalMoves();
		this.colorAtTurn = OTHER_COLOR[colorAtTurn];
	//	System.out.println(this);
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
		//checkMoves() ;
		
		/*
		if(move.getNotation().equals("a8a7")) {
			System.out.println("WTF");
			out(this.tCallBacks[41].get());			
			out(context.occ);
			
		}
		
		
		
		if(movecount==48) {
			System.out.println("WTF");
		}
		
		System.out.println("Move("+move+"):"+movecount++);
		*/
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
/*!!!*/		removePseudoMoves(otherTypeColor,newPos);
			cbsNewPosReplace= this.tCallBacks[newPos].get();
			this.allOfOneColor[otherColor].XOR(newPosMask);
			if(otherTypeColor>>7==PIECE_TYPE_PAWN) {
				context.pawns[otherColor]^=newPosMask;
			}
		}else {
			cbsNewPosEmptyBefore = this.tCallBacks[newPos].get();
		}

		
		int typeColor = fields[oldPos].getAndSet(-1);

/*!!!*/	removePseudoMoves(typeColor,oldPos);

		if(move.isPromotion()) {
			typeColor = move.getPromotePieceType();
			pieceType = typeColor>>7;
		}
/*u*/	fields[newPos].set(typeColor);
		// update occupancy
/*u*/	this.allOfOneColor[color].XOR(moveMask);
		// update moves
		
		cbsOldPos = this.tCallBacks[oldPos].get();
		if(pieceType==PIECE_TYPE_KING) {
			this.kings[color].set(1L<<newPos);
		}
		if(pieceType==PIECE_TYPE_PAWN) {
			//out(moveMask);
			//out(context.pawns[color]);
			context.pawns[color]^=moveMask;
			//out(context.pawns[color]);
			
		}
		if(context.enpMask!=0L)	{
			if(move.getEnPassanteSquare()==context.enpPos) {
				int enpPawnPos = move.getEnPassantePawnPos();
				cbsEnpPawnRemoved = this.tCallBacks[enpPawnPos].get(); 				
				int enpTypeColor = fields[enpPawnPos].get();
/*u*/			this.fields[enpPawnPos].set(-1);
/*u*/			this.allOfOneColor[otherColor].unsetBit(enpPawnPos);// might be empty anyways
				context.pawns[color]^=newPosMask;
				removePseudoMoves(enpTypeColor,enpPawnPos);
			}
		}

			
		if(move.isTwoSquarePush()) {
			int enpSquare= move.getEnPassanteSquare();
			this.enPassantePos.set(1L<<enpSquare);//@TBD always set (geht mit stack)
		}

		
		context.allOfOneColor[COLOR_WHITE] = this.allOfOneColor[COLOR_WHITE].get();// @todo WTF too expensive
		context.allOfOneColor[COLOR_BLACK] = this.allOfOneColor[COLOR_BLACK].get();
		
		context.occ = context.allOfOneColor[COLOR_WHITE]|context.allOfOneColor[COLOR_BLACK];
		context.notOcc = ~(context.occ);
				
		if(pieceType!=PIECE_TYPE_PAWN) {
			this.updateNonPawnPseudoMoves(typeColor, newPos);
		}else {
			//for Pawns just add the two attackers
			int[] attacks = move.getPawnNewAttacks();
			for(int i=0;i<attacks.length;i++) {
				this.tCallBacks[attacks[i]].setBit(newPos);
			}
			

			this.callBacks[newPos].set(move.getPawnNewAttackMask());
		}
			
		long cbs = cbsNewPosEmptyBefore|(cbsNewPosReplace) |cbsOldPos|cbsEnpPawnRemoved;
		cbs &= (context.occ)&~(moveMask)&~(context.pawns[COLOR_WHITE]|context.pawns[COLOR_BLACK]);//@todo occ var
		
		long kingsCalled = cbs&MASK_ALL_KINGS;
		int retVal = Long.bitCount(cbs);
		for(int i=0;i<retVal;i++) {
			int cbPos = Long.numberOfTrailingZeros(cbs);
			//long cbPosMask = 1L<<cbPos;
			cbs &= cbs - 1;				
			int cbTypeColor = fields[cbPos].get();
			cbscounter++;
			this.updateNonPawnPseudoMoves(cbTypeColor, cbPos);
		}
		
		this.updatePawnPseudoMoves(color);
		this.updatePawnPseudoMoves(otherColor);
		
		if(context.ntchd!=0) {
			// if there is no combination remaining set to 0
			
			if(move.isRochadeDisabler()) { 
				if(move.isRochade()) {
					this.setMove(move.getRookMove(),context);
				}
				// update fields
				this.untouched.AND_NOT(moveMask);
			}
					
			long newCastleMovesKQ=0;
			long newCastleMoveskq=0;
			
			//@TODO WTF: Combine E1 and combine with nonPawn stuff
			
			//remove rochade protagonists in occ if touched
			long ntchdOcc= context.occ & MASK_NOT_ALL_ROOKS_KINGS |context.ntchd;
		
			//ntchdOcc and MASK_E1_H1 should be identical in the range of MASK_CASTLE_ALL_K
			if (((ntchdOcc^MASK_E1_H1)& MASK_CASTLE_ALL_K)==0L) {
				newCastleMovesKQ|= MASK_CASTLE_KING_K;
			}
			if (((ntchdOcc^MASK_E1_A1)& MASK_CASTLE_ALL_Q)==0L) {
				newCastleMovesKQ|= MASK_CASTLE_KING_Q;
			}
			if (((ntchdOcc^MASK_E8_H8)& MASK_CASTLE_ALL_k)==0L) {
				newCastleMoveskq|= MASK_CASTLE_KING_k;
			}
			if (((ntchdOcc^MASK_E8_A8)& MASK_CASTLE_ALL_q)==0L) {
				newCastleMoveskq|= MASK_CASTLE_KING_q;
			}
			//boolean change=false;
			if( context.oldCastleMovesKQ !=newCastleMovesKQ) {
				this.moveMasks[_E1].OR(newCastleMovesKQ);
				//change=true;
			}
			if( context.oldCastleMoveskq !=newCastleMoveskq) {
				this.moveMasks[_E8].OR(newCastleMoveskq);
				//change=true;
			}
			//if(change|kingsCalled!=0) {
				int delta =Long.bitCount(newCastleMovesKQ |newCastleMoveskq)-context.oldCastleMoves;
				if(delta!=0) {
					this.moveCount[color].addition(delta);
				}
			//}WTF@TODO
		}			
	
		
		
		
		
		/*
		checkMoves() ;
		*/
		
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
			
			context.enpMask= this.enPassantePos.getAndSet(0L);
			context.enpPos = Long.numberOfTrailingZeros(context.enpMask);
			for(int i=0;i<2;i++) {
				context.mLeftOld[i]= mLeft[i].get();
				context.mRightOld[i]= mRight[i].get();
				context.mOneUpOld[i]= mOneUp[i].get();
				context.mTwoUpOld[i]= mTwoUp[i].get();
			}
			context.ntchd = untouched.get();
			if(context.ntchd!=0) {
				if(((context.ntchd & MASK_NOT_ALL_ROOKS) ==0 || (context.ntchd & MASK_ALL_ROOKS)==0)) {
					context.ntchd = 0L;
					untouched.set(0L);
				}else {
					context.oldCastleMovesKQ = moveMasks[_E1].get() & MASK_CASTLE_KING_KQkq;
					context.oldCastleMoveskq = moveMasks[_E8].get() & MASK_CASTLE_KING_KQkq;
					context.oldCastleMoves =Long.bitCount(context.oldCastleMovesKQ|context.oldCastleMoveskq);
				}
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
		for (int j = 0; j < 64; j++) {
			attacks[j] = (int)(Long.bitCount(tCallBacks[j].get()&own));
		}
		return attacks;
	}
	
	private void handleRochade() {
		/*
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
		*/

		/*
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
		*/

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
		System.out.println("CBS COutner!"+cbscounter);
	}
	
	
}
