/**
 * @author Richard Sundqvist
 * @group 31
 */

// public static final Dimension SIZE = new Dimension(10, 16); rekommenderas i GameController, men
// det fungerar med 10x10.

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;

/**
 * @author Admin A model to run the game Tetris. Does not capture keystrokes or handle graphics.
 *     Does not keep track of individual blocks after they're settled; No cascading pieces.
 */
public class TetrisModel extends GameModel {

  private static final GameTile BLANK_TILE = new RectangularTile(new Color(255, 255, 255));
  private static final GameTile BLACK_TILE = new RectangularTile(new Color(0, 0, 0));
  /** Colors for the different tetrominos. Based on http://en.wikipedia.org/wiki/tetromino */
  private static final GameTile I_TILE = new RectangularTile(new Color(0, 255, 255));
  private static final GameTile O_TILE = new RectangularTile(new Color(255, 255, 0));
  private static final GameTile T_TILE = new RectangularTile(new Color(255, 0, 255));
  private static final GameTile J_TILE = new RectangularTile(new Color(0, 0, 255));
  private static final GameTile L_TILE = new RectangularTile(new Color(255, 128, 255));
  private static final GameTile S_TILE = new RectangularTile(new Color(0, 255, 0));
  private static final GameTile Z_TILE = new RectangularTile(new Color(255, 0, 0));
  // Storing these locally should be benefitial to performance.
  final int board_width = getGameboardSize().width;
  final int board_height = getGameboardSize().height;
  /** Temporary coordinates for a given piece. */
  int[][] tmpCoordinates = new int[4][2];
  int rotationPointX; // defines x-coord of the tile to rotate around.
  int rotationPointY; // defines y-coord of the tile to rotate around.
  /** Calculated based on the number of rows destroyed. */
  private int score = 0;
  /** Block falls instantly if <code>true</code>. */
  private boolean plummetTetromino = false;
  /** <code>true</code> if the next block failed to spawn. */
  private boolean gameOver = false;
  /** The shape currently falling. */
  private GameTile currentTetrominoTile = O_TILE;
  private Tetromino currentTetromino = Tetromino.O;
  /** The (x, y) coordinates at which the tetromino will be drawn next. */
  private int nextTetrominoCoordinates[][] = new int[4][2];
  /** Current (x, y) coordinates for the 4 different tiles representing the current tetromino. */
  private int currentTetrominoCoordinates[][] = new int[4][2];
  /** Clears the board and spawns the first piece. */
  public TetrisModel() {

    // Blank out the whole gameboard
    for (int i = 0; i < board_width; i++) {
      for (int j = 0; j < board_height; j++) {
        setGameboardState(i, j, BLANK_TILE);
      }
    }
    // playTheme(true);
    // create the first tetromino
    currentTetromino =
        Tetromino.getTetromino((int) (Math.random() * 7)); // Randomize the first piece.
    spawnTetromino(currentTetromino, false);
  } // End constructor

