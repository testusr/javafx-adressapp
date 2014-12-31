package ch.makery.address.view;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import org.controlsfx.dialog.Dialogs;

import ch.makery.address.MainApp;
import ch.makery.address.model.Person;
import ch.makery.address.util.DateUtil;

public class PersonOverviewController {
	@FXML
	private TableView<Person> personTable;
	@FXML
	private TableColumn<Person, String> firstNameColumn;
	@FXML
	private TableColumn<Person, String> lastNameColumn;

	@FXML
	private Label firstNameLabel;
	@FXML
	private Label lastNameLabel;
	@FXML
	private Label streetLabel;
	@FXML
	private Label postalCodeLabel;
	@FXML
	private Label cityLabel;
	@FXML
	private Label birthdayLabel;

	private MainApp mainApp;

	/**
	 * The constructor is called before the initialize method
	 */
	public PersonOverviewController() {
	};

	@FXML
	private void initialize() {
		// Initialize the person table with the two columns.
		firstNameColumn.setCellValueFactory(cellData -> cellData.getValue()
				.firstNameProperty());
		lastNameColumn.setCellValueFactory(cellData -> cellData.getValue()
				.lastNameProperty());

		// Clear Person details
		showPersonDetails(null);

		// Listen fot selection changes and show the person details when changed
		personTable
				.getSelectionModel()
				.selectedItemProperty()
				.addListener(
						(observable, oldValue, newValue) -> showPersonDetails(newValue));
	}

	@FXML
	private void handleDeletePerson() {
		int selectedIndex = personTable.getSelectionModel().getSelectedIndex();
		if (selectedIndex >= 0) {
			personTable.getItems().remove(selectedIndex);
		} else {
			try {
				// Nothing selected.
				Dialogs.create().title("No Selection")
						.masthead("No Person Selected")
						.message("Please select a person in the table.")
						.showWarning();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void showPersonDetails(Person person) {
		if (person != null) {
			this.firstNameLabel.setText(person.getFirstName());
			this.lastNameLabel.setText(person.getLastName());
			this.streetLabel.setText(person.getStreet());
			this.postalCodeLabel
					.setText(String.valueOf(person.getPostalCode()));
			this.cityLabel.setText(person.getCity());
			this.birthdayLabel.setText(DateUtil.format(person.getBirthday()));
		} else {
			this.firstNameLabel.setText("");
			this.lastNameLabel.setText("");
			this.streetLabel.setText("");
			this.postalCodeLabel.setText("");
			this.cityLabel.setText("");
			this.birthdayLabel.setText("");
		}
	}

	/**
	 * Called when the user clicks the new button. Opens a dialog to edit
	 * details for a new person
	 */
	@FXML
	private void handleNewPerson() {
		Person tempPerson = new Person();
		boolean okClicked = mainApp.showPersonEditDialog(tempPerson);
		if (okClicked) {
			mainApp.getPersonData().add(tempPerson);
		}
	}

	@FXML
	private void handleEditPerson() {
		Person selectedPerson = personTable.getSelectionModel()
				.getSelectedItem();
		if (selectedPerson != null) {
			boolean okClicked = mainApp.showPersonEditDialog(selectedPerson);
			if (okClicked) {
				showPersonDetails(selectedPerson);
			}
		} else {
			// Nothing selected
			Dialogs.create().title("No Selection")
					.masthead("No Person Selected")
					.message("Please select a person in the table.")
					.showWarning();
		}
	}

	/**
	 * Is called by the main application to give a reference back to itself
	 * 
	 * @param mainApp
	 */
	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;

		personTable.setItems(mainApp.getPersonData());
	}

}
