import java.util.ArrayList;
import java.util.HashMap;

import perft.Game;
import perft.chess.ChessGameFactory;
import perft.chess.core.datastruct.ArrayStack;
import perft.chess.core.o.O;
import perft.chess.perftbb.BBPosition;
import perft.chess.perftmb.MBPosition;

public class Test {
	/*
	Perft(1) = 20 in 0.001312 sec
	Perft(2) = 400 in 0.001263 sec
	Perft(3) = 8902 in 0.004491 sec
	Perft(4) = 197281 in 0.023202 sec
	Perft(5) = 4865609 in 0.419626 sec
	Perft(6) = 119060324 in 10.080624 sec		   
	Perft(7) = 3195901860 in 277.148570 sec
	perft( 1)=           20 ( 0.000 sec)
	perft( 2)=          400 ( 0.000 sec)
	perft( 3)=         8902 ( 0.000 sec)
	perft( 4)=       197281 ( 0.000 sec)
	perft( 5)=      4865609 ( 0.016 sec)
	perft( 6)=    119060324 ( 0.414 sec)
	*/

	private static ChessGameFactory factory  = new ChessGameFactory();

	private static boolean useBulk = true;

	public static void main(String[] args) {
		ChessGameFactory.bitBoard=true;
		//debug (6,1,"r3k2r/8/8/8/8/8/8/R3K2R w KQkq -");
		
		
		//test (5,167815740,"r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w - -");
		//test (5,202355867,"r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -");
		
		test(6, 120921506,"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR b KQkq - 0 1");
		//debug(1, 16,"3qk3/8/8/8/8/8/8/3QK4 b - - 0 1");
		//test(6, 120921506,"r7/pp6/8/8/8/8/PP6/R7 b - - 0 1");
					
		//test(2, 1890,"q7/8/8/8/8/8/8/7Q w - - 0 1");
	
		//test(6, 120921506,"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR b KQkq - 0 1");
		
		//test(5, 1890,"k7/8/8/8/8/8/8/7K b - - 0 1");
		
		
		
		if(true)return;
		debug(1, 4732,"k7/pppppppp/8/8/8/8/PPPPPPPP/7K b - - 0 1");
		test(3, 8902,"c");
		test(4,4085603,"r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -");
		
		
		
		
		
		
		
		
		
		//test(4, 2, "k7/Pp6/1P6/8/1p6/6p1/2P3Pp/7K w - - 0 1");

		//		test(4, 7,"k1K5/8/8/8/8/8/8/8 w - - 0 1");
		//test(4, 197281,"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
		//test(4,4085603,"r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -");
		//test(6, 1134888, "3k4/3p4/8/K1P4r/8/8/8/8 b - - 0 1");
		//test(4, 10276, "8/8/4k3/8/2p5/8/B2P2K1/8 w - - 0 1");
		if(true)return;
		
		//test(6, 119060324,"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
	//	test(4,4085603,"r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -");
		
		//test(5,193690690,"r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -");
		
		test(2, 3, "4PKR1/4PPP1/8/8/8/r7/8/7k w - -");
		
		test(1, 2, "7K/7N/8/8/8/8/8/k6r w - -");
		
		test(1, 2, "7K/8/8/8/8/8/8/k6r w - -");
		
		test(2, 2, "4PKR1/4PPP1/8/8/8/8/8/7k w - - ");		

		test(4, 2, "k7/Pp6/1P6/8/1p6/6p1/2P3Pp/7K w - - 0 1");

		test(2, 4, "r3k3/Bp2p3/pP2P3/P7/8/6p1/6Pp/7K w q - 0 1");
		
		test(1, 5, "r3k2r/p1pp1pb1/bn2Qnp1/2qPN3/1p2P3/2N5/PPPBBPPP/R3K2R b KQkq - 3 2");
		
		test(2, 4, "k7/Pp6/1P6/8/1p6/6p1/2P3Pp/7K w - - 0 1");

		test(1, 9, "2r5/3pk3/8/2P5/8/2K5/8/8 w - - 5 4");
		
		test(1, 8, "8/8/8/2k5/2pP4/8/B7/4K3 b - d3 0 3");

		test(1, 7, "K1k5/8/8/8/8/8/p7/8 b - - 0 1");
		
		test(1, 8, "r6r/1b2k1bq/8/8/7B/8/8/R3K2R b KQ - 3 2");

		test(1, 7, "k7/8/8/q7/8/8/3Q4/4K3 w - - 0 1");

		test(2, 19, "8/P7/8/8/8/8/8/K1k5 b - - 0 1");
		
		test(2, 15, "8/P7/8/8/8/8/8/K1k5 w - - 0 1");
		
		test(2, 19, "8/P7/8/8/8/8/8/K1k5 b - - 0 1");
		
		test(2, 15, "8/P7/8/8/8/8/8/K1k5 w - - 0 1");
	
		test(1, 26, "r3k2r/8/8/8/8/8/8/R3K2R b KQkq - 0 1");

		test(2, 27, "8/P1k5/K7/8/8/8/8/8 w - - 0 1");
		
		test(3, 24,"4PKR1/4PPP1/8/8/8/8/7n/7k w - -");
		
		test(3, 33,"4PKR1/4PPP1/8/8/8/7n/8/7k w - -");
		
		test(1, 39, "rnb2k1r/pp1Pbppp/2p5/q7/2B5/8/PPPQNnPP/RNB1K2R w KQ - 3 9");
		
		test(1, 44, "2kr3r/p1ppqpb1/bn2Qnp1/3PN3/1p2P3/2N5/PPPBBPPP/R3K2R b KQ - 3 2");
		
		test(3, 99, "k7/8/8/8/1p1p4/pPpPp3/P1P1P3/R3K3 w Q - 0 1");
		
		test(2, 199, "r3k3/8/8/8/8/8/8/R3K3 w q - 0 1");

		test(3, 273, "8/P1k5/K7/8/8/8/8/8 w - - 0 1");
	
		test(3, 918, "8/3p4/8/k1P4Q/8/8/8/6K1 b - - 0 1");

		test(3, 1150, "8/2p5/8/k2P3Q/8/8/8/6K1 b - - 0 1");

		test(2, 1141, "r3k2r/1b4bq/8/8/8/8/7B/R3K2R w KQkq - 0 1");
		
		test(4, 1329, "8/P1k5/K7/8/8/8/8/8 w - - 0 1");

		test(2, 1797,"r4knr/p1ppqPb1/bn4p1/4N3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQ - 0 1");
			
		test(6, 2217, "K1k5/8/P7/8/8/8/8/8 w - - 0 1");

		test(3, 2958, "r3k3/8/8/8/8/8/8/R3K3 w - - 0 1");
		
		test(3, 3305, "r3k3/8/8/8/8/8/8/R3K3 w Qq - 0 1");

		test(3, 8902,"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");

		test(4, 10276, "8/8/4k3/8/2p5/8/B2P2K1/8 w - - 0 1");

		test(5, 18135, "8/P1k5/K7/8/8/8/8/8 w - - 0 1");

		test(4, 23527, "8/8/2k5/5q2/5n2/8/5K2/8 b - - 0 1");

		test(6, 92683, "8/P1k5/K7/8/8/8/8/8 w - - 0 1");
		
		test(5, 151107, "5R2/7k/8/8/K7/8/8/6R1 w - - 0 1");
		
		test(6, 217342, "4k3/1P6/8/8/8/8/K7/8 w - - 0 1");

		test(7, 567584, "8/k1P5/8/1K6/8/8/8/8 w - - 0 1");
		
		test(6, 661072, "5k2/8/8/8/8/8/8/4K2R w K - 0 1");
				
		test(6, 803711, "3k4/8/8/8/8/8/8/R3K3 w Q - 0 1");
		
		test(5, 1004658, "8/8/1P2K3/8/2n5/1q6/8/5k2 b - - 0 1");
		
		test(4, 1720476, "r3k2r/8/3Q4/8/8/5q2/8/R3K2R b KQkq - 0 1");
		
		test(6, 1440467, "8/8/1k6/2b5/2pP4/8/5K2/8 b - d3 0 1");
			
		test(6, 1134888, "3k4/3p4/8/K1P4r/8/8/8/8 b - - 0 1");
	
		test(6, 3821001, "2K2r2/4P3/8/8/8/8/8/3k4 w - - 0 1");
		
		//test(5, 4865609,"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
		
		test(6, 119060324,"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
		
		test(6,8031647685L,"r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -");

	}
	
	
	private static void debug(int depth, long nodes, String fen) {
		Test.debug(new String[0], null, depth, nodes, fen);
	}
	private static void debug(String [] debugMoves, HashMap<String,Long>refMap,int depth, long nodes, String fen) {
		//O.N=false;
		ChessGameFactory.bitBoard=false;
		Game reference = new Game(factory,fen);
		
		ChessGameFactory.bitBoard=true;
		Game game = new Game(factory,fen);
		
		System.out.println("After loading:\n"+game);
		int hash1 = game.getBoard().getHash();
		System.out.println("DIE SPIELE SIND ERÖFFNET!");
		MBPosition.registerCount =0;
		MBPosition.unRegisterCount =0;
		
		//O.N=true;
		long timeStamp = System.currentTimeMillis();
		long games = 0;
		String headLine="";
		if(useBulk) {
			headLine +="Bulk Counting";
		}else {
			headLine +="Real Counting";
		}
		games = game.debugPerft(reference.getBoard(),depth,null);
		
		double time = ((double)(long)((System.currentTimeMillis()-timeStamp)/10)/100);
		boolean correct = games == nodes;

		System.out.println(headLine);
		System.out.print("game\n"+game.toString());
		System.out.println("");
		System.out.println("  Delta:"+MBPosition.counter);
		int hash2= game.getBoard().getHash();
		System.out.println("  Hashes:"+hash1+"/"+hash2);
		System.out.println("");
		System.out.println("  Callbacks:");
		System.out.println("  Reg:   "+MBPosition.registerCount+":  ("+(int)(MBPosition.registerCount/game.getMoveCounter())+"/move)  ("+((int)((MBPosition.registerCount/(time*10))/10))+"/s)");
		System.out.println("  UnReg: " +MBPosition.unRegisterCount+":  ("+(int)(MBPosition.unRegisterCount/game.getMoveCounter())+"/move)  ("+((int)((MBPosition.unRegisterCount/(time*10))/10))+"/s)");
		System.out.println("");
		System.out.println("  Nodes (bulk: "+useBulk+"):");
		System.out.println("  Real Nodes: "+game.getMoveCounter()+":  "+(int)(game.getMoveCounter()/time)+"/s");
		System.out.println("  Bulk Nodes: "+games +":  "+(int)(games/time)+"/s");
		
		System.out.println("");
		System.out.println("  FEN:\""+fen+"\"");
		System.out.println("  Depth: "+ depth);
		System.out.println("  Time: "+ time +"s");
		if(correct) {
			System.out.print("[OK]: ");
		}else {
			System.out.print("[FAILED]: ");
		}
		System.out.println("("+games+" of "+nodes+")");
				
		
		if(!correct) {
			System.exit(-1);
		}
	}
	private static void test(int depth, long nodes, String fen) {
		//O.N=false;
		Game game = new Game(factory,fen);
		
		System.out.println("After loading:\n"+game);
		int hash1 = game.getBoard().getHash();
		System.out.println("DIE SPIELE SIND ERÖFFNET!");
		MBPosition.registerCount =0;
		MBPosition.unRegisterCount =0;
		
		//O.N=true;
		long timeStamp = System.currentTimeMillis();
		long games = 0;
		String headLine="";
		if(useBulk) {
			headLine +="Bulk Counting";
		}else {
			headLine +="Real Counting";
		}
		games = game.perft(depth,useBulk);
		//games = game.bulkPerft(depth);
		
		double time = ((double)(long)((System.currentTimeMillis()-timeStamp)/10)/100);
		boolean correct = games == nodes;

		System.out.println(headLine);
		System.out.print("game\n"+game.toString());
		System.out.println("");
		System.out.println("  Delta:"+MBPosition.counter);
		int hash2= game.getBoard().getHash();
		System.out.println("  Hashes:"+hash1+"/"+hash2);
		System.out.println("");
		System.out.println("  Callbacks:");
		System.out.println("  Reg:   "+MBPosition.registerCount+":  ("+(int)(MBPosition.registerCount/game.getMoveCounter())+"/move)  ("+((int)((MBPosition.registerCount/(time*10))/10))+"/s)");
		System.out.println("  UnReg: " +MBPosition.unRegisterCount+":  ("+(int)(MBPosition.unRegisterCount/game.getMoveCounter())+"/move)  ("+((int)((MBPosition.unRegisterCount/(time*10))/10))+"/s)");
		System.out.println("");
		System.out.println("  Nodes (bulk: "+useBulk+"):");
		System.out.println("  Real Nodes: "+game.getMoveCounter()+":  "+(int)(game.getMoveCounter()/time)+"/s");
		System.out.println("  Bulk Nodes: "+games +":  "+(int)(games/time)+"/s");
		
		System.out.println("");
		System.out.println("  FEN:\""+fen+"\"");
		System.out.println("  Depth: "+ depth);
		System.out.println("  Time: "+ time +"s");
		if(correct) {
			System.out.print("[OK]: ");
		}else {
			System.out.print("[FAILED]: ");
		}
		System.out.println("("+games+" of "+nodes+")");
				
			
		if(!correct) {
			System.exit(-1);
		}
	}
	

	

}