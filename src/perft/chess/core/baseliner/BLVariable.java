package perft.chess.core.baseliner;

public class BLVariable<T> {
	private final int bL_Index;
	private final BaseLiner bl;
	private final BLArrayStack varStack;
	public BLVariable (BaseLiner bl, T initVal) {
		this.bl = bl;
		bL_Index = bl.getCurrOffsetRegister(1);
		varStack = bl.getVarStacksObj(bL_Index);
		setTouchless(initVal);
	}
	public BLVariable (BaseLiner bl) {
		this.bl = bl;
		bL_Index = bl.getCurrOffsetRegister(1);
		varStack = bl.getVarStacksObj(bL_Index);
	}
	private void setTouchless(T value) {
		varStack.addAndTouched(value);
	}

	public void set(T value) {
		if(varStack.addAndTouched(value)) {
			bl.touchObj(bL_Index);
		}
	}
	public T get() {
		return (T)varStack.get();
	}
	public int getChanges() {
		return varStack.stackSize();
	}
	public String toString() {
		return ""+get();
	}
}
