package perft.chess.perftbb;


import static perft.chess.Definitions.*;

import perft.chess.core.datastruct.IndexedElement;


public class Move implements IndexedElement  {

	private static int moveIDCounter =0;
	private final String notation;
	private final int oldPos;
	private final int newPos;
	
	private final int moveType;
	private final int elementIndex;
	private final int promotePieceType;
	private final int rookPos;
	private final int dirOfRochade;
	private final Move rookMove;
	private final boolean isTwoSquarePush;
	private final int enPassanteSquare;
	private final int enPassantePawnPos;
	
	private final boolean isAttackerMove;
	
	private final boolean isKingSensing;
	private final boolean isRochade;
	private final int callbackType;
	private final int dirX;
	private final int dirY;
	private final int color;
	private final int type;
	private final int typeColor;
	private final boolean isRochadeDisabler;
	private int[] pawnNewAttacks;
	private long pawnNewAttackMask;
	
	public static int getMaxMoveID() {
		return moveIDCounter;
	}
	
	public static void resetMoveIDs() {
		moveIDCounter =0;
	}
	public Move(int type, int typeColor, int color, int callbackType,int oldPos, int newPos, int moveType,int dirX, int dirY) {
		this(type, typeColor,color,callbackType, oldPos, newPos, moveType, dirX, dirY,-1);
	}
	
	public Move(int type, int typeColor, int color, int callbackType,int oldPos, int newPos, int moveType,int dirX, int dirY,int promotePieceType) {
		this(type, typeColor,color,callbackType, oldPos, newPos, moveType,dirX, dirY, promotePieceType, -1, 0);		
	}
	
	
	public Move(int type, int typeColor, int color, int callbackType, int oldPos, int newPos, int moveType,int dirX, int dirY,int promotePieceType, int rookPos, int dirOfRochade) {
		this.elementIndex= Move.moveIDCounter++;
		this.type = type;
		this.typeColor = typeColor;
		this.color = color;
		this.oldPos= oldPos;
		this.newPos = newPos;
		this.moveType = moveType;
		this.promotePieceType = promotePieceType==-1? promotePieceType:(promotePieceType*2+color)*64; 
		this.rookPos =rookPos;
		this.dirOfRochade = dirOfRochade ;
		this.callbackType = callbackType;
		this.dirX=dirX;
		this.dirY=dirY;
		if(rookPos==-1) {
			rookMove = null;
		}else {
			rookMove = new Move(PIECE_TYPE_ROOK,((PIECE_TYPE_ROOK <<1) + color) << 6, color, CALLBACK_TYPE_OTHER, rookPos, oldPos+(dirOfRochade),MOVE_TYPE_PUSH_BEAT,dirX*-1,dirY);
		}
		if(moveType ==MOVE_TYPE_PAWN_PUSH) {
			if(getRankForPos(oldPos)==_2 &&  getRankForPos(newPos)==_4) {
				isTwoSquarePush = true;
				enPassanteSquare = getPosForFileRank(getFileForPos(newPos),_3);
				enPassantePawnPos = getPosForFileRank(getFileForPos(newPos),_4);
			}else if(getRankForPos(oldPos)==_7 &&  getRankForPos(newPos)==_5) {
				isTwoSquarePush = true;
				enPassanteSquare = getPosForFileRank(getFileForPos(newPos),_6);
				enPassantePawnPos = getPosForFileRank(getFileForPos(newPos),_5);
			}else {
				isTwoSquarePush = false;
				enPassanteSquare = -1;
				enPassantePawnPos = -1;
			}
		}else if(moveType ==MOVE_TYPE_PAWN_BEAT_OR_ENPASSANTE) {
			if(getRankForPos(oldPos)==_5 &&  getRankForPos(newPos)==_6) {
				isTwoSquarePush = false;
				enPassanteSquare = getPosForFileRank(getFileForPos(newPos),_6);
				enPassantePawnPos = getPosForFileRank( getFileForPos(newPos),_5);
			}else if(getRankForPos(oldPos)==_4 &&  getRankForPos(newPos)==_3) {
				isTwoSquarePush = false;
				enPassanteSquare = getPosForFileRank(getFileForPos(newPos),_3);
				enPassantePawnPos = getPosForFileRank(getFileForPos(newPos),_4 );
			}else {
				isTwoSquarePush = false;
				enPassanteSquare = -1;
				enPassantePawnPos = -1;
			}
		}else {
			isTwoSquarePush = false;
			enPassanteSquare = -1;
			enPassantePawnPos = -1;
		}
	
		switch(this.moveType) {
			case MOVE_TYPE_PUSH_BEAT:
			case MOVE_TYPE_PAWN_BEAT:
			case MOVE_TYPE_PAWN_BEAT_CONVERT:
			case MOVE_TYPE_PAWN_BEAT_OR_ENPASSANTE:
						this.isAttackerMove=true;
				break;
			default:
				this.isAttackerMove=false;
		}
		this.isKingSensing = (moveType==MOVE_TYPE_KING_SENSING);
		this.isRochade = this.dirOfRochade!=0;
		this.notation = generateNotation();
		
		this.isRochadeDisabler = (typeColor == PIECE_TYPE_WHITE_ROOK&&(oldPos==_A1 ||oldPos==_H1))||
				(typeColor == PIECE_TYPE_BLACK_ROOK&&(oldPos==_A8 ||oldPos==_H8))||
				(typeColor == PIECE_TYPE_WHITE_KING&&(oldPos==_E1))||
				(typeColor == PIECE_TYPE_BLACK_KING&&(oldPos==_E8))||
				(color==COLOR_BLACK&&(newPos==_A1 ||newPos==_H1||newPos==_E1 ))||
				(color==COLOR_WHITE&&(newPos==_A8 ||newPos==_H8||newPos==_E8 ));

		if(type == PIECE_TYPE_PAWN) {
			int[] attacks;
			long mask = 0L;
			int file = getFileForPos(newPos);
			if(file!=_A && file!=_H) {
				attacks=new int[2];
			}else {
				attacks=new int[1];
			}
			int counter =0;
			if(file!=_A) {
				attacks[counter]= color==COLOR_WHITE?newPos+7:newPos-9;
				mask|=1L<<attacks[counter];
				counter++;
			}
			if(file!=_H) {
				attacks[counter]=color==COLOR_WHITE?newPos+9:newPos-7;
				mask|=1L<<attacks[counter];
			}
			this.pawnNewAttacks=attacks;
			this.pawnNewAttackMask=mask;
		}
		
	}
	
