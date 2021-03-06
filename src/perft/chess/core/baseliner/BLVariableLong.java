package perft.chess.core.baseliner;


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
				bl.touchLong(bL_Index);
			}
		}
		
		public void toggleBit(int bitIndex) {
			if(varStack.toggleBitTouched(bitIndex)){
				bl.touchLong(bL_Index);
			}
		}
		public void moveBit(int oldIndex, int newIndex) {
			if(varStack.moveBitTouched(oldIndex, newIndex)){
				bl.touchLong(bL_Index);
			}
		}
		
		
		
		public void setBit(int bitIndex) {
			if(varStack.setBitTouched(bitIndex)){
				bl.touchLong(bL_Index);
			}
		}
		
		public void setBitTouchless(int bitIndex) {
			varStack.setBitTouched(bitIndex);
		}

		
		public void unsetBit(int bitIndex) {
			if(varStack.unsetBitTouched(bitIndex)){
				bl.touchLong(bL_Index);
			}
		}
		public boolean getBit(int bitIndex) {
			return varStack.getBit(bitIndex);
		}
		public int getBitAsInt(int bitIndex) {
			return varStack.getBitAsInt(bitIndex);
		}
		
		public int updateIndices(int[] indices) {
			return varStack.updateIndices(indices);
		}

		
		public long get() {
			return varStack.get();
		}
		public int getChanges() {
			return varStack.stackSize();
		}
		public void decr() {
			if(varStack.decrAndTouched()) {
				bl.touchLong(bL_Index);
			}
		}
		public void incr() {
			if(varStack.incrAndTouched()) {
				bl.touchLong(bL_Index);
			}
		}
		public void OR(long value) {
			if(varStack.orAndTouched(value)) {
				bl.touchLong(bL_Index);
			}
		}
		public void AND(long value) {
			if(varStack.andAndTouched(value)) {
				bl.touchLong(bL_Index);
			}
		}
		
		public void AND_NOT(long value) {
			if(varStack.andNotTouched(value)) {
				bl.touchLong(bL_Index);
			}
		}
		
		
		public void XOR(long value) {
			if(varStack.xorAndTouched(value)) {
				bl.touchLong(bL_Index);
			}
		}
		public long getAndSet(long value) {
			long ret = get();
			if(varStack.addAndTouched(value)) {
				bl.touchLong(bL_Index);
			}
			return ret;
		}

		
		public String toString() {
			return ""+get();
		}	
	}
