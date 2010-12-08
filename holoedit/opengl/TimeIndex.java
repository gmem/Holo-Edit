/**
 *  -----------------------------------------------------------------------------
 *  
 *  Holo-Edit, spatial sound trajectories editor, part of Holophon
 *  Copyright (C) 2006 GMEM
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA 
 *  
 *  -----------------------------------------------------------------------------
 */
package holoedit.opengl;

/**
 * utility class indexing picked objects on a 32 bit signed integer
 * 
 * GLUint : 32bits
 * ___________________________________________________________________________________________
 * |		TYPE			|		TK			|		SEQ				|		PT				|
 * |-----------------	|--------------------	|------------------------	|------------------------	|
 * |		2 (0-1)		|		6 (0-63)		|		10 (0-1023)		|		14 (0-16383)		|
 * |-----------------	|--------------------	|------------------------	|------------------------	|
 * |	 	0 = point	|	curve number		|		seq number		|		point number		|
 * |-----------------	|--------------------	|------------------------	|------------------------	|
 * |	 	1 = line		|	curve number		|		seq number		|		point number		|
 * |-----------------	|--------------------	|------------------------	|------------------------	|
 * |	 	2 = curve	|	curve number		|			�			|			�			|
 * |-----------------	|--------------------	|------------------------	|------------------------	|
 * |	 	3 = other	|		�			|		0 : scroll		|	0 : h_scroll			|
 * |					|					|						|	1 : h_scroll_left	|
 * |					|					|						|	2 : v_scroll_right	|
 * |					|					|						|	3 : v_scroll			|
 * |					|					|						|	4 : v_scroll_left	|
 * |					|					|						|	5 : v_scroll_right	|
 * |	 	 			|		�			|		1 : timescale	|	0 : timescale		|
 * |					|					|						|	1 : timescale_bloc	|
 * |					|					|						|	2 : timescale_beg	|
 * |					|					|						|	3 : timescale_end	|
 * |					|					|						|	4 : timescale_back	|
 * |					|					|						|	5 : timescale_forw	|
 * |					|					|						|	6 : local_timescale	|
 * |	 	 			|	curve number		|		2 : header		|	0 : header_poly		|
 * |					|					|						|	1 : +				|
 * |					|					|						|	2 : -				|
 * |					|					|						|	3 : scale			|
 * |-----------------	|--------------------	|------------------------	|------------------------	|
 * |		3			|		63			|		1023				|		16383				|	<< NULL
 * |_________________	|____________________	|________________________	|________________________	|
 * 
 */

public class TimeIndex
{
	public static final int SIZE_TYPE = 2;
	public static final int SIZE_CURVE = 6;
	public static final int SIZE_SEQ = 10;
	public static final int SIZE_PT = 14;
	public static final int NBBIT_TYPE = (int)Math.pow(2,SIZE_TYPE) - 1;
	public static final int NBBIT_CURVE = (int)Math.pow(2,SIZE_CURVE) - 1;
	public static final int NBBIT_SEQ = (int)Math.pow(2,SIZE_SEQ) - 1;
	public static final int NBBIT_PT = (int)Math.pow(2,SIZE_PT) - 1;
	public static final int MASK_PT_TYPE = NBBIT_PT;
	public static final int MASK_SEQ_NUM = NBBIT_SEQ << SIZE_PT;
	public static final int MASK_CURVE_NUM = NBBIT_CURVE << ( SIZE_SEQ + SIZE_PT );

	public static final int TYPE_PT = 0;
	public static final int TYPE_LINE = 1;
	public static final int TYPE_CURVE = -2;
	public static final int TYPE_OT = -1;
	
	public static final int SCROLL = 0;
	
	public static final int SCROLL_H = 0;
	public static final int SCROLL_H_LEFT = 1;
	public static final int SCROLL_H_RIGHT = 2;
	public static final int SCROLL_V = 3;
	public static final int SCROLL_V_LEFT = 4;
	public static final int SCROLL_V_RIGHT = 5;
	
	public static final int TIMESCALE = 1;
	
	public static final int TIMESCALE_BG = 0;
	public static final int TIMESCALE_BLOC = 1;
	public static final int TIMESCALE_BEG = 2;
	public static final int TIMESCALE_END = 3;
	public static final int TIMESCALE_BACK = 4;
	public static final int TIMESCALE_FORW = 5;
	public static final int TIMESCALE_LOCAL = 6;
	
	public static final int HEADER = 2;
	
	public static final int HEADER_POLY = 0;
	public static final int HEADER_PLUS = 1;
	public static final int HEADER_MINUS = 2;
	public static final int HEADER_SCALE = 3;
	
	public static final int NULL = encode(3,63,1023,16383);
	
	public static final int SCROLL_H_IND = encode(TYPE_OT,0,SCROLL,SCROLL_H);
	public static final int SCROLL_HL_IND = encode(TYPE_OT,0,SCROLL,SCROLL_H_LEFT);
	public static final int SCROLL_HR_IND = encode(TYPE_OT,0,SCROLL,SCROLL_H_RIGHT);
	public static final int SCROLL_V_IND = encode(TYPE_OT,0,SCROLL,SCROLL_V);
	public static final int SCROLL_VL_IND = encode(TYPE_OT,0,SCROLL,SCROLL_V_LEFT);
	public static final int SCROLL_VR_IND = encode(TYPE_OT,0,SCROLL,SCROLL_V_RIGHT);
	
