package perft;



import java.util.Arrays;
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
	private  HashMap<Long,Double>[]  hashScore;
	private  HashMap<Long,long[]>[]  hashMap2;
	public int hits=0;
	public int depth;
	
	public Game(GameFactory chessFactory,String fen,int depth) {
		this(
				chessFactory.getSpecificBoard(fen,depth),
				chessFactory.getPlayer(PlayerType.RANDOM),
				chessFactory.getPlayer(PlayerType.RANDOM),
				chessFactory.getBoardUI()
			);		
		this.depth= depth;
		hashMap= new HashMap[depth+1];	
		hashMap2= new HashMap[depth+1];	
		hashScore= new HashMap[depth+1];	
		for(int i=0;i<hashMap.length;i++) {
			hashMap[i]= new HashMap<Long,Long>();	
			hashMap2[i]= new HashMap<Long,long[]>();	
			hashScore[i]= new HashMap<Long,Double>();	
		} 

	}
	public final Board getBoard() {
		return board;
	}
	
	public Game(GameFactory chessFactory,int depth) {
		this(
				chessFactory.getInitialBoard(100),
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
	public static long getMoveCounter() {
		return moveCounter;
	}
	
	final public long perft(int deep,boolean bulk) {
		return hashedPerft(deep,deep,bulk) ;
	}
	int cacheHits=0;
	boolean debugIgnore=false;

	
	
	final public  long bulkPerft(int deep) {
		
		long moveCount=0;
		int moves = board.getMoves();
		if ( deep ==1) {
			return moves;
		}
		for(int i=0;i<moves;i++) {
			moveCounter++;
			board.doMove(i,true,deep==2);
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
			board.doMove(i,true,deep==2);
			String moveStr = board.getMoveStr(i);
			//String moveStr = BBPosition.moveNotation;
			boolean worked = ref.setMoveByMoveStr(moveStr,true,deep==2);
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

/*
final private void mlPerft(long[] counts, int deep,int depth, boolean bulk) {
	int moves = board.getMoves();
	if(deep>1) {
		for(int i=0;i<moves;i++) {
			String moveStr = board.getMoveStr(i);
			moveCounter++;
			board.doMove(i);
			long hash = board.getHash();
			long[] curCounts = hashMap[deep].get(hash);
			if(curCounts ==null) {
				curCounts = new long[deep-1];
				mlPerft(curCounts, deep-1,depth, bulk);
				//for(int j=1;j<deep;j++) {
					hashMap[deep].put(hash,curCounts);			
				//}
			}
			for(int j=0;j<deep-1;j++) {
				counts[j]+=curCounts[j];
			}
		
			
			if(depth == deep) System.out.println(moveStr+" "+curCounts[0]);
			board.undoMove();
		}
		
	}
	counts[deep-1]+=moves;
}*/

	final private  long hashedPerft2(int deep,int depth, boolean bulk) {
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

				board.doMove(i,true,deep==2);
			 
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

	
		
	final private  long hashedPerft(int deep,int depth, boolean bulk) {
		int moves = board.getMoves();
		long counter = 0;
		for(int i=0;i<moves;i++) {
			board.doMove(i,true,deep==2);
			if(deep==2) {
				counter += board.getMoves();
			}else {
				long hash = board.getHash();
				Long curCount = hashMap[deep].get(hash);
				if(curCount ==null) {
					curCount = hashedPerft(deep-1,depth, bulk);
					hashMap[deep].put(hash,curCount);			
				}else {
					hits++;
					
				}
				counter +=curCount;
		
			}
			board.undoMove();
		}
		return counter;
	}

	final private  long hashedPerft3(int deep,int depth, boolean bulk) {
		long[] counts = new long[deep];
		mlPerft(counts, deep,depth);
		for(int i=0;i<counts.length;i++) {
			System.out.println(i+" "+counts[i]);
		}
		return counts[0];
	}
	
	final private void mlPerft(long[] counts, int deep,int depth) {
		int moves = board.getMoves();
		for(int i=0;i<moves;i++) {
			board.doMove(i,true,deep==2);
			if(deep==2) {
				counts[0/*deep-2*/]+= board.getMoves();
			}else {
				long hash = board.getHash();
				long[] curCount = hashMap2[deep].get(hash);
				if(curCount ==null) {
					curCount = new long[deep-1];
					mlPerft(curCount,deep-1,depth);
					
					for(int j=deep;j<=deep;j++) {
						hashMap2[j].put(hash,curCount);			
					}
				}else {
					hits++;
				}
				for(int j=0;j<deep-1;j++) {
					counts[j]+=curCount[j];
				}
			}
			board.undoMove();
		}		
		counts[deep-1]=moves;
	}
	
	
	final public void play() {
		boardUI.show(board);
		int i=0;
		while(!board.isGameOver()){
			//Player playerAtTurn = players[turn];
			System.out.println("Play deep:"+depth);
			int move = play(depth);
			board.setMove(move,false,false);
			boardUI.show(board);
			if(i++==0)break;
		}
	}
	
	private int play(int deep) {
		int moves = board.getMoves();
		Double bestScore=null;
		int bestMove=-1;
		String bestStr = null;
		String[] bestStrs=new String [deep+1];
		
		for(int i=0;i<moves;i++) {
			String str = board.getMoveStr(i);
			board.doMove(i,true,false);
			//long hash = board.getHash();
			//Double score = hashScore[deep].get(hash);
			//if(score  ==null) {
			String[] allStrs=new String [deep];
				
			Double score = score(deep-1,allStrs)*-1;
				//hashScore[deep].put(hash,score);			

			if(bestScore==null||bestScore<score) {
				bestScore = score;
				bestMove=i;
				bestStr= str;
				bestStrs = allStrs;
				allStrs[deep-1]= bestStr;
			}
			System.out.println("   Score: "+score+" for "+ str);
			//}
			board.undoMove();

		}
		System.out.println("Score: "+bestScore+" for "+ bestStr);
		System.out.println(Arrays.toString(bestStrs));
		return bestMove;
	}
	
	
	/*
	 * int negaMax( int depth ) {
    if ( depth == 0 ) return evaluate();
    int max = -oo;
    for ( all moves)  {
        score = -negaMax( depth - 1 );
        if( score > max )
            max = score;
    }
    return max;
}
	 */
	final private double score3(int deep, String[] moveStr) {
		int moves = board.getMoves();
		if ( deep == 0||moves ==0) {
	    	return board.evaluate(-1);
	    }
		double max=-1_000_000;
		for(int i=0;i<moves;i++) {
			//String[] curMoveStr = new String[deep#];
			String str = board.getMoveStr(i);
	        board.doMove(i,true,false);
			double score = score(deep-1,moveStr)*-1;
			//System.out.println("     ".substring(deep,4)+" "+str+":"+score);
			board.undoMove();
	        if( score > max) {
	        	max = score;
	        	/*
	        	for(int j=0;j<deep-1;j++) {
					moveStr[j]=curMoveStr[j];
				}
				moveStr[deep-1]=str;
				*/ 
			}
		}
		return max;
	}
	final private double score2(int deep, String[] moveStr) {
		int moves = board.getMoves();
		if ( moves ==0) {
	    	return board.evaluate(-1);
	    }
		double max=-1_000_000;
		for(int i=0;i<moves;i++) {
			//String[] curMoveStr = new String[deep#];
			String str = board.getMoveStr(i);
			double score =0;
			if(deep>1) {
				board.doMove(i,true,deep==2);
				score = score(deep-1,moveStr)*-1;
				board.undoMove();
		    }else {
				score = board.evaluate(i)*-1;
			}
			if( score > max) {
	        	max = score;
	        }
		}
		return max;
	}
	final private double score(int deep, String[] moveStr) {
		int moves = board.getMoves();
		if (moves ==0) {
	    	return board.evaluate(-1);
	    }
		double max=-1_000_000;
		for(int i=0;i<moves;i++) {
			String[] curMoveStr = new String[deep-1];
			String str = board.getMoveStr(i);
			double score = 0;
			if(deep>1) { 
				board.doMove(i,true,false);
				score= score(deep-1,curMoveStr)*-1;
				board.undoMove();
			}else {
				//board.doMove(i,true,false);
				score= board.evaluate(i)*-1;
				//board.undoMove();
			}
	        if( score > max) {
	        	max = score;
	        	for(int j=0;j<deep-1;j++) {
					moveStr[j]=curMoveStr[j];
				}
				moveStr[deep-1]=str; 
			
	        }
		}
		return max;
	}
	
/*
	final private double score(int deep, String[] moveStr) {
		int moves = board.getMoves();
		if(moves==0) {
			return board.evaluate(-1)*-1;
		}

		Double bestScore=null;
		
		for(int i=0;i<moves;i++) {
			//long hash = board.getHash();
			//Double score = hashScore[deep].get(hash);
			//if(score  ==null) {
			String[] curMoveStr = new String[deep-1];
			String str = board.getMoveStr(i);
			Double score =null;
			if(deep>1) {
				board.doMove(i,true,deep==2);
				score = score(deep-1,curMoveStr);
				board.undoMove();
			}else {
				//doMove flips it once evaluate flips it again 
				score = board.evaluate(i);
			}
			//hashScore[deep].put(hash,score);			
			//}
			if(bestScore==null||bestScore<score) {
				bestScore = score;
				for(int j=0;j<deep-1;j++) {
					moveStr[j]=curMoveStr[j];
				}
				moveStr[deep-1]=str; 
			}
		}
		return bestScore*-1;
	}
	*/
	public void go(int depth) {
		int move = this.play(depth);
		System.out.println("OINK: Found this with my ApeMind:"+this.getBoard().getMoveStr(move));
		System.out.println("bestmove "+this.getBoard().getMoveStr(move));
		
	}
}

