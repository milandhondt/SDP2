package domain;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity representing a file attached to a maintenance record. Contains
 * metadata and the binary content of the file.
 */
@Entity
@Getter
@Setter
@Table(name = "maintenance_files")
public class FileInfo implements Serializable
{

	private static final long serialVersionUID = 1L;

	/**
	 * Unique identifier for the file, auto-generated.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	/**
	 * Name of the file (e.g., filename including extension).
	 */
	public String name;

	/**
	 * MIME type of the file (e.g., "application/pdf").
	 */
	public String type;

	/**
	 * The binary content of the file.
	 */
	@Lob
	private byte[] content;

	/**
	 * Timestamp when the file was uploaded.
	 */
	private LocalDateTime uploadDate;

	/**
	 * Size of the file content in bytes.
	 */
	private long size;

	/**
	 * The maintenance entity this file is associated with.
	 */
	@ManyToOne
	@JoinColumn(name = "maintenance_id")
	private Maintenance maintenance;

	/**
	 * Default constructor initializing uploadDate to current timestamp.
	 */
	public FileInfo()
	{
		this.uploadDate = LocalDateTime.now();
	}

	/**
	 * Constructs a new FileInfo instance with the given file details. Sets the
	 * upload date to the current timestamp and calculates the size.
	 *
	 * @param name        the file name
	 * @param type        the MIME type of the file
	 * @param content     the binary content of the file
	 * @param maintenance the associated maintenance record
	 */
	public FileInfo(String name, String type, byte[] content, Maintenance maintenance)
	{
		this.name = name;
		this.type = type;
		this.content = content;
		this.maintenance = maintenance;
		this.uploadDate = LocalDateTime.now();
		this.size = content != null ? content.length : 0;
	}

	/**
	 * Returns a string representation of the file info.
	 *
	 * @return a string describing the file name and type
	 */
	@Override
	public String toString()
	{
		return "FileInfo{" + "name='" + name + '\'' + ", type='" + type + '\'' + '}';
	}
}
