package deneme;

import java.util.ArrayList;
import java.util.Random;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.Music;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;
import org.newdawn.slick.geom.Vector2f;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

public class GameMaster extends BasicGameState{
	Player player;
	ArrayList<Bullet> bulletList;
	ArrayList<Enemy> enemyList;
	Image background;
	
	private Sound pew;
	private Sound boom;
	private Sound playerBoom;
	
	//*************************ST**************************
	int score=0;
	boolean isFinalSpawn = false;
	
	IconXAmount keyXAmount;
	IconXAmount FreshmenChestXAmount;
	IconXAmount SophomoreChestXAmount;
	IconXAmount JuniorChestXAmount;
	IconXAmount SeniorChestXAmount;
	IconXAmount coinXAmount;

	//ArrayList<MultiBar> multiBarList;
	ArrayList<Bonus> bonusList;
	private boolean paused;
	private boolean soundOn;
	
	final float KEY_SPAWN_RATE = 20f;
	final float COIN_SPAWN_RATE = 25f;
	final float FRESHMEN_CHEST_SPAWN_RATE = 10f;
	final float SOPHOMORE_CHEST_SPAWN_RATE = 7.5f;
	final float JUNIOR_CHEST_SPAWN_RATE = 5f;
	final float SENIOR_CHEST_SPAWN_RATE = 2.5f;
	final float BONUS_SPAWN = 2000f;
	
	
	final float QUIZ_SPAWN_RATE = 30f;
	final float LAB_SPAWN_RATE = 20f;
	final float ASSIGNMENT_SPAWN_RATE = 10f;
	final float MIDTERM_SPAWN_RATE = 7.5f;
	final float ENEMY_SPAWN = 2000f;
	
	private long nextTimeToSpawnBonus = 0;
	private long nextTimeToSpawnEnemy = 0;
	
	//*************************ST**************************
	
	public static Color BACKGROUND = Color.gray;
	
	private static GameMaster gm = null;
	
	private GameMaster()
	{
		super();
	}
	
	public static GameMaster getGameMaster()
	{
		if(gm==null)
		{
			gm = new GameMaster();
		}
		return gm;
	}
	
	public void handleCollisions()
	{
		handleBulletCollisions();
		if(player!=null){
			handleEnemyPlayerCollisions();
			//*************************ST**************************
			handleBonusCollisions();
			//*************************ST**************************
		}
		handleEnemyEnemyCollisions();
	}
	
	//To avoid the enemies to go through each other.
	private void handleEnemyEnemyCollisions() 
	{
		int j = 0;
		while(j<enemyList.size())
		{
			for (int i=j+1; i< enemyList.size(); i++)
			{
				if(enemyList.get(i).collides(enemyList.get(j)))
				{
					enemyList.get(i).bounceOff(enemyList.get(j), 
							enemyList.get(i).getDimentions().x/10);
					//enemyList.get(i).setStay(true);
				}
				/*else
					enemyList.get(i).setStay(false);*/
			}
			j++;
		}
	}

	private void handleEnemyPlayerCollisions() 
	{
		for (int i = 0; i< enemyList.size(); i++)
		{
			if(player.collides(enemyList.get(i)))
			{
				player.takeDamage(enemyList.get(i).getStats().getBodyDamage());
				enemyList.get(i).takeDamage(player.getStats().getBodyDamage());
				
				player.bounceOff(enemyList.get(i), 30f);
			}
		}
		handleRemovals((ArrayList)enemyList);
	}

	private void handleBulletCollisions() 
	{
		for (int i = 0; i< bulletList.size(); i++)
		{
			Bullet curBullet = bulletList.get(i);
			for (int j = 0; j< enemyList.size(); j++)
			{
				if(curBullet.collides(enemyList.get(j)) && !curBullet.isEnemyBullet())
				{
					enemyList.get(j).takeDamage(curBullet.getDamage());
					curBullet.setToBeRemoved(true);
					//*************************ST**************************
					if(enemyList.get(j).isToBeRemoved()){
						score = (int) (score + enemyList.get(j).enemyStats.getMaxHealth());
						boom.play();
					}
					//*************************ST**************************
				}
			}
			
			if (player != null)
			{
				if (curBullet.collides(player) && curBullet.isEnemyBullet())
				{
					player.takeDamage(curBullet.getDamage());
					curBullet.setToBeRemoved(true);
				}
			}
		}
		handleRemovals((ArrayList)bulletList);
		handleRemovals((ArrayList)enemyList);
	}
	