  public static void playSound(int soundIndex) {
    try {
      AudioInputStream audioInputStream;
      Clip clip = AudioSystem.getClip();

      // Select sound
      switch (soundIndex) {
        case 0: // Tetromino at rest
          audioInputStream =
              AudioSystem.getAudioInputStream(new File("at_rest.wav").getAbsoluteFile());
          break;
        case 1: // Completed row
          audioInputStream =
              AudioSystem.getAudioInputStream(new File("complete_row.wav").getAbsoluteFile());
          break;
        case 2: // Game over
          clip.stop();
          audioInputStream =
              AudioSystem.getAudioInputStream(new File("game_over.wav").getAbsoluteFile());
          break;
        default: // nosound
          // System.out.println("Unknown sound: " + soundIndex);
          return;
      }
      clip.open(audioInputStream);
      clip.start();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  private void tetrominoControl(final int key) {
    switch (key) {
      case KeyEvent.VK_LEFT:
        // strafe left
        tetrominoXAxisMovement(-1);
        break;
      case KeyEvent.VK_RIGHT:
        // strafe right
        tetrominoXAxisMovement(+1);
        break;
      case KeyEvent.VK_A:
        // rush left
        // Would rather use double tap arrow key, but GameController does not support this.
        tetrominoXAxisMovement(-1);
        tetrominoXAxisMovement(-1);
        break;
      case KeyEvent.VK_S:
        // rush right
        tetrominoXAxisMovement(+1);
        tetrominoXAxisMovement(+1);
        break;
      case KeyEvent.VK_DOWN:
        // 2x fall speed
        tetrominoYAxisMovement(1);
        break;
      case KeyEvent.VK_SPACE:
        // plummet
        plummetTetromino = true;
        break;
      case KeyEvent.VK_Z:
        // rotate anti-clockwise
        rotateTetromino(false);
        break;
      case KeyEvent.VK_X:
        // rotate clockwise
        rotateTetromino(true);
        break;
      default:
        // Don't change direction if another key is pressed
        break;
    }
  } // End updateCommand METHOD

  @Override
  public void gameUpdate(int lastKey) throws GameOverException {

    if (gameOver) {
      throw new GameOverException(score);
    }
    copySIArray(nextTetrominoCoordinates, tmpCoordinates);

    if (tetrominoYAxisMovement(1)
        == false) { // spawn a new piece if the current one cannot drop any further
      playSound(0); // play at rest sound
      if (findCompleteRows()) {
        playSound(1); // Play only once if one or more rows are destroyed.
      }
      currentTetromino =
          Tetromino.getTetromino((int) (Math.random() * 7)); // Spawn a new random piece.
      spawnTetromino(currentTetromino, false);
      if (locationObstructed(
          nextTetrominoCoordinates)) { // getGameboardState(board_width/2, 0) != BLANK_TILE
        spawnTetromino(currentTetromino, true);
        gameOver = true;
        // playTheme(false);
        playSound(2); // Play game over sound.
      }
      writeTetromino(false);
    } else {
      // dropTetromino before updateCommand makes "infinity spin" more difficult.
      copySIArray(nextTetrominoCoordinates, tmpCoordinates);
      tetrominoControl(lastKey);
      writeTetromino(true);
    }
  } // End gameUpdate METHOD

  /**
   * Rotates a piece around a given center point.
   *
   * @param clockwise Performs a clockwise rotation by performing two anti-clockwise rotations if
   *     <code>true</code>.
   */
  private boolean rotateTetromino(boolean clockwise) {

    // Solution based on http://en.wikipedia.org/wiki/Rotation_matrix and
    // http://stackoverflow.com/questions/233850/tetris-piece-rotation-algorithm

    // Set rotation point.
    switch (currentTetromino.index) {
      case 0: // I
        rotationPointX = nextTetrominoCoordinates[2][0];
        rotationPointY = nextTetrominoCoordinates[2][1];
        break;
      case 1: // O
        // do nothing
        return true;
      case 2: // T
        rotationPointX = nextTetrominoCoordinates[2][0];
        rotationPointY = nextTetrominoCoordinates[2][1];
        break;
      case 3: // J
        rotationPointX = nextTetrominoCoordinates[2][0];
        rotationPointY = nextTetrominoCoordinates[2][1];
        break;
      case 4: // L
        rotationPointX = nextTetrominoCoordinates[2][0];
        rotationPointY = nextTetrominoCoordinates[2][1];
        break;
      case 5: // S
        rotationPointX = nextTetrominoCoordinates[2][0];
        rotationPointY = nextTetrominoCoordinates[2][1];
        break;
      case 6: // Z
        rotationPointX = nextTetrominoCoordinates[2][0];
        rotationPointY = nextTetrominoCoordinates[2][1];
        break;
      default:
        throw new IllegalArgumentException("Unknown shape: " + currentTetromino.toString() + ".");
    }

    // Perform a counter-clockwise rotation.
    for (int i = 0; i < 4; i++) {

      // Function assumes rotation around (0, 0): Apply offset.
      tmpCoordinates[i][0] = tmpCoordinates[i][0] - rotationPointX;
      tmpCoordinates[i][1] = tmpCoordinates[i][1] - rotationPointY;

      // Function assumes axes propagate to the top and to the right: invert y-axis coordinate.
      tmpCoordinates[i][1] = -tmpCoordinates[i][1];

      // Perform rotation.
      int tmpY = tmpCoordinates[i][0];
      int tmpX = -tmpCoordinates[i][1];
      // Once both calculations are completed the old values are no longer needed. Fetch results
      // from temporary variables.
      tmpCoordinates[i][1] = tmpY;
      tmpCoordinates[i][0] = tmpX;

      // Revert y-axis coordinate inversion.
      tmpCoordinates[i][1] = -tmpCoordinates[i][1];

      // Revert offset.
      tmpCoordinates[i][0] = tmpCoordinates[i][0] + rotationPointX;
      tmpCoordinates[i][1] = tmpCoordinates[i][1] + rotationPointY;
    }

    // Performs two counter-clockwise rotations to imitate a single clockwise rotation.
    if (clockwise) {
      rotateTetromino(false);
      rotateTetromino(false);
    }

    return moveTetrominoIfPositionClear(nextTetrominoCoordinates, tmpCoordinates);
  } // End rotateTetromino METHOD

  /**
   * Moves the current tetromino sideways, if possible.
   *
   * @param deltaX The desired distanse to move.
   */
  private boolean tetrominoXAxisMovement(int deltaX) {

    // copySIArray(nextTetrominoCoordinates, tmpCoordinates);

    for (int i = 0; i < 4; i++) {
      tmpCoordinates[i][0] = nextTetrominoCoordinates[i][0] + deltaX;
    }

    return moveTetrominoIfPositionClear(nextTetrominoCoordinates, tmpCoordinates);
  } // End strafeTetromino METHOD

  /**
   * Shifts the tetromino along the y-axis.
   *
   * @param deltaY the desired fall distance.
   * @param plummetTetromino Fall instantly if <code>true</code>.
   */
  private boolean tetrominoYAxisMovement(int deltaY) {

    // copySIArray(nextTetrominoCoordinates, tmpCoordinates);

    for (int i = 0; i < 4; i++) {
      tmpCoordinates[i][1] = nextTetrominoCoordinates[i][1] + deltaY;
    }

    if (plummetTetromino) {
      plummetTetromino = false;

      while (tetrominoYAxisMovement(1)) ;
      // Necessary only if the piece hits the edge
      writeTetromino(false);
    }

    return moveTetrominoIfPositionClear(nextTetrominoCoordinates, tmpCoordinates);
  } // End dropTetromino METHOD

  /**
   * Attempts to move a piece from source to target coordinates. Returns true if successful. This
   * method is possibly much less efficient than tetrominoCollision (see bottom), but should be
   * easier to use.
   *
   * @param sourceCoordinates The source location of the piece to move.
   * @param targetCoordinates The target location of the piece.
   * @return <code>True</code> if move was successful.
   */
  private boolean moveTetrominoIfPositionClear(
      int[][] sourceCoordinates, int[][] targetCoordinates) {

    // Did it like this because i *think* its a performance boost compared to having it all in one
    // loop.
    for (int i = 0; i < 4; i++) {
      if (targetCoordinates[i][1] > board_height - 1
          || targetCoordinates[i][1] < 0
          || // y-bounds
          targetCoordinates[i][0] > board_width - 1
          || targetCoordinates[i][0] < 0) { // x-bounds
        // Do nothing if target position is out of bounds
        return false;
      }
    }

    // Clear source tiles.
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 2; j++) {
        setGameboardState(sourceCoordinates[i][0], sourceCoordinates[i][1], BLANK_TILE);
      }
    }

    // Since the source is cleared the piece can only be moved if all target tiles are blank.
    if (locationObstructed(targetCoordinates)) {

      // If location is blocked, write piece back to source location.
      for (int i = 0; i < 4; i++) {
        for (int j = 0; j < 2; j++) {
          setGameboardState(sourceCoordinates[i][0], sourceCoordinates[i][1], currentTetrominoTile);
        }
      }
      // System.out.println("false, collision");
      return false;

    } else {
      // If location is clear, write piece back to target location
      // Could be controlled using a 2nd parameter if you only wanted the return value, not the
      // movement.
      copySIArray(targetCoordinates, nextTetrominoCoordinates);
      // System.out.println("true");
      return true;
    }
  } // End checkCollision METHOD

