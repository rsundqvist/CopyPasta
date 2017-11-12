/**
*  @author Richard Sundqvist, Erik Pihl
*  @Group 64
*/

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

	public class Player{
		
		//class
		// inget
		
		//instance 
		private int found = 0; //antal kortpar hittade
		private int attempts = 0; //antal kortpar vända
		private int score = 0;
		private boolean weightedScore = true;
		private String name = "";

		private JPanel playerCard;
		private JLabel playerAttempts;
		private JLabel playerScore;
		private JButton lastFound;
		

		
		public Player(String newName, final Memory mem){
			playerCard = new JPanel();
			playerCard.setBackground(Color.LIGHT_GRAY); //Color.LIGHT_GRAY
			name = newName;
			JLabel playerName = new JLabel(name);
			
			//Visar senaste kortet som hittades
			lastFound = new JButton();
			lastFound.setEnabled(false);
			lastFound.setPreferredSize (new Dimension (140, 90));
			lastFound.setToolTipText("Visa bilden. Klicka igen för att fortsätta spela.");
			lastFound.addActionListener(new ActionListener() {
			    public void actionPerformed(ActionEvent e) {
			    	mem.viewImage(lastFound.getIcon(), Player.this);
			    }
			});
			
			playerAttempts = new JLabel("Vända kortpar: " + attempts);
			playerScore = new JLabel("<html>Hittade kortpar: " + found + "<br> Poäng: " + score + "</html>");
			
			playerName.setFont(new Font("Tahoma", Font.BOLD, 15));
			playerAttempts.setFont(new Font("Arial", Font.BOLD, 10));
			playerScore.setFont(new Font("Arial", Font.BOLD, 10));
			
			playerName.setPreferredSize(new Dimension(100, 20));
			playerAttempts.setPreferredSize(new Dimension(100, 20));
			playerScore.setPreferredSize(new Dimension(100, 40));
			
			playerCard.add(playerName);
			playerCard.add(playerAttempts);
			playerCard.add(playerScore);
			playerCard.add(lastFound);
			mem.addPlayerCard(playerCard);
		}
		
		//GETTERS and SETTERS
		public String getPlayerName(){
			return name;
		}
		
		public void setWeightedScore(boolean set){
			weightedScore = set;
		}
		
		public int getPlayerScore(){
			return this.score;
		}
		
		//Functional methods
		
		public void guessRight(Memory mem){
			this.found++;
			this.attempts++;
			this.playerScore(true, mem);
			playerAttempts.setText("Vända kortpar: " + attempts);
			playerScore.setText("<html>Hittade kortpar: " + found+"<br> Poäng: " + score + "</html>");
			
		}
		public void setFoundIcon(Icon pic){
			this.lastFound.setIcon(pic); 
			lastFound.setEnabled(true);
		}
		public void guessWrong(Memory mem){
			this.attempts++;
			this.playerScore(false, mem);
			playerAttempts.setText("Vända kortpar: " + attempts);
			playerScore.setText("<html>Hittade kortpar: " + found+"<br> Poäng: " + score + "</html>");
		}
		public void setActivePlayer(Player previousPlayer){
			previousPlayer.playerCard.setBackground(Color.LIGHT_GRAY);
			this.playerCard.setBackground(Color.GREEN);
		}
		
		public void playerScore(boolean rightAns, Memory mem){
			//viktade poäng
			if (weightedScore){
				if (rightAns){
					this.score = this.score+5*mem.getNbrOfCards();
				} else {
					this.score = this.score-this.attempts*5/mem.getNbrOfCards();
					if (this.score < 0){
						this.score = 0;
					}
				}
			//ej viktade poäng	
			} else if (!weightedScore && rightAns) {
				this.score = this.score+10;
			}
			//TODO: svävande text över kortet som visar hur många poäng man fick
		} //End METHOD playerScore
		
	} //End CLASS Player