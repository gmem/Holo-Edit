package holoedit.gui;

import holoedit.HoloEdit;
import holoedit.data.HoloWaveForm;
import holoedit.data.HoloExternalData;
import holoedit.data.HoloSDIFdata;
import holoedit.data.HoloSDIFdataStat;
import holoedit.opengl.OpenGLUt;
import holoedit.opengl.TessCallback;
import holoedit.util.Ut;
import java.awt.GridLayout;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;
import javax.media.opengl.glu.GLUtessellator;
import javax.swing.JPanel;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import com.sun.opengl.util.GLUT;

public class HoloPointEditorRenderer extends JPanel implements GLEventListener {
	
	public float[] bgColor = {1,1,1,1};
	public float[] marginColor = {0.8f,0.8f,0.8f,1};
	public float[] waveColor = {0,0,0,1};
	public float[] waveColorTrans = {0,0,0,0.4f};
	private float[] wc = waveColor;
	public float[] dataColor = {0,0,0.8f,1f};
	public float[] dataColorTrans = {0,0,0.8f,0.4f};
	public float[] dc = dataColor;
	//public float[] polyWaveColor = {0.5f,0.5f,0.5f,0.5f};
	private float[] scaleLineColor = { 0, 0, 0, 1 };
	private float[] borderColor = { 0.95f, 0.95f, 0.95f, 1 };
	private HoloWaveForm hwf;
	private HoloExternalData hxtdt;
	private HoloSDIFdata[] hsdifdtTab;	
	private GLU glu = new GLU();
	private GL gl;
	private GLUT glut = new GLUT();
	private GLUtessellator tobj;
	private TessCallback tessCallback;
	private int width;
	private int height;
	private final int YscaleWidth = 40;
	private int YscaleW = 0;
	private float scaleLineWidth = 1;
	private final int marginHeight = 10;
	private int drawMarginHeight = 5;
	private float minTimeHwf = Float.MAX_VALUE;
	private float maxTimeHwf = Float.MIN_VALUE;
	private float minTimeDatas = Float.MAX_VALUE;
	private float maxTimeDatas = Float.MIN_VALUE;
	private float minX = Float.MAX_VALUE;
	private float maxX = Float.MIN_VALUE;
	private float minXview = Float.MAX_VALUE;
	private float maxXview = Float.MAX_VALUE;
	private float minYdatas = 0;
	private float maxYdatas = 50;
	private int minYwav = -250;
	private int maxYwav = 250;
	private int listID = 0;
	private int YscaleListID = 0;
	private int marginListID = 0;
	private int axeListID = 0;
	private GLCanvas glp;
	
	public HoloPointEditorRenderer(HoloEdit owner)
	{
		super();
		glp = new GLCanvas(owner.glcap, null, owner.glpb.getContext(), null);
		glp.addGLEventListener(this);
	//	glp.addMouseWheelListener(this);
		setLayout(new GridLayout(1, 1));
		add(glp);
	}
	
	public void setHoloWaveForm(HoloWaveForm _hwf)
	{
		hwf = _hwf;
		if(hwf != null) {
			maxTimeHwf = hwf.getFileLength();
			minTimeHwf = 0;
		} else {
			maxTimeHwf = Float.MIN_VALUE;
			minTimeHwf = Float.MAX_VALUE;
		}
		glp.display();
	}	

	public void setHoloSDIFdataTab(HoloSDIFdata[] _hsdifdt)
	{
		hsdifdtTab = _hsdifdt;
		if (hsdifdtTab != null && hsdifdtTab.length>0){
			minTimeDatas = HoloSDIFdataStat.getSmallestSDIFstartTime(hsdifdtTab);
			maxTimeDatas = HoloSDIFdataStat.getBiggestSDIFendTime(hsdifdtTab);
			minYdatas = HoloSDIFdataStat.getSmallestY(hsdifdtTab);
			maxYdatas = HoloSDIFdataStat.getBiggestY(hsdifdtTab);
			YscaleW = YscaleWidth;
		} else{
			minTimeDatas = Float.MAX_VALUE;
			maxTimeDatas = Float.MIN_VALUE;
			YscaleW = 0;
		}
		drawMarginHeight = maxYdatas==minYdatas ? 10: 0;

		gl.glDeleteLists(YscaleListID, 1);
		YscaleListID = 0;
		
		glp.display();
	}
	
	public HoloWaveForm getHoloWaveForm(){
		return hwf;
	}

