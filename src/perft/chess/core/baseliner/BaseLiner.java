package perft.chess.core.baseliner;

import perft.chess.core.datastruct.ArrayStackInt;

public class BaseLiner {
	private static final int OBJ_TOUCHED_LIST = 0;
	
	private int level =1;
	private int registry1 =1;
	private int registry2 =1;
	
	private ArrayStackInt touchedList;
	private ArrayStackInt[] touchedListPool;
	private final BLArrayStack[] varStacks;
	private final BLArrayStackInt[] varStacksInt;
	
	private final int[] indexCounters = new int[10];
	private final int totalObj;
	public static boolean isAlreadyInSimulation =false;

	//100000,100,500)
	public BaseLiner (int totalObj, int totalInt, int maxLevel,int maxLocalChanges){
		this.totalObj = totalObj;
		registry2 = totalObj;
		
		varStacks = new BLArrayStack[totalObj];
		for(int i=0;i<varStacks.length;i++) {
			varStacks[i]= new BLArrayStack(this,maxLevel);
		}
		varStacksInt = new BLArrayStackInt[totalInt];
		for(int i=0;i<varStacksInt.length;i++) {
			varStacksInt[i]= new BLArrayStackInt(this,maxLevel);
		}
		
		touchedListPool = new ArrayStackInt[maxLevel];
		
		for(int i=0;i<touchedListPool.length;i++) {
			touchedListPool[i] =  new ArrayStackInt(maxLocalChanges);
		}
		touchedList = touchedListPool[level].reset();
	}
	
	public int getCurrOffsetRegister(int x) {
		int cur = registry1;
		registry1  +=x;
		return cur;
	}

	public int getCurrOffsetRegisterInt(int x) {
		int cur = registry2;
		registry2  +=x;
		return cur;
	}

	public void startNextLevel() {
		setOldTouchedList(touchedList);
		touchedList = touchedListPool[++level].reset();		
	}

	
	public int getLevel() {
		return level;
	}
	public void undo() {
		int size = touchedList.size();
		for(int i=0;i<size;i++) {
			int index = touchedList.remove();
			if(index < totalObj) {
				varStacks[index].remove();
			}else {
				varStacksInt[index-this.totalObj].remove();
			}
		}
		touchedList.reset();
		touchedList = getTouchedList();
		level--;
	}

	private void setOldTouchedList(ArrayStackInt touchedList) {
		if(varStacks[OBJ_TOUCHED_LIST].addAndTouched(touchedList)) {
			touchedList.add(OBJ_TOUCHED_LIST);
		}
	}
	
	private ArrayStackInt getTouchedList() {
		return (ArrayStackInt)varStacks[OBJ_TOUCHED_LIST].get();
	}

	//@@TODO touching and untouching (idea untouch => untouch sensitive touchlist
	void setObj(int index, Object value) {
		if(varStacks[index].addAndTouched(value)) {
			touchedList.add(index);
		}
	}
	
	void setObjTouchless(int index, Object value) {
		varStacks[index].addAndTouched(value);
	}
	
	Object getObj(int index) {
		return varStacks[index].get();
	}
	
	int getChanges(int index) {
		return varStacks[index].stackSize();
	}
	
	void setInt(int index, int value) {
		if(varStacksInt[index-totalObj].addAndTouched(value)) {
			touchedList.add(index);
		}
	}
	
	void incrInt(int index) {
		if(varStacksInt[index-totalObj].incrAndTouched()) {
			touchedList.add(index);
		}
	}
	
	void decrInt(int index) {
		if(varStacksInt[index-totalObj].decrAndTouched()) {
			touchedList.add(index);
		}
	}
	
	void xorInt(int index,int value) {
		if(varStacksInt[index-totalObj].xorAndTouched(value)) {
			touchedList.add(index);
		}
	}
	
	void setIntTouchlessInt(int index, int value) {
		varStacksInt[index-totalObj].addAndTouched(value);
	}
	
	int getInt(int index) {
		return varStacksInt[index-totalObj].get();
	}
	
	
	int getChangesInt(int index) {
		return varStacksInt[index-totalObj].stackSize();
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
	
}
