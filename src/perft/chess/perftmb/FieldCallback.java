package perft.chess.perftmb;

import static perft.chess.Definitions.*;

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
	
	
	
	public FieldCallback(Field field, Move move, int ii, int jj, int moveIndex){
		this.field=field;
		this.moveIndex = moveIndex;
		this.dirX = move.getDirX();
		this.dirY= move.getDirY();
		this.color = move.getColor();
		this.isPromotion = move.isPromotion();
		this.callbackType = move.getCallbackType();
		if(callbackType==CALLBACK_TYPE_ROCHADE_TEST) {
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