	public HoloExternalData getHoloExternalData(){
		return hxtdt;
	}
	
	public void init(GLAutoDrawable drawable)
	{
		gl = drawable.getGL();
		
		tessCallback = new TessCallback(gl,glu);
		tobj = glu.gluNewTess();
		glu.gluTessCallback(tobj, GLU.GLU_TESS_VERTEX, tessCallback);
		glu.gluTessCallback(tobj, GLU.GLU_TESS_BEGIN, tessCallback);
		glu.gluTessCallback(tobj, GLU.GLU_TESS_END, tessCallback);
		glu.gluTessCallback(tobj, GLU.GLU_TESS_ERROR, tessCallback);
		glu.gluTessCallback(tobj, GLU.GLU_TESS_COMBINE, tessCallback);

		gl.glShadeModel(GL.GL_SMOOTH); // Enable Smooth Shading
		gl.glClearColor(bgColor[0],bgColor[1],bgColor[2],bgColor[3]); // White Background
		gl.glPointSize(5);
		gl.glViewport(0, 0, width, height);
		gl.glEnable(GL.GL_POINT_SMOOTH);
		gl.glEnable(GL.GL_BLEND);
		gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		gl.glMatrixMode(GL.GL_PROJECTION);
		
		if(listID == 0)
		{
			listID = gl.glGenLists(1000);
		}
	}
	
	public void display(GLAutoDrawable drawable)
	{
		gl = drawable.getGL();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		gl.glLoadIdentity();

		maxX = Ut.max(maxTimeDatas, maxTimeHwf);
		minX = Ut.min(minTimeDatas, minTimeHwf);
		
		gl.glViewport(0, 0, width, height);

		// axes si hwf prŽsente
		if(hwf != null)
			drawAxe();
		// marges
		drawMargins();
		// echelle si sdif prŽsentes
		if (hsdifdtTab != null) {
			gl.glViewport(width-YscaleW, marginHeight, YscaleW, height-2*marginHeight);
			drawYScale();
		}
		gl.glViewport(0, marginHeight, width-YscaleW, height-2*marginHeight);
		glu.gluOrtho2D(minX, maxX, minYwav, maxYwav);		
		if(hwf != null)
			if(hwf.isFine())
			{
				gl.glPushMatrix();
				gl.glEnable(GL.GL_BLEND);

				if (hsdifdtTab != null && hsdifdtTab.length>0) // si affichage de sdif en mm tps, on met de la transparence
					wc = waveColorTrans;
				else wc = waveColor;

				listID = hwf.drawSoundPool(gl, wc,listID);
				gl.glDisable(GL.GL_BLEND); 	
				gl.glPopMatrix();
			}
		gl.glLoadIdentity();

		gl.glViewport(0, marginHeight, width-YscaleW, height-2*marginHeight);
		
		glu.gluOrtho2D(minX, maxX, minYdatas-drawMarginHeight, maxYdatas+drawMarginHeight);
		if (hsdifdtTab != null){
			gl.glPushMatrix();
			gl.glEnable(GL.GL_BLEND);
			
			if (hwf != null && hwf.isFine()) // si affichage de hwf en mm tps, on met de la transparence
				dc = dataColorTrans;
			else
				dc = dataColor;
			
			for (int i=0; i<hsdifdtTab.length; i++)
				listID = hsdifdtTab[i].drawSoundPool(gl, dc, listID);	
			gl.glDisable(GL.GL_BLEND); 	
			gl.glPopMatrix();
		}
		gl.glLoadIdentity();
		
		gl.glFlush();
	}
	
	/** Dessin des deux marges.	 */
	private void drawMargins()
	{
		if (marginListID == 0) {
			marginListID = gl.glGenLists(1);
			gl.glNewList(marginListID, GL.GL_COMPILE_AND_EXECUTE);
			OpenGLUt.glColor(gl,marginColor);
			glu.gluOrtho2D(0, width, height, 0);
			gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
			gl.glRectf(0, 0, width, marginHeight);		// top margin
			gl.glRectf(0, height-marginHeight, width, height);		// bottom margin
			gl.glEndList();
		}else {
			gl.glCallList(marginListID);
		}
		gl.glLoadIdentity();
	}
	
