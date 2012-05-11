package se.chalmers.labyrinth;

public class Timer {
	private long lastTime;
	private long totalTime;
	private boolean paused;
	
	public Timer() {
		// Initiera en ny timer
		paused = true;
		totalTime = 0;
	}
	
	public void start() {
		// Starta tideräkningen här
		paused = false;
		lastTime = System.currentTimeMillis();
	}
	
	public long stop() {
		// Hämta senaste tiden, stäng av räknaren, returnera tiden och nollställ
		pause();
		return getTime();
	}
	
	public void pause() {
		// Uppdatera tiden och sätt den sedan till pausad
		update();
		paused = true;
	}
	
	public void resume() {
		// Sätt lastTime till nu och avsluta pausningen
		lastTime = System.currentTimeMillis();
		paused = false;
	}
	
	public void reset() {
		// Nollställ räknaren
		totalTime = 0;
	}
	
	private void update() {
		// Öka bara på den totala tiden om räknaren INTE är pausad
		if (!paused) {
			long now = System.currentTimeMillis();
			totalTime += now - lastTime;
			lastTime = now;
		}
	}
	
	public long getTime() {
		// Uppdatera och returnera tiden
		update();
		return totalTime;
	}
}
