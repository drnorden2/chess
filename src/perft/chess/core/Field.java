package perft.chess.core;

import perft.chess.core.baseliner.*;
import perft.chess.core.datastruct.IndexedElement;
import perft.chess.core.o.O;

public class Field implements IndexedElement {
	
	private final int pos;
	private final int file;
	private final int rank;
	private final BaseLiner bl;
	//** under baseline **/
	private final BLVariable<Piece> piece;
	
	private final BLIndexedList<Move> pseudoMoves;
	private final Position position;
	BLIndexedList<FieldCallback> callBacks;
	public final static int NOTIFY_NOW_EMPTY =0;
	public final static int NOTIFY_NOW_OCCUPIED =1;
	public final static int NOTIFY_NOW_REPLACED =2;
	public final static int NOTIFY_ATTACKER_CHANGED =3;
	
	public final static int MOVE_IS_POSSIBLE= 0;
	public final static int MOVE_NOT_POSSIBLE=1;
	public final static int MOVE_IS_PINNED=2;
	public final static int MOVE_IS_UNCLEAR=3;
	static int optimizationCounter=0;
	FieldCallback[] fieldCBBuffer = new FieldCallback[64];//@TODO Dynamic !!!!!!!!!!!!!!!!!!!!!!!1
	
//	private static boolean isalreadyInNotification=false;

		

	public Field(BaseLiner bl, Position position, int pos) {
		this.bl = bl;
		this.pos = pos;
		this.file = Move.getFile(pos);
		this.rank= Move.getRank(pos);
		
		this.position = position;
		this.piece = new BLVariable<Piece>(this.bl, null);
		pseudoMoves = new BLIndexedList<Move>(this.bl, 36, 36);// TBD reduce
		callBacks = new BLIndexedList<FieldCallback>(bl, 64, 64);
	}
 
		
	// Field reference to Piece and pos of piece
	public void stagePiece(Piece pieceObj) {
		this.piece.set(pieceObj);
		pieceObj.setPosition(this.pos);
	}


	public void unStagePiece(Piece pieceObj) {
		pieceObj.setPosition(-1);
		this.piece.set(null);
		
	}

	// fields current Pseudomoves (until end of Ray) and Attacks
	public void stagePseudoMoves() {
		Piece piece = this.piece.get();
		int movesIndex = piece.getMoveIndex();
		Move[][] moves = position.moveManager.getRawMoves(movesIndex);
		addRemovePseudoMoves(piece,moves, -1,-1,false);
	}

	public void unStageAllMoves(Piece piece) {
		int movesIndex = piece.getMoveIndex();
		Move[][] moves = position.moveManager.getRawMoves(movesIndex);
		addRemovePseudoMoves(piece,moves, -1,-1,true);
	}


/*
	public int unStagePieceToReduceAttack(Piece oldPiece,int kingPos) {
		int counter = pseudoMoves.size();
		for (int i=0;i<counter;i++) {
			Move move = pseudoMoves.getElement(i);
			if( move.getNewPos() == kingPos && move.isAttackerMove() ){
				return -1;
			}
		}
		return 0;
	}
*/
	
	public int getPseudoMoveCount() {
		return pseudoMoves.size();
	}

