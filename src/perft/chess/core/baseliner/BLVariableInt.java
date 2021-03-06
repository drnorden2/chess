package perft.chess.core.baseliner;


public class BLVariableInt {
	private final int bL_Index;
	private BLArrayStackInt varStack;
	private final BaseLiner bl;
	public BLVariableInt (BaseLiner bl, int initVal) {
		this.bl = bl;
		bL_Index = bl.getCurrOffsetRegisterInt(1);
		varStack = bl.getVarStacksInt(bL_Index);
		setTouchlessInt(initVal);
	}
	public BLVariableInt (BaseLiner bl) {
		this(bl,0);		
	}
	public void setTouchlessInt(int value) {
		varStack.addAndTouched(value);
	}

	public void set(int value) {
		if(varStack.addAndTouched(value)) {
			bl.touchInt(bL_Index);
		}
	}
	public int get() {
		return varStack.get();
	}
	public int getAndSet(int value) {
		int ret = get();
		if(varStack.addAndTouched(value)) {
			bl.touchInt(bL_Index);
		}
		return ret;
	}
	
	public int getChanges() {
		return varStack.stackSize();
	}
	public void decr() {
		if(varStack.decrAndTouched()) {
			bl.touchInt(bL_Index);
		}
	}
	public void incr() {
		if(varStack.incrAndTouched()) {
			bl.touchInt(bL_Index);
		}
	}
	public void addition(int value) {
		if(varStack.additionAndTouched(value)) {
			bl.touchInt(bL_Index);
		}
	}
	public void subtraction(int value) {
		if(varStack.subtractionAndTouched(value)) {
			bl.touchInt(bL_Index);
		}
	}
	
	public void XOR(int value) {
		if(varStack.xorAndTouched(value)) {
			bl.touchInt(bL_Index);
		}
	}
	
	public String toString() {
		return ""+get();
	}	
}
