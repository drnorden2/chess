package perft.chess.core;

import perft.chess.core.baseliner.BLIndexedList;

public class LegalMoveTester {
	private final Position position;
	//private final boolean[] rescueMap=new boolean[64];
	private long rescueMap = 0L;
	public LegalMoveTester(Position position) {
		this.position=position;
	}
	

	public void checkLegalMovesOpt (){
		
		int curColor = position.getColorAtTurn();
		BLIndexedList<Piece> allPiecesOfCurCol = position.allPieces[curColor];
		int enpassantePos = position.enPassantePos.get();
		int otherColor = (curColor +1)%2;
		int kingPos = position.getKingPos(curColor);
		
		int kingAttacks = position.attackTable[otherColor].get(kingPos);
		
		FieldCallback kingAttackerCB = null;
		if(kingAttacks==1) {
			kingAttackerCB = kingAttackedBy(position.fields[kingPos],otherColor);
			updateRescueMap(kingPos, kingAttackerCB.getElementIndex()); 
		}
		
		for(int i=0;i<allPiecesOfCurCol.size();i++) {
			Piece piece = allPiecesOfCurCol.getElement(i);	
			int oldPos = piece.getPosition();
			Field field = position.fields[oldPos];
			//int pseudoMoveCount =  field.getPseudoMoveList(pseudoMoves);
			int pseudoMoveCount =  field.pseudoMoves.size();
			
			if(pseudoMoveCount!=0) {
				
				if(piece.getType()==Piece.PIECE_TYPE_KING) {
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
	}

	
	public void checkLegalMovesAll(){
		int curColor = position.getColorAtTurn();
		BLIndexedList<Piece> allPiecesOfCurCol = position.allPieces[curColor];
		for(int i=0;i<allPiecesOfCurCol.size();i++) {
			Piece piece = allPiecesOfCurCol.getElement(i);	
			int oldPos = piece.getPosition();
			Field field = position.fields[oldPos];
			int pseudoMoveCount =  field.pseudoMoves.size();
			if(pseudoMoveCount!=0) {
				for(int ii=0;ii<pseudoMoveCount;ii++) {
					position.addLegalMove(field.pseudoMoves.getElement(ii));
				}
			}
		}
	}
	
	public void updateRescueMap(int kingPos, int attackerPos) {
		if(MoveManager.trackBack[kingPos][attackerPos]==attackerPos) {
			this.rescueMap|= 1L << attackerPos;
			
		}else {
			do {
				this.rescueMap|= 1L << attackerPos;
				attackerPos = MoveManager.trackBack[kingPos][attackerPos];
			}while(kingPos!=attackerPos);
		}
		/*
		if(position.wtfIteration==121) {
			System.out.print("RescuePath:\n*");
			for(int i=0;i<64;i++) {
				System.out.print(this.rescueMap[i]?'X':' ');
				if((i+1)%8==0) {
					System.out.print("*\n*");
				}
			}
		}*/
	}	

	private void handleKingPiece(Field oldKingField, int oldKingPos, int otherColor, int oldKingPosAttacks, int pseudoMoveCount) {
		for(int ii=0;ii<pseudoMoveCount ;ii++) {
			Move move= oldKingField.pseudoMoves.getElement(ii);
			int newKingPos = move.getNewPos();
			int newKingPosAttacks = position.attackTable[otherColor].get(newKingPos);
			if(newKingPosAttacks!=0) {
				continue; // nothing can be done!
			}else {
				if(oldKingPosAttacks==0) {//no Check
					if(move.getMoveType()== Move.MOVE_TYPE_ROCHADE) {
						// no check and king target safe => only test the jump one!
						if(position.attackTable[otherColor].get(oldKingPos+move.getDirOfRochade()) == 0){
							position.addLegalMove(move);
							continue;
						}     
					}else {// Safe normal King moves!
						position.addLegalMove(move); 
						continue;	
					}
				}else {//Check
					if(move.getMoveType()== Move.MOVE_TYPE_ROCHADE) {
						continue;
					}
					 
					 // last remaining case: King is in check and wants to get to safe spot (does check follow him?)
					boolean kingEscapes =true;
					for(int i=0; i<oldKingField.getRegisteredCallbackCount();i++){
						FieldCallback cb = oldKingField.getRegisteredCallback(i);
						//@TODO CB Should know the color!
						if(cb.getCallbackType() == FieldCallback.CALLBACK_TYPE_BEAT_RAY
								&& cb.getColor()==otherColor) {
							// if it is just one thread and you can beat one! there you go 
							if(newKingPos == cb.getElementIndex()) {
								continue;
							}
							int xCB=cb.getDirX();
							int yCB=cb.getDirY();
							int xyCV=xCB*yCB;
							xCB=(xCB < 0) ? -xCB : xCB;
							yCB=(yCB < 0) ? -yCB : yCB;
							
							int xM=move.getDirX();
							int yM=move.getDirY();
							int xyM=xM*yM;
							xM=(xM < 0) ? -xM : xM;
							yM=(yM < 0) ? -yM : yM;
											
							// this move is on the same axis as this threas!
							if((Math.abs(xCB-xM ) + Math.abs(yCB-yM)==0)&&xyM ==0
									||xyM == xyCV &&xyM !=0){
								kingEscapes =false;
								break;
							}
						}
					}
					if(kingEscapes ) {
						position.addLegalMove(move);
					} 
				}
			}
		}

	}
	
		
	
	/*
	 * Only called when not King move and not more than one attack on the king
	 */
	private void handleOtherPiece(Field field, Piece piece, int oldPos, int curColor, int otherColor, int kingPos, int kingAttacks, FieldCallback attacker,  int pseudoMoveCount, int enpassantePos) {
		FieldCallback oldPosRegToKing = field.getRegisteredCallbackForPos(kingPos);
		FieldCallback pinner =null;
		
		if(oldPosRegToKing!=null) {
			pinner = this.piecePinnedBy(field,oldPosRegToKing, oldPos, otherColor, kingPos);
		}
		if(pinner==null && kingAttacks ==0) {
			//all but enpassante ok!
			for(int ii=0;ii<pseudoMoveCount ;ii++) {
				Move move= field.pseudoMoves.getElement(ii);
				if(!(move.getNewPos()==enpassantePos && move.isEnpassanteMove() && isEnpassanteDiscovery(field, oldPosRegToKing, oldPos, otherColor, kingPos, move, enpassantePos))) {
					// does the enpassante create a self check by discovery?
					// eighter the pawn beaten pawn or the ray of both
					position.addLegalMove(move);			
				}
			}
		}else if(pinner==null && kingAttacks !=0) {
			//check rescue moves
				
			for(int ii=0;ii<pseudoMoveCount ;ii++) {
				Move move= field.pseudoMoves.getElement(ii);
				//enpassante case relevant!
				if(((this.rescueMap >> move.getNewPos()) & 1L)==1L 
						||(move.getNewPos()==enpassantePos && move.isEnpassanteMove() && attacker.getElementIndex()==move.getEnPassantePawnPos() )) {		
					if(!(move.getNewPos()==enpassantePos && move.isEnpassanteMove()  && isEnpassanteDiscovery(field, oldPosRegToKing, oldPos, otherColor, kingPos, move, enpassantePos))) {
						// does the enpassante create a self check by discovery?
						// eighter the pawn beaten pawn or the ray of both
						// move has to finally block on rescue map!
						position.addLegalMove(move);
					}
				}
			}
		}else if(pinner!=null && kingAttacks !=0) {
			return;// no pinned move can prevent a check because it cannot be within XRAY!
		}else {
			// only remaining:
			//if(pinner!=null && kingAttacks ==0) >> check slider Moves
			//calculate sliding moves in XRay all are fine just check enpassante case
			
			int xCB=pinner.getDirX();
			int yCB=pinner.getDirY();
			int xyCV=xCB*yCB;
			xCB=(xCB < 0) ? -xCB : xCB;
			yCB=(yCB < 0) ? -yCB : yCB;
			
			for(int ii=0;ii<pseudoMoveCount ;ii++) {
				Move move= field.pseudoMoves.getElement(ii);
				if(!(move.getNewPos()==enpassantePos && move.isEnpassanteMove() && isEnpassanteDiscovery(field, oldPosRegToKing, oldPos, otherColor, kingPos, move, enpassantePos))) {
					// does the enpassante create a self check by discovery?
					// eighter the pawn beaten pawn or the ray of both
					// move has to finally still slide!
					int xM=move.getDirX();
					int yM=move.getDirY();
					int xyM=xM*yM;
					xM=(xM < 0) ? -xM : xM;
					yM=(yM < 0) ? -yM : yM;							
					// this move is on the same axis as this threas!
					if((Math.abs(xCB-xM ) + Math.abs(yCB-yM)==0)&&xyM ==0
							||xyM == xyCV &&xyM !=0){
						position.addLegalMove(move);		
					}
				}
			}			
		}
	}

	public boolean isEnpassanteDiscovery(Field oldPosField, FieldCallback oldPosRegToKing,int oldPos, int otherColor, int kingPos, Move move, int enpassantePos) {
		if(Move.getRank(kingPos)==Move.getRank(oldPos)) {
			 // is king on same rank als OldPos? => does other field have an attacker 
			int proxyPos = oldPos;
			if(oldPosRegToKing!=null) {
				//Then it has to be the other
				proxyPos = move.getEnPassantePawnPos();
			}
			Field proxyField = position.fields[proxyPos];
			for(int i=0; i<proxyField.getRegisteredCallbackCount();i++){
				FieldCallback cb = proxyField.getRegisteredCallback(i);
				if(cb.getCallbackType() == FieldCallback.CALLBACK_TYPE_BEAT_RAY) {
					if(cb.getColor()==otherColor) {
						//in a straight line
						if(cb.getDirX()!=0 &&cb.getDirY()==0) {
							return true;			
						}
					}
				}
			}	
		}else {// above pinner cannot be part of case2
			 // is pawn to be beaten pinned?
			int eppPos = move.getEnPassantePawnPos();
			Field eppField =position.fields[eppPos];
			FieldCallback eppRegToKing= eppField.getRegisteredCallbackForPos(kingPos);
			if(eppRegToKing!=null) {
				FieldCallback pinner = this.piecePinnedBy(eppField,eppRegToKing, eppPos , otherColor, kingPos);
				if(pinner!=null) {
					return true;
				}
			}
		}
		return false;
	}

	/*
	 * these are only the harmless non King non Enpassante moves with 1x check max
	 */
	public FieldCallback piecePinnedBy(Field oldPosField,FieldCallback oldPosRegToKing, int oldPos, int otherColor,int kingPos) {		
		if(oldPosRegToKing.getCallbackType()==FieldCallback.CALLBACK_TYPE_CHECK_KNIGHT_ATTACK ) {
			return null;
		}
		
		//remaining cases:
		//* isPiece Pinned? yes?=>return move pattern
		// the king is dependend  => now check if it is xrayed
		for(int i=0; i<oldPosField.getRegisteredCallbackCount();i++){
			FieldCallback cb = oldPosField.getRegisteredCallback(i);
			if(cb.getCallbackType() == FieldCallback.CALLBACK_TYPE_BEAT_RAY) {
				if(cb.getColor()==otherColor) {
					//in a straight line
					int attackerPos = cb.getElementIndex();
					int cur = MoveManager.trackBack[kingPos][attackerPos];
					if(cur==attackerPos) {
						continue;
					}
					do {	
						if(cur==oldPos) {
							return cb;
						}
						cur = MoveManager.trackBack[kingPos][cur];
					}while(kingPos != cur);							
					continue;
				}
			}
		}	
		return null;//not pinned
	}		

	/*
	 * these are only the harmless non King non Enpassante moves with 1x check max
	 */
	public FieldCallback kingAttackedBy(Field kingField ,int otherColor) {
		//find the current attacker 
		for(int i=0; i<kingField.getRegisteredCallbackCount();i++){
			FieldCallback cb = kingField.getRegisteredCallback(i);
			//no Knight handeling ==>these are the callbacks of the others
			if(cb.getColor()==otherColor) {//this is the guy
				if(cb.getCallbackType()>=FieldCallback.CALLBACK_TYPE_BEAT_ONE) {
					return cb;
				}
			}
		}
		return null;//should never happen!
	}
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public void checkLegalMovesReference(){

		int curColor = position.getColorAtTurn();
		BLIndexedList<Piece> allPiecesOfCurCol = position.allPieces[curColor];
		int enpassantePos = position.enPassantePos.get();
		int otherColor = (curColor +1)%2;
		int kingPos = position.getKingPos(curColor);
		int kingAttacks = position.attackTable[otherColor].get(kingPos);
		
		for(int i=0;i<allPiecesOfCurCol.size();i++) {
			Piece piece = allPiecesOfCurCol.getElement(i);
			int oldPos = piece.getPosition();
			Field field = position.fields[oldPos];
			//int pseudoMoveCount =  field.getPseudoMoveList(pseudoMoves);
			int pseudoMoveCount =  field.pseudoMoves.size();
			for (int ii = 0; ii < pseudoMoveCount; ii++) {

				Move move = field.pseudoMoves.getElement(ii);
				//Move move = pseudoMoves[ii];
				int newPos = move.getNewPos();
				if(oldPos == kingPos && kingAttacks ==0){
					if(move.getMoveType()!= Move.MOVE_TYPE_ROCHADE) {
						if(position.attackTable[otherColor].get(move.getNewPos())==0){
							position.addLegalMove(move);
							continue;
						}
					}else{
						int dir = move.getDirOfRochade();
						int rookPos = move.getRookPos();
						int counter = 0;
						boolean isAttack = false;
						for (int j = oldPos ; j != rookPos; j = j + dir) {
							if (counter++ < 3) {
								if (position.attackTable[otherColor].get(j) != 0) {
									isAttack =true;
									break;
								}
							}
						}
						if(!isAttack){
							position.addLegalMove(move);
							continue;
						}     
					}
				}else {
					if(move.getMoveType()!= Move.MOVE_TYPE_ROCHADE &&!isMovePinnedOrNotPreventingCheckEXPENSIVE(move, curColor, enpassantePos!=-1 )) {
						position.addLegalMove(move);
					}
				}
			}
		}
	}

	
	
	
	public boolean isMovePinnedOrNotPreventingCheckEXPENSIVE(Move move, int colorOfMove, boolean enPassante) {
		System.out.println("EXPENSIVES");
		boolean isPinned =false;
		int kingPos = position.getKingPos(colorOfMove);
		// If king is currently of the board // no way to tell if it remains pinned
		
		if(!position.bl.isAlreadyInSimulation ) {
			boolean isCheck = position.isCheck(colorOfMove);
			

		// check for every move if it brings you out of Check
			position.bl.isAlreadyInSimulation = true;
			position.bl.startNextLevel();
			position.moveBeforeBaseLine(move);
			position.checkGameState(colorOfMove);
			if(this.position.isCheck((colorOfMove))) {
				isPinned =true;
			}
			position.bl.undo();			
			position.bl.isAlreadyInSimulation = false;
		}
		return isPinned;
	}
}

