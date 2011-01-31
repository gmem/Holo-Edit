package holoedit.data;

import holoedit.opengl.*;

/** usefull for synchronising Selected Points between different views **/
public class HoloPointBackRef {
	
	public HoloPoint p;
	public int tkNum;
	public int seqNum;
	public int ptNum;
	
	public HoloPointBackRef(HoloPoint _p)
	{
		p = _p;
	}
	
	public void setFromRoom(int index)
	{
		RoomIndex.decode(index);
		tkNum = RoomIndex.getTrack();
		seqNum = RoomIndex.getSeq();
		ptNum = RoomIndex.getPt();
	}
	
	public void setFromTime(int index, int _tkNum)
	{
		TimeIndex.decode(index);
		tkNum = _tkNum;
		seqNum = TimeIndex.getSeq();
		ptNum = TimeIndex.getPt();
	}
	
	public int encodeRoom()
	{
		return RoomIndex.encode(RoomIndex.TYPE_PT, tkNum, seqNum, ptNum);
	}
	
	public int encodeTime()
	{
		/* type, curve, seq, point */
		return TimeIndex.encode(RoomIndex.TYPE_PT, 1, seqNum, ptNum);
	}

}
