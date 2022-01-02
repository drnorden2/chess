package perft.chess.core.baseliner;

import perft.chess.core.datastruct.ArrayStackInt;

public class BaseLiner {
	private static final int OBJ_TOUCHED_LIST = 0;
	private static final int INT_TOUCHED_LIST = 1;
	private static final int LONG_TOUCHED_LIST = 2;
		
	
	int level =1;
	private int registryObj;
	private int registryInt;
	private int registryLong;
	
	
	private ArrayStackInt touchedListInt;
	private ArrayStackInt touchedListLong;
	private ArrayStackInt touchedListObj;
	private ArrayStackInt[] touchedListIntPool;
	private ArrayStackInt[] touchedListLongPool;
	private ArrayStackInt[] touchedListObjPool;
	
	private final BLArrayStack[] varStacks;
	private final BLArrayStackInt[] varStacksInt;
	private final BLArrayStackLong[] varStacksLong;
	
	private final int[] indexCounters = new int[10];
	
	public boolean isAlreadyInSimulation =false;

	//100000,100,500)
	
	public  BaseLiner (int totalObj, int totalInt, int totalLong, int maxLevel,int maxLocalChanges){
		registryObj = 3;
		registryInt = 0;
		registryLong = 0;
		varStacks = new BLArrayStack[totalObj];
		
		for(int i=0;i<varStacks.length;i++) {
			varStacks[i]= new BLArrayStack(this,maxLevel);
		}
		varStacksInt = new BLArrayStackInt[totalInt];
		for(int i=0;i<varStacksInt.length;i++) {
			varStacksInt[i]= new BLArrayStackInt(this,maxLevel);
		}
		varStacksLong = new BLArrayStackLong[totalLong];
		for(int i=0;i<varStacksLong.length;i++) {
			varStacksLong[i]= new BLArrayStackLong(this,maxLevel);
		}
		
		touchedListIntPool = new ArrayStackInt[maxLevel];
		touchedListLongPool = new ArrayStackInt[maxLevel];
		touchedListObjPool = new ArrayStackInt[maxLevel];
		
		for(int i=0;i<maxLevel;i++) {
			touchedListIntPool[i] =  new ArrayStackInt(maxLocalChanges);
			touchedListLongPool[i] =  new ArrayStackInt(maxLocalChanges);
			touchedListObjPool[i] =  new ArrayStackInt(maxLocalChanges);
		}
		touchedListInt = touchedListIntPool[level].reset();
		touchedListLong = touchedListLongPool[level].reset();
		touchedListObj = touchedListObjPool[level].reset();
	}
	
	public int getCurrOffsetRegister(int x) {
		int cur = registryObj;
		registryObj  +=x;
		//System.out.println("Grown to: "+registry1+" by:"+x);
		return cur;
	}

	public int getCurrOffsetRegisterInt(int x) {
		int cur = registryInt;
		registryInt  +=x;
		//System.out.println("Grown to: "+registry2+" by:"+x);
		return cur;
	}
	public int getCurrOffsetRegisterLong(int x) {
		int cur = registryLong;
		registryLong  +=x;
		//System.out.println("Grown to: "+registry2+" by:"+x);
		return cur;
	}

	
	
	public void startNextLevel() {
		setOldTouchedList(touchedListInt,touchedListLong,touchedListObj);
		
		touchedListInt = touchedListIntPool[level].reset();		
		touchedListLong = touchedListLongPool[level].reset();		
		touchedListObj = touchedListObjPool[++level].reset();		
		}

	
	public int getLevel() {
		return level;
	}
	public void undo() {
		int size = touchedListObj.size();
		for(int i=0;i<size;i++) {
			varStacks[touchedListObj.remove()].remove();			
		}
		touchedListObj.reset();
		touchedListObj =getTouchedListObj();
		
		size = touchedListInt.size();
		for(int i=0;i<size;i++) {
			varStacksInt[touchedListInt.remove()].remove();			
		}
		
		touchedListInt.reset();
		touchedListInt =getTouchedListInt();
		
		size = touchedListLong.size();
		for(int i=0;i<size;i++) {
			varStacksLong[touchedListLong.remove()].remove();			
		}		
		touchedListLong.reset();
		touchedListLong =getTouchedListLong();
		
		level--;
	}

	private void setOldTouchedList(ArrayStackInt touchedListInt,ArrayStackInt touchedListLong,ArrayStackInt touchedListObj) {
		if(varStacks[OBJ_TOUCHED_LIST].addAndTouched(touchedListObj)) {
			touchedListObj.add(OBJ_TOUCHED_LIST);
		}
		if(varStacks[INT_TOUCHED_LIST].addAndTouched(touchedListInt)) {
			touchedListObj.add(INT_TOUCHED_LIST);
		}
		if(varStacks[LONG_TOUCHED_LIST].addAndTouched(touchedListLong)) {
			touchedListObj.add(LONG_TOUCHED_LIST);
		}
	}
	
	
	
	private ArrayStackInt getTouchedListObj() {
		return (ArrayStackInt)varStacks[OBJ_TOUCHED_LIST].get();
	}
	private ArrayStackInt getTouchedListInt() {
		return (ArrayStackInt)varStacks[INT_TOUCHED_LIST].get();
	}
	private ArrayStackInt getTouchedListLong() {
		return (ArrayStackInt)varStacks[LONG_TOUCHED_LIST].get();
	}

	
	void setObjTouchless(int index, Object value) {
		varStacks[index].addAndTouched(value);
	}
	
	BLArrayStack getVarStacksObj(int index) {
		return varStacks[index];
	}	
	BLArrayStackInt getVarStacksInt(int index) {
		return varStacksInt[index];
	}
	BLArrayStackLong getVarStacksLong(int index) {
		return varStacksLong[index];
	}
	
	public int incrementIndexCounter(int indexCounterID) {
		return indexCounters[indexCounterID]++;
	}
	public int resetCounter(int indexCounterID) {
		return indexCounters[indexCounterID]=0;
	}
	public int getCounter(int indexCounterID) {
		return indexCounters[indexCounterID];
	}
	
	public void touchInt(int index) {
		touchedListInt.add(index);
	}
	public void touchLong(int index) {
		touchedListLong.add(index);
	}
	public void touchObj(int index) {
		touchedListObj.add(index);
	}
	
		
	
}


