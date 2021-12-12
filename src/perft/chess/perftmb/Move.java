package perft.chess.perftmb;

import perft.chess.core.baseliner.BaseLiner;
import perft.chess.core.datastruct.IndexedElement;
import static perft.chess.Definitions.*;


public class Move implements IndexedElement{
	

	
	public static final int INDEX_Counter_ID =0;
	private final String notation;
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
		if(moveType ==MOVE_TYPE_PAWN_PUSH) {
			if(getRankForPos(oldPos)==_2 &&  getRankForPos(newPos)==_4) {
				isTwoSquarePush = true;
				enPassanteSquare = getPosForRankFile(_3, getFileForPos(newPos));
				enPassantePawnPos = getPosForRankFile(_4, getFileForPos(newPos));
			}else if(getRankForPos(oldPos)==_7 &&  getRankForPos(newPos)==_5) {
				isTwoSquarePush = true;
				enPassanteSquare = getPosForRankFile(_6, getFileForPos(newPos));
				enPassantePawnPos = getPosForRankFile(_5, getFileForPos(newPos));
			}else {
				isTwoSquarePush = false;
				enPassanteSquare = -1;
				enPassantePawnPos = -1;
			}
		}else if(moveType ==MOVE_TYPE_PAWN_BEAT_OR_ENPASSANTE) {
			if(getRankForPos(oldPos)==_5 &&  getRankForPos(newPos)==_6) {
				isTwoSquarePush = false;
				enPassanteSquare = getPosForRankFile(_6, getFileForPos(newPos));
				enPassantePawnPos = getPosForRankFile(_5, getFileForPos(newPos));
			}else if(getRankForPos(oldPos)==_4 &&  getRankForPos(newPos)==_3) {
				isTwoSquarePush = false;
				enPassanteSquare = getPosForRankFile(_3, getFileForPos(newPos));
				enPassantePawnPos = getPosForRankFile(_4, getFileForPos(newPos));
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
		
	}
	
	
	public Move (Move move, FieldCallback cb,int ii, int jj, int moveIndex ) {
		this.ii =-1;
		this.jj =-1;
		this.fieldCB=cb;
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
	
		public int getPromotePieceType() {
		return promotePieceType;
	}
	public boolean isPromotion() {
		return (promotePieceType!=-1);
	}
	public boolean isNoPromotionOrQueen() {
		return (promotePieceType==-1||promotePieceType==PIECE_TYPE_QUEEN);
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
		return notation;
	}
	
	private  String generateNotation () {
		int r1 = getRankForPos(oldPos);
		int f1 = getFileForPos(oldPos);
		int r2 = getRankForPos(newPos);
		int f2 = getFileForPos(newPos);
		String promotion = "";
		if(this.getPromotePieceType()!=-1) {
			switch(this.getPromotePieceType()) {
			case PIECE_TYPE_BISHOP:
				promotion = "b";
				break;
			case PIECE_TYPE_KNIGHT:
				promotion = "n";
				break;
			case PIECE_TYPE_QUEEN:
				promotion = "q";
				break;
			case PIECE_TYPE_ROOK:
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