	//*************************ST**************************
	private void handleBonusCollisions()
	{
		for(int i=0; i<bonusList.size(); i++){
			if(player.collides(bonusList.get(i)))
			{
				//player.takeKey();
				bonusList.get(i).setToBeRemoved(true);
				if(bonusList.get(i) instanceof Key)
					keyXAmount.amount++;	
				if(bonusList.get(i) instanceof Coin)
					coinXAmount.amount++;
				if(bonusList.get(i) instanceof FreshmenChest)
					FreshmenChestXAmount.amount++;
				if(bonusList.get(i) instanceof SophomoreChest)
					SophomoreChestXAmount.amount++;
				if(bonusList.get(i) instanceof JuniorChest)
					JuniorChestXAmount.amount++;
				if(bonusList.get(i) instanceof SeniorChest)
					SeniorChestXAmount.amount++;
			}
		}
		//System.out.println(myKey.isToBeRemoved());
		handleRemovals((ArrayList)bonusList);
	}
	//*************************ST**************************
	
	private void handleRemovals(ArrayList<GameObject> list) {
		int size = list.size();
		for (int i = 0; i< list.size(); i++)
		{
			if(list.get(i).isToBeRemoved())
			{
				list.remove(i);
				size--;
				//System.out.println(list);
			}
		}
	}

	private void manageInput(GameContainer container) {
		
		Input input = container.getInput();
		
		//up
		if(input.isKeyDown(Input.KEY_W)){
			player.setUp(true);
		}
		else{
			player.setUp(false);
		}
	  
		//down
		if(input.isKeyDown(Input.KEY_S)){
			player.setDown(true);
		}
		else{
			player.setDown(false);
		}
	  
		//left
		if(input.isKeyDown(Input.KEY_A)){
			player.setLeft(true);
		}
		else{
			player.setLeft(false);
		}
	  
		//right
		if(input.isKeyDown(Input.KEY_D)){
			player.setRight(true);
		}
		else{
			player.setRight(false);
		}
	  
		//mouse
		if (input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON))
		{
			Bullet bullet = player.shoot(new Vector2f(input.getMouseX(), input.getMouseY()));
			if (bullet != null){
				bulletList.add(bullet);
				pew.play();
			}
		}	
	  
		if (input.isMousePressed(Input.MOUSE_RIGHT_BUTTON))
		{
			player.setPowerUpActive(!player.isPowerUpActive());	  
		}
		if (input.isKeyDown(Input.KEY_ESCAPE))
		{
			paused = true;
		}
		
	}

	//*************************ST**************************
		
	private void spawnBonus(){
		
		float rnd = (float) (Math.random()*100f);
		
		float velX = (float) (Math.random()*1820f);
		float velY = (float) (Math.random()*1040f+40f);
	
		Key k;
		Coin c;
		FreshmenChest f;
		SophomoreChest sop;
		JuniorChest j;
		SeniorChest sen;
		
		if(rnd>=(100-SENIOR_CHEST_SPAWN_RATE)){
			try {
				sen = new SeniorChest(new Vector2f(velX, velY), new Image("res/Chest/SeniorChest.png"), 800, 800);
				bonusList.add(sen); 
			} catch (SlickException e) {
				e.printStackTrace();
			}
		}
		
		else if(rnd>=(100-SENIOR_CHEST_SPAWN_RATE-JUNIOR_CHEST_SPAWN_RATE)){
			try {
				j = new JuniorChest(new Vector2f(velX, velY), new Image("res/Chest/JuniorChest.png"), 800, 800);
				bonusList.add(j); 
			} catch (SlickException e) {
				e.printStackTrace();
			}
		}
		
		else if(rnd>=(100-SENIOR_CHEST_SPAWN_RATE-JUNIOR_CHEST_SPAWN_RATE-SOPHOMORE_CHEST_SPAWN_RATE)){
			try {
				sop = new SophomoreChest(new Vector2f(velX, velY), new Image("res/Chest/SophomoreChest.png"), 800, 800);
				bonusList.add(sop); 
			} catch (SlickException e) {
				e.printStackTrace();
			}
		}
		else if(rnd>=(100-SENIOR_CHEST_SPAWN_RATE-JUNIOR_CHEST_SPAWN_RATE-SOPHOMORE_CHEST_SPAWN_RATE-FRESHMEN_CHEST_SPAWN_RATE)){
			try {
				f = new FreshmenChest(new Vector2f(velX, velY), new Image("res/Chest/FreshmenChest.png"), 800, 800);
				bonusList.add(f); 
			} catch (SlickException e) {
				e.printStackTrace();
			}
		}
		else if(rnd>=(100-SENIOR_CHEST_SPAWN_RATE-JUNIOR_CHEST_SPAWN_RATE-SOPHOMORE_CHEST_SPAWN_RATE-FRESHMEN_CHEST_SPAWN_RATE-COIN_SPAWN_RATE)){
			try {
				c = new Coin(new Vector2f(velX, velY), new Image("res/coin.png"), 800, 800);
				bonusList.add(c); 
			} catch (SlickException e) {
				e.printStackTrace();
			}
		}
		else if(rnd>=(100-SENIOR_CHEST_SPAWN_RATE-JUNIOR_CHEST_SPAWN_RATE-SOPHOMORE_CHEST_SPAWN_RATE-FRESHMEN_CHEST_SPAWN_RATE-COIN_SPAWN_RATE-KEY_SPAWN_RATE)){
			try {
				k = new Key(new Vector2f(velX, velY), new Image("res/key.png"), 800, 800);
				bonusList.add(k); 
			} catch (SlickException e) {
				e.printStackTrace();
			}	
		}
	}
	
