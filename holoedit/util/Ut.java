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
package holoedit.util;

import holoedit.gui.HoloMenuBar;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.Math;
import java.lang.reflect.InvocationTargetException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class Ut {
	public static void post(String s) {
		System.out.println(s);
	}

	/**
	 * Arrondi d'un double avec n éléments après la virgule.
	 * @param a La valeur à convertir.
	 * @param n Le nombre de décimales à conserver.
	 * @return La valeur arrondi à n décimales.
	 */
	public static double floor(double a, int n) {
		double p = Math.pow(10.0, n);
		return Math.floor((a*p)+0.5) / p;
	}

	public static int min(int a, int b) {
		return a <= b ? a : b;
	}

	public static float min(float a, float b) {
		return a <= b ? a : b;
	}

	public static double min(double a, double b) {
		return a <= b ? a : b;
	}

	/** Returns the smaller of three int values. */
	public static int min(int a, int b, int c) {
		return Math.min(Math.min(a, b), c);
	}

	/** Returns the smaller of three float values. */
	public static float min(float a, float b, float c) {
		return Math.min(Math.min(a, b), c);
	}

	public static int max(int a, int b) {
		return a >= b ? a : b;
	}

	public static float max(float a, float b) {
		return a >= b ? a : b;
	}

	public static double max(double a, double b) {
		return a >= b ? a : b;
	}

	/** Returns the greater of three int values. */
	public static int max(int a, int b, int c) {
		return Math.max(Math.max(a, b), c);
	}

	/** Returns the greater of three float values. */
	public static float max(float a, float b, float c) {
		return Math.max(Math.max(a, b), c);
	}

	public static int mean(int a, int b) {
		return ((a + b) / 2);
	}

	public static float mean(float a, float b) {
		return ((a + b) / 2);
	}

	public static double mean(double a, double b) {
		return ((a + b) / 2);
	}

	public static int mod(int value, int modulo) {
		while (value >= modulo)
			value -= modulo;
		return value;
	}

	public static float mod(float value, float modulo) {
		while (value >= modulo)
			value -= modulo;
		return value;
	}

	public static double mod(double value, double modulo) {
		while (value >= modulo)
			value -= modulo;
		return value;
	}

	public static int modabs(int value, int modulo) {
		while (value < 0)
			value += modulo;
		while (value >= modulo)
			value -= modulo;
		return value;
	}

	/** Returns the greater of two int values. */
	public static int clipL(int value, int lowlim) {
		return Math.max(value, lowlim);
	}

	/** Returns the greater of two float values. */
	public static float clipL(float value, float lowlim) {
		return Math.max(value, lowlim);
	}

	/** Returns the smaller of two int values. */
	public static int clipU(int value, int uplim) {
		return Math.min(value, uplim);
	}

	public static float clipU(float value, float uplim) {
		return Math.min(value, uplim);
	}

	public static int clip(int value, int lowlim, int uplim) {
		return Math.max(Math.min(value, uplim), lowlim);
	}

	public static double clip(double value, double lowlim, double uplim) {
		return Math.max(Math.min(value, uplim), lowlim);
	}

	public static float clip(float value, float lowlim, float uplim) {
		return Math.max(Math.min(value, uplim), lowlim);
	}

	public static int modSigned(int value, int modulo) {
		int sign = value >= 0 ? 1 : -1;
		value = Math.abs(value);
		while (value >= modulo)
			value -= modulo;
		return sign * value;
	}

	public static float modSigned(float value, float modulo) {
		int sign = value >= 0 ? 1 : -1;
		value = Math.abs(value);
		while (value >= modulo)
			value -= modulo;
		return sign * value;
	}

	public static double modSigned(double value, double modulo) {
		int sign = value >= 0 ? 1 : -1;
		value = Math.abs(value);
		while (value >= modulo)
			value -= modulo;
		return sign * value;
	}

	public static double clipL(double value, double lowlim) {
		return max(value, lowlim);
	}

	public static double clipU(double value, double uplim) {
		return min(value, uplim);
	}

	public static boolean isPair(int i) {
		return mod(i, 2) == 0;
	}

	public static int interpol(int f1, int f2, double step) {
		return (int) (f1 + step * (f2 - f1));
	}

	public static float interpol(float f1, float f2, double step) {
		return (float) (f1 + step * (f2 - f1));
	}

	public static double interpol(double f1, double f2, double step) {
		return (f1 + step * (f2 - f1));
	}

	
	public static double scale(double in,double minin,double maxin,double minout,double maxout)
	{
		double scaler = (maxout - minout) / (maxin - minin );
		
		return minout + (in - minin)*scaler;
	}
	
	public static double scale(double in,double minin,double maxin)
	{
		double rangin = maxin - minin;
		
		return (in - minin)/rangin;
	}
	
	public static boolean between(int v, int l, int u) {
		return v >= l && v <= u;
	}

	public static boolean between(float v, float l, float u) {
		return v >= l && v <= u;
	}

	public static boolean between(double v, double l, double u) {
		return v >= l && v <= u;
	}

	/** date en milliemes de secondes */
	public static String intToDate(int dateNum) {
		String date;
		int heure = dateNum / 3600000;
		int minute = (dateNum / 60000) % 60;
		int seconde = (dateNum / 1000) % 60;
		int centiSeconde = dateNum % 1000;
		date = "" + heure + ":";
		if (minute < 10)
			date = date + "0";
		date = date + minute + ":";
		if (seconde < 10)
			date = date + "0";
		date = date + seconde + ":";
		if (centiSeconde < 10)
			date = date + "0";
		date = date + centiSeconde;
		return (date);
	}

	public static String msToHMSMS(float ms) {
		int h = (int) ms / 3600000;
		int m = (int) (ms / 60000) % 60;
		int s = (int) (ms / 1000) % 60;
		int mss = (int)  ms % 1000;

		String Mss = "" + mss;
		while (Mss.length() < 3)
			Mss = "0" + Mss;
		return h + ":" + m + ":" + s + "'" + Mss + "\"";
	}

	public static int readInt4(byte[] array, int offset) {
		return (Ut.readUnsignedByte(array, offset) << 24) + (Ut.readUnsignedByte(array, offset + 1) << 16)
				+ (Ut.readUnsignedByte(array, offset + 2) << 8) + Ut.readUnsignedByte(array, offset + 3);
	}

	public static int readUnsignedByte(byte[] array, int offset) {
		return array[offset] >= 0 ? array[offset] : 256 + array[offset];
	}

	public static int readInt1(byte[] array, int offset) {
		return readUnsignedByte(array, offset);
	}

	public static int readInt2(byte[] array, int offset) {
		return (readUnsignedByte(array, offset) << 8) + readUnsignedByte(array, offset + 1);
	}

	public static int readInt3(byte[] array, int offset) {
		return (readUnsignedByte(array, offset) << 16) + (readUnsignedByte(array, offset + 1) << 8)
				+ readUnsignedByte(array, offset + 2);
	}

	public static String readString(byte[] array, int offset, int charNum) {
		String tmp = "";
		for (int i = 0; i < charNum; i++)
			tmp = tmp + (char) array[offset + i];
		return tmp;
	}

	public static void alert(final String title, final String message) 
	{
		
		//JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);

		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
			}
		});
		
		
	}
	
	
	/** Permet l'affichage d'un message d'exception dans un dialog.
	 * avec un bouton details... */
	public static void showError(Exception e){
		
		String shortMessage = "Exception while loading/running the script." +
								"'\nMessage is:\n\n" +e.getMessage();
		String totalMessage = prepareStackTrace(e);
		
		JTextArea textArea = new JTextArea(totalMessage.toString());
		textArea.setEditable(false);
		textArea.setBorder(BorderFactory.createTitledBorder("Details"));
		final JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setVisible(false); // on affiche pas direct les details

		//button details
		JButton details = new JButton("Details...");
		details.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scrollPane.setVisible(!scrollPane.isVisible());
				JDialog dialog = (JDialog) ((JComponent) e.getSource()).getRootPane().getParent();
				dialog.pack();
			}
		});
		//boutton ok
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JDialog dialog =  (JDialog) ((JComponent) e.getSource()).getRootPane().getParent();
				dialog.dispose();
			}
		});
		
		//placement des composants
		JPanel buttonsPanel = new JPanel(new FlowLayout());
		buttonsPanel.add(okButton);
		buttonsPanel.add(details);

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(scrollPane, BorderLayout.CENTER);
		mainPanel.add(buttonsPanel,BorderLayout.SOUTH);

		//affichage du message
		JOptionPane.showOptionDialog(null, shortMessage, "Error", JOptionPane.OK_OPTION
				, JOptionPane.ERROR_MESSAGE, null, new Object[]{mainPanel}, okButton);
	}
	
    /** prepare a stacktrace to be shown in an output window */
    private static String prepareStackTrace(Exception e) {
        Throwable exc = e;
        StringBuffer output = new StringBuffer();
        collectTraces(exc, output);
        if (exc.getCause() != null) {
           exc = exc.getCause();
           output.append("caused by::\n");
           output.append(exc.getMessage());
           output.append("\n");
           collectTraces(exc, output);
        }
        return output.toString();
    }

    private static void collectTraces(Throwable e, StringBuffer output) {
        StackTraceElement[] trace = e.getStackTrace();
        for (int i=0; i < trace.length; i++) {
            output.append(trace[i].toString());
            output.append("\n");
        }
    }

	public static String APP_PATH;
	/** boolean permettant de determiner l'environnement
	 * cette distinction permet de compiler l'application sur n'importe quelle plate-forme
	 * tout en permettant l'execution du code compile sur n'importe quelle plate-forme. */
	public static boolean MAC;
	/** Les codes de caractere etant differents sous mac et pc, chaque fois qu'on
	 * fait appel a certains d'entre eux
	 * on vient les chercher ici.
	 * Ce caracteres sert a la gestion des arborescences de fichiers */
	public static String dirCar;

	public static String dir(String s) {
		if (s.endsWith(dirCar))
			return s;
		return s + dirCar;
	}

	static public void print(String str) {
		System.out.println(str);
	}

	/** le petit rond ∞ / ° */
	public static char numCar;
	// son inverse pour garder la compatibilite en lecture de fichier (voir
	// TextRead > HP num (n∞, pos X, pos Y ...)
	public static char numCar2;
	public static String hv;
	public static double version = 4.5;
	//	public Application fApplication;
	static public HoloMenuBar barMenu;
	public static int drawPtsNb = 10;
	// Track Number
	public final static int INIT_TRACK_NB = 8;
	public final static int OLD_TRACK_NB = 17;
	public final static int MAX_TRACK_NB = 32;

	// GUI UPDATE MASK
	public final static int DIRTY_ALL = 1;
	public final static int DIRTY_ROOM = 2;
	public final static int DIRTY_ROOM3D = 4;
	public final static int DIRTY_SCORE = 8;
	public final static int DIRTY_TIME = 16;

}
