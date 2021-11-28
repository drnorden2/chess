package perft.chess.core.baseliner;


public class BLArrayInt {
	private final int dim;
	private final int bL_Index;
	private final BaseLiner bl;
	
	public BLArrayInt(BaseLiner bl, int dim) {
		this(bl, dim, -1);
	}
	
	public BLArrayInt(BaseLiner bl, int dim, int nullVal) {
		this.bl = bl;
		bL_Index = bl.getCurrOffsetRegisterInt(dim);
		this.dim = dim;
		for(int i=0;i<dim;i++) {
			this.setTouchLessInt(i, nullVal);
		}
	}
	public void incr(int index) {
		bl.incrInt(bL_Index+index);
	}

	public void decr(int index) {
		bl.decrInt(bL_Index+index);
	}

	
	public void set(int index, int value) {
		bl.setInt(bL_Index+index, value);
	}
	private void setTouchLessInt(int index, int value) {
		bl.setIntTouchlessInt(bL_Index+index, value);
	}
	
	public int get(int index) {
		return bl.getInt(bL_Index+index);
	}
	public String toString(){
		String str="[";
		for(int i=0;i<dim;i++) {
			str+=""+i+ ":"+this.get(i)+" ";
		}
		str+="]";
		return str;
	}
	public int length() {
		return dim;
	}
}
