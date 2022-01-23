package perft;



import java.util.HashMap;

import perft.Player.PlayerType;
import perft.chess.core.datastruct.ArrayStack;
import perft.chess.perftbb.BBAnalyzer;


final public class Game {
	private boolean isHashed = true;
	private Player[] players;
	private Board board;
	private BoardUI boardUI;
	private static long moveCounter=0;
	private  HashMap<Long,Long>[]  hashMap;
	
	public Game(GameFactory chessFactory,String fen,int depth) {
		this(
				chessFactory.getSpecificBoard(fen,depth),
				chessFactory.getPlayer(PlayerType.RANDOM),
				chessFactory.getPlayer(PlayerType.RANDOM),
				chessFactory.getBoardUI()
			);		
		hashMap= new HashMap[depth+1];	
		for(int i=0;i<hashMap.length;i++) {
			hashMap[i]= new HashMap<Long,Long>();	
		} 

	}
	public final Board getBoard() {
		return board;
	}
	
	public Game(GameFactory chessFactory,int depth) {
		this(
				chessFactory.getInitialBoard(depth),
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
	
	final public long perft(int deep,boolean bulk) {
		return hashedPerft(deep,deep,bulk) ;
	}
	int cacheHits=0;
	boolean debugIgnore=false;

	final private  long hashedPerft(int deep,int depth, boolean bulk) {
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
			try {
				String moveStr ="";
				moveCounter++;
				if(level==0) {
					moveStr = board.getMoveStr(i);
				}				

				board.doMove(i);
			 
				long hash = this.board.getHash();
				Long curMoveCount  = 0L;
				if(!board.isGameOver()&& ((bulk && deep>2) || (!bulk && deep>1))){
					if(isHashed) {
						curMoveCount  = hashMap[deep].get(hash);
					}
					if(curMoveCount==null ||!isHashed) {
						
						
						curMoveCount = hashedPerft(deep-1,depth,bulk);
						
						
						
						
						if(level==0) {
							System.out.println(moveStr+" "+curMoveCount);
						}				
	
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
				
				moveCount +=curMoveCount;	
			}catch(Exception e){
				System.out.println("Error in Move:"+e);
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
	

	
	final public  long debugPerft(Board ref, int deep,ArrayStack<String> moveList) {
		if ( deep ==0) {
			return 1;
		}
		
		
		boolean base =false;
		if(moveList==null) {
			moveList = new ArrayStack<String>(new String[50]);
			base =true;
		}
		long moveCount=0;
		int moves = board.getMoves();
		
		int other = ref.getMoves();
		
		 
		if(moves!=other /*|| !equal(aW,bW)||!equal(aB,bB)*/) {
			System.out.println("Move mismatch:"+moves+" vs"+other +" in iteration:"+board.getTotalCount());
			for(int i=0;i<moveList.size();i++) {
				System.out.println((i+1)+":"+moveList.get(i));
			}
			String origTS = this.toString();
			String refTS = ref.toString();
			System.out.println(origTS);
			System.out.println(refTS);
			
			System.out.println(BBAnalyzer.diffPositions(origTS,refTS));
			System.exit(-1);
		}
		
		for(int i=0;i<moves;i++) {
			// Patch:
			/*
			if(base && !"d5d6".equals(moveStr)) {
				continue;
			}*/
			board.doMove(i);
			String moveStr = board.getMoveStr(i);
			//String moveStr = BBPosition.moveNotation;
			boolean worked = ref.setMoveByMoveStr(moveStr);
			if(!worked) {
				System.out.println("MoveStr not found!"+moves+" vs"+other +" in iteration:"+board.getTotalCount());
				for(int j=0;i<moveList.size();j++) {
					System.out.println((j+1)+":"+moveList.get(j));
				}
				String origTS = this.toString();
				String refTS = ref.toString();
				System.out.println(origTS);
				System.out.println(refTS);
				
				System.out.println(BBAnalyzer.diffPositions(origTS,refTS));
				System.exit(-1);

			}
			moveList.add(moveStr);
			long count =debugPerft(ref, deep-1,moveList);
			if(base) {
				System.out.println("("+(i+1)+"/"+moves+") "+moveStr +":"+count);
			}
			moveCount+=count;
			moveList.remove();
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
