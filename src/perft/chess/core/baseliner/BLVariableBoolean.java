package perft.chess.core.baseliner;

public class BLVariableBoolean {
		private final int bL_Index;
		private final BaseLiner bl;
		public BLVariableBoolean (BaseLiner bl, boolean initVal) {
			this.bl = bl;
			bL_Index = bl.getCurrOffsetRegisterInt(1);
			setTouchlessInt(initVal);
		}
		public BLVariableBoolean (BaseLiner bl) {
			this(bl,false);
		}
		public void setTouchlessInt(boolean value) {
			bl.setIntTouchlessInt(bL_Index, value?0:1);
		}

		public void set(boolean value) {
			bl.setInt(bL_Index, value?0:1);
		}
		public boolean get() {
			return bl.getInt(bL_Index)==0;
		}
		public int getChanges() {
			return bl.getChangesInt(bL_Index);
		}
		public String toString() {
			return ""+bl.getInt(bL_Index);
		}
	}
