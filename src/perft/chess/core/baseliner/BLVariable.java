package perft.chess.core.baseliner;

public class BLVariable<T> {
	private final int bL_Index;
	private final BaseLiner bl;
	public BLVariable (BaseLiner bl, T initVal) {
		this.bl = bl;
		bL_Index = bl.getCurrOffsetRegister(1);
		setTouchless(initVal);
	}
	public BLVariable (BaseLiner bl) {
		this.bl = bl;
		bL_Index = bl.getCurrOffsetRegister(1);
	}
	private void setTouchless(T value) {
		bl.setObjTouchless(bL_Index, value);
	}

	public void set(T value) {
		bl.setObj(bL_Index, value);
	}
	public T get() {
		return (T) bl.getObj(bL_Index);
	}
	public int getChanges() {
		return bl.getChanges(bL_Index);
	}
	public String toString() {
		return ""+bl.getObj(bL_Index);
	}
}
