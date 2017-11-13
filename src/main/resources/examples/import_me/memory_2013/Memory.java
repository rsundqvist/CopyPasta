/** @author Richard Sundqvist, Erik Pihl @Group 64 */
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;

@SuppressWarnings("serial")
public class Memory extends JFrame {

  // class
  private static File bildmapp = new File("bildmapp");
  private static File[] bilder = bildmapp.listFiles();

  // instance
  private int currentPlayerIndex = 0;
  private int lastScore = 0;
  private int nextRows = 6;
  private int nextColumns = 6;
  private int rows = 6;
  private int columns = 6;
  private int nbrOfCards = 0;
  private int nbrOfPlayers = 0;
  private int nextNbrOfPlayers = 2;
  private int currentRound = 1;

  private boolean showingImage = false;
  private boolean multiplayer = true;
  private boolean timerNotRunning = true;
  private boolean firstCardSelection = true;
  private boolean weightedScore_nyttSpel = true;

  private Player currentPlayer = null;
  private Player currentShowingPlayer = null;
  private Player[] p = new Player[5];
  private Kort prevCard = null;
  private Kort currCard = null;
  private Kort[] kort = new Kort[bilder.length]; // [50]

  private JFrame memory;
  private JPanel spelplan;
  private JInternalFrame showImage;
  private JPanel spelare;

  // CONSTRUCTOR
  public Memory() {
    this("Memory", EXIT_ON_CLOSE);
  }

  public Memory(String str) {
    this(str, EXIT_ON_CLOSE);
  }

  public Memory(int exitOp) {
    this("Memory", exitOp);
  }

