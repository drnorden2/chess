package perft.chess.perftmb;

import perft.chess.core.datastruct.IndexedElement;

public class FieldCallback implements IndexedElement{
			
	private final Field field;
	private final int ii;
	private final int jj;
	private final int dirX;
	private final int dirY;
	private final int elementIndex;
	private final int color;
	private final int moveIndex;
	private final boolean isPromotion;
	
	private final int callbackType;
	public final static int CALLBACK_TYPE_PUSH_RAY =0;
	public final static int CALLBACK_TYPE_PUSH_ONE =1;
	public final static int CALLBACK_TYPE_KING_SENSING=2;
	public final static int CALLBACK_TYPE_ROCHADE_TEST = 3;
	public final static int CALLBACK_TYPE_CHECK_KNIGHT_ATTACK = 4;

	public final static int CALLBACK_TYPE_BEAT_ONE =5;
	public final static int CALLBACK_TYPE_BEAT_ONE_AS_PAWN=6;
	public final static int CALLBACK_TYPE_OTHER = 7;
	public final static int CALLBACK_TYPE_BEAT_RAY =8;	
	public final static int CALLBACK_TYPE_BEAT_ONE_AS_KING = 9;
	
	
	
	public FieldCallback(Field field, Move move, int ii, int jj, int moveIndex){
		this.field=field;
		this.moveIndex = moveIndex;
		this.dirX = move.getDirX();
		this.dirY= move.getDirY();
		this.color = move.getColor();
		this.isPromotion = move.isPromotion();
		this.callbackType = move.getCallbackType();
		if(callbackType==FieldCallback.CALLBACK_TYPE_ROCHADE_TEST) {
			this.jj = -1;
		}else {
			this.jj = jj;
		}
		if(move.isPromotion()) {//@todo: think about optimization 
			this.ii = -1;
		}else {
			this.ii = ii;
		}
		this.elementIndex = move.getOldPos();
	}

	public boolean isPromotion() {
		return isPromotion;
	}
	
	public int getElementIndex() {
		return elementIndex;
	}
	public int getII() {
		return ii;
	}
	public int getJJ() {
		return jj;
	}

	public int getDirX() {
		return dirX;
	}
	public int getDirY() {
		return dirY;
	}
	public int getCallbackType() {
		return this.callbackType;
	}

	public Field getField() {
		return field;
	}

	public String toString() {
		return "FieldCallBack for "+field+" will be triggered from i:"+ii+" j:"+jj;
	}
	public int  getColor() {
		return color;
	}
	public int getMoveIndex() {
		return moveIndex;
	}
}

