package ch.makery.address;

import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.controlsfx.dialog.Dialogs;

import ch.makery.address.model.Person;
import ch.makery.address.model.PersonListWrapper;
import ch.makery.address.view.BirthdayStatisticsController;
import ch.makery.address.view.PersonEditController;
import ch.makery.address.view.PersonOverviewController;
import ch.makery.address.view.RootLayoutController;

public class MainApp extends Application {

	private static final String ADRESS_APP_TITLE = "AdressApp";
	private static final String FILE_PATH_PREF = "filePath";
	private Stage primaryStage;
	private BorderPane rootLayout;

	private ObservableList<Person> personData = FXCollections
			.observableArrayList();

	public MainApp() {
		personData.add(new Person("Hand", "Mustermann"));
		personData.add(new Person("Ruth", "Mueller"));
		personData.add(new Person("Heinz", "Kurz"));
		personData.add(new Person("Cornelia", "Meier"));
		personData.add(new Person("Christian", "Hausmeister"));
		personData.add(new Person("Dennis", "Schwarz"));
		personData.add(new Person("Michael", "Weiss"));

	}

	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle(ADRESS_APP_TITLE);

		// Set the application icon.
		this.primaryStage.getIcons().add(
				new Image("file:resources/images/address_book_32.png"));

		initRootLayout();
		showPersonOverview();
	}

	public ObservableList<Person> getPersonData() {
		return personData;
	}

	public File getPersonFilePath() {
		Preferences prefs = Preferences.userNodeForPackage(MainApp.class);
		String filePath = prefs.get(FILE_PATH_PREF, null);
		if (filePath != null) {
			return new File(filePath);
		} else {
			return null;
		}
	}

	public void setPersonFilePath(File file) {
		Preferences prefs = Preferences.userNodeForPackage(MainApp.class);
		if (file != null) {
			prefs.put(FILE_PATH_PREF, file.getPath());

			// Update the stage title
			primaryStage.setTitle(ADRESS_APP_TITLE + " - " + file.getName());
		} else {
			prefs.remove(FILE_PATH_PREF);

			// Update the stage title.
			primaryStage.setTitle(ADRESS_APP_TITLE);
		}
	}

	private void initRootLayout() {
		try {
			// Load root layout from fxml file.
			FXMLLoader loader = new FXMLLoader();
            MainApp.class.getResource("view/RootLayout.fxml");
            loader.setLocation(MainApp.class
					.getResource("view/RootLayout.fxml"));
			rootLayout = (BorderPane) loader.load();

			RootLayoutController controller = loader.getController();
			controller.setMainApp(this);

			// Show the scene containing the root layout.
			Scene scene = new Scene(rootLayout);
			primaryStage.setScene(scene);
			primaryStage.show();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void showBirthdayStatistics(){
		try{
			// Load the fxml file nad create a new stage for the popup.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("view/BirthdayStatistics.fxml"));
	        AnchorPane page = (AnchorPane) loader.load();
	        Stage dialogStage = new Stage();
	        dialogStage.setTitle("Birthday Statistics");
	        dialogStage.initModality(Modality.WINDOW_MODAL);
	        dialogStage.initOwner(primaryStage);
	        Scene scene = new Scene(page);
	        dialogStage.setScene(scene);

	        // Set the persons into the controller.
	        BirthdayStatisticsController controller = loader.getController();
	        controller.setPersonData(personData);

	        dialogStage.show();
		} catch (IOException e){
			e.printStackTrace();
		}
	}

	private void showPersonOverview() {
		try {
			// Load PersonOverview.
			FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class
					.getResource("view/PersonOverview.fxml"));
			AnchorPane personOverview = (AnchorPane) loader.load();

			// Set person overview into the center of root layout.
			rootLayout.setCenter(personOverview);

			PersonOverviewController controller = loader.getController();
			controller.setMainApp(this);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean showPersonEditDialog(Person person) {
		try {
			FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class
					.getResource("view/PersonEditDialog.fxml"));
			AnchorPane page = (AnchorPane) loader.load();

			// Create the dialog Stage.
			Stage dialogStage = new Stage();
			dialogStage.setTitle("Edit Person");
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(primaryStage);

			Scene scene = new Scene(page);
			dialogStage.setScene(scene);

			// Set the person into the controller.
			PersonEditController controller = loader.getController();
			controller.setDialogeStage(dialogStage);
			controller.setPerson(person);

			// Show the dialog and wait until the user closes it
			dialogStage.showAndWait();

			return controller.isOkClicked();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void loadPersonDataFromFile(File file) {
		try {
            JAXBContext context = JAXBContext
					.newInstance(PersonListWrapper.class);
			Unmarshaller um = context.createUnmarshaller();

			// Reading XML from the file and unmarshalling.
			PersonListWrapper wrapper = (PersonListWrapper) um.unmarshal(file);

			personData.clear();
			personData.addAll(wrapper.getPersons());

			// Save the file path to the registry
			setPersonFilePath(file);

		} catch (Exception e) { // catches ANY exception
            Dialogs.create()
                    .title("Error")
                    .masthead(
                            "Could not load data from file:\n" + file.getPath())
					.showException(e);

		}
	}

	public void savePersonDataToFile(File file){
		try {
            JAXBContext context = JAXBContext
					.newInstance(PersonListWrapper.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			// Wrapping our person data.
			PersonListWrapper wrapper = new PersonListWrapper();
			wrapper.setPersons(personData);

			// Marshalling and saving XML to the file
			m.marshal(wrapper, file);

			// Save the file path to the registry.
			setPersonFilePath(file);

		} catch (Exception e){ // catches ANY Exception
            Dialogs.create().title("Error")
                    .masthead("Could not save data to file:\n" + file.getPath())
			.showException(e);
		}
	}

	/**
	 * Returns the main stage
	 *
	 * @return
	 */
	public Stage getPrimaryStage() {
		return primaryStage;
	}

	public static void main(String[] args) {
		launch(args);
	}
}
