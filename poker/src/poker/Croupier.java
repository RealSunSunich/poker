package poker;

import java.util.ArrayList;

public class Croupier {
	static String[] onTable= {"0_12","1_07","3_12","2_00","4_12"};
	static Card[] array=new Card[7];
	public static void main(String[] args) {
		String[] myCards={"0_11","3_07"};
		st(myCards);
		checkPairs();
	}
	public static void st(String[] cards){
		for(int i=0;i<onTable.length;i++)
			array[i]=new Card(onTable[i]);
		for(int i=onTable.length;i<onTable.length+cards.length;i++)
			array[i]=new Card(cards[i-onTable.length]);		
		qs(0,array.length-1);
		int result=0;		
	}
	
	public static int[][] checkPairs(){
		ArrayList<Card> com = new ArrayList<Card>();
		for(int i=0;i<array.length;i++){
			for(int j=i;j<array.length;j++){
				if(i!=j && array[i].m==array[j].m){
					com.add(array[j]);
				}
			}
			if(com.size()>0)
				array[i].setCombination(com);
		}
		return null;
	}
	public int checkStraight(ArrayList<Card> cards){
		int c=5;
		for(int i=0;i<cards.size()-1;i++){
			if(cards.get(i).m-1==cards.get(i+1).m)
				c--;
			else
				c=5;
		}
		return c==5?cards.get(0).m*5-10:-1;
	}
	public int[] checkStraight(){
		return null;
	}
	
	public int[] checkFlash(){
		ArrayList<Card> com = new ArrayList<Card>();
		for(int i=array.length;i>=0;i--){
			for(int j=array.length;j>=0;j--){
				if(i!=j && array[i].col==array[j].col){
					com.add(array[j]);
				}
			}
			if(com.size()>=5)
				array[i].setCombination((ArrayList<Card>) com.subList(0, 5));
			
		}
		return null;
	}  
	
	public static void qs(int start, int end){		
	        if (start >= end)
	            return;
	        int i = start, j = end;
	        int cur = i - (i - j) / 2;
	        while (i < j) {
	            while (i < cur && !(array[i].compareTo(array[cur])>0) ) {
	                i++;
	            }
	            while (j > cur && !(array[cur].compareTo(array[j])>0) ) {
	                j--;
	            }
	            if (i < j) {
	                Card temp = array[i];
	                array[i] = array[j];
	                array[j] = temp;
	                if (i == cur)
	                    cur = j;
	                else if (j == cur)
	                    cur = i;
	            }
	        }
	        qs(start, cur);
	        qs(cur+1, end);
	}
	static class Card implements Comparable{
		int col;
		int m;
		boolean isPair=false;
		boolean isThree=false;
		boolean isFour=false;
		ArrayList<Card> combination = new ArrayList<Card>();
		boolean isFlash=false;
		
		public Card(int c, int m){
			this.col=c;
			this.m=m;
		}
		public Card(String c,String m){
			this.col=Integer.valueOf(c);
			this.m=Integer.valueOf(m);
		}
		public Card(String full){
			this.col=Integer.valueOf(full.substring(0,1));
			this.m=Integer.valueOf(full.substring(2));
		}
		
		@Override
		public int compareTo(Object arg0) {			
			Card o = (Card) arg0;
			if(this.m>o.m)
				return 1;
			else if(o.m==this.m)
				return 0;
			else
				return -1;
		}
		
		@Override
		public boolean equals(Object arg0){
			Card o = (Card) arg0;
			if(o.col==this.col && o.m==this.m)
				return true;
			return false;
			
		}
		public boolean isPartOfPair(){
			return isPair;
		}
		public boolean isPartOfThree(){
			return isThree;
		}
		public boolean isPartOfFour(){
			return isFour;
		}	
		public void setPair(Card pair){
			combination.add(pair);
			this.isPair=true;
		}
		public void setThree(Card pair,Card pair2){
			combination.add(pair);
			combination.add(pair2);
			this.isThree=true;
		}
		public void setFour(ArrayList<Card> threeAnother){
			combination = threeAnother;
			this.isFour=true;
		}
		public void setCombination(ArrayList<Card> comb){
			switch(comb.size()){
				case 1: isPair=true; break;
				case 2: isThree=true; break;
				case 3: isFour=true; break;
				case 4: isFlash=true; break;
			}
			this.combination=comb;
		}
		public int getValueOfCom(){
			if(this.combination.size()>0){
				int res=m+1;
				res=(50+m+1)*this.combination.size()+1;
				return res;
			}
			return this.m+1;
		}
		public boolean isPardOfFlash(){
			return isFlash;
		}
	}

}
