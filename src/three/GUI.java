package three;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import two.Die;
import two.LoadedDie;

/**
 * The "game" then proceeds as follows: If the first roll of the die is a 1, the
 * player immediately loses and the game is over. Otherwise the first roll of
 * the die becomes the "point", and the player continues to roll the die. If the
 * player rolls the point value a second time before rolling a 1, the player
 * wins! But if the player rolls a 1 before the point shows up again, the player
 * loses. In either case, the game is over.
 *
 * @author Corey Richardson <richarcm@clarkson.edu>
 * @version 0.1
 * Date: Sat Oct 5 19:05:13 EDT 2013
 */
public class GUI extends Application {
    int num_sides = 0;
    int target_side = 0;
    
    Die die;
    
    @Override
    public void start(final Stage primaryStage) {
        final GUI self = this;
        // It'd be cleaner to do a state machine here, and separate the game
        // logic out into its own class, interacting with it as a state machine.
        // But, Java doesn't have ADTs, so it's not easy to do cleanly.
        primaryStage.setTitle("Dice Game");
        final GridPane grid = new GridPane();
        Label l = new Label("Chose number of sides");
        final TextField tf = new TextField();
        Button ok = new Button("Continue");
        ok.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
                self.num_sides = Integer.parseInt(tf.getText());
                try {
                    Die d = new Die(self.num_sides);
                } catch (IllegalArgumentException e) {
                    Label error = new Label(e.getMessage());
                    grid.add(error, 0, 2);
                    return;
                }
                self.maybe_do_weighted(primaryStage);
            }
        });
        
        grid.add(l, 0, 0);
        grid.add(tf, 0, 1);
        grid.add(ok, 1, 1);
        
        primaryStage.setScene(new Scene(grid, 300, 200));
        primaryStage.show();
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    void start_no_load(final Stage s) {
        this.die = new Die(this.num_sides);
        this.start_game(s);
    }
    
    void prompt_for_loaded(final Stage s) {
        final GUI self = this;
        
        final GridPane grid = new GridPane();
        Label weight_label = new Label("Side to weight");
        Label percent_label = new Label("Percent to weight with");
        final TextField weight_input = new TextField();
        final TextField percent_input = new TextField();
        final Button enter = new Button("Play!");
        
        grid.add(weight_label, 0, 0);
        grid.add(percent_label, 1, 0);
        grid.add(weight_input, 0, 1);
        grid.add(percent_input, 1, 1);
        grid.add(enter, 0, 2);
        
        enter.setOnAction(new EventHandler<ActionEvent>() {
             public void handle(ActionEvent event) {
                try {
                    int side = Integer.parseInt(weight_input.getText());
                    int percent = Integer.parseInt(percent_input.getText());
                    self.die = new LoadedDie(num_sides, side, percent);
                } catch (Exception e) {
                    grid.add(new Label(e.getMessage()), 0, 3);
                    return;
                }
                self.start_game(s);
            }
        });
        
        s.setScene(new Scene(grid, 300, 200));
    }

    void maybe_do_weighted(final Stage s) {
        final GUI self = this;
        
        GridPane grid = new GridPane();
        Label l = new Label("Weight the die?");
        Button yes = new Button("Yes");
        yes.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                self.prompt_for_loaded(s);
            }
        });
        Button no = new Button("No");
        no.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                self.start_no_load(s);
            }
        });
        grid.add(l, 0, 0);
        grid.add(yes, 0, 1);
        grid.add(no, 1, 1);

        s.setScene(new Scene(grid));
    }
    
    void start_game(final Stage s) {
        final GUI self = this;
        GridPane g = new GridPane();
        Label l = new Label("Roll the die for the target: ");
        Button b = new Button("Roll");
        
        b.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                self.target_side = self.die.roll();
                if (self.target_side == 1) {
                    GridPane g = new GridPane();
                    g.add(new Label("Unlucky! First roll was a 1"), 0, 0);
                    s.setScene(new Scene(g));
                    return;
                }
                self.game_main_loop(s);
            }
        });
        g.add(l, 0, 0);
        g.add(b, 1, 0);
        s.setScene(new Scene(g, 300, 200));
    }
    
    void game_main_loop(final Stage s) {
        final GUI self = this;
        final GridPane g = new GridPane();
        
        Label target = new Label("Target: " + Integer.toString(this.target_side));
        Button roll = new Button("Roll");
        
        roll.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                int roll = self.die.roll();
                System.out.println("Rolled a " + roll);
                boolean gameover = false;
                
                GridPane gp2 = new GridPane();
                
                if (roll == 1) {
                    gameover = true;
                    gp2.add(new Label("Game over, you lose!"), 0, 2);
                } else if (roll == self.target_side) {
                    gameover = true;
                    gp2.add(new Label("Game over, you win!"), 0, 2);
                }
                
                if (gameover) {
                    s.setScene(new Scene(gp2, 300, 200));
                } else {
                    GridPane g = new GridPane();
                    // because GridPane won't let us remove the node at a given
                    // coordinate, and we can't store a reference to the label
                    // used as the "Rolled a ..." because any captured upvars
                    // need to be final, reconstruct the grid.
                    //
                    // Ugh.
                    Label target = new Label("Target: " + Integer.toString(self.target_side));
                    Button rollb = new Button("Roll");
                    rollb.setOnAction(this);
                    g.add(target, 0, 0);
                    g.add(rollb, 1, 0);
                    g.add(new Label("Rolled a " + roll), 0, 1);
                    s.setScene(new Scene(g, 300, 200));
                }
            }
        });
        
        g.add(target, 0, 0);
        g.add(roll, 1, 0);
        
        s.setScene(new Scene(g, 300, 200));
    }
    
    void end_game(Stage s) {
        GridPane g = new GridPane();
        g.add(new Label("Thanks for playing!"), 0, 0);
        s.setScene(new Scene(g, 300, 200));
    }
}