  /**
   * Determines wether a given tetromino can be drawn at the location.
   *
   * @return <code>true</code> if the entire tetromino does not collide at any point.
   */
  private boolean locationObstructed(int[][] targetLocation) {
    for (int i = 0; i < 4; i++) {

      if (getGameboardState(targetLocation[i][0], targetLocation[i][1]) != BLANK_TILE) {
        return true;
      }
    }
    return false;
  } // End locationObstructed METHOD

  /** Clears full lines, calls dropLines(..) to collapse above rows. */
  private boolean findCompleteRows() {

    boolean rowComplete = true;
    boolean ans = false;
    int collapsedRows = 0;

    for (int row = board_height - 1; row > -1; ) {
      rowComplete = true;
      for (int j = 0; j < board_width; j++) {
        if (getGameboardState(j, row) == BLANK_TILE) {
          row--;
          rowComplete = false;
          break;
        }
      }
      if (rowComplete) {
        // Enter if the row is complete
        ans = true;
        collapsedRows++;
        collapseIntoRow(row);
      }
    }
    score = score + 10 * collapsedRows;
    return ans;
  } // End dropLines METHOD

  /**
   * Move all rows about the given row one step down.
   *
   * @param row the row to collapse into.
   */
  private void collapseIntoRow(int row) {

    for (; row > 1; row--) { // Drop all rows
      for (int j = 0; j < board_width; j++) { // column
        setGameboardState(
            j,
            row,
            getGameboardState(j, row - 1)); // Get the above tile, write it to the given row.
      }
    }
  } // End dropLines METHOD

