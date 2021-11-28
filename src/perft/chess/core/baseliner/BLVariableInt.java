package perft.chess.core.baseliner;

public class BLVariableInt {
	private final int bL_Index;
	private final BaseLiner bl;
	public BLVariableInt (BaseLiner bl, int initVal) {
		this.bl = bl;
		bL_Index = bl.getCurrOffsetRegisterInt(1);
		setTouchlessInt(initVal);
	}
	public BLVariableInt (BaseLiner bl) {
		this(bl,0);
	}
	public void setTouchlessInt(int value) {
		bl.setIntTouchlessInt(bL_Index, value);
	}

	public void set(int value) {
		bl.setInt(bL_Index, value);
	}
	public int get() {
		return bl.getInt(bL_Index);
	}
	public int getChanges() {
		return bl.getChangesInt(bL_Index);
	}
	public void decr() {
		bl.decrInt(bL_Index);
	}
	public void incr() {
		bl.decrInt(bL_Index);
	}
	
	public void XOR(int value) {
		bl.xorInt(bL_Index,value);
	}
	
	public String toString() {
		return ""+bl.getInt(bL_Index);
	}
}