  public Memory(String title, int exitOp) {

    // MAIN WINDOW
    memory = new JFrame();
    spelare = new JPanel();
    spelplan = new JPanel();

    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    memory.setSize(dim.width / 5 * 4, dim.height / 5 * 4); // ~80% av sk�rmstorlek
    memory.setLocation(
        dim.width / 2 - memory.getSize().width / 2,
        dim.height / 2 - memory.getSize().height / 2); // mitt i sk�rmen
    memory.setDefaultCloseOperation(exitOp);
    memory.setTitle(title);
    memory.setLayout(new BorderLayout());

    // MENUES
    JMenuBar menubar = new JMenuBar();
    JMenu game = new JMenu("Spel");
    JMenu settings = new JMenu("Inst�llningar");

    // MENU ITEMS
    JMenuItem game_newGame = new JMenuItem("Nytt spel");
    JMenuItem game_quit = new JMenuItem("Avsluta");
    JMenuItem settings_nbrOfPlayers = new JMenuItem("�ndra antalet spelare");
    JMenuItem settings_rowsAndColumns = new JMenuItem("�ndra spelplanens storlek");
    final JCheckBoxMenuItem settings_weightedScore = new JCheckBoxMenuItem("Viktade po�ng", true);

    // game_newGame
    game_newGame.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            nyttSpel();
          }
        });
    game_newGame.setAccelerator(
        KeyStroke.getKeyStroke('N', KeyEvent.CTRL_DOWN_MASK)); // hotkey CTRL+N
    game_newGame.setToolTipText("Starta ett nytt spel med de valda inst�llningarna.");

    // game_quit
    game_quit.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            Object[] options0 = {"Forts�tt spela", "Avsluta"};
            int exit =
                JOptionPane.showOptionDialog(
                    spelplan,
                    "Verkligen avsluta? ",
                    "Avsluta",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options0,
                    options0[0]);
            if (exit == 1) {
              System.exit(0);
            }
          }
        });

    // settings_nbrOfPlayers
    final String[] options1 = new String[] {"   1   ", "   2   ", "   3   ", "   4   ", "   5   "};
    settings_nbrOfPlayers.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            nextNbrOfPlayers =
                (1
                    + JOptionPane.showOptionDialog(
                        spelplan,
                        "V�lj antalet spelare:",
                        "�ndra antal spelare",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        null,
                        options1,
                        options1[0]));
          }
        });
    settings_nbrOfPlayers.setAccelerator(
        KeyStroke.getKeyStroke('W', KeyEvent.CTRL_DOWN_MASK)); // hotkey CTRL+W

    // settings_rowsAndColumns
    settings_rowsAndColumns.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            JTextField field1 = new JTextField();
            JTextField field2 = new JTextField();
            int input_rows = 0;
            int input_columns = 0;
            Object[] message = {
              "Du spelar just nu med "
                  + rows
                  + " rader och "
                  + columns
                  + " kolumner. \n\nNytt antal rader:",
              field1,
              "Nytt antal kolumner:",
              field2,
            };
            int option =
                JOptionPane.showConfirmDialog(
                    spelplan, message, "�ndra spelplanens storlek", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION)
              if (field1.getText().length() != 0 && field2.getText().length() != 0) {

                try {
                  input_rows = Integer.parseInt(field1.getText());
                  input_columns = Integer.parseInt(field2.getText());

                  // k�rs i try d� den rows/cols blir 0 om length = 0,  ger tv� felmeddelanden om
                  // den ligger utanf�r. B�ttre prestanda att anv�nda en bool?
                  if (input_rows * input_columns >= 4
                      && input_rows * input_columns - ((input_rows * input_columns) % 2)
                          <= bilder.length * 2) {
                    nextRows = input_rows;
                    nextColumns = input_columns;
                  } else {
                    JOptionPane.showMessageDialog(
                        spelplan,
                        "Ogilitigt antal rader och kolumner. Spelplanen kan inneh�lla minst 4 och h�gst "
                            + bilder.length * 2
                            + " kort.");
                  }
                } catch (NumberFormatException e) {
                  JOptionPane.showMessageDialog(spelplan, "Endast siffror");
                }
              }
          }
        });
    settings_rowsAndColumns.setAccelerator(
        KeyStroke.getKeyStroke('D', KeyEvent.CTRL_DOWN_MASK)); // hotkey CTRL+D

    // settings_weightedScore
    settings_weightedScore.addItemListener(
        new ItemListener() {
          public void itemStateChanged(ItemEvent e) {
            weightedScore_nyttSpel = settings_weightedScore.isSelected();
          }
        });
    settings_weightedScore.setToolTipText(
        "VALD: Ger spelaren po�ng baserat p� antalet f�rs�k och m�ngden kort kvar p� spelplanen.");

    // add + setVisible
    memory.setJMenuBar(menubar);
    menubar.add(game);
    menubar.add(settings);

    game.add(game_newGame);
    game.add(game_quit);
    settings.add(settings_nbrOfPlayers);
    settings.add(settings_rowsAndColumns);
    settings.add(settings_weightedScore);

    memory.setVisible(true);
    game.setVisible(true);
    settings.setVisible(true);

    // PANELS
    memory.add(spelare, BorderLayout.WEST);
    memory.add(spelplan, BorderLayout.CENTER);

    spelplan.setLayout(new GridLayout(rows, columns));
    spelplan.setBackground(Color.white);
    spelare.setBackground(Color.LIGHT_GRAY);
    spelare.setPreferredSize(new Dimension(170, memory.getHeight()));

    // creation of cards
    for (int i = 0; i < kort.length; i++) {
      kort[i] = new Kort(new ImageIcon(bilder[i].getAbsolutePath()), Kort.Status.DOLT);
    }

    nyttSpel();
  } // END CONSTRUCTOR

  // ta emot argument, upprepa f�rsta raden i try inom if-sats f�r att spela upp olika ljud
  public static void playSound() {
    try {
      AudioInputStream audioInputStream =
          AudioSystem.getAudioInputStream(new File("match.wav").getAbsoluteFile());
      Clip clip = AudioSystem.getClip();
      clip.open(audioInputStream);
      // tycker det l�ter alldeles f�r mycket om den spelar ljud �ven d� man inte hittar n�got
      clip.start();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  @SuppressWarnings("unused")
  public static void main(String[] args) {

    Memory m = new Memory();
    // Memory t1 = new Memory("test 1", DISPOSE_ON_CLOSE);
    // Memory t2 = new Memory("test 2", DISPOSE_ON_CLOSE);

  }

  public void nyttSpel() {

    // Clear board and write/reset variables
    currentRound++;
    nbrOfPlayers = nextNbrOfPlayers;
    currentPlayerIndex = 0;

    firstCardSelection = true;
    timerNotRunning = true;

    rows = nextRows;
    columns = nextColumns;

    spelare.removeAll();
    spelplan.removeAll();

    // rensar spelare vid nytt spel, garbage collectorn sk�ter resten
    for (int i = 0; i < 5; i++) {
      p[i] = null;
    }

    // inte j�ttesnyggt, men lyckades inte s�tta godt. antal spelare och rensa med reflexion
    for (int i = 0; i < nbrOfPlayers; i++) {
      p[i] = new Player("Spelare " + (i + 1), this);
      p[i].setWeightedScore(weightedScore_nyttSpel);
    }
    // singleplayerl�ge?
    if (this.nbrOfPlayers != 1) {
      multiplayer = true;
    } else {
      multiplayer = false;
    }

    currentPlayer = p[0];
    currentPlayer.setActivePlayer(currentPlayer);

    // KORTHANTERING
    Verktyg.slumpOrdning(kort); // nya kort varje g�ng
    nbrOfCards = rows * columns - (rows * columns % 2);
    Kort[] spelkort = new Kort[nbrOfCards];

    // Dubblering, slumpning
    int cloneIndex = 0;
    for (int i = 0; i < (nbrOfCards) / 2; i++) {
      spelkort[cloneIndex] = kort[i].copy(); // "original"
      cloneIndex++;
      spelkort[cloneIndex] = kort[i].copy(); // kopia
      cloneIndex++;
    }
    Verktyg.slumpOrdning(spelkort);

    // Placering p� spelplan
    for (int i = 0; i < nbrOfCards; i++) {
      spelplan.add(spelkort[i]);
      spelkort[i].addActionListener(
          new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              if (timerNotRunning) {
                cardAction((Kort) e.getSource());
              }
            }
          });
    }

    // g�r plats f�r spelare och kort
    spelare.setLayout(new GridLayout(nbrOfPlayers, 1)); // (5, 1)
    spelplan.setLayout(new GridLayout(rows, columns));

    spelare.updateUI();
    spelplan.updateUI();

    if (showingImage) {
      closeImage();
    }
  } // End METHOD nyttSpel

  // Anropas n�r anv�ndaren klickar p� ett kort
  private void cardAction(Kort Btn) {
    currCard = Btn;

    currCard.setStatus(Kort.Status.SYNLIGT);
    if (firstCardSelection) {
      firstCardSelection = !firstCardSelection;
      currCard.setEnabled(false);
      prevCard = currCard;
    } else {
      firstCardSelection = !firstCardSelection;
      currCard.setEnabled(false);
      matchAttempt();
    }
  }

  // Korten lika?
  private void matchAttempt() {
    final int roundOnEntry = currentRound;

    if (prevCard.sammaBild(currCard)) {

      currentPlayer.setFoundIcon(currCard.getIcon());
      timerNotRunning = false;
      prevCard.setDisabledIcon(null);
      currCard.setDisabledIcon(null);
      currentPlayer.guessRight(this);
      nbrOfCards = nbrOfCards - 2;
      playSound();

      new java.util.Timer()
          .schedule(
              new java.util.TimerTask() {
                @Override
                public void run() {

                  if (currentRound == roundOnEntry) {
                    timerNotRunning = true;
                    prevCard.setStatus(Kort.Status.SAKNAS);
                    currCard.setStatus(Kort.Status.SAKNAS);
                    prevCard.setEnabled(false);
                    currCard.setEnabled(false);
                    // Alla kort hittade?
                    if (nbrOfCards == 0) {
                      roundEnd();
                    }
                  }
                }
              },
              350);
      // !prevCard.sammaBild(currCard)
    } else {
      timerNotRunning = false;

      new java.util.Timer()
          .schedule(
              new java.util.TimerTask() {
                @Override
                public void run() {
                  if (currentRound == roundOnEntry) {
                    // CARDLOCK
                    prevCard.setStatus(Kort.Status.DOLT);
                    currCard.setStatus(Kort.Status.DOLT);
                    prevCard.setEnabled(true);
                    currCard.setEnabled(true);

                    // Change player
                    currentPlayer.guessWrong(Memory.this);
                    timerNotRunning = true;
                    if (multiplayer) {
                      if (currentPlayerIndex != (nbrOfPlayers - 1)) {
                        currentPlayerIndex++;
                        currentPlayer = p[currentPlayerIndex];
                        currentPlayer.setActivePlayer(p[currentPlayerIndex - 1]);

                      } else {
                        currentPlayer = p[0];
                        currentPlayer.setActivePlayer(p[currentPlayerIndex]);
                        currentPlayerIndex = 0;
                      }
                    }
                  }
                }
              },
              1500);
    }
  } // End METHOD matchAttempt

  // Vem vann?
  public void roundEnd() {

    Player vinnarN = p[0];
    String msg = "";
    if (multiplayer) {
      for (int i = 0; i < nbrOfPlayers; i++) {
        if (vinnarN.getPlayerScore() < p[i].getPlayerScore()) vinnarN = p[i];
      }
      msg =
          vinnarN.getPlayerName()
              + " vann med "
              + vinnarN.getPlayerScore()
              + " po�ng! \nB�sta po�ngen hittills: "
              + lastScore;

      if (vinnarN.getPlayerScore() > lastScore) {
        lastScore = vinnarN.getPlayerScore();
      }
      // singleplayer
    } else {
      String str = "";
      if (lastScore < p[0].getPlayerScore()) str = "Grattis!";
      else {
        str = "Tyv�rr!";
      }
      msg =
          str
              + " Du fick "
              + p[0].getPlayerScore()
              + " po�ng! \nB�sta po�ngen f�rra g�ngen var: "
              + lastScore;
      lastScore = p[0].getPlayerScore();
    }

    Object[] options0 = {"Nytt spel", "Fors�tt", "Avsluta"};
    int end =
        JOptionPane.showOptionDialog(
            spelplan,
            msg,
            "Spelet slut!",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options0,
            options0[0]);
    switch (end) {
      case 0:
        nyttSpel();
        break;
      case 1: // do nothing
        break;
      case 2: // skulle kunna g�ra doClick p� avsluta knappen
        Object[] options2 = {"Forts�tt", "Avsluta"};
        int exit =
            JOptionPane.showOptionDialog(
                spelplan,
                "Verkligen avsluta? ",
                "Avsluta",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options2,
                options0[0]);
        if (exit == 1) {
          System.exit(0);
        }
        break;
    }
  } // End METHOD roundEnd

  // Visar bilden p� spelarkortet
  public void viewImage(Icon pic, Player currShow) {
    if (!showingImage) {
      showImage(pic);
      currentShowingPlayer = currShow;
    } else if (showingImage && currShow == currentShowingPlayer) {
      closeImage();
      currentShowingPlayer = null;
    } else {
      closeImage();
      currentShowingPlayer = currShow;
      showImage(pic);
    }
  }

  public void showImage(Icon pic) {

    showingImage = true;

    JLabel img = new JLabel();
    img.setIcon(pic);
    showImage = new JInternalFrame(pic.toString());
    showImage.setLayout(new BorderLayout());
    showImage.setBackground(Color.white);

    JPanel dummy_left = new JPanel(), dummy_right = new JPanel();
    int dummyWidth = (spelplan.getWidth() - pic.getIconWidth()) / 2;
    dummy_left.setPreferredSize(new Dimension(dummyWidth, 1));
    dummy_right.setPreferredSize(new Dimension(dummyWidth, 1));
    dummy_left.setBackground(Color.white);
    dummy_right.setBackground(Color.white);

    // Tvinga fram r�tt storlek p� mittpanelen
    showImage.add(dummy_left, BorderLayout.WEST);
    showImage.add(img, BorderLayout.CENTER);
    showImage.add(dummy_right, BorderLayout.EAST);

    memory.remove(spelplan);
    memory.add(showImage, BorderLayout.CENTER);

    showImage.setVisible(true);
  }

  // St�ng bildvisaren
  private void closeImage() {
    showingImage = false;
    showImage.dispose();
    memory.remove(showImage);
    memory.add(spelplan, BorderLayout.CENTER);
  }

  public int getNbrOfCards() {
    return nbrOfCards;
  }

  public void addPlayerCard(Component card) {
    spelare.add(card);
  }
} // End CLASS Memory