private void spawnEnemy(){
		
		float rnd = (float) (Math.random()*100f);
		
		float velX = (float) (Math.random()*1820f);
		float velY = (float) (Math.random()*1040f+40f);
	
		Bug b;
		Quiz q;
		Lab l;
		Assignment a;
		Midterm m;
		
		if(rnd>=(95)){
			m =  new Midterm(new Vector2f(velX, velY), 150f, player, bulletList);
			enemyList.add(m);
		}
		
		else if(rnd>=(85)){
			a = new Assignment(new Vector2f(velX, velY), 1.6f,
					100f, player);
			enemyList.add(a);
		}
		
		else if(rnd>=(25)){
			l = new Lab(new Vector2f(velX, velY), 
					100f, player, enemyList);
			enemyList.add(l);
		}
		else if(rnd>=(5)){
			q = new Quiz(new Vector2f(velX, velY), 
					100f, player, bulletList);
			enemyList.add(q);
		}
		else if(rnd>=(5)){
			b = new Bug(new Vector2f(velX, velY), 
					100f, player);
			enemyList.add(b);
		}
	}

	//*************************ST**************************
	@Override
	public void init(GameContainer container,  StateBasedGame game) throws SlickException 
	{
		pew = new Sound("res/Sound/pew.wav");
		boom = new Sound("res/Sound/kaboom.wav");
		playerBoom = new Sound("res/Sound/playerboom.wav");
		paused=false;
		
		container.setShowFPS(false); 
		
		background = new Image("res/Background.png");
		background = background.getScaledCopy(1.5f);
		
		player = Player.getPlayer();
		player.getStats().setCurHealth(player.getStats().getMaxHealth());
		player.getStats().setDead(false);
		player.setPosition(new Vector2f((float)container.getScreenWidth()/2, (float)container.getScreenHeight()/2));
		
		bulletList = new ArrayList<Bullet>();
		enemyList = new ArrayList<Enemy>();
		//*************************ST**************************
		bonusList = new ArrayList<Bonus>();
				
		/*Enemy enemy1 = new Bug(new Vector2f(300f, 300f), 50f, 3f, player);
		enemyList.add(enemy1);*/
/*		
		Enemy enemy2 = new Quiz(new Vector2f(900f, 300f), 100f, player, bulletList);
		enemyList.add(enemy2);
		multiBarList.add(new MultiBar(enemy2, true));
		//System.out.println(enemyList==null);
		
		Enemy enemy3 = new Lab(new Vector2f(400f, 500f), 
				100f, player, enemyList);
		enemyList.add(enemy3);
		multiBarList.add(new MultiBar(enemy3, true));
	
		Enemy enemy4 = new Assignment(new Vector2f(400f, 500f), 1.6f,
				100f, player);
		enemyList.add(enemy4);
		multiBarList.add(new MultiBar(enemy4, true));
		
		Enemy enemy5 = new Midterm(new Vector2f(1500f, 600f), 150f, player, bulletList);
		enemyList.add(enemy5);
		multiBarList.add(new MultiBar(enemy5, true));
		
		Enemy enemy6= new Final(new Vector2f(1200f, 200f), 500f, player, bulletList);
		enemyList.add(enemy6);
		multiBarList.add(new MultiBar(enemy6, true)); 
*/		
		keyXAmount 			 = new IconXAmount(new Vector2f(5f,5f), 
								new Vector2f(0f,0f), 
								new Icon(new Image("res/iconXAmount/keyX.png")), 
								0 ); //it will change with the amount of keys player has
		FreshmenChestXAmount = new IconXAmount(new Vector2f(125f,5f), 
								new Vector2f(0f,0f), 
								new Icon(new Image("res/iconXAmount/FreshmenchestX.png")), 
								0 ); //it will change with the amount of chest player has

		SophomoreChestXAmount = new IconXAmount(new Vector2f(245f,5f), 
								new Vector2f(0f,0f), 
								new Icon(new Image("res/iconXAmount/SophomoreChestX.png")), 
								0 ); //it will change with the amount of chest player has

		JuniorChestXAmount = new IconXAmount(new Vector2f(365f,5f), 
								new Vector2f(0f,0f), 
								new Icon(new Image("res/iconXAmount/JuniorChestX.png")), 
								0 ); //it will change with the amount of chest player has

		SeniorChestXAmount = new IconXAmount(new Vector2f(485f,5f), 
								new Vector2f(0f,0f), 
								new Icon(new Image("res/iconXAmount/SeniorChestX.png")), 
								0 ); //it will change with the amount of chest player has
		
		coinXAmount = new IconXAmount(new Vector2f(605f,5f), 
				new Vector2f(0f,0f), 
				new Icon(new Image("res/iconXAmount/coinX.png")), 
				0 ); //it will change with the amount of chest player has
/*
		Random rnd = new Random();
		float velX = rnd.nextFloat()*1800f;
		float velY = rnd.nextFloat()*1600f;
		bonusList.add(new Key(new Vector2f(velX, velY), new Image("res/key.png"), 800, 800));
		
		Random rnd2 = new Random();
		float velX2 = rnd2.nextFloat()*1800f;
		float velY2 = rnd2.nextFloat()*1600f;
		bonusList.add(new Coin(new Vector2f(velX2, velY2), new Image("res/coin.png"), 800, 800));
		*/
		//*************************ST**************************
		
	}
	
	@Override
	public void update(GameContainer container, StateBasedGame game, int arg2) throws SlickException 
	{
		if(player !=null){
			manageInput(container);
			player.update();
		}
		for (int i =0; i < bulletList.size(); i++){
			bulletList.get(i).update();
		}
		
		for (int i =0; i < enemyList.size(); i++){
			enemyList.get(i).update();
		}
		
		handleCollisions();
		
		//*************************ST**************************
		keyXAmount.update();
		FreshmenChestXAmount.update();
		SophomoreChestXAmount.update();
		JuniorChestXAmount.update();
		SeniorChestXAmount.update();
		coinXAmount.update();
		
		for (int i=0; i<bonusList.size(); i++){
			bonusList.get(i).update();
		}
		
		if(System.currentTimeMillis() > nextTimeToSpawnBonus){
			nextTimeToSpawnBonus =  (long)BONUS_SPAWN + System.currentTimeMillis();
			if(bonusList.size() < 3){
				spawnBonus();
			}
		}
		
		if (System.currentTimeMillis() >= nextTimeToSpawnEnemy)
		{
			nextTimeToSpawnEnemy = (long)ENEMY_SPAWN + System.currentTimeMillis();
			if((enemyList.size() < 10) && !isFinalSpawn){
				spawnEnemy();
			}
		}
		
		if(enemyList.isEmpty())
		{
			Enemy enemy6= new Final(new Vector2f(1200f, 200f), 500f, player, bulletList);
			enemyList.add(enemy6);
			isFinalSpawn = true;
		}
		
		//*************************ST**************************
		
		if (player != null && player.getStats().isDead()){
			
			player = null;
			container.setMusicOn(false);
			game.getState(5).init(container, game);
			game.enterState(5);
			playerBoom.play();
		}
		if(paused)
		{
			game.enterState(3);
			paused= !paused;
		}	
	}

	@Override
	public void render(GameContainer container, StateBasedGame game, Graphics g)
	{
		//g.setBackground(BACKGROUND);
		background.draw();
		
		//*************************ST**************************
		keyXAmount.draw(g);
		FreshmenChestXAmount.draw(g);
		SophomoreChestXAmount.draw(g);
		JuniorChestXAmount.draw(g);
		SeniorChestXAmount.draw(g);
		coinXAmount.draw(g);

		for (int i =0; i < bonusList.size(); i++){
			bonusList.get(i).draw(g);
		}

		g.setColor(Color.black);
		
		String scoreLabel = "";
		if(player != null){
			scoreLabel = "SCORE : " + score;	
		}
		//g.setFont( (org.newdawn.slick.Font) new Font( "TimesRoman" , Font.PLAIN, 18 ));
		g.drawString(scoreLabel, 1780f, 5f);
		
		//*************************ST**************************
		
		if(player != null){
			player.draw(g);
		}
		if(player == null){
			g.drawString("GAME OVER!", container.getWidth()/2, container.getHeight()/2);
		}
		for (int i=0; i<bulletList.size(); i++){
			bulletList.get(i).draw(g);
		}
		for (int i=0; i<enemyList.size(); i++){
			enemyList.get(i).draw(g);
		}
	}
	
	@Override
	public int getID() {
		return 1;
	}

}