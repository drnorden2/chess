package perft.chess.core.baseliner;

import perft.chess.core.datastruct.BitBoard;

public class BLVariableLong {
		private final int bL_Index;
		private BLArrayStackLong varStack;
		private final BaseLiner bl;
		public BLVariableLong (BaseLiner bl, long initVal) {
			this.bl = bl;
			bL_Index = bl.getCurrOffsetRegisterLong(1);
			varStack = bl.getVarStacksLong(bL_Index);
			setTouchlessInt(initVal);
		}
		public BLVariableLong (BaseLiner bl) {
			this(bl,0L);		
		}
		public void setTouchlessInt(long value) {
			varStack.addAndTouched(value);
		}

		public void set(long value) {
			if(varStack.addAndTouched(value)) {
				bl.touch(bL_Index);
			}
		}
		
		public void toggleBit(int bitIndex) {
			if(varStack.toggleBitTouched(bitIndex)){
				bl.touch(bL_Index);
			}
		}
		
		public long get() {
			return varStack.get();
		}
		public int getChanges() {
			return varStack.stackSize();
		}
		public void decr() {
			if(varStack.decrAndTouched()) {
				bl.touch(bL_Index);
			}
		}
		public void incr() {
			if(varStack.incrAndTouched()) {
				bl.touch(bL_Index);
			}
		}
		
		public void XOR(long value) {
			if(varStack.xorAndTouched(value)) {
				bl.touch(bL_Index);
			}
		}
		
		public String toString() {
			return ""+get();
		}	
	}