	/*
	public Move (Move move, int ii, int jj, int moveIndex ) {
		this.ii =-1;
		this.jj =-1;
		this.notation = move.notation;
		this.elementIndex=moveIndex;
		this.oldPos = move.oldPos;
		this.newPos = move.newPos;
		this.moveType = move.moveType;
		this.promotePieceType = move.promotePieceType;
		this.rookPos = move.rookPos;
		this.dirOfRochade = move.dirOfRochade;
		this.rookMove = move.rookMove;
		this.isTwoSquarePush = move.isTwoSquarePush;
		this.enPassanteSquare = move.enPassanteSquare;
		this.enPassantePawnPos = move.enPassantePawnPos;
		this.isAttackerMove = move.isAttackerMove;
		this.isKingSensing = move.isKingSensing;
		this.isRochade = move.isRochade;
		this.callbackType = move.callbackType;
		this.dirX = move.dirX;
		this.dirY = move.dirY;
		this.color = move.color;
	}
*/
	public int getElementIndex() {
		return this.elementIndex;
	}
	public int getOldPos() {
		return this.oldPos;
	}
	
	public int getNewPos() {
		return this.newPos;
	}
	
	public int getMoveType() {
		return this.moveType;
	}
	public int getDirOfRochade() {
		return this.dirOfRochade;
	}
	public int getRookPos() {
		return this.rookPos;
	}
	
	
	public String toString () {
		String val ="["+elementIndex+"] "+getPieceCharForTypeColor(typeColor) + " : "+generateNotation()+  "   From "+oldPos+" to "+newPos+" : Promotion:"+this.getPromotePieceType();	
		return val;
	}
	public Move getRookMove() {
		return rookMove;
	}
	public boolean isTwoSquarePush() {
		return isTwoSquarePush;
	}
	
	public int getEnPassanteSquare() {
		return enPassanteSquare;		
	}
	public int getEnPassantePawnPos() {
		return enPassantePawnPos;		
	}
	
		public int getPromotePieceType() {
		return promotePieceType;
	}
	public boolean isPromotion() {
		return (promotePieceType!=-1);
	}
	
	
	
	public int getCallbackType() {
		return this.callbackType;
	}

	public boolean isRochade() {
		return this.isRochade;		
	}
	
	public boolean isAttackerMove() {
		return this.isAttackerMove;		
	}
	public boolean isKingSensing() {
		return this.isKingSensing;		
	}
	 
	public int getDirX() {
		return dirX;
	}
	public int getDirY() {
		return dirY;
	}
	public boolean isEnpassanteMove() {
		return this.moveType==MOVE_TYPE_PAWN_BEAT_OR_ENPASSANTE ;
	}
	public String getNotation() {
     	return this.notation;
	}
	
	private  String generateNotation () {
		int r1 = getRankForPos(oldPos);
		int f1 = getFileForPos(oldPos);
		int r2 = getRankForPos(newPos);
		int f2 = getFileForPos(newPos);
		String promotion = "";
		if(this.getPromotePieceType()!=-1) {
			promotion += (""+getPieceCharForTypeColor(this.getPromotePieceType())).toLowerCase();
		}
		return ""+((char)('a'+f1))  +""+ (1+r1)+""
				+((char)('a'+f2))  +""+ (1+r2)+promotion+"";
	}	
	
	public int getColor() {
		return this.color;
	}
	public boolean isRochadeDisabler() {
		return this.isRochadeDisabler;
	}
	public int getPieceType() {
		return this.type;
	}
	public int getTypeColor() {
		return this.typeColor;
	}
	public int[] getPawnNewAttacks() {
		return this.pawnNewAttacks;
	}
	public long getPawnNewAttackMask() {
		return this.pawnNewAttackMask;
	}
}
