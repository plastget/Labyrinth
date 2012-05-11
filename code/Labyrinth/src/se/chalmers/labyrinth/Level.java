package se.chalmers.labyrinth;

import java.util.ArrayList;

import android.graphics.Color;

public class Level {
	private ArrayList<Wall> walls;
	private ArrayList<Hole> sinkHoles;
	private Hole finalHole;
	
	public Level() {
		sinkHoles = new ArrayList<Hole>();
		walls = new ArrayList<Wall>();
		
		initiateLevel();
	}
	
	
	private void initiateLevel() {
		// För att i framtiden kunna hämta levels ur databasen/filer

		// Mest temporärt just nu med endast en level!
        int wallColor = Color.GRAY;
        
        // Kanter
        walls.add(new Wall(20f, 400f, 220f, 420f, wallColor));
        walls.add(new Wall(220f, 100f, 240f, 420f, wallColor));
        
        // Yttre kanterna (för tillfället inställda efter Galaxy S & S2)
        walls.add(new Wall(0f, 780f, 480f, 800f, wallColor));
        walls.add(new Wall(0f, 0, 480f, 20, wallColor));
        walls.add(new Wall(460f, 0f, 480f, 800f, wallColor));
        walls.add(new Wall(0f, 0f, 20f, 800f, wallColor));
        
        //Alla sjunkhål
        sinkHoles.add(new Hole(30f,Color.BLACK,300f,150f));
        sinkHoles.add(new Hole(30f,Color.BLACK,150f,150f));
        
        // Lägg till sista hålet
		finalHole = new Hole(30f, Color.BLUE, 300f, 600f);
	}

	public ArrayList<Wall> getWalls() {
		return walls;
	}

	public void setWalls(ArrayList<Wall> walls) {
		this.walls = walls;
	}

	public ArrayList<Hole> getSinkHoles() {
		return sinkHoles;
	}

	public void setSinkHoles(ArrayList<Hole> sinkHoles) {
		this.sinkHoles = sinkHoles;
	}

	public Hole getFinalHole() {
		return finalHole;
	}

	public void setFinalHole(Hole finalHole) {
		this.finalHole = finalHole;
	}
}
