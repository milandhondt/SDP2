module be.hogent.Java_G12_SDP_22
{
	requires javafx.controls;
	requires jakarta.persistence;
	requires lombok;
	requires org.kordamp.ikonli.javafx;
	requires org.kordamp.ikonli.bootstrapicons;
	requires org.apache.pdfbox;
	requires javafx.swing;
	requires javafx.media;
	requires de.mkammerer.argon2.nolibs;
	requires javafx.base;
	requires javafx.graphics;

	exports domain;
	exports main;
	exports dto;
	exports util;
	exports interfaces;

	opens images;
	opens css;

	opens domain to org.eclipse.persistence.core, jakarta.persistence;
}