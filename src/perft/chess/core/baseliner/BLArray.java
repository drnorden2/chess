package perft.chess.core.baseliner;

public class BLArray<T> {
	private final int dim;
	private final int bL_Index;
	private final BaseLiner bl;
	private BLArrayStack[] array; 
	public BLArray(BaseLiner bl, int dim) {
		this(bl, dim, null);
	}
	
	public BLArray(BaseLiner bl, int dim, T nullVal) {
		this.bl = bl;
		bL_Index = bl.getCurrOffsetRegister(dim);
		this.dim = dim;
		array = new BLArrayStack[dim];
		for(int i=0;i<dim;i++) {
			array[i]=bl.getVarStacksObj(bL_Index+i);
			this.setTouchLess(i, nullVal);
		}
	}
	
	public void set(int index, T value) {
		if(array[index].addAndTouched(value)) {
			bl.touch(bL_Index+index);
		}
	}
	private void setTouchLess(int index, T value) {
		array[index].addAndTouched(value);
	}
	
	public T get(int index) {
		if(index==-1) {
			return null;
		}
		return (T)array[index].get();
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
	
	public static void main(String[] args) {
		BaseLiner bl = new BaseLiner(50,50,20,20);
		BLArray myIntArray= new BLArray(bl,20,20);
		System.out.println(myIntArray);
		bl.startNextLevel();
		for(int i=0;i<10;i++) {
			myIntArray.set(i, myIntArray.length()-i);
		}
		System.out.println(myIntArray);
		bl.startNextLevel();
		for(int i=5;i<myIntArray.length();i++) {
			myIntArray.set(i, i);
		}
		System.out.println(myIntArray);
		bl.undo();
		System.out.println(myIntArray);
		bl.undo();
		System.out.println(myIntArray);
		
		
	}	
}
