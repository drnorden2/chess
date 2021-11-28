import perft.Game;


import perft.chess.ChessGameFactory;
import perft.chess.core.Field;
import perft.chess.core.o.O;


/*
erft(1) = 20 in 0.001312 sec
Perft(2) = 400 in 0.001263 sec
Perft(3) = 8902 in 0.004491 sec
Perft(4) = 197281 in 0.023202 sec
Perft(5) = 4865609 in 0.419626 sec
Perft(6) = 119060324 in 10.080624 sec
		   
Perft(7) = 3195901860 in 277.148570 sec
*/
public class Main {
/*
	private static ChessGameFactory factory  = new ChessGameFactory();
	private static boolean useBulk = true;

	public static void main(String[] args) {
		//test(4,197281,"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
		test(5,4865609,"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
		
		if(true)return;

		//test(3, 3305, "r3k3/8/8/8/8/8/8/R3K3 w Qq - 0 1");
		
		
		test(1, 26, "r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1");
		
		test(1, 5, "r3k2r/p1pp1pb1/bn2Qnp1/2qPN3/1p2P3/2N5/PPPBBPPP/R3K2R b KQkq - 3 2");
		
		test(2, 4, "k7/Pp6/1P6/8/1p6/6p1/2P3Pp/7K w - - 0 1");

		test(4, 2, "k7/Pp6/1P6/8/1p6/6p1/2P3Pp/7K w - - 0 1");

		test(4, 10276, "8/8/4k3/8/2p5/8/B2P2K1/8 w - - 0 1");

		test(3,24,"4PKR1/4PPP1/8/8/8/8/7n/7k w - -");
		
		test(3,33,"4PKR1/4PPP1/8/8/8/7n/8/7k w - -");
		
		test(6, 2217, "K1k5/8/P7/8/8/8/8/8 w - - 0 1");

		test(1, 26, "r3k2r/8/8/8/8/8/8/R3K2R b KQkq - 0 1");
		
		test(2, 199, "r3k3/8/8/8/8/8/8/R3K3 w q - 0 1");
		
		test(3, 2958, "r3k3/8/8/8/8/8/8/R3K3 w - - 0 1");
		
		test(3, 3305, "r3k3/8/8/8/8/8/8/R3K3 w Qq - 0 1");
		
		test(2, 19, "8/P7/8/8/8/8/8/K1k5 b - - 0 1");
		
		test(2, 15, "8/P7/8/8/8/8/8/K1k5 w - - 0 1");
		
		test(2, 19, "8/P7/8/8/8/8/8/K1k5 b - - 0 1");
		
		test(2, 15, "8/P7/8/8/8/8/8/K1k5 w - - 0 1");
		
		test(3, 99 , "k7/8/8/8/1p1p4/pPpPp3/P1P1P3/R3K3 w Q - 0 1");
		
		test(2,2,"4PKR1/4PPP1/8/8/8/p7/8/7k w - -");
		
		test(2,3,"4PKR1/4PPP1/8/8/8/r7/8/7k w - -");
		
		test(1,2,"7K/7N/8/8/8/8/8/k6r w - -");
		
		test(1,2,"7K/8/8/8/8/8/8/k6r w - -");
		
		test(1,1,"7K/8/8/8/8/8/8/k5r w - -");
		
		test(2,2,"4PKR1/4PPP1/8/8/8/8/8/7k w - - ");
		
		test(3,8902,"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
		
		test(1, 9, "2r5/3pk3/8/2P5/8/2K5/8/8 w - - 5 4");
		
		test(1, 8, "8/8/8/2k5/2pP4/8/B7/4K3 b - d3 0 3");

		test(1, 7, "K1k5/8/8/8/8/8/p7/8 b - - 0 1");
		
		test(1, 8, "r6r/1b2k1bq/8/8/7B/8/8/R3K2R b KQ - 3 2");

		test(1, 7, "k7/8/8/q7/8/8/3Q4/4K3 w - - 0 1");

		test(1, 39, "rnb2k1r/pp1Pbppp/2p5/q7/2B5/8/PPPQNnPP/RNB1K2R w KQ - 3 9");
		
		test(1, 44, "2kr3r/p1ppqpb1/bn2Qnp1/3PN3/1p2P3/2N5/PPPBBPPP/R3K2R b KQ - 3 2");
	
		test(4, 23527, "8/8/2k5/5q2/5n2/8/5K2/8 b - - 0 1");
	
		test(3, 62379, "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8");
		
		test(3, 89890, "r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10");
		
		test(2, 27, "8/P1k5/K7/8/8/8/8/8 w - - 0 1");
		
		test(3, 273, "8/P1k5/K7/8/8/8/8/8 w - - 0 1");
		
		test(5, 18135, "8/P1k5/K7/8/8/8/8/8 w - - 0 1");
		
		test(4, 1329, "8/P1k5/K7/8/8/8/8/8 w - - 0 1");
		
		test(6, 92683, "8/P1k5/K7/8/8/8/8/8 w - - 0 1");

		test(6, 661072, "5k2/8/8/8/8/8/8/4K2R w K - 0 1");

		
		test(1,20,"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");

 		test(2,400,"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
		
		test(3,8902,"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
		
		test(4,197281,"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
		
		test(5,4865609,"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
		
		test(6,119060324,"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");

		
		test(6, 803711, "3k4/8/8/8/8/8/8/R3K3 w Q - 0 1");

		test(4, 1274206, "r3k2r/1b4bq/8/8/8/8/7B/R3K2R w KQkq - 0 1");

		test(6, 3821001, "2K2r2/4P3/8/8/8/8/8/3k4 w - - 0 1");

		test(6, 217342, "4k3/1P6/8/8/8/8/K7/8 w - - 0 1");
		
		test(7, 567584, "8/k1P5/8/1K6/8/8/8/8 w - - 0 1");

		test(5, 1004658, "8/8/1P2K3/8/2n5/1q6/8/5k2 b - - 0 1");
		
		test(4, 1720476, "r3k2r/8/3Q4/8/8/5q2/8/R3K2R b KQkq - 0 1");
		
		test(6, 1440467, "8/8/1k6/2b5/2pP4/8/5K2/8 b - d3 0 1");
			
		test(6, 1134888, "3k4/3p4/8/K1P4r/8/8/8/8 b - - 0 1");
	}
	
	
	
	private static void test(int depth, int nodes, String fen) {
		//O.N=false;
		Game game = new Game(factory,fen);
		
		
		System.out.println("After loading:\n"+game);
		
		System.out.println("DIE SPIELE SIND ERÖFFNET!");
		Field.registerCount =0;
		Field.unRegisterCount =0;
		
		//O.N=true;
		long timeStamp = System.currentTimeMillis();
		long games = 0;
		String headLine="";
		if(useBulk) {
			headLine +="Bulk Counting";
		}else {
			headLine +="Real Counting";
		}
		games = game.perft(depth,new String [0],null,useBulk);

		double time = ((double)(long)((System.currentTimeMillis()-timeStamp)/10)/100);
		boolean correct = games == nodes;

		
		System.out.println(headLine);
		System.out.print("game\n"+game.toString());
		if(correct) {
			System.out.print("[OK] ");
		}else {
			System.out.print("[FAILED] ");
		}
		System.out.println("FEN:\""+fen+"\"    w. depth:"+ depth+  " in "  + time +"s ("+games+" of "+nodes+")\n");
		System.out.println("MoveCounter:"+game.getMoveCounter());
		System.out.println("Register/move:"+(int)(Field.registerCount/game.getMoveCounter())+"  /"+((int)((Field.registerCount/(time*10))/10))+"/s");
		System.out.println("UnRegist/move:"+(int)(Field.unRegisterCount/game.getMoveCounter())+"  /"+((int)((Field.unRegisterCount/(time*10))/10))+"/s");
		
		
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
*/

}


