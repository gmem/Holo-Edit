/*
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
 * 
0 TK	|	TRACK NUM	|	SEQ_NUM/WAV_NUM/DATA_NUM	|	OBJ_TYPE seq/seqbeg/seqend/wav/head/height/zoom/disp/data/databeg/dataend
1b	|	6b(0-63)		|	10b(0-1023)					|	6b(0-127)
------------------------------------------------------------------------------------------
1 OT	|	TRACK NUM	|	MARKER_NUM 		|	OBJ_TYPE scroll_time-scroll_time_left-scroll_time_right/scroll_track-scroll_track_left-scroll_track_right/time_scale/scrub_cursor/marker
1b	|	6(0-63)		|	10b(0-1023)		|	6b(0-127)			
------------------------------------------------------------------------------------------
 *
 */

public class ScoreIndex
{
	public static final int SIZE_TYPE = 1;
	public static final int SIZE_TRACK = 6;
	public static final int SIZE_SEQ = 10;
	public static final int SIZE_OBJ = 6;
	public static final int NBBIT_TYPE = (int)Math.pow(2,SIZE_TYPE) - 1;
	public static final int NBBIT_TRACK = (int)Math.pow(2,SIZE_TRACK) - 1;
	public static final int NBBIT_SEQ = (int)Math.pow(2,SIZE_SEQ) - 1;
	public static final int NBBIT_OBJ = (int)Math.pow(2,SIZE_OBJ) - 1;
	public static final int MASK_OBJ_TYPE = NBBIT_OBJ;
	public static final int MASK_SEQ_NUM = NBBIT_SEQ << SIZE_OBJ;
	public static final int MASK_TRACK_NUM = NBBIT_TRACK << ( SIZE_SEQ + SIZE_OBJ );

	public static final int TYPE_TK = 0;
	public static final int TYPE_OT = 1;
	
	public static final int SEQ_POLY = 0;
	public static final int SEQ_BEGIN = 1;
	public static final int SEQ_END = 2;
	public static final int WAVE_POLY = 3;
	public static final int TK_HEAD = 4;
	public static final int TK_HEIGHT = 5;
	public static final int TK_ZOOM = 6;
	public static final int TK_DISP = 7;
	public static final int DATA_POLY = 8;
	public static final int DATA_BEGIN = 9;
	public static final int DATA_END = 10;

	// scrollBar horizontale et boutton +/- (zoom horizontal sur le temps)
	public static final int OT_SCROLL_H = 0;
	public static final int OT_SCROLL_HL = 1;
	public static final int OT_SCROLL_HR = 2;
	public static final int OT_SCROLL_H_BP = 3;	// boutton + 
	public static final int OT_SCROLL_H_BM = 4;	// boutton - 
	public static final int OT_SCROLL_H_IND = encode(TYPE_OT,0,0,OT_SCROLL_H);
	public static final int OT_SCROLL_HL_IND = encode(TYPE_OT,0,0,OT_SCROLL_HL);
	public static final int OT_SCROLL_HR_IND = encode(TYPE_OT,0,0,OT_SCROLL_HR);
	public static final int OT_SCROLL_H_BP_IND = encode(TYPE_OT,0,0,OT_SCROLL_H_BP);
	public static final int OT_SCROLL_H_BM_IND = encode(TYPE_OT,0,0,OT_SCROLL_H_BM);
	
	// scrollBar verticale et boutton +/- (zoom vertical sur les tracks)
	public static final int OT_SCROLL_V = 5;
	public static final int OT_SCROLL_VDOWN = 6;
	public static final int OT_SCROLL_VUP = 7;
	public static final int OT_SCROLL_V_BP = 8;	// boutton + 
	public static final int OT_SCROLL_V_BM = 9;	// boutton - 
	public static final int OT_SCROLL_V_IND = encode(TYPE_OT,0,0,OT_SCROLL_V);
	public static final int OT_SCROLL_VDOWN_IND = encode(TYPE_OT,0,0,OT_SCROLL_VDOWN);
	public static final int OT_SCROLL_VUP_IND = encode(TYPE_OT,0,0,OT_SCROLL_VUP);
	public static final int OT_SCROLL_V_BP_IND = encode(TYPE_OT,0,0,OT_SCROLL_V_BP);
	public static final int OT_SCROLL_V_BM_IND = encode(TYPE_OT,0,0,OT_SCROLL_V_BM);
	
