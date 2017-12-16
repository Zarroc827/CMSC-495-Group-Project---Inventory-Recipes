package api;

import database.*;
import gui.RestaurantApp;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Name: Dustin Summers
 * Date: 11/06/2017
 * Description:
 *
 * Controller for GUI.
 *
 */
public class RecipeFinderController {

    private generateMenu myMenu;

    @FXML
    private BorderPane borderPane;

    @FXML
    private Pane recipePane;

    @FXML
    private javafx.scene.control.Button findRecipesButton, getDirectionsButton, generateMenuButton;

    private Message message = new Message();
    private ArrayList<RecipeObject> recipes;
    private ArrayList<IngredientObject> ingredients;
    private final String APP_ID = "342f012e&";
    private final String APP_KEY = "759ed5ecf4b779bded17abc45600d7e8&";

    public RecipeFinderController(){}


    /**
     * This class is called automatically upon initialization of the controller class
     */
    @FXML
    public void syncIngredients(){
        toggleButtons(true);
        System.out.println("Initiate controller");
        ArrayList<IngredientObject> ingredients = buildInventoryList();
        if(ingredients == null){
            System.out.println("Currently no ingredients available");
        } else {
            showIngredientList(ingredients);
            toggleButtons(false);
        }
    }

    /**
     *
     * @return
     */
    private void toggleButtons(boolean toggle){
        findRecipesButton.setDisable(toggle);
        getDirectionsButton.setDisable(toggle);
        generateMenuButton.setDisable(toggle);
    }

    private ArrayList<IngredientObject> buildInventoryList(){
        ingredients = new ArrayList<>();

        RestaurantApp app = RestaurantApp.getInstance();

        InventoryTransaction inventoryTransaction = new InventoryTransaction(app.getSessionManager(), app.getUser());

        List<Inventory> inventoryList = inventoryTransaction.list();
        System.out.println("Inventory List Size: " + inventoryList.size());
        for (int i = 0; i < inventoryList.size(); i++) {
            String ingredientName = inventoryList.get(i).getName();
            ingredients.add(new IngredientObject(ingredientName));
        }

        return ingredients;
    }

    /**
     * On First display of the pane, will show what we currently have in inventory.
     */
    private void showIngredientList(ArrayList<IngredientObject> ingredients){
        recipePane.getChildren().clear();
        System.out.println("We are showing the list now!");
        final SwingNode swingNode = new SwingNode();
        showSwingNodeIngredientList(swingNode, ingredients);
        recipePane.getChildren().add(swingNode);
        borderPane.setCenter(recipePane);
    }

    /**
     * This is a weird "java" hack that I needed to do in order to properly display my Custom object's elements
     *
     * @param swingNode
     */
    private void showSwingNodeIngredientList(final SwingNode swingNode, ArrayList<IngredientObject> ingredients){

        SwingUtilities.invokeLater(() -> {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            for(int i = 0; i < ingredients.size(); i++){
                panel.add(ingredients.get(i).getIngredientJPanelObject());
            }
            JScrollPane scrollPane = new JScrollPane(panel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setMaximumSize(new Dimension(350, 330));
            swingNode.setContent(scrollPane);
        });
    }


    /**
     * findRecipes()
     * Button Controller.  Couple of steps here:
     * 1) Check To make sure we have internet
     * 2) If we have internet, pull in the recipe list from our desired URL.
     * <p>
     */
    @FXML
    private void findRecipes() {
        findRecipesButton.setDisable(true);

        String searchRecipeURL = "https://api.yummly.com/v1/api/recipes?_app_id="+APP_ID+"_app_key="+APP_KEY+"q=";
        StringBuilder ingredientString = new StringBuilder();
        int j = 0;
        for (int i = 0; i < ingredients.size(); i++) {
            if(ingredients.get(i).isCheckBoxChecked()){
                if(j==0){
                    j++;
                    ingredientString = new StringBuilder(ingredients.get(i).getIngredientTitle());
                } else {
                    ingredientString.append("+").append(ingredients.get(i).getIngredientTitle());
                }
            }

        }
        searchRecipeURL += ingredientString;

        //Send up the recipeURL!
        recipes = RecipesQuery.extractRecipes(searchRecipeURL);
        System.out.println("Passing in RecipeURL: " + searchRecipeURL);

        //Ensure Recipes were returned
         if (recipes.size() == 0) {
             System.out.println("No recipes returned!");
             message.showMessage("No Recipes", "No Recipes Returned", "No Recipes based off of stock!  Acquire more items!", Alert.AlertType.ERROR);
         } else {
             displayRecipes(recipes);
         }
    }

    /**
     * Launches the other stage to display the recipes generated from our current inventory
     *
     * From this, I will generate a separate window, not relying on JavaFX so that I can retain
     * a reference to the RecipesArrayList/Object that I setup in this phase...
     *
     * @param recipes
     */
    private void displayRecipes (ArrayList < RecipeObject > recipes) {
        recipePane.getChildren().clear();
        final SwingNode swingNode = new SwingNode();
        createSwingNodeContent(swingNode, recipes);
        recipePane.getChildren().add(swingNode);
        borderPane.setCenter(recipePane);
    }

    /**
     * This is a weird "java" hack that I needed to do in order to properly display my Custom object's elements
     *
     * @param swingNode
     * @param recipes
     */
    private void createSwingNodeContent(final SwingNode swingNode, ArrayList<RecipeObject> recipes){
        SwingUtilities.invokeLater(() -> {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            for(int i = 0; i < recipes.size(); i++){
                panel.add(recipes.get(i).getRecipeJPanelObject());
            }
            JScrollPane scrollPane = new JScrollPane(panel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setMaximumSize(new Dimension(350, 330));
            swingNode.setContent(scrollPane);
        });
    }

    /**
     * Launches web-page based off of selected recipes
     */
    @FXML
    private void getDirections(){
        System.out.println("Get recipes pushed");
        //Pull in the Recipes from our ArrayList RecipeList
        for(int i = 0; i<recipes.size(); i++){
            if(recipes.get(i).isCheckBoxChecked()) {
                //For each Box that is checked, we need to make a separate RecipeID request
                String getRecipeURL = "https://api.yummly.com/v1/api/recipe/"+recipes.get(i).getRecipeID()+"?_app_id="+APP_ID+"_app_key="+APP_KEY;

                //Send up the getRecipeURL
                System.out.println("Passing in RecipeURL: " + getRecipeURL);
                String directionsURL = RecipesQuery.extractDirectionsURL(getRecipeURL);

                if(Desktop.isDesktopSupported()){
                    try {
                        Desktop.getDesktop().browse(new URI(directionsURL));
                    } catch (IOException | URISyntaxException e) {
                        e.printStackTrace();
                    }
                } else{
                    message.showMessage("Error", "Unable to Open Browser",
                            "Your Operating System is not supported.  Please browse to the following website to view the directions" + directionsURL,
                            Alert.AlertType.INFORMATION);
                }
            }
        }
    }


    /**
     * Generates a menu based off of selected items
     */
    @FXML
    private void generateMenu(){
        System.out.println("Generate menu pressed!");
        myMenu = new generateMenu();
        myMenu.setVisible(true);
        myMenu.setSize(400,600);
        myMenu.setLayout(new FlowLayout());

        for(int i = 0; i<recipes.size(); i++){
            if(recipes.get(i).isCheckBoxChecked()) {
                myMenu.addItem(myMenu, recipes.get(i).getRecipeTitle());
            }
        }


    }
}
