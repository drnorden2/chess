package perft.chess.core.datastruct;
import java.lang.reflect.Array;

public class UndoIndexedList<T extends IndexedElement> extends IndexedList<T >{
	private ArrayStack[] allRemovesLists;
	private ArrayStack[] allAddsLists;
	private int level = -1;
	public UndoIndexedList (Class<T> clazz, int size, int indexRange,int depth) {
		super(clazz, size , indexRange);
		allRemovesLists=  new ArrayStack[depth];
		for(int i=0;i<depth;i++) {
			ArrayStack<T> list= new ArrayStack<T>((T[]) Array.newInstance(clazz, size));
			allRemovesLists[i] =list;
		}
		allAddsLists=  new ArrayStack[depth];
		for(int i=0;i<depth;i++) {
			ArrayStack<T> list= new ArrayStack<T>((T[]) Array.newInstance(clazz, size));
			allAddsLists[i] =list;
		}
		
		
	}

	public void startNewLevel() {
		level++;
		allAddsLists[level].reset();
		allRemovesLists[level].reset();
	}
	public void undo() {
		for(int i=0;i<allAddsLists[level].size();i++) {
		//	super.re
		}
		level--;
		
	}

	
	@Override
	public boolean add(T element) {
		// TODO Auto-generated method stub
		return super.add(element);
	}

	@Override
	public void removeAll() {
		// TODO Auto-generated method stub
		super.removeAll();
	}

	@Override
	public T removeFirstElement() {
		// TODO Auto-generated method stub
		return super.removeFirstElement();
	}

	@Override
	public boolean remove(T element) {
		// TODO Auto-generated method stub
		return super.remove(element);
	}
	

}
