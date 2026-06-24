
module MyPart {

	requires javafx.controls;
	requires javafx.fxml;
	requires javafx.graphics;
	requires javafx.base;

	requires java.sql;
	requires mysql.connector.j;

	opens ui to javafx.graphics, javafx.fxml, javafx.base;
	opens model to javafx.base;

	exports ui;
	exports model;
}