	public static final int TIMESCALE_BG_IND = encode(TYPE_OT,0,TIMESCALE,TIMESCALE_BG);
	public static final int TIMESCALE_BLOC_IND = encode(TYPE_OT,0,TIMESCALE,TIMESCALE_BLOC);
	public static final int TIMESCALE_BEG_IND = encode(TYPE_OT,0,TIMESCALE,TIMESCALE_BEG);
	public static final int TIMESCALE_END_IND = encode(TYPE_OT,0,TIMESCALE,TIMESCALE_END);
	public static final int TIMESCALE_BACK_IND = encode(TYPE_OT,0,TIMESCALE,TIMESCALE_BACK);
	public static final int TIMESCALE_FORW_IND = encode(TYPE_OT,0,TIMESCALE,TIMESCALE_FORW);
	public static final int TIMESCALE_LOCAL_IND = encode(TYPE_OT,0,TIMESCALE,TIMESCALE_LOCAL);
	
	public static int type = -1;
	public static int cu = -1;
	public static int seq = -1;
	public static int pt = -1;

	public static int getNull()
	{
		return NULL;
	}
	public static boolean isNull(int index)
	{
		return index == NULL;
	}

	public static int encode(int _type, int tkNum, int seqNum, int ptNum)
	{
		return (_type << (SIZE_CURVE+SIZE_SEQ+SIZE_PT)) + (tkNum << (SIZE_SEQ+SIZE_PT)) + (seqNum << SIZE_PT) + ptNum;
	}
	
	public static int[] decode(int code)
	{
		type = (code >> (SIZE_CURVE+SIZE_SEQ+SIZE_PT));
		cu = (code & MASK_CURVE_NUM) >> (SIZE_SEQ+SIZE_PT);
		seq = (code & MASK_SEQ_NUM) >> SIZE_PT;
		pt = code & MASK_PT_TYPE;
		
		int[] tmp = {type,cu,seq,pt};
		return tmp;
	}
	
	public static String toStr()
	{
		return("type:"+type+" tk:"+cu+" seq:"+seq+" pt:"+pt);
	}
	
	public static String toStr2()
	{
		return("("+type+" "+cu+" "+seq+" "+pt+")");
	}
	
	public static boolean isPoint()
	{
		return type == TYPE_PT;
	}
	
	public static boolean isLine()
	{
		return type == TYPE_LINE;
	}

	public static boolean isCurve()
	{
		return type == TYPE_CURVE;
	}
	
	public static int getCurve()
	{
		return cu;
	}
	
	public static int getSeq()
	{
		return seq;
	}

	public static int getPt()
	{
		return pt;
	}
	
	public static boolean isHeaderPoly()
	{
		return type == TYPE_OT && seq == HEADER && pt == HEADER_POLY;
	}
	
	public static boolean isHeaderScale()
	{
		return type == TYPE_OT && seq == HEADER && pt == HEADER_SCALE;
	}
	
	public static boolean isPlus()
	{
		return type == TYPE_OT && seq == HEADER && pt == HEADER_PLUS;
	}
	
	public static boolean isMinus()
	{
		return type == TYPE_OT && seq == HEADER && pt == HEADER_MINUS;
	}
	
	public static boolean isScroll()
	{
		return type == TYPE_OT && seq == SCROLL;
	}
	public static boolean isScrollH()
	{
		return type == TYPE_OT && seq == SCROLL && pt == SCROLL_H;
	}
	public static boolean isScrollV()
	{
		return type == TYPE_OT && seq == SCROLL && pt == SCROLL_V;
	}
	public static boolean isScrollHLeft()
	{
		return type == TYPE_OT && seq == SCROLL && pt == SCROLL_H_LEFT;
	}
	public static boolean isScrollHRight()
	{
		return type == TYPE_OT && seq == SCROLL && pt == SCROLL_H_RIGHT;
	}
	public static boolean isScrollVLeft()
	{
		return type == TYPE_OT && seq == SCROLL && pt == SCROLL_V_LEFT;
	}
	public static boolean isScrollVRight()
	{
		return type == TYPE_OT && seq == SCROLL && pt == SCROLL_V_RIGHT;
	}
	public static boolean isTimeScaleLocal()
	{
		return type == TYPE_OT && seq == TIMESCALE && pt == TIMESCALE_LOCAL;
	}
	public static boolean isTimeScaleBeg()
	{
		return type == TYPE_OT && seq == TIMESCALE && pt == TIMESCALE_BEG;
	}
	public static boolean isTimeScaleEnd()
	{
		return type == TYPE_OT && seq == TIMESCALE && pt == TIMESCALE_END;
	}
	public static boolean isTimeScaleBack()
	{
		return type == TYPE_OT && seq == TIMESCALE && pt == TIMESCALE_BACK;
	}
	public static boolean isTimeScaleForw()
	{
		return type == TYPE_OT && seq == TIMESCALE && pt == TIMESCALE_FORW;
	}
	public static boolean isTimeScaleBloc()
	{
		return type == TYPE_OT && seq == TIMESCALE && pt == TIMESCALE_BLOC;
	}
	public static boolean isTimeScaleBg()
	{
		return type == TYPE_OT && seq == TIMESCALE && pt == TIMESCALE_BG;
	}
}
