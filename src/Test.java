import java.util.HashMap;

import perft.Game;


import perft.chess.ChessGameFactory;
import perft.chess.core.Position;
/*
Perft(1) = 20 in 0.001312 sec
Perft(2) = 400 in 0.001263 sec
Perft(3) = 8902 in 0.004491 sec
Perft(4) = 197281 in 0.023202 sec
Perft(5) = 4865609 in 0.419626 sec
Perft(6) = 119060324 in 10.080624 sec		   
Perft(7) = 3195901860 in 277.148570 sec
*/
import perft.chess.core.o.O;

public class Test {
	private static ChessGameFactory factory  = new ChessGameFactory();
	private static boolean useBulk = false;

	public static void main(String[] args) {
		test(4, 2, "k7/Pp6/1P6/8/1p6/6p1/2P3Pp/7K w - - 0 1");

		//		test(4, 7,"k1K5/8/8/8/8/8/8/8 w - - 0 1");
//		test(3, 8902,"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
		//test(2,4085603,"r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -");
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
	
	
	private static void test(int depth, long nodes, String fen) {
		Test.test(new String[0], null, depth, nodes, fen);
	}
	private static void test(String [] debugMoves, HashMap<String,Long>refMap,int depth, long nodes, String fen) {
		//O.N=false;
		Game game = new Game(factory,fen);
		
		System.out.println("After loading:\n"+game);
		int hash1 = game.getBoard().getHash();
		System.out.println("DIE SPIELE SIND ERÖFFNET!");
		Position.registerCount =0;
		Position.unRegisterCount =0;
		
		//O.N=true;
		long timeStamp = System.currentTimeMillis();
		long games = 0;
		String headLine="";
		if(useBulk) {
			headLine +="Bulk Counting";
		}else {
			headLine +="Real Counting";
		}
		games = game.perft(depth,debugMoves,refMap,useBulk);

		double time = ((double)(long)((System.currentTimeMillis()-timeStamp)/10)/100);
		boolean correct = games == nodes;

		System.out.println(headLine);
		System.out.print("game\n"+game.toString());
		System.out.println("");
		System.out.println("  Delta:"+Position.counter);
		int hash2= game.getBoard().getHash();
		System.out.println("  Hashes:"+hash1+"/"+hash2);
		System.out.println("");
		System.out.println("  Callbacks:");
		System.out.println("  Reg:   "+Position.registerCount+":  ("+(int)(Position.registerCount/game.getMoveCounter())+"/move)  ("+((int)((Position.registerCount/(time*10))/10))+"/s)");
		System.out.println("  UnReg: " +Position.unRegisterCount+":  ("+(int)(Position.unRegisterCount/game.getMoveCounter())+"/move)  ("+((int)((Position.unRegisterCount/(time*10))/10))+"/s)");
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
	

	private static void speedTest() {
		long timeStamp =0;
		double time =0;
		int size = 10000;
		
		int [] array1  = new int[size];
		Integer [] array2  = new Integer[size];
		
		timeStamp = System.currentTimeMillis();
		for(int i=0;i<size;i++) {
			for(int j=0;j<size;j++) {
				array1[i]=i+j;
				array1[i]=array1[i]*array1[i];
			}
		}
		
		time = ((double)(long)((System.currentTimeMillis()-timeStamp)/10)/100);
		System.out.println("Run 1 in "  + time +"s");
		
		
		
		
		timeStamp = System.currentTimeMillis();
		for(int i=0;i<size;i++) {
			for(int j=0;j<size;j++) {
				array2[i]=i+j;
				array2[i]=array2[i]*array2[i];
			}
		}
		
		time = ((double)(long)((System.currentTimeMillis()-timeStamp)/10)/100);
		
		
		System.out.println("Run 2 in "  + time +"s");
	}		
	private static void compare() {
		String all1 = 
				"d5e6:203255185:"+ 
				"d5d6:151133066:"+ 
				"e5g4:144264874:"+ 
				"e5g6:165477768:"+ 
				"e5c4:145182844:"+ 
				"e5c6:169836097:"+ 
				"e5f7:176070755:"+ 
				"e5d3:140737072:"+ 
				"e5d7:193856446:"+ 
				"c3a4:191260040:"+ 
				"c3d1:165415976:"+ 
				"c3b1:165673862:"+ 
				"c3b5:166970874:"+ 
				"f3e3:189120807:"+ 
				"f3d3:164583144:"+ 
				"f3g3:198078522:"+ 
				"f3h3:210100865:"+ 
				"f3g4:189789456:"+ 
				"f3h5:197839051:"+ 
				"f3f4:181938761:"+ 
				"f3f5:226135507:"+ 
				"f3f6:146338070:"+ 
				"a2a3:197413067:"+ 
				"a2a4:183872225:"+ 
				"b2b3:153953689:"+ 
				"d2c1:158801466:"+ 
				"d2e3:184114087:"+ 
				"d2f4:165805784:"+ 
				"d2g5:177883051:"+ 
				"d2h6:161319567:"+ 
				"e2d3:167737155:"+ 
				"e2c4:170094798:"+ 
				"e2b5:158033152:"+ 
				"e2a6:130642863:"+ 
				"e2d1:131348645:"+ 
				"e2f1:174218453:"+ 
				"g2h3:158328615:"+ 
				"g2g3:141076301:"+ 
				"g2g4:135208177:"+ 
				"a1b1:160413321:"+ 
				"a1c1:159720218:"+ 
				"a1d1:149265033:"+ 
				"e1f1:139601450:"+ 
				"e1g1:172063416:"+ 
				"e1d1:148612404:"+ 
				"e1c1:148701308:"+ 
				"h1g1:166086672:"+ 
				"h1f1:154273720:";

		String all2="a2a3:197413067:"+
				"b2b3:153953689:"+
				"g2g3:141076301:"+
				"d5d6:151133066:"+
				"a2a4:183872225:"+
				"g2g4:135208177:"+
				"g2h3:158328615:"+
				"d5e6:203255191:"+
				"c3b1:165673862:"+
				"c3d1:165415976:"+
				"c3a4:191260040:"+
				"c3b5:166970874:"+
				"e5d3:140737072:"+
				"e5c4:145182844:"+
				"e5g4:144264874:"+
				"e5c6:169836097:"+
				"e5g6:165477768:"+
				"e5d7:193856446:"+
				"e5f7:176070755:"+
				"d2c1:158801466:"+
				"d2e3:184114087:"+
				"d2f4:165805784:"+
				"d2g5:177883051:"+
				"d2h6:161319567:"+
				"e2d1:131348645:"+
				"e2f1:174218453:"+
				"e2d3:167737155:"+
				"e2c4:170094798:"+
				"e2b5:158033152:"+
				"e2a6:130642863:"+
				"a1b1:160413321:"+
				"a1c1:159720218:"+
				"a1d1:149265033:"+
				"h1f1:154273720:"+
				"h1g1:166086672:"+
				"f3d3:164583144:"+
				"f3e3:189120807:"+
				"f3g3:198078522:"+
				"f3h3:210100865:"+
				"f3f4:181938761:"+
				"f3g4:189789456:"+
				"f3f5:226135507:"+
				"f3h5:197839051:"+
				"f3f6:146338070:"+
				"e1d1:148612404:"+
				"e1f1:139601450:"+
				"e1g1:172063416:"+
				"e1c1:148701308:";
		HashMap <String,Long>map1 = getMap(all1);
		HashMap <String,Long>map2 = getMap(all2);
		for(String move:map1.keySet()) {
			Long val1 = map1.get(move);
			Long val2 = map2.get(move);
			if(!val1.equals(val2)) {
				System.out.println("move:"+move+": "+val1+"!="+val2);
			}
		}
		
	}
	private static HashMap <String,Long> getMap(String all){

		String[] strs = all.split(":");
		HashMap <String,Long> map = new HashMap <String,Long> ();
		
		for(int i=0;i<strs.length/2;i++) {
			map.put(strs[i*2],(Long)(long)Integer.parseInt(strs[i*2+1].trim()));
			
		}
		return map;
	}	
}