package perft.chess.core;

import perft.chess.core.baseliner.BaseLiner;
import perft.chess.core.datastruct.IndexedElement;

public class Move implements IndexedElement{
	public static final int MOVE_TYPE_PUSH_BEAT = 0;
	public static final int MOVE_TYPE_PAWN_BEAT= 1;
	public static final int MOVE_TYPE_PAWN_PUSH= 2;
	public static final int MOVE_TYPE_ROCHADE= 3;
	public static final int MOVE_TYPE_PAWN_BEAT_OR_ENPASSANTE= 4;
	public static final int MOVE_TYPE_PAWN_PUSH_CONVERT= 5;
	public static final int MOVE_TYPE_PAWN_BEAT_CONVERT= 6;
	public static final int MOVE_TYPE_KING_SENSING = 7;
	public static final int MOVE_TYPE_INITAL_PLACEMENT = 8;
	
	
	public static final int _A = 0;
	public static final int _B = 1;
	public static final int _C = 2;
	public static final int _D = 3;
	public static final int _E = 4;
	public static final int _F = 5;
	public static final int _G = 6;
	public static final int _H = 7;

	public static final int _8 = 0;
	public static final int _7 = 1;
	public static final int _6 = 2;
	public static final int _5 = 3;
	public static final int _4 = 4;
	public static final int _3 = 5;
	public static final int _2 = 6;
	public static final int _1 = 7;

	public static final int ROCHADE_W_ROOK_LONG = 2;
	public static final int ROCHADE_W_ROOK_SHORT = 6;
	public static final int ROCHADE_B_ROOK_LONG = 58;
	public static final int ROCHADE_B_ROOK_SHORT = 62;
	

	
	public static final int INDEX_Counter_ID =0;
	
	private final int oldPos;
	private final int newPos;
	
	private final int ii;
	private final int jj;
	private final FieldCallback fieldCB;
	
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
	
	
	public Move(int color, int callbackType,int oldPos, int newPos, int moveType,int dirX, int dirY) {
		this(color,callbackType, oldPos, newPos, moveType, dirX, dirY,-1);
	}
	
	public Move(int color, int callbackType,int oldPos, int newPos, int moveType,int dirX, int dirY,int promotePieceType) {
		this(color,callbackType, oldPos, newPos, moveType,dirX, dirY, promotePieceType, -1, 0);		
	}
	
	
	public Move(int color, int callbackType, int oldPos, int newPos, int moveType,int dirX, int dirY,int promotePieceType, int rookPos, int dirOfRochade) {
		this.elementIndex=-1;
		
		this.ii =-1;
		this.jj =-1;
		this.fieldCB=null;
		this.color = color;
		this.oldPos= oldPos;
		this.newPos = newPos;
		this.moveType = moveType;
		this.promotePieceType = promotePieceType; 
		this.rookPos =rookPos;
		this.dirOfRochade = dirOfRochade ;
		this.callbackType = callbackType;
		this.dirX=dirX;
		this.dirY=dirY;
		if(rookPos==-1) {
			rookMove = null;
		}else {
			rookMove = new Move(color, FieldCallback.CALLBACK_TYPE_OTHER, rookPos, oldPos+(dirOfRochade),MOVE_TYPE_PUSH_BEAT,dirX*-1,dirY);
		}
		if(moveType ==Move.MOVE_TYPE_PAWN_PUSH) {
			if(getRank(oldPos)==_2 &&  getRank(newPos)==_4) {
				isTwoSquarePush = true;
				enPassanteSquare = getPos(_3, getFile(newPos));
				enPassantePawnPos = getPos(_4, getFile(newPos));
			}else if(getRank(oldPos)==_7 &&  getRank(newPos)==_5) {
				isTwoSquarePush = true;
				enPassanteSquare = getPos(_6, getFile(newPos));
				enPassantePawnPos = getPos(_5, getFile(newPos));
			}else {
				isTwoSquarePush = false;
				enPassanteSquare = -1;
				enPassantePawnPos = -1;
			}
		}else if(moveType ==Move.MOVE_TYPE_PAWN_BEAT_OR_ENPASSANTE) {
			if(getRank(oldPos)==_5 &&  getRank(newPos)==_6) {
				isTwoSquarePush = false;
				enPassanteSquare = getPos(_6, getFile(newPos));
				enPassantePawnPos = getPos(_5, getFile(newPos));
			}else if(getRank(oldPos)==_4 &&  getRank(newPos)==_3) {
				isTwoSquarePush = false;
				enPassanteSquare = getPos(_3, getFile(newPos));
				enPassantePawnPos = getPos(_4, getFile(newPos));
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
			case Move.MOVE_TYPE_PUSH_BEAT:
			case Move.MOVE_TYPE_PAWN_BEAT:
			case Move.MOVE_TYPE_PAWN_BEAT_CONVERT:
			case Move.MOVE_TYPE_PAWN_BEAT_OR_ENPASSANTE:
						this.isAttackerMove=true;
				break;
			default:
				this.isAttackerMove=false;
		}
		this.isKingSensing = (moveType==Move.MOVE_TYPE_KING_SENSING);
		this.isRochade = this.dirOfRochade!=0;
	}
	
	
	public Move (Move move, FieldCallback cb,int ii, int jj, int moveIndex ) {
		this.ii =-1;
		this.jj =-1;
		this.fieldCB=cb;
		
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
		String val ="["+elementIndex+"] From "+oldPos+" to "+newPos+" : Promotion:"+this.getPromotePieceType();	
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
	
	public static int getRank(int pos) {
		return (int)(pos/8);
	}
	public static int getFile(int pos) {
		return (int)(pos%8);
	}

	
	public static int getPos(int rank, int file) {
 		int index = 8 * rank + file;
		return index;
	}
	public int getPromotePieceType() {
		return promotePieceType;
	}
	public boolean isPromotion() {
		return (promotePieceType!=-1);
	}
	public boolean isNoPromotionOrQueen() {
		return (promotePieceType==-1||promotePieceType==Piece.PIECE_TYPE_QUEEN);
	}
	
	
	public int getCallbackType() {
		return this.callbackType;
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
		int r1 = Move.getRank(oldPos);
		int f1 = Move.getFile(oldPos);
		int r2 = Move.getRank(newPos);
		int f2 = Move.getFile(newPos);
		String promotion = "";
		if(this.getPromotePieceType()!=-1) {
			switch(this.getPromotePieceType()) {
			case Piece.PIECE_TYPE_BISHOP:
				promotion = "b";
				break;
			case Piece.PIECE_TYPE_KNIGHT:
				promotion = "n";
				break;
			case Piece.PIECE_TYPE_QUEEN:
				promotion = "q";
				break;
			case Piece.PIECE_TYPE_ROOK:
				promotion = "r";
				break;
			}
			
		}
		return ""+((char)('a'+f1))  +""+ (8-r1)+""
				+((char)('a'+f2))  +""+ (8-r2)+promotion+"";
	}	
	
	public int getII() {
		return ii;
	}
	public int getJJ() {
		return jj;
	}
	public FieldCallback  getFieldCB() {
		return fieldCB;
	}
	public int getColor() {
		return color;
	}
	
}
