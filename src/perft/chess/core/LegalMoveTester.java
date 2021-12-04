package perft.chess.core;

import perft.chess.core.baseliner.BLIndexedList;

public class LegalMoveTester {
	private final Position position;
	private final boolean[] rescueMap=new boolean[64];
	private final Move[] pseudoMoves = new Move[64];
	public LegalMoveTester(Position position) {
		this.position=position;
	}
	

	public void checkLegalMovesOpt(){
		
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
			
			if(position.wtfIteration== 121  && piece.getPosition()==34 ){
				System.out.println("3rd WTHacker!");
			}
			
			int oldPos = piece.getPosition();
			Field field = position.fields[oldPos];
			int pseudoMoveCount =  field.getPseudoMoveList(pseudoMoves);
			if(pseudoMoveCount!=0) {
				if(piece.getType()==Piece.PIECE_TYPE_KING) {
					//private void handleKingPiece(Field oldKingField, int oldKingPos, int otherColor, int oldKingPosAttacks, int pseudoMoveCount,int level) {
					this.handleKingPiece(field, oldPos, otherColor, kingAttacks,pseudoMoves,pseudoMoveCount);
				}else {
					if(kingAttacks<2) {
						//private void handleOtherPiece(Field field, Piece piece, int oldPos, int curColor, int otherColor, int kingPos, int kingAttacks, int level, int pseudoMoveCount) {
						this.handleOtherPiece(field, piece, oldPos, curColor, otherColor, kingPos, kingAttacks,kingAttackerCB,pseudoMoves, pseudoMoveCount,enpassantePos);
					}//otherwise there is no hope!
				}
			}
		}
		if(kingAttacks==1) {
			updateRescueMap(kingPos, kingAttackerCB.getElementIndex()); 
		}
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
			int pseudoMoveCount =  field.getPseudoMoveList(pseudoMoves);
			for (int ii = 0; ii < pseudoMoveCount; ii++) {
				Move move = pseudoMoves[ii];
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
	
	public void updateRescueMap(int kingPos, int attackerPos) {
		if(MoveManager.trackBack[kingPos][attackerPos]==attackerPos) {
			this.rescueMap[attackerPos]=!this.rescueMap[attackerPos];
		}else {
			do {
				this.rescueMap[attackerPos]=!this.rescueMap[attackerPos];
				attackerPos = MoveManager.trackBack[kingPos][attackerPos];
			}while(kingPos!=attackerPos);
		}
		if(position.wtfIteration==4) {
			System.out.print("RescuePath:\n*");
			for(int i=0;i<64;i++) {
				System.out.print(this.rescueMap[i]?'X':' ');
				if((i+1)%8==0) {
					System.out.print("*\n*");
				}
			}
		}
	}	

	private void handleKingPiece(Field oldKingField, int oldKingPos, int otherColor, int oldKingPosAttacks, Move[] pseudoMoves, int pseudoMoveCount) {
		for(int ii=0;ii<pseudoMoveCount ;ii++) {
			Move move= pseudoMoves[ii];
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
	private void handleOtherPiece(Field field, Piece piece, int oldPos, int curColor, int otherColor, int kingPos, int kingAttacks, FieldCallback attacker, Move[] pseudoMoves, int pseudoMoveCount, int enpassantePos) {
		FieldCallback pinner = this.piecePinnedBy(field, piece, oldPos, otherColor, kingPos);
		if(pinner==null && kingAttacks ==0) {
			//all but enpassante ok!
			for(int ii=0;ii<pseudoMoveCount ;ii++) {
				Move move = pseudoMoves[ii];
				if(move.getNewPos()==enpassantePos ) {
					if(!isMovePinnedOrNotPreventingCheckEXPENSIVE(move, curColor, true)) {
						position.addLegalMove(move);		
					}
				}else {
					position.addLegalMove(move);		
				}
			}
		}else if(pinner==null && kingAttacks !=0) {
			//check rescue moves
				
			for(int ii=0;ii<pseudoMoveCount ;ii++) {
				Move move = pseudoMoves[ii];
				//enpassante case relevant!
				if(move.getNewPos()==enpassantePos ) {
					if(!isMovePinnedOrNotPreventingCheckEXPENSIVE(move, curColor, true)) {
						position.addLegalMove(move);		
					}
				}else {
					if(this.rescueMap[move.getNewPos()]) {
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
				Move move = pseudoMoves[ii];
				if(move.getNewPos()==enpassantePos ) {
					if(!isMovePinnedOrNotPreventingCheckEXPENSIVE(move, curColor, true)) {
						position.addLegalMove(move);		
					}
				}else {
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


	/*
	 * these are only the harmless non King non Enpassante moves with 1x check max
	 */
	public FieldCallback piecePinnedBy(Field oldPosField ,Piece piece, int oldPos, int otherColor,int kingPos) {
		
		// if not under check and old pos is not in sight of king => free to move!
		FieldCallback oldPosRegToKing = oldPosField.getRegisteredCallbackForPos(kingPos);
		boolean oldPosCanBePinned = oldPosRegToKing!=null;
		if(!oldPosCanBePinned){
			return null;//not pinned
		}
		if(oldPosRegToKing.getCallbackType()==FieldCallback.CALLBACK_TYPE_CHECK_KNIGHT_ATTACK ) {
			return null;
		}
		
		//remaining cases:
		//* isPiece Pinned? yes?=>return move pattern
		if(oldPosCanBePinned) {
			// the king is dependend  => now check if it is xrayed
			for(int i=0; i<oldPosField.getRegisteredCallbackCount();i++){
				FieldCallback cb = oldPosField.getRegisteredCallback(i);
				//@TODO CB Should know the color!
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
	
	public boolean isMovePinnedOrNotPreventingCheckEXPENSIVE(Move move, int colorOfMove, boolean enPassante) {
		boolean isPinned =false;
		int kingPos = position.getKingPos(colorOfMove);
		// If king is currently of the board // no way to tell if it remains pinned
		
		if(!position.bl.isAlreadyInSimulation ) {
			boolean isCheck = position.isCheck(colorOfMove);
			

		// check for every move if it brings you out of Check
			position.bl.isAlreadyInSimulation = true;
			//O.ENTER("Simulate for isMovePinned:"+move);
			position.bl.startNextLevel();
			position.moveBeforeBaseLine(move);
			position.checkGameState(colorOfMove);
			if(this.position.isCheck((colorOfMove))) {
				isPinned =true;
			}
			position.bl.undo();			
			//O.EXIT("Simulate for isMovePinned:"+move+":"+isPinned);
			position.bl.isAlreadyInSimulation = false;
		}
		/*
		if(opt1!=isPinned) {
			System.out.println("\nFailed to check"+move+" ("+opt1+"/"+isPinned+") "+position);
			this.isMovePinnedOrNotPreventingCheck(move, colorOfMove, enPassante);
		}*/
		return isPinned;
	}
}

/*
 * these are only the harmless non King non Enpassante moves with 1x check max
 */
/*
public boolean isOtherMovePinnedOrNotPreventingCheck(Move move, int colorOfMove, boolean enPassante) {
	int kingPos = position.getKingPos(colorOfMove);
	// If king is currently of the board // no way to tell if it remains pinned
	int otherColor = (colorOfMove+1)%2;
	boolean isCheck = position.isCheck(colorOfMove);
			
	int oldPos = move.getOldPos();
	int newPos = move.getNewPos();
	//!!always (regardless of check or not) required to be ensured that Old Pos does not open any thread to king
	Field oldPosField = position.fields[oldPos];
	Field newPosField = position.fields[newPos];
	
	
	FieldCallback oldPosRegToKing = oldPosField.getRegisteredCallbackForPos(kingPos);
	//new pos is only relevant for check situations, as it can kill the attacker or block the check
	FieldCallback newPosRegToKing = newPosField .getRegisteredCallbackForPos(kingPos) ;
	Piece piece = oldPosField.getPiece();
	boolean oldPosCanBePinned = oldPosRegToKing!=null;
	boolean canProtectedKing = newPosRegToKing!=null;
	// if under check and the move does not move into sight of King (to block or beat)=> locked
	if(isCheck && !canProtectedKing){
		return true;//not possible
	}
	
	// if not under check and old pos is not in sight of king => free to move!
	if(!isCheck && !oldPosCanBePinned){
		return false;//possible
	}
	
	//remaining cases:
	//* isMove Pinned? yes?=>true
	boolean isTrappedInXray = false;
	if(oldPosCanBePinned) {
		// the king is dependend  => now check if it is xrayed
		for(int i=0; i<oldPosField.getRegisteredCallbackCount();i++){
			FieldCallback cb = oldPosField.getRegisteredCallback(i);
			
			//@TODO CB Should know the color!
			if(cb.getCallbackType() == FieldCallback.CALLBACK_TYPE_BEAT_RAY) {
				Piece attacker = position.fields[cb.getElementIndex()].getPiece();
				if(attacker.getColor()==otherColor) {
					boolean canAttackKing =MoveManager.isPieceAttacker[attacker.getMoveIndex()][kingPos];
					if (canAttackKing) {
						int xCB=cb.getDirX();
						int yCB=cb.getDirY();
						
						int xM=move.getDirX();
						int yM=move.getDirY();
						// this move is on the same axis as this threas!
						if((xCB*xCB-xM*xM )+ (yCB*yCB-yM*yM)!=0){
							return true; // it is definetely pinned 
						}else {
							isTrappedInXray =true;
						}
					}
					break; // this was the one Xrayer 
				}
			}
		}	
	}
	
	// oldpos is not pinned
	
	//* if no: isCheck?
	//* 	if yes: does move kill an attack? => false
	//* 	if no: does move block a check? => false
	//* if no check => false
	
	
	//if check and oldPos is Xrayed => not able to help with this move!
	if(isCheck) {
		if(isTrappedInXray) {
			return true;
		}
		// if the newPos is visible to King
		if(canProtectedKing) {
			for(int i=-1; i<newPosField.getRegisteredCallbackCount();i++){
				Piece attacker = null;
				if(i==-1) {
					attacker = position.fields[newPos].getPiece();
					if(attacker ==null) {
						continue;
					}
					boolean isKnight = attacker.getType()==Piece.PIECE_TYPE_KNIGHT;
					boolean isKnightCallBack = newPosRegToKing.getCallbackType()==FieldCallback.CALLBACK_TYPE_CHECK_KNIGHT_ATTACK;
					if(isKnight!=isKnightCallBack) {
						continue;
					}
				}else {
					FieldCallback cb = newPosField.getRegisteredCallback(i);
					if(cb.getCallbackType() != FieldCallback.CALLBACK_TYPE_BEAT_RAY) {
						continue;
					}
					attacker = position.fields[cb.getElementIndex()].getPiece();
				}
				//@TODO CB Should know the color!
				if(attacker.getColor()==otherColor) {
					boolean kingAttacked = oldPosField.getRegisteredCallbackForPos(attacker.getPosition())!=null;
					if(kingAttacked) {
						return false;//move to the rescue!!
					}
				}
			}
		}
	}
	
	return false;// no pin of oldPos and no check 
}
*/

