package perft;



import java.util.HashMap;

import perft.Player.PlayerType;
import perft.chess.core.o.O;


final public class Game {
	private boolean isHashed = false;
	private Player[] players;
	private Board board;
	private BoardUI boardUI;
	private static long moveCounter=0;
	private  HashMap<Integer,Long>[]  hashMap;
	
	public Game(GameFactory chessFactory,String fen) {
		this(
				chessFactory.getSpecificBoard(fen),
				chessFactory.getPlayer(PlayerType.RANDOM),
				chessFactory.getPlayer(PlayerType.RANDOM),
				chessFactory.getBoardUI()
			);		

	}
	public final Board getBoard() {
		return board;
	}
	
	public Game(GameFactory chessFactory) {
		this(
				chessFactory.getInitialBoard(),
				chessFactory.getPlayer(PlayerType.RANDOM),
				chessFactory.getPlayer(PlayerType.RANDOM),
				chessFactory.getBoardUI()
			);		

	}	
	public Game(Board board, Player whitePlayer, Player blackPlayer, BoardUI boardUI) {
		this.players = new Player[] {whitePlayer, blackPlayer};
		this.board = board;
		this.boardUI = boardUI;
	}
	final public void play() {
		boardUI.show(board);
		while(!board.isGameOver()){
			int turn = board.getTurn();
			Player playerAtTurn = players[turn];
			int move = playerAtTurn.play(board);
			board.setMove(move);
			boardUI.show(board);
		}
	}
	public static long getMoveCounter() {
		return moveCounter;
	}
	
	final public long perft(int deep, String [] debugMoves, HashMap<String,Long>refMap,boolean bulk) {
		
		hashMap = new HashMap[deep+1];
		for(int i=0;i<hashMap.length;i++) {
			hashMap[i]= new HashMap<Integer,Long>();
		}
		if(isHashed ) {
			hashMap =null;
		}
		return hashedPerft(deep,deep,debugMoves,refMap, bulk) ;
	}
	int cacheHits=0;
	boolean debugIgnore=false;

	final private  long hashedPerft(int deep,int depth, String [] debugMoves, HashMap<String,Long>refMap,boolean bulk) {
		if(deep==0) {
			return 1;
		}
		
		long moveCount=0;
		int moves = board.getMoves();
		if (bulk && deep ==1) {
			return moves;
		}
		int level = depth - deep;
		
		for(int i=0;i<moves;i++) {
			String moveStr = board.getMoveStr(i);
			boolean printMove=false;
			try {
				if(!debugIgnore && level<debugMoves.length) {
					if(debugMoves[level].equals(moveStr)) {
						System.out.println("Move:" + moveStr+" ");
						printMove=true;
					}else {
						if(debugMoves[level].startsWith(moveStr)){
							System.out.println("Restarting w.Move:" + moveStr+" ");
							printMove=true;
							debugIgnore=true;
						}else {
							continue;
						}
					}
				}
				
				moveCounter++;
				
				board.doMove(i);
				if(printMove) {
					System.out.println(board);
				}

				int hash = this.board.getHash();
					
			
				Long curMoveCount  = 0L;
				if(!board.isGameOver()&& ((bulk && deep>2) || (!bulk && deep>1))){
					if(isHashed) {
						curMoveCount  = hashMap[deep].get(hash);
					}
					if(curMoveCount==null ||!isHashed) {
						curMoveCount=0L;
						curMoveCount = hashedPerft(deep-1,depth,debugMoves, refMap,bulk);
						if(isHashed) {	
							hashMap[deep].put(hash, curMoveCount);
						}
					}else {
						cacheHits+=curMoveCount;
					}
				}else if(bulk && deep==2) {
					curMoveCount+=board.getMoves();;
				}else if(deep==1) {
					curMoveCount+=1;
				}
			
				board.undoMove();
				
				if(debugMoves.length==level) {
					if(refMap!=null) {
						Long val = refMap.get(moveStr);
						val= (val==null?0:val);
						if(((long)val)!=curMoveCount) {
							System.out.println("Mismatch for Move:"+moveStr+": (ref:"+val+")!=(cur:"+curMoveCount+")");
							throw new RuntimeException("Mismatch for Move:"+moveStr+": (ref:"+val+")!=(cur:"+curMoveCount+")");
						}
					}
					System.out.println(" "+moveStr+":"+ curMoveCount + "("+cacheHits+")");
				}
				moveCount +=curMoveCount;	
			}catch(Exception e){
				System.out.println("Error in Move:"+moveStr);
				//System.out.println(board);
				e.printStackTrace();
				throw e;
			}
		}
		return moveCount;
	}

	final public  long bulkPerft(int deep) {
		
		long moveCount=0;
		int moves = board.getMoves();
		if ( deep ==1) {
			return moves;
		}
		for(int i=0;i<moves;i++) {
			moveCounter++;
			board.doMove(i);
			moveCount +=bulkPerft(deep-1);
			board.undoMove();
		}
		return moveCount;
	}

	final public  long debugPerft(Board ref, int deep) {
		long moveCount=0;
		int moves = board.getMoves();
		int other = ref.getMoves();
		
		if(moves!=other) {
			System.out.println("unexpeceded Move amount: "+moves+" (ref:"+other+")");
			System.out.println(this.toString());
			System.out.println(ref.toString());
			
			
			System.exit(-1);
		}
		
		if ( deep ==0) {
			return 1;
		}
		for(int i=0;i<moves;i++) {
			String moveStr = board.getMoveStr(i);
			board.doMove(i);
			O.UT("***********************************************");
			O.UT((i+1)+"/"+moves+":"+ moveStr +"("+deep+")" +this.toString());
			ref.setMoveByMoveStr(moveStr);
			O.UT(ref.toString());
			O._push("     ");
			moveCount +=debugPerft(ref, deep-1);
			O.UT(moveStr+"Movecount("+deep+"):"+moveCount);  
			O._pop();
			board.undoMove();
			ref.undoMove();
		}
		return moveCount;
	}

	
	@Override
	public String toString() {
		return board.toString();
	}
}