/*
 * rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1
 * Depth 	Nodes 	Captures 	E.p. 	Castles 	Promotions 	Checks 	Discovery Checks 	Double Checks 	Checkmates
 * 0 	1 	0 	0 	0 	0 	0 	0 	0 	0
 * 1 	20 	0 	0 	0 	0 	0 	0 	0 	0
 * 2 	400 	0 	0 	0 	0 	0 	0 	0 	0
 * 3 	8,902 	34 	0 	0 	0 	12 	0 	0 	0
 * 4 	197,281 	1576 	0 	0 	0 	469 	0 	0 	8
 * 5 	4,865,609 	82,719 	258 	0 	0 	27,351 	6 	0 	347
 * 6 	119,060,324 	2,812,008 	5248 	0 	0 	809,099 	329 	46 	10,828
 * 7 	3,195,901,860 	108,329,926 	319,617 	883,453 	0 	33,103,848 	18,026 	1628 	435,767
 * 8 	84,998,978,956 	3,523,740,106 	7,187,977 	23,605,205 	0 	968,981,593 	847,039 	147,215 	9,852,036
 * 9 	2,439,530,234,167 	125,208,536,153 	319,496,827 	1,784,356,000 	17,334,376 	36,095,901,903 	37,101,713 	5,547,231 	400,191,963
 * 10 	69,352,859,712,417
 * 11 	2,097,651,003,696,806
 * 12 	62,854,969,236,701,747 [2]
 * 13 	1,981,066,775,000,396,239
 * 14 	61,885,021,521,585,529,237 [3]
 * 15 	2,015,099,950,053,364,471,960 
*/

/* Pseudo moves
Perft(1): 20
Perft(2): 400
Perft(3): 8902
Perft(4): 197742
Perft(5): 4897256
Perft(6): 120921506
*/