  /**
   * Draws a new tetromino at the top centre location.
   *
   * @param arg The shape to draw.
   */
  private void spawnTetromino(Tetromino arg, boolean obsructed) {

    // tetrominoesSpawned++;

    if (arg == Tetromino.I) {
      nextTetrominoCoordinates[0][0] = getGameboardSize().width / 2;
      nextTetrominoCoordinates[0][1] = 0;
      nextTetrominoCoordinates[1][0] = getGameboardSize().width / 2;
      nextTetrominoCoordinates[1][1] = 1;
      nextTetrominoCoordinates[2][0] = getGameboardSize().width / 2;
      nextTetrominoCoordinates[2][1] = 2;
      nextTetrominoCoordinates[3][0] = getGameboardSize().width / 2;
      nextTetrominoCoordinates[3][1] = 3;
      currentTetrominoTile = I_TILE;
    } else if (arg == Tetromino.O) {
      nextTetrominoCoordinates[0][0] = getGameboardSize().width / 2;
      nextTetrominoCoordinates[0][1] = 0;
      nextTetrominoCoordinates[1][0] = getGameboardSize().width / 2 - 1;
      nextTetrominoCoordinates[1][1] = 0;
      nextTetrominoCoordinates[2][0] = getGameboardSize().width / 2;
      nextTetrominoCoordinates[2][1] = 1;
      nextTetrominoCoordinates[3][0] = getGameboardSize().width / 2 - 1;
      nextTetrominoCoordinates[3][1] = 1;
      currentTetrominoTile = O_TILE;
    } else if (arg == Tetromino.T) {
      nextTetrominoCoordinates[0][0] = getGameboardSize().width / 2;
      nextTetrominoCoordinates[0][1] = 0;
      nextTetrominoCoordinates[1][0] = getGameboardSize().width / 2 - 1;
      nextTetrominoCoordinates[1][1] = 1;
      nextTetrominoCoordinates[2][0] = getGameboardSize().width / 2;
      nextTetrominoCoordinates[2][1] = 1;
      nextTetrominoCoordinates[3][0] = getGameboardSize().width / 2 + 1;
      nextTetrominoCoordinates[3][1] = 1;
      currentTetrominoTile = T_TILE;
    } else if (arg == Tetromino.J) {
      nextTetrominoCoordinates[0][0] = getGameboardSize().width / 2 - 1;
      nextTetrominoCoordinates[0][1] = 0;
      nextTetrominoCoordinates[1][0] = getGameboardSize().width / 2 - 1;
      nextTetrominoCoordinates[1][1] = 1;
      nextTetrominoCoordinates[2][0] = getGameboardSize().width / 2;
      nextTetrominoCoordinates[2][1] = 1;
      nextTetrominoCoordinates[3][0] = getGameboardSize().width / 2 + 1;
      nextTetrominoCoordinates[3][1] = 1;
      currentTetrominoTile = J_TILE;
    } else if (arg == Tetromino.L) {
      nextTetrominoCoordinates[0][0] = getGameboardSize().width / 2 + 1;
      nextTetrominoCoordinates[0][1] = 0;
      nextTetrominoCoordinates[1][0] = getGameboardSize().width / 2 - 1;
      nextTetrominoCoordinates[1][1] = 1;
      nextTetrominoCoordinates[2][0] = getGameboardSize().width / 2;
      nextTetrominoCoordinates[2][1] = 1;
      nextTetrominoCoordinates[3][0] = getGameboardSize().width / 2 + 1;
      nextTetrominoCoordinates[3][1] = 1;
      currentTetrominoTile = L_TILE;
    } else if (arg == Tetromino.S) {
      nextTetrominoCoordinates[0][0] = getGameboardSize().width / 2;
      nextTetrominoCoordinates[0][1] = 0;
      nextTetrominoCoordinates[1][0] = getGameboardSize().width / 2 + 1;
      nextTetrominoCoordinates[1][1] = 0;
      nextTetrominoCoordinates[2][0] = getGameboardSize().width / 2;
      nextTetrominoCoordinates[2][1] = 1;
      nextTetrominoCoordinates[3][0] = getGameboardSize().width / 2 - 1;
      nextTetrominoCoordinates[3][1] = 1;
      currentTetrominoTile = S_TILE;
    } else if (arg == Tetromino.Z) {
      nextTetrominoCoordinates[0][0] = getGameboardSize().width / 2;
      nextTetrominoCoordinates[0][1] = 0;
      nextTetrominoCoordinates[1][0] = getGameboardSize().width / 2 - 1;
      nextTetrominoCoordinates[1][1] = 0;
      nextTetrominoCoordinates[2][0] = getGameboardSize().width / 2;
      nextTetrominoCoordinates[2][1] = 1;
      nextTetrominoCoordinates[3][0] = getGameboardSize().width / 2 + 1;
      nextTetrominoCoordinates[3][1] = 1;
      currentTetrominoTile = Z_TILE;
    } else {
      throw new IllegalArgumentException();
    }

    if (obsructed) {
      currentTetrominoTile = BLACK_TILE;
    }
  } // End spawnTetromino METHOD

