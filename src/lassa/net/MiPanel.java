package lassa.net;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class MiPanel extends JPanel implements KeyListener, MouseMotionListener {
	
	private MovimientoNave mvN;
	private MovimientoEstrellas mvE;
	private MovimientoDisparos mvD;
	private MovimientoEnemigo mvEn;
	private MovimientoColisiones mvC;
	private MovimientoFuego mvF;
	private GeneradorEnemigos gEn;
	private GeneradorDisparos gD;
	
	private Image[] naves = new Image[8];
	private Image[] estrellasImagenes = new Image[3];
	private Image[] naveImagenes = new Image[8];
	private Image[] imagenesEnemigos = new Image[5];
	private Image[] fuego = new Image[8];
	private Image[] fuegoEnemigo = new Image[8];
	private Image[] explosion = new Image[7];
	private Image[] disparo = new Image[3];
	private Image disparoEnemigo;
	private Image corazon;
	
	private ArrayList<Disparo> disparos = new ArrayList<Disparo>();
	private ArrayList<Disparo> disparosEnemigos = new ArrayList<Disparo>();
	private ArrayList<Estrella> estrellas = new ArrayList<Estrella>();
	private ArrayList<Enemigo> enemigos = new ArrayList<Enemigo>();
	private ArrayList<Explosion> explosiones = new ArrayList<Explosion>();
	
	private int naveX = 620;
	private int naveY = 600;
	private int vida = 100;
	private int vidas = 3;
	private int nivel = 1;
	private int progreso = 0;
	private int contador = 0;
	
	private Nave nave = new Nave(naveX, naveY, vida, vidas, nivel);
	
	private int opcion;
	private int numeroFuego;
	private int puntuacion;
	private int numeroDisparos;
	private boolean jugando;
	private boolean muerto;
	private Font titulos = new Font("Agency", Font.BOLD, 24);
	private Font texto = new Font("Agency", Font.ITALIC, 14);
	private Font puntos = new Font("Agency", Font.BOLD, 70);
	
	public boolean getMuerto() {
		return muerto;
	}
	
	public void setMuerto (boolean muerto) {
		this.muerto = muerto;
	}
	
	public boolean getJugando() {
		return jugando;
	}
	
	public void setJugando (boolean jugando) {
		this.jugando = jugando;
	}
	
	public int getPuntuacion() {
		return puntuacion;
	}

	public void setPuntuacion (int puntuacion) {
		this.puntuacion = puntuacion;
	}

	public int getNumeroFuego() {
		return numeroFuego;
	}

	public void setNumeroFuego (int numeroFuego) {
		this.numeroFuego = numeroFuego;
	}

	public int getOpcion() {
		return opcion;
	}

	public void setOpcion (int opcion) {
		this.opcion = opcion;
	}
	
	public MiPanel() {
		setBackground(Color.BLACK);
		
		setMuerto(false);
		
		setNumeroFuego(1);
		
		setPuntuacion(0);
		
		setJugando(false);
		
		numeroDisparos = 1;
		
		Toolkit t = Toolkit.getDefaultToolkit();
		
		getImagenes(t);
		
		setEstrellas();
		
		mvE = new MovimientoEstrellas (this, estrellas);
		mvE.start();
	}
	
	@Override
	public void paint (Graphics g) {
		super.paint(g);
		
		for (int i = 0; i < estrellas.size(); i++) {
			g.drawImage(estrellasImagenes[estrellas.get(i).getTipo() - 1], estrellas.get(i).getX(), estrellas.get(i).getY(), this);
		}
		
		if (!getJugando() && !getMuerto()) {
			printMenu(g);
		}
		
		if (getJugando()) {
			printJuego(g);
		}
		
		if (!getJugando() && getMuerto()) {
			printPuntuacion(g);
		}
	}
	
	public void printPuntuacion (Graphics g) {
		
		g.setColor(Color.WHITE);
		g.setFont(puntos);
		g.drawString("Game Over", 425, 300);
		g.drawString("Score: ", 425, 400);
		g.drawString(String.valueOf(getPuntuacion()), 675, 400);
		
		if (contador < 25) {
			g.drawImage(explosion[progreso], nave.getX(), nave.getY(), this);
			progreso++;
			if (progreso == 7) {
				progreso = 0;
			}
			contador++;
		}
	}
	
	public void printJuego (Graphics g) {
		
		for (int i = disparos.size() - 1; i > -1; i--) {
			g.drawImage(disparo[numeroDisparos - 1], disparos.get(i).getX(), disparos.get(i).getY(), this);
		}
		
		for (int i = disparosEnemigos.size() - 1; i > -1; i--) {
			g.drawImage(disparoEnemigo, disparosEnemigos.get(i).getX(), disparosEnemigos.get(i).getY(), this);
		}
		
		for (int i = enemigos.size() - 1; i > -1; i--) {
			g.drawImage(imagenesEnemigos[enemigos.get(i).getNivel()-1], enemigos.get(i).getX(), enemigos.get(i).getY(), this);
			g.drawImage(fuegoEnemigo[getNumeroFuego() - 1], enemigos.get(i).getX() + 15, enemigos.get(i).getY() - 30, this);
		}
		
		for (int i = explosiones.size() - 1; i > -1; i--) {
			g.drawImage(explosion[explosiones.get(i).getProgreso() - 1], explosiones.get(i).getX(), explosiones.get(i).getY(), this);
		}
		
		g.drawImage(naveImagenes[nave.getNivel()-1], nave.getX(), nave.getY(), this);
		g.drawImage(fuego[getNumeroFuego() - 1], nave.getX() + 15, nave.getY() + 50, this);
		
		g.setColor(Color.GREEN);
		g.setFont(texto);
		
		g.drawRect(1215, 75, 25, 500);
		g.fillRect(1215, 500 + 75 - nave.getVida()*5, 25, nave.getVida()*5);
		
		g.drawString("Vida: " + String.valueOf(nave.getVida()), 1200, 65);
		g.drawString("Nivel: " + String.valueOf(nave.getNivel()), 1205, 600);
		g.drawString("Vidas: " + String.valueOf(nave.getVidas()), 1200, 625);
		g.drawString("Score: " + String.valueOf(getPuntuacion()), 1200, 650);
		
		for (int i = 0; i < nave.getVidas(); i++) {
			g.drawImage(corazon, 1185 + 30*i, 20, this);
		}
		
	}
	
	public void printMenu (Graphics g) {
		int fila = 0;
		int columna = 0;
		
		g.setColor(Color.WHITE);
		
		g.setFont(titulos);
		g.drawString("Elige tu nave", 550, 75);
		
		g.setFont(texto);
		
		for (int i = 0; i < 8; i++) {
			g.drawString("Nave " + (i + 1), 165 + (columna*300), 180 + (fila*300));
			g.drawImage(naves[i], 150 + (columna*300), 200 + (fila*300), this);
			columna++;
			if (i == 3) {
				fila = 1;
				columna = 0;
			}
		}
	}
	
	public void setHilos() {
		mvN = new MovimientoNave (this, nave);
		mvN.start();
		
		mvD = new MovimientoDisparos (this, disparos, disparosEnemigos);
		mvD.start();
		
		mvEn = new MovimientoEnemigo (this, enemigos);
		mvEn.start();
		
		mvC = new MovimientoColisiones (this, nave, enemigos, disparos, disparosEnemigos, explosiones);
		mvC.start();
		
		mvF = new MovimientoFuego (this, explosiones);
		mvF.start();
		
		gEn = new GeneradorEnemigos (this, enemigos);
		gEn.start();
		
		gD = new GeneradorDisparos (this, enemigos, disparosEnemigos);
		gD.start();
	}
	
	public void setEnemigos() {
		for (int i = 0; i < 5; i++) {
			
			int nivel = (int) (Math.random()*5) + 1;
			int enemigoX = (int) (Math.random()*1000) + 100;
			int enemigoY = 0;
			int vida = (6 - nivel)*100;
			
			Enemigo enemigo = new Enemigo(enemigoX, enemigoY, vida, nivel);
			
			enemigos.add(enemigo);
		}
	}
	
	public void setEstrellas() {
		for (int i = 0; i < 500; i++) {
			int tipo = (int) (Math.random()*3) + 1;
			int estrellaX = (int) (Math.random()*1300);
			int estrellaY = (int) (Math.random()*720);
			Estrella estrella = new Estrella(estrellaX, estrellaY, tipo);
			estrellas.add(estrella);
		}
	}
	
	public void getImagenes (Toolkit t) {
		
		for (int i = 0; i < 3; i++) {
			disparo[i] = t.getImage(getClass().getResource("imagenes/disparos/disparo" + (i + 1) + ".png"));
		}
		
		disparoEnemigo = t.getImage(getClass().getResource("imagenes/disparos/disparo4.png"));
		
		for (int i = 0; i < 3; i++) {
			estrellasImagenes[i] = t.getImage(getClass().getResource("imagenes/estrellas/estrella" + (i + 1) + ".png"));
		}
		
		for (int i = 0; i < 5; i++) {
			imagenesEnemigos[i] = t.getImage(getClass().getResource("imagenes/enemigos/enemigo0" + (i + 1) + ".png"));
			imagenesEnemigos[i] = imagenesEnemigos[i].getScaledInstance(60, 60, Image.SCALE_DEFAULT);
		}
		
		for (int i = 0; i < 8; i++) {
			naves[i] = t.getImage(getClass().getResource("imagenes/nave " + (i + 1) + "/nave" + (i + 1) + "-1.png"));
			naves[i] = naves[i].getScaledInstance(80, 80, Image.SCALE_DEFAULT);
			fuego[i] = t.getImage(getClass().getResource("imagenes/fuegotrasero/fuego" + (i + 1) + ".png"));
			fuegoEnemigo[i] = t.getImage(getClass().getResource("imagenes/fuegotrasero/fuegoenemigo" + (i + 1) + ".png"));
		}
		
		for (int i = 0; i < 7; i++) {
			explosion[i] = t.getImage(getClass().getResource("imagenes/explosion/explosion-0" + (i + 1) + ".png"));
		}
		
		corazon = t.getImage(getClass().getResource("imagenes/vidas/vida.png"));
		corazon = corazon.getScaledInstance(25, 25, Image.SCALE_DEFAULT);	
	}
	
	public void getNave() {
		// TODO Auto-generated method stub
		Toolkit t = Toolkit.getDefaultToolkit();
		for (int i = 0; i < 8; i++) {
			naveImagenes[i] = t.getImage(getClass().getResource("imagenes/nave " + getOpcion() + "/nave" + getOpcion() + "-" + (i + 1) + ".png"));
			naveImagenes[i] = naveImagenes[i].getScaledInstance(60, 60, Image.SCALE_DEFAULT);
		}
	}

	@Override
	public void keyPressed(KeyEvent event) {
		// TODO Auto-generated method stub
		if (!getJugando() && !getMuerto()) {
			switch (event.getKeyCode()) {
			case 49:  setOpcion(1);
				   	  break;
			case 50:  setOpcion(2);
				      break;
			case 51:  setOpcion(3);
				      break;
			case 52:  setOpcion(4);
				      break;
			case 53:  setOpcion(5);
				      break;
			case 54:  setOpcion(6);
				      break;
			case 55:  setOpcion(7);
				      break;
			case 56:  setOpcion(8);
				      break;
			case 97:  setOpcion(1);
		 		 	  break;
			case 98:  setOpcion(2);
		 		 	  break;
			case 99:  setOpcion(3);
		 		 	  break;
			case 100: setOpcion(4);
		 		  	  break;
			case 101: setOpcion(5);
		 		  	  break;
			case 102: setOpcion(6);
		 		  	  break;
			case 103: setOpcion(7);
		 		      break;
			case 104: setOpcion(8);
		 		      break;
			}
			
			getNave();
			
			setJugando(true);
			
			setHilos();
			
			setEnemigos();
		}
		
		if (event.getKeyCode() == 32 && getJugando()) {
			
			if (nave.getNivel() > 3 && nave.getNivel() < 7) {
				numeroDisparos = 2;
			}
			
			if (nave.getNivel() == 8) {
				numeroDisparos = 3;
			}
			
			switch (numeroDisparos) {
			case 2:		for (int i = 0; i < numeroDisparos; i++) {
							int disparoX = nave.getX() + 15 + 30*i - 13;
							int disparoY = nave.getY() - 40;
							int disparoTipo = 0;
							Disparo disparo = new Disparo(disparoX, disparoY, disparoTipo);
							disparos.add(disparo);
						}				
						break;
						
			case 3:		for (int i = 0; i < numeroDisparos; i++) {
							int disparoX = nave.getX() + 30*i - 13;
							int disparoY = nave.getY() - 40;
							int disparoTipo = 0;
							Disparo disparo = new Disparo(disparoX, disparoY, disparoTipo);
							disparos.add(disparo);
						}
						break;
				
			default:	for (int i = 0; i < numeroDisparos; i++) {
							int disparoX = nave.getX() + 17;
							int disparoY = nave.getY() - 40;
							int disparoTipo = 0;
							Disparo disparo = new Disparo(disparoX, disparoY, disparoTipo);
							disparos.add(disparo);
						}
						break;
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent event) {
		// TODO Auto-generated method stub
	}

	@Override
	public void keyTyped(KeyEvent event) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mouseDragged(MouseEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent event) {
		// TODO Auto-generated method stub
		
		if (getJugando()) {
			nave.setX(event.getX() - 30);
			nave.setY(event.getY() - 60);
		}
	}
}