	public Move getPseudoMove(int index) {
		return pseudoMoves.getElement(index);
	}
	public boolean containsPseudoMove(int index) {
		return pseudoMoves.contains(index);
	}

	
	public void addRemovePseudoMoves(Piece piece, Move[][] moves, int ii, int jj, boolean onlyRemove) {

		int color = piece.getColor();
		int newPos;
		int iiMax =moves.length;
		
		if(ii==-1) {
			ii=0;
		}else {
			iiMax =Math.min(ii+1, iiMax);
		}
		if(jj==-1) {
			jj=0;
		}
			
		for (int i = ii; i < iiMax; i++) {
			boolean remove = onlyRemove;

			for (int j = jj; j < moves[i].length; j++) {
				Move move = moves[i][j];
				newPos = move.getNewPos();
				
				
				if(move.isNoPromotionOrQueen()) {
					if(!remove) {
						if(position.fields[newPos].registerCallback(move.getFieldCB())) {
							Position.registerCount++;
							if(move.isAttackerMove() ) {
								position.attackTable[color].incr(newPos);
							}
						}
					}else {
						if(position.fields[newPos].unRegisterCallback(move.getFieldCB())) {
							Position.unRegisterCount++;
							if(move.isAttackerMove() ) {
								position.attackTable[color].decr(newPos);
							}
						}else {
							break;
						}
					}
				}
				
				if(move.getMoveType()!=Move.MOVE_TYPE_KING_SENSING) {
					if (!remove && isPseudoMove(piece, move)) {
						pseudoMoves.add(move);
					}else {
						pseudoMoves.remove(move);
					}
				}
				
				//exit ray!
				if (position.fields[newPos].getPiece() != null) {
					if(!remove && jj==-1) {// this is a real add
						break;
					}
					remove=true;
				}
			}
		}
	}
	private boolean isPseudoMove(Piece piece, Move move) {
		//O.ENTER("isPseudoMove");
		boolean isPossible = true;		
		int moveType = move.getMoveType();
		int oldPos = move.getOldPos();
		int newPos = move.getNewPos();
		boolean isOtherPiece =false;
		boolean sameColor = false;
		Piece otherPiece = position.fields[newPos].getPiece();
		
		if(otherPiece!=null) {
			isOtherPiece =true;
			sameColor = otherPiece.getColor()==piece.getColor();
		}
		
		switch (moveType) {
		case Move.MOVE_TYPE_PUSH_BEAT:
			isPossible = !isOtherPiece || isOtherPiece && !sameColor;
			break;
		case Move.MOVE_TYPE_PAWN_PUSH_CONVERT:
		case Move.MOVE_TYPE_PAWN_PUSH:
			isPossible = !isOtherPiece;
			break;
		case Move.MOVE_TYPE_ROCHADE:
			if (piece.isTouched()) { // king was touched
				isPossible = false;
			} else {
				int dir = move.getDirOfRochade();
				int rookPos = move.getRookPos();
				Piece rook = position.fields[rookPos].getPiece();
				if (rook == null || rook.isTouched()) { // king was touched
					isPossible = false;
				} else {
					for (int i = oldPos + dir; i != rookPos; i = i + dir) {
						if (position.fields[i].getPiece() != null) {
							isPossible = false;
							break;
						}
					}
				}
			}
			break;
		case Move.MOVE_TYPE_PAWN_BEAT_OR_ENPASSANTE:
			if (!isOtherPiece && position.enPassantePos.get() != -1 && position.enPassantePos.get() == newPos) {
				int twoPushPawnPos = move.getPos(move.getRank(oldPos), move.getFile(newPos));
				Piece pawnToRemove = position.fields[twoPushPawnPos].getPiece();
				if(pawnToRemove !=null &&pawnToRemove.getType()==Piece.PIECE_TYPE_PAWN && pawnToRemove.getColor()!=piece.getColor()){
					//Test if enpassante removal causes check by simulation!
					break;
				}else {
					isPossible = false;					
				}
			}else  {
				isPossible = isOtherPiece && !sameColor;
			}
			break;
		case Move.MOVE_TYPE_PAWN_BEAT_CONVERT:
		case Move.MOVE_TYPE_PAWN_BEAT:
				isPossible = isOtherPiece && !sameColor;
			break;
		}
		return isPossible;		
	}
	
	public Piece getPiece() {
		return this.piece.get();
	}


	@Override
	public int getElementIndex() {
		// TODO Auto-generated method stub
		return pos;
	}
	public int getRank() {
		return this.rank;
	}
	
	public int getFile() {
		return this.file;
	}
	
	public boolean registerCallback(FieldCallback cb) {
		return callBacks.add(cb);
	}
	public boolean isPosRegistered(int pos) {
		return callBacks.contains(pos);
	}
	
	public int getRegisteredCallbackCount() {
		return callBacks.size();
	}
	

	public FieldCallback getRegisteredCallback(int index) {
		return callBacks.getElement(index);
	}
	
	public FieldCallback getRegisteredCallbackForPos(int pos) {
		return callBacks.getByElementIndex(pos);
	}
	
