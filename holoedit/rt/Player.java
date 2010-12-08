package holoedit.rt;

import holoedit.gui.SimpleSoundPool;

public interface Player
{
	public boolean askForSave();
	public boolean isPlaying();
	public void stop();
	public void setSaved(boolean b);
	public int setSessionName(String s);
	public void send(String s);
	public void done();
	public void setBegin(int i);
	public void setEnd(int i);
	public void setTotal(int i);
	public SimpleSoundPool getSoundPool();
	public void update();
}