	/** Dessin de l'axe horizontal. */
	private void drawAxe()
	{
		if (axeListID == 0) {
			axeListID = gl.glGenLists(1);
			gl.glNewList(axeListID, GL.GL_COMPILE_AND_EXECUTE);
				OpenGLUt.glColor(gl,marginColor);
				glu.gluOrtho2D(0, width, height, 0);
				gl.glBegin(GL.GL_LINES);
					OpenGLUt.drawPoint(gl,0,height/2);
					OpenGLUt.drawPoint(gl, width-YscaleWidth,height/2);
				gl.glEnd();
			gl.glEndList();
		}else {
			gl.glCallList(axeListID);
		}
		gl.glLoadIdentity();
	}
	
	private void drawYScale()
	{
		if (YscaleListID == 0)
		{
			YscaleListID = gl.glGenLists(1);
			gl.glNewList(YscaleListID, GL.GL_COMPILE_AND_EXECUTE);
			float TPOS = 0.55f;
			glu.gluOrtho2D(0, 1.5f, minYdatas-drawMarginHeight, maxYdatas+drawMarginHeight);
			// le rectangle de fond
			gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
			gl.glColor4fv(borderColor, 0);
			gl.glRectf(0f, minYdatas-drawMarginHeight, 1.5f, maxYdatas+drawMarginHeight);
	
			gl.glLineWidth(scaleLineWidth);
	//		gl.glColor4fv(scaleLineColor, 0);
			gl.glColor4fv(dataColor, 0);

			int begNumber = (int) Math.floor(minYdatas); // on part de l'entier inferieur

			float offset = (maxYdatas - minYdatas)/20;

			if (offset>10) {
				offset = (int) Math.floor(offset/10)*10;
			} else if (offset==0) {
				offset = 1;
			}else {
				int n=2;
				float tempOffset;
				do {
					tempOffset = (float) Ut.floor(offset, n);
					n++;
				}while (tempOffset==0f); // au cas ou < 0.01
				offset = tempOffset;
			}
			gl.glBegin(GL.GL_LINES);

			for (float i = begNumber; i <= maxYdatas; i+=offset)
			{
				gl.glVertex2f(0f, i);
				gl.glVertex2f(0.3f, i);
			}
			gl.glEnd();
			gl.glBegin(GL.GL_LINES);
			for (float i = begNumber; i <= maxYdatas; i+=offset/2)
			{
				gl.glVertex2f(0f, i);
				gl.glVertex2f(0.2f, i);
			}
			gl.glEnd();

			NumberFormat nf = NumberFormat.getNumberInstance(java.util.Locale.UK); // pour tjrs avoir des '.' et pas de ','
			DecimalFormat df = (DecimalFormat) nf;
			df.applyPattern("#.###"); // on Žcrit jusqu'ˆ 3 dŽcimales

			for(float i = begNumber ; i <= maxYdatas ; i+=offset)
			{
				gl.glRasterPos2f(TPOS, i);
				glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, ""+df.format(i));
			}
			gl.glEndList();
		}
		else
		{
			gl.glCallList(YscaleListID);
		}
		gl.glLoadIdentity();
	}
	
	public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h)
	{
		width = w;
		height = h;
		gl.glDeleteLists(YscaleListID, 1);
		YscaleListID = 0;
		gl.glDeleteLists(marginListID, 1);
		marginListID = 0;
		gl.glDeleteLists(axeListID, 1);
		axeListID = 0;
	}
	
	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged){}
	

/*	public void mouseWheelMoved(MouseWheelEvent e)
	{	
		// possible seulement dans le cas ou on a 1 et 1 seule data sŽlectionnŽe/dessinŽe.
		if (hwf==null && hsdifdtTab!=null && hsdifdtTab.length==1)
		{			
			hsdifdtTab[0].setDirty(true);
			float minY = hsdifdtTab[0].getMinY();
			float maxY = hsdifdtTab[0].getMaxY();
			
			if (e.isShiftDown()) {
				if (e.getUnitsToScroll()!=0)
					hsdifdtTab[0].setMaxY(maxY-(float)e.getUnitsToScroll()/100);
			} else
				hsdifdtTab[0].setMinY(minY-((float)e.getUnitsToScroll())/1000);
			
			rescaleYsdif = HoloSDIFdata.getRescaleYallSDIF(hsdifdtTab);
			minHsdifdt = HoloSDIFdataStat.getSmallestSDIFstartTime(hsdifdtTab);
			maxHsdifdt = HoloSDIFdataStat.getBiggestSDIFendTime(hsdifdtTab);
			glp.display();
		}
	}*/

}