  /**
   * Commits the coordinates nextTetrominoCoordinates to the board. Assumes given values are valid.
   *
   * @param clear Clears location of the previous shape if <code>true</code>.
   * @param nextTetrominoCoordinates The coordinates to write.
   */
  private void writeTetromino(boolean clear) {

    // Clear old shape
    if (clear) {
      for (int i = 0; i < 4; i++) {
        setGameboardState(
            currentTetrominoCoordinates[i][0], currentTetrominoCoordinates[i][1], BLANK_TILE);
      }
    }
    // draw shape at new location
    for (int i = 0; i < 4; i++) {
      setGameboardState(
          nextTetrominoCoordinates[i][0], nextTetrominoCoordinates[i][1], currentTetrominoTile);
      currentTetrominoCoordinates[i][0] = nextTetrominoCoordinates[i][0];
      currentTetrominoCoordinates[i][1] = nextTetrominoCoordinates[i][1];
    }
  } // End METHOD writeTetromino

  /**
   * Copies the elements of a shapeIndex array.
   *
   * @param src Source array.
   * @param dest Target array.
   */
  private void copySIArray(int[][] src, int[][] dest) {
    for (int i = 0; i < 4; i++) {
      dest[i][0] = src[i][0]; // x value
      dest[i][1] = src[i][1]; // y value
    }
  }

