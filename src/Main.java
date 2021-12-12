import perft.Game;
import perft.chess.ChessGameFactory;
import perft.chess.core.o.O;
import perft.chess.perftmb.Field;


public class Main {
}


/*
I don't know on what kind of hardware you ran perft(), but the results look slow indeed. 
For instance my magic bitboard implementation runs perft 6 in 0.77 seconds and perft 7 in 19,8 seconds,
this is with bulk-counting. Without bulk-counting it is: 1.76 seconds and 46.6 seconds, 
tested on a 3800 MHz. i9-10908XE.

Perft(1) = 20 in 0.001312 sec
Perft(2) = 400 in 0.001263 sec
Perft(3) = 8902 in 0.004491 sec
Perft(4) = 197281 in 0.023202 sec
Perft(5) = 4865609 in 0.419626 sec
Perft(6) = 119060324 in 10.080624 sec
		   
Perft(7) = 3195901860 in 277.148570 sec
*/


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