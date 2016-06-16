package ZZZ_microtrafficsim.osmcreator.user.controller;

import javafx.scene.Node;
import javafx.scene.input.InputEvent;

/**
 * @author Dominic Parga Cacheiro
 */
public interface UserInputController {
  void transiate(UserEvent userEvent, InputEvent inputEvent, Node clickedNode);
  void setUserState(UserState userState);
}