	public static final int OT_TIMESCALE = 6;
	public static final int OT_TIMESEL_BEG = 7;
	public static final int OT_TIMESEL_END = 8;
	public static final int OT_TIMESEL = 9;
	public static final int OT_TIMESCALE_IND = encode(TYPE_OT,0,0,OT_TIMESCALE);
	public static final int OT_TIMESEL_BEG_IND = encode(TYPE_OT,0,0,OT_TIMESEL_BEG);
	public static final int OT_TIMESEL_END_IND = encode(TYPE_OT,0,0,OT_TIMESEL_END);
	public static final int OT_TIMESEL_IND = encode(TYPE_OT,0,0,OT_TIMESEL);
	
	public static final int OT_SCRUB_CURSOR = 10;
	public static final int OT_SCRUB_CURSOR_IND = encode(TYPE_OT,0,0,OT_SCRUB_CURSOR);
	
	public static final int OT_MARKER = 11;
	
	public static final int NULL = -1;
	
	public static int type = -1;
	public static int tk = -1;
	public static int seq = -1;
	public static int obj = -1;

	public static int getNull()
	{
		return NULL;
	}
	public static boolean isNull(int index)
	{
		return index == NULL;
	}

	public static int encode(int _type, int tkNum, int seqNum, int objType)
	{
		return (_type << (SIZE_TRACK+SIZE_SEQ+SIZE_OBJ)) + (tkNum << (SIZE_SEQ+SIZE_OBJ)) + (seqNum << SIZE_OBJ) + objType;
	}
	
	public static int[] decode(int code)
	{
		type = (code >> (SIZE_TRACK+SIZE_SEQ+SIZE_OBJ));
		tk = (code & MASK_TRACK_NUM) >> (SIZE_SEQ+SIZE_OBJ);
		seq = (code & MASK_SEQ_NUM) >> SIZE_OBJ;
		obj = code & MASK_OBJ_TYPE;
		
		int[] tmp = new int[]{type,tk,seq,obj};
		return tmp;
	}
	
	public static String toStr()
	{
		return("type:"+type+" tk:"+tk+" seq:"+seq+" obj:"+obj);
	}
	
	public static String toStr2()
	{
		return("("+type+" "+tk+" "+seq+" "+obj+")");
	}
	
	public static boolean isTrack()
	{
		return type == TYPE_TK && obj == TK_DISP;
	}
	
	public static int getTrack()
	{
		return tk;
	}
	
	public static boolean isSeq()
	{
		return type == TYPE_TK && obj == SEQ_POLY;
	}
	
	public static boolean isWave()
	{
		return type == TYPE_TK && obj == WAVE_POLY;
	}
	
	public static boolean isData() // used for sdif
	{
		return type == TYPE_TK && obj == DATA_POLY;
	}
	
	public static int getSeq()
	{
		return seq;
	}

	public static boolean isSeqBegin()
	{
		return type == TYPE_TK && obj == SEQ_BEGIN;
	}
	
	public static boolean isSeqEnd()
	{
		return type == TYPE_TK && obj == SEQ_END;
	}
	
	public static boolean isDataBegin()
	{
		return type == TYPE_TK && obj == DATA_BEGIN;
	}
	
	public static boolean isDataEnd()
	{
		return type == TYPE_TK && obj == DATA_END;
	}
	
	public static boolean isHeader()
	{
		return type == TYPE_TK && obj == TK_HEAD;
	}
	
	public static boolean isTimeScale()
	{
		return type == TYPE_OT && obj == OT_TIMESCALE;
	}
	public static boolean isTimeSelBeg()
	{
		return type == TYPE_OT && obj == OT_TIMESEL_BEG;
	}
	public static boolean isTimeSelEnd()
	{
		return type == TYPE_OT && obj == OT_TIMESEL_END;
	}
	public static boolean isTimeSel()
	{
		return type == TYPE_OT && obj == OT_TIMESEL;
	}
	public static boolean isTimeScroll()
	{
		return type == TYPE_OT && obj == OT_SCROLL_H;
	}
	public static boolean isTimeScrollLeft()
	{
		return type == TYPE_OT && obj == OT_SCROLL_HL;
	}
	public static boolean isTimeScrollRight()
	{
		return type == TYPE_OT && obj == OT_SCROLL_HR;
	}
	public static boolean isTrackScroll()
	{
		return type == TYPE_OT && obj == OT_SCROLL_V;
	}
	public static boolean isTrackScrollDown()
	{
		return type == TYPE_OT && obj == OT_SCROLL_VDOWN;
	}
	public static boolean isTrackScrollUp()
	{
		return type == TYPE_OT && obj == OT_SCROLL_VUP;
	}
	public static boolean isButtonPlus()
	{
		return obj == OT_SCROLL_H_BP || obj == OT_SCROLL_V_BP ;
	}
	public static boolean isButtonMinus()
	{
		return obj == OT_SCROLL_H_BM || obj == OT_SCROLL_V_BM ;
	}
}