	public int  getRegisteredCallbackType(int pos) {
		FieldCallback fieldCB = callBacks.getByElementIndex(pos);
		if(fieldCB ==null) {
			return -1;
		}
		return fieldCB.getCallbackType();
	}

	
	public boolean unRegisterCallback(FieldCallback cur) {
		return callBacks.remove(cur);
	}
		
	
	
	public void notifyCallBacks(int notifyType, int color, int notPos, boolean isKnight, int kingPos) {
		int callBackCount = 0;
		// invocation of callbacks
		for (int i = 0; i < callBacks.size(); i++) {
			FieldCallback cb= callBacks.getElement(i);
			if(cb.getElementIndex()==notPos ) {
				continue;// the old and the new position do not have to notify each other again
			}
		
			if(!isKnight && position.getKingPos(color)==this.getElementIndex() && cb.getCallbackType() == FieldCallback.CALLBACK_TYPE_CHECK_KNIGHT_ATTACK)  {
				continue;
			}
			
			fieldCBBuffer[callBackCount++] =cb; 
		}
		for (int i=0;i<callBackCount;i++) {
			FieldCallback fieldCB = fieldCBBuffer[i];
			//System.out.println("Callback from "+notPos+" to "+fieldCB.getElementIndex()+" "+fieldCB.getCallbackType());
			notifyCallBack(fieldCB, notifyType, color);
		}
	}

	
	void notifyCallBack(FieldCallback fieldCB, int notifyType,int color) {
		
		Field notifiedField = fieldCB.getField();
		Piece notifiedPiece = notifiedField.getPiece();
		if(notifiedPiece ==null) {
			//O.UT("WHAT JUST HAPPENED - No piece to notify?"+notifiedField.getElementIndex());
			return;
		}
		int ii = fieldCB.getII();
		int jj = fieldCB.getJJ();
		int callbackType = fieldCB.getCallbackType();
		int notifiedPieceColor = notifiedPiece.getColor();
		
		
		switch(notifyType) {
			case NOTIFY_NOW_EMPTY:
			case NOTIFY_NOW_OCCUPIED:
				//something of the other color went out of the way where we anyways always could beat
				if(callbackType==FieldCallback.CALLBACK_TYPE_BEAT_ONE && color!=notifiedPieceColor) {
					optimizationCounter++;
					return;
				}else if(callbackType==FieldCallback.CALLBACK_TYPE_BEAT_AS_PAWN && color==notifiedPieceColor) {
					optimizationCounter++;
					return;
				}
				break;
			case NOTIFY_NOW_REPLACED:
				//for pushers it does not matter if something remains blocking
				//|| callbackType==FieldCallback.CALLBACK_TYPE_PUSH_RAY suffers from on own Q on R3 beats n on R4
				if(callbackType==FieldCallback.CALLBACK_TYPE_PUSH_ONE ) {
					optimizationCounter++;
					return;
				}
				break;
		}
		
	    notifiedField.notifyFieldChange(ii,jj,callbackType);
	    
	}
	
	 void notifyFieldChange(int ii, int jj,  int callbackType) {
		Piece piece = this.getPiece();
		if (piece != null) {
			//this.removePseudoMoves(piece,ii, jj);
			int movesIndex = piece.getMoveIndex();
			Move[][] moves = position.moveManager.getRawMoves(movesIndex);
			if(position.wtfIteration==1823 && this.getElementIndex()==54) {
				System.out.println("WTF Here");
			}
			this.addRemovePseudoMoves(piece, moves, ii, jj,false);
		}
	}
	 
 
	 
	 
	public String toString() {
		return "Field "+pos+" w. "+this.piece.get();
	}
	
	 public static String toString(Object[] a) {
	        if (a == null)
	            return "null";

	        int iMax = a.length - 1;
	        if (iMax == -1)
	            return "[]";

	        StringBuilder b = new StringBuilder();
	        b.append('[');
	        for (int i = 0; ; i++) {
	            b.append(String.valueOf(a[i]));
	            if (i == iMax)
	                return b.append(']').toString();
	            b.append("\n");
	        }
	    }

}