  //	//SOUND
  //	private static AudioInputStream themeStream;
  //	private static Clip themeClip;
  //    public static void playTheme(boolean start){
  //	    try{
  //	    	themeStream = AudioSystem.getAudioInputStream(new File("theme.wav").getAbsoluteFile());
  //	        themeClip = AudioSystem.getClip();
  //	        themeClip.open(themeStream);
  //	        //tycker det l�ter alldeles f�r mycket om den spelar ljud �ven d� man inte hittar n�got
  //	        if (start){
  //	        	themeClip.start();
  //	        } else {
  //	        	themeClip.stop();
  //	        }
  //
  //	    }catch(Exception ex){
  //	        ex.printStackTrace();
  //	    }
  //    }

  public static enum Tetromino {
    I(0),
    O(1),
    T(2),
    J(3),
    L(4),
    S(5),
    Z(6);
    public int index = 0;

    Tetromino(int i) {
      this.index = i;
    }

    public static final Tetromino getTetromino(int i) {
      switch (i) {
        case 0:
          return I;
        case 1:
          return O;
        case 2:
          return T;
        case 3:
          return J;
        case 4:
          return L;
        case 5:
          return S;
        case 6:
          return Z;
        default:
          throw new IllegalArgumentException("Arguments [0, 6] permitted.");
      }
    }
  } // End ENUM Shape
} // End TetrisModel CLASS

// Rejected solution, all other methods have been rewritten to use
// moveTetrominoIfPositionClear(int[][], int[][]).

/// **
// * Deterimines wether the current piece will collide. Returns <code>false</code> can move as
// requested.
// * Works regardless of tetromino orientation.
// *
// * @param deltaX The requested change along the x-axis.
// * @param deltaY The requsted change along the y-axis.
// * @return <code>true</code> if performing the requested position change would cause a collision
// or move the piece out of bounds.
// */
// private boolean tetrominoCollision (int deltaX, int deltaY, int[][] arg){
//
//	for (int i = 0; i < 4; i++ ){
//		if ( (arg[i][1]+deltaY > board_height-1 ||
//				arg[i][1]+deltaY < 0 ||
//				arg[i][0]+deltaX > board_width-1 ||
//				arg[i][0]+deltaX < 0) ){
//			return true;
//		}
//
//		if (getGameboardState(arg[i][0]+deltaX, arg[i][1]+deltaY) != BLANK_TILE){
////			if (tetrominoesSpawned>1 && currentTetrominoIndex[0][1] > 2){
////				System.out.println("nu!");
////			}
//			for (int j = 0; j < 4; j++){
//				if ( deltaY != 0 && arg[i][0] == currentTetrominoCoordinates[j][0] && arg[i][1]+deltaY ==
// currentTetrominoCoordinates[j][1] ){ //deltaY != 0 &&
//					//The shape collided with itself in the offending position, exit current iteration on i.
//					break;
//				}
//				if ( deltaX != 0 && arg[i][1] == currentTetrominoCoordinates[j][1] && arg[i][0]+deltaX ==
// currentTetrominoCoordinates[j][0] ){ //deltaX != 0 &&
//					//The shape collided with itself in the offending position, exit current iteration on i.
//					break;
//				}
//				if (j>=3){
//					//The shape did not find itself in the offending position, return true.
//					return true;
//				}
//
//			}
//		}
//	}
//	//
//	return false;
// } // end tetrominoCollision METHOD
