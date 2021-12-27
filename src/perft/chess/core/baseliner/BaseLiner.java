package perft.chess.core.baseliner;

import perft.chess.core.datastruct.ArrayStackInt;

public class BaseLiner {
	private static final int OBJ_TOUCHED_LIST = 0;
	
	int level =1;
	private int registry1 =1;
	private int registry2 =1;
	private int registry3 =1;
	
	
	private ArrayStackInt touchedList;
	private ArrayStackInt[] touchedListPool;
	private final BLArrayStack[] varStacks;
	private final BLArrayStackInt[] varStacksInt;
	private final BLArrayStackLong[] varStacksLong;
	
	private final int[] indexCounters = new int[10];
	private final int totalObj;
	private final int totalObjAndInt;
	
	public boolean isAlreadyInSimulation =false;

	//100000,100,500)
	
	public  BaseLiner (int totalObj, int totalInt, int totalLong, int maxLevel,int maxLocalChanges){
		this.totalObj = totalObj;
		this.totalObjAndInt = totalObj+totalInt;
		registry2 = totalObj;
		registry3 = totalObjAndInt ;
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
		
		touchedListPool = new ArrayStackInt[maxLevel];
		
		for(int i=0;i<touchedListPool.length;i++) {
			touchedListPool[i] =  new ArrayStackInt(maxLocalChanges);
		}
		touchedList = touchedListPool[level].reset();
	}
	
	public int getCurrOffsetRegister(int x) {
		int cur = registry1;
		registry1  +=x;
		//System.out.println("Grown to: "+registry1+" by:"+x);
		return cur;
	}

	public int getCurrOffsetRegisterInt(int x) {
		int cur = registry2;
		registry2  +=x;
		//System.out.println("Grown to: "+registry2+" by:"+x);
		return cur;
	}
	public int getCurrOffsetRegisterLong(int x) {
		int cur = registry3;
		registry3  +=x;
		//System.out.println("Grown to: "+registry2+" by:"+x);
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
			}else if(index <totalObjAndInt){
				varStacksInt[index-this.totalObj].remove();
			}else {
				varStacksLong[index-this.totalObjAndInt].remove();				
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

	
	void setObjTouchless(int index, Object value) {
		varStacks[index].addAndTouched(value);
	}
	
	BLArrayStack getVarStacksObj(int index) {
		return varStacks[index];
	}	
	BLArrayStackInt getVarStacksInt(int index) {
		return varStacksInt[index-totalObj];
	}
	BLArrayStackLong getVarStacksLong(int index) {
		return varStacksLong[index-totalObjAndInt];
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
	
	public void touch(int index) {
		touchedList.add(index);
	}
	
		
	
}


