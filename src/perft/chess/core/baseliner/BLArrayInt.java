package perft.chess.core.baseliner;


public class BLArrayInt {
	private final int dim;
	private final int bL_Index;
	private final BaseLiner bl;
	private BLArrayStackInt[] array; 
	
	public BLArrayInt(BaseLiner bl, int dim) {
		this(bl, dim, -1);
	}
	
	public BLArrayInt(BaseLiner bl, int dim, int nullVal) {
		this.bl = bl;
		bL_Index = bl.getCurrOffsetRegisterInt(dim);
		this.dim = dim;
		array = new BLArrayStackInt[dim];
		for(int i=0;i<dim;i++) {
			array[i]=bl.getVarStacksInt(bL_Index+i);
			this.setTouchLessInt(i, nullVal);
		}
	}
	
	public void incr(int index) {
		if(array[index].incrAndTouched()) {
			bl.touch(bL_Index+index);
		}
	}

	public void decr(int index) {
		if(array[index].decrAndTouched()) {
			bl.touch(bL_Index+index);
		}
	}

	
	public void set(int index, int value) {
		if(array[index].addAndTouched(value)) {
			bl.touch(bL_Index+index);
		}
	}
	
	private void setTouchLessInt(int index, int value) {
		array[index].addAndTouched(value);
	}
	
	public int get(int index) {
		return array[index].get();
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
