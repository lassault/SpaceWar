package lassa.net;

import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.JFrame;

@SuppressWarnings("serial")
public class MiFrame extends JFrame {

	public MiFrame() {
		MiPanel mp = new MiPanel();
		add(mp);
		addKeyListener(mp);
		addMouseMotionListener(mp);
		setFocusable(true);
		setTitle("Space War");
		Toolkit t = Toolkit.getDefaultToolkit();
		Image icono;
		icono = t.getImage(getClass().getResource("imagenes/enemigos/enemigo05.png"));
		setIconImage(icono);
		setSize(1300,720);
		setDefaultCloseOperation(3);
		setLocationRelativeTo(null);
	}
	
}
