package jp.seraphyware.example.ui;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

import org.controlsfx.control.spreadsheet.GridBase;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetCellType;
import org.controlsfx.control.spreadsheet.SpreadsheetView;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import jp.seraphyware.example.util.AbstractWindowController;
import jp.seraphyware.example.util.CDIFXMLLoader;
import jp.seraphyware.example.util.ErrorDialogUtils;
import jp.seraphyware.example.util.MessageResource;

@Dependent
public class SysPropsWnd extends AbstractWindowController implements Initializable {

	@Inject
	@CDIFXMLLoader
	private Instance<FXMLLoader> ldrProvider;
	
	@Inject
	@MessageResource
	private ResourceBundle resources;

	@FXML
	private StackPane pnlCenter;
	
	@Override
	protected void makeRoot() {
		FXMLLoader ldr = ldrProvider.get();
		try {
			ldr.setController(this);
			ldr.setLocation(getClass().getResource("SysPropsWnd.fxml")); //$NON-NLS-1$
			setRoot(ldr.load());

		} catch (IOException ex) {
			throw new UncheckedIOException(ex);

		} finally {
			ldrProvider.destroy(ldr);
		}
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		TreeMap<String, String> sysProps = new TreeMap<>();
		for (String name : System.getProperties().stringPropertyNames()) {
			String value = System.getProperty(name);
			sysProps.put(name, value);
		}
		
	    double defaultWidth = 200;
	    GridBase grid = new GridBase(sysProps.size(), 2);

        ObservableList<ObservableList<SpreadsheetCell>> rows = FXCollections.observableArrayList();
        int row = 0;
        for (Map.Entry<String, String> entry : sysProps.entrySet()) {
        	String name = entry.getKey();
        	String value = entry.getValue();

            SpreadsheetCell nameCell = SpreadsheetCellType.STRING.createCell(row, 0, 1, 1, name);
            SpreadsheetCell valueCell = SpreadsheetCellType.STRING.createCell(row, 1, 1, 1, value);

            ObservableList<SpreadsheetCell> cells = FXCollections.observableArrayList();
            cells.addAll(nameCell, valueCell);
            
            rows.add(cells);
        	row++;
        }
        grid.setRows(rows);

        List<String> columnHeaders = grid.getColumnHeaders();
        columnHeaders.add("name");
        columnHeaders.add("value");

        SpreadsheetView sheet = new SpreadsheetView(grid);
        sheet.getColumns().stream().filter((column) -> (column.isColumnFixable())).forEach((column) -> {
            column.setPrefWidth(defaultWidth);
        });

        pnlCenter.getChildren().add(sheet);
	}

	@Override
	protected Stage createStage() {
		Stage stg = super.createStage();
		stg.setTitle(resources.getString("sysPropsWnd.title"));
		return stg;
	}
	
	@Override
	public void onCloseRequest(WindowEvent event) {
		onClose();
	}
	
	@FXML
	protected void onClose() {
		closeWindow();
	}
	
	public void showAndWait() {
		Stage stg = getStage();
		stg.initModality(Modality.WINDOW_MODAL);
		stg.showAndWait();
	}
	
}
