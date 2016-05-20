package poker;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application{

	class CycleIterator<T> implements Iterator<Integer>{

		private List<Integer> list;
		private Iterator<Integer> iter;

		public CycleIterator(List<Integer> list) {
			this.list = list;
			this.iter = this.list.iterator();
		}
		
		@Override
		public boolean hasNext() {
			// TODO Auto-generated method stub
			return true;
		}

		@Override
		public Integer next() {
			if(iter.hasNext())
				return iter.next();
			else{
				iter=list.iterator();
				return iter.next();
			}
		}
		
	}
	
	public static void main(String[] args) {
		
		
		launch(args);

	}

	private int counter;

	@Override
	public void start(Stage primaryStage) throws Exception {
		
		int[] cardsIdx = new int[52];
		
//		WritableImage[] cardsImg = new WritableImage[52];
		
		Random random = new Random();
		File cardsImgFile = new File("res/cards.jpg");
		
		Image img = new Image(new FileInputStream(cardsImgFile));
//		img.getPixelReader().
		
		for(int i=0; i<=51;i++){
			int nextInt = random.nextInt(52);
			
			while(Arrays.binarySearch(cardsIdx, nextInt)>=0){
				nextInt = random.nextInt(52);
			}
			cardsIdx[i]=nextInt;
			
			/*BufferedImage subimage;
			if(nextInt<13){
				
				
				subimage = ImageIO.read(cardsImgFile).getSubimage(0, nextInt*180, 120, 180);
				
			}
			else if(nextInt<26){
				subimage = ImageIO.read(cardsImgFile).getSubimage(120, (nextInt-13)*180, 120, 180);
			}
			else if(nextInt<39){
				subimage = ImageIO.read(cardsImgFile).getSubimage(240, (nextInt-26)*180, 120, 180);
			}
			else{
				subimage = ImageIO.read(cardsImgFile).getSubimage(360, (nextInt-39)*180, 120, 180);
			}
			
			
			WritableImage fxImage = new WritableImage(120, 180);
			SwingFXUtils.toFXImage(subimage,fxImage);
			
			cardsImg[i]=fxImage;*/
			
		}
		
		primaryStage.setTitle("Best Super Mega Poker");
		
		StackPane pane = new StackPane();
		
		Scene ms = new Scene(pane,800,600);
		
		primaryStage.setScene(ms);
		
		Canvas cnvs = new Canvas(800,600);
		GraphicsContext gc = cnvs.getGraphicsContext2D();
		
		List<Integer> players = new ArrayList<>(3);
		players.add(0);
		players.add(1);
		players.add(2);
		
		CycleIterator<Integer> player = new CycleIterator<>(players);
		
		for(int i=0; i<=5; i++){
			int nextInt = cardsIdx[i];
			drawCards(img, gc, nextInt, player.next());
			
			if(i%players.size()==0){
				counter++;
			}
		}

		
		pane.getChildren().add(cnvs);
		
		primaryStage.show();
		
		
	}

	private void drawCards(Image img, GraphicsContext gc, int nextInt, Integer player) {
		if(nextInt<13){
//			gc.drawImage(img, 215, 300, 120,180, 0, nextInt*180, 120, 180);
			drawCard(img, gc,0, nextInt, player);
		}
		else if(nextInt<26){
//			gc.drawImage(img, 215, 300, 120,180, 120, (nextInt-13)*180, 120, 180);
			drawCard(img, gc,1, nextInt-13, player);
		}
		else if(nextInt<39){
//			gc.drawImage(img, 215, 300, 120,180, 
//					240, (nextInt-26)*180, 120, 180);
			drawCard(img, gc,2, nextInt-26, player);
		}
		else{
			drawCard(img, gc,3, nextInt-39, player);
		}
	}

	private void drawCard(Image img, GraphicsContext gc, int column, int nextInt, int player) {
		if (player==0)
			gc.drawImage(img, 
				120*column, (nextInt)*180, 120, 180
				, 215+40*counter, 300, 120,180);
		if (player==1)
			gc.drawImage(img, 
					120*column, (nextInt)*180, 120, 180
					, 0+40*counter, 0, 120,180);
		
		if (player==2)
			gc.drawImage(img, 
					120*column, (nextInt)*180, 120, 180
					, 400+40*counter, 0, 120,180);

		
	}

}
