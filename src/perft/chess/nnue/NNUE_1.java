/***
 * Java ported version of NNUE Probe
 * 
 * For NNUE Probe see:
 * Source NNUE Probe by David Shawul
 * https://github.com/dshawul/nnue-probe
 * 
 * inspired by Maksim Korzh's takes on the use of this library
 * https://github.com/maksimKorzh/
 * https://www.youtube.com/channel/UCB9-prLkPwgvlKKqDgXhsMQ
 * 
 */
	

package perft.chess.nnue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class NNUE_1 {
	public static final int[] NOT =new int[] {1,0};
	public static final int white = 0;
	public static final int black = 1;
	public static final int[] colors =new int[] {white,black};
	public static final int blank=0;
	public static final int wking=1;
	public static final int wqueen=2;
	public static final int wrook=3;
	public static final int wbishop=4;
	public static final int wknight=5;
	public static final int wpawn=6;
	public static final int bking=7;
	public static final int bqueen=8;
	public static final int brook=9;
	public static final int bbishop=10;
	public static final int bknight=11;
	public static final int bpawn=12;

	public static final int[]  pieces  = new int[] {blank/*=0*/,wking,wqueen,wrook,wbishop,wknight,wpawn,
	            bking,bqueen,brook,bbishop,bknight,bpawn};


	/*
	#include <assert.h>
	#include <stdio.h>
	#include <stdint.h>
	#include <string.h>
	#include <stdlib.h>

	//--------------------
	//-------------------


	//-------------------
	#include "misc.h"
	#define DLL_EXPORT
	#include "nnue.h"
	#undef DLL_EXPORT

	#define KING(c)    ( (c) ? bking : wking )
	#define IS_KING(p) ( ((p) == wking) || ((p) == bking) )
	//-------------------

	// Old gcc on Windows is unable to provide a 32-byte aligned stack.
	// We need to hack around this when using AVX2 and AVX512.
	#if     defined(__GNUC__ ) && (__GNUC__ < 9) && defined(_WIN32) \
	    && !defined(__clang__) && !defined(__INTEL_COMPILER) \
	    &&  defined(USE_AVX2)
	#define ALIGNMENT_HACK
	#endif


	enum {
	  PS_W_PAWN   =  1,
	  PS_B_PAWN   =  1 * 64 + 1,
	  PS_W_KNIGHT =  2 * 64 + 1,
	  PS_B_KNIGHT =  3 * 64 + 1,
	  PS_W_BISHOP =  4 * 64 + 1,
	  PS_B_BISHOP =  5 * 64 + 1,
	  PS_W_ROOK   =  6 * 64 + 1,
	  PS_B_ROOK   =  7 * 64 + 1,
	  PS_W_QUEEN  =  8 * 64 + 1,
	  PS_B_QUEEN  =  9 * 64 + 1,
	  PS_END      = 10 * 64 + 1
	};

	uint32_t PieceToIndex[2][14] = {
	  { 0, 0, PS_W_QUEEN, PS_W_ROOK, PS_W_BISHOP, PS_W_KNIGHT, PS_W_PAWN,
	       0, PS_B_QUEEN, PS_B_ROOK, PS_B_BISHOP, PS_B_KNIGHT, PS_B_PAWN, 0},
	  { 0, 0, PS_B_QUEEN, PS_B_ROOK, PS_B_BISHOP, PS_B_KNIGHT, PS_B_PAWN,
	       0, PS_W_QUEEN, PS_W_ROOK, PS_W_BISHOP, PS_W_KNIGHT, PS_W_PAWN, 0}
	};

	// Version of the evaluation file
	static const uint32_t NnueVersion = 0x7AF32F16u;

	// Constants used in evaluation value calculation
	enum {
	  FV_SCALE = 16,
	  SHIFT = 6
	};

	enum {
	  kHalfDimensions = 256,
	  FtInDims = 64 * PS_END, // 64 * 641
	  FtOutDims = kHalfDimensions * 2
	};
	*/
	public static final int[] KING = new int[] {bking , wking};
	public static final boolean[] IS_KING =  new boolean[]{
			false,/*blank*/
			true,false,false,false,false,false,
			true,false,false,false,false,false};

	//enum {
	private static final int PS_W_PAWN   =  1;
	private static final int PS_B_PAWN   =  1 * 64 + 1;
	private static final int PS_W_KNIGHT =  2 * 64 + 1;
	private static final int PS_B_KNIGHT =  3 * 64 + 1;
	private static final int PS_W_BISHOP =  4 * 64 + 1;
	private static final int PS_B_BISHOP =  5 * 64 + 1;
	private static final int PS_W_ROOK   =  6 * 64 + 1;
	private static final int PS_B_ROOK   =  7 * 64 + 1;
	private static final int PS_W_QUEEN  =  8 * 64 + 1;
	private static final int PS_B_QUEEN  =  9 * 64 + 1;
	private static final int PS_END = 10 * 64 + 1;
	//}

	public static final int[][] PieceToIndex = new int [][]{
	  { 0, 0, PS_W_QUEEN, PS_W_ROOK, PS_W_BISHOP, PS_W_KNIGHT, PS_W_PAWN,
	       0, PS_B_QUEEN, PS_B_ROOK, PS_B_BISHOP, PS_B_KNIGHT, PS_B_PAWN, 0},
	  { 0, 0, PS_B_QUEEN, PS_B_ROOK, PS_B_BISHOP, PS_B_KNIGHT, PS_B_PAWN,
	       0, PS_W_QUEEN, PS_W_ROOK, PS_W_BISHOP, PS_W_KNIGHT, PS_W_PAWN, 0}
	};

	// Version of the evaluation file
	private static final int NnueVersion = 0x7AF32F16;

	// Constants used in evaluation value calculation

	//enum {
	private static final int FV_SCALE = 16;
	private static final int SHIFT = 6;
	//}
	
	//enum {
	private static final int  kHalfDimensions = 256;
	private static final int  FtInDims = 64 * PS_END; // 64 * 641
	private static final int  FtOutDims = kHalfDimensions * 2;
	//}
	
	//enum {
	private static final int TransformerStart = 3 * 4 + 177;
	private static final int NetworkStart = TransformerStart + 4 + 2 * 256 + 2 * 256 * 64 * 641;
	//};


	// Input feature converter
//		static int16_t ft_biases alignas(64) [kHalfDimensions];
//		static int16_t ft_weights alignas(64) [kHalfDimensions * FtInDims];
	private final static int[] ft_biases = new int [kHalfDimensions];
	private final static int[] ft_weights = new int [kHalfDimensions * FtInDims];

	
	
	
	
	/*
	* nnue data structure from h file
	*/

	private final class DirtyPiece {
	  int dirtyNum;
	  int[] pc = new int[3];
	  int[] from= new int[3];
	  int[] to= new int[3];
	} 

	private final class Accumulator {
	  //alignas(64) int16_t accumulation[2][256];
		int[][] accumulation = new int [2][256];
		int computedAccumulation;
	} 

	private final class NNUEdata {
	  Accumulator accumulator = new Accumulator();
	  DirtyPiece dirtyPiece= new DirtyPiece();
	} 

	/**
	* position data structure passed to core subroutines
	*  See @nnue_evaluate for a description of parameters
	*/
	private final class Position {
	  int player;
	  int[] pieces; // int* pieces;
	  int[] squares;//int* squares;
	  NNUEdata[] nnue = new NNUEdata[3]; //NNUEdata* nnue[3];
	} 

	
	
	
	
	
	
	
	
	
/*	
	// USE_MMX generates _mm_empty() instructions, so undefine if not needed

	static_assert(kHalfDimensions % 256 == 0, "kHalfDimensions should be a multiple of 256");

	#define VECTOR

	#undef VECTOR
	#define SIMD_WIDTH 16 // dummy
	*/
//	typedef uint8_t uint8_t /*mask_t*/; // dummy
//
//
//	typedef uint64_t mask2_t;
//
//	typedef int8_t uint8_t /*clipped_t*/;
//	typedef int8_t uint8_t /*weight_t*/;

	private final class IndexList{
	  int/*size_t*/ size;
	  int[] /*unsigned*/ values= new int[30];
	} 


	
	
	
	private final int orient(int c, int s)
	{
	  return s ^ (c == white ? 0x00 : 0x3f);
	}

	private final int /*unsigned*/ make_index(int c, int s, int pc, int ksq)
	{
	  return orient(c, s) + PieceToIndex[c][pc] + PS_END * ksq;
	}

	private final void half_kp_append_active_indices(/*const*/ Position /* * */pos, /*const*/ int c, IndexList /* * */ active){
	  int ksq = pos.squares[c];
	  ksq = orient(c, ksq);
	  for (int i = 2; pos.pieces[i]!=0/*c++ boolean*/; i++) {
	    int sq = pos.squares[i];
	    int pc = pos.pieces[i];
	    active.values[active.size++] = make_index(c, sq, pc, ksq);
	    System.out.println("active.values["+(active.size-1)+"] = "+active.values[active.size-1] );
	  }
	}

	//static void half_kp_append_changed_indices(const Position *pos, const int c, const DirtyPiece *dp, IndexList *removed, IndexList *added)
	private final void half_kp_append_changed_indices(Position pos, int c, DirtyPiece dp, IndexList removed, IndexList added){
	  int ksq = pos.squares[c];
	  ksq = orient(c, ksq);
	  for (int i = 0; i < dp.dirtyNum; i++) {
	    int pc = dp.pc[i];
	    if (IS_KING[pc]) continue;
	    if (dp.from[i] != 64)
	      removed.values[removed.size++] = make_index(c, dp.from[i], pc, ksq);
	    if (dp.to[i] != 64)
	      added.values[added.size++] = make_index(c, dp.to[i], pc, ksq);
	  }
	}


	//static void append_active_indices(const Position *pos, IndexList active[2]){
	private final void append_active_indices(Position pos, IndexList[] active){
	  for (int /*unsigned*/ c = 0; c < 2; c++)
	    half_kp_append_active_indices(pos, c, /*&*/active[c]);
	}

	// static void append_changed_indices(const Position *pos, IndexList
	// removed[2],IndexList added[2], bool reset[2]){
	private final void append_changed_indices(Position pos, IndexList[] removed, IndexList[] added, boolean[] reset) {
		// const DirtyPiece *dp = &(pos.nnue[0].dirtyPiece);
		DirtyPiece dp = pos.nnue[0].dirtyPiece;
		// assert(dp.dirtyNum != 0);

		if(pos.nnue[1].accumulator.computedAccumulation != 0/* Cpp Boolean true */) {
			for (int /* unsigned */ c = 0; c < 2; c++) {
				reset[c] = dp.pc[0] == /* (int) */KING[c];
				if (reset[c])
					half_kp_append_active_indices(pos, c, /* & */added[c]);
				else
					half_kp_append_changed_indices(pos, c, dp, /* & */removed[c], /* & */added[c]);
			}
		} else {
			// const DirtyPiece *dp2 = &(pos.nnue[1].dirtyPiece);
			DirtyPiece dp2 = pos.nnue[1].dirtyPiece;

			for (int /* unsigned */ c = 0; c < 2; c++) {
				reset[c] = dp.pc[0] == KING[c] || dp2.pc[0] == KING[c];
				if (reset[c])
					half_kp_append_active_indices(pos, c, /* & */added[c]);
				else {
					half_kp_append_changed_indices(pos, c, dp, /* & */removed[c], /* & */added[c]);
					half_kp_append_changed_indices(pos, c, dp2, /* & */removed[c], /* & */added[c]);
				}
			}
		}
	}

	// InputLayer = InputSlice<256 * 2>
	// out: 512 x uint8_t /*clipped_t*/

	// Hidden1Layer = ClippedReLu<AffineTransform<InputLayer, 32>>
	// 512 x uint8_t /*clipped_t*/ . 32 x int32_t . 32 x uint8_t /*clipped_t*/

	// Hidden2Layer = ClippedReLu<AffineTransform<hidden1, 32>>
	// 32 x uint8_t /*clipped_t*/ . 32 x int32_t . 32 x uint8_t /*clipped_t*/

	// OutputLayer = AffineTransform<HiddenLayer2, 1>
	// 32 x uint8_t /*clipped_t*/ . 1 x int32_t

//	static uint8_t /*weight_t*/ hidden1_weights alignas(64) [32 * 512];
//	static uint8_t /*weight_t*/ hidden2_weights alignas(64) [32 * 32];
//	static uint8_t /*weight_t*/ output_weights alignas(64) [1 * 32];
	static short[] hidden1_weights = new short[32 * 512];
	static short[] hidden2_weights = new short[32 * 32];
	static short[] output_weights = new short[1 * 32];

//	static int32_t hidden1_biases alignas(64) [32];
//	static int32_t hidden2_biases alignas(64) [32];
//	static int32_t output_biases[1];
	static int[] hidden1_biases = new int[32];
	static int[] hidden2_biases = new int[32];
	static int[] output_biases = new int[1];

	//int32_t affine_propagate(uint8_t /*clipped_t*/ *input, int32_t *biases, uint8_t /*weight_t*/ *weights)
	int affine_propagate(short[] input, int[] biases, short[] weights)
	{
	  int /*int32_t*/ sum = biases[0];
	  for (int /*unsigned*/ j = 0; j < 32; j++)
	    sum += weights[j] * input[j];
	  return sum;

	}

	//static_assert(FtOutDims % 64 == 0, "FtOutDims not a multiple of 64");


//	void affine_txfm(uint8_t /*clipped_t*/ *input, void *output, unsigned inDims,
//	    unsigned outDims, int32_t *biases, const uint8_t /*weight_t*/ *weights,
//	    uint8_t /*mask_t*/ *inMask, uint8_t /*mask_t*/ *outMask, const bool pack8_and_calc_mask)
//	{
//	  (void)inMask; (void)outMask; (void)pack8_and_calc_mask;
//
//	  int32_t tmp[outDims];
//
//	  for (unsigned i = 0; i < outDims; i++)
//	    tmp[i] = biases[i];
//
//	  for (unsigned idx = 0; idx < inDims; idx++)
//	    if (input[idx])
//	      for (unsigned i = 0; i < outDims; i++)
//	        tmp[i] += (int8_t)input[idx] * weights[outDims * idx + i];
//
//	  uint8_t /*clipped_t*/ *outVec = (uint8_t /*clipped_t*/ *)output;
//	  for (unsigned i = 0; i < outDims; i++)
//	    outVec[i] = clamp(tmp[i] >> SHIFT, 0, 127);
//	}




	private final void affine_txfm(short[] input, short[] output, int inDims, int outDims, int[] biases, short[] weights,short[] inMask, short[] outMask, boolean pack8_and_calc_mask){
	  //(void)inMask; (void)outMask; (void)pack8_and_calc_mask;

	  int[] tmp = new int[outDims];

	  for (int /*unsigned*/ i = 0; i < outDims; i++) {
	    tmp[i] = biases[i];
	 	System.out.println("v-bias["+i+"]="+biases[i]);
	  }
	  for (int /*unsigned*/ idx = 0; idx < inDims; idx++) {
		  
	    if (input[idx]!=0) {
	    	//System.out.println("input["+idx+"]="+input[idx]);
	      for (int /*unsigned*/ i = 0; i < outDims; i++) {
	    	//System.out.println("n-bias["+i+"]="+biases[i]);
		  	//System.out.println("v-tmp["+i+"]="+tmp[i]);
	  		    tmp[i] += (short)(input[idx] * weights[outDims * idx + i]);
	  		//System.out.println("n-tmp["+i+"]="+tmp[i]);	
	      }
	    }
	  }
 	  short[] outVec = output;
	  for (int /*unsigned*/ i = 0; i < outDims; i++) {
		//System.out.println("tmp["+i+"]="+tmp[i]);
		outVec[i] = clamp(tmp[i] >> SHIFT, 0, 127);
	   // System.out.println("outVec["+i+"]="+outVec[i]);
	  }
	}











	// Calculate cumulative value without using difference calculation
	//void refresh_accumulator(Position *pos){
	void refresh_accumulator(Position pos){
	  Accumulator /* * */ accumulator = /*&*/(pos.nnue[0].accumulator);

	  IndexList[] activeIndices = new IndexList[] {new IndexList(),new IndexList()}; 
	  activeIndices[0].size = activeIndices[1].size = 0;
	  append_active_indices(pos, activeIndices);

	  for (int /*unsigned*/   c = 0; c < 2; c++) {
		// System.arraycopy(src, srcPos, dest, destPos, length);
		// void* memcpy( void* dest, const void* src, std::size_t count );
		// memcpy(accumulator.accumulation[c], ft_biases,kHalfDimensions * sizeof(int16_t));
		  for(int i=0;i<255;i++) {
				System.out.println("accumulator.accumulation[0]["+i+"]="+accumulator.accumulation[0][i]);
			 }
		  System.arraycopy(ft_biases, 0, accumulator.accumulation[c], 0, kHalfDimensions);
		for(int i=0;i<255;i++) {
			System.out.println("accumulator.accumulation[0]["+i+"]="+accumulator.accumulation[c][i] +" "+ft_biases[i]);
		 }
		for (int/*size_t*/ k = 0; k < activeIndices[c].size; k++) {
	      int /*unsigned*/ index = activeIndices[c].values[k];
	      int /*unsigned*/offset = kHalfDimensions * index;

	      for (int /*unsigned*/j = 0; j < kHalfDimensions; j++)
	        accumulator.accumulation[c][j] += ft_weights[offset + j];
	    }
	  }
	  /*
	  for(int i=0;i<255;i++) {
		  System.out.println("accumulator.accumulation[0]["+i+"]="+accumulator.accumulation[0][i]);
	  }
	  for(int i=0;i<255;i++) {
		  System.out.println("accumulator.accumulation[1]["+i+"]="+accumulator.accumulation[1][i]);
	  }*/
	  accumulator.computedAccumulation = 1;
	}

	// Calculate cumulative value using difference calculation if possible
	//bool update_accumulator(Position *pos)
	boolean update_accumulator(Position pos) {
		Accumulator /* * */ accumulator = /* & */(pos.nnue[0].accumulator);
		if (accumulator.computedAccumulation != 0/* bool eval */)
			return true;

		Accumulator /* **/ prevAcc;

		//if (   (!pos.nnue[1] || !(prevAcc = /*&*/pos.nnue[1].accumulator).computedAccumulation)
		//&& (!pos.nnue[2] || !(prevAcc = /*&*/pos.nnue[2].accumulator).computedAccumulation) )
		//return false;

		if (( (pos.nnue[1]==null)  || ((prevAcc = /*&*/pos.nnue[1].accumulator).computedAccumulation==0))
	      && ( (pos.nnue[2]==null)  || ((prevAcc = /*&*/pos.nnue[2].accumulator).computedAccumulation==0)))
	    return false;

		
		IndexList[] removed_indices = new IndexList[] { new IndexList(), new IndexList() };
		IndexList[] added_indices = new IndexList[] { new IndexList(), new IndexList() };
		removed_indices[0].size = removed_indices[1].size = 0;
		added_indices[0].size = added_indices[1].size = 0;
		boolean[] reset = new boolean[2]; /* TODO */// true or false?
		append_changed_indices(pos, removed_indices, added_indices, reset);

		for (int /* unsigned */ c = 0; c < 2; c++) {
			if (reset[c]) {
				// System.arraycopy(src, srcPos, dest, destPos, length);
				// void* memcpy( void* dest, const void* src, std::size_t count );
				// memcpy(accumulator.accumulation[c], ft_biases, kHalfDimensions *
				// sizeof(int16_t));
				System.arraycopy(ft_biases, 0, accumulator.accumulation[c], 0, kHalfDimensions);

			} else {
				// memcpy(accumulator.accumulation[c], prevAcc.accumulation[c], kHalfDimensions
				// * sizeof(int16_t));
				System.arraycopy(prevAcc.accumulation[c], 0, accumulator.accumulation[c], 0, kHalfDimensions);
				// Difference calculation for the deactivated features
				for (int /* unsigned */ k = 0; k < removed_indices[c].size; k++) {
					int /* unsigned */ index = removed_indices[c].values[k];
					int /* const unsigned */ offset = kHalfDimensions * index;

					for (int /* unsigned */ j = 0; j < kHalfDimensions; j++)
						accumulator.accumulation[c][j] -= ft_weights[offset + j];
				}
			}

			// Difference calculation for the activated features
			for (int /* unsigned */ k = 0; k < added_indices[c].size; k++) {
				int /* unsigned */ index = added_indices[c].values[k];
				int /* const unsigned */ offset = kHalfDimensions * index;

				for (int /* unsigned */ j = 0; j < kHalfDimensions; j++)
					accumulator.accumulation[c][j] += ft_weights[offset + j];
			}
		}

		accumulator.computedAccumulation = 1;
		return true;
	}

	// Convert input features
	//void transform(Position *pos, uint8_t /*clipped_t*/ *output, uint8_t /*mask_t*/ *outMask)
	void transform(Position pos, short[]  output, short[] outMask){
	  if (!update_accumulator(pos))
	    refresh_accumulator(pos);

	  //int16_t (*accumulation)[2][256] = &pos.nnue[0].accumulator.accumulation;
	  int[][] accumulation = pos.nnue[0].accumulator.accumulation;
	  
	  /*TODO*///(void)outMask; // avoid compiler warning

	  /*const*/ int[] perspectives = new int[]{ pos.player, NOT[pos.player] };
	  for (int /*unsigned*/ p = 0; p < 2; p++) {
	    int /*const unsigned*/ offset = kHalfDimensions * p;

	    for (int /*unsigned*/ i = 0; i < kHalfDimensions; i++) {
	      //int16_t sum = (*accumulation)[perspectives[p]][i];
	    	int sum =accumulation[perspectives[p]][i];
		    output[offset + i] = clamp(sum, 0, 127);
		    System.out.println("output["+(offset + i)+"]="+output[offset + i]);
	    }
	  }
	}

	class NetData {
//	  alignas(64) uint8_t /*clipped_t*/ input[FtOutDims];
//	  uint8_t /*clipped_t*/ hidden1_out[32];
//	  int8_t hidden2_out[32];
	  short[] input = new short[FtOutDims];
	  short[] hidden1_out = new short[32];
	  short[] hidden2_out = new short[32];

	}

	// Evaluation function
//	int nnue_evaluate_pos(Position *pos)
	int nnue_evaluate_pos(Position pos) {
//	  int32_t out_value;
//	  alignas(8) uint8_t /*mask_t*/ input_mask[FtOutDims / (8 * sizeof(uint8_t /*mask_t*/))];
//	  alignas(8) uint8_t /*mask_t*/ hidden1_mask[8 / sizeof(uint8_t /*mask_t*/)] = { 0 };
//	  struct NetData buf;
		short[] input_mask = new short[64];
		short[] hidden1_mask = new short[8];
		
		NetData buf = new NetData();
		
		transform(pos, buf.input, input_mask);
		
		affine_txfm(buf.input, buf.hidden1_out, FtOutDims, 32, hidden1_biases, hidden1_weights, input_mask,
				hidden1_mask, true);
		
		affine_txfm(buf.hidden1_out, buf.hidden2_out, 32, 32, hidden2_biases, hidden2_weights, hidden1_mask, null,false);
		
		int out_value = affine_propagate((short[]) buf.hidden2_out, output_biases, output_weights);
		return out_value / FV_SCALE;
	}

	//unsigned wt_idx(unsigned r, unsigned c, unsigned dims)
	static int wt_idx(int r, int c, int dims){
	  //(void)dims;
	  return c * 32 + r;
	}
	
	short clamp(int a, int b, int c) {
		//System.out.println(a+" < "+b+" ? "+b+ " : ("+a+" > "+c+" ? "+c+" : "+a+")"+(a < b ? b : (a > c ? c : a)));
		return (short)(	a < b ? b : (a > c ? c : a));
	}
	
	
	/*
	FEN
	*/
	private static final String piece_name = "_KQRBNPkqrbnp_";
	private static final String rank_name = "12345678";
	private static final String file_name = "abcdefgh";
	private static final String col_name = "WB";
	private static final String cas_name = "KQkq";

	//void decode_fen(const char* fen_str, int* player, int* castle, int* fifty, int* move_number, int* piece, int* square){
	void decode_fen(String fen_str, int[] player, int[] castle, int[] fifty, int[] move_number, int[] piece, int[] square){
	  //"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
	  /*decode fen*/
	  int sq,index = 2;
	  //const char* p = fen_str,*pfen;
	  int p = 0;
	  int pfen;
	  for(int r = 7;r >= 0; r--) {
	      for(int f = 0;f <= 7;f++) {
	          sq = r * 8 + f;
	          //if((pfen = strchr(piece_name,*p)) != 0) {
	          if((pfen = piece_name.indexOf(fen_str.charAt(p)))!= -1) {
	        	  
	              //int pc = (int)(strchr(piece_name,*pfen) - piece_name);
	        	  int pc = pfen;
	              if(pc == 1) {
	                 piece[0] = pc;
	                 square[0] = sq;
	              } else if(pc == 7) {
	                 piece[1] = pc;
	                 square[1] = sq;
	              } else {
	                 piece[index] = pc;
	                 square[index] = sq;
	                 index++;
	              }
	          } else if((pfen = rank_name.indexOf(fen_str.charAt(p)))!= -1) {
	              for(int i = 0;i <=pfen;i++) {
	                  f++;
	              }
	          } 
	          p++;
	      }
	      p++;
	  }
	  piece[index] = 0;
	  square[index] = 0;

	  /*player*/
      if((pfen = col_name.indexOf(fen_str.toUpperCase().charAt(p)))!= -1) {
	      player[0] = pfen;//((pfen - col_name) >= 2);
      }
	  p++;
	  p++;

	  /*castling rights*/
	  castle[0] = 0;
	  if(fen_str.charAt(p)== '-') {
	      p++;
	  } else {
	      while((pfen = cas_name.indexOf(fen_str.charAt(p)))!= -1) {
	          castle[0] |= (1 << pfen);
	          p++;
	      }
	  }
	  /*epsquare*/
	  int epsquare=0;
	  p++;
	  if(fen_str.charAt(p)== '-') {
		  epsquare = 0;
	      p++;
	  } else {
		  //epsquare = (int)(strchr(file_name,*p) - file_name);
	      epsquare = file_name.indexOf(fen_str.charAt(p));
	      p++;
	      //epsquare += 16 * (int)(strchr(rank_name,*p) - rank_name);
	      epsquare +=16 * rank_name.indexOf(fen_str.charAt(p));
	      p++;
	  }
	  square[index] = epsquare;

	  /*fifty & hply*/
	  p++;
	  /*TODO*//*
	  if(*p && *(p+1) && isdigit(*p) && ( isdigit(*(p+1)) || *(p+1) == ' ' ) ) {
	      sscanf(p,"%d %d",fifty,move_number);
	      if(*move_number <= 0) *move_number = 1;
	  } else {
	      *fifty = 0;
	      *move_number = 1;
	  }*/
      fifty[0] = 0;
      move_number[0] = 1;

	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	



	
	/******
	 * Read eval_file 	 
	 **/
	
	
	// static bool load_eval_file(const char *evalFile)
	private final boolean load_eval_file(String evalFile) {
		byte[] evalData;
		try {
			evalData = Files.readAllBytes(Paths.get(evalFile));
			System.out.println("# Loaded EvalFile successfully");
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		boolean success = verify_net(evalData);
		if (success) {
			System.out.println("# Integrity checked successfully");
			init_weights(evalData);
			System.out.println("# initalized weights successfully");
		}
		return success;
	}
	
	
	//static bool verify_net(const void *evalData, size_t size)
	private final boolean verify_net(byte[] evalData){
	  if (evalData.length != 21022697) return false;

	  int pointer = 0;
	  if (readu_le_u32(evalData,pointer) != NnueVersion) return false;
	  if (readu_le_u32(evalData,pointer + 4) != 0x3e5aa6ee) return false;
	  if (readu_le_u32(evalData,pointer + 8) != 177) return false;
	  if (readu_le_u32(evalData,pointer + TransformerStart) != 0x5d69d7b8) return false;
	  if (readu_le_u32(evalData,pointer + NetworkStart) != 0x63337156) return false;
	  
	  return true;
	}
	/*
	static void init_weights(const void *evalData){
	  const char *d = (const char *)evalData + TransformerStart + 4;

	  // Read transformer
	  for (unsigned i = 0; i < kHalfDimensions; i++, d += 2)
	    ft_biases[i] = readu_le_u16(d);
	  for (unsigned i = 0; i < kHalfDimensions * FtInDims; i++, d += 2)
	    ft_weights[i] = readu_le_u16(d);

	  // Read network
	  d += 4;
	  for (unsigned i = 0; i < 32; i++, d += 4)
	    hidden1_biases[i] = readu_le_u32(d);
	  d = read_hidden_weights(hidden1_weights, 512, d);
	  for (unsigned i = 0; i < 32; i++, d += 4)
	    hidden2_biases[i] = readu_le_u32(d);
	  d = read_hidden_weights(hidden2_weights, 32, d);
	  for (unsigned i = 0; i < 1; i++, d += 4)
	    output_biases[i] = readu_le_u32(d);
	  read_output_weights(output_weights, d);

	}
	*/
	//static void init_weights(const void *evalData){
	private final void init_weights(byte [] evalData){
	int pointer  = TransformerStart + 4;

	// Read transformer
	  for (int /*unsigned*/ i = 0; i < kHalfDimensions; i++, pointer += 2)
	    ft_biases[i] = readu_le_u16(evalData,pointer);
	  for (int /*unsigned*/ i = 0; i < kHalfDimensions * FtInDims; i++, pointer += 2)
	    ft_weights[i] = readu_le_u16(evalData,pointer);

	  // Read network
	  pointer += 4;
	  for (int /*unsigned*/ i = 0; i < 32; i++, pointer += 4) {
	    hidden1_biases[i] = readu_le_u32(evalData,pointer);
	    System.out.println("HiddenBias:"+hidden1_biases[i]);
	  }
	  pointer = read_hidden_weights(hidden1_weights, 512, evalData,pointer);
	  for (int /*unsigned*/ i = 0; i < 32; i++, pointer += 4)
	    hidden2_biases[i] = readu_le_u32(evalData,pointer);
	  pointer = read_hidden_weights(hidden2_weights, 32, evalData,pointer);
	  for (int /*unsigned*/ i = 0; i < 1; i++, pointer += 4)
	    output_biases[i] = readu_le_u32(evalData,pointer);
	  read_output_weights(output_weights, evalData,pointer);
	}
	

	//static const char *read_hidden_weights(uint8_t /*weight_t*/ *w, unsigned dims, const char *d)
	static int read_hidden_weights(short[] w, int dims, byte[] eval, int pointer)
	{
	  for (int /*unsigned*/ r = 0; r < 32; r++)
	    for (int /*unsigned*/  c = 0; c < dims; c++)
	      //w[wt_idx(r, c, dims)] = *d++;
	    	w[wt_idx(r, c, dims)] = eval[pointer++];

	  System.out.println("  # Read Hidden Weights until "+pointer +" (End of array is "+eval.length+")");
	  return pointer;
	}

	
//	static void read_output_weights(uint8_t /*weight_t*/ *w, const char *d)
	static int read_output_weights(short w[], byte[] eval, int pointer)
	{
	  for (int /*unsigned*/ i = 0; i < 32; i++) {
	    /*unsigned*/ //int c = i;
	    //w[c] = *d++;
	    w[i] = eval[pointer++];
	  }
	  System.out.println("  # Read Output Weights until "+pointer +" (End of array is "+eval.length+")");
	  return pointer;
	}

	
		
	private final int readu_le_u32(byte[] evalData, int pointer)
	{
	 return ((evalData[pointer+0]&0xFF) | ((evalData[pointer+1]&0xFF) << 8) | ((evalData[pointer+2]&0xFF) << 16) | ((evalData[pointer+3]&0xFF) << 24));
	}

	private final short readu_le_u16(byte[] evalData, int pointer)
	{
//		System.out.println(String.format("0x%08X", evalData[pointer+0]&0xFF));
//		System.out.println(String.format("0x%08X", evalData[pointer+1]&0xFF));
//		System.out.println(String.format("0x%08X", (int)((evalData[pointer+0]&0xFF) | ((evalData[pointer+1]&0xFF) << 8))));
// 		System.out.println((short)((evalData[pointer+0]&0xFF) | ((evalData[pointer+1]&0xFF) << 8)));
//		
		return (short)((evalData[pointer+0]&0xFF) | ((evalData[pointer+1]&0xFF) << 8));
	}

	/************************************************************************
	*         EXTERNAL INTERFACES
	*
	* Load a NNUE file using
	*
	*   nnue_init(file_path)
	*
	* and then probe score using one of three functions, whichever
	* is convenient. From easy to hard
	*   
	*   a) nnue_evaluate_fen         - accepts a fen string for evaluation
	*   b) nnue_evaluate             - suitable for use in engines
	*   c) nnue_evaluate_incremental - for ultimate performance but will need
	*                                  some work on the engines side.
	*
	**************************************************************************/

	/**
	* Load NNUE file
	*/
	public final void nnue_init(String evalFile)
	{
	  System.out.println("*** LOADING NNUE: "+evalFile+" ***");
	  
	  if (load_eval_file(evalFile)) {
		System.out.println("*** NNUE LOADED! ***");
	    return;
	  }
	  System.out.println(" ! NNUE FILE NOT FOUD OR CORRUPTED!");
	}

	
	/**
	* Evaluation subroutine suitable for chess engines.
	* -------------------------------------------------
	* Piece codes are
	*     wking=1, wqueen=2, wrook=3, wbishop= 4, wknight= 5, wpawn= 6,
	*     bking=7, bqueen=8, brook=9, bbishop=10, bknight=11, bpawn=12,
	* Squares are
	*     A1=0, B1=1 ... H8=63
	* Input format:
	*     piece[0] is white king, square[0] is its location
	*     piece[1] is black king, square[1] is its location
	*     ..
	*     piece[x], square[x] can be in any order
	*     ..
	*     piece[n+1] is set to 0 to represent end of array
	* Returns
	*   Score relative to side to move in approximate centi-pawns
	*/

	//int nnue_evaluate(int player, int* pieces, int* squares)
	public final int nnue_evaluate(int player, int[] pieces, int[] squares){
	  NNUEdata nnue= new NNUEdata();
	  nnue.accumulator.computedAccumulation = 0;
	  Position pos=new Position();
	  	  pos.nnue[0] = /*&*/nnue;
	  	  pos.nnue[1] = null;//=0;
	  	  pos.nnue[2] = null;//=0;
	  pos.player = player;
	  pos.pieces = pieces;
	  pos.squares = squares;
	  return nnue_evaluate_pos(/*&*/pos);
	}

	
	/**
	* Incremental NNUE evaluation function.
	* -------------------------------------------------
	* First three parameters and return type are as in @nnue_evaluate
	*
	* nnue_data
	*    nnue_data[0] is pointer to NNUEdata for ply i.e. current position
	*    nnue_data[1] is pointer to NNUEdata for ply - 1
	*    nnue_data[2] is pointer to NNUEdata for ply - 2
	*/

	//int nnue_evaluate_incremental(int player, int* pieces, int* squares, NNUEdata** nnue)
	public final int nnue_evaluate_incremental(int player, int[] pieces, int[] squares, NNUEdata[] nnue)
	{
	  //assert(nnue[0] && (uint64_t)(&nnue[0].accumulator) % 64 == 0);

	  Position pos = new Position();
	  pos.nnue[0] = nnue[0];
	  pos.nnue[1] = nnue[1];
	  pos.nnue[2] = nnue[2];
	  pos.player = player;
	  pos.pieces = pieces;
	  pos.squares = squares;
	  return nnue_evaluate_pos(/*&*/pos);
	}

	/**
	* Evaluate on FEN string
	* Returns
	*   Score relative to side to move in approximate centi-pawns
	*/
	public final int nnue_evaluate_fen(String fen){
		
		int[] pieces=new int[33];
		int[] squares=new int[33];
		int[] player=new int[1];
		int[] castle=new int[1];
		int[] fifty=new int[1];
		int[] move_number=new int[1];
		//decode_fen((char*)fen,&player,&castle,&fifty,&move_number,pieces,squares);;
		decode_fen(fen,player,castle,fifty,move_number,pieces,squares);
		if(testInitialFEN(player[0],pieces,squares)) {
			System.out.println("# Fen Decoding according to c++ reference");
		}else {
			System.out.println("# Fen Decoding error");
			System.exit(-1);
		}
		return nnue_evaluate(player[0],pieces,squares);
	}

	/**
	 * Loading NNUE : nn-04cf2b4ed1da.nnue
	 * NNUE loaded !
	 * Score =  42
	 * @param args
	 */
	public static void main(String[] args) {
		NNUE_1 nnue = new NNUE_1();
		//nnue.nnue_init("C:/Users/andre/eclipse-workspace6/chess/nn-eba324f53044.nnue");
		nnue.nnue_init("/home/linux-ml/git/chess/my.nnue");
		
		int score = nnue.nnue_evaluate_fen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
		System.out.println(score);
	}
	
	/*
	 * player 0

		pieces
		  1, 7, 
		  9, 11, 10,  8, 10, 11, 9, 12, 12, 12, 12, 12, 12, 12, 12,
		  6,  6,  6, 6,  6,  6, 6,  6,  3,  5,  4,  2,  4,  5,  3,
		  0

		squares
		  4, 60, 
		 56, 57, 58, 59, 61, 62, 63, 48, 49, 50, 51, 52, 53, 54, 55,
		  8,  9, 10, 11, 12, 13, 14, 15,  0,  1,  2,  3,  5,  6,  7,
		  0
	 */
	private boolean testInitialFEN(int player, int[] pieces, int[] squares) {
		if(player !=0) {
			System.out.println(" ! Player: "+player+" expected 0");
			return false;
		}
		int[] refPieces = new int[] { 1, 7, 
				  9, 11, 10,  8, 10, 11, 9, 12, 12, 12, 12, 12, 12, 12, 12,
				  6,  6,  6, 6,  6,  6, 6,  6,  3,  5,  4,  2,  4,  5,  3,
				  0};
		int[] refSquares = new int[] { 4, 60, 
				 56, 57, 58, 59, 61, 62, 63, 48, 49, 50, 51, 52, 53, 54, 55,
				  8,  9, 10, 11, 12, 13, 14, 15,  0,  1,  2,  3,  5,  6,  7,
				  0};
		for(int i=0;i<pieces.length;i++) {
			if(pieces[i]!=refPieces[i]) {
				System.out.println(" ! Pieces["+i+"]: "+pieces[i]+" expected "+refPieces[i]);
				return false;
			}
		}
		for(int i=0;i<squares.length;i++) {
			if(squares[i]!=refSquares[i]) {
				System.out.println(" ! Squares["+i+"]: "+squares[i]+" expected "+refSquares[i]);
				return false;
			}
		}
		return true;
	}
	
}
