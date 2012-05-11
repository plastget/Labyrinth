package se.chalmers.labyrinth;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;

public class Game extends Activity {
	/*
	 * Vissa delar av den här koden är tagna från Android developers:
	 * http://developer.android.com/resources/samples/AccelerometerPlay/
	 * src/com/example/android/accelerometerplay/AccelerometerPlayActivity.html
	 * 
	 * Dessa är markerade med en kommentar innan.
	 * 
	 */
	private GameView gameView;
    private SensorManager sensorManager;
    private WindowManager windowManager;
    private Display display;
    private Sensor accelerometer;
    private PowerManager powerManager;
    private WakeLock wakeLock;
    
    private boolean gamePaused;
    
    // För tidräkning
    private Timer timer;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // För att använda sensorerna (accelerometern i vårt fall)
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        display = windowManager.getDefaultDisplay();
        
        // För att hålla skärmen igång konstant (Tagen från Android developers)
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, getClass().getName());
        
        gameView = new GameView(this);
        setContentView(gameView);
    }
    
    
    // Den här funktionen är tagen från Android developers
    // och modifierad för att fungera med våran applikation.
    @Override
    protected void onResume() {
    	super.onResume();
        /*
         * when the activity is resumed, we acquire a wake-lock so that the
         * screen stays on, since the user will likely not be fiddling with the
         * screen or buttons.
         */
    	wakeLock.acquire();
    	// Start the simulation
    	gameView.startGame();
    }
    
    // Den här funktionen är tagen från Android developers
    // och modifierad för att fungera med våran applikation.
    @Override
    protected void onPause() {
    	super.onPause();
        /*
         * When the activity is paused, we make sure to stop the simulation,
         * release our sensor resources and wake locks
         */

        // Stop the simulation
    	gameView.stopGame();
    	
    	// and release our wake-lock
    	wakeLock.release();
    }
    
    
    class GameView extends View implements SensorEventListener {
        private Ball ball;
        private float sensorX;
        private float sensorY;
        private Level currentLevel;
        
        // För FPS-beräkning
        private long fpsBeforeTime;
        private int fpsFrameCount;
        private int currentFPS;

        public GameView(Context context) {
            super(context);
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            timer = new Timer();
            
            // Skapa ett nytt spel
            newGame();
        }
        
        // Den här funktionen är tagen från Android developers
        // och modifierad för att fungera med våran applikation.
        public void startGame() {
        	sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
        
        //Den här funktionen är rakt tagen från Android developers.
        public void stopGame() {
        	sensorManager.unregisterListener(this);
        	Log.d("SENSOR_MANAGER", "Sensor shall now be turned off");	// Vår kod, till för debugging.
        }
        
        // Skapar ett nytt spel, har den i en egen funktion för att
        // möjliggöra "Retry"-knappen.
        private void newGame() {
        	// Initiera banan
        	currentLevel = new Level();
        	
            // Initiera bollen
            ball = new Ball(60f, 60f, 20f, Color.RED);
            
            // Sätt spelet i ej pausat läge
            gamePaused = false;
            
            // Starta tidräkning
            timer.start();
        }
       
        
        //Den här funktionen är rakt tagen från Android developers.
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
                return;
            /*
             * record the accelerometer data, the event's timestamp as well as
             * the current time. The latter is needed so we can calculate the
             * "present" time during rendering. In this application, we need to
             * take into account how the screen is rotated with respect to the
             * sensors (which always return data in a coordinate space aligned
             * to with the screen in its native orientation).
             */

            switch (display.getOrientation()) {
                case Surface.ROTATION_0:
                    sensorX = event.values[0];
                    sensorY = event.values[1];
                    break;
                case Surface.ROTATION_90:
                    sensorX = -event.values[1];
                    sensorY = event.values[0];
                    break;
                case Surface.ROTATION_180:
                    sensorX = -event.values[0];
                    sensorY = -event.values[1];
                    break;
                case Surface.ROTATION_270:
                    sensorX = event.values[1];
                    sensorY = -event.values[0];
                    break;
            }
        }
        
        private void showFinalMenu() {
        	// Pausa spelet och stäng av timern
        	gamePaused = true;
        	long finnishTime = timer.stop();
        	
        	// Formatera tiden efter MM:ss:mmm
            String finnishTimeString = String.format("%02d:%02d:%03d", 
            		((int) ((finnishTime / 1000) / 60)),
            		((int) ((finnishTime / 1000) % 60)),
            		((int) (finnishTime % 1000)));
            
        	// Bygg upp slutmenyn som en dialog
        	AlertDialog.Builder dialog = new AlertDialog.Builder(Game.this);
        	dialog.setTitle("Game finished!");
        	dialog.setMessage("Congratulations!\nYou finnished map on " + finnishTimeString + "!");
        	dialog.setNeutralButton("Retry", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					// Initiera ett nytt spel och nollställ timern
					newGame();
					timer.reset();
				}
			});
        	dialog.setNegativeButton("Main Menu", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					// Gå till "Main Menu" och avsluta spelet
					Intent mainMenu = new Intent(Game.this, MainMenu.class);
					startActivity(mainMenu);
					finish();
				}
			});
        	dialog.show();
        }
        
        private void checkCollisions(float sensX, float sensY) {
            
        	final float ballPosX = ball.getPosX();
        	final float ballPosY = ball.getPosY();
            
        	final float ballOffsetTop = ball.getOffsetTop();
        	final float ballOffsetDown = ball.getOffsetDown();
        	final float ballOffsetLeft = ball.getOffsetLeft();
        	final float ballOffsetRight = ball.getOffsetRight();
            
        	// Vad bollen slutligen ska uppdateras med
            float updPosX = sensX;
            float updPosY = sensY;
            
            //Beräkna för varje hål dess position samt om bollen ramlar ned
            for(Hole hole : currentLevel.getSinkHoles()){
                //Kollar ifall bollen ramlar ned i sjunkhålet genom att matcha
                //bollens koordinater med hålens offset.
                //Om bollen är till hälften i hålet eller mer räknas den som inne.
                if (ballPosY <= hole.getOffsetRight() && ballPosY >= hole.getOffsetLeft()) {
                	if (ballPosX <= hole.getOffsetTop() && ballPosX >= hole.getOffsetDown()) {
                		//Om bollen ramlar ned; flytta bollen
                		ball.setPosX(400);
                		ball.setPosY(400);
                	}
                }
            }
            
            //Hämta ut finalHole
            final Hole finalHole = currentLevel.getFinalHole();
            
            //Kollar ifall bollen ramlar ned i finalHole
            if (ballPosY <= finalHole.getOffsetRight() && ballPosY >= finalHole.getOffsetLeft()) {
            	if (ballPosX <= finalHole.getOffsetTop() && ballPosX >= finalHole.getOffsetDown()) {
            		// Visa finalMenu vid "vinst"
            		showFinalMenu();
            	}
            }
            
            // Kolla kollisioner mot alla väggar
            for (Wall wall : currentLevel.getWalls()) {
                float wallPosX1 = wall.getPosX1();
                float wallPosY1 = wall.getPosY1();
                float wallPosX2 = wall.getPosX2();
                float wallPosY2 = wall.getPosY2();
                
                // Kollar X-delen av väggen
                if (ballPosY >= wallPosY1 && ballPosY <= wallPosY2) {
                    // Om den träffar mot undersidan av bollen
                    if (ballOffsetDown > wallPosX1 && ballOffsetDown < wallPosX2) {
                        // Kolla om sensorn är åt motsatt håll jämfört med bollen.
                        // För att kunna "släppa den" från väggen.
                        if (sensX < 0) {
                            updPosX = sensX;
                        } else {
                            updPosX = 0;
                        }
                    // Om den träffar mot ovansidan av bollen
                    } else if (ballOffsetTop > wallPosX1 && ballOffsetTop < wallPosX2) {
                        if (sensX > 0) {
                            // Kolla om sensorn är åt motsatt håll jämfört med bollen.
                            // För att kunna "släppa den" från väggen.
                            updPosX = sensX;
                        } else {
                            updPosX = 0;
                        }
                    }
                }
                
                // Kollar Y-delen av väggen
                if (ballPosX > wallPosX1 && ballPosX < wallPosX2) {
                    // Om den träffar mot vänstersidan av bollen
                    if (ballOffsetLeft >= wallPosY1 && ballOffsetLeft <= wallPosY2) {
                        if (sensY > 0) {
                            // Kolla om sensorn är åt motsatt håll jämfört med bollen.
                            // För att kunna "släppa den" från väggen.
                            updPosY = sensY;
                        } else {
                            updPosY = 0;
                        }
                    // Om den träffar mot högersidan av bollen
                    } else if (ballOffsetRight >= wallPosY1 && ballOffsetRight <= wallPosY2) {
                        if (sensY < 0) {
                            // Kolla om sensorn är åt motsatt håll jämfört med bollen.
                            // För att kunna "släppa den" från väggen.
                            updPosY = sensY;
                        } else {
                            updPosY = 0;
                        }
                    }
                }
            }
            
            ball.updatePosition(updPosX, updPosY);
        }
        
        @Override
        protected void onDraw(Canvas canvas) {
            final float sensX = sensorX;
            final float sensY = sensorY;
            
            // För att enbart använda AntiAlias på objekt som behöver det (runda)
            Paint paintAA = new Paint();
            Paint paintNoAA = new Paint();
            
            // Inställningar för uppritandet
            paintAA.setAntiAlias(true);
            paintAA.setStyle(Paint.Style.FILL);
            paintNoAA.setStyle(Paint.Style.FILL);
            
            if (!gamePaused) {
            	// Kolla kollisioner och uppdatera bollens position
            	checkCollisions(sensX, sensY);
            }
            
            // Sätt bakgrunden
            paintNoAA.setColor(Color.DKGRAY);
            canvas.drawPaint(paintNoAA);
            
            // Rita upp alla väggar
            for(Wall wall : currentLevel.getWalls()) {
            	paintNoAA.setColor(wall.getColor());
                canvas.drawRect(wall.getPosX1(), wall.getPosY1(), wall.getPosX2(), wall.getPosY2(), paintNoAA);
            }

            //Rita upp hålen
            for(Hole hole : currentLevel.getSinkHoles()){
            	paintAA.setColor(hole.getColor());
            	canvas.drawCircle(hole.getPosX(), hole.getPosY(), hole.getRadius(), paintAA);
            }
            
            //Rita ut finalHole
            paintAA.setColor(currentLevel.getFinalHole().getColor());
            canvas.drawCircle(currentLevel.getFinalHole().getPosX(), currentLevel.getFinalHole().getPosY(),
            		currentLevel.getFinalHole().getRadius(), paintAA);
            
            // Hämta värdena för bollen
            final float posX = ball.getPosX();
            final float posY = ball.getPosY();
            final float radius = ball.getRadius();
            final int color = ball.getColor();
            
            // Rita upp bollen
            paintAA.setColor(color);
            canvas.drawCircle(posX, posY, radius, paintAA);
            
            // Uppdatera FPS-mätaren
            fpsFrameCount++;
            long fpsNowTime = System.currentTimeMillis();
            long fpsDiff = fpsNowTime - fpsBeforeTime;
            
            // Uppdatera den max 1 gång per sekund
            if (fpsDiff > 1000) {
            	fpsBeforeTime = fpsNowTime;
            	currentFPS = (int) (fpsFrameCount / (fpsDiff/1000));
            	fpsFrameCount = 0;
            }
            
            // Visa hur lång tid det gått sen start
            long playTime = timer.getTime();
            
            // Formatera tiden efter MM:ss:mmm
            String timeElapsed = String.format("%02d:%02d:%03d", 
            		((int) ((playTime / 1000) / 60)),
            		((int) ((playTime / 1000) % 60)),
            		((int) (playTime % 1000)));
            
            // Rita ut hur lång tid det gått
            paintAA.setColor(Color.GREEN);
            paintAA.setShadowLayer(1, 4, 4, Color.BLACK);
            paintAA.setTextSize(25);
        	// Rotera för att få texten rätt
            canvas.rotate(90, 10, 120);
            canvas.drawText("TIME: " + timeElapsed, 10f, 120f, paintAA);
            // Återställ rotationen
            canvas.rotate(270, 10, 120);
            
            // Rita ut FPS:en
            paintAA.setColor(Color.YELLOW);
            paintAA.setShadowLayer(1, 4, 4, Color.BLACK);
            paintAA.setTextSize(25);
            canvas.rotate(90, 10, 5);
            canvas.drawText("FPS: " + Integer.toString(currentFPS), 10f, 5f, paintAA);
            
            // Rita om allt igen
	        invalidate();
        	
        }
        
        public void onAccuracyChanged(Sensor arg0, int arg1) {}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater menuInflater = getMenuInflater();
    	menuInflater.inflate(R.menu.ingamemenu, menu);
    	return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	// Pausa uppritningen av spelet och timern
    	gamePaused = true;
    	timer.pause();
    	return true;
    }
    
    @Override
    public void onOptionsMenuClosed(Menu menu) {
    	// Stäng av pausningen och starta timern
    	gamePaused = false;
    	timer.resume();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    		case R.id.inGameMenuResume:
    			// Återuppta uppritningen av spelet igen och starta räknaren
    			gamePaused = false;
    			timer.resume();
    		break;
    		case R.id.inGameMenuExit:
    			// Gå till main menu
    			Intent mainMenu = new Intent(Game.this, MainMenu.class);
    			startActivity(mainMenu);
    			finish();
    		break;
    	}
    	
    	return super.onOptionsItemSelected(item);
    }
}
